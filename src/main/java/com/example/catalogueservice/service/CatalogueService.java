package com.example.catalogueservice.service;

import com.example.catalogueservice.entity.OutboxEvent;
import com.example.catalogueservice.entity.Part;
import com.example.catalogueservice.repository.OutboxRepository;
import com.example.catalogueservice.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogueService {
    private final PartRepository partRepository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public Part addPart(Part part) {
        UUID partId = part.getId() != null ? part.getId() : UUID.randomUUID();

        Part partToSave = new Part(
                partId,
                part.getSku(),
                part.getName(),
                part.getBrand(),
                part.getCategory(),
                part.getPrice(),
                part.getStatus(),
                part.getVehicleFitments(),
                part.getImages()
        );

        Part savedPart = partRepository.save(partToSave);

        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                "PART",
                savedPart.getId(),
                "PartListed",
                "{\"partId\":\"" + savedPart.getId() + "\"}",
                Instant.now()
        );
        outboxRepository.save(event);
        return savedPart;
    }
}