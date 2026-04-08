package TA_Recruitment_software.ta_jobs;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.CsvUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 岗位与学期的对应关系，仅由本模块的 CSV 维护（不修改 positions.csv）。
 * <p>
 * 规则：若某 positionId 在此文件中<strong>没有</strong>记录，则该岗位对任意学期均可见（与改前行为一致）；
 * 若有记录，则仅当 semester 与 {@link CurrentSemesterStore} 中当前学期一致时，TA 端列表才显示、才允许申请。
 */
public final class PositionSemesterStore {

    private static final Path DATA_DIR = Paths.get("TA_Recruitment_software", "ta_jobs", "data");
    private static final String FILE_NAME = "position_semesters.csv";
    private static final String HEADER = "positionId,semester";

    private PositionSemesterStore() {
    }

    public static Map<String, String> readAll() {
        try {
            Files.createDirectories(DATA_DIR);
            Path file = DATA_DIR.resolve(FILE_NAME);
            if (!Files.exists(file)) {
                Files.write(file, List.of(HEADER), StandardCharsets.UTF_8);
                return Collections.emptyMap();
            }
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            Map<String, String> map = new HashMap<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                List<String> cells = CsvUtil.parseLine(line);
                if (cells.size() >= 2) {
                    String pid = cells.get(0).trim();
                    String sem = cells.get(1).trim();
                    if (!pid.isEmpty() && !sem.isEmpty()) {
                        map.put(pid, sem);
                    }
                }
            }
            return map;
        } catch (IOException e) {
            throw new AppException("Failed to read: " + FILE_NAME);
        }
    }

    public static String labelFor(String positionId, Map<String, String> semesterByPositionId) {
        String s = semesterByPositionId.get(positionId);
        return s == null || s.isEmpty() ? "-" : s;
    }

    public static boolean isAllowedForCurrentSemester(String positionId, String currentSemester, Map<String, String> semesterByPositionId) {
        String tagged = semesterByPositionId.get(positionId);
        if (tagged == null || tagged.trim().isEmpty()) {
            return true;
        }
        return tagged.trim().equals(currentSemester);
    }
}
