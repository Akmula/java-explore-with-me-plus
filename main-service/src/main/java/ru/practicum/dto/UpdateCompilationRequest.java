package ru.practicum.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationRequest {

    @UniqueElements
    private List<Long> events;

    private Boolean pinned;

    @Size(min = 1, max = 50, message = "Количество символов должно быть от 1 до 50!")
    private String title;
}