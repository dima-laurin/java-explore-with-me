package ru.practicum.event.controller;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/events")
@Validated
public class PublicEventController {

    private final EventService eventService;

    public PublicEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Collection<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false")
            boolean onlyAvailable,
            @RequestParam(required = false)
            EventSort sort,
            @RequestParam(defaultValue = "0")
            @PositiveOrZero int from,
            @RequestParam(defaultValue = "10")
            int size,
            HttpServletRequest request) {

        return eventService.getPublicEvents(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size,
                request.getRequestURI(),
                request.getRemoteAddr()
        );
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(
            @PathVariable Long id,
            HttpServletRequest request) {

        return eventService.getPublicEvent(id, request.getRequestURI(), request.getRemoteAddr()
        );
    }
}