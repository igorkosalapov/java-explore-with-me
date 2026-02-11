package ru.practicum.ewm.server.event.service.publicapi;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.error.exception.NotFoundException;
import ru.practicum.ewm.server.event.controller.publicapi.PublicEventSort;
import ru.practicum.ewm.server.event.dto.EventFullDto;
import ru.practicum.ewm.server.event.dto.EventShortDto;
import ru.practicum.ewm.server.event.mapper.EventMapper;
import ru.practicum.ewm.server.event.model.Event;
import ru.practicum.ewm.server.event.repository.EventRepository;
import ru.practicum.ewm.server.event.repository.EventSpecifications;
import ru.practicum.ewm.server.request.model.RequestStatus;
import ru.practicum.ewm.server.request.repository.EventRequestsCount;
import ru.practicum.ewm.server.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.server.stats.StatsFacade;
import ru.practicum.ewm.server.util.DateTimeUtil;
import ru.practicum.ewm.server.util.OffsetBasedPageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private static final LocalDateTime MIN_STATS_DATE = LocalDateTime.of(2020, 1, 1, 0, 0);

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsFacade statsFacade;

    @Override
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               String rangeStart,
                                               String rangeEnd,
                                               Boolean onlyAvailable,
                                               PublicEventSort sort,
                                               int from,
                                               int size,
                                               String uri,
                                               String ip) {

        statsFacade.saveHit(uri, ip, LocalDateTime.now());

        LocalDateTime start = parseDateSafely(rangeStart);
        LocalDateTime end = parseDateSafely(rangeEnd);
        if (start == null && end == null) {
            start = LocalDateTime.now();
        }
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("rangeStart must be before rangeEnd");
        }

        Specification<Event> spec = Specification.where(EventSpecifications.fetchCategoryAndInitiator())
                .and(EventSpecifications.isPublished())
                .and(EventSpecifications.textContainsIgnoreCase(text))
                .and(EventSpecifications.categoryIn(categories))
                .and(EventSpecifications.paid(paid))
                .and(EventSpecifications.eventDateAfter(start))
                .and(EventSpecifications.eventDateBefore(end));

        if (Boolean.TRUE.equals(onlyAvailable)) {
            spec = spec.and(EventSpecifications.onlyAvailable());
        }

        PublicEventSort actualSort = (sort == null ? PublicEventSort.EVENT_DATE : sort);

        if (actualSort == PublicEventSort.VIEWS) {
            List<Event> events = eventRepository.findAll(spec);
            return mapSortAndSliceByViews(events, from, size);
        }

        OffsetBasedPageRequest page = new OffsetBasedPageRequest(from, size, Sort.by("eventDate").ascending());
        List<Event> events = eventRepository.findAll(spec, page).getContent();
        return mapToShortDtos(events);
    }

    @Override
    public EventFullDto getPublicEvent(long eventId, String uri, String ip) {
        statsFacade.saveHit(uri, ip, LocalDateTime.now());

        Event event = eventRepository.findById(eventId)
                .filter(e -> e.getState() == Event.State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long views = getViewsForUris(List.of(uri)).getOrDefault(uri, 0L);

        return EventMapper.toFullDto(event, confirmed, views);
    }

    private List<EventShortDto> mapSortAndSliceByViews(List<Event> events, int from, int size) {
        List<EventShortDto> dtos = new ArrayList<>(mapToShortDtos(events));
        dtos.sort(Comparator.comparingLong((EventShortDto dto) -> dto.getViews() == null ? 0L : dto.getViews())
                .reversed());

        int startIndex = Math.min(from, dtos.size());
        int endIndex = Math.min(from + size, dtos.size());
        return dtos.subList(startIndex, endIndex);
    }

    private List<EventShortDto> mapToShortDtos(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }

        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedMap = getConfirmedRequestsByEvent(ids);
        Map<String, Long> viewsMap = getViewsForEventIds(ids);

        return events.stream()
                .map(e -> {
                    long confirmed = confirmedMap.getOrDefault(e.getId(), 0L);
                    String eventUri = buildEventUri(e.getId());
                    long views = viewsMap.getOrDefault(eventUri, 0L);
                    return EventMapper.toShortDto(e, confirmed, views);
                })
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getConfirmedRequestsByEvent(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<EventRequestsCount> counts = requestRepository
                .countRequestsByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED);

        Map<Long, Long> result = new HashMap<>();
        for (EventRequestsCount c : counts) {
            result.put(c.getEventId(), c.getRequestsCount());
        }
        return result;
    }

    private Map<String, Long> getViewsForEventIds(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<String> uris = eventIds.stream().map(PublicEventServiceImpl::buildEventUri).toList();
        return getViewsForUris(uris);
    }

    private Map<String, Long> getViewsForUris(List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return Map.of();
        }
        return statsFacade.getViews(MIN_STATS_DATE, LocalDateTime.now(), uris, true);
    }

    private static String buildEventUri(Long eventId) {
        return "/events/" + eventId;
    }

    private static LocalDateTime parseDateSafely(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return DateTimeUtil.parse(value);
    }
}