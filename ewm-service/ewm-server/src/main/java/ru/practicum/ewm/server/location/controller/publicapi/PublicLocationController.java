package ru.practicum.ewm.server.location.controller.publicapi;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.server.location.dto.LocationDto;
import ru.practicum.ewm.server.location.service.AdminLocationService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/locations")
public class PublicLocationController {

    private final AdminLocationService locationService;

    @GetMapping
    public List<LocationDto> getLocations(
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        return locationService.getAll(from, size);
    }
}
