package ru.practicum.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.compilation.model.Compilation;

import java.util.List;

public interface CompilationRepository
        extends JpaRepository<Compilation, Long> {

    @Query(value = """
            SELECT *
            FROM compilations
            WHERE (?1 = FALSE OR pinned = ?2)
            ORDER BY id
            LIMIT ?3 OFFSET ?4
            """, nativeQuery = true)
    List<Compilation> getCompilations(boolean filterPinned, boolean pinnedForQuery, int size, int from);
}
