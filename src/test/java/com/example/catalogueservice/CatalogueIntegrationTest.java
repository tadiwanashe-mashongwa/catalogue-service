package com.example.catalogueservice;

import com.example.catalogueservice.entity.OutboxEvent;
import com.example.catalogueservice.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
class CatalogueIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
            .withDatabaseName("catalogue_db")
            .withUsername("sparelink")
            .withPassword("cashmoney");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    @KafkaListener(topics = "part-events", groupId = "test-group")
    void listen(String message) {
        messageQueue.add(message);
    }

    @Test
    void shouldPublishOutboxEventToKafka() throws Exception {
        OutboxEvent event = new OutboxEvent(
                null,
                "PART",
                UUID.randomUUID().toString(),
                "PART_CREATED",
                "{\"sku\":\"TEST-123\"}",
                Instant.now(),
                OutboxEvent.EventStatus.PENDING
        );
        outboxRepository.save(event);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            String consumedMessage = messageQueue.poll(1, TimeUnit.SECONDS);
            assertThat(consumedMessage).isNotNull();
            assertThat(consumedMessage).contains("TEST-123");
        });
    }

    @Test
    void shouldApplyInitialSchemaThroughFlyway() {
        Integer appliedMigrations = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where version = '1' and success = true",
                Integer.class
        );

        assertThat(appliedMigrations).isEqualTo(1);
    }
}
