package ru.practicum.event.mapper;

import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;

public final class EventMapper {

    private EventMapper() {
    }

    public static Event toEvent(NewEventDto dto) {
        return new Event(
                dto.getAnnotation(),
                dto.getDescription(),
                dto.getEventDate(),
                dto.getLocation(),
                dto.isPaid(),
                dto.getParticipantLimit(),
                dto.isRequestModeration(),
                dto.getTitle()
        );
    }

    public static EventShortDto toEventShortDto(
            Event event,
            Long confirmedRequests,
            Long views) {

        return new EventShortDto(
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                confirmedRequests,
                event.getEventDate(),
                event.getId(),
                UserMapper.toUserShortDto(event.getInitiator()),
                event.isPaid(),
                event.getTitle(),
                views
        );
    }

    public static EventFullDto toEventFullDto(
            Event event,
            Long confirmedRequests,
            Long views) {

        return new EventFullDto(
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                confirmedRequests,
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                event.getId(),
                UserMapper.toUserShortDto(event.getInitiator()),
                event.getLocation(),
                event.isPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.isRequestModeration(),
                event.getState(),
                event.getTitle(),
                views
        );
    }
}
