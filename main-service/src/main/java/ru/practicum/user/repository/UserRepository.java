package ru.practicum.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.user.model.User;

import java.util.Collection;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = """
            SELECT *
            FROM users
            WHERE id IN (?1)
            ORDER BY id
            LIMIT ?2 OFFSET ?3
            """, nativeQuery = true)
    List<User> getUsers(Collection<Long> ids, int size, int from);

    @Query(value = """
            SELECT *
            FROM users
            ORDER BY id
            LIMIT ?1 OFFSET ?2
            """, nativeQuery = true)
    List<User> getUsers(int size, int from);
}
