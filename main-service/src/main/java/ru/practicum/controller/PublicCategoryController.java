package ru.practicum.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/categories")
public class PublicCategoryController {

    private final CategoryService service;

    @GetMapping
    public List<CategoryDto> findAll(@PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                     @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("PublicCategoryController - Получение списка категорий!");
        return service.findAll(from, size);
    }

    @GetMapping(path = "/{catId}")
    public CategoryDto findById(@PathVariable Long catId) {
        log.info("PublicCategoryController - Получение категории по id: {}", catId);
        return service.findById(catId);
    }
}