package com.chatapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chatapp.model.Friend;
import com.chatapp.model.User;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    @Query("SELECT f FROM Friend f WHERE "
            + "(f.requester = :user1 AND f.addressee = :user2) OR "
            + "(f.requester = :user2 AND f.addressee = :user1)")
    Optional<Friend> findFriendshipBetween(@Param("user1") User user1,
            @Param("user2") User user2);

    List<Friend> findByRequesterAndStatus(User requester,
            Friend.FriendStatus status);

    List<Friend> findByAddresseeAndStatus(User addressee,
            Friend.FriendStatus status);

    @Query("SELECT f FROM Friend f WHERE "
            + "(f.requester = :user OR f.addressee = :user) AND f.status = 'ACCEPTED'")
    List<Friend> findAcceptedFriends(@Param("user") User user);

    @Query("SELECT COUNT(f) FROM Friend f WHERE f.addressee = :user AND f.status = 'PENDING'")
    Long countPendingRequests(@Param("user") User user);

    void deleteByRequester(User requester);

    void deleteByAddressee(User addressee);

    @Query("SELECT f FROM Friend f WHERE f.requester = :user1 OR f.addressee = :user2")
    List<Friend> findByRequesterOrAddressee(@Param("user1") User requester, @Param("user2") User addressee);
}
