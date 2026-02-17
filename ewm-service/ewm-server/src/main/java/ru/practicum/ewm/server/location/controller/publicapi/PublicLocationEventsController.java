package ru.practicum.ewm.server.location.controller.publicapi;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.server.event.dto.EventShortDto;
import ru.practicum.ewm.server.location.service.LocationEventsService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/locations")
public class PublicLocationEventsController {

    private final LocationEventsService locationEventsService;

    @GetMapping("/{locationId}/events")
    public List<EventShortDto> getEventsInLocation(@PathVariable @Positive long locationId,
                                                   @RequestParam(defaultValue = "0") @Min(0) int from,
                                                   @RequestParam(defaultValue = "10") @Positive int size) {
        return locationEventsService.getPublishedEventsInLocation(locationId, from, size);
    }
}
