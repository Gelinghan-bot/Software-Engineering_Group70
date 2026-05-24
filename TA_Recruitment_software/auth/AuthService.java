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

/**
 * Service layer for user authentication and account management operations.
 * <p>
 * This class provides business logic for user registration, login, logout,
 * password management, and session handling. It supports two user roles:
 * Teaching Assistant (TA) and Module Organizer (MO).
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>TA and MO account registration with PENDING approval status</li>
 *   <li>Login with password verification and approval status check</li>
 *   <li>Session management with token-based authentication</li>
 *   <li>Password change with old password verification</li>
 *   <li>Session timeout tracking</li>
 * </ul>
 * </p>
 * <p>
 * All newly registered accounts must be approved by an admin before login.
 * Passwords must meet complexity requirements: at least 8 characters,
 * containing uppercase letter, lowercase letter, and digit.
 * </p>
 *
 * @author Group70
 * @see User
 * @see SessionManager
 * @see UserRepository
 */
public class AuthService {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    /**
     * Constructs an AuthService with required dependencies.
     *
     * @param userRepository  repository for user CRUD operations
     * @param sessionManager  session manager for token-based authentication
     */
    public AuthService(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    /**
     * Registers a new Teaching Assistant (TA) account.
     * <p>
     * The account is created with PENDING approval status and must be approved
     * by an admin before the user can log in.
     * </p>
     *
     * @param accountId         unique account identifier (validated format)
     * @param password          account password (must meet complexity requirements)
     * @param confirmPassword   password confirmation (must match password)
     * @param fullName          user's full name
     * @param studentId         student ID number
     * @param major             academic major
     * @param email             email address (validated format)
     * @param phone             phone number (validated format)
     * @return the newly created {@link User} entity with PENDING approval status
     * @throws AppException if validation fails or account ID already exists
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

    /**
     * Backward-compatible overload for TA registration without confirmPassword parameter.
     * <p>
     * Delegates to {@link #registerTA(String, String, String, String, String, String, String, String)}
     * using password as confirmPassword.
     * </p>
     *
     * @param accountId   unique account identifier
     * @param password    account password
     * @param fullName    user's full name
     * @param studentId   student ID number
     * @param major       academic major
     * @param email       email address
     * @param phone       phone number
     * @return the newly created {@link User} entity
     */
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
     * Registers a new Module Organizer (MO) account.
     * <p>
     * The account is created with PENDING approval status and must be approved
     * by an admin before the user can log in.
     * </p>
     *
     * @param accountId         unique account identifier (validated format)
     * @param password          account password (must meet complexity requirements)
     * @param confirmPassword   password confirmation (must match password)
     * @param fullName          user's full name
     * @param department        department name
     * @param email             email address (validated format)
     * @param phone             phone number (validated format)
     * @return the newly created {@link User} entity with PENDING approval status
     * @throws AppException if validation fails or account ID already exists
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

    /**
     * Backward-compatible overload for MO registration without confirmPassword parameter.
     * <p>
     * Delegates to {@link #registerMO(String, String, String, String, String, String, String)}
     * using password as confirmPassword.
     * </p>
     *
     * @param accountId   unique account identifier
     * @param password    account password
     * @param fullName    user's full name
     * @param department  department name
     * @param email       email address
     * @param phone       phone number
     * @return the newly created {@link User} entity
     */
    public User registerMO(String accountId, String password, String fullName, String department, String email, String phone) {
        return registerMO(accountId, password, password, fullName, department, email, phone);
    }

    /**
     * Authenticates a user with account ID and password.
     * <p>
     * Performs the following checks in order:
     * <ol>
     *   <li>Account exists in the system</li>
     *   <li>Account is enabled (not disabled by admin)</li>
     *   <li>Account approval status is APPROVED (not PENDING or REJECTED)</li>
     *   <li>Password matches the stored hash</li>
     * </ol>
     * On success, creates a new session and returns a session token.
     * </p>
     *
     * @param accountId  the user's account ID
     * @param password   the user's password
     * @return session token for authenticated access
     * @throws AppException if account not found, disabled, pending, rejected, or password incorrect
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
        LoginHistoryStore.addAccount(checkedAccountId);
        return token;
    }

    /**
     * Logs out the current session, invalidating the session token.
     *
     * @param token  the session token to invalidate
     */
    public void logout(String token) {
        sessionManager.logout(token);
    }

    /**
     * Retrieves the current user from a valid session token.
     *
     * @param token  valid session token
     * @return the {@link User} entity associated with the session
     * @throws AppException if session invalid or user not found
     */
    public User getUserByToken(String token) {
        SessionContext ctx = sessionManager.requireSession(token);
        return userRepository.findByUserId(ctx.getUserId())
            .orElseThrow(() -> new AppException("User not found."));
    }

    /**
     * Changes the password for the currently logged-in user.
     * <p>
     * Performs the following checks:
     * <ol>
     *   <li>Old password matches the stored hash</li>
     *   <li>New password meets complexity requirements</li>
     *   <li>New password confirmation matches new password</li>
     *   <li>New password differs from old password</li>
     * </ol>
     * On success, invalidates all existing sessions for security.
     * </p>
     *
     * @param token                  valid session token
     * @param oldPassword            current password for verification
     * @param newPassword            new password to set
     * @param confirmNewPassword     new password confirmation
     * @throws AppException if session invalid, old password incorrect, or validation fails
     */
    public void changePassword(String token, String oldPassword, String newPassword, String confirmNewPassword) {
        SessionContext ctx = sessionManager.requireSession(token);
        User user = userRepository.findByUserId(ctx.getUserId())
            .orElseThrow(() -> new AppException("User not found."));

        if (!SecurityUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
            throw new AppException("Current password is incorrect.");
        }

        validatePasswordWithConfirm(newPassword, confirmNewPassword);

        if (SecurityUtil.verifyPassword(newPassword, user.getPasswordHash())) {
            throw new AppException("New password must be different from current password.");
        }

        user.setPasswordHash(SecurityUtil.sha256(newPassword));
        userRepository.save(user);

        sessionManager.invalidateUserSessions(user.getUserId());
    }

    /**
     * Checks if a session token is still valid.
     *
     * @param token  the session token to check
     * @return true if the session is valid, false otherwise
     */
    public boolean isLoggedIn(String token) {
        return sessionManager.isSessionValid(token);
    }

    /**
     * Gets remaining session time in minutes.
     *
     * @param token  the session token
     * @return remaining minutes before session expires
     */
    public long getSessionRemainingMinutes(String token) {
        return sessionManager.getRemainingMinutes(token);
    }

    /**
     * Ensures that an account ID does not already exist in the system.
     *
     * @param accountId  the account ID to check
     * @throws AppException if account ID already exists
     */
    private void ensureAccountNotExist(String accountId) {
        if (userRepository.findByAccountId(accountId).isPresent()) {
            throw new AppException("Account ID already exists.");
        }
    }

    /**
     * Validates password strength and confirms it matches the confirmation.
     * <p>
     * Password requirements:
     * <ul>
     *   <li>At least 8 characters long</li>
     *   <li>Contains at least one uppercase letter</li>
     *   <li>Contains at least one lowercase letter</li>
     *   <li>Contains at least one digit</li>
     * </ul>
     * </p>
     *
     * @param password          the password to validate
     * @param confirmPassword   the confirmation password
     * @throws AppException if passwords don't match or complexity requirements not met
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
