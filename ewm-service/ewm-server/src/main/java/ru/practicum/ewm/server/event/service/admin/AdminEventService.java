package ru.practicum.ewm.server.event.service.admin;

import ru.practicum.ewm.server.event.dto.EventFullDto;
import ru.practicum.ewm.server.event.dto.UpdateEventAdminRequest;

import java.util.List;

public interface AdminEventService {

    List<EventFullDto> getEvents(List<Long> users,
                                 List<String> states,
                                 List<Long> categories,
                                 String rangeStart,
                                 String rangeEnd,
                                 int from,
                                 int size);

    EventFullDto updateEvent(long eventId, UpdateEventAdminRequest request);
}
