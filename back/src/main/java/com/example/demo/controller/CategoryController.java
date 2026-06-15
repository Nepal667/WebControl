package com.example.demo.controller;

import com.example.demo.dto.CategoryDto;
import com.example.demo.dto.request.CreateCategoryRequest;
import com.example.demo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAll() {
        return categoryService.getAll();
    }

    @GetMapping("/{id}")
    public CategoryDto getById(@PathVariable UUID id) {
        return categoryService.getById(id);
    }

    @PostMapping
    public CategoryDto create(@RequestBody CreateCategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryDto update(@PathVariable UUID id, @RequestBody CreateCategoryRequest request) {
        return categoryService.update(id, request);
    }

    @PutMapping("/{id}/toggle")
    public CategoryDto toggle(@PathVariable UUID id) {
        return categoryService.toggleActive(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}