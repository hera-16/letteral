package com.chatapp.controller;

import com.chatapp.dto.TenantDTO;
import com.chatapp.model.*;
import com.chatapp.model.enums.TenantStatus;
import com.chatapp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TenantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant testTenant;

    @BeforeEach
    public void setup() {
        // Clean up
        tenantRepository.deleteAll();

        // Create test tenant
        testTenant = new Tenant();
        testTenant.setName("Test Company");
        testTenant.setSlug("testcompany");
        testTenant.setStatus(TenantStatus.ACTIVE);
        testTenant.setCreatedAt(LocalDateTime.now());
        testTenant = tenantRepository.save(testTenant);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCreateTenant_AsAdmin() throws Exception {
        TenantDTO tenantDTO = new TenantDTO();
        tenantDTO.setName("New Company");
        tenantDTO.setSlug("newcompany");
        tenantDTO.setPlanType("FREE");

        mockMvc.perform(post("/api/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tenantDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Company"))
                .andExpect(jsonPath("$.slug").value("newcompany"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testCreateTenant_AsRegularUser_Forbidden() throws Exception {
        TenantDTO tenantDTO = new TenantDTO();
        tenantDTO.setName("New Company");
        tenantDTO.setSlug("newcompany");
        tenantDTO.setPlanType("FREE");

        mockMvc.perform(post("/api/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tenantDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetAllTenants() throws Exception {
        mockMvc.perform(get("/api/tenants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name").value("Test Company"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetTenantById() throws Exception {
        mockMvc.perform(get("/api/tenants/" + testTenant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Company"))
                .andExpect(jsonPath("$.slug").value("testcompany"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUpdateTenant() throws Exception {
        TenantDTO updateDTO = new TenantDTO();
        updateDTO.setName("Updated Company");
        updateDTO.setSlug("testcompany");
        updateDTO.setPlanType("PRO");

        mockMvc.perform(put("/api/tenants/" + testTenant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Company"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeactivateTenant() throws Exception {
        mockMvc.perform(delete("/api/tenants/" + testTenant.getId()))
                .andExpect(status().isOk());

        // Verify tenant is deactivated
        Tenant deactivated = tenantRepository.findById(testTenant.getId()).orElse(null);
        assert deactivated != null;
        assert deactivated.getStatus() == TenantStatus.SUSPENDED || deactivated.getStatus() == TenantStatus.CLOSED;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCreateTenantWithDuplicateSlug() throws Exception {
        TenantDTO tenantDTO = new TenantDTO();
        tenantDTO.setName("Another Company");
        tenantDTO.setSlug("testcompany"); // Same slug as testTenant
        tenantDTO.setPlanType("FREE");

        mockMvc.perform(post("/api/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tenantDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/tenants"))
                .andExpect(status().isUnauthorized());
    }
}
