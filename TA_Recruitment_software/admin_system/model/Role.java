package TA_Recruitment_software.admin_system.model;

/**
 * Represents the available user roles in the TA Recruitment System.
 * <p>
 * Each role defines a specific set of permissions and access levels:
 * </p>
 * <ul>
 *   <li>{@link #TA} - Teaching Assistant: Can browse jobs, apply for positions, manage profile, and upload CV</li>
 *   <li>{@link #MO} - Module Organizer: Can publish job positions, review applications, and conduct interviews</li>
 *   <li>{@link #ADMIN} - System Administrator: Can manage users, approve accounts, reset passwords, and monitor workload</li>
 * </ul>
 *
 * @author Group70
 * @see User
 */
public enum Role {
    TA,
    MO,
    ADMIN
}
