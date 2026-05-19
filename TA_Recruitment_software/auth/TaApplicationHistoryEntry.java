package TA_Recruitment_software.auth;

/**
 * One row in the TA's cross-semester application history view.
 */
public class TaApplicationHistoryEntry {
    private final String applicationId;
    private final String positionId;
    private final String jobTitle;
    private final String semester;
    private final String status;
    private final String submissionTime;
    private final String updatedTime;
    private final String statusNote;

    public TaApplicationHistoryEntry(
        String applicationId,
        String positionId,
        String jobTitle,
        String semester,
        String status,
        String submissionTime,
        String updatedTime,
        String statusNote
    ) {
        this.applicationId = applicationId;
        this.positionId = positionId;
        this.jobTitle = jobTitle;
        this.semester = semester;
        this.status = status;
        this.submissionTime = submissionTime;
        this.updatedTime = updatedTime;
        this.statusNote = statusNote;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getPositionId() {
        return positionId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getSemester() {
        return semester;
    }

    public String getStatus() {
        return status;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public String getStatusNote() {
        return statusNote;
    }
}
