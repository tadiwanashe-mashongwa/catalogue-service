package com.example.catalogueservice.service;

import com.example.catalogueservice.entity.*;
import com.example.catalogueservice.repository.OutboxRepository;
import com.example.catalogueservice.repository.PartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

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
}