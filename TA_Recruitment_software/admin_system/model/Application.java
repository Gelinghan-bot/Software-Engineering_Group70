package TA_Recruitment_software.admin_system.model;

/**
 * Represents a TA job application submitted by a Teaching Assistant.
 * <p>
 * This entity tracks each application from submission through the review process.
 * Applications are created by TAs through {@link TA_Recruitment_software.ta_jobs.TAJobService#applyForJob}
 * and reviewed by MOs through {@link TA_Recruitment_software.mo_review.MOReviewService}.
 * </p>
 * <p>
 * Application status workflow:
 * <ol>
 *   <li>{@link ApplicationStatus#PENDING} - Initial state after TA submits application</li>
 *   <li>{@link ApplicationStatus#SHORTLISTED} - MO has selected the applicant for further review</li>
 *   <li>{@link ApplicationStatus#INTERVIEWED} - Interview has been conducted</li>
 *   <li>{@link ApplicationStatus#OFFERED} - Job offer has been extended</li>
 *   <li>{@link ApplicationStatus#HIRED} - TA has accepted the offer and is hired</li>
 *   <li>{@link ApplicationStatus#REJECTED} - Application was rejected at any stage</li>
 * </ol>
 * </p>
 *
 * @author Group70
 * @see ApplicationStatus
 * @see Position
 * @see User
 */
public class Application {
    /** Unique identifier for this application (e.g., "APP000001"). */
    private String applicationId;

    /** User ID of the TA who submitted this application. */
    private String applicantUserId;

    /** ID of the position being applied for. */
    private String positionId;

    /** Current application status in the review workflow. */
    private ApplicationStatus status;

    /** Timestamp when the application was submitted. */
    private String submissionTime;

    /** Timestamp when the status was last updated. */
    private String updatedTime;

    /** Optional note attached when status changes (e.g., rejection reason). */
    private String statusNote;

    /** Full history of status changes as a text record. */
    private String statusHistory;

    /**
     * Gets the unique application identifier.
     *
     * @return the application ID
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the unique application identifier.
     *
     * @param applicationId the application ID to set
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Gets the user ID of the TA applicant.
     *
     * @return the applicant user ID
     */
    public String getApplicantUserId() {
        return applicantUserId;
    }

    /**
     * Sets the user ID of the TA applicant.
     *
     * @param applicantUserId the applicant user ID to set
     */
    public void setApplicantUserId(String applicantUserId) {
        this.applicantUserId = applicantUserId;
    }

    /**
     * Gets the ID of the position being applied for.
     *
     * @return the position ID
     */
    public String getPositionId() {
        return positionId;
    }

    /**
     * Sets the ID of the position being applied for.
     *
     * @param positionId the position ID to set
     */
    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    /**
     * Gets the current application status.
     *
     * @return the {@link ApplicationStatus} enum value
     */
    public ApplicationStatus getStatus() {
        return status;
    }

    /**
     * Sets the current application status.
     * <p>
     * Status changes should be performed through the service layer methods
     * which enforce workflow rules and record history.
     * </p>
     *
     * @param status the {@link ApplicationStatus} to set
     */
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    /**
     * Gets the submission timestamp.
     *
     * @return the submission time string
     */
    public String getSubmissionTime() {
        return submissionTime;
    }

    /**
     * Sets the submission timestamp.
     *
     * @param submissionTime the submission time to set
     */
    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    /**
     * Gets the last update timestamp.
     *
     * @return the updated time string
     */
    public String getUpdatedTime() {
        return updatedTime;
    }

    /**
     * Sets the last update timestamp.
     *
     * @param updatedTime the updated time to set
     */
    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * Gets the note attached to the current status.
     * <p>
     * Commonly used for rejection reasons or interview feedback summaries.
     * </p>
     *
     * @return the status note
     */
    public String getStatusNote() {
        return statusNote;
    }

    /**
     * Sets the note attached to the current status.
     *
     * @param statusNote the status note to set
     */
    public void setStatusNote(String statusNote) {
        this.statusNote = statusNote;
    }

    /**
     * Gets the full status change history.
     *
     * @return the status history text
     */
    public String getStatusHistory() {
        return statusHistory;
    }

    /**
     * Sets the full status change history.
     *
     * @param statusHistory the status history to set
     */
    public void setStatusHistory(String statusHistory) {
        this.statusHistory = statusHistory;
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
