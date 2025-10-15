package com.chatapp.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.dto.ChatMessageDto;
import com.chatapp.security.UserPrincipal;
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

    @GetMapping("/groups/{groupId}/messages")
    public List<ChatMessageDto> getGroupMessages(
            @PathVariable final Long groupId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        return chatService.getGroupMessages(groupId, userId);
    }

    @GetMapping("/topics/{topicId}/messages")
    public List<ChatMessageDto> getTopicMessages(
            @PathVariable final Long topicId) {
        return chatService.getTopicMessages(topicId);
    }

    @GetMapping("/friends/{friendshipId}/messages")
    public List<ChatMessageDto> getFriendMessages(
            @PathVariable final Long friendshipId) {
        return chatService.getFriendMessages(friendshipId);
    }

    private Long resolveUserId(final Authentication authentication) {
        final Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("Unsupported principal: " + principal);
    }
}
