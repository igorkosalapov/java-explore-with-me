package ru.practicum.ewm.server.user.mapper;

import ru.practicum.ewm.server.user.dto.NewUserRequest;
import ru.practicum.ewm.server.user.dto.UserDto;
import ru.practicum.ewm.server.user.dto.UserShortDto;
import ru.practicum.ewm.server.user.model.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(NewUserRequest dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        return user;
    }

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

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
