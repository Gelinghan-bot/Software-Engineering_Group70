package TA_Recruitment_software.auth;

/**
 * School policy limits enforced for TA applications and employment.
 */
public final class TaSchoolRules {
    /** Maximum concurrent active applications per semester (non-rejected pipeline). */
    public static final int MAX_ACTIVE_APPLICATIONS_PER_SEMESTER = 8;
    /** Maximum courses a TA may be hired for in one semester. */
    public static final int MAX_HIRED_COURSES_PER_SEMESTER = 3;

    private TaSchoolRules() {
    }
}
