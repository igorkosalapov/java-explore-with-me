package ru.practicum.ewm.server.compilation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.server.compilation.model.Compilation;

import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    boolean existsByTitleIgnoreCase(String title);

    @Override
    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    Optional<Compilation> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    Page<Compilation> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    Page<Compilation> findAllByPinned(boolean pinned, Pageable pageable);
}
