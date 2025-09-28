package com.chatapp.controller;

import com.chatapp.dto.ChatMessageDto;
import com.chatapp.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    
    @GetMapping("/messages/{roomId}")
    public List<ChatMessageDto> getMessages(@PathVariable String roomId) {
        return chatService.getRecentMessages(roomId);
    }
}