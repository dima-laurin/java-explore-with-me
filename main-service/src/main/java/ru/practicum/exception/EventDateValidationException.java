package ru.practicum.exception;

public class EventDateValidationException extends RuntimeException {
    public EventDateValidationException(String message) {
        super(message);
    }
}
