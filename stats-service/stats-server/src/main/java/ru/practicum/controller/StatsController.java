package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.param.RequestParams;
import ru.practicum.service.StatService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatService service;

    @PostMapping(path = "/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void save(@Valid @RequestBody EndpointHit hit) {
        log.info("StatsController - сохранение endpoint hit {}", hit);
        service.save(hit);
    }

    @GetMapping(path = "/stats")
    public List<ViewStats> getStats(@ModelAttribute RequestParams requestParams) {
        log.info("StatsController - получение статистики с параметрами: {}", requestParams);
        return service.getViewStats(requestParams);
    }
}