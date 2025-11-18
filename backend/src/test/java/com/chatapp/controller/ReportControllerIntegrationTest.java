package com.chatapp.controller;

import com.chatapp.dto.ReportRequestDTO;
import com.chatapp.model.*;
import com.chatapp.repository.*;
import com.chatapp.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ProgressPostRepository progressPostRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User adminUser;
    private Tenant testTenant;
    private String adminToken;

    @BeforeEach
    public void setup() {
        // Clean up
        progressPostRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        // Create test tenant
        testTenant = new Tenant();
        testTenant.setName("Test Company");
        testTenant.setSubdomain("testcompany");
        testTenant.setActive(true);
        testTenant.setCreatedAt(LocalDateTime.now());
        testTenant = tenantRepository.save(testTenant);

        // Create admin user
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setTenantId(testTenant.getId());
        adminUser.setActive(true);
        adminUser.setCreatedAt(LocalDateTime.now());

        // Get or create ADMIN role
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ROLE_ADMIN");
                    role.setDescription("Administrator role");
                    return roleRepository.save(role);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        adminUser.setRoles(roles);

        adminUser = userRepository.save(adminUser);

        // Generate auth token
        adminToken = "Bearer " + jwtTokenProvider.generateToken(adminUser.getUsername());

        // Create test progress posts
        createTestProgressPosts();
    }

    private void createTestProgressPosts() {
        for (int i = 0; i < 5; i++) {
            ProgressPost post = new ProgressPost();
            post.setUserId(adminUser.getId());
            post.setTenantId(testTenant.getId());
            post.setContent("Test post " + i);
            post.setCategory(i % 2 == 0 ? "achievement" : "challenge");
            post.setVisibility("public");
            post.setCreatedAt(LocalDateTime.now().minusDays(i));
            progressPostRepository.save(post);
        }
    }

    @Test
    public void testGenerateReport() throws Exception {
        ReportRequestDTO request = new ReportRequestDTO();
        request.setTenantId(testTenant.getId());
        request.setStartDate(LocalDate.now().minusDays(10));
        request.setEndDate(LocalDate.now());
        request.setReportType("progress_summary");

        mockMvc.perform(post("/api/reports/generate")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(testTenant.getId()))
                .andExpect(jsonPath("$.reportType").value("progress_summary"));
    }

    @Test
    public void testGetReportsByTenant() throws Exception {
        mockMvc.perform(get("/api/reports/tenant/" + testTenant.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testUnauthorizedReportAccess() throws Exception {
        mockMvc.perform(get("/api/reports/tenant/" + testTenant.getId()))
                .andExpect(status().isUnauthorized());
    }
}
