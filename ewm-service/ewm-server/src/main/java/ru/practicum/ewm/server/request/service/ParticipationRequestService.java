package ru.practicum.ewm.server.request.service;

import ru.practicum.ewm.server.request.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {

    List<ParticipationRequestDto> getUserRequests(long userId);

    ParticipationRequestDto addParticipationRequest(long userId, long eventId);

    ParticipationRequestDto cancelRequest(long userId, long requestId);
}
