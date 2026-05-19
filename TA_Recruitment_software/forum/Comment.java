package TA_Recruitment_software.forum;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Comment implements Serializable {
    private String id;
    private String topicId;
    private String authorName;
    private String content;
    private String dateStr;

    public Comment(String topicId, String authorName, String content) {
        this.id = UUID.randomUUID().toString();
        this.topicId = topicId;
        this.authorName = authorName;
        this.content = content;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        this.dateStr = sdf.format(new Date());
    }

    public Comment(String id, String topicId, String authorName, String content, String dateStr) {
        this.id = id;
        this.topicId = topicId;
        this.authorName = authorName;
        this.content = content;
        this.dateStr = dateStr;
    }

    public String getId() { return id; }
    public String getTopicId() { return topicId; }
    public String getAuthorName() { return authorName; }
    public String getContent() { return content; }
    public String getDateStr() { return dateStr; }

    public String toCsvRow() {
        String safeContent = content.replace(",", "，").replace("\n", "<br>");
        return String.format("%s,%s,%s,%s,%s", id, topicId, authorName, safeContent, dateStr);
    }

    public static Comment fromCsvRow(String row) {
        String[] parts = row.split(",", 5);
        if (parts.length >= 5) {
            String content = parts[3].replace("<br>", "\n");
            return new Comment(parts[0], parts[1], parts[2], content, parts[4]);
        }
        return null;
    }
}
