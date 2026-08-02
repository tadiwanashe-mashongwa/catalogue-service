package com.example.catalogueservice.controller;

import com.example.catalogueservice.dto.ApiResponse;
import com.example.catalogueservice.entity.Brand;
import com.example.catalogueservice.service.CatalogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {
    private final CatalogueService catalogueService;

    @PostMapping
    public ResponseEntity<ApiResponse<Brand>> createBrand(@RequestBody Brand brand) {
        Brand created = catalogueService.createBrand(brand);
        return new ResponseEntity<>(ApiResponse.success("Brand created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Brand>>> getAllBrands() {
        List<Brand> brands = catalogueService.getAllBrands();
        return ResponseEntity.ok(ApiResponse.success("Brands retrieved successfully", brands));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Brand>> getBrandById(@PathVariable UUID id) {
        Brand brand = catalogueService.getBrandById(id);
        return ResponseEntity.ok(ApiResponse.success("Brand retrieved successfully", brand));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Brand>> updateBrand(@PathVariable UUID id, @RequestBody Brand brand) {
        Brand updated = catalogueService.updateBrand(id, brand);
        return ResponseEntity.ok(ApiResponse.success("Brand updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable UUID id) {
        catalogueService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.success("Brand deleted successfully", null));
    }
}