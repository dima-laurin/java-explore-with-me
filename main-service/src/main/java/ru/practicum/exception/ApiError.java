package ru.practicum.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ApiError {

    private List<String> errors;

    private String message;

    private String reason;

    private HttpStatus status;

    private LocalDateTime timestamp;
}
