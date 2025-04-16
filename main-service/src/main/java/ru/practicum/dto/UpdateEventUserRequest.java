package ru.practicum.dto;

import lombok.*;
import ru.practicum.dto.enums.UserActionState;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateEventUserRequest extends UpdateEventRequest {

    UserActionState stateAction; // Новое состояние события

}