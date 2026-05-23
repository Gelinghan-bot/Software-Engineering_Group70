package TA_Recruitment_software.profile;

import TA_Recruitment_software.admin_system.foundation.FileStorageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 每 TA 用户一份默认求职信模板（独立 CSV，不修改 users.csv / applications.csv）。
 */
public final class CoverLetterTemplateStore {

    private static final String FILE_NAME = "ta_cover_letter_templates.csv";
    private static final String HEADER = "userId,content,updatedAt";

    private CoverLetterTemplateStore() {
    }

    public static Optional<CoverLetterRecord> findByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        String id = userId.trim();
        for (List<String> row : FileStorageUtil.readRows(FILE_NAME, HEADER)) {
            if (row.size() >= 1 && id.equals(cell(row, 0))) {
                return Optional.of(new CoverLetterRecord(
                    cell(row, 0),
                    cell(row, 1),
                    cell(row, 2)
                ));
            }
        }
        return Optional.empty();
    }

    public static void save(String userId, String content, String updatedAt) {
        String id = userId == null ? "" : userId.trim();
        List<List<String>> rows = new ArrayList<>();
        for (List<String> row : FileStorageUtil.readRows(FILE_NAME, HEADER)) {
            if (row.size() >= 1 && id.equals(cell(row, 0))) {
                continue;
            }
            rows.add(row);
        }
        List<String> newRow = new ArrayList<>();
        newRow.add(id);
        newRow.add(content == null ? "" : content);
        newRow.add(updatedAt == null ? "" : updatedAt);
        rows.add(newRow);
        FileStorageUtil.writeRows(FILE_NAME, HEADER, rows);
    }

    private static String cell(List<String> row, int index) {
        if (index >= row.size()) {
            return "";
        }
        return row.get(index);
    }

    public static final class CoverLetterRecord {
        private final String userId;
        private final String content;
        private final String updatedAt;

        public CoverLetterRecord(String userId, String content, String updatedAt) {
            this.userId = userId;
            this.content = content;
            this.updatedAt = updatedAt;
        }

        public String getUserId() {
            return userId;
        }

        public String getContent() {
            return content;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }
}
