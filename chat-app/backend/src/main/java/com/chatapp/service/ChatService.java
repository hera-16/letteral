package com.chatapp.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatapp.dto.ChatMessageDto;
import com.chatapp.model.ChatMessage;
import com.chatapp.model.Group;
import com.chatapp.model.User;
import com.chatapp.repository.ChatMessageRepository;
import com.chatapp.repository.GroupRepository;
import com.chatapp.repository.UserRepository;

@Service
public class ChatService {
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
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
        List<ChatMessage> messages = chatMessageRepository.findTop50ByRoomIdOrderByCreatedAtDesc(roomId);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 匿名投稿を保存
     */
    public ChatMessageDto saveAnonymousMessage(Long groupId, Long userId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        // ユーザーの匿名名を取得
        String anonymousName = groupService.getUserAnonymousName(groupId, userId);
        
        ChatMessage chatMessage = new ChatMessage(
                content,
                user,
                group,
                anonymousName,
                ChatMessage.MessageType.ANONYMOUS_POST
        );
        
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return convertToDto(savedMessage);
    }
    
    /**
     * グループのメッセージ履歴を取得
     */
    public List<ChatMessageDto> getGroupMessages(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        List<ChatMessage> messages = chatMessageRepository.findByGroupAndNotHidden(group);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ChatMessageDto convertToDto(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        
        // 匿名投稿の場合は匿名名を使用、そうでなければユーザー名を使用
        if (message.getAnonymousSenderName() != null && !message.getAnonymousSenderName().isEmpty()) {
            dto.setSenderUsername(message.getAnonymousSenderName());
            dto.setSenderDisplayName(message.getAnonymousSenderName());
        } else {
            dto.setSenderUsername(message.getSender().getUsername());
            dto.setSenderDisplayName(message.getSender().getDisplayName());
        }
        
        dto.setRoomId(message.getRoomId());
        dto.setMessageType(message.getMessageType().toString());
        dto.setTimestamp(message.getCreatedAt().format(formatter));
        return dto;
    }
}