package com.chatapp.service;

import com.chatapp.dto.ChatMessageDto;
import com.chatapp.model.ChatMessage;
import com.chatapp.model.User;
import com.chatapp.repository.ChatMessageRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GroupService groupService;
    
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
        return getRecentMessages("group-" + groupId);
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
        // TODO: Validate that currentUser is part of this friendship
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
}