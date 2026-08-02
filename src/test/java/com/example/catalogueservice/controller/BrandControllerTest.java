package com.example.catalogueservice.controller;

import com.example.catalogueservice.entity.Brand;
import com.example.catalogueservice.service.CatalogueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BrandController.class)
class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CatalogueService catalogueService;

    @Test
    void shouldCreateBrand() throws Exception {
        Brand brand = new Brand(UUID.randomUUID(), "Honda");
        when(catalogueService.createBrand(any(Brand.class))).thenReturn(brand);

        mockMvc.perform(post("/api/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brand)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Honda"));
    }

    @Test
    void shouldGetAllBrands() throws Exception {
        when(catalogueService.getAllBrands()).thenReturn(List.of(new Brand(UUID.randomUUID(), "Toyota")));

        mockMvc.perform(get("/api/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Toyota"));
    }

    @Test
    void shouldGetBrandById() throws Exception {
        UUID id = UUID.randomUUID();
        Brand brand = new Brand(id, "Toyota");
        when(catalogueService.getBrandById(id)).thenReturn(brand);

        mockMvc.perform(get("/api/brands/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Toyota"));
    }

    @Test
    void shouldUpdateBrand() throws Exception {
        UUID id = UUID.randomUUID();
        Brand brand = new Brand(id, "Toyota Updated");
        when(catalogueService.updateBrand(eq(id), any(Brand.class))).thenReturn(brand);

        mockMvc.perform(put("/api/brands/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brand)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Toyota Updated"));
    }

    @Test
    void shouldDeleteBrand() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(catalogueService).deleteBrand(id);

        mockMvc.perform(delete("/api/brands/{id}", id))
                .andExpect(status().isOk());
    }
}