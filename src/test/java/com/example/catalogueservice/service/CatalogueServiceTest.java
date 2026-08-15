package com.example.catalogueservice.service;

import com.example.catalogueservice.dto.PartRequestDto;
import com.example.catalogueservice.dto.PartResponseDto;
import com.example.catalogueservice.entity.*;
import com.example.catalogueservice.exception.ResourceNotFoundException;
import com.example.catalogueservice.repository.BrandRepository;
import com.example.catalogueservice.repository.CategoryRepository;
import com.example.catalogueservice.repository.PartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogueServiceTest {

    @Mock
    private PartRepository partRepository;

    @Mock
    private OutboxService outboxService;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CatalogueService catalogueService;

    @Test
    void shouldSavePartAndWriteToOutbox() {
        UUID brandId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Brand brand = new Brand(brandId, "Toyota");
        Category category = new Category(catId, "Brakes", null);
        Money price = new Money(15000L, Currency.USD);

        PartRequestDto requestDto = new PartRequestDto(
                "SKU-123", "Brake Pad", brandId, catId, price, PartStatus.ACTIVE, null, null
        );
        Part savedPart = new Part(
                UUID.randomUUID(), "SKU-123", "Brake Pad", brand, category, price, PartStatus.ACTIVE, null, null
        );

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
        when(categoryRepository.findById(catId)).thenReturn(Optional.of(category));
        when(partRepository.save(any(Part.class))).thenReturn(savedPart);

        PartResponseDto responseDto = catalogueService.addPart(requestDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.sku()).isEqualTo("SKU-123");
        verify(brandRepository).findById(brandId);
        verify(categoryRepository).findById(catId);
        verify(partRepository).save(any(Part.class));
        verify(outboxService).saveEvent(eq("PART"), anyString(), eq("PartListed"), any());
    }

    @Test
    void shouldReturnPartById() {
        UUID partId = UUID.randomUUID();
        Brand brand = new Brand(UUID.randomUUID(), "Toyota");
        Category category = new Category(UUID.randomUUID(), "Brakes", null);
        Money price = new Money(15000L, Currency.USD);

        Part part = new Part(partId, "SKU-123", "Brake Pad", brand, category, price, PartStatus.ACTIVE, null, null);

        when(partRepository.findById(partId)).thenReturn(Optional.of(part));
        PartResponseDto responseDto = catalogueService.findPartById(partId);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.id()).isEqualTo(partId);
        assertThat(responseDto.sku()).isEqualTo("SKU-123");
        verify(partRepository, times(1)).findById(partId);
    }

    @Test
    void shouldReturnPartsForGivenVehicle() {
        String make = "Toyota";
        String model = "Hilux";
        int year = 2008;
        Brand brand = new Brand(UUID.randomUUID(), "Toyota");
        Category category = new Category(UUID.randomUUID(), "Brakes", null);
        Money price = new Money(15000L, Currency.USD);
        Part part = new Part(UUID.randomUUID(), "SKU-123", "Brake Pad", brand, category, price, PartStatus.ACTIVE, null, null);
        List<Part> expectedParts = List.of(part);

        when(partRepository.findByVehicleFitment(make, model, year)).thenReturn(expectedParts);

        List<PartResponseDto> actualParts = catalogueService.findPartsByVehicle(make, model, year);

        assertThat(actualParts).hasSize(1);
        assertThat(actualParts.get(0).sku()).isEqualTo("SKU-123");
        verify(partRepository, times(1)).findByVehicleFitment(make, model, year);
    }

    @Test
    void shouldReturnPartsForKeywordSearch() {
        String keyword = "brakes";
        Brand brand = new Brand(UUID.randomUUID(), "Toyota");
        Category category = new Category(UUID.randomUUID(), "Brakes", null);
        Money price = new Money(15000L, Currency.USD);
        Part part = new Part(UUID.randomUUID(), "SKU-123", "Brake Pad", brand, category, price, PartStatus.ACTIVE, null, null);
        List<Part> expectedParts = List.of(part);

        when(partRepository.searchParts(keyword)).thenReturn(expectedParts);

        List<PartResponseDto> actualParts = catalogueService.searchParts(keyword);

        assertThat(actualParts).hasSize(1);
        verify(partRepository, times(1)).searchParts(keyword);
    }

    @Test
    void shouldGetAllParts() {
        Brand brand = new Brand(UUID.randomUUID(), "Toyota");
        Category category = new Category(UUID.randomUUID(), "Brakes", null);
        Money price = new Money(15000L, Currency.USD);
        Part part = new Part(UUID.randomUUID(), "SKU-123", "Brake Pad", brand, category, price, PartStatus.ACTIVE, null, null);
        List<Part> expectedParts = List.of(part);

        when(partRepository.findAll()).thenReturn(expectedParts);

        List<PartResponseDto> actualParts = catalogueService.getAllParts();

        assertThat(actualParts).hasSize(1);
        verify(partRepository, times(1)).findAll();
    }

    @Test
    void shouldUpdatePart() {
        UUID id = UUID.randomUUID();
        UUID brandId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Brand brand = new Brand(brandId, "Toyota");
        Category category = new Category(catId, "Brakes", null);
        Money price = new Money(15000L, Currency.USD);

        PartRequestDto details = new PartRequestDto(
                "SKU-NEW", "New Name", brandId, catId, price, PartStatus.ACTIVE, null, null
        );
        Part savedPart = new Part(
                id, "SKU-NEW", "New Name", brand, category, price, PartStatus.ACTIVE, null, null
        );

        when(partRepository.findById(id)).thenReturn(Optional.of(savedPart));
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
        when(categoryRepository.findById(catId)).thenReturn(Optional.of(category));
        when(partRepository.save(any(Part.class))).thenReturn(savedPart);

        PartResponseDto updated = catalogueService.updatePart(id, details);

        assertThat(updated.sku()).isEqualTo("SKU-NEW");
        assertThat(updated.name()).isEqualTo("New Name");
        verify(partRepository, times(1)).findById(id);
        verify(partRepository, times(1)).save(any(Part.class));
        verify(outboxService, times(1)).saveEvent(eq("PART"), anyString(), eq("PartUpdated"), any());
    }

    @Test
    void shouldDeletePart() {
        UUID id = UUID.randomUUID();
        when(partRepository.existsById(id)).thenReturn(true);
        doNothing().when(partRepository).deleteById(id);

        catalogueService.deletePart(id);

        verify(partRepository, times(1)).deleteById(id);
        verify(outboxService, times(1)).saveEvent(eq("PART"), anyString(), eq("PartDeleted"), any());
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
                .isInstanceOf(ResourceNotFoundException.class)
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
                .isInstanceOf(ResourceNotFoundException.class)
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
