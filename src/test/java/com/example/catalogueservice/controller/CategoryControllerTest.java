package com.example.catalogueservice.controller;

import com.example.catalogueservice.entity.Category;
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

@WebMvcTest(CategoryController.class)
@ImportAutoConfiguration(exclude = {
        OAuth2ResourceServerAutoConfiguration.class,
        OAuth2ResourceServerWebSecurityAutoConfiguration.class
})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CatalogueService catalogueService;

    @Test
    void shouldCreateCategory() throws Exception {
        Category category = new Category(UUID.randomUUID(), "Engine", null);
        when(catalogueService.createCategory(any(Category.class))).thenReturn(category);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Engine"));
    }

    @Test
    void shouldGetAllCategories() throws Exception {
        when(catalogueService.getAllCategories()).thenReturn(List.of(new Category(UUID.randomUUID(), "Brakes", null)));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Brakes"));
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        UUID id = UUID.randomUUID();
        Category category = new Category(id, "Brakes", null);
        when(catalogueService.getCategoryById(id)).thenReturn(category);

        mockMvc.perform(get("/api/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Brakes"));
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        UUID id = UUID.randomUUID();
        Category category = new Category(id, "Engine Updated", null);
        when(catalogueService.updateCategory(eq(id), any(Category.class))).thenReturn(category);

        mockMvc.perform(put("/api/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Engine Updated"));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(catalogueService).deleteCategory(id);

        mockMvc.perform(delete("/api/categories/{id}", id))
                .andExpect(status().isOk());
    }
}
