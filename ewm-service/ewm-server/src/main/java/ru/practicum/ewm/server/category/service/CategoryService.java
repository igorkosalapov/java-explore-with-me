package ru.practicum.ewm.server.category.service;

import ru.practicum.ewm.server.category.dto.CategoryDto;
import ru.practicum.ewm.server.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(NewCategoryDto dto);

    CategoryDto update(long categoryId, CategoryDto dto);

    void delete(long categoryId);

    CategoryDto getById(long categoryId);

    List<CategoryDto> getAll(int from, int size);
}
