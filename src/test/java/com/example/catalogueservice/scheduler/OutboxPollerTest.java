package com.example.catalogueservice.scheduler;

import com.example.catalogueservice.entity.OutboxEvent;
import com.example.catalogueservice.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPollerTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OutboxPoller outboxPoller;

    @Test
    void shouldPollAndPublishPendingEventsSuccessfully() throws Exception {
        UUID eventId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.builder()
                .id(eventId)
                .aggregateType("PART")
                .aggregateId("123")
                .eventType("PartListed")
                .payload("{\"test\":\"data\"}")
                .createdAt(Instant.now())
                .status(OutboxEvent.EventStatus.PENDING)
                .build();

        when(outboxRepository.findByStatus(OutboxEvent.EventStatus.PENDING))
                .thenReturn(List.of(event));

        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        outboxPoller.pollAndPublishEvents();

        assertThat(event.getStatus()).isEqualTo(OutboxEvent.EventStatus.PROCESSED);
        verify(kafkaTemplate, times(1)).send("part-events", "123", "{\"test\":\"data\"}");
        verify(outboxRepository, times(1)).save(event);
    }
}