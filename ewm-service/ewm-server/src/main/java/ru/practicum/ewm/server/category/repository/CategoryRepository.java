package ru.practicum.ewm.server.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.server.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);
}
