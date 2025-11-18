package com.chatapp.controller;

import com.chatapp.dto.ProgressPostDTO;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private Tenant testTenant;
    private Group testGroup;
    private String authToken;

    @BeforeEach
    public void setup() {
        // Clean up
        progressPostRepository.deleteAll();
        userRepository.deleteAll();
        groupRepository.deleteAll();
        tenantRepository.deleteAll();

        // Create test tenant
        testTenant = new Tenant();
        testTenant.setName("Test Company");
        testTenant.setSubdomain("testcompany");
        testTenant.setActive(true);
        testTenant.setCreatedAt(LocalDateTime.now());
        testTenant = tenantRepository.save(testTenant);

        // Create test group
        testGroup = new Group();
        testGroup.setName("Test Group");
        testGroup.setTenantId(testTenant.getId());
        testGroup.setCreatedAt(LocalDateTime.now());
        testGroup = groupRepository.save(testGroup);

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setTenantId(testTenant.getId());
        testUser.setActive(true);
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
        post1.setUserId(testUser.getId());
        post1.setTenantId(testTenant.getId());
        post1.setContent("Post 1");
        post1.setCategory("achievement");
        post1.setVisibility("public");
        post1.setCreatedAt(LocalDateTime.now());
        progressPostRepository.save(post1);

        ProgressPost post2 = new ProgressPost();
        post2.setUserId(testUser.getId());
        post2.setTenantId(testTenant.getId());
        post2.setContent("Post 2");
        post2.setCategory("challenge");
        post2.setVisibility("public");
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
        post.setUserId(testUser.getId());
        post.setTenantId(testTenant.getId());
        post.setContent("Tenant post");
        post.setCategory("achievement");
        post.setVisibility("public");
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
