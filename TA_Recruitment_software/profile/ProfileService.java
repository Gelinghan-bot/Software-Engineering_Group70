package TA_Recruitment_software.profile;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.UserRepository;
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
        return requireTaUser(token);
    }

    public ProfileCompleteness getCompleteness(String token) {
        return ProfileCompleteness.fromUser(requireTaUser(token));
    }

    public User updateProfile(String token, String major, String email, String phone, String skills) {
        User user = requireTaUser(token);

        user.setMajor(ValidationUtil.sanitizeText(major, "Major", ProfileConstants.MAX_MAJOR_LEN));
        user.setEmail(ValidationUtil.validateEmail(email, true));
        user.setPhone(ValidationUtil.validatePhone(phone, true));
        user.setSkills(ValidationUtil.sanitizeText(skills, "Skills", ProfileConstants.MAX_SKILLS_LEN));
        userRepository.save(user);
        return user;
    }

    public User uploadCV(String token, String cvFilePath) {
        User user = requireTaUser(token);

        String sourcePath = ValidationUtil.requireNotBlank(cvFilePath, "CV file path");
        String storedPath = cvStorageService.storeCvFile(user.getUserId(), sourcePath);
        if (!CvPathHelper.existsOnDisk(storedPath)) {
            throw new AppException("CV was stored but could not be verified on disk. Please try again.");
        }
        user.setCvFilePath(storedPath);
        userRepository.save(user);
        return user;
    }

    private User requireTaUser(String token) {
        String userId = sessionManager.requireRole(token, Role.TA).getUserId();
        return userRepository.findByUserId(userId)
            .orElseThrow(() -> new AppException("User not found."));
    }
}
