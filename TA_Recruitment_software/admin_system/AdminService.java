package TA_Recruitment_software.admin_system;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.IdGenerator;
import TA_Recruitment_software.admin_system.foundation.SecurityUtil;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import TA_Recruitment_software.admin_system.model.ApprovalStatus;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.TaWorkloadSummary;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.ApplicationRepository;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.auth.SessionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service layer for system administrator operations.
 * <p>
 * This class provides business logic for system administration tasks including
 * user management, account approval, password reset, and workload monitoring.
 * All operations require ADMIN role authentication.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>Seed default admin account on first run</li>
 *   <li>List and approve/reject pending user registrations</li>
 *   <li>Enable/disable user accounts</li>
 *   <li>Reset user passwords</li>
 *   <li>Update user information</li>
 *   <li>View TA/MO workload summaries and details</li>
 * </ul>
 * </p>
 *
 * @author Group70
 * @see User
 * @see SessionManager
 * @see TaWorkloadSummary
 */
public class AdminService {
    private final UserRepository userRepository;
    private final PositionRepository positionRepository;
    private final ApplicationRepository applicationRepository;
    private final SessionManager sessionManager;

    /**
     * Constructs an AdminService with required dependencies.
     *
     * @param userRepository          repository for user CRUD operations
     * @param positionRepository      repository for position queries
     * @param applicationRepository   repository for application queries
     * @param sessionManager          session manager for authentication
     */
    public AdminService(UserRepository userRepository,
                        PositionRepository positionRepository,
                        ApplicationRepository applicationRepository,
                        SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.positionRepository = positionRepository;
        this.applicationRepository = applicationRepository;
        this.sessionManager = sessionManager;
    }

    /**
     * Seeds a default admin account if it does not already exist.
     * <p>
     * Creates an admin user with account ID "admin" and default password "Admin@123".
     * This method is typically called during application startup to ensure at least
     * one admin account exists.
     * </p>
     */
    public void seedDefaultAdmin() {
        if (userRepository.findByAccountId("admin").isPresent()) {
            return;
        }
        User admin = new User();
        admin.setUserId(IdGenerator.nextId("ADM"));
        admin.setRole(Role.ADMIN);
        admin.setAccountId("admin");
        admin.setPasswordHash(SecurityUtil.sha256("Admin@123"));
        admin.setFullName("System Admin");
        admin.setStudentId("");
        admin.setDepartment("System");
        admin.setMajor("");
        admin.setEmail("admin@bupt.edu.cn");
        admin.setPhone("");
        admin.setSkills("");
        admin.setCvFilePath("");
        admin.setApprovalStatus(ApprovalStatus.APPROVED);
        admin.setEnabled(true);
        admin.setCreatedAt(LocalDateTime.now().toString());
        userRepository.save(admin);
    }

    /**
     * Lists all users with PENDING approval status.
     *
     * @param adminToken  valid session token (must be ADMIN role)
     * @return list of pending {@link User} entities awaiting approval
     * @throws AppException if ADMIN role required
     */
    public List<User> listPendingUsers(String adminToken) {
        sessionManager.requireRole(adminToken, Role.ADMIN);
        return userRepository.findByApprovalStatus(ApprovalStatus.PENDING);
    }

    /**
     * Lists all users in the system.
     *
     * @param adminToken  valid session token (must be ADMIN role)
     * @return list of all {@link User} entities
     * @throws AppException if ADMIN role required
     */
    public List<User> listAllUsers(String adminToken) {
        sessionManager.requireRole(adminToken, Role.ADMIN);
        return userRepository.findAll();
    }

