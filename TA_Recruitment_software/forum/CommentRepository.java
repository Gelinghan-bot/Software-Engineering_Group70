package TA_Recruitment_software.forum;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CommentRepository {
    private static final String DATA_FILE = "data/comments.csv";

    public CommentRepository() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Comment> getCommentsByTopicId(String topicId) {
        List<Comment> comments = new ArrayList<>();
        List<String> lines = null;
        try {
            lines = Files.readAllLines(new File(DATA_FILE).toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            try {
                lines = Files.readAllLines(new File(DATA_FILE).toPath(), Charset.defaultCharset());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (lines != null) {
            for (String line : lines) {
                Comment c = Comment.fromCsvRow(line);
                if (c != null && c.getTopicId().equals(topicId)) {
                    comments.add(c);
                }
            }
        }
        return comments;
    }

    public void addComment(Comment comment) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DATA_FILE, true), StandardCharsets.UTF_8))) {
            writer.write(comment.toCsvRow());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Delete all comments belonging to a topic. */
    public void deleteCommentsByTopicId(String topicId) {
        File file = new File(DATA_FILE);
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            try {
                lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }
        List<String> remaining = new ArrayList<>();
        for (String line : lines) {
            Comment c = Comment.fromCsvRow(line);
            if (c != null && c.getTopicId().equals(topicId)) {
                continue;
            }
            remaining.add(line);
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String line : remaining) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Delete a comment by its ID. Returns true if deleted, false if not found. */
    public boolean deleteComment(String commentId) {
        File file = new File(DATA_FILE);
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            try {
                lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        boolean found = false;
        List<String> remaining = new ArrayList<>();
        for (String line : lines) {
            Comment c = Comment.fromCsvRow(line);
            if (c != null && c.getId().equals(commentId)) {
                found = true;
                continue; // skip this line
            }
            remaining.add(line);
        }
        if (found) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                for (String line : remaining) {
                    writer.write(line);
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return found;
    }
}
