package ru.practicum.ewm.server.user.service;

import ru.practicum.ewm.server.user.dto.NewUserRequest;
import ru.practicum.ewm.server.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(NewUserRequest request);

    List<UserDto> getAll(List<Long> ids, int from, int size);

    void delete(long userId);
}
