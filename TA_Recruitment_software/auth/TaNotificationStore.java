package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.foundation.FileStorageUtil;
import java.util.ArrayList;
import java.util.List;

final class TaNotificationStore {
    private static final String FILE_NAME = "ta_notifications.csv";
    private static final String HEADER =
        "notificationId,userId,applicationId,positionId,oldStatus,newStatus,message,createdAt,read";

    private TaNotificationStore() {
    }

    static List<TaNotification> findByUser(String userId) {
        List<TaNotification> result = new ArrayList<>();
        for (TaNotification n : findAll()) {
            if (userId.equals(n.getUserId())) {
                result.add(n);
            }
        }
        return result;
    }

    static List<TaNotification> findAll() {
        List<List<String>> rows = FileStorageUtil.readRows(FILE_NAME, HEADER);
        List<TaNotification> notifications = new ArrayList<>();
        for (List<String> row : rows) {
            notifications.add(fromRow(row));
        }
        return notifications;
    }

    static void save(TaNotification target) {
        List<TaNotification> all = findAll();
        boolean updated = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getNotificationId().equals(target.getNotificationId())) {
                all.set(i, target);
                updated = true;
                break;
            }
        }
        if (!updated) {
            all.add(target);
        }
        saveAll(all);
    }

    private static void saveAll(List<TaNotification> notifications) {
        List<List<String>> rows = new ArrayList<>();
        for (TaNotification n : notifications) {
            rows.add(toRow(n));
        }
        FileStorageUtil.writeRows(FILE_NAME, HEADER, rows);
    }

    private static List<String> toRow(TaNotification n) {
        List<String> row = new ArrayList<>();
        row.add(nvl(n.getNotificationId()));
        row.add(nvl(n.getUserId()));
        row.add(nvl(n.getApplicationId()));
        row.add(nvl(n.getPositionId()));
        row.add(nvl(n.getOldStatus()));
        row.add(nvl(n.getNewStatus()));
        row.add(nvl(n.getMessage()));
        row.add(nvl(n.getCreatedAt()));
        row.add(n.isRead() ? "true" : "false");
        return row;
    }

    private static TaNotification fromRow(List<String> row) {
        TaNotification n = new TaNotification();
        n.setNotificationId(cell(row, 0));
        n.setUserId(cell(row, 1));
        n.setApplicationId(cell(row, 2));
        n.setPositionId(cell(row, 3));
        n.setOldStatus(cell(row, 4));
        n.setNewStatus(cell(row, 5));
        n.setMessage(cell(row, 6));
        n.setCreatedAt(cell(row, 7));
        n.setRead("true".equalsIgnoreCase(cell(row, 8)));
        return n;
    }

    private static String cell(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}
