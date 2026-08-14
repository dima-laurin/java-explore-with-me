package ru.practicum.compilation.mapper;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;

import java.util.HashSet;
import java.util.Set;

public final class CompilationMapper {

    private CompilationMapper() {
    }

    public static Compilation toCompilation(
            NewCompilationDto newCompilationDto) {

        return new Compilation(
                new HashSet<>(),
                newCompilationDto.isPinned(),
                newCompilationDto.getTitle()
        );
    }

    public static CompilationDto toCompilationDto(
            Compilation compilation,
            Set<EventShortDto> events) {

        return new CompilationDto(
                events,
                compilation.getId(),
                compilation.isPinned(),
                compilation.getTitle()
        );
    }
}
