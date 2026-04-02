package TA_Recruitment_software.admin_system.model;

public class Position {
    private String positionId;
    private String courseName;
    private String responsibleMO;
    private String jobDescription;
    private String requirements;
    private String deadline;
    private String workingHours;
    private String publishedByUserId;
    private PositionStatus status;

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getResponsibleMO() {
        return responsibleMO;
    }

    public void setResponsibleMO(String responsibleMO) {
        this.responsibleMO = responsibleMO;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public String getPublishedByUserId() {
        return publishedByUserId;
    }

    public void setPublishedByUserId(String publishedByUserId) {
        this.publishedByUserId = publishedByUserId;
    }

    public PositionStatus getStatus() {
        return status;
    }

    public void setStatus(PositionStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Position{"
            + "positionId='" + positionId + '\''
            + ", courseName='" + courseName + '\''
            + ", responsibleMO='" + responsibleMO + '\''
            + ", deadline='" + deadline + '\''
            + ", status=" + status
            + '}';
    }
}
