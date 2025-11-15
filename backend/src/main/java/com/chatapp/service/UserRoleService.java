package com.chatapp.service;

import com.chatapp.model.*;
import com.chatapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserRoleService {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleHierarchyRepository roleHierarchyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * 指定したユーザーの組織内での現在有効なロールを取得
     */
    public Optional<UserRole> getCurrentRole(Long userId, Long organizationId) {
        return userRoleRepository.findByUserId(userId).stream()
                .filter(role -> role.getOrganizationId().equals(organizationId))
                .filter(role -> "APPROVED".equals(role.getApprovalStatus()))
                .findFirst();
    }

    /**
     * テナント内の全ユーザーロールを取得
     */
    public List<UserRole> getTenantRoles(Long tenantId) {
        return userRoleRepository.findByTenantId(tenantId);
    }

    /**
     * ユーザーにロールを割り当て
     */
    @Transactional
    public UserRole assignRole(Long userId, RoleName roleName, Long tenantId, Long organizationId, Long assignedByUserId) {
        // 既存の同一組織のロールを確認
        Optional<UserRole> existingRole = getCurrentRole(userId, organizationId);

        if (existingRole.isPresent()) {
            // 既存のロールを更新
            UserRole role = existingRole.get();
            role.setRoleName(roleName);
            role.setRoleLevel(roleName.getLevel());
            role.setApprovedBy(assignedByUserId);
            role.setApprovedAt(LocalDateTime.now());
            role.setApprovalStatus("APPROVED");
            return userRoleRepository.save(role);
        } else {
            // 新しいロールを作成
            UserRole newRole = new UserRole();
            newRole.setUserId(userId);
            newRole.setRoleName(roleName);
            newRole.setRoleLevel(roleName.getLevel());
            newRole.setTenantId(tenantId);
            newRole.setOrganizationId(organizationId);
            newRole.setApprovedBy(assignedByUserId);
            newRole.setApprovedAt(LocalDateTime.now());
            newRole.setApprovalStatus("APPROVED");
            newRole.setAssignedAt(LocalDateTime.now());
            return userRoleRepository.save(newRole);
        }
    }

    /**
     * ロールを取り消し（承認ステータスをREVOKEDに変更）
     */
    @Transactional
    public void revokeRole(Long userRoleId) {
        UserRole role = userRoleRepository.findById(userRoleId)
                .orElseThrow(() -> new RuntimeException("UserRole not found"));

        role.setApprovalStatus("REVOKED");
        userRoleRepository.save(role);
    }

    /**
     * ユーザーが特定のロール以上の権限を持っているかチェック
     */
    public boolean hasRoleOrHigher(Long userId, Long organizationId, RoleName minimumRole) {
        Optional<UserRole> currentRole = getCurrentRole(userId, organizationId);
        if (currentRole.isEmpty()) {
            return false;
        }

        RoleName userRole = currentRole.get().getRoleName();
        return userRole.isAtLeast(minimumRole);
    }

    /**
     * あるロールから昇格可能なロールのリストを取得
     */
    public List<RoleName> getPromotableRoles(RoleName currentRole, Long tenantId) {
        int currentLevel = currentRole.getLevel();

        // 現在のレベルより高いロールを全て取得
        return roleHierarchyRepository.findAll().stream()
                .filter(hierarchy -> hierarchy.getTenantId().equals(tenantId))
                .filter(hierarchy -> hierarchy.getRoleLevel() > currentLevel)
                .map(RoleHierarchy::getRoleName)
                .distinct()
                .toList();
    }

    /**
     * テナント内のロール統計を取得
     */
    public java.util.Map<RoleName, Long> getRoleStatistics(Long tenantId) {
        List<UserRole> roles = userRoleRepository.findByTenantId(tenantId).stream()
                .filter(role -> "APPROVED".equals(role.getApprovalStatus()))
                .toList();

        return roles.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        UserRole::getRoleName,
                        java.util.stream.Collectors.counting()
                ));
    }

    /**
     * 組織内のユーザーロール一覧を取得
     */
    public List<UserRole> getOrganizationRoles(Long organizationId) {
        return userRoleRepository.findAll().stream()
                .filter(role -> role.getOrganizationId().equals(organizationId))
                .filter(role -> "APPROVED".equals(role.getApprovalStatus()))
                .toList();
    }

    /**
     * ユーザーの全ロール情報を取得（複数組織に所属している場合）
     */
    public List<UserRole> getUserRoles(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .filter(role -> "APPROVED".equals(role.getApprovalStatus()))
                .toList();
    }

    /**
     * 昇格申請の承認
     */
    @Transactional
    public void approvePromotion(Long userRoleId, Long approvedByUserId) {
        UserRole role = userRoleRepository.findById(userRoleId)
                .orElseThrow(() -> new RuntimeException("UserRole not found"));

        role.setApprovalStatus("APPROVED");
        role.setApprovedBy(approvedByUserId);
        role.setApprovedAt(LocalDateTime.now());
        userRoleRepository.save(role);
    }

    /**
     * 昇格申請の却下
     */
    @Transactional
    public void rejectPromotion(Long userRoleId, Long rejectedByUserId) {
        UserRole role = userRoleRepository.findById(userRoleId)
                .orElseThrow(() -> new RuntimeException("UserRole not found"));

        role.setApprovalStatus("REJECTED");
        role.setApprovedBy(rejectedByUserId);
        role.setApprovedAt(LocalDateTime.now());
        userRoleRepository.save(role);
    }
}
