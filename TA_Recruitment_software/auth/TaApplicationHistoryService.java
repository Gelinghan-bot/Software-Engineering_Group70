package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.repository.ApplicationRepository;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import TA_Recruitment_software.ta_jobs.PositionSemesterStore;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Full application history across all semesters for the logged-in TA.
 */
public class TaApplicationHistoryService {
    private final ApplicationRepository applicationRepository;
    private final PositionRepository positionRepository;
    private final SessionManager sessionManager;

    public TaApplicationHistoryService(
        ApplicationRepository applicationRepository,
        PositionRepository positionRepository,
        SessionManager sessionManager
    ) {
        this.applicationRepository = applicationRepository;
        this.positionRepository = positionRepository;
        this.sessionManager = sessionManager;
    }

    public List<TaApplicationHistoryEntry> listFullHistory(String token) {
        String userId = sessionManager.requireRole(token, Role.TA).getUserId();
        Map<String, String> semesterByPosition = PositionSemesterStore.readAll();

        List<TaApplicationHistoryEntry> entries = new ArrayList<>();
        for (Application app : applicationRepository.findByApplicant(userId)) {
            Optional<Position> positionOpt = positionRepository.findById(app.getPositionId());
            String jobTitle = positionOpt.map(Position::getJobTitle).orElse("(unknown position)");
            String semester = semesterByPosition.getOrDefault(app.getPositionId(), "-");

            entries.add(new TaApplicationHistoryEntry(
                app.getApplicationId(),
                app.getPositionId(),
                jobTitle,
                semester,
                app.getStatus().name(),
                app.getSubmissionTime(),
                app.getUpdatedTime(),
                app.getStatusNote()
            ));
        }

        entries.sort(Comparator.comparing(TaApplicationHistoryEntry::getSubmissionTime).reversed());
        return entries;
    }

    public List<TaApplicationHistoryEntry> listBySemester(String token, String semesterFilter) {
        if (semesterFilter == null || semesterFilter.trim().isEmpty() || "All".equalsIgnoreCase(semesterFilter.trim())) {
            return listFullHistory(token);
        }
        String filter = semesterFilter.trim();
        List<TaApplicationHistoryEntry> filtered = new ArrayList<>();
        for (TaApplicationHistoryEntry entry : listFullHistory(token)) {
            if (filter.equals(entry.getSemester())) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public List<String> listDistinctSemesters(String token) {
        List<String> semesters = new ArrayList<>();
        for (TaApplicationHistoryEntry entry : listFullHistory(token)) {
            String sem = entry.getSemester();
            if (sem != null && !sem.isEmpty() && !"-".equals(sem) && !semesters.contains(sem)) {
                semesters.add(sem);
            }
        }
        semesters.sort(String::compareTo);
        return semesters;
    }
}
