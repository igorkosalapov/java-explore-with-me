package ru.practicum.ewm.server.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.server.request.model.RequestStatus;
import ru.practicum.ewm.server.request.repository.EventRequestsCount;
import ru.practicum.ewm.server.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.server.stats.StatsFacade;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventMetricsService {

    private static final LocalDateTime MIN_STATS_DATE = LocalDateTime.of(2020, 1, 1, 0, 0);

    private final ParticipationRequestRepository requestRepository;
    private final StatsFacade statsFacade;

    public Map<Long, Long> confirmedByEventIds(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Map.of();

        List<EventRequestsCount> counts =
                requestRepository.countRequestsByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED);

        Map<Long, Long> result = new HashMap<>();
        for (EventRequestsCount c : counts) {
            result.put(c.getEventId(), c.getRequestsCount());
        }
        return result;
    }

    public Map<String, Long> viewsByEventIds(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Map.of();
        List<String> uris = eventIds.stream().map(EventMetricsService::eventUri).toList();
        return viewsByUris(uris);
    }

    public Map<String, Long> viewsByUris(Collection<String> uris) {
        if (uris == null || uris.isEmpty()) return Map.of();
        return statsFacade.getViews(MIN_STATS_DATE, LocalDateTime.now(), List.copyOf(uris), true);
    }

    public static String eventUri(long eventId) {
        return "/events/" + eventId;
    }
}
