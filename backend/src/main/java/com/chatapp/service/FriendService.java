package com.chatapp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp.model.Friend;
import com.chatapp.model.User;
import com.chatapp.repository.FriendRepository;
import com.chatapp.repository.UserRepository;

@Service
@Transactional
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public FriendService(final FriendRepository friendRepository,
            final UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
    }

    public Friend sendFriendRequest(final Long requesterId,
            final String targetUserId) {
        final User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Requester not found"));

        final User target = userRepository.findByUsername(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Target user not found: " + targetUserId));

        if (requester.getId().equals(target.getId())) {
            throw new IllegalArgumentException(
                    "Cannot send friend request to yourself");
        }

        final Optional<Friend> existingFriendship = friendRepository
                .findFriendshipBetween(requester, target);
                if (existingFriendship.isPresent()) {
                        final Friend existing = existingFriendship.get();
                        switch (existing.getStatus()) {
                        case ACCEPTED:
                                throw new IllegalStateException("Already friends");
                        case PENDING:
                                throw new IllegalStateException("Friend request already sent");
                        case BLOCKED:
                                throw new IllegalStateException(
                                                "Cannot send friend request to blocked user");
                        case REJECTED:
                                existing.setStatus(Friend.FriendStatus.PENDING);
                                existing.setRequester(requester);
                                existing.setAddressee(target);
                                return friendRepository.save(existing);
                        default:
                                throw new IllegalStateException(
                                                "Unsupported status: " + existing.getStatus());
                        }
        }

        final Friend friendRequest = new Friend(requester, target);
        return friendRepository.save(friendRequest);
    }

    public Friend acceptFriendRequest(final Long userId,
            final Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final Friend request = friendRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Friend request not found"));

        if (!request.getAddressee().getId().equals(userId)) {
            throw new IllegalStateException(
                    "You can only accept requests sent to you");
        }

        if (request.getStatus() != Friend.FriendStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        request.setStatus(Friend.FriendStatus.ACCEPTED);
        return friendRepository.save(request);
    }

    public Friend rejectFriendRequest(final Long userId,
            final Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final Friend request = friendRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Friend request not found"));

        if (!request.getAddressee().getId().equals(userId)) {
            throw new IllegalStateException(
                    "You can only reject requests sent to you");
        }

        if (request.getStatus() != Friend.FriendStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        request.setStatus(Friend.FriendStatus.REJECTED);
        return friendRepository.save(request);
    }

    public Friend blockUser(final Long userId, final Long targetUserId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Target user not found"));

        final Optional<Friend> existingFriendship = friendRepository
                .findFriendshipBetween(user, target);

        if (existingFriendship.isPresent()) {
            final Friend existing = existingFriendship.get();
            existing.setStatus(Friend.FriendStatus.BLOCKED);
            existing.setRequester(user);
            existing.setAddressee(target);
            return friendRepository.save(existing);
        }
        final Friend blockRelation = new Friend(user, target);
        blockRelation.setStatus(Friend.FriendStatus.BLOCKED);
        return friendRepository.save(blockRelation);
    }

    public void removeFriend(final Long userId, final Long friendId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Friend not found"));

        final Optional<Friend> friendship = friendRepository
                .findFriendshipBetween(user, friend);
        if (friendship.isPresent()
                && friendship.get().getStatus() == Friend.FriendStatus.ACCEPTED) {
            friendRepository.delete(friendship.get());
        } else {
            throw new IllegalStateException("No friendship found");
        }
    }

    public List<User> getFriends(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final List<Friend> friendships = friendRepository
                .findAcceptedFriends(user);

        return friendships.stream().map(friendship -> friendship.getRequester()
                .getId().equals(userId) ? friendship.getAddressee()
                        : friendship.getRequester())
                .collect(Collectors.toList());
    }

    public List<Friend> getPendingRequests(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        return friendRepository.findByAddresseeAndStatus(user,
                Friend.FriendStatus.PENDING);
    }

    public List<Friend> getSentRequests(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        return friendRepository.findByRequesterAndStatus(user,
                Friend.FriendStatus.PENDING);
    }

    public Long getPendingRequestCount(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        return friendRepository.countPendingRequests(user);
    }

    public void resetAllFriendships(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        // Delete all friendships where user is either requester or addressee
        friendRepository.deleteByRequester(user);
        friendRepository.deleteByAddressee(user);
    }

    public List<com.chatapp.controller.FriendController.FriendshipDetail> getAllFriendshipsWithDetails(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        // Get all friendships where user is involved
        final List<Friend> allFriendships = friendRepository.findByRequesterOrAddressee(user, user);
        
        return allFriendships.stream()
                .map(friendship -> new com.chatapp.controller.FriendController.FriendshipDetail(
                        friendship.getId(),
                        friendship.getRequester().getUsername(),
                        friendship.getRequester().getDisplayName(),
                        friendship.getAddressee().getUsername(),
                        friendship.getAddressee().getDisplayName(),
                        friendship.getStatus().name(),
                        "N/A"
                ))
                .collect(Collectors.toList());
    }
}
