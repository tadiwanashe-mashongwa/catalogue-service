package com.example.catalogueservice.scheduler;

import com.example.catalogueservice.entity.OutboxEvent;
import com.example.catalogueservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void pollAndPublishEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatus(OutboxEvent.EventStatus.PENDING);

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send(event.getAggregateType().toLowerCase() + "-events", event.getAggregateId(), event.getPayload())
                        .get();

                event.setStatus(OutboxEvent.EventStatus.PROCESSED);
                outboxRepository.save(event);
                log.info("Successfully published outbox event id: {} to Kafka", event.getId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event id: {} to Kafka", event.getId(), e);
                event.setStatus(OutboxEvent.EventStatus.FAILED);
                outboxRepository.save(event);
            }
        }
    }
}