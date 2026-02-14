package ru.practicum.ewm.server.location.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class NewLocationDto {

    @NotBlank
    private String name;

    @NotNull
    private Float lat;

    @NotNull
    private Float lon;

    @NotNull
    @Positive
    private Integer radiusM;
}
