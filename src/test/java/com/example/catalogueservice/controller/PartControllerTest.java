package com.example.catalogueservice.controller;

import com.example.catalogueservice.dto.PartRequestDto;
import com.example.catalogueservice.dto.PartResponseDto;
import com.example.catalogueservice.entity.Currency;
import com.example.catalogueservice.entity.Money;
import com.example.catalogueservice.entity.PartStatus;
import com.example.catalogueservice.exception.ResourceNotFoundException;
import com.example.catalogueservice.exception.StalePartVersionException;
import com.example.catalogueservice.service.CatalogueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration;
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

@WebMvcTest(PartController.class)
@ImportAutoConfiguration(exclude = {
        OAuth2ResourceServerAutoConfiguration.class,
        OAuth2ResourceServerWebSecurityAutoConfiguration.class
})
class PartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CatalogueService catalogueService;

    @Test
    void shouldCreatePart() throws Exception {
        UUID brandId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Money price = new Money(15000L, Currency.USD);
        PartRequestDto requestDto = new PartRequestDto("SKU-1", "Brake Pad", brandId, catId, price, PartStatus.ACTIVE, null, null);
        PartResponseDto responseDto = new PartResponseDto(UUID.randomUUID(), "SKU-1", "Brake Pad", brandId, "Toyota", catId, "Brakes", price, PartStatus.ACTIVE, null, null);

        when(catalogueService.addPart(any(PartRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sku").value("SKU-1"));
    }

    @Test
    void shouldGetAllParts() throws Exception {
        when(catalogueService.getAllParts()).thenReturn(List.of());

        mockMvc.perform(get("/api/parts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldGetPartById() throws Exception {
        UUID id = UUID.randomUUID();
        PartResponseDto responseDto = new PartResponseDto(
                id, "SKU-1", "Test", null, "Brand", null, "Cat", null, null, null, null
        );
        when(catalogueService.findPartById(id)).thenReturn(responseDto);

        mockMvc.perform(get("/api/parts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("SKU-1"));
    }

    @Test
    void shouldSearchParts() throws Exception {
        when(catalogueService.searchParts("brakes")).thenReturn(List.of());

        mockMvc.perform(get("/api/parts/search").param("keyword", "brakes"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFindPartsByVehicle() throws Exception {
        when(catalogueService.findPartsByVehicle("Toyota", "Hilux", 2008)).thenReturn(List.of());

        mockMvc.perform(get("/api/parts/vehicle")
                        .param("make", "Toyota")
                        .param("model", "Hilux")
                        .param("year", "2008"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdatePart() throws Exception {
        UUID id = UUID.randomUUID();
        UUID brandId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Money price = new Money(20000L, Currency.USD);
        PartRequestDto requestDto = new PartRequestDto("SKU-2", "Updated", brandId, catId, price, PartStatus.ACTIVE, null, null);
        PartResponseDto responseDto = new PartResponseDto(id, "SKU-2", "Updated", brandId, "Toyota", catId, "Brakes", price, PartStatus.ACTIVE, null, null);

        when(catalogueService.updatePart(eq(id), any(PartRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/parts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("SKU-2"));
    }

    @Test
    void shouldReturnConflictWhenUpdatingPartWithStaleVersion() throws Exception {
        UUID id = UUID.randomUUID();
        UUID brandId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Money price = new Money(20000L, Currency.USD);
        PartRequestDto requestDto = new PartRequestDto(
                "SKU-2", "Updated", brandId, catId, price, PartStatus.ACTIVE, null, null, 1L
        );

        when(catalogueService.updatePart(eq(id), any(PartRequestDto.class)))
                .thenThrow(new StalePartVersionException("Part version 1 is stale; current version is 2."));

        mockMvc.perform(put("/api/parts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Part version 1 is stale; current version is 2."));
    }

    @Test
    void shouldDeletePart() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(catalogueService).deletePart(id);

        mockMvc.perform(delete("/api/parts/{id}", id))
                .andExpect(status().isOk());
    }
    @Test
    void shouldReturnNotFoundWhenPartDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(catalogueService.findPartById(id)).thenThrow(new ResourceNotFoundException("Part not found"));

        mockMvc.perform(get("/api/parts/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Part not found"));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingPartWithInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
