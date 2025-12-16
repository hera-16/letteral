package com.chatapp.service;

import com.chatapp.model.*;
import com.chatapp.repository.ProgressPostRepository;
import com.chatapp.repository.OneOnOneMeetingRepository;
import com.chatapp.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    private final ProgressPostRepository progressPostRepository;
    private final OneOnOneMeetingRepository meetingRepository;
    private final UserRepository userRepository;

    public ExportService(ProgressPostRepository progressPostRepository,
                        OneOnOneMeetingRepository meetingRepository,
                        UserRepository userRepository) {
        this.progressPostRepository = progressPostRepository;
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
    }

    /**
     * 進捗投稿をCSV形式でエクスポート
     */
    public byte[] exportProgressPostsToCSV(User user, Tenant tenant,
                                          LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        List<ProgressPost> posts = progressPostRepository.findByAuthorAndCreatedAtBetween(
                user, startDate, endDate);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // CSVヘッダー
        String header = "日付,タイトル,内容,達成率,ブロッカー,学び,次のアクション,タグ\n";
        outputStream.write(header.getBytes(StandardCharsets.UTF_8));

        // データ行
        for (ProgressPost post : posts) {
            StringBuilder row = new StringBuilder();
            row.append(escapeCsvField(post.getCreatedAt().format(formatter))).append(",");
            row.append(escapeCsvField(post.getTitle() != null ? post.getTitle() : "")).append(",");
            row.append(escapeCsvField(post.getContent())).append(",");
            row.append(post.getAchievementRate() != null ? post.getAchievementRate() : "").append(",");
            row.append(escapeCsvField(post.getBlockers() != null ? post.getBlockers() : "")).append(",");
            row.append(escapeCsvField(post.getLearnings() != null ? post.getLearnings() : "")).append(",");
            row.append(escapeCsvField(post.getNextAction() != null ? post.getNextAction() : "")).append(",");
            row.append(escapeCsvField(post.getTags() != null ? String.join(";", post.getTags()) : ""));
            row.append("\n");

            outputStream.write(row.toString().getBytes(StandardCharsets.UTF_8));
        }

        return outputStream.toByteArray();
    }

    /**
     * ダイジェストをCSV形式でエクスポート
     */
    public byte[] exportDigestToCSV(ProgressDigest digest) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // ダイジェスト情報
        StringBuilder content = new StringBuilder();
        content.append("進捗ダイジェスト\n\n");
        content.append("期間タイプ,").append(digest.getDigestType()).append("\n");
        content.append("期間開始,").append(digest.getPeriodStart().format(formatter)).append("\n");
        content.append("期間終了,").append(digest.getPeriodEnd().format(formatter)).append("\n\n");

        content.append("統計情報\n");
        content.append("総投稿数,").append(digest.getTotalPosts()).append("\n");
        content.append("達成した目標,").append(digest.getCompletedGoals()).append("\n");
        content.append("進行中の取り組み,").append(digest.getOngoingGoals()).append("\n");
        content.append("課題数,").append(digest.getChallenges()).append("\n\n");

        content.append("サマリー\n");
        content.append(escapeCsvField(digest.getSummary() != null ? digest.getSummary() : "")).append("\n\n");

        content.append("主な達成事項\n");
        content.append(escapeCsvField(digest.getTopAchievements() != null ? digest.getTopAchievements() : "")).append("\n\n");

        content.append("主な課題\n");
        content.append(escapeCsvField(digest.getTopChallenges() != null ? digest.getTopChallenges() : "")).append("\n\n");

        content.append("重要な学び\n");
        content.append(escapeCsvField(digest.getKeyLearnings() != null ? digest.getKeyLearnings() : "")).append("\n\n");

        content.append("次のステップ\n");
        content.append(escapeCsvField(digest.getNextSteps() != null ? digest.getNextSteps() : "")).append("\n");

        outputStream.write(content.toString().getBytes(StandardCharsets.UTF_8));
        return outputStream.toByteArray();
    }

    /**
     * ダイジェストをMarkdown形式でエクスポート
     */
    public byte[] exportDigestToMarkdown(ProgressDigest digest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        StringBuilder markdown = new StringBuilder();
        markdown.append("# 進捗ダイジェスト\n\n");
        markdown.append("**期間タイプ**: ").append(digest.getDigestType()).append("\n\n");
        markdown.append("**期間**: ").append(digest.getPeriodStart().format(formatter))
                .append(" ~ ").append(digest.getPeriodEnd().format(formatter)).append("\n\n");

        markdown.append("## 統計情報\n\n");
        markdown.append("- 総投稿数: ").append(digest.getTotalPosts()).append("件\n");
        markdown.append("- 達成した目標: ").append(digest.getCompletedGoals()).append("件\n");
        markdown.append("- 進行中の取り組み: ").append(digest.getOngoingGoals()).append("件\n");
        markdown.append("- 課題: ").append(digest.getChallenges()).append("件\n\n");

        markdown.append("## サマリー\n\n");
        markdown.append(digest.getSummary() != null ? digest.getSummary() : "なし").append("\n\n");

        if (digest.getTopAchievements() != null && !digest.getTopAchievements().trim().isEmpty()) {
            markdown.append("## 主な達成事項\n\n");
            markdown.append(digest.getTopAchievements()).append("\n\n");
        }

        if (digest.getTopChallenges() != null && !digest.getTopChallenges().trim().isEmpty()) {
            markdown.append("## 主な課題\n\n");
            markdown.append(digest.getTopChallenges()).append("\n\n");
        }

        if (digest.getKeyLearnings() != null && !digest.getKeyLearnings().trim().isEmpty()) {
            markdown.append("## 重要な学び\n\n");
            markdown.append(digest.getKeyLearnings()).append("\n\n");
        }

        if (digest.getNextSteps() != null && !digest.getNextSteps().trim().isEmpty()) {
            markdown.append("## 次のステップ\n\n");
            markdown.append(digest.getNextSteps()).append("\n");
        }

        return markdown.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 進捗投稿をExcel形式でエクスポート
     */
    public byte[] exportProgressPostsToExcel(User user, Tenant tenant,
                                            LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        List<ProgressPost> posts = progressPostRepository.findByAuthorAndCreatedAtBetween(
                user, startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("進捗投稿");

            // ヘッダースタイル
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ヘッダー行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"日付", "タイトル", "内容", "達成率(%)", "ブロッカー", "学び", "次のアクション", "タグ"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // データ行
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            for (ProgressPost post : posts) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(post.getCreatedAt().format(formatter));
                row.createCell(1).setCellValue(post.getTitle() != null ? post.getTitle() : "");
                row.createCell(2).setCellValue(post.getContent());
                Integer achievementRate = post.getAchievementRate();
                row.createCell(3).setCellValue(achievementRate != null ? achievementRate.doubleValue() : 0.0);
                row.createCell(4).setCellValue(post.getBlockers() != null ? post.getBlockers() : "");
                row.createCell(5).setCellValue(post.getLearnings() != null ? post.getLearnings() : "");
                row.createCell(6).setCellValue(post.getNextAction() != null ? post.getNextAction() : "");
                row.createCell(7).setCellValue(post.getTags() != null ? String.join(", ", post.getTags()) : "");
            }

            // 列幅を自動調整
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * 1on1ミーティングをCSV形式でエクスポート
     */
    public byte[] exportMeetingsToCSV(User user, Tenant tenant,
                                     LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        List<OneOnOneMeeting> meetings = meetingRepository.findByTenantAndStatusOrderByScheduledAtDesc(
                tenant, OneOnOneMeeting.MeetingStatus.COMPLETED);

        // 期間でフィルタリング
        meetings = meetings.stream()
                .filter(m -> !m.getScheduledAt().isBefore(startDate) && !m.getScheduledAt().isAfter(endDate))
                .filter(m -> m.getEmployee().getId().equals(user.getId()) || m.getManager().getId().equals(user.getId()))
                .toList();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // CSVヘッダー
        String header = "日付,従業員,マネージャー,ステータス,アジェンダ,ディスカッション,アクションアイテム,メモ,完了日\n";
        outputStream.write(header.getBytes(StandardCharsets.UTF_8));

        // データ行
        for (OneOnOneMeeting meeting : meetings) {
            StringBuilder row = new StringBuilder();
            row.append(escapeCsvField(meeting.getScheduledAt().format(formatter))).append(",");
            row.append(escapeCsvField(meeting.getEmployee().getUsername())).append(",");
            row.append(escapeCsvField(meeting.getManager().getUsername())).append(",");
            row.append(escapeCsvField(meeting.getStatus().toString())).append(",");

            String agenda = meeting.getCustomAgenda() != null ? meeting.getCustomAgenda() : meeting.getAutoGeneratedAgenda();
            row.append(escapeCsvField(agenda != null ? agenda : "")).append(",");

            row.append(escapeCsvField(meeting.getDiscussionTopics() != null ? meeting.getDiscussionTopics() : "")).append(",");
            row.append(escapeCsvField(meeting.getActionItems() != null ? meeting.getActionItems() : "")).append(",");
            row.append(escapeCsvField(meeting.getNotes() != null ? meeting.getNotes() : "")).append(",");
            row.append(escapeCsvField(meeting.getCompletedAt() != null ? meeting.getCompletedAt().format(formatter) : ""));
            row.append("\n");

            outputStream.write(row.toString().getBytes(StandardCharsets.UTF_8));
        }

        return outputStream.toByteArray();
    }

    /**
     * 1on1ミーティングをExcel形式でエクスポート
     */
    public byte[] exportMeetingsToExcel(User user, Tenant tenant,
                                       LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        List<OneOnOneMeeting> meetings = meetingRepository.findByTenantAndStatusOrderByScheduledAtDesc(
                tenant, OneOnOneMeeting.MeetingStatus.COMPLETED);

        // 期間でフィルタリング
        meetings = meetings.stream()
                .filter(m -> !m.getScheduledAt().isBefore(startDate) && !m.getScheduledAt().isAfter(endDate))
                .filter(m -> m.getEmployee().getId().equals(user.getId()) || m.getManager().getId().equals(user.getId()))
                .toList();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("1on1ミーティング");

            // ヘッダースタイル
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ヘッダー行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"日付", "従業員", "マネージャー", "ステータス", "アジェンダ",
                              "ディスカッション", "アクションアイテム", "メモ", "完了日"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // データ行
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            for (OneOnOneMeeting meeting : meetings) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(meeting.getScheduledAt().format(formatter));
                row.createCell(1).setCellValue(meeting.getEmployee().getUsername());
                row.createCell(2).setCellValue(meeting.getManager().getUsername());
                row.createCell(3).setCellValue(meeting.getStatus().toString());

                String agenda = meeting.getCustomAgenda() != null ? meeting.getCustomAgenda() : meeting.getAutoGeneratedAgenda();
                row.createCell(4).setCellValue(agenda != null ? agenda : "");

                row.createCell(5).setCellValue(meeting.getDiscussionTopics() != null ? meeting.getDiscussionTopics() : "");
                row.createCell(6).setCellValue(meeting.getActionItems() != null ? meeting.getActionItems() : "");
                row.createCell(7).setCellValue(meeting.getNotes() != null ? meeting.getNotes() : "");
                row.createCell(8).setCellValue(meeting.getCompletedAt() != null ? meeting.getCompletedAt().format(formatter) : "");
            }

            // 列幅を自動調整
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * ユーザー一覧をExcel形式でエクスポート
     */
    public byte[] exportUsersToExcel(Tenant tenant) throws IOException {
        List<User> users = userRepository.findByTenantId(tenant.getId());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("ユーザー一覧");

            // ヘッダースタイル
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ヘッダー行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "ユーザー名", "メールアドレス", "ロール", "作成日"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // データ行
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(3).setCellValue(user.getRole() != null ? String.valueOf(user.getRole()) : "");
                row.createCell(4).setCellValue(user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "");
            }

            // 列幅を自動調整
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * CSVフィールドをエスケープ
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }

        // ダブルクォートをエスケープ
        String escaped = field.replace("\"", "\"\"");

        // カンマ、改行、ダブルクォートが含まれる場合はダブルクォートで囲む
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }
}
