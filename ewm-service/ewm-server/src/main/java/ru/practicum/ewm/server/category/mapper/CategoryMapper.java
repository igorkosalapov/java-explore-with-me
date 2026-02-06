package ru.practicum.ewm.server.category.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.server.category.dto.CategoryDto;
import ru.practicum.ewm.server.category.dto.NewCategoryDto;
import ru.practicum.ewm.server.category.model.Category;

@UtilityClass
public class CategoryMapper {

    public static Category toEntity(NewCategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        return category;
    }

    public static CategoryDto toDto(Category entity) {
        return CategoryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
