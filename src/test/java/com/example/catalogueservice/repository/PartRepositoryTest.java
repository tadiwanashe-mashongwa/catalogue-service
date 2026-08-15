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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

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

    @Test
    void shouldRejectAStalePartUpdate() {
        Brand brand = entityManager.persist(new Brand(UUID.randomUUID(), "Toyota"));
        Category category = entityManager.persist(new Category(UUID.randomUUID(), "Brakes", null));
        Part part = partRepository.saveAndFlush(new Part(
                UUID.randomUUID(), "SKU-LOCK", "Brake Pad", brand, category,
                new Money(15000L, Currency.USD), PartStatus.ACTIVE, List.of(), null
        ));

        entityManager.clear();
        Part firstCopy = partRepository.findById(part.getId()).orElseThrow();
        entityManager.detach(firstCopy);
        entityManager.clear();
        Part staleCopy = partRepository.findById(part.getId()).orElseThrow();
        entityManager.detach(staleCopy);

        firstCopy.rename("Updated Brake Pad");
        partRepository.saveAndFlush(firstCopy);

        staleCopy.rename("Stale Brake Pad");
        assertThatThrownBy(() -> partRepository.saveAndFlush(staleCopy))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void shouldUpdatePartCollectionsWithoutReplacingOrphanRemovalReferences() {
        Brand brand = entityManager.persist(new Brand(UUID.randomUUID(), "Toyota"));
        Category category = entityManager.persist(new Category(UUID.randomUUID(), "Brakes", null));
        Part part = partRepository.saveAndFlush(new Part(
                UUID.randomUUID(), "SKU-COLLECTIONS", "Brake Pad", brand, category,
                new Money(15000L, Currency.USD), PartStatus.ACTIVE,
                List.of(new VehicleFitment(UUID.randomUUID(), "Toyota", "Hilux", 2005, 2011)),
                List.of(new PartImage(UUID.randomUUID(), "https://example.com/original.jpg", UUID.randomUUID()))
        ));

        entityManager.clear();
        Part managedPart = partRepository.findById(part.getId()).orElseThrow();
        Part updatedPart = new Part(
                part.getId(), "SKU-COLLECTIONS", "Updated Brake Pad", brand, category,
                new Money(16000L, Currency.USD), PartStatus.ACTIVE,
                List.of(new VehicleFitment(UUID.randomUUID(), "Toyota", "Hilux", 2012, 2016)),
                List.of(new PartImage(UUID.randomUUID(), "https://example.com/updated.jpg", UUID.randomUUID()))
        );

        managedPart.updateDetails(updatedPart);
        partRepository.saveAndFlush(managedPart);

        entityManager.clear();
        Part reloadedPart = partRepository.findById(part.getId()).orElseThrow();
        assertThat(reloadedPart.getName()).isEqualTo("Updated Brake Pad");
        assertThat(reloadedPart.getVehicleFitments()).hasSize(1);
        assertThat(reloadedPart.getVehicleFitments().get(0).getYearFrom()).isEqualTo(2012);
        assertThat(reloadedPart.getImages()).hasSize(1);
        assertThat(reloadedPart.getImages().get(0).getUrl()).isEqualTo("https://example.com/updated.jpg");
    }
}
