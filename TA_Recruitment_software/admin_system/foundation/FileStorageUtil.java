package TA_Recruitment_software.admin_system.foundation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class FileStorageUtil {
    private static final Path DATA_DIR = Paths.get("TA_Recruitment_software", "data");

    private FileStorageUtil() {
    }

    public static synchronized void ensureFile(String fileName, String headerLine) {
        try {
            Files.createDirectories(DATA_DIR);
            Path file = DATA_DIR.resolve(fileName);
            if (!Files.exists(file)) {
                List<String> lines = new ArrayList<>();
                lines.add(headerLine);
                Files.write(file, lines, StandardCharsets.UTF_8);
                return;
            }
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                lines.add(headerLine);
                Files.write(file, lines, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new AppException("Failed to initialize storage file: " + fileName);
        }
    }

    public static synchronized List<List<String>> readRows(String fileName, String headerLine) {
        ensureFile(fileName, headerLine);
        Path file = DATA_DIR.resolve(fileName);
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            List<List<String>> rows = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (!line.isEmpty()) {
                    rows.add(CsvUtil.parseLine(line));
                }
            }
            return rows;
        } catch (IOException e) {
            throw new AppException("Failed to read storage file: " + fileName);
        }
    }

    public static synchronized void writeRows(String fileName, String headerLine, List<List<String>> rows) {
        ensureFile(fileName, headerLine);
        Path file = DATA_DIR.resolve(fileName);
        List<String> lines = new ArrayList<>();
        lines.add(headerLine);
        for (List<String> row : rows) {
            lines.add(CsvUtil.toLine(row));
        }
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AppException("Failed to write storage file: " + fileName);
        }
    }

    public static synchronized void appendRow(String fileName, String headerLine, List<String> row) {
        List<List<String>> rows = readRows(fileName, headerLine);
        rows.add(row);
        writeRows(fileName, headerLine, rows);
    }
}
