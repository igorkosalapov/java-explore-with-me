package ru.practicum.ewm.server.location.service;

import ru.practicum.ewm.server.event.dto.EventShortDto;

import java.util.List;

public interface LocationEventsService {
    List<EventShortDto> getPublishedEventsInLocation(long locationId, int from, int size);
}
