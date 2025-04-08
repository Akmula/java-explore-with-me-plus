package ru.practicum.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.params.PublicEventParams;
import ru.practicum.service.EventService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/events")
public class PublicEventController {

    private final EventService service;

    @GetMapping
    public List<EventShortDto> search(@ModelAttribute PublicEventParams params,
                                      HttpServletRequest request) {
        log.info("PublicEventController - Поиск событий с параметрами: {}!", params);
        return service.searchEvents(params, request);
    }

    @GetMapping(path = "/{eventId}")
    public EventFullDto findEventById(@PathVariable Long eventId, HttpServletRequest request) {
        log.info("PublicEventController - Получение события  по id: {}", eventId);
        return service.findEventById(eventId, request);
    }
}