    /**
     * Approves or rejects a pending user registration.
     * <p>
     * Admin accounts cannot be approved or rejected by this method.
     * </p>
     *
     * @param adminToken  valid session token (must be ADMIN role)
     * @param userId      the user to approve or reject
     * @param approved    true to approve, false to reject
     * @return the updated {@link User} entity
     * @throws AppException if ADMIN role required, user not found, or user is an admin
     */
    public User approveUser(String adminToken, String userId, boolean approved) {
        sessionManager.requireRole(adminToken, Role.ADMIN);
        User user = userRepository.findByUserId(ValidationUtil.requireNotBlank(userId, "User ID"))
            .orElseThrow(() -> new AppException("User not found."));

        if (user.getRole() == Role.ADMIN) {
            throw new AppException("Admin account cannot be approved/rejected.");
        }

        user.setApprovalStatus(approved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        userRepository.save(user);
        return user;
    }

    /**
     * Enables or disables a user account.
     * <p>
     * Disabled users cannot log in to the system.
     * </p>
     *
     * @param adminToken  valid session token (must be ADMIN role)
     * @param userId      the user to enable or disable
     * @param enabled     true to enable, false to disable
     * @return the updated {@link User} entity
     * @throws AppException if ADMIN role required or user not found
     */
    public User setUserEnabled(String adminToken, String userId, boolean enabled) {
        sessionManager.requireRole(adminToken, Role.ADMIN);
        User user = userRepository.findByUserId(ValidationUtil.requireNotBlank(userId, "User ID"))
            .orElseThrow(() -> new AppException("User not found."));
        user.setEnabled(enabled);
        userRepository.save(user);
        return user;
    }

    /**
     * Resets a user's password to a new value provided by the admin.
     * <p>
     * The new password is validated against password requirements and hashed
     * using SHA-256 before storage.
     * </p>
     *
     * @param adminToken    valid session token (must be ADMIN role)
     * @param userId        the user whose password to reset
     * @param newPassword   the new password (must meet complexity requirements)
     * @return the updated {@link User} entity
     * @throws AppException if ADMIN role required, validation fails, or user not found
     */
    public User resetPassword(String adminToken, String userId, String newPassword) {
        sessionManager.requireRole(adminToken, Role.ADMIN);
        ValidationUtil.validatePassword(newPassword);
        User user = userRepository.findByUserId(ValidationUtil.requireNotBlank(userId, "User ID"))
            .orElseThrow(() -> new AppException("User not found."));
        user.setPasswordHash(SecurityUtil.sha256(newPassword));
        userRepository.save(user);
        return user;
    }

    /**
     * Updates a user's personal information.
     * <p>
     * Only non-null, non-empty fields are updated. Email is validated for format.
     * </p>
     *
     * @param adminToken   valid session token (must be ADMIN role)
     * @param userId       the user to update
     * @param fullName     new full name (optional)
     * @param email        new email address (optional, validated)
     * @param phone        new phone number (optional)
     * @param major        new academic major (optional)
     * @param department   new department (optional)
     * @param skills       new skills description (optional)
     * @return the updated {@link User} entity
     * @throws AppException if ADMIN role required, validation fails, or user not found
     */
    public User updateUserInfo(String adminToken, String userId, String fullName, String email,
                               String phone, String major, String department, String skills) {
        sessionManager.requireRole(adminToken, Role.ADMIN);
        User user = userRepository.findByUserId(ValidationUtil.requireNotBlank(userId, "User ID"))
            .orElseThrow(() -> new AppException("User not found."));

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }
        if (email != null && !email.trim().isEmpty()) {
            ValidationUtil.validateEmail(email.trim(), true);
            user.setEmail(email.trim());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            user.setPhone(phone.trim());
        }
        if (major != null && !major.trim().isEmpty()) {
            user.setMajor(major.trim());
        }
        if (department != null && !department.trim().isEmpty()) {
            user.setDepartment(department.trim());
        }
        if (skills != null) {
            user.setSkills(skills.trim());
        }

        userRepository.save(user);
        return user;
    }

    /**
     * Gets the work count for a user based on their role.
     * <p>
     * For MO users: returns the number of published positions.
     * For TA users: returns the number of approved/hired/offered applications.
     * </p>
     * <p>
     * Note: This method does not perform admin authentication; the caller must
     * already have admin authentication.
     * </p>
     *
     * @param userId  the user ID to query
     * @return work count (published positions for MO, accepted applications for TA)
     */
    public int getUserWorkCount(String userId) {
        User u = userRepository.findByUserId(userId).orElse(null);
        if (u == null) return 0;
        if (u.getRole() == Role.MO) {
            return positionRepository.findByPublisher(userId).size();
        }
        if (u.getRole() == Role.TA) {
            int count = 0;
            Set<ApplicationStatus> accepted = EnumSet.of(ApplicationStatus.APPROVED, ApplicationStatus.HIRED, ApplicationStatus.OFFERED);
            for (Application app : applicationRepository.findByApplicant(userId)) {
                if (accepted.contains(app.getStatus())) count++;
            }
            return count;
        }
        return 0;
    }

