package TA_Recruitment_software.mo_review;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.IdGenerator;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.auth.SessionManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InterviewNoteService {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final SessionManager sessionManager;

    public InterviewNoteService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public InterviewNote saveOrUpdate(String token, String applicationId, String content) {
        String moUserId = requireMoOrAdmin(token);
        if (applicationId == null || applicationId.trim().isEmpty()) {
            throw new AppException("Application ID is required.");
        }
        Optional<InterviewNote> existing = InterviewNoteStore.findByAppAndMo(applicationId, moUserId);
        InterviewNote note = existing.orElseGet(InterviewNote::new);
        if (note.getNoteId() == null || note.getNoteId().isEmpty()) {
            note.setNoteId(IdGenerator.nextId("NOTE"));
            note.setApplicationId(applicationId);
            note.setMoUserId(moUserId);
        }
        note.setNoteContent(content == null ? "" : content);
        note.setLastUpdatedAt(LocalDateTime.now().format(FORMAT));
        InterviewNoteStore.save(note);
        return note;
    }

    public Optional<InterviewNote> findNote(String token, String applicationId) {
        String moUserId = requireMoOrAdmin(token);
        return InterviewNoteStore.findByAppAndMo(applicationId, moUserId);
    }

    /** Returns applicationIds whose notes contain the keyword (case-insensitive). */
    public List<String> searchByKeyword(String token, String keyword) {
        String moUserId = requireMoOrAdmin(token);
        List<String> matched = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return matched;
        String lower = keyword.trim().toLowerCase();
        for (InterviewNote note : InterviewNoteStore.findByMo(moUserId)) {
            if (note.getNoteContent() != null && note.getNoteContent().toLowerCase().contains(lower)) {
                matched.add(note.getApplicationId());
            }
        }
        return matched;
    }

    private String requireMoOrAdmin(String token) {
        var session = sessionManager.requireSession(token);
        if (session.getRole() != Role.MO && session.getRole() != Role.ADMIN) {
            throw new AppException("Permission denied.");
        }
        return session.getUserId();
    }
}
