package ru.practicum.ewm.server.location.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.server.location.dto.LocationDto;
import ru.practicum.ewm.server.location.dto.NewLocationDto;
import ru.practicum.ewm.server.location.service.AdminLocationService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/locations")
public class AdminLocationController {

    private final AdminLocationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationDto add(@Valid @RequestBody NewLocationDto dto) {
        return service.add(dto);
    }

    @GetMapping
    public List<LocationDto> getAll(@RequestParam(defaultValue = "0") @Min(0) int from,
                                   @RequestParam(defaultValue = "10") @Positive int size) {
        return service.getAll(from, size);
    }
}
