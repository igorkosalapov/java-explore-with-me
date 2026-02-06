package ru.practicum.ewm.server.user.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.server.user.dto.UserShortDto;
import ru.practicum.ewm.server.user.model.User;

@UtilityClass
public class UserMapper {
    public static UserShortDto toShortDto(User user) {
        if (user == null) {
            return null;
        }
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
