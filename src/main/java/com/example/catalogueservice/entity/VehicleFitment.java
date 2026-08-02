package com.example.catalogueservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "vehicle_fitments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleFitment {
    @Id
    private UUID id;

    private String make;
    private String model;
    private int yearFrom;
    private int yearTo;

    @PrePersist
    public void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}