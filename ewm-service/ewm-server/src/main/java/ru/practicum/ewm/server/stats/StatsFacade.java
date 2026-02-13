package ru.practicum.ewm.server.stats;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.server.util.DateTimeUtil;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsFacade {

    private final StatsClient statsClient;
    private final String appName;

    public StatsFacade(StatsClient statsClient,
                       @Value("${stats.app:ewm-main-service}") String appName) {
        this.statsClient = statsClient;
        this.appName = appName;
    }

    public void saveHit(String uri, String ip, LocalDateTime timestamp) {
        EndpointHitDto dto = EndpointHitDto.builder()
                .app(appName)
                .uri(uri)
                .ip(ip)
                .timestamp(DateTimeUtil.format(timestamp))
                .build();
        statsClient.hit(dto);
    }

    public Map<String, Long> getViews(LocalDateTime start,
                                      LocalDateTime end,
                                      List<String> uris,
                                      boolean unique) {
        List<ViewStatsDto> stats = statsClient.getStats(
                DateTimeUtil.format(start),
                DateTimeUtil.format(end),
                uris,
                unique
        );

        Map<String, Long> result = new HashMap<>();
        for (ViewStatsDto dto : stats) {
            result.put(dto.getUri(), dto.getHits());
        }
        return result;
    }
}
