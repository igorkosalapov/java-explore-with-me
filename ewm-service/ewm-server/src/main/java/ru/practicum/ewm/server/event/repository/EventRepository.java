package ru.practicum.ewm.server.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.server.event.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    long countByCategoryId(Long categoryId);
}
