package com.chatapp.controller;

import com.chatapp.model.Organization;
import com.chatapp.model.OrganizationMember;
import com.chatapp.model.User;
import com.chatapp.model.enums.OrganizationRole;
import com.chatapp.repository.OrganizationMemberRepository;
import com.chatapp.repository.UserRepository;
import com.chatapp.security.UserPrincipal;
import com.chatapp.service.OrganizationPermissionService;
import com.chatapp.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 階層的組織管理API
 * ADMIN_ROOT、ADMIN_CORE、ADMIN_LEAD、ADMIN_SUPERによる管理をサポート
 */
@RestController
@RequestMapping("/api/organization-management")
public class OrganizationManagementController {

    @Autowired
    private OrganizationPermissionService permissionService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationMemberRepository memberRepository;

    /**
     * 組織のメンバー一覧を取得
     */
    @GetMapping("/{organizationId}/members")
    public ResponseEntity<?> getMembers(
            @PathVariable Long organizationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

            // メンバーであることを確認
            if (!permissionService.isMember(user, organizationId)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "この組織のメンバーではありません"));
            }

            Organization organization = organizationService.getOrganizationById(organizationId);
            List<OrganizationMember> members = memberRepository.findByOrganization(organization);

            List<Map<String, Object>> memberList = new ArrayList<>();
            for (OrganizationMember member : members) {
                memberList.add(createMemberResponse(member));
            }

            return ResponseEntity.ok(memberList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 組織にメンバーを追加
     * ADMIN_SUPER以上の権限が必要
     */
    @PostMapping("/{organizationId}/members")
    public ResponseEntity<?> addMember(
            @PathVariable Long organizationId,
            @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            User adder = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));
            User newMember = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

            OrganizationRole role = OrganizationRole.valueOf(request.getRole());

            OrganizationMember member = permissionService.addMemberToOrganization(
                    adder, organizationId, newMember, role);

            return ResponseEntity.ok(createMemberResponse(member));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 組織からメンバーを削除
     * ADMIN_SUPER以上の権限が必要
     */
    @DeleteMapping("/{organizationId}/members/{userId}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long organizationId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            User remover = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));
            permissionService.removeMemberFromOrganization(remover, organizationId, userId);
            return ResponseEntity.ok(Map.of("message", "メンバーを削除しました"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * メンバーの役割を変更
     * 管理者は自分と同じレベル以下の役割のみ付与可能
     */
    @PutMapping("/{organizationId}/members/{userId}/role")
    public ResponseEntity<?> updateMemberRole(
            @PathVariable Long organizationId,
            @PathVariable Long userId,
            @RequestBody UpdateRoleRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            User updater = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));
            OrganizationRole newRole = OrganizationRole.valueOf(request.getRole());

            OrganizationMember member = permissionService.updateMemberRole(
                    updater, organizationId, userId, newRole);

            return ResponseEntity.ok(createMemberResponse(member));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 子組織（階層グループ）を作成
     * ADMIN_SUPER以上の権限が必要
     */
    @PostMapping("/{parentOrganizationId}/sub-organizations")
    public ResponseEntity<?> createSubOrganization(
            @PathVariable Long parentOrganizationId,
            @RequestBody CreateSubOrgRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            User creator = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

            if (!permissionService.canCreateSubOrganization(creator, parentOrganizationId)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "子組織を作成する権限がありません"));
            }

            Organization parentOrg = organizationService.getOrganizationById(parentOrganizationId);
            Organization subOrg = new Organization(parentOrg.getTenant(), request.getName());
            subOrg.setDescription(request.getDescription());
            subOrg.setParent(parentOrg);
            subOrg = organizationService.createOrganization(subOrg);

            return ResponseEntity.ok(createOrganizationResponse(subOrg));
        } catch (SecurityException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ユーザーの組織内での権限をチェック
     */
    @GetMapping("/{organizationId}/permissions")
    public ResponseEntity<?> checkPermissions(
            @PathVariable Long organizationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

        Map<String, Object> permissions = new HashMap<>();
        permissions.put("isMember", permissionService.isMember(user, organizationId));
        permissions.put("role", permissionService.getUserRole(user, organizationId)
                .map(Enum::name).orElse(null));
        permissions.put("canAddMember", permissionService.canAddMember(user, organizationId));
        permissions.put("canRemoveMember", permissionService.canRemoveMember(user, organizationId));
        permissions.put("canCreateSubOrganization", permissionService.canCreateSubOrganization(user, organizationId));

        return ResponseEntity.ok(permissions);
    }

    // ヘルパーメソッド
    private Map<String, Object> createMemberResponse(OrganizationMember member) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", member.getId());
        response.put("userId", member.getUser().getId());
        response.put("username", member.getUser().getUsername());
        response.put("displayName", member.getUser().getDisplayName());
        response.put("role", member.getRole().name());
        response.put("joinedAt", member.getJoinedAt());
        return response;
    }

    private Map<String, Object> createOrganizationResponse(Organization org) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", org.getId());
        response.put("name", org.getName());
        response.put("description", org.getDescription());
        response.put("type", org.getOrganizationType());
        response.put("parentId", org.getParent() != null ? org.getParent().getId() : null);
        response.put("level", org.getLevel());
        response.put("path", org.getPath());
        response.put("createdAt", org.getCreatedAt());
        return response;
    }

    // リクエストDTO
    public static class AddMemberRequest {
        private String username;
        private String role;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class UpdateRoleRequest {
        private String role;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class CreateSubOrgRequest {
        private String name;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
