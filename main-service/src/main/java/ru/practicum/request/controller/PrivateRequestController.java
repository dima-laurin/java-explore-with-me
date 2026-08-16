package ru.practicum.request.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
public class PrivateRequestController {

    private final RequestService requestService;

    public PrivateRequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(
            @PathVariable Long userId) {

        return requestService.getUserRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {

        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {

        return requestService.cancelRequest(userId, requestId);
    }
}
