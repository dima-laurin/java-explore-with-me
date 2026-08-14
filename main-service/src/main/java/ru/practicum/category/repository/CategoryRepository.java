package ru.practicum.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.category.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(value = """
            SELECT *
            FROM categories
            ORDER BY id
            LIMIT ?1 OFFSET ?2
            """, nativeQuery = true)
    List<Category> getCategories(int size, int from);
}
