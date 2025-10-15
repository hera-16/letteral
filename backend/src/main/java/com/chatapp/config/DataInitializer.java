package com.chatapp.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp.model.Badge;
import com.chatapp.model.DailyChallenge;
import com.chatapp.model.User;
import com.chatapp.repository.BadgeRepository;
import com.chatapp.repository.DailyChallengeRepository;
import com.chatapp.repository.UserRepository;

/**
 * アプリケーション起動時に初期データを投入するコンポーネント。
 * data.sql の実行が環境に依存してしまうケースがあるため、
 * JPA を通して確実にデータを整備する。
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final DailyChallengeRepository dailyChallengeRepository;
    private final BadgeRepository badgeRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

    public DataInitializer(DailyChallengeRepository dailyChallengeRepository,
                                                   BadgeRepository badgeRepository,
                                                   UserRepository userRepository,
                                                   PasswordEncoder passwordEncoder) {
        this.dailyChallengeRepository = dailyChallengeRepository;
        this.badgeRepository = badgeRepository;
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
                seedUsers();
        seedDailyChallenges();
        seedBadges();
    }

        private void seedUsers() {
                List<UserSeed> seeds = List.of(
                                new UserSeed("alice", "alice@example.com", "Alice Wonderland", "password123"),
                                new UserSeed("bob", "bob@example.com", "Bob Builder", "password123"),
                                new UserSeed("charlie", "charlie@example.com", "Charlie Chaplin", "password123"),
                                new UserSeed("diana", "diana@example.com", "Diana Prince", "password123"),
                                new UserSeed("eve", "eve@example.com", "Eve Online", "password123")
                );

                int created = 0;
                int updated = 0;

                for (UserSeed seed : seeds) {
                        User user = userRepository.findByUsername(seed.username()).orElse(null);

                        if (user == null) {
                                user = new User(seed.username(),
                                                seed.email(),
                                                passwordEncoder.encode(seed.rawPassword()),
                                                seed.displayName());
                                userRepository.save(user);
                                created++;
                                continue;
                        }

                        boolean changed = false;

                        if (!seed.email().equalsIgnoreCase(user.getEmail())) {
                                user.setEmail(seed.email());
                                changed = true;
                        }

                        if (user.getDisplayName() == null || !user.getDisplayName().equals(seed.displayName())) {
                                user.setDisplayName(seed.displayName());
                                changed = true;
                        }

                        if (!passwordEncoder.matches(seed.rawPassword(), user.getPassword())) {
                                user.setPassword(passwordEncoder.encode(seed.rawPassword()));
                                changed = true;
                        }

                        if (changed) {
                                userRepository.save(user);
                                updated++;
                        }
                }

                if (created > 0 || updated > 0) {
                        LOGGER.info("Seeded users - created: {}, updated: {}", created, updated);
                } else {
                        LOGGER.info("Seeded users - no changes required");
                }
        }

    private void seedDailyChallenges() {
        List<ChallengeSeed> seeds = List.of(
                new ChallengeSeed(
                        "今日の良かったこと3つ",
                        "今日あった良いことを3つ書き出してみましょう。小さなことでもOK！",
                        10,
                        DailyChallenge.ChallengeType.GRATITUDE,
                        DailyChallenge.DifficultyLevel.EASY
                ),
                new ChallengeSeed(
                        "感謝の気持ちを伝える",
                        "誰か1人に『ありがとう』と伝えてみましょう",
                        15,
                        DailyChallenge.ChallengeType.GRATITUDE,
                        DailyChallenge.DifficultyLevel.MEDIUM
                ),
                new ChallengeSeed(
                        "今週のベストモーメント",
                        "今週で一番良かった瞬間を振り返ってみましょう",
                        15,
                        DailyChallenge.ChallengeType.GRATITUDE,
                        DailyChallenge.DifficultyLevel.MEDIUM
                ),
                new ChallengeSeed(
                        "誰かを褒める",
                        "グループで誰かを褒めるメッセージを送ってみましょう",
                        15,
                        DailyChallenge.ChallengeType.KINDNESS,
                        DailyChallenge.DifficultyLevel.EASY
                ),
                new ChallengeSeed(
                        "励ましメッセージを送る",
                        "頑張っている人に励ましの言葉をかけてみましょう",
                        15,
                        DailyChallenge.ChallengeType.KINDNESS,
                        DailyChallenge.DifficultyLevel.MEDIUM
                ),
                new ChallengeSeed(
                        "共感コメント",
                        "誰かの投稿に共感するコメントを残しましょう",
                        10,
                        DailyChallenge.ChallengeType.KINDNESS,
                        DailyChallenge.DifficultyLevel.EASY
                ),
                new ChallengeSeed(
                        "深呼吸を5回",
                        "ゆっくり深呼吸を5回して、リラックスしましょう",
                        10,
                        DailyChallenge.ChallengeType.SELF_CARE,
                        DailyChallenge.DifficultyLevel.EASY
                ),
                new ChallengeSeed(
                        "好きな音楽を聴く",
                        "好きな曲を1曲聴いて気分転換しましょう",
                        10,
                        DailyChallenge.ChallengeType.SELF_CARE,
                        DailyChallenge.DifficultyLevel.EASY
                ),
                new ChallengeSeed(
                        "5分間休憩",
                        "スマホを置いて、5分間目を閉じて休憩しましょう",
                        10,
                        DailyChallenge.ChallengeType.SELF_CARE,
                        DailyChallenge.DifficultyLevel.EASY
                ),
                new ChallengeSeed(
                        "自分を褒める",
                        "今日頑張った自分を1つ褒めてあげましょう",
                        15,
                        DailyChallenge.ChallengeType.SELF_CARE,
                        DailyChallenge.DifficultyLevel.MEDIUM
                ),
                new ChallengeSeed(
                        "3行日記",
                        "今日の出来事を3行で書いてみましょう",
                        10,
                        DailyChallenge.ChallengeType.CREATIVITY,
                        DailyChallenge.DifficultyLevel.EASY
                ),
                new ChallengeSeed(
                        "好きなものを描く",
                        "簡単なイラストや落書きをしてみましょう",
                        15,
                        DailyChallenge.ChallengeType.CREATIVITY,
                        DailyChallenge.DifficultyLevel.MEDIUM
                ),
                new ChallengeSeed(
                        "今日の空の写真",
                        "空を見上げて、写真を撮ってみましょう",
                        10,
                        DailyChallenge.ChallengeType.CREATIVITY,
                        DailyChallenge.DifficultyLevel.EASY
                ),
                new ChallengeSeed(
                        "グループで挨拶",
                        "グループチャットで『おはよう』や『おやすみ』を送ってみましょう",
                        10,
                        DailyChallenge.ChallengeType.CONNECTION,
                        DailyChallenge.DifficultyLevel.EASY
                ),
                new ChallengeSeed(
                        "質問を投げかける",
                        "グループで質問を1つ投げかけて、会話のきっかけを作りましょう",
                        15,
                        DailyChallenge.ChallengeType.CONNECTION,
                        DailyChallenge.DifficultyLevel.MEDIUM
                ),
                new ChallengeSeed(
                        "誰かの話を聞く",
                        "グループで誰かの話にしっかり耳を傾けてみましょう",
                        15,
                        DailyChallenge.ChallengeType.CONNECTION,
                        DailyChallenge.DifficultyLevel.MEDIUM
                )
        );

        Set<String> activeTitles = new HashSet<>();
        for (ChallengeSeed seed : seeds) {
            DailyChallenge challenge = dailyChallengeRepository.findByTitle(seed.title())
                    .orElseGet(DailyChallenge::new);

            challenge.setTitle(seed.title());
            challenge.setDescription(seed.description());
            challenge.setPoints(seed.points());
            challenge.setChallengeType(seed.type());
            challenge.setDifficultyLevel(seed.difficulty());
            challenge.setIsActive(true);

            dailyChallengeRepository.save(challenge);
            activeTitles.add(seed.title());
        }

        // 定義から外れた既存チャレンジは非アクティブ化する
        dailyChallengeRepository.findByIsActiveTrue().stream()
                .filter(existing -> !activeTitles.contains(existing.getTitle()))
                .forEach(existing -> {
                    existing.setIsActive(false);
                    dailyChallengeRepository.save(existing);
                });

        LOGGER.info("Seeded {} daily challenges", seeds.size());
    }

    private void seedBadges() {
        List<BadgeSeed> seeds = List.of(
                new BadgeSeed("初めての一歩", "最初のチャレンジを達成しました！すごい！", "FIRST_STEP", "🌱", 1),
                new BadgeSeed("3日連続", "3日連続でチャレンジ達成！継続は力なり！", "STREAK_3", "🔥", 3),
                new BadgeSeed("1週間連続", "7日連続達成！素晴らしい習慣です！", "STREAK_7", "⭐", 7),
                new BadgeSeed("10回達成", "合計10回のチャレンジ達成！成長していますね！", "TOTAL_10", "🎯", 10),
                new BadgeSeed("30回達成", "合計30回達成！もう習慣になっていますね！", "TOTAL_30", "🏆", 30),
                new BadgeSeed("50回達成", "合計50回達成！驚異的な継続力です！", "TOTAL_50", "👑", 50),
                new BadgeSeed("感謝マスター", "感謝系チャレンジを10回達成！感謝の心が育っています！", "GRATITUDE_10", "💝", 10),
                new BadgeSeed("優しさの達人", "優しさ系チャレンジを10回達成！あなたの優しさが世界を変えます！", "KINDNESS_10", "🤝", 10),
                new BadgeSeed("セルフケア上手", "セルフケア系チャレンジを10回達成！自分を大切にしていますね！", "SELF_CARE_10", "🧘", 10),
                new BadgeSeed("クリエイター", "創造性チャレンジを10回達成！あなたの創造力が光っています！", "CREATIVITY_10", "🎨", 10),
                new BadgeSeed("コミュニケーター", "つながり系チャレンジを10回達成！素敵な関係を築いていますね！", "CONNECTION_10", "🌈", 10),
                new BadgeSeed("花レベル3", "花レベル3到達！着実に成長していますね！", "LEVEL_3", "🌺", 3),
                new BadgeSeed("花レベル5", "花レベル5到達！半分まで来ました！", "LEVEL_5", "🌻", 5),
                new BadgeSeed("花レベル7", "花レベル7到達！もうすぐ最高レベルです！", "LEVEL_7", "🌹", 7),
                new BadgeSeed("花レベル10", "花レベル10到達！最高の花が咲きました！", "LEVEL_10", "🏵️", 10)
        );

        for (BadgeSeed seed : seeds) {
            Badge badge = badgeRepository.findByBadgeType(seed.badgeType())
                    .orElseGet(Badge::new);

            badge.setBadgeType(seed.badgeType());
            badge.setName(seed.name());
            badge.setDescription(seed.description());
            badge.setIcon(seed.icon());
            badge.setRequirementValue(seed.requirementValue());

            badgeRepository.save(badge);
        }

        LOGGER.info("Seeded {} badges", seeds.size());
    }

    private record ChallengeSeed(String title,
                                 String description,
                                 int points,
                                 DailyChallenge.ChallengeType type,
                                 DailyChallenge.DifficultyLevel difficulty) { }

    private record BadgeSeed(String name,
                              String description,
                              String badgeType,
                              String icon,
                              int requirementValue) { }

    private record UserSeed(String username,
                            String email,
                            String displayName,
                            String rawPassword) { }
}
