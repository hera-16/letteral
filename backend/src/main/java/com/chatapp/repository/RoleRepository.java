package com.chatapp.repository;

import com.chatapp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Roleエンティティのリポジトリ
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * ロール名でRoleを検索
     */
    Optional<Role> findByName(String name);

    /**
     * ロール名でRoleが存在するかチェック
     */
    boolean existsByName(String name);
}
