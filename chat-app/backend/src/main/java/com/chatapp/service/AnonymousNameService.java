package com.chatapp.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatapp.model.Group;
import com.chatapp.model.GroupMember;
import com.chatapp.repository.GroupMemberRepository;

@Service
public class AnonymousNameService {
    
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    
    private final Random random = new SecureRandom();
    
    /**
     * 招待制グループ用の匿名名を生成 (#0001 ~ #0015)
     */
    public String generateInviteGroupAnonymousName(Group group) {
        Set<String> usedNames = getUsedAnonymousNames(group);
        
        for (int i = 1; i <= 15; i++) {
            String name = String.format("#%04d", i);
            if (!usedNames.contains(name)) {
                return name;
            }
        }
        
        // 全部使われている場合はランダムに割り当て（同番号が連続する可能性あり）
        int randomNum = random.nextInt(15) + 1;
        return String.format("#%04d", randomNum);
    }
    
    /**
     * 一般グループ用の匿名名を生成 (#aZqTnBvG のような英大文字・小文字のランダム8桁ID)
     */
    public String generatePublicGroupAnonymousName(Group group) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Set<String> usedNames = getUsedAnonymousNames(group);
        
        String name;
        int attempts = 0;
        do {
            StringBuilder sb = new StringBuilder("#");
            for (int i = 0; i < 8; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }
            name = sb.toString();
            attempts++;
        } while (usedNames.contains(name) && attempts < 100); // 重複回避（最大100回試行）
        
        return name;
    }
    
    /**
     * グループ内で現在使用されている匿名名のセットを取得
     */
    private Set<String> getUsedAnonymousNames(Group group) {
        List<GroupMember> activeMembers = groupMemberRepository.findByGroupAndIsActive(group, true);
        Set<String> usedNames = new HashSet<>();
        
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        
        for (GroupMember member : activeMembers) {
            // 今日更新された匿名名のみを対象とする
            if (member.getLastAnonymousNameUpdate().isAfter(today)) {
                usedNames.add(member.getAnonymousName());
            }
        }
        
        return usedNames;
    }
    
    /**
     * 匿名名を再割り当て（毎日0:00に実行）
     */
    public void reassignAnonymousNames() {
        LocalDateTime cutoffTime = LocalDateTime.now().toLocalDate().atStartOfDay();
        List<GroupMember> membersToUpdate = groupMemberRepository.findMembersNeedingAnonymousNameUpdate(cutoffTime);
        
        for (GroupMember member : membersToUpdate) {
            String newAnonymousName;
            
            if (member.getGroup().getGroupType() == Group.GroupType.INVITE_ONLY) {
                newAnonymousName = generateInviteGroupAnonymousName(member.getGroup());
            } else {
                newAnonymousName = generatePublicGroupAnonymousName(member.getGroup());
            }
            
            member.setAnonymousName(newAnonymousName);
            member.setLastAnonymousNameUpdate(LocalDateTime.now());
            groupMemberRepository.save(member);
        }
    }
    
    /**
     * 新しいメンバーに匿名名を割り当て
     */
    public String assignAnonymousNameToNewMember(Group group) {
        if (group.getGroupType() == Group.GroupType.INVITE_ONLY) {
            return generateInviteGroupAnonymousName(group);
        } else {
            return generatePublicGroupAnonymousName(group);
        }
    }
    
    /**
     * 匿名名が今日割り当てられたものかチェック
     */
    public boolean isAnonymousNameValidForToday(GroupMember member) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        return member.getLastAnonymousNameUpdate().isAfter(startOfDay);
    }
    
    /**
     * グループ内で匿名名から実際のユーザーを特定（内部処理用）
     */
    public GroupMember findMemberByAnonymousName(Group group, String anonymousName) {
        return groupMemberRepository.findByGroupAndAnonymousNameAndIsActive(group, anonymousName, true)
                .orElse(null);
    }
}