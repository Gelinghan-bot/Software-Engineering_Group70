package TA_Recruitment_software.admin_system.model;

/**
 * Represents a user account in the TA Recruitment System.
 * <p>
 * This entity stores all user information including authentication credentials,
 * personal details, and account status. Users can have one of three roles:
 * {@link Role#TA} (Teaching Assistant), {@link Role#MO} (Module Organizer),
 * or {@link Role#ADMIN} (System Administrator).
 * </p>
 * <p>
 * Key characteristics:
 * <ul>
 *   <li>Each user has a unique {@code userId} generated with a role-based prefix</li>
 *   <li>Passwords are stored as SHA-256 hashes, never in plain text</li>
 *   <li>New registrations start with {@link ApprovalStatus#PENDING} and require admin approval</li>
 *   <li>TA users have {@code major}, {@code skills}, and {@code cvFilePath} fields</li>
 *   <li>MO users have {@code department} instead of {@code major}</li>
 *   <li>Disabled users ({@code enabled=false}) cannot log in</li>
 * </ul>
 * </p>
 *
 * @author Group70
 * @see Role
 * @see ApprovalStatus
 */
public class User {
    /** Unique identifier for this user (e.g., "TA000001", "MO000001", "ADM000001"). */
    private String userId;

    /** The user's role determining their permissions in the system. */
    private Role role;

    /** Login account ID (unique, used for authentication). */
    private String accountId;

    /** SHA-256 hash of the user's password (never stored in plain text). */
    private String passwordHash;

    /** User's full display name. */
    private String fullName;

    /** Student ID number (for TA users). */
    private String studentId;

    /** Department name (for MO users). */
    private String department;

    /** Academic major (for TA users). */
    private String major;

    /** Email address (validated format). */
    private String email;

    /** Phone number (validated format). */
    private String phone;

    /** Skills and expertise description (for TA users). */
    private String skills;

    /** Path to the stored CV file (for TA users). */
    private String cvFilePath;

    /** Account approval status (PENDING, APPROVED, or REJECTED). */
    private ApprovalStatus approvalStatus;

    /** Whether the account is enabled for login. */
    private boolean enabled;

    /** Timestamp when the account was created. */
    private String createdAt;

    /**
     * Gets the unique user identifier.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the unique user identifier.
     *
     * @param userId the user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the user's role.
     *
     * @return the {@link Role} enum value
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role the {@link Role} to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Gets the login account ID.
     *
     * @return the account ID used for authentication
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Sets the login account ID.
     *
     * @param accountId the account ID to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * Gets the SHA-256 hashed password.
     *
     * @return the password hash (not the plain text password)
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the SHA-256 hashed password.
     * <p>
     * Note: Always use {@link TA_Recruitment_software.admin_system.foundation.SecurityUtil#sha256(String)}
     * to hash the password before setting.
     * </p>
     *
     * @param passwordHash the hashed password to set
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Gets the user's full display name.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the user's full display name.
     *
     * @param fullName the full name to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the student ID (for TA users).
     *
     * @return the student ID
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Sets the student ID (for TA users).
     *
     * @param studentId the student ID to set
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    /**
     * Gets the department name (for MO users).
     *
     * @return the department
     */
    public String getDepartment() {
        return department;
    }

    /**
     * Sets the department name (for MO users).
     *
     * @param department the department to set
     */
    public void setDepartment(String department) {
        this.department = department;
    }

    /**
     * Gets the academic major (for TA users).
     *
     * @return the major
     */
    public String getMajor() {
        return major;
    }

    /**
     * Sets the academic major (for TA users).
     *
     * @param major the major to set
     */
    public void setMajor(String major) {
        this.major = major;
    }

    /**
     * Gets the email address.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     *
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the phone number.
     *
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number.
     *
     * @param phone the phone number to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the skills description (for TA users).
     *
     * @return the skills
     */
    public String getSkills() {
        return skills;
    }

    /**
     * Sets the skills description (for TA users).
     *
     * @param skills the skills to set
     */
    public void setSkills(String skills) {
        this.skills = skills;
    }

    /**
     * Gets the path to the stored CV file (for TA users).
     *
     * @return the CV file path
     */
    public String getCvFilePath() {
        return cvFilePath;
    }

    /**
     * Sets the path to the stored CV file (for TA users).
     *
     * @param cvFilePath the CV file path to set
     */
    public void setCvFilePath(String cvFilePath) {
        this.cvFilePath = cvFilePath;
    }

    /**
     * Gets the account approval status.
     *
     * @return the {@link ApprovalStatus} enum value
     */
    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    /**
     * Sets the account approval status.
     *
     * @param approvalStatus the {@link ApprovalStatus} to set
     */
    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    /**
     * Checks if the account is enabled for login.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the account is enabled.
     * <p>
     * Disabled accounts cannot log in even if approved.
     * </p>
     *
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the account creation timestamp.
     *
     * @return the creation time string
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the account creation timestamp.
     *
     * @param createdAt the creation time to set
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{"
            + "userId='" + userId + '\''
            + ", role=" + role
            + ", accountId='" + accountId + '\''
            + ", fullName='" + fullName + '\''
            + ", approvalStatus=" + approvalStatus
            + ", enabled=" + enabled
            + '}';
    }
}
