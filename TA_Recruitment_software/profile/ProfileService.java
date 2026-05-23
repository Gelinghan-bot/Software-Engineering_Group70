package TA_Recruitment_software.profile;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.auth.SessionContext;
import TA_Recruitment_software.auth.SessionManager;

public class ProfileService {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final CvStorageService cvStorageService;

    public ProfileService(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
        this.cvStorageService = new CvStorageService();
    }

    public User getMyProfile(String token) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.TA && session.getRole() != Role.MO) {
            throw new AppException("Permission denied. Only TA and MO can access this feature.");
        }
        return userRepository.findByUserId(session.getUserId())
            .orElseThrow(() -> new AppException("User not found."));
    }

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

    /** MO profile update — department instead of major, no skills/CV. */
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
