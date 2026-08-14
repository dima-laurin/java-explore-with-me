package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequesterId(Long userId);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long userId);

    List<Request> findByEventInitiatorIdAndEventId(Long userId, Long eventId);

    List<Request> findByEventInitiatorIdAndEventIdAndIdIn(Long userId, Long eventId, Collection<Long> requestIds);

    List<Request> findByEventIdAndStatus(Long eventId, RequestStatus status);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);
}