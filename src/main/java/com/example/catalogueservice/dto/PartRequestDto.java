package com.example.catalogueservice.dto;

import com.example.catalogueservice.entity.Money;
import com.example.catalogueservice.entity.PartImage;
import com.example.catalogueservice.entity.PartStatus;
import com.example.catalogueservice.entity.VehicleFitment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record PartRequestDto(
        @NotBlank String sku,
        @NotBlank String name,
        @NotNull UUID brandId,
        @NotNull UUID categoryId,
        @NotNull Money price,
        @NotNull PartStatus status,
        List<VehicleFitment> vehicleFitments,
        List<PartImage> images
) {}