package TA_Recruitment_software.forum;

import TA_Recruitment_software.admin_system.foundation.FileStorageUtil;

import java.io.*;
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
            List<String> lines = java.nio.file.Files.readAllLines(new File(DATA_FILE).toPath());
            for (String line : lines) {
                Topic t = Topic.fromCsvRow(line);
                if (t != null) {
                    topics.add(t);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return topics;
    }

    public void addTopic(Topic topic) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE, true))) {
            writer.write(topic.toCsvRow());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}