package TA_Recruitment_software.mo_review;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.IdGenerator;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import TA_Recruitment_software.admin_system.model.ApprovalStatus;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.ApplicationRepository;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.auth.SessionManager;
import TA_Recruitment_software.auth.TaNotificationService;
import TA_Recruitment_software.ta_jobs.CurrentSemesterStore;
import TA_Recruitment_software.ta_jobs.PositionSemesterStore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoInvitationService {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_HIRED_PER_SEMESTER = 3;

    private final SessionManager sessionManager;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final PositionRepository positionRepository;
    private final TaNotificationService taNotificationService;

    public MoInvitationService(
        SessionManager sessionManager,
        UserRepository userRepository,
        ApplicationRepository applicationRepository,
        PositionRepository positionRepository,
        TaNotificationService taNotificationService
    ) {
        this.sessionManager = sessionManager;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.positionRepository = positionRepository;
        this.taNotificationService = taNotificationService;
    }

    public static class EligibleTA {
        public final User user;
        public final int hiredCount;
        public EligibleTA(User user, int hiredCount) {
            this.user = user;
            this.hiredCount = hiredCount;
        }
    }

    /**
     * Returns all TAs whose hired count for the current semester is below MAX_HIRED_PER_SEMESTER.
     * The given positionId is used to filter out TAs who already applied for that position.
     */
    public List<EligibleTA> listEligibleTAs(String token, String positionId) {
        requireMoOrAdmin(token);
        String currentSemester = CurrentSemesterStore.readCurrentSemester();
        Map<String, String> semesterByPos = PositionSemesterStore.readAll();

        // Build a map: taUserId -> hiredCount in current semester
        Map<String, Integer> hiredCounts = new HashMap<>();
        for (var app : applicationRepository.findAll()) {
            if (app.getStatus() != ApplicationStatus.HIRED) continue;
            String posSem = semesterByPos.get(app.getPositionId());
            if (!currentSemester.equals(posSem)) continue;
            hiredCounts.merge(app.getApplicantUserId(), 1, Integer::sum);
        }

        List<EligibleTA> result = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            if (user.getRole() != Role.TA) continue;
            
            // Check Approval and Enabled status
            if (user.getApprovalStatus() != ApprovalStatus.APPROVED || !user.isEnabled()) continue;
            
            // Filter out TAs who already applied for the given position
            if (applicationRepository.findByApplicantAndPosition(user.getUserId(), positionId).isPresent()) {
                continue;
            }

            int hired = hiredCounts.getOrDefault(user.getUserId(), 0);
            if (hired < MAX_HIRED_PER_SEMESTER) {
                result.add(new EligibleTA(user, hired));
            }
        }
        return result;
    }

    public void sendInvitation(String token, String taUserId, String positionId, String message) {
        String moUserId = requireMoOrAdmin(token);

        User ta = userRepository.findByUserId(taUserId)
            .orElseThrow(() -> new AppException("TA not found."));
        Position position = positionRepository.findById(positionId)
            .orElseThrow(() -> new AppException("Position not found."));

        MoInvitation inv = new MoInvitation();
        inv.setInvitationId(IdGenerator.nextId("INV"));
        inv.setMoUserId(moUserId);
        inv.setTaUserId(taUserId);
        inv.setPositionId(positionId);
        inv.setMessage(message != null ? message.trim() : "");
        inv.setCreatedAt(LocalDateTime.now().format(FORMAT));
        MoInvitationStore.save(inv);

        // Deliver in-app notification to the TA
        taNotificationService.sendInvitationNotification(
            taUserId,
            inv.getInvitationId(),
            positionId,
            position.getJobTitle(),
            inv.getMessage()
        );
    }

    private String requireMoOrAdmin(String token) {
        var session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied.");
        }
        return session.getUserId();
    }
}
