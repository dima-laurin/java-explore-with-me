package ru.practicum.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.compilation.model.Compilation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    Page<Compilation> findByPinned(boolean pinned, Pageable pageable);
}
