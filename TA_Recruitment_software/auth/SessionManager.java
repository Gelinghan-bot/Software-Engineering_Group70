package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.SecurityUtil;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final int EXPIRE_MINUTES = 30;
    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();

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
            throw new AppException("Session expired. Please login again.");
        }
        context.setLastAccessAt(now);
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

    private void cleanExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        sessions.entrySet().removeIf(entry ->
            entry.getValue().getLastAccessAt().plusMinutes(EXPIRE_MINUTES).isBefore(now)
        );
    }
}
