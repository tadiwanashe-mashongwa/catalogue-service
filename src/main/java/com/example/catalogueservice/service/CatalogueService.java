package com.example.catalogueservice.service;

import com.example.catalogueservice.entity.Brand;
import com.example.catalogueservice.entity.Category;
import com.example.catalogueservice.entity.OutboxEvent;
import com.example.catalogueservice.entity.Part;
import com.example.catalogueservice.repository.BrandRepository;
import com.example.catalogueservice.repository.CategoryRepository;
import com.example.catalogueservice.repository.OutboxRepository;
import com.example.catalogueservice.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogueService {
    private final PartRepository partRepository;
    private final OutboxRepository outboxRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Transactional
    public Part addPart(Part part) {
        UUID partId = part.getId() != null ? part.getId() : UUID.randomUUID();

        Part partToSave = new Part(
                partId,
                part.getSku(),
                part.getName(),
                part.getBrand(),
                part.getCategory(),
                part.getPrice(),
                part.getStatus(),
                part.getVehicleFitments(),
                part.getImages()
        );

        Part savedPart = partRepository.save(partToSave);

        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                "PART",
                savedPart.getId(),
                "PartListed",
                "{\"partId\":\"" + savedPart.getId() + "\"}",
                Instant.now()
        );
        outboxRepository.save(event);
        return savedPart;
    }

    @Transactional(readOnly = true)
    public Part findPartById(UUID id){
        return partRepository.findById(id).orElseThrow(()-> new RuntimeException("Part not found"));
    }

    @Transactional(readOnly = true)
    public List<Part> findPartsByVehicle(String make, String model, int year) {
        return partRepository.findByVehicleFitment(make, model, year);
    }
    @Transactional(readOnly = true)
    public List<Part> searchParts(String keyword) {
        return partRepository.searchParts(keyword);
    }
    @Transactional(readOnly = true)
    public List<Part> getAllParts() {
        return partRepository.findAll();
    }

    @Transactional
    public Part updatePart(UUID id, Part partDetails) {
        Part existingPart = findPartById(id);

        Part updatedPart = new Part(
                existingPart.getId(),
                partDetails.getSku(),
                partDetails.getName(),
                partDetails.getBrand(),
                partDetails.getCategory(),
                partDetails.getPrice(),
                partDetails.getStatus(),
                partDetails.getVehicleFitments(),
                partDetails.getImages()
        );

        Part savedPart = partRepository.save(updatedPart);

        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                "PART",
                savedPart.getId(),
                "PartUpdated",
                "{\"partId\":\"" + savedPart.getId() + "\"}",
                Instant.now()
        );
        outboxRepository.save(event);

        return savedPart;
    }

    @Transactional
    public void deletePart(UUID id) {
        if (!partRepository.existsById(id)) {
            throw new RuntimeException("Part not found with id: " + id);
        }
        partRepository.deleteById(id);

        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                "PART",
                id,
                "PartDeleted",
                "{\"partId\":\"" + id + "\"}",
                Instant.now()
        );
        outboxRepository.save(event);
    }

    // --- Brand CRUD ---

    @Transactional
    public Brand createBrand(Brand brand) {
        return brandRepository.save(brand);
    }

    @Transactional(readOnly = true)
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Brand getBrandById(UUID id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
    }

    @Transactional
    public Brand updateBrand(UUID id, Brand brandDetails) {
        Brand brand = getBrandById(id);
        // Assuming Brand has a setName method or similar mutator
        Brand updatedBrand = new Brand(brand.getId(), brandDetails.getName());
        return brandRepository.save(updatedBrand);
    }

    @Transactional
    public void deleteBrand(UUID id) {
        if (!brandRepository.existsById(id)) {
            throw new RuntimeException("Brand not found with id: " + id);
        }
        brandRepository.deleteById(id);
    }

    // --- Category CRUD ---

    @Transactional
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
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
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}