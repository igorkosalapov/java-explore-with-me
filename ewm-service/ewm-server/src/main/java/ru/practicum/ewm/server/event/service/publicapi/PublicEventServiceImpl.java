package ru.practicum.ewm.server.event.service.publicapi;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.error.exception.NotFoundException;
import ru.practicum.ewm.server.event.controller.publicapi.PublicEventSort;
import ru.practicum.ewm.server.event.dto.EventFullDto;
import ru.practicum.ewm.server.event.dto.EventShortDto;
import ru.practicum.ewm.server.event.mapper.EventMapper;
import ru.practicum.ewm.server.event.model.Event;
import ru.practicum.ewm.server.event.model.State;
import ru.practicum.ewm.server.event.repository.EventRepository;
import ru.practicum.ewm.server.event.repository.EventSpecifications;
import ru.practicum.ewm.server.event.service.EventMetricsService;
import ru.practicum.ewm.server.stats.StatsFacade;
import ru.practicum.ewm.server.util.DateTimeUtil;
import ru.practicum.ewm.server.util.OffsetBasedPageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private final EventMetricsService metricsService;

    private final EventRepository eventRepository;
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

        OffsetBasedPageRequest page = new OffsetBasedPageRequest(from, size);
        List<Event> events = eventRepository.findAll(spec, page).getContent();
        return mapToShortDtos(events);
    }

    @Override
    public EventFullDto getPublicEvent(long eventId, String uri, String ip) {
        statsFacade.saveHit(uri, ip, LocalDateTime.now());

        Event event = eventRepository.findById(eventId)
                .filter(e -> e.getState() == State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        long confirmed = metricsService.confirmedByEventIds(List.of(eventId)).getOrDefault(eventId, 0L);

        String eventUri = EventMetricsService.eventUri(eventId);
        long views = metricsService.viewsByUris(List.of(eventUri)).getOrDefault(eventUri, 0L);

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
        Map<Long, Long> confirmedMap = metricsService.confirmedByEventIds(ids);
        Map<String, Long> viewsMap = metricsService.viewsByEventIds(ids);

        return events.stream()
                .map(e -> {
                    long confirmed = confirmedMap.getOrDefault(e.getId(), 0L);
                    String eventUri = EventMetricsService.eventUri(e.getId());
                    long views = viewsMap.getOrDefault(eventUri, 0L);
                    return EventMapper.toShortDto(e, confirmed, views);
                })
                .collect(Collectors.toList());
    }

    private static LocalDateTime parseDateSafely(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return DateTimeUtil.parse(value);
    }
}