package ru.practicum.exception;

public class NotFoundException extends RuntimeException {

    private static final String MSG_TEMPLATE = "Объект с id = %d не найден или недоступен!";

    public NotFoundException(Long id) {
        super(MSG_TEMPLATE.formatted(id));
    }
}