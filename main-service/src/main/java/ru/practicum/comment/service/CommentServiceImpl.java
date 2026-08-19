package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentReactionDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentReaction;
import ru.practicum.comment.model.CommentState;
import ru.practicum.comment.model.ReactionType;
import ru.practicum.comment.repository.CommentReactionRepository;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.config.OffsetPageRequest;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final CommentReactionRepository commentReactionRepository;

    private final UserRepository userRepository;

    private final EventRepository eventRepository;

    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id=" + userId + " не найден"));

        Event event = eventRepository.findByIdAndState(
                        eventId,
                        EventState.PUBLISHED
                )
                .orElseThrow(() -> new NotFoundException(
                        "Опубликованное событие с id=" + eventId + " не найдено"));

        Comment comment = CommentMapper.toComment(dto, user, event);

        comment.setState(CommentState.PENDING);
        comment.setCreatedOn(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(
                savedComment,
                0,
                0
        );
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto) {

        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Комментарий с id=" + commentId + " пользователя с id=" + userId + " не найден"));

        if (dto.getText() != null) {
            comment.setText(dto.getText());
            comment.setState(CommentState.PENDING);
        }

        Comment savedComment = commentRepository.save(comment);

        return buildCommentDto(savedComment);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {

        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Комментарий с id=" + commentId + " пользователя с id=" + userId + " не найден"));

        commentRepository.delete(comment);
    }

    @Override
    public CommentDto setReaction(Long userId, Long commentId, CommentReactionDto dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id=" + userId + " не найден"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        "Комментарий с id=" + commentId + " не найден"));

        if (comment.getState() != CommentState.CONFIRMED) {
            throw new ConflictException(
                    "Реакцию можно поставить только на подтвержденный комментарий");
        }

        Optional<CommentReaction> existingReaction =
                commentReactionRepository.findByCommentIdAndUserId(commentId, userId);

        if (existingReaction.isEmpty()) {

            CommentReaction reaction = new CommentReaction();

            reaction.setComment(comment);
            reaction.setUser(user);
            reaction.setType(dto.getReaction());
            reaction.setCreatedOn(LocalDateTime.now());

            commentReactionRepository.save(reaction);

        } else {

            CommentReaction reaction = existingReaction.get();

            if (reaction.getType() == dto.getReaction()) {

                commentReactionRepository.delete(reaction);

            } else {

                reaction.setType(dto.getReaction());

                commentReactionRepository.save(reaction);
            }
        }

        return buildCommentDto(comment);
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId, int from, int size) {

        eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(
                        "Опубликованное событие с id=" + eventId + " не найдено"));

        Sort sort = Sort.by(Sort.Direction.DESC, "createdOn");

        Pageable page = new OffsetPageRequest(from, size, sort);

        List<Comment> comments = commentRepository.findByEventIdAndState(eventId, CommentState.CONFIRMED, page);

        return comments.stream()
                .map(this::buildCommentDto)
                .toList();
    }

    @Override
    public List<CommentDto> getUserComments(Long userId, int from, int size) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id=" + userId + " не найден"));

        Sort sort = Sort.by(Sort.Direction.DESC, "createdOn");

        Pageable page = new OffsetPageRequest(from, size, sort);

        List<Comment> comments = commentRepository.findByAuthorId(userId, page);

        return comments.stream()
                .map(this::buildCommentDto)
                .toList();
    }

    @Override
    public List<CommentDto> getAdminComments(CommentState state, int from, int size) {

        Sort sort = Sort.by(Sort.Direction.DESC, "createdOn");

        Pageable page = new OffsetPageRequest(from, size, sort);

        List<Comment> comments = commentRepository.findByState(state, page);

        return comments.stream()
                .map(this::buildCommentDto)
                .toList();
    }

    @Override
    public CommentDto updateCommentState(Long commentId, CommentState state) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        "Комментарий с id=" + commentId + " не найден"));

        if (comment.getState() != CommentState.PENDING) {
            throw new ConflictException(
                    "Модерировать можно только комментарий в статусе PENDING");
        }

        if (state != CommentState.CONFIRMED
                && state != CommentState.REJECTED) {

            throw new BadRequestException(
                    "Комментарий можно подтвердить или отклонить");
        }

        comment.setState(state);

        Comment savedComment = commentRepository.save(comment);

        return buildCommentDto(savedComment);
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        "Комментарий с id=" + commentId + " не найден"));

        commentRepository.delete(comment);
    }

    private CommentDto buildCommentDto(Comment comment) {

        long likes = commentReactionRepository.countByCommentIdAndType(comment.getId(), ReactionType.LIKE);

        long dislikes = commentReactionRepository.countByCommentIdAndType(comment.getId(), ReactionType.DISLIKE);

        return CommentMapper.toCommentDto(comment, likes, dislikes);
    }
}
