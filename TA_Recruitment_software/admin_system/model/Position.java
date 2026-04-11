package TA_Recruitment_software.admin_system.model;

public class Position {
    private String positionId;
    private String jobTitle;
    private String grade;
    private String major;
    private String jobType;
    private String responsibleMO;
    private String jobDescription;
    private String requirements;
    private String interviewLocation;
    private String deadline;
    private String publishedByUserId;
    private PositionStatus status;

    public String getPositionId() { return positionId; }
    public void setPositionId(String positionId) { this.positionId = positionId; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getResponsibleMO() { return responsibleMO; }
    public void setResponsibleMO(String responsibleMO) { this.responsibleMO = responsibleMO; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getInterviewLocation() { return interviewLocation; }
    public void setInterviewLocation(String interviewLocation) { this.interviewLocation = interviewLocation; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getPublishedByUserId() { return publishedByUserId; }
    public void setPublishedByUserId(String publishedByUserId) { this.publishedByUserId = publishedByUserId; }

    public PositionStatus getStatus() { return status; }
    public void setStatus(PositionStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Position{"
            + "positionId='" + positionId + '\''
            + ", jobTitle='" + jobTitle + '\''
            + ", grade='" + grade + '\''
            + ", major='" + major + '\''
            + ", jobType='" + jobType + '\''
            + ", responsibleMO='" + responsibleMO + '\''
            + ", deadline='" + deadline + '\''
            + ", status=" + status
            + '}';
    }
}