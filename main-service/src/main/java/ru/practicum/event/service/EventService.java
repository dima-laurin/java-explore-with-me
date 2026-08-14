package ru.practicum.event.service;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.EventSort;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventService {

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventRequest);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventRequest);

    List<EventFullDto> getAdminEvents(
            Collection<Long> users,
            Collection<EventState> states,
            Collection<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size
    );

    List<EventShortDto> getPublicEvents(
            String text,
            Collection<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            boolean onlyAvailable,
            EventSort sort,
            int from,
            int size,
            String uri,
            String ip
    );

    EventFullDto getPublicEvent(Long eventId, String uri, String ip);
}
