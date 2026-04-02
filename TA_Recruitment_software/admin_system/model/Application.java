package TA_Recruitment_software.admin_system.model;

public class Application {
    private String applicationId;
    private String applicantUserId;
    private String positionId;
    private ApplicationStatus status;
    private String submissionTime;
    private String updatedTime;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicantUserId() {
        return applicantUserId;
    }

    public void setApplicantUserId(String applicantUserId) {
        this.applicantUserId = applicantUserId;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "Application{"
            + "applicationId='" + applicationId + '\''
            + ", applicantUserId='" + applicantUserId + '\''
            + ", positionId='" + positionId + '\''
            + ", status=" + status
            + ", submissionTime='" + submissionTime + '\''
            + '}';
    }
}
