package ru.practicum.ewm.server.compilation.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.server.compilation.dto.CompilationDto;
import ru.practicum.ewm.server.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.server.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.server.compilation.service.CompilationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    public ResponseEntity<CompilationDto> saveCompilation(@RequestBody @Valid NewCompilationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compilationService.create(dto));
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<Void> deleteCompilation(@PathVariable long compId) {
        compilationService.delete(compId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable long compId,
                                           @RequestBody @Valid UpdateCompilationRequest dto) {
        return compilationService.update(compId, dto);
    }
}
