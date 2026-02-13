package ru.practicum.ewm.server.event.controller.privateapi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.server.event.dto.EventFullDto;
import ru.practicum.ewm.server.event.dto.EventShortDto;
import ru.practicum.ewm.server.event.dto.NewEventDto;
import ru.practicum.ewm.server.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.server.event.service.EventService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable long userId,
                                             @RequestParam(defaultValue = "0") @Min(0) int from,
                                             @RequestParam(defaultValue = "10") @Positive int size) {
        return eventService.getUserEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable long userId,
                                 @Valid @RequestBody NewEventDto request) {
        return eventService.addEvent(userId, request);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable long userId,
                                 @PathVariable long eventId) {
        return eventService.getUserEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable long userId,
                                    @PathVariable long eventId,
                                    @Valid @RequestBody UpdateEventUserRequest request) {
        return eventService.updateUserEvent(userId, eventId, request);
    }
}
