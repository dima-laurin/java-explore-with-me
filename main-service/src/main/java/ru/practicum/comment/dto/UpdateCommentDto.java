package ru.practicum.comment.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCommentDto {

    @Size(min = 1, max = 2000)
    private String text;
}
