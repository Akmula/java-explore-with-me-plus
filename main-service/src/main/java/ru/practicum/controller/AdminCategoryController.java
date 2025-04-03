package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.service.CategoryService;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/admin/categories")
public class AdminCategoryController {

    private final CategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto add(@RequestBody @Valid NewCategoryDto dto) {
        log.info("AdminCategoryController - Добавление категории: {}", dto);
        return service.add(dto);
    }

    @PatchMapping(path = "/{catId}")
    public CategoryDto update(@PathVariable Long catId, @RequestBody @Valid CategoryDto dto) {
        log.info("AdminCategoryController - Обновление категории с id: {}", catId);
        return service.update(catId, dto);
    }

    @DeleteMapping(path = "/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long catId) {
        log.info("AdminCategoryController - Удаление категории с id: {}", catId);
        service.deleteById(catId);
    }
}