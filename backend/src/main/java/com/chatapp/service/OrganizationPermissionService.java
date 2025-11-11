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
