package ru.practicum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {

    @NotBlank(message = "Email не может быть пустым!")
    @Email(message = "Не корректный email")
    @Size(min = 6, max = 254, message = "Количество символов должно быть от 6 до 254!")
    private String email;

    @NotBlank(message = "Имя не может быть пустым!")
    @Size(min = 2, max = 250, message = "Количество символов должно быть от 2 до 250!")
    private String name;
}