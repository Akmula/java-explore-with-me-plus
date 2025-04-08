package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.dto.params.AdminEventParams;
import ru.practicum.service.EventService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/admin/events")
public class AdminEventController {

    private final EventService service;

    @GetMapping
    public List<EventFullDto> search(@ModelAttribute AdminEventParams params) {
        log.info("AdminEventController - Поиск событий админом с параметрами: {}!", params);
        return service.search(params);
    }

    @PatchMapping(path = "/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId, @RequestBody @Valid UpdateEventAdminRequest dto) {
        log.info("AdminEventController - Обновление события админом по id: {}", eventId);
        return service.updateEventByAdmin(eventId, dto);
    }
}