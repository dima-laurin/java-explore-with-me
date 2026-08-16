package ru.practicum.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Optional<Event> findByIdAndState(Long eventId, EventState state);

    boolean existsByCategoryId(Long categoryId);

    @Query(value = """
        SELECT *
        FROM events
        WHERE (:filterUsers = FALSE OR initiator_id IN (:users))
          AND (:filterStates = FALSE OR state IN (:states))
          AND (:filterCategories = FALSE OR category_id IN (:categories))
          AND (:filterRangeStart = FALSE OR event_date >= :rangeStart)
          AND (:filterRangeEnd = FALSE OR event_date <= :rangeEnd)
        ORDER BY id
        """, nativeQuery = true)
    List<Event> getAdminEvents(
            @Param("filterUsers") boolean filterUsers,
            @Param("users") Collection<Long> users,
            @Param("filterStates") boolean filterStates,
            @Param("states") Collection<String> states,
            @Param("filterCategories") boolean filterCategories,
            @Param("categories") Collection<Long> categories,
            @Param("filterRangeStart") boolean filterRangeStart,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("filterRangeEnd") boolean filterRangeEnd,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

    @Query(value = """
        SELECT *
        FROM events
        WHERE state = 'PUBLISHED'
          AND (
                :filterText = FALSE
                OR LOWER(annotation) LIKE LOWER(CONCAT('%', :text, '%'))
                OR LOWER(description) LIKE LOWER(CONCAT('%', :text, '%'))
              )
          AND (:filterCategories = FALSE OR category_id IN (:categories))
          AND (:filterPaid = FALSE OR paid = :paid)
          AND (:filterRangeStart = FALSE OR event_date >= :rangeStart)
          AND (:filterRangeEnd = FALSE OR event_date <= :rangeEnd)
        ORDER BY id
        """, nativeQuery = true)
    List<Event> getPublicEvents(
            @Param("filterText") boolean filterText,
            @Param("text") String text,
            @Param("filterCategories") boolean filterCategories,
            @Param("categories") Collection<Long> categories,
            @Param("filterPaid") boolean filterPaid,
            @Param("paid") boolean paid,
            @Param("filterRangeStart") boolean filterRangeStart,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("filterRangeEnd") boolean filterRangeEnd,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

    @Query(value = """
        SELECT *
        FROM events
        WHERE state = 'PUBLISHED'
          AND (
                :filterText = FALSE
                OR LOWER(annotation) LIKE LOWER(CONCAT('%', :text, '%'))
                OR LOWER(description) LIKE LOWER(CONCAT('%', :text, '%'))
              )
          AND (:filterCategories = FALSE OR category_id IN (:categories))
          AND (:filterPaid = FALSE OR paid = :paid)
          AND (:filterRangeStart = FALSE OR event_date >= :rangeStart)
          AND (:filterRangeEnd = FALSE OR event_date <= :rangeEnd)
          ORDER BY event_date
        """, nativeQuery = true)
    List<Event> getPublicEventsOrderByEventDate(
            @Param("filterText") boolean filterText,
            @Param("text") String text,
            @Param("filterCategories") boolean filterCategories,
            @Param("categories") Collection<Long> categories,
            @Param("filterPaid") boolean filterPaid,
            @Param("paid") boolean paid,
            @Param("filterRangeStart") boolean filterRangeStart,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("filterRangeEnd") boolean filterRangeEnd,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );
}