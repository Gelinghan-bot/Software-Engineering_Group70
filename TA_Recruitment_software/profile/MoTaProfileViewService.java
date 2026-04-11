package TA_Recruitment_software.profile;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.ApprovalStatus;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.auth.SessionManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Lets an MO list approved TAs and load a TA profile (including CV path) for review.
 * Does not expose passwords; callers must not display {@link User#getPasswordHash()} in UI.
 */
public class MoTaProfileViewService {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public MoTaProfileViewService(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    public List<User> listApprovedTas(String token) {
        sessionManager.requireRole(token, Role.MO);
        List<User> result = new ArrayList<>();
        for (User u : userRepository.findAll()) {
            if (u.getRole() == Role.TA
                && u.getApprovalStatus() == ApprovalStatus.APPROVED
                && u.isEnabled()) {
                result.add(u);
            }
        }
        return result;
    }

    public User getTaProfileForMo(String token, String taUserId) {
        sessionManager.requireRole(token, Role.MO);
        String id = ValidationUtil.requireNotBlank(taUserId, "TA user ID");
        User u = userRepository.findByUserId(id.trim())
            .orElseThrow(() -> new AppException("TA not found."));
        if (u.getRole() != Role.TA) {
            throw new AppException("Selected user is not a TA.");
        }
        if (u.getApprovalStatus() != ApprovalStatus.APPROVED || !u.isEnabled()) {
            throw new AppException("This TA account is not available for review.");
        }
        return u;
    }
}
