package TA_Recruitment_software.ta_jobs;

import TA_Recruitment_software.admin_system.foundation.AppException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 当前学期代码，仅存于 ta_jobs 模块目录下的纯文本文件（首行非空即生效）。
 */
public final class CurrentSemesterStore {

    private static final Path DATA_DIR = Paths.get("TA_Recruitment_software", "ta_jobs", "data");
    private static final String FILE_NAME = "current_semester.txt";
    private static final String DEFAULT_SEMESTER = "2026-Spring";

    private CurrentSemesterStore() {
    }

    public static String readCurrentSemester() {
        try {
            Files.createDirectories(DATA_DIR);
            Path file = DATA_DIR.resolve(FILE_NAME);
            if (!Files.exists(file)) {
                Files.write(file, List.of(DEFAULT_SEMESTER), StandardCharsets.UTF_8);
                return DEFAULT_SEMESTER;
            }
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null) {
                    continue;
                }
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
            }
            Files.write(file, List.of(DEFAULT_SEMESTER), StandardCharsets.UTF_8);
            return DEFAULT_SEMESTER;
        } catch (IOException e) {
            throw new AppException("Failed to read current semester file: " + FILE_NAME);
        }
    }
}
