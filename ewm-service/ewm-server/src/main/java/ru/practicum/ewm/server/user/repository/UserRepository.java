package ru.practicum.ewm.server.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.server.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
