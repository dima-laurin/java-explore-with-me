package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.comment.model.ReactionType;

@Getter
@Setter
@NoArgsConstructor
public class CommentReactionDto {

    @NotNull
    private ReactionType reaction;
}
