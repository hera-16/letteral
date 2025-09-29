package com.chatapp.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.dto.ChatMessageDto;
import com.chatapp.service.ChatService;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(final ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/messages/{roomId}")
    public List<ChatMessageDto> getMessages(@PathVariable final String roomId) {
        return chatService.getRecentMessages(roomId);
    }
}
