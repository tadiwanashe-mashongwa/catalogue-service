package com.example.catalogueservice.repository;

import com.example.catalogueservice.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class PartRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindByVehicleFitment() {
        Brand brand = new Brand(UUID.randomUUID(), "Toyota");
        entityManager.persist(brand);

        Category category = new Category(UUID.randomUUID(), "Brakes", null);
        entityManager.persist(category);

        Money price = new Money(15000L, Currency.USD);

        VehicleFitment fitment = new VehicleFitment(UUID.randomUUID(), "Toyota", "Hilux", 2005, 2011);
        Part part = new Part(UUID.randomUUID(), "SKU-999", "Brake Pad", brand, category, price, PartStatus.ACTIVE, List.of(fitment), null);

        partRepository.save(part);

        List<Part> foundParts = partRepository.findByVehicleFitment("Toyota", "Hilux", 2008);

        assertThat(foundParts).isNotEmpty();
        assertThat(foundParts.get(0).getSku()).isEqualTo("SKU-999");
    }
    @Test
    void shouldSearchPartsByKeyword() {
        Brand brand = new Brand(UUID.randomUUID(), "Toyota");
        entityManager.persist(brand);

        Category category = new Category(UUID.randomUUID(), "Brakes", null);
        entityManager.persist(category);

        Money price = new Money(15000L, Currency.USD);
        Part part = new Part(UUID.randomUUID(), "SKU-ABC", "Brake Rotors", brand, category, price, PartStatus.ACTIVE, List.of(), null);
        partRepository.save(part);

        List<Part> results = partRepository.searchParts("rotors");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getSku()).isEqualTo("SKU-ABC");
    }
}