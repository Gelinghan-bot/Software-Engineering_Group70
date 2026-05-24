package TA_Recruitment_software.admin_system.model;

/**
 * Represents the possible states of a TA job application in the review workflow.
 * <p>
 * Typical workflow: SUBMITTED → PENDING → SHORTLISTED → INTERVIEWED → OFFERED → HIRED
 * Applications can be REJECTED at any stage.
 * </p>
 *
 * @author Group70
 * @see Application
 */
public enum ApplicationStatus {
    /** Application is pending MO review. */
    PENDING,
    /** Application has been approved for further consideration. */
    APPROVED,
    /** Application has been rejected. */
    REJECTED,
    /** Applicant has been hired. */
    HIRED,
    /** Application has been submitted. */
    SUBMITTED,
    /** Applicant has been shortlisted for interview. */
    SHORTLISTED,
    /** Interview has been completed. */
    INTERVIEWED,
    /** Job offer has been extended. */
    OFFERED
}
