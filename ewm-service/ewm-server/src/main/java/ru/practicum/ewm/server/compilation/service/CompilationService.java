package ru.practicum.ewm.server.compilation.service;

import ru.practicum.ewm.server.compilation.dto.CompilationDto;
import ru.practicum.ewm.server.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.server.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto create(NewCompilationDto request);

    CompilationDto update(Long compilationId, UpdateCompilationRequest request);

    void delete(Long compilationId);

    List<CompilationDto> getAll(Boolean pinned, int from, int size);

    CompilationDto getById(Long compilationId);
}
