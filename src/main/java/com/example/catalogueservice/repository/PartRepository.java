package com.example.catalogueservice.repository;

import com.example.catalogueservice.entity.Part;
import com.example.catalogueservice.entity.PartStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PartRepository extends JpaRepository<Part, UUID> {

    @Query("SELECT DISTINCT p FROM Part p JOIN p.vehicleFitments f WHERE LOWER(f.make) = LOWER(:make) AND LOWER(f.model) = LOWER(:model) AND :year BETWEEN f.yearFrom AND f.yearTo")
    List<Part> findByVehicleFitment(@Param("make") String make, @Param("model") String model, @Param("year") int year);

    @Query("SELECT p FROM Part p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Part> searchParts(@Param("keyword") String keyword);

    @Query("""
            SELECT p FROM Part p
            WHERE p.status = :status
              AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Part> findByStatusAndKeyword(
            @Param("status") PartStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Page<Part> findByStatus(PartStatus status, Pageable pageable);

    @Query("SELECT p FROM Part p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Part> searchParts(@Param("keyword") String keyword, Pageable pageable);
}
