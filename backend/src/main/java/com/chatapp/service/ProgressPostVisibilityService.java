package com.chatapp.service;

import com.chatapp.model.Organization;
import com.chatapp.model.ProgressPost;
import com.chatapp.model.User;
import com.chatapp.repository.OrganizationMemberRepository;
import com.chatapp.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 進捗投稿の可視性を管理するサービス
 * 組織階層とユーザーの権限に基づいて、投稿の閲覧権限を判定する
 *
 * このサービスは Visibility (COMPANY, ORGANIZATION, DEPARTMENT, TEAM, PRIVATE) に基づく可視性制御を行います。
 *
 * BoxAccessControlService との違い:
 * - ProgressPostVisibilityService: Visibility による可視性制御（このクラス）
 * - BoxAccessControlService: BoxType による階層的権限制御
 *
 * Visibility 仕様:
 * - COMPANY: 全社公開 - 同じテナントのすべてのユーザー
 * - ORGANIZATION: 部門公開 - 投稿組織とその上位組織のメンバー
 * - DEPARTMENT: 部署内 - 投稿組織とその上位組織のメンバー
 * - TEAM: チーム公開 - 投稿組織のメンバーのみ
 * - PRIVATE: 非公開 - 投稿者のみ
 */
@Service
@Transactional(readOnly = true)
public class ProgressPostVisibilityService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    public ProgressPostVisibilityService(
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository organizationMemberRepository) {
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
    }

    /**
     * ユーザーが投稿を閲覧できるかチェック
     *
     * 新しいロジック (組織階層による権限制御):
     * - 同じテナント内であること
     * - 投稿された組織またはその上位組織のメンバーであること
     * - TENANT_ADMIN/SUPER_ADMIN役割を持つ場合は全投稿を閲覧可能
     *
     * @param user ユーザー
     * @param post 投稿
     * @return 閲覧可能な場合true
     */
    public boolean canViewPost(User user, ProgressPost post) {
        // 1. テナントが異なる場合は閲覧不可
        if (!user.getTenant().getId().equals(post.getTenant().getId())) {
            return false;
        }

        // 2. CEO/SUPER_ADMIN権限チェック（全投稿を閲覧可能）
        String userRole = user.getRole();
        if ("TENANT_ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            return true;
        }

        // 3. 投稿された組織を確認
        Organization postOrganization = post.getOrganization();
        if (postOrganization == null) {
            // 組織が設定されていない場合は閲覧不可
            return false;
        }

        // 4. 投稿された組織またはその上位組織のメンバーであるかチェック
        return canViewOrganizationPost(user, postOrganization);
    }

    /**
     * ユーザーが組織の投稿を閲覧できるかチェック
     * 組織階層を考慮し、投稿組織またはその上位組織のメンバーであれば閲覧可能
     * TENANT_ADMIN/SUPER_ADMIN役割を持つ場合は全投稿を閲覧可能
     *
     * @param user ユーザー
     * @param organization 投稿された組織
     * @return 閲覧可能な場合true
     */
    public boolean canViewOrganizationPost(User user, Organization organization) {
        // CEO/SUPER_ADMIN権限チェック（全投稿を閲覧可能）
        String userRole = user.getRole();
        if ("TENANT_ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            return true;
        }

        // 投稿された組織のメンバーか確認
        if (isOrganizationMember(user, organization)) {
            return true;
        }

        // 上位組織のメンバーか確認（親→祖父母...と遡る）
        Organization currentOrg = organization;
        while (currentOrg.getParent() != null) {
            currentOrg = currentOrg.getParent();
            if (isOrganizationMember(user, currentOrg)) {
                return true;
            }
        }

        return false;
    }

    /**
     * ユーザーが組織のメンバーかチェック
     *
     * @param user ユーザー
     * @param organization 組織
     * @return メンバーの場合true
     */
    public boolean isOrganizationMember(User user, Organization organization) {
        return organizationMemberRepository
                .findByUserIdAndOrganizationId(user.getId(), organization.getId())
                .isPresent();
    }

    /**
     * ユーザーが閲覧可能な組織IDのセットを取得
     * ユーザーが所属する組織とその下位組織すべてを含む
     * TENANT_ADMIN/SUPER_ADMIN役割を持つ場合はテナント内の全組織を返す
     *
     * @param user ユーザー
     * @param tenantId テナントID
     * @return 閲覧可能な組織IDのセット
     */
    public Set<Long> getViewableOrganizationIds(User user, Long tenantId) {
        Set<Long> viewableOrgIds = new HashSet<>();

        // CEO/SUPER_ADMIN権限チェック（全組織を閲覧可能）
        String userRole = user.getRole();
        if ("TENANT_ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            // テナント内の全組織を返す
            return organizationRepository.findByTenantAndIsActiveOrderByDisplayOrder(
                    user.getTenant(), true)
                    .stream()
                    .map(Organization::getId)
                    .collect(Collectors.toSet());
        }

        // ユーザーが所属する組織を取得
        List<Long> memberOrganizationIds = organizationMemberRepository
                .findByUser(user)
                .stream()
                .filter(member -> member.getOrganization().getTenant().getId().equals(tenantId))
                .map(member -> member.getOrganization().getId())
                .collect(Collectors.toList());

        // 各所属組織とその下位組織を追加
        for (Long orgId : memberOrganizationIds) {
            Organization org = organizationRepository.findById(orgId).orElse(null);
            if (org != null) {
                viewableOrgIds.add(orgId);
                addDescendantOrganizations(org, viewableOrgIds);
            }
        }

        return viewableOrgIds;
    }

    /**
     * 組織の下位組織（子・孫...）をすべて取得してセットに追加
     *
     * @param organization 親組織
     * @param organizationIds 組織IDを追加するセット
     */
    private void addDescendantOrganizations(Organization organization, Set<Long> organizationIds) {
        List<Organization> children = organizationRepository.findByParentAndIsActiveOrderByDisplayOrder(organization, true);
        for (Organization child : children) {
            organizationIds.add(child.getId());
            addDescendantOrganizations(child, organizationIds); // 再帰的に下位組織を追加
        }
    }

    /**
     * 投稿リストをフィルタリングして、ユーザーが閲覧可能な投稿のみを返す
     *
     * @param user ユーザー
     * @param posts 投稿リスト
     * @return フィルタリングされた投稿リスト
     */
    public List<ProgressPost> filterViewablePosts(User user, List<ProgressPost> posts) {
        return posts.stream()
                .filter(post -> canViewPost(user, post))
                .collect(Collectors.toList());
    }

    /**
     * ユーザーが特定の組織以下の投稿を閲覧できるかチェック
     * 組織本体またはその親組織のメンバーである必要がある
     * TENANT_ADMIN/SUPER_ADMIN役割を持つ場合は全投稿を閲覧可能
     *
     * @param user ユーザー
     * @param organizationId 組織ID
     * @return 閲覧可能な場合true
     */
    public boolean canViewOrganizationPosts(User user, Long organizationId) {
        // CEO/SUPER_ADMIN権限チェック（全投稿を閲覧可能）
        String userRole = user.getRole();
        if ("TENANT_ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            return true;
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElse(null);
        if (organization == null) {
            return false;
        }

        // 該当組織のメンバーか確認
        if (isOrganizationMember(user, organization)) {
            return true;
        }

        // 上位組織のメンバーか確認
        Organization currentOrg = organization;
        while (currentOrg.getParent() != null) {
            currentOrg = currentOrg.getParent();
            if (isOrganizationMember(user, currentOrg)) {
                return true;
            }
        }

        return false;
    }
}
