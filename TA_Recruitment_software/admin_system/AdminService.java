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

public class AdminService {
    private final UserRepository userRepository;
    private final PositionRepository positionRepository;
    private final ApplicationRepository applicationRepository;
    private final SessionManager sessionManager;

    public AdminService(UserRepository userRepository,
                        PositionRepository positionRepository,
                        ApplicationRepository applicationRepository,
                        SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.positionRepository = positionRepository;
        this.applicationRepository = applicationRepository;
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

    public List<TaWorkloadSummary> listTaWorkloadSummary(String adminToken) {
        sessionManager.requireRole(adminToken, Role.ADMIN);

        List<TaWorkloadSummary> workloadSummaries = new ArrayList<>();
        Set<ApplicationStatus> acceptedStatuses = EnumSet.of(ApplicationStatus.APPROVED, ApplicationStatus.HIRED);

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
                Position position = positionRepository.findById(app.getPositionId()).orElse(null);
                if (position == null) {
                    continue;
                }
                assignedCount++;
                totalHours += parseWorkingHours(position.getWorkingHours());
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
