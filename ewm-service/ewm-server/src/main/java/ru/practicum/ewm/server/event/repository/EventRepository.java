package ru.practicum.ewm.server.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.server.event.model.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    long countByCategoryId(Long categoryId);

    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);
}
