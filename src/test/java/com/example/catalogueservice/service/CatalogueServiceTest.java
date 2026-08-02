package com.example.catalogueservice.service;

import com.example.catalogueservice.entity.*;
import com.example.catalogueservice.repository.BrandRepository;
import com.example.catalogueservice.repository.CategoryRepository;
import com.example.catalogueservice.repository.OutboxRepository;
import com.example.catalogueservice.repository.PartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogueServiceTest {

    @Mock
    private PartRepository partRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @InjectMocks
    private CatalogueService catalogueService;
    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void shouldSavePartAndWriteToOutbox() {
        UUID partId = UUID.randomUUID();
        Brand brand = new Brand(UUID.randomUUID(), "Toyota");
        Category category = new Category(UUID.randomUUID(), "Brakes", null);
        Money price = new Money(15000L, Currency.USD);

        Part part = new Part(partId, "SKU-123", "Brake Pad", brand, category, price, PartStatus.ACTIVE, null, null);

        when(partRepository.save(any(Part.class))).thenReturn(part);

        Part savedPart = catalogueService.addPart(part);

        assertThat(savedPart).isNotNull();
        assertThat(savedPart.getId()).isEqualTo(partId);
        verify(partRepository).save(any(Part.class));
        verify(outboxRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldReturnPartById() {
        UUID partId = UUID.randomUUID();
        Brand brand = new Brand(UUID.randomUUID(), "Toyota");
        Category category = new Category(UUID.randomUUID(), "Brakes", null);
        Money price = new Money(15000L, Currency.USD);

        Part part = new Part(partId, "SKU-123", "Brake Pad", brand, category, price, PartStatus.ACTIVE, null, null);

        when(partRepository.findById(partId)).thenReturn(Optional.of(part));
        Part retrievedPart = catalogueService.findPartById(partId);

        assertThat(retrievedPart).isNotNull();
        assertThat(retrievedPart.getId()).isEqualTo(partId);
        assertThat(retrievedPart.getSku()).isEqualTo("SKU-123");
        verify(partRepository, times(1)).findById(partId);
    }
    @Test
    void shouldReturnPartsForGivenVehicle() {
        String make = "Toyota";
        String model = "Hilux";
        int year = 2008;
        List<Part> expectedParts = List.of(new Part());

        when(partRepository.findByVehicleFitment(make, model, year)).thenReturn(expectedParts);

        List<Part> actualParts = catalogueService.findPartsByVehicle(make, model, year);

        assertThat(actualParts).isEqualTo(expectedParts);
        verify(partRepository, times(1)).findByVehicleFitment(make, model, year);
    }
    @Test
    void shouldReturnPartsForKeywordSearch() {
        String keyword = "brakes";
        List<Part> expectedParts = List.of(new Part());

        when(partRepository.searchParts(keyword)).thenReturn(expectedParts);

        List<Part> actualParts = catalogueService.searchParts(keyword);

        assertThat(actualParts).isEqualTo(expectedParts);
        verify(partRepository, times(1)).searchParts(keyword);
    }
    @Test
    void shouldGetAllParts() {
        List<Part> expectedParts = List.of(new Part());
        when(partRepository.findAll()).thenReturn(expectedParts);

        List<Part> actualParts = catalogueService.getAllParts();

        assertThat(actualParts).isEqualTo(expectedParts);
        verify(partRepository, times(1)).findAll();
    }

    @Test
    void shouldUpdatePart() {
        UUID id = UUID.randomUUID();
        Part existingPart = new Part(id, "SKU-OLD", "Old Name", null, null, null, null, null, null);
        Part details = new Part(null, "SKU-NEW", "New Name", null, null, null, null, null, null);

        when(partRepository.findById(id)).thenReturn(Optional.of(existingPart));
        when(partRepository.save(any(Part.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Part updated = catalogueService.updatePart(id, details);

        assertThat(updated.getSku()).isEqualTo("SKU-NEW");
        assertThat(updated.getName()).isEqualTo("New Name");
        verify(partRepository, times(1)).save(any(Part.class));
        verify(outboxRepository, times(1)).save(any(OutboxEvent.class));
    }

    @Test
    void shouldDeletePart() {
        UUID id = UUID.randomUUID();
        when(partRepository.existsById(id)).thenReturn(true);
        doNothing().when(partRepository).deleteById(id);
        when(outboxRepository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        catalogueService.deletePart(id);

        verify(partRepository, times(1)).deleteById(id);
        verify(outboxRepository, times(1)).save(any(OutboxEvent.class));
    }


    @Test
    void shouldCreateBrand() {
        Brand brand = new Brand(UUID.randomUUID(), "Toyota");
        when(brandRepository.save(brand)).thenReturn(brand);

        Brand savedBrand = catalogueService.createBrand(brand);

        assertThat(savedBrand).isEqualTo(brand);
        verify(brandRepository, times(1)).save(brand);
    }

    @Test
    void shouldGetBrandById() {
        UUID id = UUID.randomUUID();
        Brand brand = new Brand(id, "Toyota");
        when(brandRepository.findById(id)).thenReturn(Optional.of(brand));

        Brand foundBrand = catalogueService.getBrandById(id);

        assertThat(foundBrand).isEqualTo(brand);
    }

    @Test
    void shouldThrowExceptionWhenBrandNotFound() {
        UUID id = UUID.randomUUID();
        when(brandRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogueService.getBrandById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Brand not found");
    }

    @Test
    void shouldUpdateBrand() {
        UUID id = UUID.randomUUID();
        Brand existingBrand = new Brand(id, "Toyota");
        Brand details = new Brand(null, "Honda");
        when(brandRepository.findById(id)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.save(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Brand updated = catalogueService.updateBrand(id, details);

        assertThat(updated.getName()).isEqualTo("Honda");
        verify(brandRepository, times(1)).save(any(Brand.class));
    }

    @Test
    void shouldDeleteBrand() {
        UUID id = UUID.randomUUID();
        when(brandRepository.existsById(id)).thenReturn(true);
        doNothing().when(brandRepository).deleteById(id);

        catalogueService.deleteBrand(id);

        verify(brandRepository, times(1)).deleteById(id);
    }

    @Test
    void shouldCreateCategory() {
        Category category = new Category(UUID.randomUUID(), "Brakes", null);
        when(categoryRepository.save(category)).thenReturn(category);

        Category savedCategory = catalogueService.createCategory(category);

        assertThat(savedCategory).isEqualTo(category);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void shouldGetCategoryById() {
        UUID id = UUID.randomUUID();
        Category category = new Category(id, "Brakes", null);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        Category foundCategory = catalogueService.getCategoryById(id);

        assertThat(foundCategory).isEqualTo(category);
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogueService.getCategoryById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void shouldUpdateCategory() {
        UUID id = UUID.randomUUID();
        Category existing = new Category(id, "Brakes", null);
        Category details = new Category(null, "Engine", null);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category updated = catalogueService.updateCategory(id, details);

        assertThat(updated.getName()).isEqualTo("Engine");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void shouldDeleteCategory() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.existsById(id)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(id);

        catalogueService.deleteCategory(id);

        verify(categoryRepository, times(1)).deleteById(id);
    }

}