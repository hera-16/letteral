package com.chatapp.repository;

import com.chatapp.model.Friend;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    
    // 特定のユーザー間のフレンド関係を検索
    @Query("SELECT f FROM Friend f WHERE " +
           "(f.requester = :user1 AND f.addressee = :user2) OR " +
           "(f.requester = :user2 AND f.addressee = :user1)")
    Optional<Friend> findFriendshipBetween(@Param("user1") User user1, @Param("user2") User user2);
    
    // ユーザーが送信したフレンド申請を取得
    List<Friend> findByRequesterAndStatus(User requester, Friend.FriendStatus status);
    
    // ユーザーが受信したフレンド申請を取得
    List<Friend> findByAddresseeAndStatus(User addressee, Friend.FriendStatus status);
    
    // ユーザーのフレンド一覧を取得（承認済みのもの）
    @Query("SELECT f FROM Friend f WHERE " +
           "(f.requester = :user OR f.addressee = :user) AND f.status = 'ACCEPTED'")
    List<Friend> findAcceptedFriends(@Param("user") User user);
    
    // ユーザーの全フレンド関係を取得（送信・受信両方）
    @Query("SELECT f FROM Friend f WHERE f.requester = :user OR f.addressee = :user")
    List<Friend> findAllFriendRelations(@Param("user") User user);
    
    // 待機中のフレンド申請数を取得
    @Query("SELECT COUNT(f) FROM Friend f WHERE f.addressee = :user AND f.status = 'PENDING'")
    Long countPendingRequests(@Param("user") User user);
    
    // ユーザーがブロックした人一覧
    @Query("SELECT f FROM Friend f WHERE f.requester = :user AND f.status = 'BLOCKED'")
    List<Friend> findBlockedUsers(@Param("user") User user);
}