package TA_Recruitment_software.ta_jobs;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.IdGenerator;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.PositionStatus;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.ApplicationRepository;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.auth.SessionContext;
import TA_Recruitment_software.auth.SessionManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TAJobService {
    private final PositionRepository positionRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public TAJobService(
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

    public List<Position> listAvailableJobs() {
        List<Position> all = positionRepository.findAll();
        List<Position> available = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Position position : all) {
            if (position.getStatus() != PositionStatus.OPEN) {
                continue;
            }
            LocalDate deadline = LocalDate.parse(position.getDeadline());
            if (deadline.isBefore(today)) {
                continue;
            }
            available.add(position);
        }
        return available;
    }

    public Application applyForJob(String token, String positionId) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        String checkedPositionId = ValidationUtil.requireNotBlank(positionId, "Position ID");

        User ta = userRepository.findByUserId(session.getUserId())
            .orElseThrow(() -> new AppException("TA account not found."));
        validateProfileBeforeApply(ta);

        Position position = positionRepository.findById(checkedPositionId)
            .orElseThrow(() -> new AppException("Position not found."));
        if (position.getStatus() != PositionStatus.OPEN) {
            throw new AppException("Position is not open.");
        }
        if (LocalDate.parse(position.getDeadline()).isBefore(LocalDate.now())) {
            throw new AppException("Cannot apply. Position deadline has passed.");
        }
        if (applicationRepository.findByApplicantAndPosition(ta.getUserId(), checkedPositionId).isPresent()) {
            throw new AppException("Duplicate application is not allowed.");
        }

        Application app = new Application();
        app.setApplicationId(IdGenerator.nextId("APP"));
        app.setApplicantUserId(ta.getUserId());
        app.setPositionId(checkedPositionId);
        app.setStatus(ApplicationStatus.PENDING);
        String now = LocalDateTime.now().toString();
        app.setSubmissionTime(now);
        app.setUpdatedTime(now);
        applicationRepository.save(app);
        return app;
    }

    public List<Application> listMyApplications(String token) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        return applicationRepository.findByApplicant(session.getUserId());
    }

    private void validateProfileBeforeApply(User user) {
        if (isBlank(user.getEmail()) || isBlank(user.getPhone()) || isBlank(user.getMajor()) || isBlank(user.getCvFilePath())) {
            throw new AppException("Please complete profile and upload CV before applying.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
