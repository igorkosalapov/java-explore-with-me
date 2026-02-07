package ru.practicum.ewm.server.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.error.exception.ConditionNotMetException;
import ru.practicum.ewm.server.error.exception.ConflictException;
import ru.practicum.ewm.server.error.exception.NotFoundException;
import ru.practicum.ewm.server.event.model.Event;
import ru.practicum.ewm.server.event.repository.EventRepository;
import ru.practicum.ewm.server.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.server.request.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.server.request.model.ParticipationRequest;
import ru.practicum.ewm.server.request.model.RequestStatus;
import ru.practicum.ewm.server.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.server.user.model.User;
import ru.practicum.ewm.server.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(long userId) {
        getUserOrThrow(userId);
        return requestRepository.findAllByRequesterId(userId, Sort.by("created").descending())
                .stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(long userId, long eventId) {
        User requester = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }

        if (event.getInitiator() != null && event.getInitiator().getId() != null
                && event.getInitiator().getId().equals(userId)) {
            throw new ConditionNotMetException("The initiator cannot request participation in their own event");
        }

        if (event.getState() != Event.State.PUBLISHED) {
            throw new ConditionNotMetException("Event must be published");
        }

        int limit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;
        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (limit > 0 && confirmed >= limit) {
            throw new ConditionNotMetException("The participant limit has been reached");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(requester);

        boolean moderation = event.getRequestModeration() != null ? event.getRequestModeration() : true;
        if (!moderation) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        ParticipationRequest saved = requestRepository.save(request);
        return ParticipationRequestMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        getUserOrThrow(userId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (request.getRequester() == null || request.getRequester().getId() == null
                || !request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " was not found");
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest saved = requestRepository.save(request);
        return ParticipationRequestMapper.toDto(saved);
    }

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Event getEventOrThrow(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }
}
