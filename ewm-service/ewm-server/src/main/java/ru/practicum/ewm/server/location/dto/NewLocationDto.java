package ru.practicum.ewm.server.location.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NewLocationDto {

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "-90.0", inclusive = true)
    @DecimalMax(value = "90.0", inclusive = true)
    private Float lat;

    @NotNull
    @DecimalMin(value = "-180.0", inclusive = true)
    @DecimalMax(value = "180.0", inclusive = true)
    private Float lon;

    @NotNull
    @Positive
    private Integer radiusM;
}
