package ru.practicum.ewm.server.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Широта и долгота места проведения события.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @NotNull
    private Float lat;

    @NotNull
    private Float lon;
}
