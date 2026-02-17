package ru.practicum.ewm.server.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.event.dto.EventShortDto;
import ru.practicum.ewm.server.event.mapper.EventMapper;
import ru.practicum.ewm.server.event.model.Event;
import ru.practicum.ewm.server.event.repository.EventRepository;
import ru.practicum.ewm.server.event.service.EventMetricsService;
import ru.practicum.ewm.server.location.model.LocationArea;
import ru.practicum.ewm.server.util.OffsetBasedPageRequest;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationEventsServiceImpl implements LocationEventsService {

    private final AdminLocationService locationService;
    private final EventRepository eventRepository;
    private final EventMetricsService metricsService;

    @Override
    public List<EventShortDto> getPublishedEventsInLocation(long locationId, int from, int size) {
        LocationArea loc = locationService.getOrThrow(locationId);

        OffsetBasedPageRequest page = new OffsetBasedPageRequest(from, size);

        List<Event> events = eventRepository
                .findPublishedInRadius(loc.getLat(), loc.getLon(), loc.getRadiusM(), page)
                .getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmed = metricsService.confirmedByEventIds(ids);
        Map<String, Long> views = metricsService.viewsByEventIds(ids);

        return events.stream()
                .map(e -> {
                    long c = confirmed.getOrDefault(e.getId(), 0L);
                    long v = views.getOrDefault(EventMetricsService.eventUri(e.getId()), 0L);
                    return EventMapper.toShortDto(e, c, v);
                })
                .toList();
    }
}
