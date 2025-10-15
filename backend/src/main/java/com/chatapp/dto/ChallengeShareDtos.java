package com.chatapp.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 共有タイムライン関連のDTO
 */
public final class ChallengeShareDtos {

    private ChallengeShareDtos() {
    }

    public static class CreateShareRequest {
        private Long challengeId;
        private String comment;
        private String mood;

        public Long getChallengeId() {
            return challengeId;
        }

        public void setChallengeId(Long challengeId) {
            this.challengeId = challengeId;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getMood() {
            return mood;
        }

        public void setMood(String mood) {
            this.mood = mood;
        }
    }

    public static class ReactionRequest {
        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class ChallengeShareResponse {
        private Long id;
        private UserSummary user;
        private ChallengeSummary challenge;
        private String comment;
        private String mood;
        private LocalDateTime sharedAt;
        private Map<String, Long> reactions;
        private String userReaction;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public UserSummary getUser() {
            return user;
        }

        public void setUser(UserSummary user) {
            this.user = user;
        }

        public ChallengeSummary getChallenge() {
            return challenge;
        }

        public void setChallenge(ChallengeSummary challenge) {
            this.challenge = challenge;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getMood() {
            return mood;
        }

        public void setMood(String mood) {
            this.mood = mood;
        }

        public LocalDateTime getSharedAt() {
            return sharedAt;
        }

        public void setSharedAt(LocalDateTime sharedAt) {
            this.sharedAt = sharedAt;
        }

        public Map<String, Long> getReactions() {
            return reactions;
        }

        public void setReactions(Map<String, Long> reactions) {
            this.reactions = reactions;
        }

        public String getUserReaction() {
            return userReaction;
        }

        public void setUserReaction(String userReaction) {
            this.userReaction = userReaction;
        }
    }

    public static class UserSummary {
        private Long id;
        private String username;
        private String displayName;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }

    public static class ChallengeSummary {
        private Long id;
        private String title;
        private String challengeType;
        private Integer points;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getChallengeType() {
            return challengeType;
        }

        public void setChallengeType(String challengeType) {
            this.challengeType = challengeType;
        }

        public Integer getPoints() {
            return points;
        }

        public void setPoints(Integer points) {
            this.points = points;
        }
    }

    public static class PagedShareResponse {
        private List<ChallengeShareResponse> shares;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;

        public List<ChallengeShareResponse> getShares() {
            return shares;
        }

        public void setShares(List<ChallengeShareResponse> shares) {
            this.shares = shares;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isHasNext() {
            return hasNext;
        }

        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }
    }
}
