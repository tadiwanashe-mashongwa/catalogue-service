package com.example.catalogueservice.service;

import com.example.catalogueservice.entity.*;
import com.example.catalogueservice.repository.OutboxRepository;
import com.example.catalogueservice.repository.PartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

}