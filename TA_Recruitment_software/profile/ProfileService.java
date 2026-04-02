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

    public ProfileService(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
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

    public User uploadCV(String token, String cvFilePath) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        User user = userRepository.findByUserId(session.getUserId())
            .orElseThrow(() -> new AppException("User not found."));

        String path = ValidationUtil.requireNotBlank(cvFilePath, "CV file path");
        String lower = path.toLowerCase();
        if (!(lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx"))) {
            throw new AppException("CV must be .pdf, .doc or .docx");
        }
        user.setCvFilePath(path);
        userRepository.save(user);
        return user;
    }
}
