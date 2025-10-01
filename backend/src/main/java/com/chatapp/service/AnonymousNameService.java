package com.chatapp.service;

import com.chatapp.model.Group;
import com.chatapp.model.GroupMemberAlias;
import com.chatapp.model.User;
import com.chatapp.repository.GroupMemberAliasRepository;
import com.chatapp.repository.GroupRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnonymousNameService {
    
    @Autowired
    private GroupMemberAliasRepository aliasRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    // 花の名前リスト
    private static final String[] FLOWER_NAMES = {
        "桜", "梅", "桃", "藤", "菖蒲", "紫陽花", "百合", "朝顔",
        "向日葵", "撫子", "桔梗", "秋桜", "菊", "椿", "水仙", "蓮",
        "牡丹", "芍薬", "薔薇", "カーネーション", "チューリップ", "スイートピー",
        "カスミソウ", "マーガレット", "ガーベラ", "ダリア", "ラベンダー", "スミレ",
        "パンジー", "コスモス", "彼岸花", "金木犀", "沈丁花", "鈴蘭",
        "クレマチス", "ブーゲンビリア", "ハイビスカス", "蘭", "胡蝶蘭", "カトレア",
        "アネモネ", "フリージア", "ミモザ", "勿忘草", "鉄線花", "白詰草",
        "蒲公英", "れんげ", "菜の花", "福寿草"
    };
    
    private final SecureRandom random = new SecureRandom();
    
    /**
     * グループ内で対象ユーザーの匿名名を取得（viewerIdなし版）
     * 全員が同じ匿名名を見るため、viewerIdは不要
     */
    @Transactional
    public String getAnonymousName(Long targetUserId, Long groupId) {
        System.out.println("🌸 getAnonymousName called - targetUserId: " + targetUserId + ", groupId: " + groupId);
        
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        // 既存の匿名名を検索
        GroupMemberAlias alias = aliasRepository.findByTargetUserAndGroup(targetUser, group)
                .orElseGet(() -> {
                    // 新しい匿名名を生成して保存
                    String anonymousName = generateAnonymousName();
                    System.out.println("🌸 Creating new anonymous name: " + anonymousName + " for targetUserId: " + targetUserId + " in groupId: " + groupId);
                    GroupMemberAlias newAlias = new GroupMemberAlias(targetUser, group, anonymousName);
                    return aliasRepository.save(newAlias);
                });
        
        // ローテーションが必要かチェック
        if (needsRotation(alias)) {
            System.out.println("🌸 Rotation needed for alias ID: " + alias.getId());
            rotateAnonymousName(alias);
        }
        
        String result = alias.getAnonymousName();
        System.out.println("🌸 Returning anonymous name: " + result);
        return result;
    }
    
    /**
     * グループ内で対象ユーザーの匿名名を取得
     * 存在しない場合は新規作成
     * ローテーション日時をチェックして、期限切れの場合は更新
     * 全員が同じ匿名名を見る
     */
    @Transactional
    public String getAnonymousName(Long viewerId, Long targetUserId, Long groupId) {
        System.out.println("🌸 getAnonymousName called - viewerId: " + viewerId + ", targetUserId: " + targetUserId + ", groupId: " + groupId);
        
        // 自分自身を見る場合は「あなた」を返す
        if (viewerId.equals(targetUserId)) {
            System.out.println("🌸 Returning 'あなた' (self)");
            return "あなた";
        }
        
        // viewerIdに依存しない匿名名を取得
        return getAnonymousName(targetUserId, groupId);
    }
    
    /**
     * グループ内のすべてのユーザーの匿名名マップを取得
     * viewerIdは自分自身の判定にのみ使用
     */
    @Transactional
    public Map<Long, String> getAnonymousNameMap(Long viewerId, Long groupId) {
        List<GroupMemberAlias> aliases = aliasRepository.findByGroupId(groupId);
        Map<Long, String> nameMap = new HashMap<>();
        
        for (GroupMemberAlias alias : aliases) {
            // ローテーションが必要かチェック
            if (needsRotation(alias)) {
                rotateAnonymousName(alias);
            }
            nameMap.put(alias.getTargetUser().getId(), alias.getAnonymousName());
        }
        
        // 自分自身
        nameMap.put(viewerId, "あなた");
        
        return nameMap;
    }
    
    /**
     * ランダムな花の名前を生成
     */
    private String generateAnonymousName() {
        String flowerName = FLOWER_NAMES[random.nextInt(FLOWER_NAMES.length)];
        System.out.println("🌸 Generated flower name: " + flowerName);
        return flowerName;
    }
    
    /**
     * 匿名名のローテーションが必要かチェック
     * 最終ローテーション日が今日より前の場合、ローテーションが必要
     */
    private boolean needsRotation(GroupMemberAlias alias) {
        if (alias.getLastRotationDate() == null) {
            return true;
        }
        LocalDate today = LocalDate.now();
        return alias.getLastRotationDate().isBefore(today);
    }
    
    /**
     * 匿名名をローテーション(更新)
     */
    @Transactional
    public void rotateAnonymousName(GroupMemberAlias alias) {
        String newName = generateAnonymousName();
        alias.setAnonymousName(newName);
        alias.setLastRotationDate(LocalDate.now());
        aliasRepository.save(alias);
    }
    
    /**
     * 特定のグループの匿名名をすべてリセット
     */
    @Transactional
    public void resetGroupAliases(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        List<GroupMemberAlias> aliases = aliasRepository.findByGroup(group);
        aliasRepository.deleteAll(aliases);
    }
}
