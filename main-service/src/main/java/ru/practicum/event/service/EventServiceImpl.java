package ru.practicum.event.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.config.OffsetPageRequest;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.AdminStateAction;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.UserStateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.EventDateValidationException;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private static final String APP_NAME = "ewm-main-service";

    private static final LocalDateTime STATS_START =
            LocalDateTime.of(1970, 1, 1, 0, 0);

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;

    private final StatsClient statsClient =
            new StatsClient("http://stats-server:9090");

    public EventServiceImpl(
            EventRepository eventRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            RequestRepository requestRepository) {

        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id=" + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException(
                        "Категория с id=" + newEventDto.getCategory() + " не найдена"));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {

            throw new EventDateValidationException(
                    "Дата события должна быть не раньше чем через два часа");
        }

        Event event = EventMapper.toEvent(newEventDto);

        event.setInitiator(initiator);
        event.setCategory(category);

        Event savedEvent = eventRepository.save(event);

        return EventMapper.toEventFullDto(savedEvent, 0L, 0L);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id=" + userId + " не найден"));

        Sort sortById = Sort.by(Sort.Direction.ASC, "id");

        Pageable page = new OffsetPageRequest(from, size, sortById);

        List<Event> events = eventRepository.findByInitiatorId(userId, page);

        Map<String, Long> views = getViews(events);

        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event,
                        getConfirmedRequests(event.getId()),
                        views.getOrDefault(
                                "/events/" + event.getId(),
                                0L
                        )
                ))
                .toList();
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " не найдено"));

        Map<String, Long> views = getViews(List.of(event));

        long eventViews = views.getOrDefault("/events/" + event.getId(), 0L);

        return EventMapper.toEventFullDto(event, getConfirmedRequests(event.getId()), eventViews);
    }

    @Transactional
    @Override
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventRequest) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " не найдено"));

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {

            throw new ConflictException(
                    "Можно изменять только события в состоянии PENDING или CANCELED");
        }

        if (updateEventRequest.getEventDate() != null
                && updateEventRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {

            throw new EventDateValidationException(
                    "Дата события должна быть не раньше чем через два часа");
        }

        if (updateEventRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventRequest.getAnnotation());
        }

        if (updateEventRequest.getCategory() != null) {

            Category category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException(
                            "Категория с id=" + updateEventRequest.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        if (updateEventRequest.getDescription() != null) {
            event.setDescription(updateEventRequest.getDescription());
        }

        if (updateEventRequest.getEventDate() != null) {
            event.setEventDate(updateEventRequest.getEventDate());
        }

        if (updateEventRequest.getLocation() != null) {
            event.setLocation(updateEventRequest.getLocation());
        }

        if (updateEventRequest.getPaid() != null) {
            event.setPaid(updateEventRequest.getPaid());
        }

        if (updateEventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventRequest.getParticipantLimit());
        }

        if (updateEventRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventRequest.getRequestModeration());
        }

        if (updateEventRequest.getTitle() != null) {
            event.setTitle(updateEventRequest.getTitle());
        }

        if (updateEventRequest.getStateAction() != null) {

            if (updateEventRequest.getStateAction() == UserStateAction.SEND_TO_REVIEW) {

                event.setState(EventState.PENDING);
            }

            if (updateEventRequest.getStateAction() == UserStateAction.CANCEL_REVIEW) {

                event.setState(EventState.CANCELED);
            }
        }

        Event updatedEvent = eventRepository.save(event);

        Map<String, Long> views = getViews(List.of(updatedEvent));

        long eventViews = views.getOrDefault("/events/" + updatedEvent.getId(), 0L);

        return EventMapper.toEventFullDto(updatedEvent, getConfirmedRequests(updatedEvent.getId()), eventViews);
    }

    @Transactional
    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventRequest) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " не найдено"));

        if (updateEventRequest.getEventDate() != null
                && updateEventRequest.getEventDate()
                .isBefore(LocalDateTime.now().plusHours(1))) {

            throw new EventDateValidationException(
                    "Дата события должна быть не раньше чем через час");
        }

        if (updateEventRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventRequest.getAnnotation());
        }

        if (updateEventRequest.getCategory() != null) {

            Category category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException(
                            "Категория с id=" + updateEventRequest.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        if (updateEventRequest.getDescription() != null) {
            event.setDescription(updateEventRequest.getDescription());
        }

        if (updateEventRequest.getEventDate() != null) {
            event.setEventDate(updateEventRequest.getEventDate());
        }

        if (updateEventRequest.getLocation() != null) {
            event.setLocation(updateEventRequest.getLocation());
        }

        if (updateEventRequest.getPaid() != null) {
            event.setPaid(updateEventRequest.getPaid());
        }

        if (updateEventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventRequest.getParticipantLimit());
        }

        if (updateEventRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventRequest.getRequestModeration());
        }

        if (updateEventRequest.getTitle() != null) {
            event.setTitle(updateEventRequest.getTitle());
        }

        if (updateEventRequest.getStateAction() != null) {

            if (updateEventRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT) {

                if (event.getState() != EventState.PENDING) {

                    throw new ConflictException(
                            "Опубликовать можно только событие в состоянии PENDING");
                }

                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {

                    throw new ConflictException(
                            "Дата события должна быть не раньше чем через час");
                }

                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }

            if (updateEventRequest.getStateAction() == AdminStateAction.REJECT_EVENT) {

                if (event.getState() == EventState.PUBLISHED) {

                    throw new ConflictException(
                            "Опубликованное событие нельзя отклонить");
                }

                event.setState(EventState.CANCELED);
            }
        }

        Event updatedEvent = eventRepository.save(event);

        Map<String, Long> views = getViews(List.of(updatedEvent));

        long eventViews = views.getOrDefault("/events/" + updatedEvent.getId(), 0L);

        return EventMapper.toEventFullDto(updatedEvent, getConfirmedRequests(updatedEvent.getId()), eventViews);
    }

    @Override
    public List<EventFullDto> getAdminEvents(
            Collection<Long> users,
            Collection<EventState> states,
            Collection<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size) {

        boolean filterUsers = users != null && !users.isEmpty();

        boolean filterStates = states != null && !states.isEmpty();

        boolean filterCategories = categories != null && !categories.isEmpty();

        boolean filterRangeStart = rangeStart != null;

        boolean filterRangeEnd = rangeEnd != null;

        Collection<Long> usersForQuery =
                filterUsers
                        ? users
                        : List.of(-1L);

        Collection<String> statesForQuery =
                filterStates
                        ? states.stream()
                        .map(EventState::name)
                        .toList()
                        : List.of(EventState.PENDING.name());

        Collection<Long> categoriesForQuery =
                filterCategories
                        ? categories
                        : List.of(-1L);

        LocalDateTime rangeStartForQuery =
                filterRangeStart
                        ? rangeStart
                        : LocalDateTime.now();

        LocalDateTime rangeEndForQuery =
                filterRangeEnd
                        ? rangeEnd
                        : LocalDateTime.now();

        Pageable page = new OffsetPageRequest(
                from,
                size,
                Sort.unsorted()
        );

        List<Event> events =
                eventRepository.getAdminEvents(
                        filterUsers,
                        usersForQuery,
                        filterStates,
                        statesForQuery,
                        filterCategories,
                        categoriesForQuery,
                        filterRangeStart,
                        rangeStartForQuery,
                        filterRangeEnd,
                        rangeEndForQuery,
                        page
                );

        Map<String, Long> views = getViews(events);

        return events.stream()
                .map(event -> EventMapper.toEventFullDto(
                        event,
                        getConfirmedRequests(event.getId()),
                        views.getOrDefault(
                                "/events/" + event.getId(),
                                0L
                        )
                ))
                .toList();
    }

    @Override
    public List<EventShortDto> getPublicEvents(
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
            String ip) {

        saveHit(uri, ip);

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {

            throw new EventDateValidationException(
                    "Дата начала диапазона должна быть раньше даты окончания");
        }

        boolean filterText = text != null && !text.isBlank();

        boolean filterCategories = categories != null && !categories.isEmpty();

        boolean filterPaid = paid != null;

        boolean filterRangeStart = rangeStart != null;

        boolean filterRangeEnd = rangeEnd != null;

        String textForQuery =
                filterText
                        ? text
                        : "";

        Collection<Long> categoriesForQuery =
                filterCategories
                        ? categories
                        : List.of(-1L);

        boolean paidForQuery =
                filterPaid
                        ? paid
                        : false;

        if (rangeStart == null && rangeEnd == null) {
            filterRangeStart = true;
            rangeStart = LocalDateTime.now();
        }

        LocalDateTime rangeStartForQuery =
                filterRangeStart
                        ? rangeStart
                        : LocalDateTime.now();

        LocalDateTime rangeEndForQuery =
                filterRangeEnd
                        ? rangeEnd
                        : LocalDateTime.now();

        boolean requiresPostProcessing =
                onlyAvailable || sort == EventSort.VIEWS;

        Pageable pageable;

        if (requiresPostProcessing) {
            pageable = Pageable.unpaged();
        } else {
            pageable = new OffsetPageRequest(from, size, Sort.unsorted());
        }

        List<Event> events;

        if (sort == EventSort.EVENT_DATE) {

            events = eventRepository.getPublicEventsOrderByEventDate(
                    filterText,
                    textForQuery,
                    filterCategories,
                    categoriesForQuery,
                    filterPaid,
                    paidForQuery,
                    filterRangeStart,
                    rangeStartForQuery,
                    filterRangeEnd,
                    rangeEndForQuery,
                    pageable
            );

        } else {

            events = eventRepository.getPublicEvents(
                    filterText,
                    textForQuery,
                    filterCategories,
                    categoriesForQuery,
                    filterPaid,
                    paidForQuery,
                    filterRangeStart,
                    rangeStartForQuery,
                    filterRangeEnd,
                    rangeEndForQuery,
                    pageable
            );
        }

        if (onlyAvailable) {

            events = events.stream()
                    .filter(event ->
                            event.getParticipantLimit() == 0
                                    || getConfirmedRequests(event.getId())
                                    < event.getParticipantLimit()
                    )
                    .toList();
        }

        Map<String, Long> views = getViews(events);

        if (sort == EventSort.VIEWS) {

            events = events.stream()
                    .sorted((first, second) -> {

                        long firstViews =
                                views.getOrDefault(
                                        "/events/" + first.getId(),
                                        0L
                                );

                        long secondViews =
                                views.getOrDefault(
                                        "/events/" + second.getId(),
                                        0L
                                );

                        return Long.compare(
                                secondViews,
                                firstViews
                        );
                    })
                    .toList();
        }

        if (requiresPostProcessing) {

            events = events.stream()
                    .skip(from)
                    .limit(size)
                    .toList();
        }

        return events.stream()
                .map(event ->
                        EventMapper.toEventShortDto(
                                event,
                                getConfirmedRequests(event.getId()),
                                views.getOrDefault(
                                        "/events/" + event.getId(),
                                        0L
                                )
                        )
                )
                .toList();
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId, String uri, String ip) {

        saveHit(uri, ip);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " не найдено"));

        Map<String, Long> views = getViews(List.of(event));

        long eventViews = views.getOrDefault("/events/" + event.getId(), 0L);

        return EventMapper.toEventFullDto(event, getConfirmedRequests(event.getId()), eventViews);
    }

    private long getConfirmedRequests(Long eventId) {

        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    private void saveHit(String uri, String ip) {

        EndpointHitDto hit = new EndpointHitDto(APP_NAME, uri, ip, LocalDateTime.now().withNano(0));

        statsClient.addHit(hit);
    }

    private Map<String, Long> getViews(List<Event> events) {

        if (events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(event ->
                        "/events/" + event.getId()
                )
                .toList();

        List<ViewStatsDto> stats = statsClient.viewStats(STATS_START, LocalDateTime.now(), uris, true);

        return stats.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        ViewStatsDto::getHits
                ));
    }
}
