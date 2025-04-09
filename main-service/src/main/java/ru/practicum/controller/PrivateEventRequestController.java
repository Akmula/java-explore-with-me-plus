package ru.practicum.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.service.EventRequestService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/users/{userId}/requests")
public class PrivateEventRequestController {

    private final EventRequestService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addEventRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("PrivateEventRequestController - Добавление запроса на участие в событии с id: {}", eventId);
        return service.addEventRequest(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> findAllRequestByUserId(@PathVariable Long userId) {
        log.info("PrivateEventRequestController - Получение запросов на участие в событиях пользователем с id: {}!",
                userId);
        return service.findAllRequestByUserId(userId);
    }

    @PatchMapping(path = "/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long requestId, @PathVariable Long userId) {
        log.info("PrivateEventRequestController - Отмена запроса на участие в событии с id:{}, пользователем с id: {}!",
                requestId, userId);
        return service.cancelRequest(requestId, userId);
    }
}