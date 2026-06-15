package com.example.demo.service;

import com.example.demo.dto.CategoryDto;
import com.example.demo.dto.request.CreateCategoryRequest;
import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getAll() {
        return categoryRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public CategoryDto getById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable : " + id));
        return toDto(category);
    }

    public CategoryDto create(CreateCategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(true);
        return toDto(categoryRepository.save(category));
    }

    public CategoryDto update(UUID id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable : " + id));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return toDto(categoryRepository.save(category));
    }

    public CategoryDto toggleActive(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable : " + id));
        category.setIsActive(!category.getIsActive());
        return toDto(categoryRepository.save(category));
    }

    public void delete(UUID id) {
        categoryRepository.deleteById(id);
    }

    public CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIsActive(category.getIsActive());
        return dto;
    }
}