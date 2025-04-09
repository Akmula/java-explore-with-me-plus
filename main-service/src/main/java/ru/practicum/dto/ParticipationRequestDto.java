package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.enums.EventRequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created; // Дата и время создания заявки (в формате "yyyy-MM-dd HH:mm:ss")

    private Long event; // Идентификатор события

    private Long id; // Идентификатор заявки

    private Long requester; // Идентификатор пользователя, отправившего заявку

    private EventRequestStatus status; // Статус заявки
}