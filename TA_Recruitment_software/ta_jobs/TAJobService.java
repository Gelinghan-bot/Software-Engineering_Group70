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
import java.util.Map;

/**
 * Service layer for Teaching Assistant (TA) job browsing and application operations.
 * <p>
 * This class provides business logic for TA candidates to browse available job positions,
 * apply for positions, and track their application history. All operations are restricted
 * to users with the TA role.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>List all open, non-expired positions matching the current semester</li>
 *   <li>Submit job applications with pre-application profile validation</li>
 *   <li>View personal application history for the current TA user</li>
 *   <li>Prevent duplicate applications for the same position</li>
 * </ul>
 * </p>
 * <p>
 * Before applying, TAs must have completed their profile including email, phone, major,
 * and uploaded CV. Applications are submitted with PENDING status.
 * </p>
 *
 * @author Group70
 * @see Application
 * @see Position
 * @see SessionManager
 */
public class TAJobService {
    private final PositionRepository positionRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    /**
     * Constructs a TAJobService with required dependencies.
     *
     * @param positionRepository     repository for position CRUD operations
     * @param applicationRepository  repository for application CRUD operations
     * @param userRepository         repository for user queries
     * @param sessionManager         session manager for authentication
     */
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

    /**
     * Lists all available job positions that are open, not expired, and match the current semester.
     * <p>
     * A position is considered available if:
     * <ul>
     *   <li>Status is OPEN</li>
     *   <li>Deadline is today or in the future</li>
     *   <li>If registered in {@link PositionSemesterStore}, the semester matches the current semester</li>
     * </ul>
     * </p>
     *
     * @return list of available {@link Position} entities
     */
    public List<Position> listAvailableJobs() {
        String currentSemester = CurrentSemesterStore.readCurrentSemester();
        Map<String, String> semesterByPositionId = PositionSemesterStore.readAll();
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
            if (!PositionSemesterStore.isAllowedForCurrentSemester(position.getPositionId(), currentSemester, semesterByPositionId)) {
                continue;
            }
            available.add(position);
        }
        return available;
    }

    /**
     * Submits an application for a specific position on behalf of the current TA user.
     * <p>
     * Before applying, the following validations are performed:
     * <ul>
     *   <li>User must have TA role</li>
     *   <li>User account must exist and be valid</li>
     *   <li>User profile must be complete (email, phone, major, CV uploaded)</li>
     *   <li>Position must exist, be OPEN, and not expired</li>
     *   <li>Position must match the current semester</li>
     *   <li>User must not have already applied for this position (no duplicates)</li>
     * </ul>
     * </p>
     * <p>
     * On success, a new {@link Application} is created with PENDING status and a unique
     * application ID generated by {@link IdGenerator}.
     * </p>
     *
     * @param token       valid session token (must be TA role)
     * @param positionId  the position to apply for
     * @return the newly created {@link Application} entity
     * @throws AppException if TA role required, profile incomplete, position invalid, or duplicate application
     */
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
        String currentSemester = CurrentSemesterStore.readCurrentSemester();
        Map<String, String> semesterByPositionId = PositionSemesterStore.readAll();
        if (!PositionSemesterStore.isAllowedForCurrentSemester(checkedPositionId, currentSemester, semesterByPositionId)) {
            throw new AppException("This position is not available for the current semester.");
        }
        if (applicationRepository.findByApplicantAndPosition(ta.getUserId(), checkedPositionId).isPresent()) {
            throw new AppException("You have already applied for this position. Duplicate applications are not allowed.");
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

    /**
     * Lists all applications submitted by the current TA user.
     *
     * @param token  valid session token (must be TA role)
     * @return list of {@link Application} entities submitted by the current user
     * @throws AppException if TA role required
     */
    public List<Application> listMyApplications(String token) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        return applicationRepository.findByApplicant(session.getUserId());
    }

    /**
     * Validates that a TA user has completed their profile before applying for a position.
     * <p>
     * Required fields: email, phone, major, and CV file path.
     * </p>
     *
     * @param user  the TA user to validate
     * @throws AppException if any required field is blank
     */
    private void validateProfileBeforeApply(User user) {
        if (isBlank(user.getEmail()) || isBlank(user.getPhone()) || isBlank(user.getMajor()) || isBlank(user.getCvFilePath())) {
            throw new AppException("Please complete profile and upload CV before applying.");
        }
    }

    /**
     * Checks if a string value is null or consists only of whitespace.
     *
     * @param value  the string to check
     * @return true if the value is null or blank, false otherwise
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