    /**
     * Generates a workload summary for all TA users in the system.
     * <p>
     * For each TA, calculates the number of assigned positions and total estimated
     * working hours based on the job types of accepted applications.
     * </p>
     *
     * @param adminToken  valid session token (must be ADMIN role)
     * @return list of {@link TaWorkloadSummary} for all TAs
     * @throws AppException if ADMIN role required
     */
    public List<TaWorkloadSummary> listTaWorkloadSummary(String adminToken) {
        sessionManager.requireRole(adminToken, Role.ADMIN);

        List<TaWorkloadSummary> workloadSummaries = new ArrayList<>();
        Set<ApplicationStatus> acceptedStatuses = EnumSet.of(ApplicationStatus.APPROVED, ApplicationStatus.HIRED, ApplicationStatus.OFFERED);

        for (User ta : userRepository.findAll()) {
            if (ta.getRole() != Role.TA) {
                continue;
            }

            int assignedCount = 0;
            double totalHours = 0.0;
            for (Application app : applicationRepository.findByApplicant(ta.getUserId())) {
                if (!acceptedStatuses.contains(app.getStatus())) {
                    continue;
                }
                assignedCount++;
                Position position = positionRepository.findById(app.getPositionId()).orElse(null);
                if (position != null) {
                    totalHours += parseWorkingHours(position.getJobType());
                }
            }

            workloadSummaries.add(new TaWorkloadSummary(
                ta.getUserId(),
                ta.getAccountId(),
                ta.getFullName(),
                assignedCount,
                totalHours
            ));
        }

        return workloadSummaries;
    }

    /**
     * Gets detailed work information for a specific TA user.
     * <p>
     * Returns a list of position details including position ID, title, location,
     * job type, and application status for all accepted applications of the TA.
     * </p>
     *
     * @param adminToken  valid session token (must be ADMIN role)
     * @param userId      the TA user ID
     * @return list of String arrays containing position details
     * @throws AppException if ADMIN role required, user not found, or user is not a TA
     */
    public List<String[]> getTaWorkDetail(String adminToken, String userId) {
        sessionManager.requireRole(adminToken, Role.ADMIN);
        User ta = userRepository.findByUserId(userId)
            .orElseThrow(() -> new AppException("User not found."));
        if (ta.getRole() != Role.TA) {
            throw new AppException("User is not a TA.");
        }

        List<String[]> result = new ArrayList<>();
        Set<ApplicationStatus> acceptedStatuses = EnumSet.of(ApplicationStatus.APPROVED, ApplicationStatus.HIRED, ApplicationStatus.OFFERED);

        for (Application app : applicationRepository.findByApplicant(userId)) {
            if (!acceptedStatuses.contains(app.getStatus())) {
                continue;
            }
            Position position = positionRepository.findById(app.getPositionId()).orElse(null);
            result.add(new String[]{
                app.getPositionId(),
                position != null ? position.getJobTitle() : "(Deleted Position)",
                position != null ? nvl(position.getInterviewLocation(), "N/A") : "N/A",
                position != null && position.getJobType() != null ? position.getJobType() : "N/A",
                app.getStatus().name()
            });
        }
        return result;
    }

    /**
     * Gets detailed work information for a specific MO user.
     * <p>
     * Returns a list of position details including position ID, title, status,
     * headcount, deadline, and interview location for all positions published by the MO.
     * </p>
     *
     * @param adminToken  valid session token (must be ADMIN role)
     * @param userId      the MO user ID
     * @return list of String arrays containing position details
     * @throws AppException if ADMIN role required, user not found, or user is not an MO
     */
    public List<String[]> getMoWorkDetail(String adminToken, String userId) {
        sessionManager.requireRole(adminToken, Role.ADMIN);
        User mo = userRepository.findByUserId(userId)
            .orElseThrow(() -> new AppException("User not found."));
        if (mo.getRole() != Role.MO) {
            throw new AppException("User is not an MO.");
        }

        List<String[]> result = new ArrayList<>();
        for (Position position : positionRepository.findByPublisher(userId)) {
            result.add(new String[]{
                position.getPositionId(),
                position.getJobTitle(),
                position.getStatus().name(),
                String.valueOf(position.getHeadcount()),
                nvl(position.getDeadline(), "N/A"),
                nvl(position.getInterviewLocation(), "N/A")
            });
        }
        return result;
    }

    /**
     * Returns the value if not null or empty, otherwise returns the default value.
     *
     * @param value         the value to check
     * @param defaultValue  the default value to return if value is null or empty
     * @return the value or default value
     */
    private String nvl(String value, String defaultValue) {
        return (value == null || value.trim().isEmpty()) ? defaultValue : value;
    }

    /**
     * Parses a numeric working hours value from a job type string.
     * <p>
     * Extracts the first number found in the string (e.g., "10 hours" → 10.0).
     * </p>
     *
     * @param workingHours  the job type string containing hours information
     * @return parsed hours value, or 0.0 if no number found
     */
    private double parseWorkingHours(String workingHours) {
        if (workingHours == null || workingHours.trim().isEmpty()) {
            return 0.0;
        }
        Matcher matcher = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)").matcher(workingHours.trim());
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return 0.0;
            }
        }
        return 0.0;
    }
}
