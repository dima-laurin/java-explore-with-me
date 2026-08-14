package ru.practicum.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query(value = """
            SELECT *
            FROM events
            WHERE initiator_id = ?1
            ORDER BY id
            LIMIT ?2 OFFSET ?3
            """, nativeQuery = true)
    List<Event> getUserEvents(Long userId, int size, int from);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Optional<Event> findByIdAndState(Long eventId, EventState state);

    boolean existsByCategoryId(Long categoryId);

    @Query(value = """
            SELECT *
            FROM events
            WHERE (?1 = FALSE OR initiator_id IN (?2))
              AND (?3 = FALSE OR state IN (?4))
              AND (?5 = FALSE OR category_id IN (?6))
              AND (?7 = FALSE OR event_date >= ?8)
              AND (?9 = FALSE OR event_date <= ?10)
            ORDER BY id
            LIMIT ?11 OFFSET ?12
            """, nativeQuery = true)
    List<Event> getAdminEvents(
            boolean filterUsers,
            Collection<Long> users,
            boolean filterStates,
            Collection<String> states,
            boolean filterCategories,
            Collection<Long> categories,
            boolean filterRangeStart,
            LocalDateTime rangeStart,
            boolean filterRangeEnd,
            LocalDateTime rangeEnd,
            int size,
            int from
    );

    @Query(value = """
        SELECT *
        FROM events
        WHERE state = 'PUBLISHED'
          AND (?1 = FALSE
               OR LOWER(annotation) LIKE LOWER(CONCAT('%', ?2, '%'))
               OR LOWER(description) LIKE LOWER(CONCAT('%', ?2, '%')))
          AND (?3 = FALSE OR category_id IN (?4))
          AND (?5 = FALSE OR paid = ?6)
          AND (?7 = FALSE OR event_date >= ?8)
          AND (?9 = FALSE OR event_date <= ?10)
        """, nativeQuery = true)
    List<Event> getPublicEvents(
            boolean filterText,
            String text,
            boolean filterCategories,
            Collection<Long> categories,
            boolean filterPaid,
            boolean paid,
            boolean filterRangeStart,
            LocalDateTime rangeStart,
            boolean filterRangeEnd,
            LocalDateTime rangeEnd
    );
}