package ru.practicum.request.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.model.RequestUpdateStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public RequestServiceImpl(
            RequestRepository requestRepository,
            UserRepository userRepository,
            EventRepository eventRepository) {

        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id=" + userId + " не найден"));

        List<Request> requests = requestRepository.findByRequesterId(userId);

        return RequestMapper.toParticipationRequestDtoList(requests);
    }

    @Transactional
    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id=" + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " не найдено"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(
                    "Владелец события не может подать заявку на своё событие");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException(
                    "Нельзя подать заявку на неопубликованное событие");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {

            throw new ConflictException(
                    "Пользователь уже отправил заявку на это событие");
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {

            throw new ConflictException(
                    "Достигнут лимит участников события");
        }

        Request request = new Request(event, requester);

        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {

            request.setStatus(RequestStatus.CONFIRMED);
        }

        Request savedRequest = requestRepository.save(request);

        return RequestMapper.toParticipationRequestDto(savedRequest);
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {

        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Заявка с id=" + requestId + " не найдена"));

        request.setStatus(RequestStatus.CANCELED);

        Request savedRequest = requestRepository.save(request);

        return RequestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {

        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " не найдено"));

        List<Request> requests = requestRepository.findByEventInitiatorIdAndEventId(userId, eventId);

        return RequestMapper.toParticipationRequestDtoList(requests);
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " не найдено"));

        List<Request> requests = requestRepository.findByEventInitiatorIdAndEventIdAndIdIn(
                userId, eventId, updateRequest.getRequestIds());

        for (Request request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException(
                        "Изменить можно только заявки в состоянии PENDING");
            }
        }

        if (updateRequest.getStatus() == RequestUpdateStatus.REJECTED) {

            for (Request request : requests) {
                request.setStatus(RequestStatus.REJECTED);
            }

            List<Request> savedRequests = requestRepository.saveAll(requests);

            return RequestMapper.toStatusUpdateResult(savedRequests);
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {

            throw new ConflictException(
                    "Достигнут лимит участников события");
        }

        List<Request> changedRequests = new ArrayList<>();

        for (Request request : requests) {

            if (event.getParticipantLimit() == 0 || confirmedRequests < event.getParticipantLimit()) {

                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests++;

            } else {
                request.setStatus(RequestStatus.REJECTED);
            }

            changedRequests.add(request);
        }

        requestRepository.saveAll(changedRequests);

        if (event.getParticipantLimit() > 0
                && confirmedRequests >= event.getParticipantLimit()) {

            List<Request> pendingRequests = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);

            for (Request request : pendingRequests) {
                request.setStatus(RequestStatus.REJECTED);
            }

            requestRepository.saveAll(pendingRequests);

            changedRequests.addAll(pendingRequests);
        }

        return RequestMapper.toStatusUpdateResult(changedRequests);
    }
}
