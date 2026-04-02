package TA_Recruitment_software.mo_publish;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.IdGenerator;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.PositionStatus;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.auth.SessionContext;
import TA_Recruitment_software.auth.SessionManager;
import java.time.LocalDate;
import java.util.List;

public class MOPublishService {
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public MOPublishService(
        PositionRepository positionRepository,
        UserRepository userRepository,
        SessionManager sessionManager
    ) {
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    public Position publishPosition(
        String token,
        String courseName,
        String jobDescription,
        String requirements,
        String deadline,
        String workingHours
    ) {
        SessionContext session = sessionManager.requireRole(token, Role.MO);
        User mo = userRepository.findByUserId(session.getUserId())
            .orElseThrow(() -> new AppException("MO account not found."));

        String checkedCourse = ValidationUtil.validateName(courseName, "Course name");
        String checkedDesc = ValidationUtil.sanitizeText(jobDescription, "Job description", 800);
        String checkedReq = ValidationUtil.sanitizeText(requirements, "Requirements", 600);
        String checkedHours = ValidationUtil.sanitizeText(workingHours, "Working hours", 80);
        LocalDate checkedDeadline = ValidationUtil.validateDate(deadline, "Deadline");
        ValidationUtil.ensureTodayOrFuture(checkedDeadline, "Deadline");

        Position position = new Position();
        position.setPositionId(IdGenerator.nextId("POS"));
        position.setCourseName(checkedCourse);
        position.setResponsibleMO(mo.getFullName());
        position.setJobDescription(checkedDesc);
        position.setRequirements(checkedReq);
        position.setDeadline(checkedDeadline.toString());
        position.setWorkingHours(checkedHours);
        position.setPublishedByUserId(mo.getUserId());
        position.setStatus(PositionStatus.OPEN);

        positionRepository.save(position);
        return position;
    }

    public Position updateDeadline(String token, String positionId, String newDeadline) {
        SessionContext session = sessionManager.requireRole(token, Role.MO);
        Position position = positionRepository.findById(ValidationUtil.requireNotBlank(positionId, "Position ID"))
            .orElseThrow(() -> new AppException("Position not found."));

        if (!position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only update your own positions.");
        }
        LocalDate checkedDate = ValidationUtil.validateDate(newDeadline, "Deadline");
        ValidationUtil.ensureTodayOrFuture(checkedDate, "Deadline");
        position.setDeadline(checkedDate.toString());
        positionRepository.save(position);
        return position;
    }

    public Position closePosition(String token, String positionId) {
        SessionContext session = sessionManager.requireRole(token, Role.MO);
        Position position = positionRepository.findById(ValidationUtil.requireNotBlank(positionId, "Position ID"))
            .orElseThrow(() -> new AppException("Position not found."));

        if (!position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only close your own positions.");
        }
        position.setStatus(PositionStatus.CLOSED);
        positionRepository.save(position);
        return position;
    }

    public List<Position> listMyPositions(String token) {
        SessionContext session = sessionManager.requireRole(token, Role.MO);
        return positionRepository.findByPublisher(session.getUserId());
    }
}
