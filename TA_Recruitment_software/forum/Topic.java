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
    private int favorites;
    private java.util.Set<String> likedByUsers = new java.util.HashSet<>();
    private java.util.Set<String> favoritedByUsers = new java.util.HashSet<>();
    private String dateStr;

    public Topic(String authorName, String title, String content) {
        this.id = UUID.randomUUID().toString();
        this.authorName = authorName;
        this.title = title;
        this.content = content;
        this.likes = 0;
        this.comments = 0;
        this.favorites = 0;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        this.dateStr = sdf.format(new Date());
    }

    public Topic(String id, String authorName, String title, String content, int likes, int comments, String dateStr) {
        this.id = id;
        this.authorName = authorName;
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.comments = comments;
        this.favorites = 0;
        this.dateStr = dateStr;
    }

    public String getId() { return id; }
    public String getAuthorName() { return authorName; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getLikes() { return likes; }
    public int getComments() { return comments; }
    public int getFavorites() { return favorites; }
    public String getDateStr() { return dateStr; }
    public java.util.Set<String> getLikedByUsers() { return likedByUsers; }
    public java.util.Set<String> getFavoritedByUsers() { return favoritedByUsers; }

    public void setLikes(int likes) { this.likes = likes; }
    public void setComments(int comments) { this.comments = comments; }
    public void setFavorites(int favorites) { this.favorites = favorites; }
    public void incrementComments() { this.comments++; }
    
    public boolean toggleLike(String username) {
        if (likedByUsers.contains(username)) {
            likedByUsers.remove(username);
            likes = likedByUsers.size();
            return false;
        } else {
            likedByUsers.add(username);
            likes = likedByUsers.size();
            return true;
        }
    }

    public boolean toggleFavorite(String username) {
        if (favoritedByUsers.contains(username)) {
            favoritedByUsers.remove(username);
            favorites = favoritedByUsers.size();
            return false;
        } else {
            favoritedByUsers.add(username);
            favorites = favoritedByUsers.size();
            return true;
        }
    }

    public String toCsvRow() {
        // Escape commas and newlines
        String safeTitle = title.replace(",", "，").replace("\n", " ");
        String safeContent = content.replace(",", "，").replace("\n", "<br>");
        String likesStr = String.join("|", likedByUsers);
        String favStr = String.join("|", favoritedByUsers);
        if (likesStr.isEmpty()) likesStr = "none";
        if (favStr.isEmpty()) favStr = "none";
        return String.format("%s,%s,%s,%s,%d,%d,%s,%d,%s,%s", 
            id, authorName, safeTitle, safeContent, likes, comments, dateStr, favorites, likesStr, favStr);
    }

    public static Topic fromCsvRow(String row) {
        String[] parts = row.split(",", 10);
        if (parts.length >= 7) {
            String content = parts[3].replace("<br>", "\n");
            Topic t = new Topic(parts[0], parts[1], parts[2], content, 
                Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), parts[6]);
            
            if (parts.length >= 10) {
                t.favorites = Integer.parseInt(parts[7]);
                if (!parts[8].equals("none")) {
                    t.likedByUsers.addAll(java.util.Arrays.asList(parts[8].split("\\|")));
                }
                if (!parts[9].equals("none")) {
                    t.favoritedByUsers.addAll(java.util.Arrays.asList(parts[9].split("\\|")));
                }
            }
            return t;
        }
        return null;
    }
}