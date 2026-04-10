package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.IdGenerator;
import TA_Recruitment_software.admin_system.foundation.SecurityUtil;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.ApprovalStatus;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import java.time.LocalDateTime;

public class AuthService {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public AuthService(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    /**
     * Register a new Teaching Assistant account.
     * The account is created with PENDING approval status and must be approved by admin before login.
     */
    public User registerTA(
        String accountId,
        String password,
        String confirmPassword,
        String fullName,
        String studentId,
        String major,
        String email,
        String phone
    ) {
        String checkedAccountId = ValidationUtil.validateAccountId(accountId);
        validatePasswordWithConfirm(password, confirmPassword);
        ValidationUtil.validateName(fullName, "Full name");
        ValidationUtil.requireNotBlank(studentId, "Student ID");
        ValidationUtil.requireNotBlank(major, "Major");
        ValidationUtil.validateEmail(email, true);
        ValidationUtil.validatePhone(phone, true);

        ensureAccountNotExist(checkedAccountId);

        User ta = new User();
        ta.setUserId(IdGenerator.nextId("TA"));
        ta.setRole(Role.TA);
        ta.setAccountId(checkedAccountId);
        ta.setPasswordHash(SecurityUtil.sha256(password));
        ta.setFullName(fullName.trim());
        ta.setStudentId(studentId.trim());
        ta.setMajor(major.trim());
        ta.setEmail(email.trim());
        ta.setPhone(phone.trim());
        ta.setSkills("");
        ta.setCvFilePath("");
        ta.setApprovalStatus(ApprovalStatus.PENDING);
        ta.setEnabled(true);
        ta.setCreatedAt(LocalDateTime.now().toString());

        userRepository.save(ta);
        return ta;
    }

    // Backward-compatible overload without confirmPassword
    public User registerTA(
        String accountId,
        String password,
        String fullName,
        String studentId,
        String major,
        String email,
        String phone
    ) {
        return registerTA(accountId, password, password, fullName, studentId, major, email, phone);
    }

    /**
     * Register a new Module Officer account.
     * The account is created with PENDING approval status and must be approved by admin before login.
     */
    public User registerMO(
        String accountId,
        String password,
        String confirmPassword,
        String fullName,
        String department,
        String email,
        String phone
    ) {
        String checkedAccountId = ValidationUtil.validateAccountId(accountId);
        validatePasswordWithConfirm(password, confirmPassword);
        ValidationUtil.validateName(fullName, "Full name");
        ValidationUtil.requireNotBlank(department, "Department");
        ValidationUtil.validateEmail(email, true);
        ValidationUtil.validatePhone(phone, true);

        ensureAccountNotExist(checkedAccountId);

        User mo = new User();
        mo.setUserId(IdGenerator.nextId("MO"));
        mo.setRole(Role.MO);
        mo.setAccountId(checkedAccountId);
        mo.setPasswordHash(SecurityUtil.sha256(password));
        mo.setFullName(fullName.trim());
        mo.setDepartment(department.trim());
        mo.setEmail(email.trim());
        mo.setPhone(phone.trim());
        mo.setSkills("");
        mo.setCvFilePath("");
        mo.setApprovalStatus(ApprovalStatus.PENDING);
        mo.setEnabled(true);
        mo.setCreatedAt(LocalDateTime.now().toString());

        userRepository.save(mo);
        return mo;
    }

    // Backward-compatible overload without confirmPassword
    public User registerMO(String accountId, String password, String fullName, String department, String email, String phone) {
        return registerMO(accountId, password, password, fullName, department, email, phone);
    }

    /**
     * Authenticate user with account ID and password.
     * Checks account enabled status and admin approval before granting access.
     * Returns a session token on success.
     */
    public String login(String accountId, String password) {
        String checkedAccountId = ValidationUtil.validateAccountId(accountId);
        ValidationUtil.requireNotBlank(password, "Password");

        User user = userRepository.findByAccountId(checkedAccountId)
            .orElseThrow(() -> new AppException("Invalid account or password."));

        if (!user.isEnabled()) {
            throw new AppException("Account is disabled. Please contact admin.");
        }
        if (user.getApprovalStatus() == ApprovalStatus.REJECTED) {
            throw new AppException("Account registration was rejected. Please contact admin.");
        }
        if (user.getApprovalStatus() == ApprovalStatus.PENDING) {
            throw new AppException("Account is pending approval. Please wait for admin to approve your registration.");
        }
        if (!SecurityUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new AppException("Invalid account or password.");
        }
        String token = sessionManager.createSession(user);
        // Save successful login to history
        LoginHistoryStore.addAccount(checkedAccountId);
        return token;
    }

    /**
     * Logout the current session, invalidating the token.
     */
    public void logout(String token) {
        sessionManager.logout(token);
    }

    /**
     * Retrieve the current user from a valid session token.
     */
    public User getUserByToken(String token) {
        SessionContext ctx = sessionManager.requireSession(token);
        return userRepository.findByUserId(ctx.getUserId())
            .orElseThrow(() -> new AppException("User not found."));
    }

    /**
     * Change password for the currently logged-in user.
     * Requires old password verification and new password confirmation.
     */
    public void changePassword(String token, String oldPassword, String newPassword, String confirmNewPassword) {
        SessionContext ctx = sessionManager.requireSession(token);
        User user = userRepository.findByUserId(ctx.getUserId())
            .orElseThrow(() -> new AppException("User not found."));

        // Verify old password
        if (!SecurityUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
            throw new AppException("Current password is incorrect.");
        }

        // Validate new password
        validatePasswordWithConfirm(newPassword, confirmNewPassword);

        // Ensure new password differs from old
        if (SecurityUtil.verifyPassword(newPassword, user.getPasswordHash())) {
            throw new AppException("New password must be different from current password.");
        }

        user.setPasswordHash(SecurityUtil.sha256(newPassword));
        userRepository.save(user);

        // Invalidate all existing sessions for security, then create a new one
        sessionManager.invalidateUserSessions(user.getUserId());
    }

    /**
     * Check if a session token is still valid.
     */
    public boolean isLoggedIn(String token) {
        return sessionManager.isSessionValid(token);
    }

    /**
     * Get remaining session time in minutes.
     */
    public long getSessionRemainingMinutes(String token) {
        return sessionManager.getRemainingMinutes(token);
    }

    private void ensureAccountNotExist(String accountId) {
        if (userRepository.findByAccountId(accountId).isPresent()) {
            throw new AppException("Account ID already exists.");
        }
    }

    /**
     * Validate password strength and confirm match.
     * Requirements: at least 8 chars, contains uppercase, lowercase, and digit.
     */
    private void validatePasswordWithConfirm(String password, String confirmPassword) {
        ValidationUtil.validatePassword(password);

        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new AppException("Passwords do not match.");
        }

        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new AppException("Password must contain at least one uppercase letter, one lowercase letter, and one digit.");
        }
    }
}
