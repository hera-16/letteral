package com.chatapp.service;

import com.chatapp.model.BoxType;
import com.chatapp.model.Organization;
import com.chatapp.model.OrganizationMember;
import com.chatapp.model.enums.OrganizationRole;
import com.chatapp.model.ProgressPost;
import com.chatapp.model.User;
import com.chatapp.repository.OrganizationMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Boxアクセス制御サービス
 * Letteral権限表に基づく閲覧・投稿・返信権限のチェック
 *
 * このサービスは BoxType (全社箱・部署箱・課箱・プロジェクト箱) に基づく権限制御を行います。
 *
 * 組織階層とロールベースのアクセス制御:
 * - BoxAccessControlService: BoxType による階層的権限制御（このクラス）
 * - OrganizationPermissionService: 組織階層とロールによるアクセス制御
 *
 * 権限仕様:
 * 1. 全社箱: PM以上は全投稿閲覧、一般は自分の投稿のみ
 * 2. 部署箱: 部長 + 所属部署メンバー
 * 3. 課箱: 課長 + 所属課メンバー
 * 4. プロジェクト箱: PM + プロジェクトメンバー
 * 5. 返信: PM以上のみ可能
 * 6. 返信閲覧: 投稿者本人 + PM以上
 */
@Service
public class BoxAccessControlService {

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    /**
     * ユーザーが投稿を閲覧可能かチェック
     *
     * @param user ユーザー
     * @param post 投稿
     * @return 閲覧可能ならtrue
     */
    public boolean canViewPost(User user, ProgressPost post) {
        if (post == null || user == null) {
            return false;
        }

        BoxType boxType = post.getBoxType();
        String boxTypeName = (boxType != null && boxType.getBoxName() != null)
            ? boxType.getBoxName().toUpperCase()
            : "COMPANY";

        OrganizationRole userRole = getUserOrganizationRole(user);

        switch (boxTypeName) {
            case "COMPANY":
                return canViewCompanyBox(user, post, userRole);

            case "DEPARTMENT":
                return canViewDepartmentBox(user, post, userRole);

            case "SECTION":
                return canViewSectionBox(user, post, userRole);

            case "PROJECT":
                return canViewProjectBox(user, post, userRole);

            default:
                return false;
        }
    }

    /**
     * 全社箱の閲覧権限チェック
     * - PM(ADMIN_SUPER)以上: 全投稿閲覧可能
     * - 一般メンバー: 自分の投稿のみ閲覧可能
     */
    private boolean canViewCompanyBox(User user, ProgressPost post, OrganizationRole userRole) {
        // PM(ADMIN_SUPER)以上は全投稿閲覧可能
        if (userRole.hasAuthorityOver(OrganizationRole.ADMIN_SUPER)) {
            return true;
        }

        // 一般メンバー(MEMBER)は自分の投稿のみ
        User author = post.getAuthor();
        return author != null && author.getId().equals(user.getId());
    }

    /**
     * 部署箱の閲覧権限チェック
     * - 部長(ADMIN_CORE)以上: 全投稿閲覧可能
     * - 所属部署のメンバー: 閲覧可能
     */
    private boolean canViewDepartmentBox(User user, ProgressPost post, OrganizationRole userRole) {
        Organization org = post.getOrganization();
        if (org == null) {
            return false;
        }

        // その組織での権限を取得
        OrganizationRole roleInOrg = getUserOrganizationRole(user, org.getId());

        // 部長(ADMIN_CORE)以上または所属メンバー
        return roleInOrg.hasAuthorityOver(OrganizationRole.ADMIN_CORE)
                || userBelongsToOrganization(user, org.getId());
    }

    /**
     * 課箱の閲覧権限チェック
     * - 課長(ADMIN_LEAD)以上: 全投稿閲覧可能
     * - 所属課のメンバー: 閲覧可能
     */
    private boolean canViewSectionBox(User user, ProgressPost post, OrganizationRole userRole) {
        Organization org = post.getOrganization();
        if (org == null) {
            return false;
        }

        // その組織での権限を取得
        OrganizationRole roleInOrg = getUserOrganizationRole(user, org.getId());

        // 課長(ADMIN_LEAD)以上または所属メンバー
        return roleInOrg.hasAuthorityOver(OrganizationRole.ADMIN_LEAD)
                || userBelongsToOrganization(user, org.getId());
    }

