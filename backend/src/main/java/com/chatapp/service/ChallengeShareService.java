package com.chatapp.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp.dto.ChallengeShareDtos.ChallengeShareResponse;
import com.chatapp.dto.ChallengeShareDtos.ChallengeSummary;
import com.chatapp.dto.ChallengeShareDtos.PagedShareResponse;
import com.chatapp.dto.ChallengeShareDtos.UserSummary;
import com.chatapp.model.ChallengeShare;
import com.chatapp.model.ChallengeShareReaction;
import com.chatapp.model.ChallengeShareReaction.ReactionType;
import com.chatapp.model.ChallengeShareReadStatus;
import com.chatapp.model.DailyChallenge;
import com.chatapp.model.User;
import com.chatapp.repository.ChallengeCompletionRepository;
import com.chatapp.repository.ChallengeShareReactionRepository;
import com.chatapp.repository.ChallengeShareReadStatusRepository;
import com.chatapp.repository.ChallengeShareRepository;
import com.chatapp.repository.DailyChallengeRepository;
import com.chatapp.repository.UserRepository;

@Service
public class ChallengeShareService {

    private final ChallengeShareRepository shareRepository;
    private final ChallengeShareReactionRepository reactionRepository;
    private final ChallengeShareReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final DailyChallengeRepository dailyChallengeRepository;
    private final ChallengeCompletionRepository completionRepository;

    public ChallengeShareService(ChallengeShareRepository shareRepository,
                                 ChallengeShareReactionRepository reactionRepository,
                                 ChallengeShareReadStatusRepository readStatusRepository,
                                 UserRepository userRepository,
                                 DailyChallengeRepository dailyChallengeRepository,
                                 ChallengeCompletionRepository completionRepository) {
        this.shareRepository = shareRepository;
        this.reactionRepository = reactionRepository;
        this.readStatusRepository = readStatusRepository;
        this.userRepository = userRepository;
        this.dailyChallengeRepository = dailyChallengeRepository;
        this.completionRepository = completionRepository;
    }

    @Transactional
    public ChallengeShareResponse createShare(Long userId, Long challengeId, String comment, String mood) {
        User user = getUser(userId);
        DailyChallenge challenge = dailyChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("チャレンジが見つかりません"));

        boolean completed = completionRepository.existsByUserAndChallenge(user, challenge);
        if (!completed) {
            throw new IllegalStateException("チャレンジ達成後にのみ共有できます");
        }

        ChallengeShare share = new ChallengeShare();
        share.setUser(user);
        share.setChallenge(challenge);
        share.setComment((comment == null || comment.isBlank()) ? "チャレンジを達成しました！" : comment.trim());
        share.setMood((mood != null && !mood.isBlank()) ? mood.trim() : null);
        share.setSharedAt(LocalDateTime.now());

        ChallengeShare saved = shareRepository.save(share);

        // 投稿者自身の既読状態は即時更新
        markTimelineRead(userId);

