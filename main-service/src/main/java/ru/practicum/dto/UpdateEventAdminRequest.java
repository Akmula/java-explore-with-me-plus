package ru.practicum.dto;

import lombok.*;
import ru.practicum.dto.enums.AdminActionState;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateEventAdminRequest extends UpdateEventRequest {

    private AdminActionState stateAction; // Новое состояние события
}