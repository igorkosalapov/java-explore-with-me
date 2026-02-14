package ru.practicum.ewm.server.event.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.category.model.Category;
import ru.practicum.ewm.server.category.repository.CategoryRepository;
import ru.practicum.ewm.server.error.exception.ConditionNotMetException;
import ru.practicum.ewm.server.error.exception.NotFoundException;
import ru.practicum.ewm.server.event.dto.EventFullDto;
import ru.practicum.ewm.server.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.server.event.mapper.EventMapper;
import ru.practicum.ewm.server.event.model.Event;
import ru.practicum.ewm.server.event.model.State;
import ru.practicum.ewm.server.event.repository.EventRepository;
import ru.practicum.ewm.server.event.repository.EventSpecifications;
import ru.practicum.ewm.server.event.service.EventMetricsService;
import ru.practicum.ewm.server.util.DateTimeUtil;
import ru.practicum.ewm.server.util.OffsetBasedPageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEventServiceImpl implements AdminEventService {

    private final EventMetricsService metricsService;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<EventFullDto> getEvents(List<Long> users,
                                        List<String> states,
                                        List<Long> categories,
                                        String rangeStart,
                                        String rangeEnd,
                                        int from,
                                        int size) {

        LocalDateTime start = parseDateSafely(rangeStart);
        LocalDateTime end = parseDateSafely(rangeEnd);
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("rangeStart must be before rangeEnd");
        }

        List<State> stateEnums = State.from(states);

        Specification<Event> spec = Specification.where(EventSpecifications.fetchCategoryAndInitiator())
                .and(EventSpecifications.initiatorIn(users))
                .and(EventSpecifications.stateIn(stateEnums))
                .and(EventSpecifications.categoryIn(categories))
                .and(EventSpecifications.eventDateAfter(start))
                .and(EventSpecifications.eventDateBefore(end));

        OffsetBasedPageRequest page = new OffsetBasedPageRequest(from, size);
        List<Event> events = eventRepository.findAll(spec, page).getContent();

        return mapToFullDtos(events);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            event.setCategory(getCategoryOrThrow(request.getCategory()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getLocation() != null) {
            if (request.getLocation().getLat() == null || request.getLocation().getLon() == null) {
                throw new IllegalArgumentException("Location must contain lat and lon");
            }
            event.setLocation(request.getLocation());
        }
        if (request.getEventDate() != null) {
            LocalDateTime newEventDate = DateTimeUtil.parse(request.getEventDate());
            if (newEventDate != null && newEventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new IllegalArgumentException("Event date must be at least 1 hour in the future");
            }
            event.setEventDate(newEventDate);
        }

        if (request.getStateAction() != null) {
            applyStateAction(event, request.getStateAction());
        }

        Event saved = eventRepository.save(event);

        long confirmed = metricsService.confirmedByEventIds(List.of(eventId)).getOrDefault(eventId, 0L);
        String uri = EventMetricsService.eventUri(eventId);
        long views = metricsService.viewsByUris(List.of(uri)).getOrDefault(uri, 0L);

        return EventMapper.toFullDto(saved, confirmed, views);
    }

    private void applyStateAction(Event event, UpdateEventAdminRequest.StateAction action) {
        LocalDateTime now = LocalDateTime.now();

        switch (action) {
            case PUBLISH_EVENT:
                if (event.getState() != State.PENDING) {
                    throw new ConditionNotMetException(
                            "Cannot publish the event because it's not in the right state: " + event.getState());
                }
                if (event.getEventDate() == null || event.getEventDate().isBefore(now.plusHours(1))) {
                    throw new ConditionNotMetException("Event date must be at least 1 hour in the future");
                }
                event.setState(State.PUBLISHED);
                event.setPublishedOn(now);
                break;

            case REJECT_EVENT:
                if (event.getState() == State.PUBLISHED) {
                    throw new ConditionNotMetException(
                            "Cannot reject the event because it's already published");
                }
                event.setState(State.CANCELED);
                break;

            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    private Category getCategoryOrThrow(long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
    }

    private static LocalDateTime parseDateSafely(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return DateTimeUtil.parse(value);
    }

    private List<EventFullDto> mapToFullDtos(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }

        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedMap = metricsService.confirmedByEventIds(ids);
        Map<String, Long> viewsMap = metricsService.viewsByEventIds(ids);

        return events.stream()
                .map(e -> {
                    long confirmed = confirmedMap.getOrDefault(e.getId(), 0L);
                    String uri = EventMetricsService.eventUri(e.getId());
                    long views = viewsMap.getOrDefault(uri, 0L);

                    return EventMapper.toFullDto(e, confirmed, views);
                })
                .toList();
    }
}

