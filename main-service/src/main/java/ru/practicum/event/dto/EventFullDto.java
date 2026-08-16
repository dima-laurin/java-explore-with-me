package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {

    private String annotation;

    private CategoryDto category;

    private Long confirmedRequests;

    private LocalDateTime createdOn;

    private String description;

    private LocalDateTime eventDate;

    private Long id;

    private UserShortDto initiator;

    private Location location;

    private boolean paid;

    private int participantLimit;

    private LocalDateTime publishedOn;

    private boolean requestModeration;

    private EventState state;

    private String title;

    private Long views;
}
