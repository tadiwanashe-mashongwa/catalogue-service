package com.example.catalogueservice.service;

import com.example.catalogueservice.dto.PartRequestDto;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogueServiceSadPathTest {

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
    void shouldThrowExceptionWhenAddingPartWithInvalidBrand() {
        UUID brandId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Money price = new Money(15000L, Currency.USD);
        PartRequestDto requestDto = new PartRequestDto(
                "SKU-123", "Brake Pad", brandId, catId, price, PartStatus.ACTIVE, null, null
        );

        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogueService.addPart(requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Brand not found");

        verify(partRepository, never()).save(any());
        verify(outboxService, never()).saveEvent(any(), any(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentPart() {
        UUID id = UUID.randomUUID();
        UUID brandId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Money price = new Money(15000L, Currency.USD);
        PartRequestDto requestDto = new PartRequestDto(
                "SKU-123", "Brake Pad", brandId, catId, price, PartStatus.ACTIVE, null, null
        );

        when(partRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> catalogueService.updatePart(id, requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Part not found");

        verify(partRepository, never()).save(any());
        verify(outboxService, never()).saveEvent(any(), any(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentPart() {
        UUID id = UUID.randomUUID();

        when(partRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> catalogueService.deletePart(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Part not found");

        verify(partRepository, never()).deleteById(any());
        verify(outboxService, never()).saveEvent(any(), any(), any(), any());
    }
}