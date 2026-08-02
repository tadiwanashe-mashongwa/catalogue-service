package com.example.catalogueservice.repository;

import com.example.catalogueservice.entity.Part;
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
}