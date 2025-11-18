package com.chatapp.service;

import com.chatapp.model.*;
import com.chatapp.repository.ProgressDigestRepository;
import com.chatapp.repository.ProgressPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressDigestServiceTest {

    @Mock
    private ProgressDigestRepository digestRepository;

    @Mock
    private ProgressPostRepository progressPostRepository;

    @InjectMocks
    private ProgressDigestService progressDigestService;

    private User testUser;
    private Tenant testTenant;
    private ProgressPost testPost1;
    private ProgressPost testPost2;
    private ProgressDigest testDigest;

    @BeforeEach
    void setUp() {
        // Test tenant
        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setName("Test Tenant");

        // Test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setTenantId(1L);
        testUser.setDisplayName("Test User");

        // Test progress posts
        testPost1 = new ProgressPost();
        testPost1.setId(1L);
        testPost1.setAuthor(testUser);
        testPost1.setContent("Achievement 1");
        testPost1.setBlockers("Challenge 1");
        testPost1.setLearnings("Learning 1");
        testPost1.setNextAction("Next action 1");
        testPost1.setCreatedAt(LocalDateTime.now());

        testPost2 = new ProgressPost();
        testPost2.setId(2L);
        testPost2.setAuthor(testUser);
        testPost2.setContent("Achievement 2");
        testPost2.setBlockers("");
        testPost2.setLearnings("Learning 2");
        testPost2.setNextAction("Next action 2");
        testPost2.setCreatedAt(LocalDateTime.now());

        // Test digest
        testDigest = new ProgressDigest();
        testDigest.setId(1L);
        testDigest.setUser(testUser);
        testDigest.setTenant(testTenant);
        testDigest.setDigestType(ProgressDigest.DigestType.WEEKLY);
        testDigest.setPeriodStart(LocalDateTime.now().minusDays(7));
        testDigest.setPeriodEnd(LocalDateTime.now());
        testDigest.setTotalPosts(2);
        testDigest.setCompletedGoals(2);
        testDigest.setOngoingGoals(2);
        testDigest.setChallenges(1);
    }

    @Test
    void testGenerateWeeklyDigest_Success() {
        // Arrange
        LocalDateTime periodStart = LocalDateTime.now().minusDays(7);
        List<ProgressPost> posts = Arrays.asList(testPost1, testPost2);

        when(digestRepository.existsByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
                any(), any(), any(), any(), any())).thenReturn(false);
        when(progressPostRepository.findByAuthorAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(posts);
        when(digestRepository.save(any(ProgressDigest.class))).thenReturn(testDigest);

        // Act
        ProgressDigest result = progressDigestService.generateWeeklyDigest(testUser, testTenant, periodStart);

        // Assert
        assertNotNull(result);
        assertEquals(ProgressDigest.DigestType.WEEKLY, result.getDigestType());
        assertEquals(2, result.getTotalPosts());
        verify(digestRepository, times(1)).save(any(ProgressDigest.class));
        verify(progressPostRepository, times(1))
                .findByAuthorAndCreatedAtBetween(any(), any(), any());
    }

    @Test
    void testGenerateWeeklyDigest_AlreadyExists() {
        // Arrange
        LocalDateTime periodStart = LocalDateTime.now().minusDays(7);

        when(digestRepository.existsByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
                any(), any(), any(), any(), any())).thenReturn(true);
        when(digestRepository.findByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
                any(), any(), any(), any(), any())).thenReturn(Optional.of(testDigest));

        // Act
        ProgressDigest result = progressDigestService.generateWeeklyDigest(testUser, testTenant, periodStart);

        // Assert
        assertNotNull(result);
        assertEquals(testDigest.getId(), result.getId());
        verify(digestRepository, never()).save(any(ProgressDigest.class));
        verify(progressPostRepository, never()).findByAuthorAndCreatedAtBetween(any(), any(), any());
    }

    @Test
    void testGenerateMonthlyDigest_Success() {
        // Arrange
        LocalDateTime periodStart = LocalDateTime.now().minusMonths(1);
        List<ProgressPost> posts = Arrays.asList(testPost1, testPost2);

        when(digestRepository.existsByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
                any(), any(), any(), any(), any())).thenReturn(false);
        when(progressPostRepository.findByAuthorAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(posts);

        ProgressDigest monthlyDigest = new ProgressDigest();
        monthlyDigest.setId(2L);
        monthlyDigest.setDigestType(ProgressDigest.DigestType.MONTHLY);
        monthlyDigest.setTotalPosts(2);
        when(digestRepository.save(any(ProgressDigest.class))).thenReturn(monthlyDigest);

        // Act
        ProgressDigest result = progressDigestService.generateMonthlyDigest(testUser, testTenant, periodStart);

        // Assert
        assertNotNull(result);
        assertEquals(ProgressDigest.DigestType.MONTHLY, result.getDigestType());
        verify(digestRepository, times(1)).save(any(ProgressDigest.class));
    }

    @Test
    void testGenerateCurrentWeekDigest() {
        // Arrange
        List<ProgressPost> posts = Arrays.asList(testPost1, testPost2);

        when(digestRepository.existsByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
                any(), any(), any(), any(), any())).thenReturn(false);
        when(progressPostRepository.findByAuthorAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(posts);
        when(digestRepository.save(any(ProgressDigest.class))).thenReturn(testDigest);

        // Act
        ProgressDigest result = progressDigestService.generateCurrentWeekDigest(testUser, testTenant);

        // Assert
        assertNotNull(result);
        assertEquals(ProgressDigest.DigestType.WEEKLY, result.getDigestType());
        verify(digestRepository, times(1)).save(any(ProgressDigest.class));
    }

    @Test
    void testGenerateCurrentMonthDigest() {
        // Arrange
        List<ProgressPost> posts = Arrays.asList(testPost1, testPost2);
        ProgressDigest monthlyDigest = new ProgressDigest();
        monthlyDigest.setDigestType(ProgressDigest.DigestType.MONTHLY);

        when(digestRepository.existsByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
                any(), any(), any(), any(), any())).thenReturn(false);
        when(progressPostRepository.findByAuthorAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(posts);
        when(digestRepository.save(any(ProgressDigest.class))).thenReturn(monthlyDigest);

        // Act
        ProgressDigest result = progressDigestService.generateCurrentMonthDigest(testUser, testTenant);

        // Assert
        assertNotNull(result);
        assertEquals(ProgressDigest.DigestType.MONTHLY, result.getDigestType());
        verify(digestRepository, times(1)).save(any(ProgressDigest.class));
    }

    @Test
    void testGetUserDigests() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProgressDigest> digestPage = new PageImpl<>(Arrays.asList(testDigest));

        when(digestRepository.findByUserAndTenantOrderByPeriodStartDesc(testUser, testTenant, pageable))
                .thenReturn(digestPage);

        // Act
        Page<ProgressDigest> result = progressDigestService.getUserDigests(testUser, testTenant, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testDigest.getId(), result.getContent().get(0).getId());
        verify(digestRepository, times(1))
                .findByUserAndTenantOrderByPeriodStartDesc(testUser, testTenant, pageable);
    }

    @Test
    void testGetUserDigestsByType() {
        // Arrange
        List<ProgressDigest> digests = Arrays.asList(testDigest);

        when(digestRepository.findByUserAndTenantAndDigestTypeOrderByPeriodStartDesc(
                testUser, testTenant, ProgressDigest.DigestType.WEEKLY))
                .thenReturn(digests);

        // Act
        List<ProgressDigest> result = progressDigestService.getUserDigestsByType(
                testUser, testTenant, ProgressDigest.DigestType.WEEKLY);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDigest.getId(), result.get(0).getId());
        verify(digestRepository, times(1))
                .findByUserAndTenantAndDigestTypeOrderByPeriodStartDesc(
                        testUser, testTenant, ProgressDigest.DigestType.WEEKLY);
    }

    @Test
    void testGenerateDigest_WithNoPosts() {
        // Arrange
        LocalDateTime periodStart = LocalDateTime.now().minusDays(7);
        List<ProgressPost> emptyPosts = Arrays.asList();

        when(digestRepository.existsByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
                any(), any(), any(), any(), any())).thenReturn(false);
        when(progressPostRepository.findByAuthorAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(emptyPosts);

        ProgressDigest emptyDigest = new ProgressDigest();
        emptyDigest.setTotalPosts(0);
        when(digestRepository.save(any(ProgressDigest.class))).thenReturn(emptyDigest);

        // Act
        ProgressDigest result = progressDigestService.generateWeeklyDigest(testUser, testTenant, periodStart);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalPosts());
        verify(digestRepository, times(1)).save(any(ProgressDigest.class));
    }

    @Test
    void testGenerateDigest_WithMultiplePosts() {
        // Arrange
        LocalDateTime periodStart = LocalDateTime.now().minusDays(7);

        ProgressPost post3 = new ProgressPost();
        post3.setId(3L);
        post3.setContent("Achievement 3");
        post3.setBlockers("Challenge 3");
        post3.setLearnings("Learning 3");
        post3.setNextAction("");

        List<ProgressPost> multiplePosts = Arrays.asList(testPost1, testPost2, post3);

        when(digestRepository.existsByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
                any(), any(), any(), any(), any())).thenReturn(false);
        when(progressPostRepository.findByAuthorAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(multiplePosts);

        ProgressDigest multiDigest = new ProgressDigest();
        multiDigest.setTotalPosts(3);
        when(digestRepository.save(any(ProgressDigest.class))).thenReturn(multiDigest);

        // Act
        ProgressDigest result = progressDigestService.generateWeeklyDigest(testUser, testTenant, periodStart);

        // Assert
        assertNotNull(result);
        verify(digestRepository, times(1)).save(any(ProgressDigest.class));
        verify(progressPostRepository, times(1))
                .findByAuthorAndCreatedAtBetween(any(), any(), any());
    }
}
