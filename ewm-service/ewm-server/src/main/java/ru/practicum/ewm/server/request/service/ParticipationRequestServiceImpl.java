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
import ru.practicum.ewm.server.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.server.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.server.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.server.request.dto.RequestUpdateStatus;
import ru.practicum.ewm.server.request.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.server.request.model.ParticipationRequest;
import ru.practicum.ewm.server.request.model.RequestStatus;
import ru.practicum.ewm.server.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.server.user.model.User;
import ru.practicum.ewm.server.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        request.setCreated(LocalDateTime.now().withNano(0));
        request.setEvent(event);
        request.setRequester(requester);

        boolean moderation = event.getRequestModeration() != null ? event.getRequestModeration() : true;
        if (!moderation || limit == 0) {
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

    @Override
    public List<ParticipationRequestDto> getEventParticipants(long userId, long eventId) {
        getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (event.getInitiator() == null || event.getInitiator().getId() == null
                || !event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        return requestRepository.findAllByEventId(eventId, Sort.by("created").descending())
                .stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(long userId,
                                                             long eventId,
                                                             EventRequestStatusUpdateRequest updateRequest) {
        getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (event.getInitiator() == null || event.getInitiator().getId() == null
                || !event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        List<Long> requestIds = updateRequest.getRequestIds();
        RequestUpdateStatus targetStatus = updateRequest.getStatus();

        List<ParticipationRequest> requests = requestRepository.findAllByIdInAndEventId(requestIds, eventId);
        if (requests.size() != requestIds.size()) {
            Set<Long> found = new HashSet<>();
            for (ParticipationRequest r : requests) {
                found.add(r.getId());
            }
            for (Long id : requestIds) {
                if (!found.contains(id)) {
                    throw new NotFoundException("Request with id=" + id + " was not found");
                }
            }
        }

        for (ParticipationRequest r : requests) {
            if (r.getStatus() != RequestStatus.PENDING) {
                throw new IllegalArgumentException("Request must have status PENDING");
            }
        }

        List<ParticipationRequestDto> confirmedDtos = new ArrayList<>();
        List<ParticipationRequestDto> rejectedDtos = new ArrayList<>();

        if (targetStatus == RequestUpdateStatus.CONFIRMED) {
            int limit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;
            long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

            if (limit > 0 && confirmed + requests.size() > limit) {
                throw new ConditionNotMetException("The participant limit has been reached");
            }

            for (ParticipationRequest r : requests) {
                r.setStatus(RequestStatus.CONFIRMED);
            }
            List<ParticipationRequest> saved = requestRepository.saveAll(requests);
            for (ParticipationRequest r : saved) {
                confirmedDtos.add(ParticipationRequestMapper.toDto(r));
            }

            if (limit > 0 && confirmed + requests.size() >= limit) {
                Set<Long> justConfirmed = new HashSet<>();
                for (ParticipationRequest r : saved) {
                    justConfirmed.add(r.getId());
                }

                List<ParticipationRequest> toReject = requestRepository.findAllByEventIdAndStatus(
                        eventId,
                        RequestStatus.PENDING,
                        Sort.by("created").descending()
                );

                if (!toReject.isEmpty()) {
                    List<ParticipationRequest> reallyReject = new ArrayList<>();
                    for (ParticipationRequest r : toReject) {
                        if (!justConfirmed.contains(r.getId())) {
                            r.setStatus(RequestStatus.REJECTED);
                            reallyReject.add(r);
                        }
                    }
                    if (!reallyReject.isEmpty()) {
                        List<ParticipationRequest> rejectedSaved = requestRepository.saveAll(reallyReject);
                        for (ParticipationRequest r : rejectedSaved) {
                            rejectedDtos.add(ParticipationRequestMapper.toDto(r));
                        }
                    }
                }
            }
        } else {
            for (ParticipationRequest r : requests) {
                r.setStatus(RequestStatus.REJECTED);
            }
            List<ParticipationRequest> saved = requestRepository.saveAll(requests);
            for (ParticipationRequest r : saved) {
                rejectedDtos.add(ParticipationRequestMapper.toDto(r));
            }
        }

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedDtos)
                .rejectedRequests(rejectedDtos)
                .build();
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
