package com.example.catalogueservice.controller;

import com.example.catalogueservice.dto.ApiResponse;
import com.example.catalogueservice.entity.Category;
import com.example.catalogueservice.service.CatalogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CatalogueService catalogueService;

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Category category) {
        Category created = catalogueService.createCategory(category);
        return new ResponseEntity<>(ApiResponse.success("Category created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = catalogueService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable UUID id) {
        Category category = catalogueService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable UUID id, @RequestBody Category category) {
        Category updated = catalogueService.updateCategory(id, category);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        catalogueService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
}