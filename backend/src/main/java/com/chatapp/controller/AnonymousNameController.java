package com.chatapp.controller;

import com.chatapp.model.Group;
import com.chatapp.repository.GroupRepository;
import com.chatapp.service.AnonymousNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/anonymous")
public class AnonymousNameController {
    
    @Autowired
    private AnonymousNameService anonymousNameService;
    
    @Autowired
    private GroupRepository groupRepository;
    
    /**
     * 特定のグループ内で閲覧者が見るすべてのユーザーの匿名名マップを取得
     * Key: targetUserId, Value: anonymousName
     */
    @GetMapping("/groups/{groupId}/names")
    public ResponseEntity<Map<Long, String>> getAnonymousNames(
            @PathVariable Long groupId,
            Authentication authentication) {
        
        // 認証からユーザーIDを取得
        Long viewerId = getUserIdFromAuthentication(authentication);
        
        // 匿名名マップを取得
        Map<Long, String> nameMap = anonymousNameService.getAnonymousNameMap(viewerId, groupId);
        
        return ResponseEntity.ok(nameMap);
    }
    
    /**
     * 特定のグループの匿名名をすべてリセット（管理者のみ）
     */
    @DeleteMapping("/groups/{groupId}/names")
    public ResponseEntity<Void> resetAnonymousNames(
            @PathVariable Long groupId,
            Authentication authentication) {
        
        // グループ管理者権限チェック
        Long userId = getUserIdFromAuthentication(authentication);
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));
        
        if (!group.getCreator().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        anonymousNameService.resetGroupAliases(groupId);
        
        return ResponseEntity.ok().build();
    }
    
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // UserPrincipalからユーザーIDを取得
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.chatapp.security.UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("Invalid authentication principal");
    }
}
