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
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can publish positions.");
        }
        User publisher = userRepository.findByUserId(session.getUserId())
            .orElseThrow(() -> new AppException("Account not found."));

        String checkedCourse = ValidationUtil.validateName(courseName, "Course name");
        String checkedDesc = ValidationUtil.sanitizeText(jobDescription, "Job description", 800);
        String checkedReq = ValidationUtil.sanitizeText(requirements, "Requirements", 600);
        String checkedHours = ValidationUtil.sanitizeText(workingHours, "Working hours", 80);
        LocalDate checkedDeadline = ValidationUtil.validateDate(deadline, "Deadline");
        ValidationUtil.ensureTodayOrFuture(checkedDeadline, "Deadline");

        Position position = new Position();
        position.setPositionId(IdGenerator.nextId("POS"));
        position.setCourseName(checkedCourse);
        position.setResponsibleMO(publisher.getFullName());
        position.setJobDescription(checkedDesc);
        position.setRequirements(checkedReq);
        position.setDeadline(checkedDeadline.toString());
        position.setWorkingHours(checkedHours);
        position.setPublishedByUserId(publisher.getUserId());
        position.setStatus(PositionStatus.OPEN);

        positionRepository.save(position);
        return position;
    }

    public Position updateDeadline(String token, String positionId, String newDeadline) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can update positions.");
        }
        Position position = positionRepository.findById(ValidationUtil.requireNotBlank(positionId, "Position ID"))
            .orElseThrow(() -> new AppException("Position not found."));

        // Admin can update any position; MO can only update their own
        if (session.getRole() == Role.MO && !position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only update your own positions.");
        }
        LocalDate checkedDate = ValidationUtil.validateDate(newDeadline, "Deadline");
        ValidationUtil.ensureTodayOrFuture(checkedDate, "Deadline");
        position.setDeadline(checkedDate.toString());
        positionRepository.save(position);
        return position;
    }

    public Position closePosition(String token, String positionId) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can close positions.");
        }
        Position position = positionRepository.findById(ValidationUtil.requireNotBlank(positionId, "Position ID"))
            .orElseThrow(() -> new AppException("Position not found."));

        // Admin can close any position; MO can only close their own
        if (session.getRole() == Role.MO && !position.getPublishedByUserId().equals(session.getUserId())) {
            throw new AppException("Permission denied. You can only close your own positions.");
        }
        position.setStatus(PositionStatus.CLOSED);
        positionRepository.save(position);
        return position;
    }

    public List<Position> listMyPositions(String token) {
        SessionContext session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied. Only MO and ADMIN can view positions.");
        }
        // Admin can see all positions; MO can only see their own
        if (session.getRole() == Role.ADMIN) {
            return positionRepository.findAll();
        }
        return positionRepository.findByPublisher(session.getUserId());
    }
}
