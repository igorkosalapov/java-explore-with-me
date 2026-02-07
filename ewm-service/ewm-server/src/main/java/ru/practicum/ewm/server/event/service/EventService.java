package ru.practicum.ewm.server.event.service;

import ru.practicum.ewm.server.event.dto.EventFullDto;
import ru.practicum.ewm.server.event.dto.EventShortDto;
import ru.practicum.ewm.server.event.dto.NewEventDto;
import ru.practicum.ewm.server.event.dto.UpdateEventUserRequest;

import java.util.List;

public interface EventService {

    List<EventShortDto> getUserEvents(long userId, int from, int size);

    EventFullDto addEvent(long userId, NewEventDto dto);

    EventFullDto getUserEvent(long userId, long eventId);

    EventFullDto updateUserEvent(long userId, long eventId, UpdateEventUserRequest dto);
}
