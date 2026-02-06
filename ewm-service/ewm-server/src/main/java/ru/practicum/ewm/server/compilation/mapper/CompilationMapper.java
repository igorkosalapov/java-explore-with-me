package ru.practicum.ewm.server.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.server.compilation.dto.CompilationDto;
import ru.practicum.ewm.server.compilation.model.Compilation;
import ru.practicum.ewm.server.event.dto.EventShortDto;
import ru.practicum.ewm.server.event.mapper.EventMapper;

import java.util.List;

@UtilityClass
public class CompilationMapper {

    public static CompilationDto toDto(Compilation compilation) {
        List<EventShortDto> events = compilation.getEvents() == null ? List.of() : compilation.getEvents().stream()
                .map(e -> EventMapper.toShortDto(e, 0L, 0L))
                .toList();

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(events)
                .build();
    }
}
