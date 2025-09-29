package com.chatapp.websocket;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.chatapp.dto.ChatMessageDto;
import com.chatapp.service.ChatService;

@Component
public class WebSocketEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    
    @Autowired
    private ChatService chatService;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null) {
            logger.debug("No session attributes found on disconnect event");
            return;
        }

        String username = (String) sessionAttributes.get("username");
        String roomId = (String) sessionAttributes.get("roomId");

        if (username != null && roomId != null) {
            logger.info("User Disconnected : " + username);
            
            ChatMessageDto chatMessage = new ChatMessageDto();
            chatMessage.setMessageType("LEAVE");
            chatMessage.setSenderUsername(username);
            chatMessage.setRoomId(roomId);
            chatMessage.setContent(username + " left the chat");
            
            ChatMessageDto savedMessage = chatService.saveMessage(chatMessage);
            messagingTemplate.convertAndSend("/topic/" + roomId, savedMessage);
        }
    }
}