package com.reything.subsmentbe.controller;

import com.reything.subsmentbe.dto.category.CategoryDto;
import com.reything.subsmentbe.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public record CategoriesResponse(boolean success, List<CategoryDto> categories) {
    }

    @GetMapping
    @Operation(summary = "Tüm kategoriler")
    public CategoriesResponse list() {
        return new CategoriesResponse(true, categoryService.list());
    }
}
