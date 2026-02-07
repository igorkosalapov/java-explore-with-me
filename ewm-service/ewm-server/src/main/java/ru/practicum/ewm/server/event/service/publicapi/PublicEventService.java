package ru.practicum.ewm.server.event.service.publicapi;

import ru.practicum.ewm.server.event.controller.publicapi.PublicEventSort;
import ru.practicum.ewm.server.event.dto.EventFullDto;
import ru.practicum.ewm.server.event.dto.EventShortDto;

import java.util.List;

public interface PublicEventService {

    List<EventShortDto> getPublicEvents(String text,
                                        List<Long> categories,
                                        Boolean paid,
                                        String rangeStart,
                                        String rangeEnd,
                                        Boolean onlyAvailable,
                                        PublicEventSort sort,
                                        int from,
                                        int size,
                                        String uri,
                                        String ip);

    EventFullDto getPublicEvent(long eventId, String uri, String ip);
}
