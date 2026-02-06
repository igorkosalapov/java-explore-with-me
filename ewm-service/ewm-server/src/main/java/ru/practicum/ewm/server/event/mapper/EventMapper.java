package ru.practicum.ewm.server.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.server.category.mapper.CategoryMapper;
import ru.practicum.ewm.server.event.dto.EventShortDto;
import ru.practicum.ewm.server.event.model.Event;
import ru.practicum.ewm.server.user.mapper.UserMapper;

@UtilityClass
public class EventMapper {

    public static EventShortDto toShortDto(Event event, long confirmedRequests, long views) {
        if (event == null) {
            return null;
        }
        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .eventDate(event.getEventDate())
                .paid(event.isPaid())
                .category(CategoryMapper.toDto(event.getCategory()))
                .initiator(UserMapper.toShortDto(event.getInitiator()))
                .confirmedRequests(confirmedRequests)
                .views(views)
                .build();
    }
}
