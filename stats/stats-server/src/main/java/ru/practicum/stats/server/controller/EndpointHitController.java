package ru.practicum.stats.server.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.service.EndpointHitService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class EndpointHitController {

    private final EndpointHitService endpointHitService;

    public EndpointHitController(EndpointHitService endpointHitService) {
        this.endpointHitService = endpointHitService;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void addHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        endpointHitService.addHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> viewStats(@RequestParam LocalDateTime start,
                                        @RequestParam LocalDateTime end,
                                        @RequestParam(required = false) List<String> uris,
                                        @RequestParam(defaultValue = "false") boolean unique) {
        return endpointHitService.viewStats(start, end, uris, unique);
    }
}
