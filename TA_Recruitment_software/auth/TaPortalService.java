package TA_Recruitment_software.auth;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.repository.ApplicationRepository;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.ta_jobs.TAJobService;
import java.nio.file.Path;
import java.util.List;

/**
 * TA auth-side portal: notifications, compliance, history, PDF export, and guarded apply.
 */
public class TaPortalService {
    private final SessionManager sessionManager;
    private final TaNotificationService notificationService;
    private final TaComplianceService complianceService;
    private final TaApplicationHistoryService historyService;
    private final TaProfilePdfService profilePdfService;
    private final TAJobService taJobService;

    public TaPortalService(
        UserRepository userRepository,
        ApplicationRepository applicationRepository,
        PositionRepository positionRepository,
        SessionManager sessionManager,
        TAJobService taJobService
    ) {
        this.sessionManager = sessionManager;
        this.notificationService = new TaNotificationService(applicationRepository, positionRepository, sessionManager);
        this.complianceService = new TaComplianceService(applicationRepository, positionRepository);
        this.historyService = new TaApplicationHistoryService(applicationRepository, positionRepository, sessionManager);
        this.profilePdfService = new TaProfilePdfService(userRepository, sessionManager);
        this.taJobService = taJobService;
    }

    public static TaPortalService fromContext(RecruitmentSystemContext context) {
        return new TaPortalService(
            new UserRepository(),
            new ApplicationRepository(),
            new PositionRepository(),
            context.getSessionManager(),
            context.getTaJobService()
        );
    }

    public int syncNotifications(String token) {
        return notificationService.syncNotifications(token);
    }

    public int countUnreadNotifications(String token) {
        return notificationService.countUnread(token);
    }

    public List<TaNotification> listNotifications(String token, boolean unreadOnly) {
        return notificationService.listNotifications(token, unreadOnly);
    }

    public void markNotificationRead(String token, String notificationId) {
        notificationService.markAsRead(token, notificationId);
    }

    public void markAllNotificationsRead(String token) {
        notificationService.markAllAsRead(token);
    }

    public TaComplianceService.TaComplianceSummary getCurrentComplianceSummary(String token) {
        String userId = sessionManager.requireRole(token, Role.TA).getUserId();
        return complianceService.getCurrentSemesterSummary(userId);
    }

    public List<TaApplicationHistoryEntry> listApplicationHistory(String token) {
        return historyService.listFullHistory(token);
    }

    public List<TaApplicationHistoryEntry> listApplicationHistoryBySemester(String token, String semester) {
        return historyService.listBySemester(token, semester);
    }

    public List<String> listHistorySemesters(String token) {
        return historyService.listDistinctSemesters(token);
    }

    public Path exportProfilePdf(String token, Path outputFile) {
        return profilePdfService.exportProfilePdf(token, outputFile);
    }

    /**
     * Apply for a position after enforcing school application/hiring limits.
     */
    public Application applyForJobWithCompliance(String token, String positionId) {
        String userId = sessionManager.requireRole(token, Role.TA).getUserId();
        complianceService.validateCanApply(userId, positionId);
        return taJobService.applyForJob(token, positionId);
    }
}
