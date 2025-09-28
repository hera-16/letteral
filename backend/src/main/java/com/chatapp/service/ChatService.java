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