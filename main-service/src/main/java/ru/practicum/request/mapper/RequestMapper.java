package ru.practicum.request.mapper;

import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;

import java.util.Collection;
import java.util.List;

public final class RequestMapper {

    private RequestMapper() {
    }

    public static ParticipationRequestDto toParticipationRequestDto(
            Request request) {

        return new ParticipationRequestDto(
                request.getCreated(),
                request.getEvent().getId(),
                request.getId(),
                request.getRequester().getId(),
                request.getStatus()
        );
    }

    public static List<ParticipationRequestDto> toParticipationRequestDtoList(
            Collection<Request> requests) {

        return requests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .toList();
    }

    public static EventRequestStatusUpdateResult toStatusUpdateResult(
            Collection<Request> requests) {

        List<ParticipationRequestDto> requestsDto =
                toParticipationRequestDtoList(requests);

        List<ParticipationRequestDto> confirmedRequests =
                requestsDto.stream()
                        .filter(request ->
                                request.getStatus() == RequestStatus.CONFIRMED)
                        .toList();

        List<ParticipationRequestDto> rejectedRequests =
                requestsDto.stream()
                        .filter(request ->
                                request.getStatus() == RequestStatus.REJECTED)
                        .toList();

        return new EventRequestStatusUpdateResult(
                confirmedRequests,
                rejectedRequests
        );
    }
}
