package com.chatapp.controller;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final CopyOnWriteArrayList<Message> messages = new CopyOnWriteArrayList<>();

    @GetMapping("/messages")
    public List<Message> listMessages() {
        return messages;
    }

    @PostMapping("/messages")
    public ResponseEntity<Message> postMessage(@RequestBody MessageRequest request) {
        if (!StringUtils.hasText(request.sender()) || !StringUtils.hasText(request.content())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Message message = new Message(request.sender(), request.content(), Instant.now());
        messages.add(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    public record Message(String sender, String content, Instant timestamp) {}

    public record MessageRequest(String sender, String content) {}
}

