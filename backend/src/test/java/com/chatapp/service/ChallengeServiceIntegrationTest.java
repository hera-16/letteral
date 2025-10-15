package com.chatapp.service;

import com.chatapp.model.DailyChallenge;
import com.chatapp.model.User;
import com.chatapp.model.UserBadge;
import com.chatapp.repository.DailyChallengeRepository;
import com.chatapp.repository.UserBadgeRepository;
import com.chatapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ChallengeServiceIntegrationTest {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyChallengeRepository dailyChallengeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Test
    void completingFirstChallengeAwardsFirstStepBadge() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setDisplayName("Test User");
        user = userRepository.save(user);

        DailyChallenge challenge = dailyChallengeRepository.findByIsActiveTrue()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No seeded daily challenges found"));

        Map<String, Object> result = challengeService.completeChallenge(user.getId(), challenge.getId(), "Great job!");

        assertThat(result).containsKeys("completion", "newBadges");
        @SuppressWarnings("unchecked")
        List<UserBadge> newBadges = (List<UserBadge>) result.get("newBadges");
        assertThat(newBadges)
                .isNotNull()
                .isNotEmpty()
                .extracting(badge -> badge.getBadge().getBadgeType())
                .contains("FIRST_STEP");

        boolean badgePersisted = userBadgeRepository.existsByUserAndBadge_BadgeType(user, "FIRST_STEP");
        assertThat(badgePersisted).isTrue();
    }

    @Test
    void cannotCompleteMoreThanThreeChallengesPerDay() {
    User user = new User();
    user.setUsername("limituser");
    user.setEmail("limit@example.com");
    user.setPassword("password");
    user.setDisplayName("Limit User");
    User savedUser = userRepository.save(user);

        List<DailyChallenge> activeChallenges = dailyChallengeRepository.findByIsActiveTrue();
        assertThat(activeChallenges).hasSizeGreaterThanOrEqualTo(4);

        for (int i = 0; i < 3; i++) {
            DailyChallenge challenge = activeChallenges.get(i);
            challengeService.completeChallenge(savedUser.getId(), challenge.getId(), "note" + i);
        }

        DailyChallenge fourthChallenge = activeChallenges.get(3);

        assertThatThrownBy(() ->
                challengeService.completeChallenge(savedUser.getId(), fourthChallenge.getId(), null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("3つまで");

        assertThat(challengeService.getTodayRecommendedChallenges(savedUser.getId())).isEmpty();
        assertThat(challengeService.getTodayCompletedCount(savedUser.getId())).isEqualTo(3);
    }
}
