package com.chatapp.service;

import com.chatapp.model.Organization;
import com.chatapp.model.OrganizationMember;
import com.chatapp.model.User;
import com.chatapp.model.enums.OrganizationRole;
import com.chatapp.repository.OrganizationMemberRepository;
import com.chatapp.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 組織内の階層的権限管理サービス
 * ADMIN_ROOT、ADMIN_CORE、ADMIN_LEAD、ADMIN_SUPERの階層構造をサポート
 */
@Service
public class OrganizationPermissionService {

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    /**
     * ユーザーが組織内で特定の権限を持っているかチェック
     */
    public boolean hasPermission(User user, Long organizationId, OrganizationRole requiredRole) {
        // CEO/SUPER_ADMIN権限チェック（全権限を持つ）
        String userRole = user.getRole();
        if ("TENANT_ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            return true;
        }

        Optional<OrganizationMember> memberOpt = organizationMemberRepository
                .findByUserIdAndOrganizationId(user.getId(), organizationId);

        if (memberOpt.isEmpty()) {
            return false;
        }

        OrganizationMember member = memberOpt.get();
        return member.getRole().hasAuthorityOver(requiredRole);
    }

    /**
     * ユーザーが特定の役割をメンバーに付与できるかチェック
     */
    public boolean canAssignRole(User assigner, Long organizationId, OrganizationRole targetRole) {
        // CEO/SUPER_ADMIN権限チェック（全役割を付与可能）
        String userRole = assigner.getRole();
        if ("TENANT_ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            return true;
        }

        Optional<OrganizationMember> assignerMemberOpt = organizationMemberRepository
                .findByUserIdAndOrganizationId(assigner.getId(), organizationId);

        if (assignerMemberOpt.isEmpty()) {
            return false;
        }

        OrganizationMember assignerMember = assignerMemberOpt.get();
        return assignerMember.getRole().canAssignRole(targetRole);
    }

    /**
     * ユーザーが組織でメンバーを追加できるかチェック
     * ADMIN_SUPER以上の権限が必要
     */
    public boolean canAddMember(User user, Long organizationId) {
        return hasPermission(user, organizationId, OrganizationRole.ADMIN_SUPER);
    }

    /**
     * ユーザーが組織でメンバーを削除できるかチェック
     * ADMIN_SUPER以上の権限が必要
     */
    public boolean canRemoveMember(User user, Long organizationId) {
        return hasPermission(user, organizationId, OrganizationRole.ADMIN_SUPER);
    }

    /**
     * ユーザーが組織で子組織（階層グループ）を作成できるかチェック
     * ADMIN_SUPER以上の権限が必要
     */
    public boolean canCreateSubOrganization(User user, Long parentOrganizationId) {
        return hasPermission(user, parentOrganizationId, OrganizationRole.ADMIN_SUPER);
    }

    /**
     * ユーザーが組織内の特定メンバーを管理できるかチェック
     */
    public boolean canManageMember(User manager, Long organizationId, Long targetUserId) {
        // CEO/SUPER_ADMIN権限チェック（全メンバーを管理可能）
        String userRole = manager.getRole();
        if ("TENANT_ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            return true;
        }

        Optional<OrganizationMember> managerMemberOpt = organizationMemberRepository
                .findByUserIdAndOrganizationId(manager.getId(), organizationId);
        Optional<OrganizationMember> targetMemberOpt = organizationMemberRepository
                .findByUserIdAndOrganizationId(targetUserId, organizationId);

        if (managerMemberOpt.isEmpty() || targetMemberOpt.isEmpty()) {
            return false;
        }

        OrganizationMember managerMember = managerMemberOpt.get();
        OrganizationMember targetMember = targetMemberOpt.get();

        // 管理者は同じレベル以下のメンバーのみ管理可能
        return managerMember.getRole().hasAuthorityOver(targetMember.getRole());
    }

    /**
     * ユーザーの組織内での役割を取得
     */
    public Optional<OrganizationRole> getUserRole(User user, Long organizationId) {
        return organizationMemberRepository
                .findByUserIdAndOrganizationId(user.getId(), organizationId)
                .map(OrganizationMember::getRole);
    }

    /**
     * ユーザーが組織のメンバーかチェック
     */
    public boolean isMember(User user, Long organizationId) {
        return organizationMemberRepository
                .findByUserIdAndOrganizationId(user.getId(), organizationId)
                .isPresent();
    }

