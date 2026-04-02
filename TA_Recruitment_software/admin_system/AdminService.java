package TA_Recruitment_software.admin_system;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.IdGenerator;
import TA_Recruitment_software.admin_system.foundation.SecurityUtil;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.ApprovalStatus;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.auth.SessionManager;
import java.time.LocalDateTime;
import java.util.List;

public class AdminService {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public AdminService(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

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

    public List<User> listPendingUsers(String adminToken) {
        sessionManager.requireRole(adminToken, Role.ADMIN);
        return userRepository.findByApprovalStatus(ApprovalStatus.PENDING);
    }

    public List<User> listAllUsers(String adminToken) {
        sessionManager.requireRole(adminToken, Role.ADMIN);
        return userRepository.findAll();
    }

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

    public User setUserEnabled(String adminToken, String userId, boolean enabled) {
        sessionManager.requireRole(adminToken, Role.ADMIN);
        User user = userRepository.findByUserId(ValidationUtil.requireNotBlank(userId, "User ID"))
            .orElseThrow(() -> new AppException("User not found."));
        user.setEnabled(enabled);
        userRepository.save(user);
        return user;
    }

    public User resetPassword(String adminToken, String userId, String newPassword) {
        sessionManager.requireRole(adminToken, Role.ADMIN);
        ValidationUtil.validatePassword(newPassword);
        User user = userRepository.findByUserId(ValidationUtil.requireNotBlank(userId, "User ID"))
            .orElseThrow(() -> new AppException("User not found."));
        user.setPasswordHash(SecurityUtil.sha256(newPassword));
        userRepository.save(user);
        return user;
    }
}
