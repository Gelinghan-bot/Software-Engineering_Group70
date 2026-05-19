package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.foundation.FileStorageUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks the last known application status per TA, used to detect status changes for notifications.
 */
final class TaApplicationStatusCheckpointStore {
    private static final String FILE_NAME = "ta_app_status_checkpoints.csv";
    private static final String HEADER = "userId,applicationId,lastStatus";

    private TaApplicationStatusCheckpointStore() {
    }

    static Map<String, String> readForUser(String userId) {
        Map<String, String> map = new HashMap<>();
        List<List<String>> rows = FileStorageUtil.readRows(FILE_NAME, HEADER);
        for (List<String> row : rows) {
            if (row.size() >= 3 && userId.equals(row.get(0).trim())) {
                map.put(row.get(1).trim(), row.get(2).trim());
            }
        }
        return map;
    }

    static void writeForUser(String userId, Map<String, String> applicationIdToStatus) {
        List<List<String>> rows = FileStorageUtil.readRows(FILE_NAME, HEADER);
        rows.removeIf(row -> row.size() >= 1 && userId.equals(row.get(0).trim()));

        for (Map.Entry<String, String> entry : applicationIdToStatus.entrySet()) {
            List<String> row = List.of(userId, entry.getKey(), entry.getValue());
            rows.add(row);
        }
        FileStorageUtil.writeRows(FILE_NAME, HEADER, rows);
    }
}
