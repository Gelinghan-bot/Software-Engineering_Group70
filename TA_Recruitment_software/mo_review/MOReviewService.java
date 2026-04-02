package TA_Recruitment_software.mo_review;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.repository.ApplicationRepository;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import TA_Recruitment_software.auth.SessionContext;
import TA_Recruitment_software.auth.SessionManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MOReviewService {
    private final PositionRepository positionRepository;
    private final ApplicationRepository applicationRepository;
    private final SessionManager sessionManager;

    public MOReviewService(
        PositionRepository positionRepository,
        ApplicationRepository applicationRepository,
        SessionManager sessionManager
    ) {
        this.positionRepository = positionRepository;
        this.applicationRepository = applicationRepository;
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

    public Application updateApplicationStatus(String token, String applicationId, ApplicationStatus newStatus) {
        SessionContext session = sessionManager.requireRole(token, Role.MO);
        Application app = applicationRepository.findById(ValidationUtil.requireNotBlank(applicationId, "Application ID"))
            .orElseThrow(() -> new AppException("Application not found."));
        Position position = positionRepository.findById(app.getPositionId())
            .orElseThrow(() -> new AppException("Position not found for this application."));

        if (!position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only update applications for your own positions.");
        }
        app.setStatus(newStatus);
        app.setUpdatedTime(LocalDateTime.now().toString());
        applicationRepository.save(app);
        return app;
    }
}
