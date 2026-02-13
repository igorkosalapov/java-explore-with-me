package ru.practicum.ewm.server.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.ewm.server.error.exception.ConditionNotMetException;
import ru.practicum.ewm.server.error.exception.ConflictException;
import ru.practicum.ewm.server.error.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestControllerAdvice
public class ErrorHandler {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldError();
        String message = ex.getMessage();
        if (fieldError != null) {
            message = "Field: " + fieldError.getField()
                    + ". Error: " + fieldError.getDefaultMessage()
                    + ". Value: " + fieldError.getRejectedValue();
        }
        return buildError(message, HttpStatus.BAD_REQUEST, "Incorrectly made request.");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException ex) {
        ConstraintViolation<?> violation = ex.getConstraintViolations().stream().findFirst().orElse(null);
        String message = ex.getMessage();
        if (violation != null) {
            String path = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "";
            String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            message = "Field: " + field
                    + ". Error: " + violation.getMessage()
                    + ". Value: " + violation.getInvalidValue();
        }
        return buildError(message, HttpStatus.BAD_REQUEST, "Incorrectly made request.");
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            TypeMismatchException.class,
            DateTimeParseException.class,
            IllegalArgumentException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(Exception ex) {
        return buildError(ex, HttpStatus.BAD_REQUEST, "Incorrectly made request.");
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException ex) {
        return buildError(ex, HttpStatus.NOT_FOUND, "The required object was not found.");
    }

    @ExceptionHandler({
            ConflictException.class,
            DataIntegrityViolationException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIntegrityConflict(Exception ex) {
        return buildError(ex, HttpStatus.CONFLICT, "Integrity constraint has been violated.");
    }

    @ExceptionHandler(ConditionNotMetException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConditionNotMet(ConditionNotMetException ex) {
        return buildError(ex, HttpStatus.CONFLICT, "For the requested operation the conditions are not met.");
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpected(Throwable ex) {
        return buildError(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error.");
    }

    private ApiError buildError(Throwable ex, HttpStatus status, String reason) {
        return buildError(ex.getMessage(), status, reason);
    }

    private ApiError buildError(String message, HttpStatus status, String reason) {
        return ApiError.builder()
                .errors(List.of())
                .message(message)
                .reason(reason)
                .status(status.name())
                .timestamp(LocalDateTime.now().format(TS_FORMAT))
                .build();
    }
}
