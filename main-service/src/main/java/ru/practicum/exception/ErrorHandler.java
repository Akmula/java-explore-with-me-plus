package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handlerNotFoundException(final NotFoundException e) {
        String stackTrace = getStackTrace(e);
        log.error("Ошибка: 404 NOT_FOUND - {}", stackTrace);
        return new ApiError("Объект не найден или недоступен", e.getMessage(),
                HttpStatus.NOT_FOUND.name(), LocalDateTime.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        String stackTrace = getStackTrace(e);
        log.error("Ошибка валидации: 400 BAD_REQUEST - {}", stackTrace);
        return new ApiError("Запрос составлен некорректно",
                Objects.requireNonNull(Objects.requireNonNull(e.getFieldError()).getDefaultMessage()),
                HttpStatus.BAD_REQUEST.name(), LocalDateTime.now());
    }

    @ExceptionHandler({BadRequestException.class, MissingServletRequestParameterException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerBadRequestException(final BadRequestException e) {
        String stackTrace = getStackTrace(e);
        log.error("Ошибка: 400 BAD_REQUEST - {}", stackTrace);
        return new ApiError("Запрос составлен некорректно", e.getMessage(),
                HttpStatus.BAD_REQUEST.name(), LocalDateTime.now());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerDataIntegrityViolationException(final DataIntegrityViolationException e) {
        String stackTrace = getStackTrace(e);
        log.error("Ошибка: 409 CONFLICT - {}", stackTrace);
        return new ApiError("Нарушение целостности данных", e.getCause().getCause().getLocalizedMessage(),
                HttpStatus.CONFLICT.name(), LocalDateTime.now());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerConflictException(final ConflictException e) {
        String stackTrace = getStackTrace(e);
        log.error("Ошибка валидации: 409 CONFLICT - {}", stackTrace);
        return new ApiError("Ошибка обновления!",
                e.getMessage(),
                HttpStatus.CONFLICT.name(), LocalDateTime.now());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handlerException(final Exception e) {
        String stackTrace = getStackTrace(e);
        log.error("Ошибка: 500 INTERNAL_SERVER_ERROR - {}", stackTrace);
        return new ApiError("Неизвестная ошибка", e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.name(), LocalDateTime.now());
    }

    private static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}