    /**
     * プロジェクト箱の閲覧権限チェック
     * - PM(ADMIN_SUPER)以上: 全投稿閲覧可能
     * - プロジェクトメンバー: 閲覧可能
     */
    private boolean canViewProjectBox(User user, ProgressPost post, OrganizationRole userRole) {
        Organization org = post.getOrganization();
        if (org == null) {
            return false;
        }

        // その組織での権限を取得
        OrganizationRole roleInOrg = getUserOrganizationRole(user, org.getId());

        // PM(ADMIN_SUPER)以上またはプロジェクトメンバー
        return roleInOrg.hasAuthorityOver(OrganizationRole.ADMIN_SUPER)
                || userBelongsToOrganization(user, org.getId());
    }

    /**
     * ユーザーが返信を閲覧可能かチェック
     * - 投稿者本人
     * - PM以上
     * - 一般メンバーは他者の返信は見えない
     *
     * @param user ユーザー
     * @param post 対象の投稿
     * @param replyAuthorId 返信者のID
     * @return 閲覧可能ならtrue
     */
    public boolean canViewReply(User user, ProgressPost post, Long replyAuthorId) {
        if (user == null || post == null) {
            return false;
        }

        // 投稿者本人
        User author = post.getAuthor();
        if (author != null && author.getId().equals(user.getId())) {
            return true;
        }

        // ADMIN_SUPER以上
        OrganizationRole userRole = getUserOrganizationRole(user);
        if (userRole.hasAuthorityOver(OrganizationRole.ADMIN_SUPER)) {
            return true;
        }

        // 一般メンバーは他者の返信は見えない
        return false;
    }

    /**
     * ユーザーが返信可能かチェック
     * - ADMIN_SUPER以上のみ返信可能
     *
     * @param user ユーザー
     * @return 返信可能ならtrue
     */
    public boolean canReply(User user) {
        if (user == null) {
            return false;
        }

        OrganizationRole userRole = getUserOrganizationRole(user);
        return userRole.hasAuthorityOver(OrganizationRole.ADMIN_SUPER);
    }

    /**
     * ユーザーが分析機能を使用可能かチェック
     *
     * @param user ユーザー
     * @param boxType Box種別
     * @return 使用可能ならtrue
     */
    public boolean canAnalyze(User user, String boxType) {
        if (user == null || boxType == null) {
            return false;
        }

        OrganizationRole userRole = getUserOrganizationRole(user);

        switch (boxType.toUpperCase()) {
            case "COMPANY":
                // 全社箱: 社長のみ
                return userRole == OrganizationRole.ADMIN_ROOT || userRole == OrganizationRole.OWNER;

            case "DEPARTMENT":
                // 部署箱: 部長以上
                return userRole.hasAuthorityOver(OrganizationRole.ADMIN_CORE);

            case "SECTION":
                // 課箱: 課長以上
                return userRole.hasAuthorityOver(OrganizationRole.ADMIN_LEAD);

            case "PROJECT":
                // プロジェクト箱: ADMIN_SUPER以上
                return userRole.hasAuthorityOver(OrganizationRole.ADMIN_SUPER);

            default:
                return false;
        }
    }

    /**
     * ユーザーが特定の組織に所属しているかチェック
     *
     * @param user ユーザー
     * @param organizationId 組織ID
     * @return 所属していればtrue
     */
    private boolean userBelongsToOrganization(User user, Long organizationId) {
        if (user == null || organizationId == null) {
            return false;
        }

        // OrganizationMemberテーブルから所属をチェック
        return organizationMemberRepository
                .findByUserIdAndOrganizationId(user.getId(), organizationId)
                .isPresent();
    }

    /**
     * ユーザーの組織内権限階級を取得
     * ユーザーが所属するすべての組織の中で最も高い権限を返す
     *
     * @param user ユーザー
     * @return 組織内権限階級
     */
    private OrganizationRole getUserOrganizationRole(User user) {
        if (user == null) {
            return OrganizationRole.MEMBER;
        }

        // ユーザーのすべての組織メンバーシップから最高権限を取得
        return organizationMemberRepository.findByUserId(user.getId())
                .stream()
                .map(OrganizationMember::getRole)
                .min((r1, r2) -> Integer.compare(r1.ordinal(), r2.ordinal())) // ordinalが小さいほど高権限
                .orElse(OrganizationRole.MEMBER); // 所属組織がない場合はMEMBER
    }

    /**
     * ユーザーの特定組織における権限階級を取得
     *
     * @param user ユーザー
     * @param organizationId 組織ID
     * @return 組織内権限階級
     */
    private OrganizationRole getUserOrganizationRole(User user, Long organizationId) {
        if (user == null || organizationId == null) {
            return OrganizationRole.MEMBER;
        }

        // 指定された組織でのメンバーシップを取得
        Optional<OrganizationMember> memberOpt = organizationMemberRepository
                .findByUserIdAndOrganizationId(user.getId(), organizationId);

        return memberOpt.map(OrganizationMember::getRole)
                .orElse(OrganizationRole.MEMBER);
    }
}
