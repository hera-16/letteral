package com.chatapp.repository;

import com.chatapp.model.BoxType;
import com.chatapp.model.Organization;
import com.chatapp.model.ProgressPost;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.model.enums.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 進捗投稿リポジトリ
 */
@Repository
public interface ProgressPostRepository extends JpaRepository<ProgressPost, Long> {

    @EntityGraph(attributePaths = {"author", "tenant", "organization"})
    Page<ProgressPost> findByTenantOrderByCreatedAtDesc(Tenant tenant, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "organization"})
    Page<ProgressPost> findByOrganizationOrderByCreatedAtDesc(Organization organization, Pageable pageable);

    @EntityGraph(attributePaths = {"tenant"})
    Page<ProgressPost> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    List<ProgressPost> findByTenantAndPostDateBetweenOrderByPostDateDesc(Tenant tenant, LocalDate startDate, LocalDate endDate);

    List<ProgressPost> findByOrganizationAndPostDateBetweenOrderByPostDateDesc(Organization organization, LocalDate startDate, LocalDate endDate);

    Optional<ProgressPost> findByIdAndTenant(Long id, Tenant tenant);

    @EntityGraph(attributePaths = {"author", "tenant"})
    @Query("SELECT p FROM ProgressPost p WHERE p.tenant = :tenant AND p.postType = :postType ORDER BY p.createdAt DESC")
    Page<ProgressPost> findByTenantAndPostType(@Param("tenant") Tenant tenant, @Param("postType") PostType postType, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "tenant"})
    @Query("SELECT p FROM ProgressPost p WHERE p.tenant = :tenant AND p.postDate >= :startDate AND p.postDate <= :endDate AND p.postType = :postType ORDER BY p.postDate DESC")
    List<ProgressPost> findByTenantAndDateRangeAndPostType(@Param("tenant") Tenant tenant, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("postType") PostType postType);

    Long countByAuthor(User author);

    Long countByOrganizationAndPostDateBetween(Organization organization, LocalDate startDate, LocalDate endDate);

    @Query("SELECT p FROM ProgressPost p WHERE p.author = :author AND p.createdAt >= :startDate AND p.createdAt <= :endDate ORDER BY p.createdAt DESC")
    List<ProgressPost> findByAuthorAndCreatedAtBetween(@Param("author") User author, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM ProgressPost p WHERE p.tenant.id = :tenantId")
    Long countByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(p) FROM ProgressPost p WHERE p.tenant.id = :tenantId AND p.createdAt >= :startDate")
    Long countByTenantIdAndCreatedAtAfter(@Param("tenantId") Long tenantId, @Param("startDate") java.time.LocalDateTime startDate);

    // ========================================
    // Box System関連クエリメソッド
    // ========================================

    /**
     * BoxTypeで進捗投稿を検索（ページネーション）
     */
    @EntityGraph(attributePaths = {"author", "tenant", "boxType"})
    Page<ProgressPost> findByBoxTypeOrderByCreatedAtDesc(BoxType boxType, Pageable pageable);

    /**
     * BoxTypeとテナントで進捗投稿を検索
     */
    @EntityGraph(attributePaths = {"author", "organization", "boxType"})
    @Query("SELECT p FROM ProgressPost p WHERE p.tenant = :tenant AND p.boxType = :boxType ORDER BY p.createdAt DESC")
    Page<ProgressPost> findByTenantAndBoxType(@Param("tenant") Tenant tenant, @Param("boxType") BoxType boxType, Pageable pageable);

    /**
     * 複数のBoxTypeで進捗投稿を検索（階層的閲覧に使用）
     */
    @EntityGraph(attributePaths = {"author", "tenant", "boxType"})
    @Query("SELECT p FROM ProgressPost p WHERE p.tenant = :tenant AND p.boxType IN :boxTypes ORDER BY p.createdAt DESC")
    Page<ProgressPost> findByTenantAndBoxTypeIn(@Param("tenant") Tenant tenant, @Param("boxTypes") List<BoxType> boxTypes, Pageable pageable);

    /**
     * 組織とBoxTypeで進捗投稿を検索
     */
    @EntityGraph(attributePaths = {"author", "organization", "boxType"})
    @Query("SELECT p FROM ProgressPost p WHERE p.organization = :organization AND p.boxType = :boxType ORDER BY p.createdAt DESC")
    Page<ProgressPost> findByOrganizationAndBoxType(@Param("organization") Organization organization, @Param("boxType") BoxType boxType, Pageable pageable);

    /**
     * テナント、BoxType、投稿タイプで進捗投稿を検索
     */
    @EntityGraph(attributePaths = {"author", "tenant", "boxType"})
    @Query("SELECT p FROM ProgressPost p WHERE p.tenant = :tenant AND p.boxType = :boxType AND p.postType = :postType ORDER BY p.createdAt DESC")
    Page<ProgressPost> findByTenantAndBoxTypeAndPostType(@Param("tenant") Tenant tenant, @Param("boxType") BoxType boxType, @Param("postType") PostType postType, Pageable pageable);

    /**
     * 日付範囲とBoxTypeで進捗投稿を検索
     */
    @EntityGraph(attributePaths = {"author", "tenant", "boxType"})
    @Query("SELECT p FROM ProgressPost p WHERE p.tenant = :tenant AND p.boxType = :boxType AND p.postDate >= :startDate AND p.postDate <= :endDate ORDER BY p.postDate DESC")
    List<ProgressPost> findByTenantAndBoxTypeAndPostDateBetween(@Param("tenant") Tenant tenant, @Param("boxType") BoxType boxType, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 返信投稿を検索
     */
    @EntityGraph(attributePaths = {"author", "parentPost"})
    @Query("SELECT p FROM ProgressPost p WHERE p.parentPost.id = :parentPostId ORDER BY p.createdAt ASC")
    List<ProgressPost> findByParentPostId(@Param("parentPostId") Long parentPostId);

    /**
     * 返信投稿をページネーションで検索
     */
    @EntityGraph(attributePaths = {"author", "parentPost"})
    Page<ProgressPost> findByParentPostOrderByCreatedAtAsc(ProgressPost parentPost, Pageable pageable);

    /**
     * ルート投稿のみを検索（返信ではない投稿）
     */
    @EntityGraph(attributePaths = {"author", "tenant", "boxType"})
    @Query("SELECT p FROM ProgressPost p WHERE p.tenant = :tenant AND p.isReply = false ORDER BY p.createdAt DESC")
    Page<ProgressPost> findRootPostsByTenant(@Param("tenant") Tenant tenant, Pageable pageable);

    /**
     * BoxTypeでルート投稿のみを検索
     */
    @EntityGraph(attributePaths = {"author", "tenant", "boxType"})
    @Query("SELECT p FROM ProgressPost p WHERE p.tenant = :tenant AND p.boxType = :boxType AND p.isReply = false ORDER BY p.createdAt DESC")
    Page<ProgressPost> findRootPostsByTenantAndBoxType(@Param("tenant") Tenant tenant, @Param("boxType") BoxType boxType, Pageable pageable);

    /**
     * BoxTypeの投稿数をカウント
     */
    Long countByBoxType(BoxType boxType);

    /**
     * テナントとBoxTypeで投稿数をカウント
     */
    @Query("SELECT COUNT(p) FROM ProgressPost p WHERE p.tenant = :tenant AND p.boxType = :boxType")
    Long countByTenantAndBoxType(@Param("tenant") Tenant tenant, @Param("boxType") BoxType boxType);

    /**
     * 組織内の最大匿名番号を取得（新規投稿の番号割り当てに使用）
     */
    @Query("SELECT COALESCE(MAX(p.anonymousNumber), 0) FROM ProgressPost p WHERE p.organization.id = :organizationId")
    Integer findMaxAnonymousNumberByOrganizationId(@Param("organizationId") Long organizationId);
}
