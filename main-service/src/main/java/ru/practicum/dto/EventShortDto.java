package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
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
public class EventShortDto {

    @NotBlank(message = "Название категории не может быть пустым!")
    @Size(min = 1, max = 50, message = "Количество символов должно быть от 1 до 50!")
    private String annotation; // Краткое описание

    private CategoryDto category; // Категория

    Long confirmedRequests; // Количество одобренных заявок на участие в данном событии

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate; // Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")

    Long id; // Идентификатор

    UserShortDto initiator; // Пользователь (краткая информация)

    Boolean paid; // Нужно ли оплачивать участие

    String title; // Заголовок

    Long views; // Количество просмотрев события
}