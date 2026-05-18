package TA_Recruitment_software.forum;

import java.io.*;
import java.util.*;

public class PollRepository {
    private static final String DATA_FILE = "data/poll_votes.csv";

    // Returns a map of username -> voted subject
    public Map<String, String> loadVotes() {
        Map<String, String> votes = new HashMap<>();
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
            return votes;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    votes.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return votes;
    }

    public void saveVote(String username, String subject) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DATA_FILE, true), "UTF-8"))) {
            bw.write(username + "," + subject);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
