package ru.practicum.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException exception) {
        return new ApiError(
                List.of(),
                exception.getMessage(),
                "Запрашиваемый объект не найден.",
                HttpStatus.NOT_FOUND,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(
            DataIntegrityViolationException exception) {

        return new ApiError(
                List.of(),
                "Не удалось выполнить операцию из-за нарушение целостности данных.",
                "Нарушение целостности данных.",
                HttpStatus.CONFLICT,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> "Поле: " + error.getField()
                        + ". Ошибка: " + error.getDefaultMessage()
                        + ". Значение: " + error.getRejectedValue())
                .findFirst()
                .orElse("Некорректные данные запроса.");

        return new ApiError(
                List.of(),
                message,
                "Запрос составлен некорректно.",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(ConflictException exception) {
        return new ApiError(
                List.of(),
                exception.getMessage(),
                "Условия выполнения операции не соблюдены.",
                HttpStatus.CONFLICT,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(
            BadRequestException exception) {

        return new ApiError(
                List.of(),
                exception.getMessage(),
                "Некорректно составлен запрос.",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolationException(
            ConstraintViolationException exception) {

        return new ApiError(
                List.of(),
                exception.getMessage(),
                "Запрос составлен некорректно.",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception) {

        return new ApiError(
                List.of(),
                "Некорректный формат тела запроса.",
                "Запрос составлен некорректно.",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception) {

        return new ApiError(
                List.of(),
                "Некорректное значение параметра: " + exception.getName(),
                "Запрос составлен некорректно.",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(EventDateValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidDateRangeException(
            EventDateValidationException exception) {

        return new ApiError(
                List.of(),
                exception.getMessage(),
                "Запрос составлен некорректно.",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
    }
}
