package com.chatapp.service;

import com.chatapp.dto.CreatePostReplyRequest;
import com.chatapp.dto.PostReplyDTO;
import com.chatapp.model.*;
import com.chatapp.model.enums.OrganizationRole;
import com.chatapp.repository.PostReplyRepository;
import com.chatapp.repository.ProgressPostRepository;
import com.chatapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PostReplyServiceのユニットテスト
 * 返信の作成、取得、権限チェックをテスト
 */
@ExtendWith(MockitoExtension.class)
class PostReplyServiceTest {

    @Mock
    private PostReplyRepository postReplyRepository;

    @Mock
    private ProgressPostRepository progressPostRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationPermissionService organizationPermissionService;

    @InjectMocks
    private PostReplyService postReplyService;

    private User testUser;
    private User adminUser;
    private Organization testOrganization;
    private ProgressPost testPost;
    private PostReply testReply;

    @BeforeEach
    void setUp() {
        // テスト組織
        testOrganization = new Organization();
        testOrganization.setId(1L);
        testOrganization.setName("Test Organization");

        // テストユーザー（投稿者）
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        // 管理者ユーザー
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("adminuser");

        // テスト投稿
        testPost = new ProgressPost();
        testPost.setId(1L);
        testPost.setAuthor(testUser);
        testPost.setOrganization(testOrganization);
        testPost.setContent("Test progress post");
        testPost.setCreatedAt(LocalDateTime.now());

        // テスト返信
        testReply = new PostReply();
        testReply.setId(1L);
        testReply.setPost(testPost);
        testReply.setAuthor(testUser);
        testReply.setContent("Test reply");
        testReply.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateReply_ByAuthor_Success() {
        // Arrange
        CreatePostReplyRequest request = new CreatePostReplyRequest();
        request.setPostId(1L);
        request.setContent("New reply content");

        when(progressPostRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(postReplyRepository.save(any(PostReply.class))).thenReturn(testReply);
        when(organizationPermissionService.getUserRole(testUser, 1L))
                .thenReturn(Optional.empty());

        // Act
        PostReplyDTO result = postReplyService.createReply(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(testReply.getId(), result.getId());
        assertEquals(testReply.getContent(), result.getContent());
        verify(postReplyRepository, times(1)).save(any(PostReply.class));
    }

    @Test
    void testCreateReply_ByAdmin_Success() {
        // Arrange
        CreatePostReplyRequest request = new CreatePostReplyRequest();
        request.setPostId(1L);
        request.setContent("Admin reply");

        when(progressPostRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(organizationPermissionService.getUserRole(adminUser, 1L))
                .thenReturn(Optional.of(OrganizationRole.ADMIN_SUPER));

        PostReply adminReply = new PostReply();
        adminReply.setId(2L);
        adminReply.setPost(testPost);
        adminReply.setAuthor(adminUser);
        adminReply.setContent("Admin reply");
        adminReply.setCreatedAt(LocalDateTime.now());

        when(postReplyRepository.save(any(PostReply.class))).thenReturn(adminReply);

        // Act
        PostReplyDTO result = postReplyService.createReply(2L, request);

        // Assert
        assertNotNull(result);
        assertEquals(adminReply.getId(), result.getId());
        verify(postReplyRepository, times(1)).save(any(PostReply.class));
    }

    @Test
    void testCreateReply_UnauthorizedUser_ThrowsException() {
        // Arrange
        User unauthorizedUser = new User();
        unauthorizedUser.setId(3L);
        unauthorizedUser.setUsername("unauthorized");

        CreatePostReplyRequest request = new CreatePostReplyRequest();
        request.setPostId(1L);
        request.setContent("Unauthorized reply");

        when(progressPostRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(3L)).thenReturn(Optional.of(unauthorizedUser));
        when(organizationPermissionService.getUserRole(unauthorizedUser, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            postReplyService.createReply(3L, request);
        });

        verify(postReplyRepository, never()).save(any(PostReply.class));
    }

    @Test
    void testCreateReply_PostNotFound_ThrowsException() {
        // Arrange
        CreatePostReplyRequest request = new CreatePostReplyRequest();
        request.setPostId(999L);
        request.setContent("Reply to non-existent post");

        when(progressPostRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            postReplyService.createReply(1L, request);
        });

        verify(postReplyRepository, never()).save(any(PostReply.class));
    }

    @Test
    void testCreateReply_UserNotFound_ThrowsException() {
        // Arrange
        CreatePostReplyRequest request = new CreatePostReplyRequest();
        request.setPostId(1L);
        request.setContent("Reply content");

        when(progressPostRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            postReplyService.createReply(999L, request);
        });

        verify(postReplyRepository, never()).save(any(PostReply.class));
    }

    @Test
    void testGetRepliesByPostId_ByAuthor_Success() {
        // Arrange
        PostReply reply1 = new PostReply();
        reply1.setId(1L);
        reply1.setPost(testPost);
        reply1.setAuthor(testUser);
        reply1.setContent("Reply 1");
        reply1.setCreatedAt(LocalDateTime.now());

        PostReply reply2 = new PostReply();
        reply2.setId(2L);
        reply2.setPost(testPost);
        reply2.setAuthor(testUser);
        reply2.setContent("Reply 2");
        reply2.setCreatedAt(LocalDateTime.now().plusMinutes(5));

        List<PostReply> replies = Arrays.asList(reply1, reply2);

        when(progressPostRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(postReplyRepository.findByPostIdOrderByCreatedAtAsc(1L)).thenReturn(replies);
        when(organizationPermissionService.getUserRole(testUser, 1L))
                .thenReturn(Optional.empty());

        // Act
        List<PostReplyDTO> result = postReplyService.getRepliesByPostId(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Reply 1", result.get(0).getContent());
        assertEquals("Reply 2", result.get(1).getContent());
        verify(postReplyRepository, times(1)).findByPostIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void testGetRepliesByPostId_ByAdmin_Success() {
        // Arrange
        when(progressPostRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(organizationPermissionService.getUserRole(adminUser, 1L))
                .thenReturn(Optional.of(OrganizationRole.OWNER));
        when(postReplyRepository.findByPostIdOrderByCreatedAtAsc(1L))
                .thenReturn(Arrays.asList(testReply));

        // Act
        List<PostReplyDTO> result = postReplyService.getRepliesByPostId(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(postReplyRepository, times(1)).findByPostIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void testGetRepliesByPostId_UnauthorizedUser_ReturnsEmptyList() {
        // Arrange
        User unauthorizedUser = new User();
        unauthorizedUser.setId(3L);
        unauthorizedUser.setUsername("unauthorized");

        when(progressPostRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(3L)).thenReturn(Optional.of(unauthorizedUser));
        when(organizationPermissionService.getUserRole(unauthorizedUser, 1L))
                .thenReturn(Optional.empty());

        // Act
        List<PostReplyDTO> result = postReplyService.getRepliesByPostId(1L, 3L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(postReplyRepository, never()).findByPostIdOrderByCreatedAtAsc(anyLong());
    }

    @Test
    void testGetRepliesByPostId_PostNotFound_ThrowsException() {
        // Arrange
        when(progressPostRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            postReplyService.getRepliesByPostId(999L, 1L);
        });
    }

    @Test
    void testGetReplyCount() {
        // Arrange
        when(postReplyRepository.countByPostId(1L)).thenReturn(5L);

        // Act
        long count = postReplyService.getReplyCount(1L);

        // Assert
        assertEquals(5L, count);
        verify(postReplyRepository, times(1)).countByPostId(1L);
    }

    @Test
    void testGetReplyCount_NoReplies() {
        // Arrange
        when(postReplyRepository.countByPostId(1L)).thenReturn(0L);

        // Act
        long count = postReplyService.getReplyCount(1L);

        // Assert
        assertEquals(0L, count);
        verify(postReplyRepository, times(1)).countByPostId(1L);
    }

    @Test
    void testCreateReply_WithDifferentAdminRoles() {
        // ADMIN_LEAD
        testCreateReplyWithRole(OrganizationRole.ADMIN_LEAD);

        // ADMIN_CORE
        testCreateReplyWithRole(OrganizationRole.ADMIN_CORE);

        // ADMIN_ROOT
        testCreateReplyWithRole(OrganizationRole.ADMIN_ROOT);
    }

    private void testCreateReplyWithRole(OrganizationRole role) {
        // Arrange
        CreatePostReplyRequest request = new CreatePostReplyRequest();
        request.setPostId(1L);
        request.setContent("Admin reply with role: " + role);

        when(progressPostRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(organizationPermissionService.getUserRole(adminUser, 1L))
                .thenReturn(Optional.of(role));
        when(postReplyRepository.save(any(PostReply.class))).thenReturn(testReply);

        // Act
        PostReplyDTO result = postReplyService.createReply(2L, request);

        // Assert
        assertNotNull(result);
    }
}
