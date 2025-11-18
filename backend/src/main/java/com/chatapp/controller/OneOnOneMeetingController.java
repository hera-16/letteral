package com.chatapp.controller;

import com.chatapp.model.OneOnOneMeeting;
import com.chatapp.model.User;
import com.chatapp.service.OneOnOneMeetingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/one-on-one")
public class OneOnOneMeetingController {

    private final OneOnOneMeetingService meetingService;

    public OneOnOneMeetingController(OneOnOneMeetingService meetingService) {
        this.meetingService = meetingService;
    }

    /**
     * 1on1ミーティングを作成
     * マネージャー以上の権限が必要
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<OneOnOneMeeting> createMeeting(
            @AuthenticationPrincipal User currentUser,
            @RequestBody CreateMeetingRequest request) {

        // TODO: employeeをIDから取得する処理を追加
        User employee = currentUser; // 仮実装

        OneOnOneMeeting meeting = meetingService.createMeeting(
                employee,
                currentUser,
                currentUser.getTenant(),
                request.getScheduledAt());

        return ResponseEntity.ok(meeting);
    }

    /**
     * ミーティングを更新
     */
    @PutMapping("/{meetingId}")
    public ResponseEntity<OneOnOneMeeting> updateMeeting(
            @PathVariable Long meetingId,
            @RequestBody UpdateMeetingRequest request) {

        OneOnOneMeeting meeting = meetingService.updateMeeting(
                meetingId,
                request.getCustomAgenda(),
                request.getDiscussionTopics(),
                request.getActionItems(),
                request.getNotes());

        return ResponseEntity.ok(meeting);
    }

    /**
     * ミーティングを完了にする
     */
    @PostMapping("/{meetingId}/complete")
    public ResponseEntity<OneOnOneMeeting> completeMeeting(@PathVariable Long meetingId) {
        OneOnOneMeeting meeting = meetingService.completeMeeting(meetingId);
        return ResponseEntity.ok(meeting);
    }

    /**
     * 従業員としてのミーティング一覧を取得
     */
    @GetMapping("/as-employee")
    public ResponseEntity<Page<OneOnOneMeeting>> getEmployeeMeetings(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        Page<OneOnOneMeeting> meetings = meetingService.getEmployeeMeetings(
                user, user.getTenant(), pageable);
        return ResponseEntity.ok(meetings);
    }

    /**
     * マネージャーとしてのミーティング一覧を取得
     */
    @GetMapping("/as-manager")
    public ResponseEntity<Page<OneOnOneMeeting>> getManagerMeetings(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        Page<OneOnOneMeeting> meetings = meetingService.getManagerMeetings(
                user, user.getTenant(), pageable);
        return ResponseEntity.ok(meetings);
    }

    /**
     * 今後のミーティング一覧を取得
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<OneOnOneMeeting>> getUpcomingMeetings(
            @AuthenticationPrincipal User user) {
        List<OneOnOneMeeting> meetings = meetingService.getUpcomingMeetings(
                user, user.getTenant());
        return ResponseEntity.ok(meetings);
    }

    /**
     * アジェンダをプレビュー
     */
    @GetMapping("/agenda/preview")
    public ResponseEntity<String> previewAgenda(
            @AuthenticationPrincipal User user,
            @RequestParam LocalDateTime scheduledAt) {
        String agenda = meetingService.generateAgenda(user, user.getTenant(), scheduledAt);
        return ResponseEntity.ok(agenda);
    }

    // DTOs
    public static class CreateMeetingRequest {
        private Long employeeId;
        private LocalDateTime scheduledAt;

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public LocalDateTime getScheduledAt() {
            return scheduledAt;
        }

        public void setScheduledAt(LocalDateTime scheduledAt) {
            this.scheduledAt = scheduledAt;
        }
    }

    public static class UpdateMeetingRequest {
        private String customAgenda;
        private String discussionTopics;
        private String actionItems;
        private String notes;

        public String getCustomAgenda() {
            return customAgenda;
        }

        public void setCustomAgenda(String customAgenda) {
            this.customAgenda = customAgenda;
        }

        public String getDiscussionTopics() {
            return discussionTopics;
        }

        public void setDiscussionTopics(String discussionTopics) {
            this.discussionTopics = discussionTopics;
        }

        public String getActionItems() {
            return actionItems;
        }

        public void setActionItems(String actionItems) {
            this.actionItems = actionItems;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}
