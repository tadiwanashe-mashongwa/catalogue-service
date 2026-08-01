package com.example.catalogueservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "part_images")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PartImage {
    @Id
    private UUID id;
    private String url;
    private UUID embeddingId;
}