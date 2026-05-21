package TA_Recruitment_software.mo_review;

public class InterviewNote {
    private String noteId;
    private String applicationId;
    private String moUserId;
    private String noteContent;
    private String lastUpdatedAt;

    public String getNoteId() { return noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getMoUserId() { return moUserId; }
    public void setMoUserId(String moUserId) { this.moUserId = moUserId; }

    public String getNoteContent() { return noteContent; }
    public void setNoteContent(String noteContent) { this.noteContent = noteContent; }

    public String getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(String lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
}
