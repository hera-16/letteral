package com.chatapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.dto.AnonymousPostRequest;
import com.chatapp.dto.ChatMessageDto;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.service.ChatService;
import com.chatapp.service.GroupService;

@Controller
public class ChatController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ChatService chatService;
    
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessage) {
        // メッセージをデータベースに保存
        ChatMessageDto savedMessage = chatService.saveMessage(chatMessage);
        
        // 指定されたルームの全ユーザーにメッセージを送信
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), savedMessage);
    }
    
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessageDto chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // WebSocketセッションにユーザー名を追加
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSenderUsername());
        
        // JOIN メッセージを送信
        chatMessage.setMessageType("JOIN");
        ChatMessageDto savedMessage = chatService.saveMessage(chatMessage);
        
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), savedMessage);
    }
}

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
class ChatRestController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private GroupService groupService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @GetMapping("/messages/{roomId}")
    public List<ChatMessageDto> getMessages(@PathVariable String roomId) {
        return chatService.getRecentMessages(roomId);
    }
    
    /**
     * グループに匿名投稿
     */
    @PostMapping("/groups/{groupId}/anonymous")
    public ResponseEntity<ChatMessageDto> postAnonymousMessage(
            @PathVariable Long groupId,
            @RequestBody AnonymousPostRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        // TODO: ユーザー名からユーザーIDを取得する実装が必要
        Long userId = getUserIdFromUsername(username);
        
        ChatMessageDto message = chatService.saveAnonymousMessage(groupId, userId, request.getContent());
        
        // WebSocketで即座に配信
        messagingTemplate.convertAndSend("/topic/group_" + groupId, message);
        
        return ResponseEntity.ok(message);
    }
    
    /**
     * グループのメッセージ履歴を取得
     */
    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getGroupMessages(
            @PathVariable Long groupId,
            Authentication authentication) {
        
        String username = authentication.getName();
        Long userId = getUserIdFromUsername(username);
        
        // ユーザーがグループのメンバーかチェック
        try {
            groupService.getUserAnonymousName(groupId, userId);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).build(); // アクセス拒否
        }
        
        List<ChatMessageDto> messages = chatService.getGroupMessages(groupId);
        return ResponseEntity.ok(messages);
    }
    
    @Autowired
    private UserRepository userRepository;
    
    private Long getUserIdFromUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return user.getId();
    }
}