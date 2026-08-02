package com.example.catalogueservice.controller;

import com.example.catalogueservice.dto.ApiResponse;
import com.example.catalogueservice.dto.PartRequestDto;
import com.example.catalogueservice.dto.PartResponseDto;
import com.example.catalogueservice.service.CatalogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final CatalogueService catalogueService;

    @PostMapping
    public ResponseEntity<ApiResponse<PartResponseDto>> createPart(@RequestBody PartRequestDto requestDto) {
        PartResponseDto created = catalogueService.addPart(requestDto);
        return new ResponseEntity<>(ApiResponse.success("Part created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PartResponseDto>>> getAllParts() {
        List<PartResponseDto> parts = catalogueService.getAllParts();
        return ResponseEntity.ok(ApiResponse.success("Parts retrieved successfully", parts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartResponseDto>> getPartById(@PathVariable UUID id) {
        PartResponseDto part = catalogueService.findPartById(id);
        return ResponseEntity.ok(ApiResponse.success("Part retrieved successfully", part));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PartResponseDto>>> searchParts(@RequestParam String keyword) {
        List<PartResponseDto> parts = catalogueService.searchParts(keyword);
        return ResponseEntity.ok(ApiResponse.success("Parts searched successfully", parts));
    }

    @GetMapping("/vehicle")
    public ResponseEntity<ApiResponse<List<PartResponseDto>>> findPartsByVehicle(
            @RequestParam String make,
            @RequestParam String model,
            @RequestParam int year) {
        List<PartResponseDto> parts = catalogueService.findPartsByVehicle(make, model, year);
        return ResponseEntity.ok(ApiResponse.success("Parts filtered by vehicle successfully", parts));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PartResponseDto>> updatePart(@PathVariable UUID id, @RequestBody PartRequestDto requestDto) {
        PartResponseDto updated = catalogueService.updatePart(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Part updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePart(@PathVariable UUID id) {
        catalogueService.deletePart(id);
        return ResponseEntity.ok(ApiResponse.success("Part deleted successfully", null));
    }
}