    /**
     * ユーザーが組織のメンバーを閲覧できるかチェック
     * - 組織のメンバーである場合
     * - 親組織の管理者（ADMIN_SUPER以上）である場合
     * - ADMIN_CORE/ADMIN_ROOTの場合（テナント全体を管理）
     * - TENANT_ADMIN/SUPER_ADMIN役割を持つ場合（CEO等、全組織を閲覧可能）
     */
    public boolean canViewMembers(User user, Long organizationId) {
        System.out.println("=== canViewMembers チェック開始 ===");
        System.out.println("ユーザーID: " + user.getId() + ", 組織ID: " + organizationId);

        // 組織情報を取得
        Organization targetOrg = organizationRepository.findById(organizationId)
                .orElse(null);
        if (targetOrg == null) {
            System.out.println("✗ 組織が見つかりません");
            return false;
        }
        System.out.println("対象組織: " + targetOrg.getName() + " (テナントID: " + targetOrg.getTenant().getId() + ")");

        // CEO/SUPER_ADMIN権限チェック（全組織を閲覧可能）
        String userRole = user.getRole();
        System.out.println("ユーザーロール: " + userRole);
        if ("TENANT_ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            // 同じテナント内であれば閲覧可能
            if (user.getTenantId().equals(targetOrg.getTenant().getId())) {
                System.out.println("✓ TENANT_ADMIN/SUPER_ADMINとして全組織を閲覧可能");
                return true;
            }
        }

        // 直接のメンバーである場合
        if (isMember(user, organizationId)) {
            System.out.println("✓ 直接のメンバーです");
            return true;
        }
        System.out.println("✗ 直接のメンバーではありません");

        // 同じテナント内の組織に所属しているかチェック
        // ADMIN_COREまたはADMIN_ROOTの場合、テナント全体を閲覧可能
        List<OrganizationMember> userMemberships = organizationMemberRepository.findByUserId(user.getId());
        System.out.println("ユーザーの所属組織数: " + userMemberships.size());

        Optional<OrganizationMember> anyMembershipInTenant = userMemberships.stream()
                .filter(member -> {
                    boolean sameTenant = member.getTenant().getId().equals(targetOrg.getTenant().getId());
                    System.out.println("  - 組織: " + member.getOrganization().getName() +
                                     ", 役割: " + member.getRole() +
                                     ", 同じテナント: " + sameTenant);
                    return sameTenant;
                })
                .filter(member -> member.getRole() == OrganizationRole.ADMIN_CORE
                        || member.getRole() == OrganizationRole.ADMIN_ROOT)
                .findFirst();

        if (anyMembershipInTenant.isPresent()) {
            System.out.println("✓ ADMIN_CORE/ADMIN_ROOTとしてテナント全体を閲覧可能");
            return true;
        }

        // 親組織の管理者（ADMIN_SUPER以上）かチェック
        System.out.println("親組織チェック開始...");
        Organization currentOrg = targetOrg;
        int level = 0;
        while (currentOrg.getParent() != null) {
            level++;
            currentOrg = currentOrg.getParent();
            System.out.println("  レベル" + level + ": " + currentOrg.getName() + " (ID: " + currentOrg.getId() + ")");

            Optional<OrganizationMember> parentMembership = organizationMemberRepository
                    .findByUserIdAndOrganizationId(user.getId(), currentOrg.getId());

            if (parentMembership.isPresent()) {
                OrganizationRole role = parentMembership.get().getRole();
                System.out.println("    → ユーザーの役割: " + role);
                // ADMIN_SUPER以上であれば子孫組織を閲覧可能
                if (role == OrganizationRole.ADMIN_SUPER
                        || role == OrganizationRole.ADMIN_LEAD
                        || role == OrganizationRole.ADMIN_CORE
                        || role == OrganizationRole.ADMIN_ROOT) {
                    System.out.println("✓ 親組織の管理者として閲覧可能");
                    return true;
                }
            } else {
                System.out.println("    → メンバーではありません");
            }
        }

        System.out.println("✗ 閲覧権限がありません");
        return false;
    }

    /**
     * 組織に新しいメンバーを追加
     */
    @Transactional
    public OrganizationMember addMemberToOrganization(
            User adder,
            Long organizationId,
            User newMember,
            OrganizationRole role) {

        // 権限チェック
        if (!canAddMember(adder, organizationId)) {
            throw new SecurityException("メンバーを追加する権限がありません");
        }

        if (!canAssignRole(adder, organizationId, role)) {
            throw new SecurityException("この役割を付与する権限がありません");
        }

        // 既存メンバーチェック
        if (isMember(newMember, organizationId)) {
            throw new IllegalArgumentException("ユーザーは既にこの組織のメンバーです");
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("組織が見つかりません"));

        OrganizationMember member = new OrganizationMember(
                organization.getTenant(),
                organization,
                newMember
        );
        member.setRole(role);

        return organizationMemberRepository.save(member);
    }

    /**
     * 組織からメンバーを削除
     */
    @Transactional
    public void removeMemberFromOrganization(User remover, Long organizationId, Long targetUserId) {
        // 権限チェック
        if (!canRemoveMember(remover, organizationId)) {
            throw new SecurityException("メンバーを削除する権限がありません");
        }

        if (!canManageMember(remover, organizationId, targetUserId)) {
            throw new SecurityException("このメンバーを管理する権限がありません");
        }

        OrganizationMember targetMember = organizationMemberRepository
                .findByUserIdAndOrganizationId(targetUserId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("メンバーが見つかりません"));

        organizationMemberRepository.delete(targetMember);
    }

    /**
     * メンバーの役割を変更
     */
    @Transactional
    public OrganizationMember updateMemberRole(
            User updater,
            Long organizationId,
            Long targetUserId,
            OrganizationRole newRole) {

        // 権限チェック
        if (!canManageMember(updater, organizationId, targetUserId)) {
            throw new SecurityException("このメンバーを管理する権限がありません");
        }

        if (!canAssignRole(updater, organizationId, newRole)) {
            throw new SecurityException("この役割を付与する権限がありません");
        }

        OrganizationMember targetMember = organizationMemberRepository
                .findByUserIdAndOrganizationId(targetUserId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("メンバーが見つかりません"));

        targetMember.setRole(newRole);
        return organizationMemberRepository.save(targetMember);
    }
}
