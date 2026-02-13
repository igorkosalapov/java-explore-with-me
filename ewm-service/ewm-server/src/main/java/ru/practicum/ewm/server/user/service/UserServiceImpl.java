package ru.practicum.ewm.server.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.error.exception.ConflictException;
import ru.practicum.ewm.server.error.exception.NotFoundException;
import ru.practicum.ewm.server.user.dto.NewUserRequest;
import ru.practicum.ewm.server.user.dto.UserDto;
import ru.practicum.ewm.server.user.mapper.UserMapper;
import ru.practicum.ewm.server.user.model.User;
import ru.practicum.ewm.server.user.repository.UserRepository;
import ru.practicum.ewm.server.util.OffsetBasedPageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(NewUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("Email must be unique");
        }
        User saved = userRepository.save(UserMapper.toEntity(request));
        return UserMapper.toDto(saved);
    }

    @Override
    public List<UserDto> getAll(List<Long> ids, int from, int size) {
        if (ids != null && !ids.isEmpty()) {
            return userRepository.findAllById(ids).stream()
                    .map(UserMapper::toDto)
                    .toList();
        }
        OffsetBasedPageRequest pageRequest = new OffsetBasedPageRequest(from, size, Sort.by("id").ascending());
        return userRepository.findAll(pageRequest)
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        userRepository.delete(user);
    }
}
