package TA_Recruitment_software.mo_review;

public class MoInvitation {
    private String invitationId;
    private String moUserId;
    private String taUserId;
    private String positionId;
    private String message;
    private String createdAt;

    public String getInvitationId() { return invitationId; }
    public void setInvitationId(String invitationId) { this.invitationId = invitationId; }

    public String getMoUserId() { return moUserId; }
    public void setMoUserId(String moUserId) { this.moUserId = moUserId; }

    public String getTaUserId() { return taUserId; }
    public void setTaUserId(String taUserId) { this.taUserId = taUserId; }

    public String getPositionId() { return positionId; }
    public void setPositionId(String positionId) { this.positionId = positionId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
