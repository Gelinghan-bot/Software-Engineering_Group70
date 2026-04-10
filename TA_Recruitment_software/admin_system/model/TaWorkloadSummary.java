package TA_Recruitment_software.admin_system.model;

public class TaWorkloadSummary {
    private String userId;
    private String accountId;
    private String fullName;
    private int assignedPositionCount;
    private double totalAssignedHours;

    public TaWorkloadSummary(String userId, String accountId, String fullName,
                             int assignedPositionCount, double totalAssignedHours) {
        this.userId = userId;
        this.accountId = accountId;
        this.fullName = fullName;
        this.assignedPositionCount = assignedPositionCount;
        this.totalAssignedHours = totalAssignedHours;
    }

    public String getUserId() {
        return userId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getFullName() {
        return fullName;
    }

    public int getAssignedPositionCount() {
        return assignedPositionCount;
    }

    public double getTotalAssignedHours() {
        return totalAssignedHours;
    }

    @Override
    public String toString() {
        return "TA Workload{" +
            "userId='" + userId + '\'' +
            ", accountId='" + accountId + '\'' +
            ", fullName='" + fullName + '\'' +
            ", assignedPositionCount=" + assignedPositionCount +
            ", totalAssignedHours=" + totalAssignedHours +
            '}';
    }
}
