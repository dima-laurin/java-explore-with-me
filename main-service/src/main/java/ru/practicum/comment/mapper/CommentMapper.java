package ru.practicum.comment.mapper;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

public final class CommentMapper {

    private CommentMapper() {
    }

    public static Comment toComment(
            NewCommentDto dto,
            User author,
            Event event) {

        return new Comment(
                dto.getText(),
                author,
                event
        );
    }

    public static CommentDto toCommentDto(
            Comment comment,
            long likes,
            long dislikes) {

        return new CommentDto(
                comment.getId(),
                comment.getText(),
                UserMapper.toUserShortDto(comment.getAuthor()),
                comment.getEvent().getId(),
                comment.getState(),
                comment.getCreatedOn(),
                likes,
                dislikes
        );
    }
}
