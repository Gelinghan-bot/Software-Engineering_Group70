package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.model.Role;
import java.time.LocalDateTime;

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
