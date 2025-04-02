package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.params.AdminUserParams;
import ru.practicum.service.UserService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/admin/users")
public class AdminUserController {

    private final UserService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.info("AdminUserController - Добавление пользователя: {}", newUserRequest);
        return service.save(newUserRequest);
    }

    @GetMapping
    public List<UserDto> findAll(@ModelAttribute @Valid AdminUserParams params) {
        log.info("AdminUserController - Получение пользователей по параметрам: {}", params);
        return service.findAll(params);
    }

    @DeleteMapping(path = "/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("AdminUserController - Удаление пользователя с id: {}", userId);
        service.deleteById(userId);
    }
}