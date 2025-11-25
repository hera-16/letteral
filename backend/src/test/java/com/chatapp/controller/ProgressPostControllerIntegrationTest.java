package com.chatapp.controller;

import com.chatapp.dto.ProgressPostDTO;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProgressPostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private GroupRepository groupRepository;

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

    private User testUser;
    private Tenant testTenant;
    private Organization testOrganization;
    private Group testGroup;
    private String authToken;

    @BeforeEach
    public void setup() {
        // Clean up
        progressPostRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();
        groupRepository.deleteAll();
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

        // Create test group
        testGroup = new Group();
        testGroup.setName("Test Group");
        testGroup.setGroupType(Group.GroupType.PUBLIC_TOPIC);
        testGroup.setCreatedAt(LocalDateTime.now());

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setTenantId(testTenant.getId());
        testUser.setPrimaryOrganizationId(testOrganization.getId());
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());

        // Get or create USER role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ROLE_USER");
                    role.setDescription("Standard user role");
                    return roleRepository.save(role);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);

        testUser = userRepository.save(testUser);

        // Now save group with creator
        testGroup.setCreator(testUser);
        testGroup = groupRepository.save(testGroup);

        // Generate auth token
        authToken = "Bearer " + jwtTokenProvider.generateToken(testUser.getUsername());
    }

    @Test
    public void testCreateProgressPost() throws Exception {
        ProgressPostDTO postDTO = new ProgressPostDTO();
        postDTO.setContent("Today I completed the user authentication feature");
        postDTO.setCategory("achievement");
        postDTO.setVisibility("public");

        mockMvc.perform(post("/api/progress")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Today I completed the user authentication feature"))
                .andExpect(jsonPath("$.category").value("achievement"))
                .andExpect(jsonPath("$.visibility").value("public"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));
    }

    @Test
    public void testGetProgressPostsByUser() throws Exception {
        // Create test progress posts
        ProgressPost post1 = new ProgressPost();
        post1.setTenant(testTenant);
        post1.setOrganization(testOrganization);
        post1.setAuthor(testUser);
        post1.setContent("Post 1");
        post1.setPostDate(LocalDate.now());
        post1.setVisibility(Visibility.ORGANIZATION);
        post1.setCreatedAt(LocalDateTime.now());
        progressPostRepository.save(post1);

        ProgressPost post2 = new ProgressPost();
        post2.setTenant(testTenant);
        post2.setOrganization(testOrganization);
        post2.setAuthor(testUser);
        post2.setContent("Post 2");
        post2.setPostDate(LocalDate.now());
        post2.setVisibility(Visibility.ORGANIZATION);
        post2.setCreatedAt(LocalDateTime.now());
        progressPostRepository.save(post2);

        mockMvc.perform(get("/api/progress/user/" + testUser.getId())
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].content", containsInAnyOrder("Post 1", "Post 2")));
    }

    @Test
    public void testGetProgressPostsByTenant() throws Exception {
        // Create progress post
        ProgressPost post = new ProgressPost();
        post.setTenant(testTenant);
        post.setOrganization(testOrganization);
        post.setAuthor(testUser);
        post.setContent("Tenant post");
        post.setPostDate(LocalDate.now());
        post.setVisibility(Visibility.COMPANY);
        post.setCreatedAt(LocalDateTime.now());
        progressPostRepository.save(post);

        mockMvc.perform(get("/api/progress/tenant/" + testTenant.getId())
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].content").value("Tenant post"));
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/progress/user/" + testUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateProgressPostWithInvalidData() throws Exception {
        ProgressPostDTO postDTO = new ProgressPostDTO();
        // Missing required content
        postDTO.setCategory("achievement");

        mockMvc.perform(post("/api/progress")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDTO)))
                .andExpect(status().isBadRequest());
    }
}
