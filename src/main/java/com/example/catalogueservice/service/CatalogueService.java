package com.example.catalogueservice.service;

import com.example.catalogueservice.dto.PartRequestDto;
import com.example.catalogueservice.dto.PartResponseDto;
import com.example.catalogueservice.dto.PagedResponse;
import com.example.catalogueservice.entity.Brand;
import com.example.catalogueservice.entity.Category;
import com.example.catalogueservice.entity.Part;
import com.example.catalogueservice.entity.PartStatus;
import com.example.catalogueservice.exception.ResourceNotFoundException;
import com.example.catalogueservice.exception.StalePartVersionException;
import com.example.catalogueservice.repository.BrandRepository;
import com.example.catalogueservice.repository.CategoryRepository;
import com.example.catalogueservice.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogueService {
    private final PartRepository partRepository;
    private final OutboxService outboxService;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Transactional
    public PartResponseDto addPart(PartRequestDto requestDto) {
        UUID partId = UUID.randomUUID();
        Part partToSave = toEntity(requestDto, partId);

        Part savedPart = partRepository.save(partToSave);
        PartResponseDto responseDto = toDto(savedPart);

        outboxService.saveEvent("PART", savedPart.getId().toString(), "PartListed", responseDto);
        return responseDto;
    }

    @Transactional(readOnly = true)
    public PartResponseDto findPartById(UUID id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + id));
        return toDto(part);
    }

    @Transactional(readOnly = true)
    public List<PartResponseDto> findPartsByVehicle(String make, String model, int year) {
        return partRepository.findByVehicleFitment(make, model, year).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PartResponseDto> searchParts(String keyword) {
        return partRepository.searchParts(keyword).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PartResponseDto> getAllParts() {
        return partRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<PartResponseDto> getParts(int page, int size, PartStatus status, String keyword) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        Page<Part> parts = partRepository.findByStatusAndKeyword(status, normalizedKeyword, PageRequest.of(page, size));

        return new PagedResponse<>(
                parts.getContent().stream().map(this::toDto).toList(),
                parts.getNumber(),
                parts.getSize(),
                parts.getTotalElements(),
                parts.getTotalPages()
        );
    }

    @Transactional
    public PartResponseDto updatePart(UUID id, PartRequestDto requestDto) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + id));
        if (!java.util.Objects.equals(requestDto.version(), part.getVersion())) {
            throw new StalePartVersionException(
                    "Part version " + requestDto.version() + " is stale; current version is " + part.getVersion() + "."
            );
        }
        part.updateDetails(toEntity(requestDto, id));
        Part savedPart = partRepository.saveAndFlush(part);
        PartResponseDto responseDto = toDto(savedPart);

        outboxService.saveEvent("PART", savedPart.getId().toString(), "PartUpdated", responseDto);

        return responseDto;
    }

    @Transactional
    public void deletePart(UUID id) {
        if (!partRepository.existsById(id)) {
            throw new ResourceNotFoundException("Part not found with id: " + id);
        }
        partRepository.deleteById(id);

        outboxService.saveEvent("PART", id.toString(), "PartDeleted", id);
    }

    // --- Brand CRUD ---

    public Brand createBrand(Brand brand) {
        if (brand.getId() == null) {
            brand.setId(UUID.randomUUID());
        }
        return brandRepository.save(brand);
    }

    @Transactional(readOnly = true)
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Brand getBrandById(UUID id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
    }

    @Transactional
    public Brand updateBrand(UUID id, Brand brandDetails) {
        Brand brand = getBrandById(id);
        Brand updatedBrand = new Brand(brand.getId(), brandDetails.getName());
        return brandRepository.save(updatedBrand);
    }

    @Transactional
    public void deleteBrand(UUID id) {
        if (!brandRepository.existsById(id)) {
            throw new ResourceNotFoundException("Brand not found with id: " + id);
        }
        brandRepository.deleteById(id);
    }

    // --- Category CRUD ---

    @Transactional
    public Category createCategory(Category category) {
        if(category.getId()==null){
            category.setId(UUID.randomUUID());
        }
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional
    public Category updateCategory(UUID id, Category categoryDetails) {
        Category category = getCategoryById(id);
        Category updatedCategory = new Category(category.getId(), categoryDetails.getName(), categoryDetails.getParentId());
        return categoryRepository.save(updatedCategory);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    // --- Private Mapping Helper Methods ---

    private Part toEntity(PartRequestDto dto, UUID id) {
        Brand brand = brandRepository.findById(dto.brandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + dto.brandId()));
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.categoryId()));

        return new Part(
                id,
                dto.sku(),
                dto.name(),
                brand,
                category,
                dto.price(),
                dto.status(),
                dto.vehicleFitments(),
                dto.images()
        );
    }

    private PartResponseDto toDto(Part part) {
        return new PartResponseDto(
                part.getId(),
                part.getSku(),
                part.getName(),
                part.getBrand() != null ? part.getBrand().getId() : null,
                part.getBrand() != null ? part.getBrand().getName() : null,
                part.getCategory() != null ? part.getCategory().getId() : null,
                part.getCategory() != null ? part.getCategory().getName() : null,
                part.getPrice(),
                part.getStatus(),
                part.getVehicleFitments(),
                part.getImages(),
                part.getVersion()
        );
    }
}
