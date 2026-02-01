package ru.practicum.stats.server.mapper;

import lombok.Data;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.server.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public final class EndpointHitMapper {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EndpointHitEntity toEntity(EndpointHitDto dto) {
        return EndpointHitEntity.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .hitTime(LocalDateTime.parse(dto.getTimestamp(), FMT))
                .build();
    }
}
