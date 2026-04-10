package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.FileStorageUtil;
import TA_Recruitment_software.admin_system.foundation.SecurityUtil;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final int EXPIRE_MINUTES = 7 * 24 * 60; // 7 days
    private static final String SESSIONS_FILE = "sessions.csv";
    private static final String SESSIONS_HEADER = "token,userId,role,accountId,fullName,lastAccessAt";
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();

    public SessionManager() {
        loadSessions();
    }

    public String createSession(User user) {
        // Remove any existing sessions for this user (single-session policy)
        invalidateUserSessions(user.getUserId());

        String token = SecurityUtil.randomToken(24);
        SessionContext context = new SessionContext(
            token, user.getUserId(), user.getRole(),
            user.getAccountId(), user.getFullName(),
            LocalDateTime.now()
        );
        sessions.put(token, context);
        saveSessions();
        return token;
    }

    public SessionContext requireSession(String token) {
        if (token == null || token.isEmpty()) {
            throw new AppException("Please login first.");
        }
        SessionContext context = sessions.get(token);
        if (context == null) {
            throw new AppException("Please login first.");
        }
        LocalDateTime now = LocalDateTime.now();
        if (context.getLastAccessAt().plusMinutes(EXPIRE_MINUTES).isBefore(now)) {
            sessions.remove(token);
            saveSessions();
            throw new AppException("Session expired. Please login again.");
        }
        context.setLastAccessAt(now);
        saveSessions();
        return context;
    }

    public SessionContext requireRole(String token, Role role) {
        SessionContext context = requireSession(token);
        if (context.getRole() != role) {
            throw new AppException("Permission denied. Required role: " + role);
        }
        return context;
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
            saveSessions();
        }
    }

    /**
     * Invalidate all sessions belonging to a specific user.
     * Used when admin disables a user or when enforcing single-session policy.
     */
    public void invalidateUserSessions(String userId) {
        List<String> tokensToRemove = new ArrayList<>();
        for (Map.Entry<String, SessionContext> entry : sessions.entrySet()) {
            if (entry.getValue().getUserId().equals(userId)) {
                tokensToRemove.add(entry.getKey());
            }
        }
        for (String t : tokensToRemove) {
            sessions.remove(t);
        }
        if (!tokensToRemove.isEmpty()) {
            saveSessions();
        }
    }

    /**
     * Get the number of currently active (non-expired) sessions.
     */
    public int getActiveSessionCount() {
        cleanExpiredSessions();
        return sessions.size();
    }

    /**
     * Check if a token is still valid without throwing exceptions.
     */
    public boolean isSessionValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        SessionContext context = sessions.get(token);
        if (context == null) {
            return false;
        }
        if (context.getLastAccessAt().plusMinutes(EXPIRE_MINUTES).isBefore(LocalDateTime.now())) {
            sessions.remove(token);
            saveSessions();
            return false;
        }
        return true;
    }

    /**
     * Get remaining session time in minutes.
     */
    public long getRemainingMinutes(String token) {
        if (token == null) return 0;
        SessionContext context = sessions.get(token);
        if (context == null) return 0;
        LocalDateTime expiry = context.getLastAccessAt().plusMinutes(EXPIRE_MINUTES);
        LocalDateTime now = LocalDateTime.now();
        if (expiry.isBefore(now)) return 0;
        return java.time.Duration.between(now, expiry).toMinutes();
    }

    /**
     * Get all valid (non-expired) sessions.
     */
    public java.util.Map<String, SessionContext> getAllValidSessions() {
        cleanExpiredSessions();
        return new java.util.LinkedHashMap<>(sessions);
    }

    private void cleanExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        boolean hasExpired = sessions.entrySet().removeIf(entry ->
            entry.getValue().getLastAccessAt().plusMinutes(EXPIRE_MINUTES).isBefore(now)
        );
        if (hasExpired) {
            saveSessions();
        }
    }

    private void saveSessions() {
        try {
            List<List<String>> rows = new ArrayList<>();
            for (SessionContext context : sessions.values()) {
                List<String> fields = new ArrayList<>();
                fields.add(context.getToken());
                fields.add(context.getUserId());
                fields.add(context.getRole().name());
                fields.add(context.getAccountId());
                fields.add(context.getFullName());
                fields.add(context.getLastAccessAt().format(DATETIME_FORMAT));
                rows.add(fields);
            }
            FileStorageUtil.writeRows(SESSIONS_FILE, SESSIONS_HEADER, rows);
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to save sessions: " + e.getMessage());
        }
    }

    private void loadSessions() {
        try {
            List<List<String>> rows = FileStorageUtil.readRows(SESSIONS_FILE, SESSIONS_HEADER);
            LocalDateTime now = LocalDateTime.now();
            for (List<String> row : rows) {
                if (row.size() >= 6) {
                    try {
                        String token = row.get(0);
                        String userId = row.get(1);
                        Role role = Role.valueOf(row.get(2));
                        String accountId = row.get(3);
                        String fullName = row.get(4);
                        LocalDateTime lastAccessAt = LocalDateTime.parse(row.get(5), DATETIME_FORMAT);

                        // Only load non-expired sessions
                        if (lastAccessAt.plusMinutes(EXPIRE_MINUTES).isAfter(now)) {
                            SessionContext context = new SessionContext(token, userId, role, accountId, fullName, lastAccessAt);
                            sessions.put(token, context);
                        }
                    } catch (Exception e) {
                        // Skip invalid session data
                        System.err.println("Skipping invalid session data: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // If file doesn't exist or can't be read, start with empty sessions
            System.err.println("Failed to load sessions: " + e.getMessage());
        }
    }
}
