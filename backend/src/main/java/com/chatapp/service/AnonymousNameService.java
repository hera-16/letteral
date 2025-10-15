package com.chatapp.service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp.model.Group;
import com.chatapp.model.GroupMemberAlias;
import com.chatapp.model.User;
import com.chatapp.repository.GroupMemberAliasRepository;
import com.chatapp.repository.GroupRepository;
import com.chatapp.repository.UserRepository;

@Service
public class AnonymousNameService {
    
    @Autowired
    private GroupMemberAliasRepository aliasRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    // 花の名前リスト（読み仮名付き）
    private static final String[] FLOWER_NAMES = {
        "桜(さくら)", "梅(うめ)", "桃(もも)", "藤(ふじ)", "菖蒲(しょうぶ)", "紫陽花(あじさい)", "百合(ゆり)", "朝顔(あさがお)",
        "向日葵(ひまわり)", "撫子(なでしこ)", "桔梗(ききょう)", "秋桜(コスモス)", "菊(きく)", "椿(つばき)", "水仙(すいせん)", "蓮(はす)",
        "牡丹(ぼたん)", "芍薬(しゃくやく)", "薔薇(ばら)", "カーネーション", "チューリップ", "スイートピー",
        "カスミソウ", "マーガレット", "ガーベラ", "ダリア", "ラベンダー", "スミレ(すみれ)",
        "パンジー", "コスモス", "彼岸花(ひがんばな)", "金木犀(きんもくせい)", "沈丁花(じんちょうげ)", "鈴蘭(すずらん)",
        "クレマチス", "ブーゲンビリア", "ハイビスカス", "蘭(らん)", "胡蝶蘭(こちょうらん)", "カトレア",
        "アネモネ", "フリージア", "ミモザ", "勿忘草(わすれなぐさ)", "鉄線花(てっせんか)", "白詰草(しろつめくさ)",
        "蒲公英(たんぽぽ)", "れんげ", "菜の花(なのはな)", "福寿草(ふくじゅそう)"
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
