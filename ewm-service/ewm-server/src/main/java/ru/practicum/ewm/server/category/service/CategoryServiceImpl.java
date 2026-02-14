package ru.practicum.ewm.server.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.category.dto.CategoryDto;
import ru.practicum.ewm.server.category.dto.NewCategoryDto;
import ru.practicum.ewm.server.category.mapper.CategoryMapper;
import ru.practicum.ewm.server.category.model.Category;
import ru.practicum.ewm.server.category.repository.CategoryRepository;
import ru.practicum.ewm.server.error.exception.ConditionNotMetException;
import ru.practicum.ewm.server.error.exception.ConflictException;
import ru.practicum.ewm.server.error.exception.NotFoundException;
import ru.practicum.ewm.server.event.repository.EventRepository;
import ru.practicum.ewm.server.util.OffsetBasedPageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException("Category name must be unique");
        }
        Category saved = categoryRepository.save(CategoryMapper.toEntity(dto));
        return CategoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto update(long categoryId, CategoryDto dto) {
        Category category = getCategoryOrThrow(categoryId);

        String newName = dto.getName();
        if (!category.getName().equalsIgnoreCase(newName) && categoryRepository.existsByNameIgnoreCase(newName)) {
            throw new ConflictException("Category name must be unique");
        }

        category.setName(newName);
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(long categoryId) {
        Category category = getCategoryOrThrow(categoryId);
        if (eventRepository.countByCategoryId(category.getId()) > 0) {
            throw new ConditionNotMetException("The category is not empty");
        }
        categoryRepository.delete(category);
    }

    @Override
    public CategoryDto getById(long categoryId) {
        return CategoryMapper.toDto(getCategoryOrThrow(categoryId));
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        OffsetBasedPageRequest pageRequest = new OffsetBasedPageRequest(from, size);
        return categoryRepository.findAll(pageRequest)
                .stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    private Category getCategoryOrThrow(long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
    }
}
