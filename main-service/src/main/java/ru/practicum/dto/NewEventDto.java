package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank(message = "Краткое описание события не может быть пустым!")
    @Size(min = 20, max = 2000, message = "Количество символов должно быть от 20 до 2000!")
    private String annotation; // Краткое описание

    @NotNull(message = "Категория должна быть указана!")
    private Long category; // Категория

    @NotBlank(message = "Полное описание события не может быть пустым!")
    @Size(min = 20, max = 7000, message = "Количество символов должно быть от 20 до 7000!")
    private String description; // Полное описание события

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate; // Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")

    Location location; // Широта и долгота места проведения события

    Boolean paid; // Нужно ли оплачивать участие

    @PositiveOrZero(message = "Лимит участников должен быть равен 0 или больше")
    Integer participantLimit; // Ограничение на количество участников. Значение 0 - означает отсутствие ограничения

    Boolean requestModeration; // Нужна ли пре-модерация заявок на участие

    @NotBlank(message = "Заголовок события не может быть пустым!")
    @Size(min = 3, max = 120, message = "Количество символов должно быть от 3 до 120!")
    String title; // Заголовок

}