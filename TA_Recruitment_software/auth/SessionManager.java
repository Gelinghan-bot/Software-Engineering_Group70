package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.SecurityUtil;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final int EXPIRE_MINUTES = 30;
    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();

    public String createSession(User user) {
        String token = SecurityUtil.randomToken(24);
        SessionContext context = new SessionContext(token, user.getUserId(), user.getRole(), LocalDateTime.now());
        sessions.put(token, context);
        return token;
    }

    public SessionContext requireSession(String token) {
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
        sessions.remove(token);
    }
}
