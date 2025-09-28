package com.chatapp.repository;

import com.chatapp.model.ChatMessage;
import com.chatapp.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // 既存のチャット機能（互換性維持）
    List<ChatMessage> findByRoomIdOrderByCreatedAtDesc(String roomId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.roomId = :roomId AND cm.createdAt >= :since ORDER BY cm.createdAt ASC")
    List<ChatMessage> findRecentMessagesByRoom(@Param("roomId") String roomId, @Param("since") LocalDateTime since);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.roomId = :roomId ORDER BY cm.createdAt DESC")
    List<ChatMessage> findTop50ByRoomIdOrderByCreatedAtDesc(@Param("roomId") String roomId);
    
    // Letteral機能用のクエリ
    
    // グループ内のメッセージを取得（非表示でないもの）
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.group = :group AND cm.isHidden = false ORDER BY cm.createdAt DESC")
    List<ChatMessage> findByGroupAndNotHidden(@Param("group") Group group);
    
    // 削除予定のメッセージを取得（毎朝7:00の自動削除用）
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.scheduledDeleteAt <= :now")
    List<ChatMessage> findMessagesScheduledForDeletion(@Param("now") LocalDateTime now);
    
    // グループ内の今日のメッセージ数を取得
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.group = :group AND cm.createdAt >= :startOfDay")
    Long countTodayMessages(@Param("group") Group group, @Param("startOfDay") LocalDateTime startOfDay);
    
    // 通報されたメッセージを取得
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.reportCount >= :threshold AND cm.isReported = false")
    List<ChatMessage> findReportedMessages(@Param("threshold") Integer threshold);
    
    // 非表示になったメッセージを取得
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.hideCount >= :threshold AND cm.isHidden = false")
    List<ChatMessage> findHiddenMessages(@Param("threshold") Integer threshold);
    
    // ランダムな1投稿を取得（一般グループ用）
    @Query(value = "SELECT * FROM chat_messages cm WHERE cm.group_id = :groupId AND cm.is_hidden = false " +
                   "AND cm.message_type = 'ANONYMOUS_POST' ORDER BY RAND() LIMIT 1", nativeQuery = true)
    ChatMessage findRandomPostByGroup(@Param("groupId") Long groupId);
    
    // ユーザーが今日投稿したか確認
    @Query("SELECT COUNT(cm) > 0 FROM ChatMessage cm WHERE cm.sender = :user AND cm.group = :group " +
           "AND cm.createdAt >= :startOfDay AND cm.messageType = 'ANONYMOUS_POST'")
    Boolean hasUserPostedTodayInGroup(@Param("user") com.chatapp.model.User user, 
                                    @Param("group") Group group, 
                                    @Param("startOfDay") LocalDateTime startOfDay);
}