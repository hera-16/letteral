package com.chatapp.service;

import com.chatapp.model.Group;
import com.chatapp.model.GroupMember;
import com.chatapp.model.User;
import com.chatapp.repository.GroupRepository;
import com.chatapp.repository.GroupMemberRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnonymousNameService anonymousNameService;

    /**
     * 招待専用グループを作成
     */
    public Group createInviteGroup(String groupName, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = new Group();
        group.setName(groupName);
        group.setGroupType(Group.GroupType.INVITE_ONLY);
        group.setMaxMembers(15);
        group.setInviteCode(generateInviteCode());
        group.setCreatedAt(LocalDateTime.now());

        Group savedGroup = groupRepository.save(group);

        // 作成者をグループメンバーとして追加
        joinGroup(savedGroup.getId(), creatorId);

        return savedGroup;
    }

    /**
     * 公開トピックグループを作成
     */
    public Group createPublicGroup(String groupName, String description, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = new Group();
        group.setName(groupName);
        group.setDescription(description);
        group.setGroupType(Group.GroupType.PUBLIC_TOPIC);
        group.setMaxMembers(null); // 無制限
        group.setCreatedAt(LocalDateTime.now());

        Group savedGroup = groupRepository.save(group);

        // 作成者をグループメンバーとして追加
        joinGroup(savedGroup.getId(), creatorId);

        return savedGroup;
    }

    /**
     * 招待コードでグループに参加
     */
    public GroupMember joinGroupByInviteCode(String inviteCode, Long userId) {
        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        return joinGroup(group.getId(), userId);
    }

    /**
     * グループに参加
     */
    public GroupMember joinGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 既に参加しているかチェック
        Optional<GroupMember> existingMember = groupMemberRepository.findByUserAndGroupAndIsActive(user, group, true);
        if (existingMember.isPresent()) {
            GroupMember member = existingMember.get();
            if (member.getBanUntil() != null && member.getBanUntil().isAfter(LocalDateTime.now())) {
                throw new RuntimeException("User is banned from this group");
            }
            return member;
        }

        // 招待専用グループの場合、人数制限をチェック
        if (group.getGroupType() == Group.GroupType.INVITE_ONLY) {
            long memberCount = groupMemberRepository.countActiveMembers(group);
            if (memberCount >= group.getMaxMembers()) {
                throw new RuntimeException("Group is full");
            }
        }

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setJoinedAt(LocalDateTime.now());

        // 匿名名を生成・設定
        String anonymousName = anonymousNameService.assignAnonymousNameToNewMember(group);
        member.setAnonymousName(anonymousName);
        member.setLastAnonymousNameUpdate(LocalDateTime.now());

        return groupMemberRepository.save(member);
    }

    /**
     * ユーザーが参加している全グループを取得
     */
    public List<Group> getUserGroups(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return groupRepository.findByUser(user);
    }

    /**
     * グループの詳細を取得
     */
    public Group getGroupDetails(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    /**
     * グループメンバー一覧を取得
     */
    public List<GroupMember> getGroupMembers(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return groupMemberRepository.findByGroupAndIsActive(group, true);
    }

    /**
     * グループから退出
     */
    public void leaveGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupMember member = groupMemberRepository.findByUserAndGroupAndIsActive(user, group, true)
                .orElseThrow(() -> new RuntimeException("User is not a member of this group"));

        member.setIsActive(false);
        groupMemberRepository.save(member);
    }

    /**
     * 公開グループを検索
     */
    public List<Group> searchPublicGroups(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return groupRepository.findPublicGroups();
        }
        return groupRepository.findPublicGroupsByKeyword(keyword);
    }

    /**
     * 招待コードを生成
     */
    private String generateInviteCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (groupRepository.findByInviteCode(code).isPresent());
        return code;
    }

    /**
     * ユーザーの特定グループでの匿名名を取得
     */
    public String getUserAnonymousName(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupMember member = groupMemberRepository.findByUserAndGroupAndIsActive(user, group, true)
                .orElseThrow(() -> new RuntimeException("User is not a member of this group"));

        return member.getAnonymousName();
    }
}