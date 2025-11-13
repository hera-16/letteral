package com.chatapp.repository;

import com.chatapp.model.Organization;
import com.chatapp.model.ProgressPost;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.model.enums.PostType;
import com.chatapp.model.enums.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<ProgressPost> findByTenantOrderByCreatedAtDesc(Tenant tenant, Pageable pageable);

    Page<ProgressPost> findByOrganizationOrderByCreatedAtDesc(Organization organization, Pageable pageable);

    Page<ProgressPost> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    List<ProgressPost> findByTenantAndPostDateBetweenOrderByPostDateDesc(Tenant tenant, LocalDate startDate, LocalDate endDate);

    List<ProgressPost> findByOrganizationAndPostDateBetweenOrderByPostDateDesc(Organization organization, LocalDate startDate, LocalDate endDate);

    Optional<ProgressPost> findByIdAndTenant(Long id, Tenant tenant);

    @Query("SELECT p FROM ProgressPost p WHERE p.tenant = :tenant AND p.postType = :postType ORDER BY p.createdAt DESC")
    Page<ProgressPost> findByTenantAndPostType(@Param("tenant") Tenant tenant, @Param("postType") PostType postType, Pageable pageable);

    @Query("SELECT p FROM ProgressPost p WHERE p.organization = :organization AND p.visibility IN :visibilities ORDER BY p.createdAt DESC")
    Page<ProgressPost> findByOrganizationAndVisibilityIn(@Param("organization") Organization organization, @Param("visibilities") List<Visibility> visibilities, Pageable pageable);

    @Query("SELECT p FROM ProgressPost p WHERE p.tenant = :tenant AND p.postDate >= :startDate AND p.postDate <= :endDate AND p.postType = :postType ORDER BY p.postDate DESC")
    List<ProgressPost> findByTenantAndDateRangeAndPostType(@Param("tenant") Tenant tenant, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("postType") PostType postType);

    Long countByAuthor(User author);

    Long countByOrganizationAndPostDateBetween(Organization organization, LocalDate startDate, LocalDate endDate);
}
