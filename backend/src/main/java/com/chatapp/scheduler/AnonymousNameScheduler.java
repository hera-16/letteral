package com.chatapp.scheduler;

import com.chatapp.model.GroupMemberAlias;
import com.chatapp.repository.GroupMemberAliasRepository;
import com.chatapp.service.AnonymousNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 匿名名の自動ローテーションスケジューラー
 * 毎日朝7時に前日以前の匿名名を更新
 */
@Component
public class AnonymousNameScheduler {

    @Autowired
    private GroupMemberAliasRepository aliasRepository;

    @Autowired
    private AnonymousNameService anonymousNameService;

    /**
     * 毎日朝7時に実行
     * cron式: 秒 分 時 日 月 曜日
     * 0 0 7 * * * = 毎日7時0分0秒
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void rotateAnonymousNames() {
        System.out.println("===== 匿名名ローテーション開始 =====");
        
        LocalDate today = LocalDate.now();
        
        // 最終ローテーション日が今日より前のすべてのエイリアスを取得
        List<GroupMemberAlias> aliasesToRotate = aliasRepository.findAll().stream()
                .filter(alias -> alias.getLastRotationDate() == null || 
                                 alias.getLastRotationDate().isBefore(today))
                .toList();
        
        System.out.println("ローテーション対象: " + aliasesToRotate.size() + " 件");
        
        // 各エイリアスをローテーション
        for (GroupMemberAlias alias : aliasesToRotate) {
            anonymousNameService.rotateAnonymousName(alias);
        }
        
        System.out.println("===== 匿名名ローテーション完了 =====");
    }
}
