package com.example.catalogueservice.repository;

import com.example.catalogueservice.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PartRepository  extends JpaRepository<Part,UUID> {
}
