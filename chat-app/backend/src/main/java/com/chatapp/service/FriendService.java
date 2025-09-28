package com.chatapp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp.model.Friend;
import com.chatapp.model.User;
import com.chatapp.repository.FriendRepository;
import com.chatapp.repository.UserRepository;

@Service
@Transactional
public class FriendService {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * ユーザーIDでフレンド申請を送信
     */
    public Friend sendFriendRequest(Long requesterId, String targetUserId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        
        User target = userRepository.findByUsername(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found: " + targetUserId));
        
        // 自分自身には申請できない
        if (requester.getId().equals(target.getId())) {
            throw new RuntimeException("Cannot send friend request to yourself");
        }
        
        // 既存の関係をチェック
        Optional<Friend> existingFriendship = friendRepository.findFriendshipBetween(requester, target);
        if (existingFriendship.isPresent()) {
            Friend existing = existingFriendship.get();
            if (existing.getStatus() == Friend.FriendStatus.ACCEPTED) {
                throw new RuntimeException("Already friends");
            } else if (existing.getStatus() == Friend.FriendStatus.PENDING) {
                throw new RuntimeException("Friend request already sent");
            } else if (existing.getStatus() == Friend.FriendStatus.BLOCKED) {
                throw new RuntimeException("Cannot send friend request to blocked user");
            }
        }
        
        Friend friendRequest = new Friend(requester, target);
        return friendRepository.save(friendRequest);
    }

    /**
     * フレンド申請を承認
     */
    public Friend acceptFriendRequest(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Friend request = friendRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));
        
        // 申請の受信者が現在のユーザーかチェック
        if (!request.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("You can only accept requests sent to you");
        }
        
        if (request.getStatus() != Friend.FriendStatus.PENDING) {
            throw new RuntimeException("Request is not pending");
        }
        
        request.setStatus(Friend.FriendStatus.ACCEPTED);
        return friendRepository.save(request);
    }

    /**
     * フレンド申請を拒否
     */
    public Friend rejectFriendRequest(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Friend request = friendRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));
        
        if (!request.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("You can only reject requests sent to you");
        }
        
        if (request.getStatus() != Friend.FriendStatus.PENDING) {
            throw new RuntimeException("Request is not pending");
        }
        
        request.setStatus(Friend.FriendStatus.REJECTED);
        return friendRepository.save(request);
    }

    /**
     * フレンドをブロック
     */
    public Friend blockUser(Long userId, Long targetUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        
        Optional<Friend> existingFriendship = friendRepository.findFriendshipBetween(user, target);
        
        if (existingFriendship.isPresent()) {
            Friend existing = existingFriendship.get();
            existing.setStatus(Friend.FriendStatus.BLOCKED);
            // ブロック操作は常に申請者が現在のユーザーになるよう調整
            existing.setRequester(user);
            existing.setAddressee(target);
            return friendRepository.save(existing);
        } else {
            Friend blockRelation = new Friend(user, target);
            blockRelation.setStatus(Friend.FriendStatus.BLOCKED);
            return friendRepository.save(blockRelation);
        }
    }

    /**
     * フレンド関係を削除
     */
    public void removeFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));
        
        Optional<Friend> friendship = friendRepository.findFriendshipBetween(user, friend);
        if (friendship.isPresent() && friendship.get().getStatus() == Friend.FriendStatus.ACCEPTED) {
            friendRepository.delete(friendship.get());
        } else {
            throw new RuntimeException("No friendship found");
        }
    }

    /**
     * ユーザーのフレンド一覧を取得
     */
    public List<User> getFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Friend> friendships = friendRepository.findAcceptedFriends(user);
        
        return friendships.stream()
                .map(friendship -> {
                    // 相手方のユーザーを返す
                    return friendship.getRequester().getId().equals(userId) 
                            ? friendship.getAddressee() 
                            : friendship.getRequester();
                })
                .collect(Collectors.toList());
    }

    /**
     * 受信したフレンド申請一覧を取得
     */
    public List<Friend> getPendingRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return friendRepository.findByAddresseeAndStatus(user, Friend.FriendStatus.PENDING);
    }

    /**
     * 送信したフレンド申請一覧を取得
     */
    public List<Friend> getSentRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return friendRepository.findByRequesterAndStatus(user, Friend.FriendStatus.PENDING);
    }

    /**
     * 待機中の申請数を取得
     */
    public Long getPendingRequestCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return friendRepository.countPendingRequests(user);
    }
}