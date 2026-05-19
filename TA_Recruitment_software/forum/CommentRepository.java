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
            lines = Files.readAllLines(new File(DATA_FILE).toPath(), Charset.defaultCharset());
        } catch (Exception e) {
            try {
                lines = Files.readAllLines(new File(DATA_FILE).toPath(), StandardCharsets.UTF_8);
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
}
