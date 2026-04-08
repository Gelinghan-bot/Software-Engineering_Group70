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

    public User registerTA(
        String accountId,
        String password,
        String fullName,
        String studentId,
        String major,
        String email,
        String phone
    ) {
        String checkedAccountId = ValidationUtil.validateAccountId(accountId);
        ValidationUtil.validatePassword(password);
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
        ta.setApprovalStatus(ApprovalStatus.APPROVED);
        ta.setEnabled(true);
        ta.setCreatedAt(LocalDateTime.now().toString());

        userRepository.save(ta);
        return ta;
    }

    public User registerMO(String accountId, String password, String fullName, String department, String email, String phone) {
        String checkedAccountId = ValidationUtil.validateAccountId(accountId);
        ValidationUtil.validatePassword(password);
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

    public String login(String accountId, String password) {
        String checkedAccountId = ValidationUtil.validateAccountId(accountId);
        ValidationUtil.requireNotBlank(password, "Password");

        User user = userRepository.findByAccountId(checkedAccountId)
            .orElseThrow(() -> new AppException("Invalid account or password."));

        if (!user.isEnabled()) {
            throw new AppException("Account is disabled. Please contact admin.");
        }
        // Allow TA to login without admin approval. MO accounts still require APPROVED.
        if (user.getRole() == Role.MO && user.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new AppException("Account is pending/rejected and cannot login.");
        }
        if (!SecurityUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new AppException("Invalid account or password.");
        }
        return sessionManager.createSession(user);
    }

    public User getUserByToken(String token) {
        SessionContext ctx = sessionManager.requireSession(token);
        return userRepository.findByUserId(ctx.getUserId())
            .orElseThrow(() -> new AppException("User not found."));
    }

    private void ensureAccountNotExist(String accountId) {
        if (userRepository.findByAccountId(accountId).isPresent()) {
            throw new AppException("Account ID already exists.");
        }
    }
}
