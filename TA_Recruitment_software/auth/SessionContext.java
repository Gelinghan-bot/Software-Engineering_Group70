package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.model.Role;
import java.time.LocalDateTime;

public class SessionContext {
    private final String token;
    private final String userId;
    private final Role role;
    private LocalDateTime lastAccessAt;

    public SessionContext(String token, String userId, Role role, LocalDateTime lastAccessAt) {
        this.token = token;
        this.userId = userId;
        this.role = role;
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

    public LocalDateTime getLastAccessAt() {
        return lastAccessAt;
    }

    public void setLastAccessAt(LocalDateTime lastAccessAt) {
        this.lastAccessAt = lastAccessAt;
    }
}
