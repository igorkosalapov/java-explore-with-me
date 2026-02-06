package ru.practicum.ewm.server.event.dto;

import lombok.Builder;
import lombok.Value;
import ru.practicum.ewm.server.category.dto.CategoryDto;
import ru.practicum.ewm.server.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Value
@Builder
public class EventShortDto {
    String annotation;
    CategoryDto category;
    long confirmedRequests;
    LocalDateTime eventDate;
    Long id;
    UserShortDto initiator;
    boolean paid;
    String title;
    long views;
}
