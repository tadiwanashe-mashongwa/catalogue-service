package com.example.catalogueservice.service;

import com.example.catalogueservice.entity.OutboxEvent;
import com.example.catalogueservice.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OutboxServiceTest {

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
    void shouldSaveEventSuccessfully() throws Exception {
        Object payload = new Object();
        when(objectMapper.writeValueAsString(payload)).thenReturn("{\"test\":\"payload\"}");

        outboxService.saveEvent("Part", "123", "PART_CREATED", payload);

        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository, times(1)).save(eventCaptor.capture());

        OutboxEvent savedEvent = eventCaptor.getValue();
        assertThat(savedEvent.getAggregateType()).isEqualTo("Part");
        assertThat(savedEvent.getAggregateId()).isEqualTo("123");
        assertThat(savedEvent.getEventType()).isEqualTo("PART_CREATED");
        assertThat(savedEvent.getPayload()).isEqualTo("{\"test\":\"payload\"}");
        assertThat(savedEvent.getStatus()).isEqualTo(OutboxEvent.EventStatus.PENDING);
    }
}