package ru.practicum.ewm.server.category.controller.publicapi;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.server.category.dto.CategoryDto;
import ru.practicum.ewm.server.category.service.CategoryService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        return categoryService.getAll(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable long catId) {
        return categoryService.getById(catId);
    }
}
