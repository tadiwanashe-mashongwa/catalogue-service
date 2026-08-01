package com.example.catalogueservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "parts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Part {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Embedded
    private Money price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "part_id")
    private List<VehicleFitment> vehicleFitments;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "part_id")
    private List<PartImage> images;

    public boolean fits(Vehicle vehicle) {
        if (vehicleFitments == null) {
            return false;
        }
        return vehicleFitments.stream().anyMatch(fitment ->
                fitment.getMake().equalsIgnoreCase(vehicle.make()) &&
                        fitment.getModel().equalsIgnoreCase(vehicle.model()) &&
                        vehicle.year() >= fitment.getYearFrom() &&
                        vehicle.year() <= fitment.getYearTo()
        );
    }
}