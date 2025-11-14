package com.chatapp.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.dto.ChatMessageDto;
import com.chatapp.security.Permission;
import com.chatapp.security.RequirePermission;
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

    /**
     * ルームのメッセージを取得
     * 全ロール：チャット閲覧可能
     */
    @GetMapping("/messages/{roomId}")
    @RequirePermission(Permission.CHAT_VIEW)
    public List<ChatMessageDto> getMessages(@PathVariable final String roomId) {
        return chatService.getRecentMessages(roomId);
    }

    /**
     * グループのメッセージを取得
     * 全ロール：チャット閲覧可能
     */
    @GetMapping("/groups/{groupId}/messages")
    @RequirePermission(Permission.CHAT_VIEW)
    public List<ChatMessageDto> getGroupMessages(
            @PathVariable final Long groupId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        return chatService.getGroupMessages(groupId, userId);
    }

    /**
     * トピックのメッセージを取得
     * 全ロール：チャット閲覧可能
     */
    @GetMapping("/topics/{topicId}/messages")
    @RequirePermission(Permission.CHAT_VIEW)
    public List<ChatMessageDto> getTopicMessages(
            @PathVariable final Long topicId) {
        return chatService.getTopicMessages(topicId);
    }

    private Long resolveUserId(final Authentication authentication) {
        final Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("Unsupported principal: " + principal);
    }
}
