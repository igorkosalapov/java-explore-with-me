package ru.practicum.ewm.server.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.server.category.mapper.CategoryMapper;
import ru.practicum.ewm.server.event.dto.EventShortDto;
import ru.practicum.ewm.server.event.model.Event;
import ru.practicum.ewm.server.user.mapper.UserMapper;
import ru.practicum.ewm.server.util.DateTimeUtil;

@UtilityClass
public class EventMapper {

    public static EventShortDto toShortDto(Event event) {
        if (event == null) {
            return null;
        }
        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toDto(event.getCategory()))
                .initiator(UserMapper.toShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .eventDate(DateTimeUtil.format(event.getEventDate()))
                .confirmedRequests(0L)
                .views(0L)
                .build();
    }
}
