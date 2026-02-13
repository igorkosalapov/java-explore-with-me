package ru.practicum.ewm.server.request.controller.privateapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.server.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.server.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.server.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.server.request.service.ParticipationRequestService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events/{eventId}/requests")
public class PrivateEventParticipationRequestController {

    private final ParticipationRequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getEventParticipants(@PathVariable long userId,
                                                              @PathVariable long eventId) {
        return requestService.getEventParticipants(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable long userId,
                                                              @PathVariable long eventId,
                                                              @Valid @RequestBody EventRequestStatusUpdateRequest body) {
        return requestService.changeRequestStatus(userId, eventId, body);
    }
}
