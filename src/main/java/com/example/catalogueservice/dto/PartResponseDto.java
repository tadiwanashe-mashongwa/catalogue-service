package com.example.catalogueservice.dto;

import com.example.catalogueservice.entity.Money;
import com.example.catalogueservice.entity.PartImage;
import com.example.catalogueservice.entity.PartStatus;
import com.example.catalogueservice.entity.VehicleFitment;

import java.util.List;
import java.util.UUID;

public record PartResponseDto(
        UUID id,
        String sku,
        String name,
        UUID brandId,
        String brandName,
        UUID categoryId,
        String categoryName,
        Money price,
        PartStatus status,
        List<VehicleFitment> vehicleFitments,
        List<PartImage> images,
        Long version
) {
    public PartResponseDto(UUID id, String sku, String name, UUID brandId, String brandName, UUID categoryId,
                           String categoryName, Money price, PartStatus status, List<VehicleFitment> vehicleFitments,
                           List<PartImage> images) {
        this(id, sku, name, brandId, brandName, categoryId, categoryName, price, status, vehicleFitments, images, null);
    }
}
