package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.enums.EventState;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {

    private String annotation; // Краткое описание

    private CategoryDto category; // Категория

    Long confirmedRequests; // Количество одобренных заявок на участие в данном событии

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn; // Дата и время создания события (в формате "yyyy-MM-dd HH:mm:ss")

    private String description; // Полное описание события

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate; // Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")

    Long id; // Идентификатор

    UserShortDto initiator; // Пользователь (краткая информация)

    Location location; // Широта и долгота места проведения события

    Boolean paid; // Нужно ли оплачивать участие

    Integer participantLimit; // Ограничение на количество участников. Значение 0 - означает отсутствие ограничения

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn; // Дата и время публикации события (в формате "yyyy-MM-dd HH:mm:ss")

    Boolean requestModeration; // Нужна ли пре-модерация заявок на участие

    EventState state; // Список состояний жизненного цикла события

    String title; // Заголовок

    Long views; // Количество просмотрев события
}