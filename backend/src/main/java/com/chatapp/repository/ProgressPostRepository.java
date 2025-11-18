package com.chatapp.repository;

import com.chatapp.model.Organization;
import com.chatapp.model.ProgressPost;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.model.enums.PostType;
import com.chatapp.model.enums.Visibility;
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

    @EntityGraph(attributePaths = {"author", "tenant"})
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

    @EntityGraph(attributePaths = {"author", "organization"})
    @Query("SELECT p FROM ProgressPost p WHERE p.organization = :organization AND p.visibility IN :visibilities ORDER BY p.createdAt DESC")
    Page<ProgressPost> findByOrganizationAndVisibilityIn(@Param("organization") Organization organization, @Param("visibilities") List<Visibility> visibilities, Pageable pageable);

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
}
