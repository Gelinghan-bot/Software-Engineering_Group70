package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.foundation.FileStorageUtil;
import TA_Recruitment_software.admin_system.model.Role;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SessionContext {
    private final String token;
    private final String userId;
    private final Role role;
    private final String accountId;
    private final String fullName;
    private LocalDateTime lastAccessAt;

    public SessionContext(String token, String userId, Role role, String accountId, String fullName, LocalDateTime lastAccessAt) {
        this.token = token;
        this.userId = userId;
        this.role = role;
        this.accountId = accountId;
        this.fullName = fullName;
        this.lastAccessAt = lastAccessAt;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDateTime getLastAccessAt() {
        return lastAccessAt;
    }

    public void setLastAccessAt(LocalDateTime lastAccessAt) {
        this.lastAccessAt = lastAccessAt;
    }
}

/**
 * Manages login history for the application.
 * Stores account IDs of successfully logged-in users locally for convenience.
 */
final class LoginHistoryStore {
    private static final String FILE_NAME = "login_history.txt";
    private static final int MAX_HISTORY = 10;

    static void addAccount(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            return;
        }

        List<String> history = readAll();
        String trimmedId = accountId.trim();

        history.remove(trimmedId);
        history.add(0, trimmedId);

        if (history.size() > MAX_HISTORY) {
            history = new ArrayList<>(history.subList(0, MAX_HISTORY));
        }

        writeAll(history);
    }

    static List<String> getAllAccounts() {
        return readAll();
    }

    static void clearAll() {
        writeAll(new ArrayList<>());
    }

    static void removeAccount(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            return;
        }

        List<String> history = readAll();
        history.remove(accountId.trim());
        writeAll(history);
    }

    private static List<String> readAll() {
        try {
            List<List<String>> rows = FileStorageUtil.readRows(FILE_NAME, "");
            Set<String> uniqueAccounts = new LinkedHashSet<>();
            for (List<String> row : rows) {
                if (!row.isEmpty()) {
                    String account = row.get(0).trim();
                    if (!account.isEmpty()) {
                        uniqueAccounts.add(account);
                    }
                }
            }
            return new ArrayList<>(uniqueAccounts);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private static void writeAll(List<String> accounts) {
        try {
            List<List<String>> rows = new ArrayList<>();
            for (String account : accounts) {
                List<String> row = new ArrayList<>();
                row.add(account);
                rows.add(row);
            }
            FileStorageUtil.writeRows(FILE_NAME, "", rows);
        } catch (Exception ignored) {
            // Silent fail for file write issues
        }
    }
}
