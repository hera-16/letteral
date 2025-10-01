package com.chatapp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp.model.Topic;
import com.chatapp.model.User;
import com.chatapp.repository.TopicRepository;
import com.chatapp.repository.UserRepository;

/**
 * Service for managing public topics.
 */
@Service
@Transactional
public class TopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public TopicService(final TopicRepository topicRepository,
            final UserRepository userRepository) {
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new topic.
     *
     * @param name        topic name
     * @param description topic description
     * @param category    topic category
     * @param creatorId   ID of the creator
     * @return created topic
     */
    public Topic createTopic(final String name, final String description,
            final String category, final Long creatorId) {
        final User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final Topic topic = new Topic(name, description, category, creator);
        return topicRepository.save(topic);
    }

    /**
     * Get all active topics.
     *
     * @return list of active topics
     */
    public List<Topic> getAllActiveTopics() {
        return topicRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    /**
     * Get topics by category.
     *
     * @param category category name
     * @return list of topics
     */
    public List<Topic> getTopicsByCategory(final String category) {
        return topicRepository
                .findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(category);
    }

    /**
     * Get all available categories.
     *
     * @return list of category names
     */
    public List<String> getAllCategories() {
        return topicRepository.findAllCategories();
    }

    /**
     * Search topics by name.
     *
     * @param searchTerm search term
     * @return list of matching topics
     */
    public List<Topic> searchTopics(final String searchTerm) {
        return topicRepository.searchByName(searchTerm);
    }

    /**
     * Get topic details by ID.
     *
     * @param topicId topic ID
     * @return topic details
     */
    public Topic getTopicById(final Long topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Topic not found"));
    }

    /**
     * Update topic details.
     *
     * @param topicId     topic ID
     * @param userId      user ID (must be creator)
     * @param name        new name
     * @param description new description
     * @param category    new category
     * @return updated topic
     */
    public Topic updateTopic(final Long topicId, final Long userId,
            final String name, final String description, final String category) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Topic not found"));

        // Only creator can update
        if (!topic.getCreator().getId().equals(userId)) {
            throw new IllegalStateException(
                    "Only the creator can update this topic");
        }

        if (name != null && !name.trim().isEmpty()) {
            topic.setName(name);
        }
        if (description != null) {
            topic.setDescription(description);
        }
        if (category != null) {
            topic.setCategory(category);
        }

        return topicRepository.save(topic);
    }

    /**
     * Deactivate a topic.
     *
     * @param topicId topic ID
     * @param userId  user ID (must be creator)
     */
    public void deactivateTopic(final Long topicId, final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Topic not found"));

        // Only creator can deactivate
        if (!topic.getCreator().getId().equals(userId)) {
            throw new IllegalStateException(
                    "Only the creator can deactivate this topic");
        }

        topic.setIsActive(false);
        topicRepository.save(topic);
    }

    /**
     * Reactivate a topic.
     *
     * @param topicId topic ID
     * @param userId  user ID (must be creator)
     */
    public void reactivateTopic(final Long topicId, final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Topic not found"));

        // Only creator can reactivate
        if (!topic.getCreator().getId().equals(userId)) {
            throw new IllegalStateException(
                    "Only the creator can reactivate this topic");
        }

        topic.setIsActive(true);
        topicRepository.save(topic);
    }

    /**
     * Delete a topic permanently.
     *
     * @param topicId topic ID
     * @param userId  user ID (must be creator)
     */
    public void deleteTopic(final Long topicId, final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Topic not found"));

        // Only creator can delete
        if (!topic.getCreator().getId().equals(userId)) {
            throw new IllegalStateException(
                    "Only the creator can delete this topic");
        }

        topicRepository.delete(topic);
    }

    /**
     * Get all topics created by a user.
     *
     * @param userId user ID
     * @return list of topics
     */
    public List<Topic> getUserCreatedTopics(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        return topicRepository.findByCreatorOrderByCreatedAtDesc(user);
    }
}
