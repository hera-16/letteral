package com.chatapp.controller;

import com.chatapp.dto.ReportRequestDTO;
import com.chatapp.model.*;
import com.chatapp.model.enums.TenantStatus;
import com.chatapp.model.enums.Visibility;
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
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User adminUser;
    private Tenant testTenant;
    private Organization testOrganization;
    private String adminToken;

    @BeforeEach
    public void setup() {
        // Clean up
        progressPostRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();
        tenantRepository.deleteAll();

        // Create test tenant
        testTenant = new Tenant();
        testTenant.setName("Test Company");
        testTenant.setSlug("testcompany");
        testTenant.setStatus(TenantStatus.ACTIVE);
        testTenant.setCreatedAt(LocalDateTime.now());
        testTenant = tenantRepository.save(testTenant);

        // Create test organization
        testOrganization = new Organization();
        testOrganization.setName("Test Organization");
        testOrganization.setTenant(testTenant);
        testOrganization.setLevel(1);
        testOrganization.setCreatedAt(LocalDateTime.now());
        testOrganization = organizationRepository.save(testOrganization);

        // Create admin user
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setTenantId(testTenant.getId());
        adminUser.setPrimaryOrganizationId(testOrganization.getId());
        adminUser.setIsActive(true);
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
            post.setTenant(testTenant);
            post.setOrganization(testOrganization);
            post.setAuthor(adminUser);
            post.setContent("Test post " + i);
            post.setPostDate(LocalDate.now().minusDays(i));
            post.setVisibility(Visibility.COMPANY);
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
