package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {

    @Size(min = 20, max = 2000, message = "Количество символов должно быть от 20 до 2000!")
    private String annotation; // Краткое описание

    private Long category; // Категория

    @Size(min = 20, max = 7000, message = "Количество символов должно быть от 20 до 7000!")
    private String description; // Полное описание события

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate; // Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")

    private Location location; // Широта и долгота места проведения события

    private Boolean paid; // Нужно ли оплачивать участие

    @PositiveOrZero
    private Integer participantLimit; // Ограничение на количество участников. Значение 0 - означает отсутствие ограничения

    private Boolean requestModeration; // Нужна ли пре-модерация заявок на участие

    @Size(min = 3, max = 120, message = "Количество символов должно быть от 3 до 120!")
    private String title; // Заголовок

}