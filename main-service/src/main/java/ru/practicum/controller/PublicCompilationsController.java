package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.params.PublicCompilationsParams;
import ru.practicum.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/compilations")
public class PublicCompilationsController {

    private final CompilationService service;

    @GetMapping
    public List<CompilationDto> findCompilation(@ModelAttribute @Valid PublicCompilationsParams params) {
        log.info("PublicCompilationsController - Получение подборок событий с параметрами: {}!", params);
        return service.findCompilation(params);
    }

    @GetMapping(path = "/{compId}")
    public CompilationDto findCompilationById(@PathVariable Long compId) {
        log.info("PublicCompilationsController - Получение подборок событий по id: {}", compId);
        return service.findCompilationById(compId);
    }
}