package ru.practicum.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.config.OffsetPageRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public UserDto createUser(NewUserRequest request) {
        User user = UserMapper.toUser(request);
        User savedUser = userRepository.save(user);

        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public List<UserDto> getUsers(Collection<Long> ids, int from, int size) {

        Sort sortById = Sort.by(Sort.Direction.ASC, "id");

        Pageable page = new OffsetPageRequest(from, size, sortById);

        List<User> users;

        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAllBy(page);
        } else {
            users = userRepository.findByIdIn(ids, page);
        }

        return users.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id=" + userId + " не найден"
                ));

        userRepository.delete(user);
    }
}