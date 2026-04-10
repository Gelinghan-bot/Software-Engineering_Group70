package TA_Recruitment_software.mo_review;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.ApplicationRepository;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.auth.SessionContext;
import TA_Recruitment_software.auth.SessionManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MOReviewService {
    private final PositionRepository positionRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    private static final Map<ApplicationStatus, List<ApplicationStatus>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = new EnumMap<>(ApplicationStatus.class);
        VALID_TRANSITIONS.put(ApplicationStatus.PENDING,
            Arrays.asList(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED));
        VALID_TRANSITIONS.put(ApplicationStatus.SUBMITTED,
            Arrays.asList(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED));
        VALID_TRANSITIONS.put(ApplicationStatus.SHORTLISTED,
            Arrays.asList(ApplicationStatus.INTERVIEWED, ApplicationStatus.OFFERED, ApplicationStatus.REJECTED));
        VALID_TRANSITIONS.put(ApplicationStatus.INTERVIEWED,
            Arrays.asList(ApplicationStatus.OFFERED, ApplicationStatus.REJECTED));
        VALID_TRANSITIONS.put(ApplicationStatus.OFFERED,
            Arrays.asList(ApplicationStatus.HIRED, ApplicationStatus.REJECTED));
        VALID_TRANSITIONS.put(ApplicationStatus.APPROVED,
            Arrays.asList(ApplicationStatus.HIRED, ApplicationStatus.REJECTED));
        VALID_TRANSITIONS.put(ApplicationStatus.REJECTED, Collections.emptyList());
        VALID_TRANSITIONS.put(ApplicationStatus.HIRED, Collections.emptyList());
    }

    public MOReviewService(
        PositionRepository positionRepository,
        ApplicationRepository applicationRepository,
        UserRepository userRepository,
        SessionManager sessionManager
    ) {
        this.positionRepository = positionRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    public List<Application> listApplicationsForPosition(String token, String positionId) {
        SessionContext session = sessionManager.requireRole(token, Role.MO);
        String checkedPositionId = ValidationUtil.requireNotBlank(positionId, "Position ID");
        Position position = positionRepository.findById(checkedPositionId)
            .orElseThrow(() -> new AppException("Position not found."));
        if (!position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only review your own positions.");
        }
        return applicationRepository.findByPosition(checkedPositionId);
    }

    public List<Application> listAllApplicationsOfMyPositions(String token) {
        SessionContext session = sessionManager.requireRole(token, Role.MO);
        List<Position> ownedPositions = positionRepository.findByPublisher(session.getUserId());
        Set<String> positionIds = new HashSet<>();
        for (Position position : ownedPositions) {
            positionIds.add(position.getPositionId());
        }

        List<Application> result = new ArrayList<>();
        for (Application app : applicationRepository.findAll()) {
            if (positionIds.contains(app.getPositionId())) {
                result.add(app);
            }
        }
        return result;
    }

    public Optional<User> getApplicantInfo(String applicantUserId) {
        return userRepository.findByUserId(applicantUserId);
    }

    public Optional<Position> getPositionInfo(String positionId) {
        return positionRepository.findById(positionId);
    }

    public List<ApplicationStatus> getValidNextStatuses(ApplicationStatus current) {
        List<ApplicationStatus> next = VALID_TRANSITIONS.get(current);
        return next != null ? new ArrayList<>(next) : Collections.emptyList();
    }

    public Application updateApplicationStatus(String token, String applicationId,
                                                ApplicationStatus newStatus, String note) {
        SessionContext session = sessionManager.requireRole(token, Role.MO);
        Application app = applicationRepository
            .findById(ValidationUtil.requireNotBlank(applicationId, "Application ID"))
            .orElseThrow(() -> new AppException("Application not found."));
        Position position = positionRepository.findById(app.getPositionId())
            .orElseThrow(() -> new AppException("Position not found for this application."));

        if (!position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only update applications for your own positions.");
        }

        List<ApplicationStatus> validNext = getValidNextStatuses(app.getStatus());
        if (!validNext.contains(newStatus)) {
            throw new AppException("Invalid status transition: " + app.getStatus() + " -> " + newStatus
                + ". Allowed: " + validNext);
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String historyEntry = now + " | " + app.getStatus() + " -> " + newStatus;
        if (note != null && !note.trim().isEmpty()) {
            historyEntry += " | Note: " + note.trim();
        }
        String existingHistory = app.getStatusHistory();
        if (existingHistory == null || existingHistory.isEmpty()) {
            app.setStatusHistory(historyEntry);
        } else {
            app.setStatusHistory(existingHistory + " ;; " + historyEntry);
        }

        app.setStatus(newStatus);
        app.setUpdatedTime(now);

        if (note != null && !note.trim().isEmpty()) {
            app.setStatusNote(note.trim());
        }

        applicationRepository.save(app);
        return app;
    }

    public Application updateApplicationStatus(String token, String applicationId,
                                                ApplicationStatus newStatus) {
        return updateApplicationStatus(token, applicationId, newStatus, null);
    }

    public void sortBySubmissionTime(List<Application> apps, boolean ascending) {
        apps.sort((a, b) -> {
            String t1 = a.getSubmissionTime() != null ? a.getSubmissionTime() : "";
            String t2 = b.getSubmissionTime() != null ? b.getSubmissionTime() : "";
            return ascending ? t1.compareTo(t2) : t2.compareTo(t1);
        });
    }

    public void sortByMajor(List<Application> apps) {
        apps.sort((a, b) -> {
            String m1 = getMajorForApp(a);
            String m2 = getMajorForApp(b);
            return m1.compareTo(m2);
        });
    }

    private String getMajorForApp(Application app) {
        Optional<User> user = userRepository.findByUserId(app.getApplicantUserId());
        return user.map(u -> u.getMajor() != null ? u.getMajor() : "").orElse("");
    }
}
