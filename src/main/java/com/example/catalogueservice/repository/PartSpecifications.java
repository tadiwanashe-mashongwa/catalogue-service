package com.example.catalogueservice.repository;

import com.example.catalogueservice.entity.Part;
import com.example.catalogueservice.entity.PartStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class PartSpecifications {

    private PartSpecifications() {
    }

    public static Specification<Part> withFilters(UUID brandId, UUID categoryId, PartStatus status, String keyword) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            if (brandId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("brand").get("id"), brandId));
            }
            if (categoryId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (status != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), pattern)
                ));
            }
            return predicate;
        };
    }
}
