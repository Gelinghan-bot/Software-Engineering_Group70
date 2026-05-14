package TA_Recruitment_software.forum;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Topic implements Serializable {
    private String id;
    private String authorName;
    private String title;
    private String content;
    private int likes;
    private int comments;
    private String dateStr;

    public Topic(String authorName, String title, String content) {
        this.id = UUID.randomUUID().toString();
        this.authorName = authorName;
        this.title = title;
        this.content = content;
        this.likes = 0;
        this.comments = 0;
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM @ h:mma");
        this.dateStr = sdf.format(new Date());
    }

    public Topic(String id, String authorName, String title, String content, int likes, int comments, String dateStr) {
        this.id = id;
        this.authorName = authorName;
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.comments = comments;
        this.dateStr = dateStr;
    }

    public String getId() { return id; }
    public String getAuthorName() { return authorName; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getLikes() { return likes; }
    public int getComments() { return comments; }
    public String getDateStr() { return dateStr; }

    public String toCsvRow() {
        // Escape commas and newlines
        String safeTitle = title.replace(",", "，").replace("\n", " ");
        String safeContent = content.replace(",", "，").replace("\n", "<br>");
        return String.format("%s,%s,%s,%s,%d,%d,%s", 
            id, authorName, safeTitle, safeContent, likes, comments, dateStr);
    }

    public static Topic fromCsvRow(String row) {
        String[] parts = row.split(",", 7);
        if (parts.length >= 7) {
            String content = parts[3].replace("<br>", "\n");
            return new Topic(parts[0], parts[1], parts[2], content, 
                Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), parts[6]);
        }
        return null;
    }
}