package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentReactionDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.model.CommentState;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long userId, Long eventId, NewCommentDto dto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto);

    void deleteComment(Long userId, Long commentId);

    CommentDto setReaction(Long userId, Long commentId, CommentReactionDto dto);

    List<CommentDto> getEventComments(Long eventId, int from, int size);

    List<CommentDto> getUserComments(Long userId, int from, int size);

    List<CommentDto> getAdminComments(CommentState state, int from, int size);

    CommentDto updateCommentState(Long commentId, CommentState state);

    void deleteCommentByAdmin(Long commentId);
}
