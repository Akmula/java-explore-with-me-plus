package ru.practicum.service;

import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto add(NewCategoryDto dto);

    CategoryDto update(Long catId, CategoryDto dto);

    void deleteById(Long catId);

    List<CategoryDto> findAll(Integer from, Integer size);

    CategoryDto findById(Long catId);
}