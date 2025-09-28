package com.chatapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.dto.GroupCreateRequest;
import com.chatapp.dto.GroupJoinRequest;
import com.chatapp.model.Group;
import com.chatapp.model.GroupMember;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.service.GroupService;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:3000")
public class GroupController {

    @Autowired
    private GroupService groupService;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 招待専用グループを作成
     */
    @PostMapping("/invite")
    public ResponseEntity<Group> createInviteGroup(@RequestBody GroupCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // TODO: ユーザー名からユーザーIDを取得するロジックが必要
        Long userId = getUserIdFromUsername(username);
        
        Group group = groupService.createInviteGroup(request.getName(), userId);
        return ResponseEntity.ok(group);
    }

    /**
     * 公開トピックグループを作成
     */
    @PostMapping("/public")
    public ResponseEntity<Group> createPublicGroup(@RequestBody GroupCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Long userId = getUserIdFromUsername(username);
        
        Group group = groupService.createPublicGroup(request.getName(), request.getDescription(), userId);
        return ResponseEntity.ok(group);
    }

    /**
     * 招待コードでグループに参加
     */
    @PostMapping("/join/invite")
    public ResponseEntity<GroupMember> joinByInviteCode(@RequestBody GroupJoinRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Long userId = getUserIdFromUsername(username);
        
        GroupMember member = groupService.joinGroupByInviteCode(request.getInviteCode(), userId);
        return ResponseEntity.ok(member);
    }

    /**
     * グループIDでグループに参加
     */
    @PostMapping("/join/{groupId}")
    public ResponseEntity<GroupMember> joinGroup(@PathVariable Long groupId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Long userId = getUserIdFromUsername(username);
        
        GroupMember member = groupService.joinGroup(groupId, userId);
        return ResponseEntity.ok(member);
    }

    /**
     * ユーザーが参加している全グループを取得
     */
    @GetMapping("/my")
    public ResponseEntity<List<Group>> getMyGroups() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Long userId = getUserIdFromUsername(username);
        
        List<Group> groups = groupService.getUserGroups(userId);
        return ResponseEntity.ok(groups);
    }

    /**
     * グループの詳細を取得
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroupDetails(@PathVariable Long groupId) {
        Group group = groupService.getGroupDetails(groupId);
        return ResponseEntity.ok(group);
    }

    /**
     * グループメンバー一覧を取得
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMember> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    /**
     * グループから退出
     */
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Long userId = getUserIdFromUsername(username);
        
        groupService.leaveGroup(groupId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 公開グループを検索
     */
    @GetMapping("/public/search")
    public ResponseEntity<List<Group>> searchPublicGroups(@RequestParam(required = false) String keyword) {
        List<Group> groups = groupService.searchPublicGroups(keyword);
        return ResponseEntity.ok(groups);
    }

    /**
     * ユーザーの匿名名を取得
     */
    @GetMapping("/{groupId}/anonymous-name")
    public ResponseEntity<String> getAnonymousName(@PathVariable Long groupId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Long userId = getUserIdFromUsername(username);
        
        String anonymousName = groupService.getUserAnonymousName(groupId, userId);
        return ResponseEntity.ok(anonymousName);
    }

    private Long getUserIdFromUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return user.getId();
    }
}