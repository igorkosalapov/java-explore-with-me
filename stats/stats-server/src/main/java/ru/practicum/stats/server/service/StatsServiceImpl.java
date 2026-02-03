package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.mapper.EndpointHitMapper;
import ru.practicum.stats.server.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EndpointHitRepository repository;

    @Override
    public void saveHit(EndpointHitDto dto) {
        repository.save(EndpointHitMapper.toEntity(dto));
    }

    @Override
    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        LocalDateTime startDt = LocalDateTime.parse(start, FMT);
        LocalDateTime endDt = LocalDateTime.parse(end, FMT);

        if (startDt.isAfter(endDt)) {
            throw new IllegalArgumentException("start must be before or equal to end");
        }

        boolean hasUris = uris != null && !uris.isEmpty();

        if (!hasUris && !unique) {
            return repository.findStats(startDt, endDt);
        } else if (!hasUris) {
            return repository.findUniqueStats(startDt, endDt);
        } else if (!unique) {
            return repository.findStatsByUris(startDt, endDt, uris);
        } else {
            return repository.findUniqueStatsByUris(startDt, endDt, uris);
        }
    }
}
