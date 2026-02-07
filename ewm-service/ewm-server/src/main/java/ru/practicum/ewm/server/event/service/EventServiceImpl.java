package ru.practicum.ewm.server.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.category.model.Category;
import ru.practicum.ewm.server.category.repository.CategoryRepository;
import ru.practicum.ewm.server.error.exception.ConditionNotMetException;
import ru.practicum.ewm.server.error.exception.NotFoundException;
import ru.practicum.ewm.server.event.dto.EventFullDto;
import ru.practicum.ewm.server.event.dto.EventShortDto;
import ru.practicum.ewm.server.event.dto.NewEventDto;
import ru.practicum.ewm.server.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.server.event.mapper.EventMapper;
import ru.practicum.ewm.server.event.model.Event;
import ru.practicum.ewm.server.event.repository.EventRepository;
import ru.practicum.ewm.server.user.model.User;
import ru.practicum.ewm.server.user.repository.UserRepository;
import ru.practicum.ewm.server.util.DateTimeUtil;
import ru.practicum.ewm.server.util.OffsetBasedPageRequest;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<EventShortDto> getUserEvents(long userId, int from, int size) {
        getUserOrThrow(userId);

        OffsetBasedPageRequest pageRequest = new OffsetBasedPageRequest(from, size, Sort.by("id").ascending());
        return eventRepository.findByInitiatorId(userId, pageRequest)
                .stream()
                .map(EventMapper::toShortDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto addEvent(long userId, NewEventDto dto) {
        User initiator = getUserOrThrow(userId);
        Category category = getCategoryOrThrow(dto.getCategory());

        LocalDateTime eventDate = DateTimeUtil.parse(dto.getEventDate());
        validateEventDateAtLeastTwoHoursFromNow(eventDate);

        LocalDateTime now = LocalDateTime.now();

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setAnnotation(dto.getAnnotation());
        event.setDescription(dto.getDescription());
        event.setCategory(category);
        event.setInitiator(initiator);
        event.setEventDate(eventDate);
        event.setCreatedOn(now);
        event.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
        event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);
        event.setLat(dto.getLocation().getLat());
        event.setLon(dto.getLocation().getLon());
        event.setState(Event.State.PENDING);

        Event saved = eventRepository.save(event);
        return EventMapper.toFullDto(saved);
    }

    @Override
    public EventFullDto getUserEvent(long userId, long eventId) {
        getUserOrThrow(userId);
        Event event = getUserEventOrThrow(userId, eventId);
        return EventMapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(long userId, long eventId, UpdateEventUserRequest dto) {
        getUserOrThrow(userId);
        Event event = getUserEventOrThrow(userId, eventId);

        if (event.getState() == Event.State.PUBLISHED) {
            throw new ConditionNotMetException("Only pending or canceled events can be changed");
        }

        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getCategory() != null) {
            event.setCategory(getCategoryOrThrow(dto.getCategory()));
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getLocation() != null) {
            event.setLat(dto.getLocation().getLat());
            event.setLon(dto.getLocation().getLon());
        }
        if (dto.getEventDate() != null) {
            LocalDateTime newDate = DateTimeUtil.parse(dto.getEventDate());
            validateEventDateAtLeastTwoHoursFromNow(newDate);
            event.setEventDate(newDate);
        }
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(Event.State.PENDING);
                case CANCEL_REVIEW -> event.setState(Event.State.CANCELED);
            }
        }

        Event saved = eventRepository.save(event);
        return EventMapper.toFullDto(saved);
    }

    private void validateEventDateAtLeastTwoHoursFromNow(LocalDateTime eventDate) {
        if (eventDate == null) {
            throw new IllegalArgumentException("eventDate must not be null");
        }
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConditionNotMetException("Event date must be at least 2 hours in the future");
        }
    }

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Category getCategoryOrThrow(long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
    }

    private Event getUserEventOrThrow(long userId, long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }
}
