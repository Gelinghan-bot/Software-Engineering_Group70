package TA_Recruitment_software.ta_jobs;

import TA_Recruitment_software.admin_system.model.Position;

public class JobMatchResult {
    private final Position position;
    private final int scorePercent;
    private final String reason;

    public JobMatchResult(Position position, int scorePercent, String reason) {
        this.position = position;
        this.scorePercent = Math.max(0, Math.min(100, scorePercent));
        this.reason = reason;
    }

    public Position getPosition() {
        return position;
    }

    public int getScorePercent() {
        return scorePercent;
    }

    public String getReason() {
        return reason;
    }
}
