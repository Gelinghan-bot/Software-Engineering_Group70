package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.repository.ApplicationRepository;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import TA_Recruitment_software.ta_jobs.CurrentSemesterStore;
import TA_Recruitment_software.ta_jobs.PositionSemesterStore;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Enforces school rules on TA applications and hiring (max courses per semester).
 */
public class TaComplianceService {
    private static final Set<ApplicationStatus> ACTIVE_APPLICATION_STATUSES = EnumSet.of(
        ApplicationStatus.PENDING,
        ApplicationStatus.SUBMITTED,
        ApplicationStatus.SHORTLISTED,
        ApplicationStatus.INTERVIEWED,
        ApplicationStatus.OFFERED,
        ApplicationStatus.APPROVED
    );

    private static final Set<ApplicationStatus> HIRED_STATUSES = EnumSet.of(
        ApplicationStatus.HIRED,
        ApplicationStatus.OFFERED,
        ApplicationStatus.APPROVED
    );

    private final ApplicationRepository applicationRepository;
    private final PositionRepository positionRepository;

    public TaComplianceService(ApplicationRepository applicationRepository, PositionRepository positionRepository) {
        this.applicationRepository = applicationRepository;
        this.positionRepository = positionRepository;
    }

    /**
     * Validates that the TA may submit a new application for the given position.
     */
    public void validateCanApply(String taUserId, String positionId) {
        String semester = resolveSemesterForPosition(positionId);
        if (semester == null || semester.isEmpty()) {
            throw new AppException("This position has no semester tag and cannot be applied to.");
        }

        int activeCount = countApplicationsInSemester(taUserId, semester, ACTIVE_APPLICATION_STATUSES);
        if (activeCount >= TaSchoolRules.MAX_ACTIVE_APPLICATIONS_PER_SEMESTER) {
            throw new AppException(
                "Application limit reached: you may have at most "
                    + TaSchoolRules.MAX_ACTIVE_APPLICATIONS_PER_SEMESTER
                    + " active applications in semester " + semester + "."
            );
        }

        int hiredCount = countApplicationsInSemester(taUserId, semester, HIRED_STATUSES);
        if (hiredCount >= TaSchoolRules.MAX_HIRED_COURSES_PER_SEMESTER) {
            throw new AppException(
                "Hiring limit reached: you are already assigned to "
                    + TaSchoolRules.MAX_HIRED_COURSES_PER_SEMESTER
                    + " course(s) in semester " + semester
                    + ". School policy allows at most "
                    + TaSchoolRules.MAX_HIRED_COURSES_PER_SEMESTER + " TA employments per semester."
            );
        }
    }

    public TaComplianceSummary getComplianceSummary(String taUserId, String semester) {
        int active = countApplicationsInSemester(taUserId, semester, ACTIVE_APPLICATION_STATUSES);
        int hired = countApplicationsInSemester(taUserId, semester, HIRED_STATUSES);
        return new TaComplianceSummary(
            semester,
            active,
            TaSchoolRules.MAX_ACTIVE_APPLICATIONS_PER_SEMESTER,
            hired,
            TaSchoolRules.MAX_HIRED_COURSES_PER_SEMESTER
        );
    }

    public TaComplianceSummary getCurrentSemesterSummary(String taUserId) {
        return getComplianceSummary(taUserId, CurrentSemesterStore.readCurrentSemester());
    }

    private int countApplicationsInSemester(String taUserId, String semester, Set<ApplicationStatus> statuses) {
        Map<String, String> semesterByPosition = PositionSemesterStore.readAll();
        int count = 0;
        for (Application app : applicationRepository.findByApplicant(taUserId)) {
            if (!statuses.contains(app.getStatus())) {
                continue;
            }
            String appSemester = semesterByPosition.get(app.getPositionId());
            if (semester.equals(appSemester)) {
                count++;
            }
        }
        return count;
    }

    private String resolveSemesterForPosition(String positionId) {
        Map<String, String> semesterByPosition = PositionSemesterStore.readAll();
        String semester = semesterByPosition.get(positionId);
        if (semester != null && !semester.isEmpty()) {
            return semester;
        }
        Position position = positionRepository.findById(positionId).orElse(null);
        return position != null ? CurrentSemesterStore.readCurrentSemester() : null;
    }

    public static final class TaComplianceSummary {
        private final String semester;
        private final int activeApplications;
        private final int maxActiveApplications;
        private final int hiredCourses;
        private final int maxHiredCourses;

        public TaComplianceSummary(
            String semester,
            int activeApplications,
            int maxActiveApplications,
            int hiredCourses,
            int maxHiredCourses
        ) {
            this.semester = semester;
            this.activeApplications = activeApplications;
            this.maxActiveApplications = maxActiveApplications;
            this.hiredCourses = hiredCourses;
            this.maxHiredCourses = maxHiredCourses;
        }

        public String getSemester() {
            return semester;
        }

        public int getActiveApplications() {
            return activeApplications;
        }

        public int getMaxActiveApplications() {
            return maxActiveApplications;
        }

        public int getHiredCourses() {
            return hiredCourses;
        }

        public int getMaxHiredCourses() {
            return maxHiredCourses;
        }

        public boolean canApplyMore() {
            return activeApplications < maxActiveApplications && hiredCourses < maxHiredCourses;
        }
    }
}
