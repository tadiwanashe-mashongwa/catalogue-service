package com.example.catalogueservice.config;

import com.example.catalogueservice.controller.PartController;
import com.example.catalogueservice.dto.PartRequestDto;
import com.example.catalogueservice.dto.PartResponseDto;
import com.example.catalogueservice.entity.Currency;
import com.example.catalogueservice.entity.Money;
import com.example.catalogueservice.entity.PartStatus;
import com.example.catalogueservice.service.CatalogueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PartController.class)
@Import(SecurityConfig.class)
@ImportAutoConfiguration({
        ServletWebSecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        OAuth2ResourceServerWebSecurityAutoConfiguration.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CatalogueService catalogueService;

    @Test
    void shouldAllowPublicCatalogueReads() throws Exception {
        when(catalogueService.getAllParts()).thenReturn(List.of());

        mockMvc.perform(get("/api/parts"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectUnauthenticatedPartCreation() throws Exception {
        mockMvc.perform(post("/api/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectCustomerPartCreation() throws Exception {
        mockMvc.perform(post("/api/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody())
                        .with(jwt().authorities(createAuthorityList("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminPartCreation() throws Exception {
        when(catalogueService.addPart(any(PartRequestDto.class))).thenReturn(new PartResponseDto(
                UUID.randomUUID(), "SKU-1", "Brake Pad", UUID.randomUUID(), "Toyota",
                UUID.randomUUID(), "Brakes", new Money(15000L, Currency.USD),
                PartStatus.ACTIVE, null, null
        ));

        mockMvc.perform(post("/api/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody())
                        .with(jwt().authorities(createAuthorityList("ROLE_ADMIN"))))
                .andExpect(status().isCreated());
    }

    private String requestBody() throws Exception {
        return objectMapper.writeValueAsString(new PartRequestDto(
                "SKU-1", "Brake Pad", UUID.randomUUID(), UUID.randomUUID(),
                new Money(15000L, Currency.USD), PartStatus.ACTIVE, null, null
        ));
    }
}
