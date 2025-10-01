package com.chatapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.model.Topic;
import com.chatapp.security.UserPrincipal;
import com.chatapp.service.TopicService;

/**
 * REST controller for public topic management.
 */
@RestController
@RequestMapping("/api/topics")
@CrossOrigin(origins = "*")
public class TopicController {

    private final TopicService topicService;

    public TopicController(final TopicService topicService) {
        this.topicService = topicService;
    }

    /**
     * Get all active topics.
     */
    @GetMapping
    public ResponseEntity<List<Topic>> getAllTopics() {
        final List<Topic> topics = topicService.getAllActiveTopics();
        return ResponseEntity.ok(topics);
    }

    /**
     * Create a new topic.
     */
    @PostMapping
    public ResponseEntity<Topic> createTopic(
            @RequestBody final CreateTopicRequest request,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final Topic topic = topicService.createTopic(
                request.getName(),
                request.getDescription(),
                request.getCategory(),
                userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(topic);
    }

    /**
     * Get topic details by ID.
     */
    @GetMapping("/{topicId}")
    public ResponseEntity<Topic> getTopicDetails(
            @PathVariable final Long topicId) {
        final Topic topic = topicService.getTopicById(topicId);
        return ResponseEntity.ok(topic);
    }

    /**
     * Get topics by category.
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Topic>> getTopicsByCategory(
            @PathVariable final String category) {
        final List<Topic> topics = topicService.getTopicsByCategory(category);
        return ResponseEntity.ok(topics);
    }

    /**
     * Get all available categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        final List<String> categories = topicService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Search topics by name.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Topic>> searchTopics(
            @RequestParam final String q) {
        final List<Topic> topics = topicService.searchTopics(q);
        return ResponseEntity.ok(topics);
    }

    /**
     * Update topic details.
     */
    @PutMapping("/{topicId}")
    public ResponseEntity<Topic> updateTopic(
            @PathVariable final Long topicId,
            @RequestBody final UpdateTopicRequest request,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final Topic topic = topicService.updateTopic(
                topicId,
                userId,
                request.getName(),
                request.getDescription(),
                request.getCategory());
        return ResponseEntity.ok(topic);
    }

    /**
     * Deactivate a topic.
     */
    @PutMapping("/{topicId}/deactivate")
    public ResponseEntity<Void> deactivateTopic(
            @PathVariable final Long topicId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        topicService.deactivateTopic(topicId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Reactivate a topic.
     */
    @PutMapping("/{topicId}/reactivate")
    public ResponseEntity<Void> reactivateTopic(
            @PathVariable final Long topicId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        topicService.reactivateTopic(topicId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a topic permanently.
     */
    @DeleteMapping("/{topicId}")
    public ResponseEntity<Void> deleteTopic(
            @PathVariable final Long topicId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        topicService.deleteTopic(topicId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all topics created by the current user.
     */
    @GetMapping("/my")
    public ResponseEntity<List<Topic>> getMyTopics(
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final List<Topic> topics = topicService.getUserCreatedTopics(userId);
        return ResponseEntity.ok(topics);
    }

    private Long resolveUserId(final Authentication authentication) {
        final Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            final UserPrincipal userPrincipal = (UserPrincipal) principal;
            return userPrincipal.getId();
        }
        throw new IllegalStateException("Unsupported principal: " + principal);
    }

    // DTOs

    public static final class CreateTopicRequest {
        private String name;
        private String description;
        private String category;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(final String category) {
            this.category = category;
        }
    }

    public static final class UpdateTopicRequest {
        private String name;
        private String description;
        private String category;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(final String category) {
            this.category = category;
        }
    }
}
