package ru.practicum.ewm.server.location.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationDto {
    private Long id;
    private String name;
    private Float lat;
    private Float lon;
    private Integer radiusM;
}
