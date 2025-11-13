package com.chatapp.service;

import com.chatapp.model.AnonymousProfile;
import com.chatapp.model.Organization;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.repository.AnonymousProfileRepository;
import com.chatapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 匿名プロファイル管理サービス
 * 組織ごとに異なる匿名IDを生成・管理
 */
@Service
@Transactional(readOnly = true)
public class AnonymousProfileService {

    private static final Logger log = LoggerFactory.getLogger(AnonymousProfileService.class);
    private final AnonymousProfileRepository anonymousProfileRepository;
    private final UserRepository userRepository;
    private final OrganizationService organizationService;

    public AnonymousProfileService(
            AnonymousProfileRepository anonymousProfileRepository,
            UserRepository userRepository,
            OrganizationService organizationService) {
        this.anonymousProfileRepository = anonymousProfileRepository;
        this.userRepository = userRepository;
        this.organizationService = organizationService;
    }

    /**
     * ユーザーID×組織IDで匿名プロファイル取得または作成
     *
     * @param userId ユーザーID
     * @param organizationId 組織ID
     * @return 匿名プロファイル
     */
    @Transactional
    public AnonymousProfile getOrCreateProfileForUserInOrganization(Long userId, Long organizationId) {
        log.debug("Getting or creating anonymous profile for userId {} in organizationId {}",
                  userId, organizationId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        Organization organization = organizationService.getOrganizationById(organizationId);

        return getOrCreateAnonymousProfile(organization, user);
    }

    /**
     * 匿名プロファイル取得または作成
     * 組織×ユーザーの組み合わせで匿名プロファイルが存在しない場合は新規作成
     *
     * @param organization 組織
     * @param user ユーザー
     * @return 匿名プロファイル
     */
    @Transactional
    public AnonymousProfile getOrCreateAnonymousProfile(Organization organization, User user) {
        log.debug("Getting or creating anonymous profile for user {} in organization {}",
                  user.getId(), organization.getId());

        Optional<AnonymousProfile> existing = anonymousProfileRepository
                .findByOrganizationAndUser(organization, user);

        if (existing.isPresent()) {
            log.debug("Found existing anonymous profile: {}", existing.get().getId());
            return existing.get();
        }

        // 新規作成
        String anonymousId = generateAnonymousId(organization, user);
        AnonymousProfile profile = new AnonymousProfile(
                organization.getTenant(),
                organization,
                user,
                anonymousId
        );

        profile.setDisplayName("匿名ユーザー#" + anonymousId.substring(anonymousId.length() - 4));
        profile.setCreatedAt(LocalDateTime.now());

        AnonymousProfile saved = anonymousProfileRepository.save(profile);
        log.info("Created new anonymous profile with ID: {}", saved.getId());

        return saved;
    }

    /**
     * 匿名ID生成
     * ユーザーID + 組織ID + ソルトをハッシュ化して生成
     *
     * @param organization 組織
     * @param user ユーザー
     * @return 匿名ID（ハッシュ文字列）
     */
    private String generateAnonymousId(Organization organization, User user) {
        try {
            String input = user.getId() + "_" + organization.getId() + "_" + System.currentTimeMillis();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());

            // バイト配列を16進数文字列に変換
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            // 最初の8文字を使用（短縮版）
            String shortHash = hexString.toString().substring(0, 8).toUpperCase();

            // 既に存在する場合は再生成（衝突回避）
            if (anonymousProfileRepository.existsByTenantAndAnonymousId(organization.getTenant(), shortHash)) {
                return generateAnonymousId(organization, user);
            }

            return shortHash;

        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating anonymous ID", e);
            throw new RuntimeException("Failed to generate anonymous ID", e);
        }
    }

    /**
     * 匿名プロファイルID取得
     *
     * @param id プロファイルID
     * @return 匿名プロファイル
     * @throws EntityNotFoundException プロファイルが見つからない場合
     */
    public AnonymousProfile getProfileById(Long id) {
        log.debug("Fetching anonymous profile by ID: {}", id);
        return anonymousProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Anonymous profile not found with ID: " + id));
    }

    /**
     * 組織×ユーザーで匿名プロファイル取得
     *
     * @param organization 組織
     * @param user ユーザー
     * @return 匿名プロファイル
     */
    public Optional<AnonymousProfile> getProfileByOrganizationAndUser(Organization organization, User user) {
        log.debug("Fetching anonymous profile for user {} in organization {}",
                  user.getId(), organization.getId());
        return anonymousProfileRepository.findByOrganizationAndUser(organization, user);
    }

    /**
     * ユーザーの全匿名プロファイル取得
     *
     * @param user ユーザー
     * @return 匿名プロファイルリスト
     */
    public List<AnonymousProfile> getProfilesByUser(User user) {
        log.debug("Fetching all anonymous profiles for user: {}", user.getId());
        return anonymousProfileRepository.findByUser(user);
    }

    /**
     * 組織内の全匿名プロファイル取得
     *
     * @param organization 組織
     * @return 匿名プロファイルリスト
     */
    public List<AnonymousProfile> getProfilesByOrganization(Organization organization) {
        log.debug("Fetching all anonymous profiles for organization: {}", organization.getId());
        return anonymousProfileRepository.findByOrganization(organization);
    }

    /**
     * 匿名IDからプロファイル検索
     *
     * @param tenant テナント
     * @param anonymousId 匿名ID
     * @return 匿名プロファイル
     */
    public Optional<AnonymousProfile> getProfileByAnonymousId(Tenant tenant, String anonymousId) {
        log.debug("Fetching anonymous profile by anonymousId: {}", anonymousId);
        return anonymousProfileRepository.findByTenantAndAnonymousId(tenant, anonymousId);
    }

    /**
     * 匿名プロファイル更新
     *
     * @param id プロファイルID
     * @param displayName 表示名
     * @param avatarUrl アバターURL
     * @return 更新された匿名プロファイル
     */
    @Transactional
    public AnonymousProfile updateProfile(Long id, String displayName, String avatarUrl) {
        log.info("Updating anonymous profile ID: {}", id);

        AnonymousProfile profile = getProfileById(id);

        if (displayName != null) {
            profile.setDisplayName(displayName);
        }
        if (avatarUrl != null) {
            profile.setAvatarUrl(avatarUrl);
        }

        AnonymousProfile updated = anonymousProfileRepository.save(profile);
        log.info("Anonymous profile updated successfully: {}", id);

        return updated;
    }

    /**
     * 匿名プロファイル削除
     *
     * @param id プロファイルID
     */
    @Transactional
    public void deleteProfile(Long id) {
        log.info("Deleting anonymous profile ID: {}", id);

        if (!anonymousProfileRepository.existsById(id)) {
            throw new EntityNotFoundException("Anonymous profile not found with ID: " + id);
        }

        anonymousProfileRepository.deleteById(id);
        log.info("Anonymous profile deleted successfully: {}", id);
    }

    /**
     * 匿名プロファイルの存在確認
     *
     * @param organization 組織
     * @param user ユーザー
     * @return 存在する場合true
     */
    public boolean existsProfile(Organization organization, User user) {
        return anonymousProfileRepository.existsByOrganizationAndUser(organization, user);
    }

    /**
     * ユーザーの匿名プロファイル数カウント
     *
     * @param user ユーザー
     * @return プロファイル数
     */
    public long countProfilesByUser(User user) {
        return anonymousProfileRepository.findByUser(user).size();
    }
}
