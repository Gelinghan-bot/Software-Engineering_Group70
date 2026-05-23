package TA_Recruitment_software.forum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TopicRepository {
    private static final String DATA_FILE = "data/topics.csv";

    public TopicRepository() {
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

    public List<Topic> getAllTopics() {
        List<Topic> topics = new ArrayList<>();
        try {
            // All writes use UTF-8; prefer UTF-8 to avoid issues on macOS and other UTF-8 systems
            List<String> lines = java.nio.file.Files.readAllLines(new File(DATA_FILE).toPath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                Topic t = Topic.fromCsvRow(line);
                if (t != null) {
                    topics.add(t);
                }
            }
        } catch (Exception e) {
            try {
                // Fall back to system default charset (e.g. GBK on Chinese Windows) for legacy files
                List<String> lines = java.nio.file.Files.readAllLines(new File(DATA_FILE).toPath(), java.nio.charset.Charset.defaultCharset());
                for (String line : lines) {
                    Topic t = Topic.fromCsvRow(line);
                    if (t != null) {
                        topics.add(t);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return topics;
    }

    public void addTopic(Topic topic) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DATA_FILE, true), StandardCharsets.UTF_8))) {
            writer.write(topic.toCsvRow());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateTopic(Topic updatedTopic) {
        List<Topic> topics = getAllTopics();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DATA_FILE, false), StandardCharsets.UTF_8))) { // Overwrite to update
            for (Topic t : topics) {
                if (t.getId().equals(updatedTopic.getId())) {
                    writer.write(updatedTopic.toCsvRow());
                } else {
                    writer.write(t.toCsvRow());
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Delete a topic by its ID. Returns true if deleted, false if not found. */
    public boolean deleteTopic(String topicId) {
        List<Topic> topics = getAllTopics();
        boolean found = false;
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DATA_FILE, false), StandardCharsets.UTF_8))) {
            for (Topic t : topics) {
                if (t.getId().equals(topicId)) {
                    found = true;
                    continue;
                }
                writer.write(t.toCsvRow());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return found;
    }
}