        return mapShare(saved, Collections.emptyMap(), Collections.emptyMap());
    }

    @Transactional(readOnly = true)
    public PagedShareResponse getTimeline(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sharedAt"));
        Page<ChallengeShare> sharePage = shareRepository.findAllByOrderBySharedAtDesc(pageable);

        List<ChallengeShare> shares = sharePage.getContent();
        List<Long> shareIds = shares.stream().map(ChallengeShare::getId).toList();

        Map<Long, Map<String, Long>> reactionCounts = buildReactionCountMap(shareIds);
        Map<Long, ReactionType> userReactionMap = buildUserReactionMap(userId, shareIds);

        List<ChallengeShareResponse> responses = shares.stream()
                .map(share -> mapShare(share, reactionCounts, userReactionMap))
                .toList();

        PagedShareResponse response = new PagedShareResponse();
        response.setShares(responses);
        response.setPage(sharePage.getNumber());
        response.setSize(sharePage.getSize());
        response.setTotalElements(sharePage.getTotalElements());
        response.setTotalPages(sharePage.getTotalPages());
        response.setHasNext(sharePage.hasNext());
        return response;
    }

    @Transactional
    public ChallengeShareResponse addOrUpdateReaction(Long userId, Long shareId, ReactionType type) {
        User user = getUser(userId);
        ChallengeShare share = shareRepository.findById(shareId)
                .orElseThrow(() -> new IllegalArgumentException("共有投稿が見つかりません"));

        Optional<ChallengeShareReaction> existing = reactionRepository.findByShareIdAndUserId(shareId, userId);
        if (existing.isPresent()) {
            ChallengeShareReaction reaction = existing.get();
            if (reaction.getType() == type) {
                reactionRepository.delete(reaction);
            } else {
                reaction.setType(type);
                reaction.setReactedAt(LocalDateTime.now());
                reactionRepository.save(reaction);
            }
        } else {
            ChallengeShareReaction reaction = new ChallengeShareReaction();
            reaction.setShare(share);
            reaction.setUser(user);
            reaction.setType(type);
            reaction.setReactedAt(LocalDateTime.now());
            reactionRepository.save(reaction);
        }

        return getShareDetail(userId, shareId);
    }

    @Transactional
    public ChallengeShareResponse removeReaction(Long userId, Long shareId, ReactionType type) {
        Optional<ChallengeShareReaction> existing = reactionRepository.findByShareIdAndUserId(shareId, userId);
        existing.ifPresent(reaction -> {
            if (reaction.getType() == type) {
                reactionRepository.delete(reaction);
            }
        });
        return getShareDetail(userId, shareId);
    }

    @Transactional(readOnly = true)
    public ChallengeShareResponse getShareDetail(Long userId, Long shareId) {
        ChallengeShare share = shareRepository.findById(shareId)
                .orElseThrow(() -> new IllegalArgumentException("共有投稿が見つかりません"));
        Map<Long, Map<String, Long>> reactionCounts = buildReactionCountMap(List.of(shareId));
        Map<Long, ReactionType> userReactionMap = buildUserReactionMap(userId, List.of(shareId));
        return mapShare(share, reactionCounts, userReactionMap);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        User user = getUser(userId);
        Optional<ChallengeShareReadStatus> statusOpt = readStatusRepository.findByUser(user);
        if (statusOpt.isEmpty() || statusOpt.get().getLastReadAt() == null) {
            return shareRepository.countByUserNot(user);
        }
        LocalDateTime lastRead = statusOpt.get().getLastReadAt();
        return shareRepository.countByUserNotAndSharedAtAfter(user, lastRead);
    }

    @Transactional
    public void markTimelineRead(Long userId) {
        User user = getUser(userId);
        ChallengeShareReadStatus status = readStatusRepository.findByUser(user)
                .orElse(null);
        
        if (status == null) {
            // 新規作成
            status = new ChallengeShareReadStatus();
            status.setUser(user);
            status.setLastReadAt(LocalDateTime.now());
            readStatusRepository.save(status);
        } else {
            // 既存レコードを更新
            status.setLastReadAt(LocalDateTime.now());
            readStatusRepository.save(status);
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));
    }

    private Map<Long, Map<String, Long>> buildReactionCountMap(List<Long> shareIds) {
        if (shareIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ChallengeShareReactionRepository.ReactionCountProjection> counts =
                reactionRepository.countByShareIds(shareIds);
        Map<Long, Map<String, Long>> result = new HashMap<>();
        for (ChallengeShareReactionRepository.ReactionCountProjection projection : counts) {
            Map<String, Long> inner = result.computeIfAbsent(projection.getShareId(), id -> new HashMap<>());
            inner.put(projection.getType().name(), projection.getCount());
        }
        return result;
    }

    private Map<Long, ReactionType> buildUserReactionMap(Long userId, List<Long> shareIds) {
        if (shareIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return reactionRepository.findByShareIdInAndUserId(shareIds, userId).stream()
                .collect(Collectors.toMap(reaction -> reaction.getShare().getId(), ChallengeShareReaction::getType));
    }

    private ChallengeShareResponse mapShare(ChallengeShare share,
                                            Map<Long, Map<String, Long>> reactionCounts,
                                            Map<Long, ReactionType> userReactionMap) {
        ChallengeShareResponse response = new ChallengeShareResponse();
        response.setId(share.getId());
        response.setComment(share.getComment());
        response.setMood(share.getMood());
        response.setSharedAt(share.getSharedAt());

        User user = share.getUser();
        UserSummary userSummary = new UserSummary();
        userSummary.setId(user.getId());
        userSummary.setUsername(user.getUsername());
        userSummary.setDisplayName(user.getDisplayName());
        response.setUser(userSummary);

        DailyChallenge challenge = share.getChallenge();
        ChallengeSummary challengeSummary = new ChallengeSummary();
        challengeSummary.setId(challenge.getId());
        challengeSummary.setTitle(challenge.getTitle());
        challengeSummary.setChallengeType(challenge.getChallengeType().name());
        challengeSummary.setPoints(challenge.getPoints());
        response.setChallenge(challengeSummary);

        Map<String, Long> counts = reactionCounts.getOrDefault(share.getId(), Collections.emptyMap());
        response.setReactions(counts);
        ReactionType userReaction = userReactionMap.get(share.getId());
        response.setUserReaction(userReaction != null ? userReaction.name() : null);
        return response;
    }
}
