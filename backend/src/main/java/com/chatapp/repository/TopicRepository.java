package com.chatapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chatapp.model.Topic;
import com.chatapp.model.User;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    /**
     * Find all active topics.
     *
     * @return list of active topics
     */
    List<Topic> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * Find topics by category.
     *
     * @param category the category name
     * @return list of topics in the category
     */
    List<Topic> findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(String category);

    /**
     * Find all topics created by a specific user.
     *
     * @param creator the user who created the topics
     * @return list of topics
     */
    List<Topic> findByCreatorOrderByCreatedAtDesc(User creator);

    /**
     * Search topics by name.
     *
     * @param name the search term
     * @return list of matching topics
     */
    @Query("SELECT t FROM Topic t WHERE t.isActive = true AND LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY t.createdAt DESC")
    List<Topic> searchByName(@Param("name") String name);

    /**
     * Get all distinct categories.
     *
     * @return list of category names
     */
    @Query("SELECT DISTINCT t.category FROM Topic t WHERE t.isActive = true AND t.category IS NOT NULL ORDER BY t.category")
    List<String> findAllCategories();
}
