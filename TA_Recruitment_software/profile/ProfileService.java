package TA_Recruitment_software.profile;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.auth.SessionContext;
import TA_Recruitment_software.auth.SessionManager;

/**
 * Service layer for user profile management operations.
 * <p>
 * This class provides business logic for viewing and updating user profiles,
 * as well as CV file management. Different profile fields are available for
 * TA and MO roles.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>View current user's profile (TA and MO roles)</li>
 *   <li>TA profile update: major, email, phone, skills</li>
 *   <li>MO profile update: full name, department, email, phone</li>
 *   <li>CV file upload and storage for TA users</li>
 * </ul>
 * </p>
 *
 * @author Group70
 * @see User
 * @see SessionManager
 * @see CvStorageService
 */
public class ProfileService {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final CvStorageService cvStorageService;

    /**
     * Constructs a ProfileService with required dependencies.
     *
     * @param userRepository  repository for user CRUD operations
     * @param sessionManager  session manager for authentication
     */
    public ProfileService(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
        this.cvStorageService = new CvStorageService();
    }

    /**
     * Retrieves the current user's profile information.
     * <p>
     * Only TA and MO roles can access this feature.
     * </p>
     *
     * @param token  valid session token
     * @return the current user's {@link User} entity
     * @throws AppException if permission denied or user not found
     */
    public User getMyProfile(String token) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.TA && session.getRole() != Role.MO) {
            throw new AppException("Permission denied. Only TA and MO can access this feature.");
        }
        return userRepository.findByUserId(session.getUserId())
            .orElseThrow(() -> new AppException("User not found."));
    }

    /**
     * Updates the TA user's profile information.
     * <p>
     * Updatable fields: major, email, phone, skills.
     * Email and phone are validated for format correctness.
     * All fields are sanitized to remove potentially harmful characters.
     * </p>
     *
     * @param token   valid session token (must be TA role)
     * @param major   academic major (max 80 chars)
     * @param email   email address (validated format)
     * @param phone   phone number (validated format)
     * @param skills  skills and expertise description (max 200 chars)
     * @return the updated {@link User} entity
     * @throws AppException if TA role required, validation fails, or user not found
     */
    public User updateProfile(String token, String major, String email, String phone, String skills) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        User user = userRepository.findByUserId(session.getUserId())
            .orElseThrow(() -> new AppException("User not found."));

        user.setMajor(ValidationUtil.sanitizeText(major, "Major", 80));
        user.setEmail(ValidationUtil.validateEmail(email, true));
        user.setPhone(ValidationUtil.validatePhone(phone, true));
        user.setSkills(ValidationUtil.sanitizeText(skills, "Skills", 200));
        userRepository.save(user);
        return user;
    }

    /**
     * Updates the MO user's profile information.
     * <p>
     * MO profiles have different fields than TA profiles:
     * <ul>
     *   <li>Full name (optional update)</li>
     *   <li>Department (instead of major)</li>
     *   <li>Email and phone</li>
     *   <li>No skills or CV fields</li>
     * </ul>
     * </p>
     *
     * @param token       valid session token (must be MO role)
     * @param fullName    full name (optional, max 80 chars)
     * @param department  department name (max 80 chars)
     * @param email       email address (validated format)
     * @param phone       phone number (validated format)
     * @return the updated {@link User} entity
     * @throws AppException if MO role required, validation fails, or user not found
     */
    public User updateMOProfile(String token, String fullName, String department, String email, String phone) {
        SessionContext session = sessionManager.requireRole(token, Role.MO);
        User user = userRepository.findByUserId(session.getUserId())
            .orElseThrow(() -> new AppException("User not found."));

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(ValidationUtil.sanitizeText(fullName.trim(), "Full Name", 80));
        }
        user.setDepartment(ValidationUtil.sanitizeText(department, "Department", 80));
        user.setEmail(ValidationUtil.validateEmail(email, true));
        user.setPhone(ValidationUtil.validatePhone(phone, true));
        userRepository.save(user);
        return user;
    }

    /**
     * Uploads a CV file for the current TA user.
     * <p>
     * The CV file is stored using {@link CvStorageService} with a unique filename
     * based on the user ID. The stored file path is saved to the user's profile.
     * </p>
     *
     * @param token       valid session token (must be TA role)
     * @param cvFilePath  path to the CV file to upload
     * @return the updated {@link User} entity with CV file path
     * @throws AppException if TA role required, file path invalid, or user not found
     */
    public User uploadCV(String token, String cvFilePath) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        User user = userRepository.findByUserId(session.getUserId())
            .orElseThrow(() -> new AppException("User not found."));

        String sourcePath = ValidationUtil.requireNotBlank(cvFilePath, "CV file path");
        String storedPath = cvStorageService.storeCvFile(user.getUserId(), sourcePath);
        user.setCvFilePath(storedPath);
        userRepository.save(user);
        return user;
    }
}
