package ru.practicum.category.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.config.OffsetPageRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public CategoryServiceImpl(
            CategoryRepository categoryRepository,
            EventRepository eventRepository) {

        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.toCategory(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.toCategoryDto(savedCategory);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(
                        "Категория с id=" + categoryId + " не найдена"));

        category.setName(categoryDto.getName());

        Category updatedCategory = categoryRepository.save(category);

        return CategoryMapper.toCategoryDto(updatedCategory);
    }

    @Transactional
    @Override
    public void deleteCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(
                        "Категория с id=" + categoryId + " не найдена"
                ));

        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException(
                    "Нельзя удалить категорию, к которой привязаны события");
        }

        categoryRepository.delete(category);
    }

    @Override
    public CategoryDto getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(
                        "Категория с id=" + categoryId + " не найдена"));

        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {

        Sort sortById = Sort.by(Sort.Direction.ASC, "id");

        Pageable page = new OffsetPageRequest(from, size, sortById);

        Page<Category> categoryPage = categoryRepository.findAll(page);

        return categoryPage.getContent().stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }
}