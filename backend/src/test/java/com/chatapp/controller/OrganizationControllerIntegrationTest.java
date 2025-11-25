package com.chatapp.controller;

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
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class OrganizationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Tenant testTenant;
    private Organization rootOrg;

    @BeforeEach
    public void setup() {
        // Clean up
        organizationRepository.deleteAll();
        tenantRepository.deleteAll();

        // Create test tenant
        testTenant = new Tenant();
        testTenant.setName("Test Company");
        testTenant.setSlug("testcompany");
        testTenant.setStatus(TenantStatus.ACTIVE);
        testTenant.setCreatedAt(LocalDateTime.now());
        testTenant = tenantRepository.save(testTenant);

        // Create root organization
        rootOrg = new Organization();
        rootOrg.setName("本社");
        rootOrg.setTenant(testTenant);
        rootOrg.setParent(null);
        rootOrg.setLevel(0);
        rootOrg.setPath("/");
        rootOrg.setCreatedAt(LocalDateTime.now());
        rootOrg = organizationRepository.save(rootOrg);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCreateOrganization() throws Exception {
        Map<String, Object> orgData = new HashMap<>();
        orgData.put("name", "開発部");
        orgData.put("tenantId", testTenant.getId());
        orgData.put("parentId", rootOrg.getId());

        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orgData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("開発部"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetOrganizationsByTenant() throws Exception {
        // Create additional organization
        Organization subOrg = new Organization();
        subOrg.setName("営業部");
        subOrg.setTenant(testTenant);
        subOrg.setParent(rootOrg);
        subOrg.setLevel(1);
        subOrg.setPath("/" + rootOrg.getId() + "/");
        subOrg.setCreatedAt(LocalDateTime.now());
        organizationRepository.save(subOrg);

        mockMvc.perform(get("/api/organizations/tenant/" + testTenant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("本社", "営業部")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetOrganizationHierarchy() throws Exception {
        // Create child organization
        Organization child = new Organization();
        child.setName("開発チーム1");
        child.setTenant(testTenant);
        child.setParent(rootOrg);
        child.setLevel(1);
        child.setPath("/" + rootOrg.getId() + "/");
        child.setCreatedAt(LocalDateTime.now());
        organizationRepository.save(child);

        mockMvc.perform(get("/api/organizations/" + rootOrg.getId() + "/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("開発チーム1"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUpdateOrganization() throws Exception {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "本社（更新）");

        mockMvc.perform(put("/api/organizations/" + rootOrg.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("本社（更新）"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeleteOrganization() throws Exception {
        mockMvc.perform(delete("/api/organizations/" + rootOrg.getId()))
                .andExpect(status().isOk());

        // Verify organization is deleted or deactivated
        Organization deleted = organizationRepository.findById(rootOrg.getId()).orElse(null);
        assert deleted == null || !deleted.getIsActive();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testCreateOrganization_AsUser_Forbidden() throws Exception {
        Map<String, Object> orgData = new HashMap<>();
        orgData.put("name", "開発部");
        orgData.put("tenantId", testTenant.getId());

        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orgData)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/organizations/tenant/" + testTenant.getId()))
                .andExpect(status().isUnauthorized());
    }
}
