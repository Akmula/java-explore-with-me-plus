package ru.practicum.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.Location;
import ru.practicum.dto.enums.EventState;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 120)
    private String title; // Заголовок

    @Column(name = "annotation", nullable = false, length = 2000)
    private String annotation; // Краткое описание

    @Column(name = "description", nullable = false, length = 7000)
    private String description; // Полное описание события

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "initiator_id")
    private User initiator; // Пользователь (краткая информация)

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "category_id")
    private Category category; // Категория

    @Embedded
    private Location location; // Широта и долгота места проведения события

    @Column(name = "paid")
    private Boolean paid; // Нужно ли оплачивать участие

    @Column(name = "participant_limit")
    private Integer participantLimit; // Ограничение на количество участников. Значение 0 - означает отсутствие ограничения

    @Column(name = "request_moderation")
    private Boolean requestModeration; // Нужна ли пре-модерация заявок на участие

    @Enumerated(EnumType.STRING)
    private EventState state; // Список состояний жизненного цикла события

    @Column(name = "created_date")
    private LocalDateTime createdOn; // Дата и время создания события (в формате "yyyy-MM-dd HH:mm:ss")

    @Column(name = "published_date")
    private LocalDateTime publishedOn; // Дата и время публикации события (в формате "yyyy-MM-dd HH:mm:ss")

    @Column(name = "event_date")
    private LocalDateTime eventDate; // Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")

    @Transient
    @Builder.Default
    private Long views = 0L; // Количество просмотрев события

    @Transient
    @Builder.Default
    private Long confirmedRequests = 0L; // Количество одобренных заявок на участие в данном событии
}