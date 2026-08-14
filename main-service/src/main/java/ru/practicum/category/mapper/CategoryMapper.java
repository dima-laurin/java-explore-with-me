package ru.practicum.category.mapper;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;

import java.util.Collection;
import java.util.List;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static Category toCategory(NewCategoryDto dto) {
        return new Category(
                dto.getName()
        );
    }

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }

    public static List<CategoryDto> toCategoryDtoList(
            Collection<Category> categories) {

        return categories.stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }
}
