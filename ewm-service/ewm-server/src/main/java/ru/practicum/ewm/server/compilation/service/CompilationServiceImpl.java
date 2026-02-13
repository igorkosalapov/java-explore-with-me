package ru.practicum.ewm.server.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.compilation.dto.CompilationDto;
import ru.practicum.ewm.server.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.server.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.server.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.server.compilation.model.Compilation;
import ru.practicum.ewm.server.compilation.repository.CompilationRepository;
import ru.practicum.ewm.server.error.exception.ConflictException;
import ru.practicum.ewm.server.error.exception.NotFoundException;
import ru.practicum.ewm.server.event.model.Event;
import ru.practicum.ewm.server.event.repository.EventRepository;
import ru.practicum.ewm.server.util.OffsetBasedPageRequest;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto request) {
        if (compilationRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new ConflictException("Compilation title must be unique");
        }

        Compilation compilation = new Compilation();
        compilation.setTitle(request.getTitle());
        compilation.setPinned(Boolean.TRUE.equals(request.getPinned()));
        compilation.setEvents(resolveEvents(request.getEvents()));

        return CompilationMapper.toDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public CompilationDto update(Long compilationId, UpdateCompilationRequest request) {
        Compilation compilation = getCompilationOrThrow(compilationId);

        if (request.getTitle() != null) {
            boolean titleChanged = !request.getTitle().equalsIgnoreCase(compilation.getTitle());
            if (titleChanged && compilationRepository.existsByTitleIgnoreCase(request.getTitle())) {
                throw new ConflictException("Compilation title must be unique");
            }
            compilation.setTitle(request.getTitle());
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getEvents() != null) {
            compilation.setEvents(resolveEvents(request.getEvents()));
        }

        return CompilationMapper.toDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public void delete(Long compilationId) {
        Compilation compilation = getCompilationOrThrow(compilationId);
        compilationRepository.delete(compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        Pageable pageable = new OffsetBasedPageRequest(from, size);

        Page<Compilation> page;
        if (pinned == null) {
            page = compilationRepository.findAll(pageable);
        } else {
            page = compilationRepository.findAllByPinned(pinned, pageable);
        }

        return page.stream()
                .map(CompilationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long compilationId) {
        return CompilationMapper.toDto(getCompilationOrThrow(compilationId));
    }

    private Compilation getCompilationOrThrow(Long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compilationId + " was not found"));
    }

    private Set<Event> resolveEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Long> uniqueIds = new HashSet<>(eventIds);
        List<Event> events = eventRepository.findAllById(uniqueIds);

        if (events.size() != uniqueIds.size()) {
            Set<Long> found = events.stream().map(Event::getId).collect(Collectors.toSet());
            Long missing = uniqueIds.stream().filter(id -> !found.contains(id)).findFirst().orElse(null);
            throw new NotFoundException("Event with id=" + missing + " was not found");
        }

        return new HashSet<>(events);
    }
}
