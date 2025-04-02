package ru.practicum.dto.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserParams {
    List<Long> ids;
    Integer from;
    Integer size;
}