package ru.practicum.compilation.controller;

import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
public class PublicCompilationController {

    private final CompilationService compilationService;

    public PublicCompilationController(
            CompilationService compilationService) {

        this.compilationService = compilationService;
    }

    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false)
            Boolean pinned,

            @RequestParam(defaultValue = "0")
            @PositiveOrZero
            int from,

            @RequestParam(defaultValue = "10")
            int size) {

        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilation(
            @PathVariable Long compId) {

        return compilationService.getCompilation(compId);
    }
}
