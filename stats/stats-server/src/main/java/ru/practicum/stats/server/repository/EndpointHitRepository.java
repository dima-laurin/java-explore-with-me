package ru.practicum.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
            SELECT new ru.practicum.stats.dto.ViewStatsDto(
                hit.app,
                hit.uri,
                COUNT(hit.id)
            )
            FROM EndpointHit hit
            WHERE hit.timestamp BETWEEN ?1 AND ?2
            GROUP BY hit.app, hit.uri
            ORDER BY COUNT(hit.id) DESC
            """)
    List<ViewStatsDto> findStats(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            SELECT new ru.practicum.stats.dto.ViewStatsDto(
                hit.app,
                hit.uri,
                COUNT(hit.id)
            )
            FROM EndpointHit hit
            WHERE hit.timestamp BETWEEN ?1 AND ?2
              AND hit.uri IN ?3
            GROUP BY hit.app, hit.uri
            ORDER BY COUNT(hit.id) DESC
            """)
    List<ViewStatsDto> findStatsByUris(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris
    );

    @Query("""
            SELECT new ru.practicum.stats.dto.ViewStatsDto(
                hit.app,
                hit.uri,
                COUNT(DISTINCT hit.ip)
            )
            FROM EndpointHit hit
            WHERE hit.timestamp BETWEEN ?1 AND ?2
            GROUP BY hit.app, hit.uri
            ORDER BY COUNT(DISTINCT hit.ip) DESC
            """)
    List<ViewStatsDto> findUniqueStats(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            SELECT new ru.practicum.stats.dto.ViewStatsDto(
                hit.app,
                hit.uri,
                COUNT(DISTINCT hit.ip)
            )
            FROM EndpointHit hit
            WHERE hit.timestamp BETWEEN ?1 AND ?2
              AND hit.uri IN ?3
            GROUP BY hit.app, hit.uri
            ORDER BY COUNT(DISTINCT hit.ip) DESC
            """)
    List<ViewStatsDto> findUniqueStatsByUris(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris
    );
}
