package TA_Recruitment_software.admin_system.model;

/**
 * Represents a job position for Teaching Assistant (TA) recruitment.
 * <p>
 * This entity stores all information about a TA position including job details,
 * requirements, application deadline, and hiring status. Positions are created
 * by Module Organizers (MO) or administrators through the {@link TA_Recruitment_software.mo_publish.MOPublishService}.
 * </p>
 * <p>
 * Position lifecycle:
 * <ol>
 *   <li>Created with {@link PositionStatus#OPEN} status</li>
 *   <li>TAs can apply while position is OPEN and deadline has not passed</li>
 *   <li>Position can be manually closed by the MO, changing status to {@link PositionStatus#CLOSED}</li>
 *   <li>Position automatically expires when deadline passes, changing status to {@link PositionStatus#EXPIRED}</li>
 * </ol>
 * </p>
 * <p>
 * Key fields:
 * <ul>
 *   <li>{@code grade}: Target student grade (e.g., "Year 2", "All Grades")</li>
 *   <li>{@code major}: Target major (e.g., "Computer Science", "All majors")</li>
 *   <li>{@code jobType}: Job category (e.g., "Grading", "Lab Support")</li>
 *   <li>{@code headcount}: Number of TAs to hire (used for auto-closing when filled)</li>
 *   <li>{@code responsibleMO}: Name of the MO responsible for this position</li>
 * </ul>
 * </p>
 *
 * @author Group70
 * @see PositionStatus
 * @see Application
 */
public class Position {
    /** Unique identifier for this position (e.g., "POS000001"). */
    private String positionId;

    /** The job title displayed to applicants. */
    private String jobTitle;

    /** Target student grade level (e.g., "Year 2", "All Grades"). */
    private String grade;

    /** Target academic major (e.g., "Computer Science", "All majors"). */
    private String major;

    /** Job category (e.g., "Grading", "Lab Support", "All Categories"). */
    private String jobType;

    /** Name of the Module Organizer responsible for this position. */
    private String responsibleMO;

    /** Detailed description of the job responsibilities. */
    private String jobDescription;

    /** Requirements and qualifications expected from applicants. */
    private String requirements;

    /** Location where interviews will be conducted. */
    private String interviewLocation;

    /** Application deadline in "yyyy-MM-dd" format. */
    private String deadline;

    /** User ID of the MO who published this position. */
    private String publishedByUserId;

    /** Current status of the position (OPEN, CLOSED, or EXPIRED). */
    private PositionStatus status;

    /** Number of TAs to hire for this position. */
    private int headcount;

    /**
     * Gets the number of TAs to hire.
     *
     * @return the headcount
     */
    public int getHeadcount() {
        return headcount;
    }

    /**
     * Sets the number of TAs to hire.
     *
     * @param headcount the headcount to set
     */
    public void setHeadcount(int headcount) {
        this.headcount = headcount;
    }

    /**
     * Gets the unique position identifier.
     *
     * @return the position ID
     */
    public String getPositionId() {
        return positionId;
    }

    /**
     * Sets the unique position identifier.
     *
     * @param positionId the position ID to set
     */
    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    /**
     * Gets the target student grade level.
     *
     * @return the grade
     */
    public String getGrade() {
        return grade;
    }

    /**
     * Sets the target student grade level.
     *
     * @param grade the grade to set
     */
    public void setGrade(String grade) {
        this.grade = grade;
    }

    /**
     * Gets the target academic major.
     *
     * @return the major
     */
    public String getMajor() {
        return major;
    }

    /**
     * Sets the target academic major.
     *
     * @param major the major to set
     */
    public void setMajor(String major) {
        this.major = major;
    }

    /**
     * Gets the job title.
     *
     * @return the job title
     */
    public String getJobTitle() {
        return jobTitle;
    }

    /**
     * Sets the job title.
     *
     * @param jobTitle the job title to set
     */
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     * Gets the job category.
     *
     * @return the job type
     */
    public String getJobType() {
        return jobType;
    }

    /**
     * Sets the job category.
     *
     * @param jobType the job type to set
     */
    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    /**
     * Gets the name of the responsible Module Organizer.
     *
     * @return the responsible MO name
     */
    public String getResponsibleMO() {
        return responsibleMO;
    }

    /**
     * Sets the name of the responsible Module Organizer.
     *
     * @param responsibleMO the responsible MO name to set
     */
    public void setResponsibleMO(String responsibleMO) {
        this.responsibleMO = responsibleMO;
    }

    /**
     * Gets the detailed job description.
     *
     * @return the job description
     */
    public String getJobDescription() {
        return jobDescription;
    }

    /**
     * Sets the detailed job description.
     *
     * @param jobDescription the job description to set
     */
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    /**
     * Gets the applicant requirements.
     *
     * @return the requirements
     */
    public String getRequirements() {
        return requirements;
    }

    /**
     * Sets the applicant requirements.
     *
     * @param requirements the requirements to set
     */
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    /**
     * Gets the interview location.
     *
     * @return the interview location
     */
    public String getInterviewLocation() {
        return interviewLocation;
    }

    /**
     * Sets the interview location.
     *
     * @param interviewLocation the interview location to set
     */
    public void setInterviewLocation(String interviewLocation) {
        this.interviewLocation = interviewLocation;
    }

    /**
     * Gets the application deadline.
     *
     * @return the deadline in "yyyy-MM-dd" format
     */
    public String getDeadline() {
        return deadline;
    }

    /**
     * Sets the application deadline.
     *
     * @param deadline the deadline to set
     */
    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    /**
     * Gets the user ID of the MO who published this position.
     *
     * @return the publisher user ID
     */
    public String getPublishedByUserId() {
        return publishedByUserId;
    }

    /**
     * Sets the user ID of the MO who published this position.
     *
     * @param publishedByUserId the publisher user ID to set
     */
    public void setPublishedByUserId(String publishedByUserId) {
        this.publishedByUserId = publishedByUserId;
    }

    /**
     * Gets the current position status.
     *
     * @return the {@link PositionStatus} enum value
     */
    public PositionStatus getStatus() {
        return status;
    }

    /**
     * Sets the current position status.
     *
     * @param status the {@link PositionStatus} to set
     */
    public void setStatus(PositionStatus status) {
        this.status = status;
    }

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
            + ", headcount=" + headcount
            + '}';
    }
}
