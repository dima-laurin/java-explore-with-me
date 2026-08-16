package ru.practicum.compilation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.config.OffsetPageRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CompilationServiceImpl implements CompilationService {

    private static final LocalDateTime STATS_START =
            LocalDateTime.of(1970, 1, 1, 0, 0);

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    private final StatsClient statsClient = new StatsClient("http://stats-server:9090");

    public CompilationServiceImpl(
            CompilationRepository compilationRepository,
            EventRepository eventRepository,
            RequestRepository requestRepository) {

        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional
    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {

        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {

            Set<Event> events = new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));

            compilation.setEvents(events);
        }

        Compilation savedCompilation = compilationRepository.save(compilation);

        return CompilationMapper.toCompilationDto(savedCompilation, getEventDtos(savedCompilation.getEvents()));
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        "Подборка с id=" + compId + " не найдена"));

        if (updateCompilationRequest.getEvents() != null) {

            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateCompilationRequest.getEvents()));

            compilation.setEvents(events);
        }

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);

        return CompilationMapper.toCompilationDto(updatedCompilation, getEventDtos(updatedCompilation.getEvents()));
    }

    @Transactional
    @Override
    public void deleteCompilation(Long compId) {

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        "Подборка с id=" + compId + " не найдена"));

        compilationRepository.delete(compilation);
    }

    @Override
    public CompilationDto getCompilation(Long compId) {

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        "Подборка с id=" + compId + " не найдена"));

        return CompilationMapper.toCompilationDto(compilation, getEventDtos(compilation.getEvents()));
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {

        Sort sortById = Sort.by(Sort.Direction.ASC, "id");

        Pageable page = new OffsetPageRequest(from, size, sortById);

        Page<Compilation> compilationPage;

        if (pinned == null) {
            compilationPage = compilationRepository.findAll(page);
        } else {
            compilationPage = compilationRepository.findByPinned(pinned, page);
        }

        List<Compilation> compilations = compilationPage.getContent();

        return compilations.stream()
                .map(compilation ->
                        CompilationMapper.toCompilationDto(
                                compilation,
                                getEventDtos(compilation.getEvents())
                        )
                )
                .toList();
    }

    private Set<EventShortDto> getEventDtos(Set<Event> events) {

        if (events.isEmpty()) {
            return Set.of();
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        List<ViewStatsDto> stats = statsClient.viewStats(STATS_START, LocalDateTime.now(), uris, true);

        Map<String, Long> views = stats.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        ViewStatsDto::getHits
                ));

        return events.stream()
                .map(event ->
                        EventMapper.toEventShortDto(
                                event,
                                requestRepository.countByEventIdAndStatus(
                                        event.getId(),
                                        RequestStatus.CONFIRMED
                                ),
                                views.getOrDefault(
                                        "/events/" + event.getId(),
                                        0L
                                )
                        )
                )
                .collect(Collectors.toSet());
    }
}
