package com.chatapp.service;

import com.chatapp.dto.ChallengeShareDtos.ChallengeShareResponse;
import com.chatapp.dto.ChallengeShareDtos.PagedShareResponse;
import com.chatapp.model.*;
import com.chatapp.model.ChallengeShareReaction.ReactionType;
import com.chatapp.repository.ChallengeCompletionRepository;
import com.chatapp.repository.DailyChallengeRepository;
import com.chatapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ChallengeShareServiceIntegrationTest {

    @Autowired
    private ChallengeShareService shareService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyChallengeRepository dailyChallengeRepository;

    @Autowired
    private ChallengeCompletionRepository completionRepository;

    private DailyChallenge sampleChallenge;

    @BeforeEach
    void setUp() {
        sampleChallenge = dailyChallengeRepository.findByIsActiveTrue()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No seeded daily challenges found"));
    }

    @Test
    void createShareAndRetrieveTimeline() {
        User user = createUser("share-user");
        recordCompletion(user, sampleChallenge);

        ChallengeShareResponse created = shareService.createShare(
                user.getId(),
                sampleChallenge.getId(),
                "今日は感謝を実践しました",
                "PROUD");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getMood()).isEqualTo("PROUD");
        assertThat(created.getReactions()).isEmpty();

        PagedShareResponse timeline = shareService.getTimeline(user.getId(), 0, 10);
        assertThat(timeline.getShares()).hasSizeGreaterThanOrEqualTo(1);
        ChallengeShareResponse response = timeline.getShares().stream()
                .filter(share -> share.getId().equals(created.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(response.getComment()).isEqualTo("今日は感謝を実践しました");
        assertThat(response.getChallenge().getId()).isEqualTo(sampleChallenge.getId());
    }

    @Test
    void addAndToggleReaction() {
        User owner = createUser("owner-user");
        recordCompletion(owner, sampleChallenge);
        ChallengeShareResponse created = shareService.createShare(
                owner.getId(), sampleChallenge.getId(), "テスト投稿", null);

        User reactor = createUser("reactor-user");
        shareService.markTimelineRead(reactor.getId()); // ensure read status exists

        ChallengeShareResponse reacted = shareService.addOrUpdateReaction(
                reactor.getId(), created.getId(), ReactionType.AWESOME);

        assertThat(reacted.getReactions()).containsEntry("AWESOME", 1L);
        assertThat(reacted.getUserReaction()).isEqualTo("AWESOME");

        ChallengeShareResponse toggled = shareService.addOrUpdateReaction(
                reactor.getId(), created.getId(), ReactionType.AWESOME);
        assertThat(toggled.getReactions()).doesNotContainKey("AWESOME");
        assertThat(toggled.getUserReaction()).isNull();
    }

    @Test
    void unreadCountReflectsReadStatus() {
        User owner = createUser("timeline-owner");
        recordCompletion(owner, sampleChallenge);
        shareService.createShare(owner.getId(), sampleChallenge.getId(), "共有テスト", null);

        User viewer = createUser("timeline-viewer");
        long unreadBefore = shareService.getUnreadCount(viewer.getId());
        assertThat(unreadBefore).isGreaterThanOrEqualTo(1);

        shareService.markTimelineRead(viewer.getId());
        long unreadAfter = shareService.getUnreadCount(viewer.getId());
        assertThat(unreadAfter).isZero();
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password");
        user.setDisplayName(username + " display");
        return userRepository.save(user);
    }

    private void recordCompletion(User user, DailyChallenge challenge) {
        ChallengeCompletion completion = new ChallengeCompletion();
        completion.setUser(user);
        completion.setChallenge(challenge);
        completion.setPointsEarned(challenge.getPoints());
        completionRepository.save(completion);
    }
}
