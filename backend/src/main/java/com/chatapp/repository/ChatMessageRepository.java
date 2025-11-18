package com.chatapp.repository;

import com.chatapp.model.ChatMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<ChatMessage> findByRoomIdOrderByCreatedAtDesc(String roomId);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.roomId = :roomId AND cm.createdAt >= :since ORDER BY cm.createdAt ASC")
    List<ChatMessage> findRecentMessagesByRoom(@Param("roomId") String roomId, @Param("since") LocalDateTime since);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.roomId = :roomId ORDER BY cm.createdAt DESC")
    List<ChatMessage> findTop50ByRoomIdOrderByCreatedAtDesc(@Param("roomId") String roomId);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.tenantId = :tenantId")
    Long countByTenantId(@Param("tenantId") Long tenantId);
}