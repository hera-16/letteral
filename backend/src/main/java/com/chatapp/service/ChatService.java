package com.chatapp.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatapp.dto.ChatMessageDto;
import com.chatapp.model.ChatMessage;
import com.chatapp.model.Friend;
import com.chatapp.model.User;
import com.chatapp.repository.ChatMessageRepository;
import com.chatapp.repository.FriendRepository;
import com.chatapp.repository.UserRepository;

@Service
public class ChatService {
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FriendRepository friendRepository;
    
    @Autowired
    private GroupService groupService;
    
    @Autowired
    private AnonymousNameService anonymousNameService;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public ChatMessageDto saveMessage(ChatMessageDto messageDto) {
        User sender = userRepository.findByUsername(messageDto.getSenderUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ChatMessage chatMessage = new ChatMessage(
                messageDto.getContent(),
                sender,
                messageDto.getRoomId(),
                ChatMessage.MessageType.valueOf(messageDto.getMessageType().toUpperCase())
        );
        
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return convertToDto(savedMessage);
    }
    
    public List<ChatMessageDto> getRecentMessages(String roomId) {
        // 最新50件を取得し、時系列順（古い→新しい）で返す
        List<ChatMessage> messages = chatMessageRepository.findTop50ByRoomIdOrderByCreatedAtDesc(roomId);
        // リストを逆順にして古い順に変更
        messages = messages.stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .collect(Collectors.toList());
        
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get messages by room ID (generic method for all room types).
     * Validates access based on room type.
     */
    @SuppressWarnings("UnnecessaryTemporaryOnConversionFromString")
    public List<ChatMessageDto> getMessagesByRoomId(String roomId, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Parse room type and validate access
        if (roomId.startsWith("group-")) {
            long groupId = Long.parseLong(roomId.substring(6));
            return getGroupMessages(groupId, currentUser.getId());
        } else if (roomId.startsWith("friend-")) {
            long friendshipId = Long.parseLong(roomId.substring(7));
            return getFriendMessages(friendshipId);
        } else if (roomId.startsWith("topic-")) {
            long topicId = Long.parseLong(roomId.substring(6));
            return getTopicMessages(topicId);
        } else {
            throw new IllegalArgumentException("Invalid room ID format: " + roomId);
        }
    }
    
    /**
     * Get recent messages for a group.
     * Room ID format: "group-{groupId}"
     * Checks if user has access to the group.
     */
    public List<ChatMessageDto> getGroupMessages(Long groupId, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return getGroupMessages(groupId, currentUser.getId());
    }
    
    /**
     * Get recent messages for a group.
     * Room ID format: "group-{groupId}"
     * Checks if user has access to the group.
     */
    public List<ChatMessageDto> getGroupMessages(Long groupId, Long userId) {
        // Check if user has access to this group (is a member)
        if (!groupService.canAccessGroup(groupId, userId)) {
            throw new IllegalStateException("You do not have access to this group");
        }
        
        // メッセージを取得
        List<ChatMessage> messages = chatMessageRepository.findTop50ByRoomIdOrderByCreatedAtDesc("group-" + groupId);
        messages = messages.stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .collect(Collectors.toList());
        
        // 匿名名を適用してDTOに変換
        return messages.stream()
                .map(message -> convertToGroupDto(message, userId, groupId))
                .collect(Collectors.toList());
    }
    
    /**
     * Get recent messages for a topic.
     * Room ID format: "topic-{topicId}"
     */
    public List<ChatMessageDto> getTopicMessages(Long topicId, String currentUsername) {
        // Topics are public, so no access check needed
        return getTopicMessages(topicId);
    }
    
    /**
     * Get recent messages for a topic.
     * Room ID format: "topic-{topicId}"
     */
    public List<ChatMessageDto> getTopicMessages(Long topicId) {
        return getRecentMessages("topic-" + topicId);
    }
    
    /**
     * Get recent messages for a friend chat.
     * Room ID format: "friend-{friendshipId}"
     */
    public List<ChatMessageDto> getFriendMessages(Long friendshipId, String currentUsername) {
        // フレンドシップの検証: currentUserがこのフレンドシップの一部であることを確認
        Friend friend = friendRepository.findById(friendshipId)
            .orElseThrow(() -> new RuntimeException("Friend relationship not found"));
        
        User currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!friend.getRequester().getId().equals(currentUser.getId()) && 
            !friend.getAddressee().getId().equals(currentUser.getId())) {
            throw new RuntimeException("User is not part of this friendship");
        }
        
        return getFriendMessages(friendshipId);
    }
    
    /**
     * Get recent messages for a friend chat.
     * Room ID format: "friend-{friendshipId}"
     */
    public List<ChatMessageDto> getFriendMessages(Long friendshipId) {
        return getRecentMessages("friend-" + friendshipId);
    }
    
    private ChatMessageDto convertToDto(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setSenderDisplayName(message.getSender().getDisplayName());
        dto.setRoomId(message.getRoomId());
        dto.setMessageType(message.getMessageType().toString());
        dto.setTimestamp(message.getCreatedAt().format(formatter));
        return dto;
    }
    
    /**
     * グループメッセージを匿名名付きDTOに変換
     */
    private ChatMessageDto convertToGroupDto(ChatMessage message, Long viewerId, Long groupId) {
        System.out.println("🔧 convertToGroupDto called - messageId: " + message.getId() + ", viewerId: " + viewerId + ", groupId: " + groupId);
        
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSenderUsername(message.getSender().getUsername());
        
        // 匿名名を取得（全員が同じ匿名名を見る2パラメータバージョン）
        // viewerIdが送信者自身の場合は「あなた」を返す
        Long senderId = message.getSender().getId();
        String anonymousName;
        if (viewerId.equals(senderId)) {
            anonymousName = "あなた";
            System.out.println("  → Self message, using: あなた");
        } else {
            anonymousName = anonymousNameService.getAnonymousName(senderId, groupId);
            System.out.println("  → Other's message, anonymous name: " + anonymousName + " for senderId: " + senderId);
        }
        dto.setSenderDisplayName(anonymousName);
        
        dto.setRoomId(message.getRoomId());
        dto.setMessageType(message.getMessageType().toString());
        dto.setTimestamp(message.getCreatedAt().format(formatter));
        
        System.out.println("  → DTO created with senderDisplayName: " + dto.getSenderDisplayName());
        return dto;
    }
}