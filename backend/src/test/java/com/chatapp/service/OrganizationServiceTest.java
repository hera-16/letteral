package com.chatapp.service;

import com.chatapp.model.Organization;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.repository.OrganizationMemberRepository;
import com.chatapp.repository.OrganizationRepository;
import com.chatapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrganizationServiceのユニットテスト
 * 組織の作成、更新、削除、階層構造管理をテスト
 */
@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private Tenant testTenant;
    private Organization rootOrganization;
    private Organization childOrganization;
    private User testUser;

    @BeforeEach
    void setUp() {
        // テストテナント
        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setName("Test Company");

        // ルート組織
        rootOrganization = new Organization();
        rootOrganization.setId(1L);
        rootOrganization.setName("Engineering");
        rootOrganization.setTenant(testTenant);
        rootOrganization.setParent(null);
        rootOrganization.setLevel(0);
        rootOrganization.setPath("/Engineering");
        rootOrganization.setIsActive(true);
        rootOrganization.setCreatedAt(LocalDateTime.now());

        // 子組織
        childOrganization = new Organization();
        childOrganization.setId(2L);
        childOrganization.setName("Backend Team");
        childOrganization.setTenant(testTenant);
        childOrganization.setParent(rootOrganization);
        childOrganization.setLevel(1);
        childOrganization.setPath("/Engineering/Backend Team");
        childOrganization.setIsActive(true);
        childOrganization.setCreatedAt(LocalDateTime.now());

        // テストユーザー
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
    }

    @Test
    void testCreateOrganization_RootOrganization_Success() {
        // Arrange
        Organization newOrg = new Organization();
        newOrg.setName("New Department");
        newOrg.setTenant(testTenant);
        newOrg.setParent(null);

        Organization savedOrg = new Organization();
        savedOrg.setId(3L);
        savedOrg.setName("New Department");
        savedOrg.setLevel(0);
        savedOrg.setPath("/New Department");

        when(organizationRepository.save(any(Organization.class))).thenReturn(savedOrg);

        // Act
        Organization result = organizationService.createOrganization(newOrg);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getLevel());
        assertEquals("/New Department", result.getPath());
        verify(organizationRepository, times(1)).save(any(Organization.class));
    }

    @Test
    void testCreateOrganization_ChildOrganization_Success() {
        // Arrange
        Organization newOrg = new Organization();
        newOrg.setName("Frontend Team");
        newOrg.setTenant(testTenant);
        newOrg.setParent(rootOrganization);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(rootOrganization));

        Organization savedOrg = new Organization();
        savedOrg.setId(3L);
        savedOrg.setName("Frontend Team");
        savedOrg.setLevel(1);
        savedOrg.setPath("/Engineering/Frontend Team");

        when(organizationRepository.save(any(Organization.class))).thenReturn(savedOrg);

        // Act
        Organization result = organizationService.createOrganization(newOrg);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getLevel());
        assertEquals("/Engineering/Frontend Team", result.getPath());
        verify(organizationRepository, times(1)).save(any(Organization.class));
    }

    @Test
    void testGetOrganizationById_Success() {
        // Arrange
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(rootOrganization));

        // Act
        Organization result = organizationService.getOrganizationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(rootOrganization.getId(), result.getId());
        assertEquals(rootOrganization.getName(), result.getName());
        verify(organizationRepository, times(1)).findById(1L);
    }

    @Test
    void testGetOrganizationById_NotFound_ThrowsException() {
        // Arrange
        when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            organizationService.getOrganizationById(999L);
        });
    }

    @Test
    void testGetOrganizationsByTenant_Success() {
        // Arrange
        List<Organization> organizations = Arrays.asList(rootOrganization, childOrganization);
        when(organizationRepository.findByTenantAndIsActiveOrderByDisplayOrder(testTenant, true))
                .thenReturn(organizations);

        // Act
        List<Organization> result = organizationService.getOrganizationsByTenant(testTenant);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(organizationRepository, times(1))
                .findByTenantAndIsActiveOrderByDisplayOrder(testTenant, true);
    }

    @Test
    void testGetChildOrganizations_Success() {
        // Arrange
        List<Organization> children = Arrays.asList(childOrganization);
        when(organizationRepository.findByParentAndIsActiveOrderByDisplayOrder(rootOrganization, true))
                .thenReturn(children);

        // Act
        List<Organization> result = organizationService.getChildOrganizations(rootOrganization);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(childOrganization.getId(), result.get(0).getId());
        verify(organizationRepository, times(1))
                .findByParentAndIsActiveOrderByDisplayOrder(rootOrganization, true);
    }

    @Test
    void testGetRootOrganizations_Success() {
        // Arrange
        List<Organization> rootOrgs = Arrays.asList(rootOrganization);
        when(organizationRepository.findByTenantAndParentIsNullAndIsActive(testTenant, true))
                .thenReturn(rootOrgs);

        // Act
        List<Organization> result = organizationService.getRootOrganizations(testTenant);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(rootOrganization.getId(), result.get(0).getId());
        verify(organizationRepository, times(1))
                .findByTenantAndParentIsNullAndIsActive(testTenant, true);
    }

    @Test
    void testGetOrganizationsByLevel_Success() {
        // Arrange
        List<Organization> levelOrgs = Arrays.asList(childOrganization);
        when(organizationRepository.findByTenantAndLevel(testTenant, 1))
                .thenReturn(levelOrgs);

        // Act
        List<Organization> result = organizationService.getOrganizationsByLevel(testTenant, 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getLevel());
        verify(organizationRepository, times(1)).findByTenantAndLevel(testTenant, 1);
    }

    @Test
    void testGetOrganizationsByPath_Success() {
        // Arrange
        String path = "/Engineering";
        List<Organization> pathOrgs = Arrays.asList(rootOrganization, childOrganization);
        when(organizationRepository.findByTenantAndPathStartingWith(testTenant, path))
                .thenReturn(pathOrgs);

        // Act
        List<Organization> result = organizationService.getOrganizationsByPath(testTenant, path);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(organizationRepository, times(1))
                .findByTenantAndPathStartingWith(testTenant, path);
    }

    @Test
    void testUpdateOrganization_Success() {
        // Arrange
        Organization updateData = new Organization();
        updateData.setName("Updated Engineering");
        updateData.setDescription("Updated description");

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(rootOrganization));

        Organization updatedOrg = new Organization();
        updatedOrg.setId(1L);
        updatedOrg.setName("Updated Engineering");
        updatedOrg.setDescription("Updated description");
        updatedOrg.setPath("/Updated Engineering");

        when(organizationRepository.save(any(Organization.class))).thenReturn(updatedOrg);

        // Act
        Organization result = organizationService.updateOrganization(1L, updateData);

        // Assert
        assertNotNull(result);
        verify(organizationRepository, times(1)).save(any(Organization.class));
    }

    @Test
    void testUpdateOrganization_NotFound_ThrowsException() {
        // Arrange
        Organization updateData = new Organization();
        updateData.setName("Updated Name");

        when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            organizationService.updateOrganization(999L, updateData);
        });

        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void testSetOrganizationActive_Success() {
        // Arrange
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(rootOrganization));

        Organization deactivatedOrg = new Organization();
        deactivatedOrg.setId(1L);
        deactivatedOrg.setIsActive(false);

        when(organizationRepository.save(any(Organization.class))).thenReturn(deactivatedOrg);

        // Act
        Organization result = organizationService.setOrganizationActive(1L, false);

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsActive());
        verify(organizationRepository, times(1)).save(any(Organization.class));
    }

    @Test
    void testDeleteOrganization_WithNoChildren_Success() {
        // Arrange
        when(organizationRepository.findById(2L)).thenReturn(Optional.of(childOrganization));
        when(organizationRepository.findByParentAndIsActiveOrderByDisplayOrder(childOrganization, true))
                .thenReturn(Arrays.asList());

        childOrganization.setIsActive(false);
        when(organizationRepository.save(any(Organization.class))).thenReturn(childOrganization);

        // Act
        organizationService.deleteOrganization(2L);

        // Assert
        verify(organizationRepository, times(1)).save(any(Organization.class));
    }

    @Test
    void testDeleteOrganization_WithChildren_ThrowsException() {
        // Arrange
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(rootOrganization));
        when(organizationRepository.findByParentAndIsActiveOrderByDisplayOrder(rootOrganization, true))
                .thenReturn(Arrays.asList(childOrganization));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            organizationService.deleteOrganization(1L);
        });

        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void testCreateOrganization_DeepHierarchy_Success() {
        // Arrange
        Organization level2Org = new Organization();
        level2Org.setId(3L);
        level2Org.setName("Backend Sub-team");
        level2Org.setParent(childOrganization);

        when(organizationRepository.findById(2L)).thenReturn(Optional.of(childOrganization));

        Organization savedOrg = new Organization();
        savedOrg.setId(3L);
        savedOrg.setName("Backend Sub-team");
        savedOrg.setLevel(2);
        savedOrg.setPath("/Engineering/Backend Team/Backend Sub-team");

        when(organizationRepository.save(any(Organization.class))).thenReturn(savedOrg);

        // Act
        Organization result = organizationService.createOrganization(level2Org);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getLevel());
        assertTrue(result.getPath().contains("Backend Team"));
        verify(organizationRepository, times(1)).save(any(Organization.class));
    }

    @Test
    void testGetOrganizations_WithActiveFilter_Success() {
        // Arrange
        List<Organization> activeOrgs = Arrays.asList(rootOrganization);
        when(organizationRepository.findByTenantAndIsActiveOrderByDisplayOrder(testTenant, true))
                .thenReturn(activeOrgs);

        // Act
        List<Organization> result = organizationService.getOrganizations(testTenant, true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
    }

    @Test
    void testUpdateOrganization_OnlyNameChanged_PathUpdated() {
        // Arrange
        Organization updateData = new Organization();
        updateData.setName("New Engineering Name");

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(rootOrganization));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
            Organization org = invocation.getArgument(0);
            org.setPath("/" + org.getName());
            return org;
        });

        // Act
        organizationService.updateOrganization(1L, updateData);

        // Assert
        verify(organizationRepository, times(1)).save(any(Organization.class));
    }
}
