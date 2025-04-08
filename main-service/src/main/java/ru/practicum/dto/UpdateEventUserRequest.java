package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.dto.enums.UserActionState;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventUserRequest {

    @NotBlank(message = "Название категории не может быть пустым!")
    @Size(min = 1, max = 50, message = "Количество символов должно быть от 1 до 50!")
    private String annotation; // Краткое описание

    private CategoryDto category; // Категория

    private String description; // Полное описание события

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate; // Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")

    Location location; // Широта и долгота места проведения события

    Boolean paid; // Нужно ли оплачивать участие

    Integer participantLimit; // Ограничение на количество участников. Значение 0 - означает отсутствие ограничения

    Boolean requestModeration; // Нужна ли пре-модерация заявок на участие

    UserActionState stateAction; // Новое состояние события

    String title; // Заголовок

}