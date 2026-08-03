package com.example.catalogueservice.service;

import com.example.catalogueservice.repository.OutboxRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OutboxServiceSadPathTest {

    private OutboxRepository outboxRepository;
    private ObjectMapper objectMapper;
    private OutboxService outboxService;

    @BeforeEach
    void setUp() {
        outboxRepository = mock(OutboxRepository.class);
        objectMapper = mock(ObjectMapper.class);
        outboxService = new OutboxService(outboxRepository, objectMapper);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenSerializationFails() throws Exception {
        Object invalidPayload = new Object();
        when(objectMapper.writeValueAsString(invalidPayload)).thenThrow(new RuntimeException("Serialization error"));

        assertThatThrownBy(() -> outboxService.saveEvent("PART", "123", "PartListed", invalidPayload))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to serialize outbox event payload");

        verify(outboxRepository, never()).save(any());
    }
}