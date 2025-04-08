package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.dto.params.PrivateEventParams;
import ru.practicum.service.EventService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/users/{userId}/events")
public class PrivateEventController {

    private final EventService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto add(@PathVariable Long userId, @RequestBody @Valid NewEventDto dto) {
        log.info("PrivateEventController - Добавление пользователем с id: {}, события: {}", userId, dto);
        return service.add(userId, dto);
    }

    @GetMapping
    public List<EventShortDto> findAllByUserId(@PathVariable Long userId, @ModelAttribute PrivateEventParams params) {
        log.info("PrivateEventController - Получение списка событий пользователя с id: {}!", userId);
        return service.findAllByUserId(userId, params);
    }

    @GetMapping(path = "/{eventId}")
    public EventFullDto findUserEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("PrivateEventController - Получение события с id: {}, пользователем с id: {}", userId, eventId);
        return service.findUserEventById(userId, eventId);
    }

    @PatchMapping(path = "/{eventId}")
    public EventFullDto updateUserEventById(@PathVariable Long userId, @PathVariable Long eventId,
                                            UpdateEventUserRequest dto) {
        log.info("PrivateEventController - Обновление события с id: {}, пользователем с id: {}", userId, eventId);
        return service.updateUserEventById(userId, eventId, dto);
    }
}