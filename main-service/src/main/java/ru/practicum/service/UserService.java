package ru.practicum.service;

import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.params.AdminUserParams;

import java.util.List;

public interface UserService {
    UserDto save(NewUserRequest newUserRequest);

    List<UserDto> findAll(AdminUserParams params);

    void deleteById(Long userId);
}