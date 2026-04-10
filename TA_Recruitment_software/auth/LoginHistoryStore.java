package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.foundation.FileStorageUtil;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages login history for the application.
 * Stores account IDs of successfully logged-in users locally for convenience.
 */
public class LoginHistoryStore {
    private static final String FILE_NAME = "login_history.txt";
    private static final int MAX_HISTORY = 10;

    /**
     * Add an account ID to the login history.
     * Prevents duplicates by moving existing entries to the top.
     */
    public static void addAccount(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            return;
        }

        List<String> history = readAll();
        String trimmedId = accountId.trim();

        // Remove if already exists, then add to front
        history.remove(trimmedId);
        history.add(0, trimmedId);

        // Keep only MAX_HISTORY entries
        if (history.size() > MAX_HISTORY) {
            history = new ArrayList<>(history.subList(0, MAX_HISTORY));
        }

        writeAll(history);
    }

    /**
     * Get all stored account IDs in order (most recent first).
     */
    public static List<String> getAllAccounts() {
        return readAll();
    }

    /**
     * Clear all login history.
     */
    public static void clearAll() {
        writeAll(new ArrayList<>());
    }

    /**
     * Remove a specific account from history.
     */
    public static void removeAccount(String accountId) {
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
