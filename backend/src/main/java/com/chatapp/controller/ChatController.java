package com.chatapp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.chatapp.dto.ChatMessageDto;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.service.AnonymousNameService;
import com.chatapp.service.ChatService;

@Controller
@RequestMapping("/chat")
public class ChatController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnonymousNameService anonymousNameService;
    
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessage) {
        // メッセージをデータベースに保存
        ChatMessageDto savedMessage = chatService.saveMessage(chatMessage);
        
        // グループチャットの場合は匿名名を適用
        if (chatMessage.getRoomId().startsWith("group-")) {
            try {
                String groupIdStr = chatMessage.getRoomId().substring(6);
                Long groupId = Long.valueOf(groupIdStr);
                User sender = userRepository.findByUsername(chatMessage.getSenderUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                // 匿名名を取得（全員が同じ匿名名を見る2パラメータバージョン）
                String anonymousName = anonymousNameService.getAnonymousName(
                        sender.getId(),  // targetUserId（送信者のID）
                        groupId          // groupId
                );
                
                savedMessage.setSenderDisplayName(anonymousName);
                System.out.println("✅ Applied anonymous name: " + anonymousName + " for user: " + sender.getUsername() + " (ID: " + sender.getId() + ") in group: " + groupId);
            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid group ID format: " + e.getMessage());
            } catch (RuntimeException e) {
                System.err.println("❌ Failed to apply anonymous name: " + e.getMessage());
                e.printStackTrace();
                // エラーが発生してもメッセージは送信する
            }
        }
        
        // 指定されたルームの全ユーザーにメッセージを送信
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), savedMessage);
    }
    
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessageDto chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // WebSocketセッションにユーザー名を追加
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return;
        }
        sessionAttributes.put("username", chatMessage.getSenderUsername());

        // JOIN メッセージを送信
        chatMessage.setMessageType("JOIN");
        ChatMessageDto savedMessage = chatService.saveMessage(chatMessage);
        
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), savedMessage);
    }
    
    // REST API endpoints for retrieving messages
    
    @GetMapping("/messages/{roomId}")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<List<ChatMessageDto>> getMessagesByRoomId(
            @PathVariable String roomId,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        List<ChatMessageDto> messages = chatService.getMessagesByRoomId(roomId, currentUsername);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/friends/{friendshipId}/messages")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<List<ChatMessageDto>> getFriendMessages(
            @PathVariable Long friendshipId,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        List<ChatMessageDto> messages = chatService.getFriendMessages(friendshipId, currentUsername);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/groups/{groupId}/messages")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<List<ChatMessageDto>> getGroupMessages(
            @PathVariable Long groupId,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        List<ChatMessageDto> messages = chatService.getGroupMessages(groupId, currentUsername);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/topics/{topicId}/messages")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<List<ChatMessageDto>> getTopicMessages(
            @PathVariable Long topicId,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        List<ChatMessageDto> messages = chatService.getTopicMessages(topicId, currentUsername);
        return ResponseEntity.ok(messages);
    }
}
