package ru.practicum.event.controller;

import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;

import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@Validated
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventFullDto> getEvents(
            @RequestParam(required = false) Collection<Long> users,
            @RequestParam(required = false) Collection<EventState> states,
            @RequestParam(required = false) Collection<Long> categories,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0")
            @PositiveOrZero int from,
            @RequestParam(defaultValue = "10")
            int size) {

        return eventService.getAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable Long eventId,
            @RequestBody @Valid UpdateEventAdminRequest updateEventRequest) {

        return eventService.updateEventByAdmin(eventId, updateEventRequest);
    }
}