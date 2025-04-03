package ru.practicum.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.entity.Category;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    @Override
    @Transactional
    public CategoryDto add(NewCategoryDto dto) {
        log.info("CategoryService - добавление категории: {}", dto);
        if (repository.existsByName(dto.getName())) {
            throw new ConflictException("Название категории: " + dto.getName() + ", существует!");
        }
        Category category = repository.save(CategoryMapper.toCategory(dto));
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryDto update(Long catId, CategoryDto dto) {
        log.info("CategoryService - обновление категории: {}", catId);
        Category category = repository.findById(catId).orElseThrow(() -> new NotFoundException(catId));
        if (repository.existsByName(dto.getName()) && !repository.findByName(dto.getName()).equals(category)) {
            throw new ConflictException("Название категории: " + dto.getName() + ", существует!");
        }
        category.setName(dto.getName());
        return CategoryMapper.toCategoryDto(repository.save(category));
    }

    @Override
    @Transactional
    public void deleteById(Long catId) {
        log.info("CategoryService - удаление категории с id: {}", catId);
        repository.findById(catId).orElseThrow(() -> new NotFoundException(catId));
        repository.deleteById(catId);
    }

    @Override
    public List<CategoryDto> findAll(Integer from, Integer size) {
        log.info("CategoryService - получение списка категорий!");
        Pageable pageable = PageRequest.of(from / size, size);
        List<Category> categories = repository.findAll(pageable).getContent();
        return categories.stream().map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto findById(Long catId) {
        log.info("CategoryService - получение категории с id: {}", catId);
        Category category = repository.findById(catId).orElseThrow(() -> new NotFoundException(catId));
        return CategoryMapper.toCategoryDto(category);
    }
}