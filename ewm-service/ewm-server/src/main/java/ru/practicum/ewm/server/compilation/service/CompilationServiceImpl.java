package ru.practicum.ewm.server.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.compilation.dto.CompilationDto;
import ru.practicum.ewm.server.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.server.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.server.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.server.compilation.model.Compilation;
import ru.practicum.ewm.server.compilation.repository.CompilationRepository;
import ru.practicum.ewm.server.error.exception.NotFoundException;
import ru.practicum.ewm.server.event.model.Event;
import ru.practicum.ewm.server.event.repository.EventRepository;
import ru.practicum.ewm.server.util.OffsetBasedPageRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        List<Long> eventIds = dto.getEvents();
        validateUniqueIds(eventIds);

        Set<Event> events = fetchEventsOrThrow(eventIds);

        Compilation compilation = Compilation.builder()
                .title(dto.getTitle())
                .pinned(Boolean.TRUE.equals(dto.getPinned()))
                .events(events)
                .build();

        return CompilationMapper.toDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public CompilationDto update(long compId, UpdateCompilationRequest dto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            validateUniqueIds(dto.getEvents());
            compilation.getEvents().clear();
            compilation.getEvents().addAll(fetchEventsOrThrow(dto.getEvents()));
        }

        return CompilationMapper.toDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public void delete(long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        org.springframework.data.domain.Pageable page = new OffsetBasedPageRequest(from, size, null);

        var comps = (pinned == null)
                ? compilationRepository.findAll(page)
                : compilationRepository.findAllByPinned(pinned, page);

        return comps.stream().map(CompilationMapper::toDto).toList();
    }

    @Override
    public CompilationDto getCompilation(long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
        return CompilationMapper.toDto(compilation);
    }

    private void validateUniqueIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Set<Long> unique = new HashSet<>(ids);
        if (unique.size() != ids.size()) {
            throw new IllegalArgumentException("Duplicate ids are not allowed");
        }
    }

    private Set<Event> fetchEventsOrThrow(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        List<Event> events = eventRepository.findAllById(ids);
        if (events.size() != ids.size()) {
            Set<Long> found = events.stream().map(Event::getId).collect(java.util.stream.Collectors.toSet());
            Long missing = ids.stream().filter(id -> !found.contains(id)).findFirst().orElse(null);
            throw new NotFoundException("Event with id=" + missing + " was not found");
        }
        java.util.Map<Long, Event> byId = events.stream()
                .collect(java.util.stream.Collectors.toMap(Event::getId, java.util.function.Function.identity()));
        java.util.LinkedHashSet<Event> res = new java.util.LinkedHashSet<>();
        ids.forEach(id -> res.add(byId.get(id)));
        return res;
    }
}
