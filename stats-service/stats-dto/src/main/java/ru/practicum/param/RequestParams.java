package ru.practicum.param;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestParams {

    @NotNull(message = "Дата старта должна быть указана.")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания должна быть указана.")
    private LocalDateTime end;

    @Builder.Default
    boolean unique = false;

    private List<String> uris;

}