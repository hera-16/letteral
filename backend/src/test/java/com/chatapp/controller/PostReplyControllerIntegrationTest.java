package com.chatapp.controller;

import com.chatapp.dto.CreatePostReplyRequest;
import com.chatapp.model.*;
import com.chatapp.model.enums.TenantStatus;
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

/**
 * PostReplyControllerの統合テスト
 * 返信作成、取得のエンドポイントをテスト
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PostReplyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProgressPostRepository progressPostRepository;

    @Autowired
    private PostReplyRepository postReplyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private User adminUser;
    private Tenant testTenant;
    private Organization testOrganization;
    private ProgressPost testPost;
    private String userToken;
    private String adminToken;

    @BeforeEach
    public void setup() {
        // Clean up
        postReplyRepository.deleteAll();
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
        testOrganization.setLevel(0);
        testOrganization.setPath("/Test Organization");
        testOrganization.setIsActive(true);
        testOrganization.setCreatedAt(LocalDateTime.now());
        testOrganization = organizationRepository.save(testOrganization);

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

        // Create test user (post author)
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setTenantId(testTenant.getId());
        testUser.setPrimaryOrganizationId(testOrganization.getId());
        testUser.setIsActive(true);
        testUser.setRoles(roles);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // Create admin user
        adminUser = new User();
        adminUser.setUsername("adminuser");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setTenantId(testTenant.getId());
        adminUser.setPrimaryOrganizationId(testOrganization.getId());
        adminUser.setIsActive(true);
        adminUser.setRoles(roles);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser = userRepository.save(adminUser);

        // Create test progress post
        testPost = new ProgressPost();
        testPost.setTenant(testTenant);
        testPost.setOrganization(testOrganization);
        testPost.setAuthor(testUser);
        testPost.setTitle("Test Post");
        testPost.setContent("Test progress post content");
        testPost.setPostDate(LocalDate.now());
        testPost.setCreatedAt(LocalDateTime.now());
        testPost = progressPostRepository.save(testPost);

        // Generate auth tokens
        userToken = "Bearer " + jwtTokenProvider.generateToken(testUser.getUsername());
        adminToken = "Bearer " + jwtTokenProvider.generateToken(adminUser.getUsername());
    }

    @Test
    public void testCreateReply_ByAuthor_Success() throws Exception {
        // Arrange
        CreatePostReplyRequest request = new CreatePostReplyRequest();
        request.setPostId(testPost.getId());
        request.setContent("This is my reply to my own post");

        // Act & Assert
        mockMvc.perform(post("/api/replies")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value(testPost.getId()))
                .andExpect(jsonPath("$.content").value("This is my reply to my own post"))
                .andExpect(jsonPath("$.authorId").value(testUser.getId()));
    }

    @Test
    public void testCreateReply_Unauthorized_Returns403() throws Exception {
        // Arrange
        CreatePostReplyRequest request = new CreatePostReplyRequest();
        request.setPostId(testPost.getId());
        request.setContent("Unauthorized reply attempt");

        // Act & Assert - adminUser trying to reply to testUser's post without proper permissions
        mockMvc.perform(post("/api/replies")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateReply_WithoutAuth_Returns401() throws Exception {
        // Arrange
        CreatePostReplyRequest request = new CreatePostReplyRequest();
        request.setPostId(testPost.getId());
        request.setContent("No auth reply");

        // Act & Assert
        mockMvc.perform(post("/api/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateReply_InvalidPostId_Returns404() throws Exception {
        // Arrange
        CreatePostReplyRequest request = new CreatePostReplyRequest();
        request.setPostId(999L);
        request.setContent("Reply to non-existent post");

        // Act & Assert
        mockMvc.perform(post("/api/replies")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateReply_EmptyContent_Returns400() throws Exception {
        // Arrange
        CreatePostReplyRequest request = new CreatePostReplyRequest();
        request.setPostId(testPost.getId());
        request.setContent("");

        // Act & Assert
        mockMvc.perform(post("/api/replies")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetRepliesByPostId_ByAuthor_Success() throws Exception {
        // Arrange
        PostReply reply1 = new PostReply();
        reply1.setPost(testPost);
        reply1.setAuthor(testUser);
        reply1.setContent("First reply");
        reply1.setCreatedAt(LocalDateTime.now());
        postReplyRepository.save(reply1);

        PostReply reply2 = new PostReply();
        reply2.setPost(testPost);
        reply2.setAuthor(testUser);
        reply2.setContent("Second reply");
        reply2.setCreatedAt(LocalDateTime.now().plusMinutes(5));
        postReplyRepository.save(reply2);

        // Act & Assert
        mockMvc.perform(get("/api/replies/post/" + testPost.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content").value("First reply"))
                .andExpect(jsonPath("$[1].content").value("Second reply"));
    }

    @Test
    public void testGetRepliesByPostId_Unauthorized_ReturnsEmptyList() throws Exception {
        // Arrange
        PostReply reply = new PostReply();
        reply.setPost(testPost);
        reply.setAuthor(testUser);
        reply.setContent("Private reply");
        reply.setCreatedAt(LocalDateTime.now());
        postReplyRepository.save(reply);

        // Act & Assert - adminUser trying to view testUser's post replies without permission
        mockMvc.perform(get("/api/replies/post/" + testPost.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetRepliesByPostId_WithoutAuth_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/replies/post/" + testPost.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetRepliesByPostId_InvalidPostId_Returns404() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/replies/post/999")
                        .header("Authorization", userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetReplyCount_Success() throws Exception {
        // Arrange
        PostReply reply1 = new PostReply();
        reply1.setPost(testPost);
        reply1.setAuthor(testUser);
        reply1.setContent("Reply 1");
        reply1.setCreatedAt(LocalDateTime.now());
        postReplyRepository.save(reply1);

        PostReply reply2 = new PostReply();
        reply2.setPost(testPost);
        reply2.setAuthor(testUser);
        reply2.setContent("Reply 2");
        reply2.setCreatedAt(LocalDateTime.now());
        postReplyRepository.save(reply2);

        // Act & Assert
        mockMvc.perform(get("/api/replies/post/" + testPost.getId() + "/count")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    public void testGetReplyCount_NoReplies_ReturnsZero() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/replies/post/" + testPost.getId() + "/count")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    public void testMultipleReplies_InOrder_Success() throws Exception {
        // Arrange & Act - Create multiple replies
        for (int i = 1; i <= 3; i++) {
            CreatePostReplyRequest request = new CreatePostReplyRequest();
            request.setPostId(testPost.getId());
            request.setContent("Reply " + i);

            mockMvc.perform(post("/api/replies")
                            .header("Authorization", userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Assert - Check replies are returned in order
        mockMvc.perform(get("/api/replies/post/" + testPost.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].content").value("Reply 1"))
                .andExpect(jsonPath("$[1].content").value("Reply 2"))
                .andExpect(jsonPath("$[2].content").value("Reply 3"));
    }
}
