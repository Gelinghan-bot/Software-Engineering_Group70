package TA_Recruitment_software.mo_review;

import TA_Recruitment_software.admin_system.foundation.FileStorageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class InterviewNoteStore {
    private static final String FILE_NAME = "interview_notes.csv";
    private static final String HEADER = "noteId,applicationId,moUserId,noteContent,lastUpdatedAt";

    private InterviewNoteStore() {}

    static List<InterviewNote> findAll() {
        List<InterviewNote> result = new ArrayList<>();
        for (List<String> row : FileStorageUtil.readRows(FILE_NAME, HEADER)) {
            result.add(fromRow(row));
        }
        return result;
    }

    static Optional<InterviewNote> findByAppAndMo(String applicationId, String moUserId) {
        for (InterviewNote note : findAll()) {
            if (applicationId.equals(note.getApplicationId()) && moUserId.equals(note.getMoUserId())) {
                return Optional.of(note);
            }
        }
        return Optional.empty();
    }

    static List<InterviewNote> findByMo(String moUserId) {
        List<InterviewNote> result = new ArrayList<>();
        for (InterviewNote note : findAll()) {
            if (moUserId.equals(note.getMoUserId())) {
                result.add(note);
            }
        }
        return result;
    }

    static void save(InterviewNote target) {
        List<InterviewNote> all = findAll();
        boolean updated = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getNoteId().equals(target.getNoteId())) {
                all.set(i, target);
                updated = true;
                break;
            }
        }
        if (!updated) {
            all.add(target);
        }
        List<List<String>> rows = new ArrayList<>();
        for (InterviewNote n : all) {
            rows.add(toRow(n));
        }
        FileStorageUtil.writeRows(FILE_NAME, HEADER, rows);
    }

    private static List<String> toRow(InterviewNote n) {
        List<String> row = new ArrayList<>();
        row.add(nvl(n.getNoteId()));
        row.add(nvl(n.getApplicationId()));
        row.add(nvl(n.getMoUserId()));
        row.add(nvl(n.getNoteContent()));
        row.add(nvl(n.getLastUpdatedAt()));
        return row;
    }

    private static InterviewNote fromRow(List<String> row) {
        InterviewNote n = new InterviewNote();
        n.setNoteId(cell(row, 0));
        n.setApplicationId(cell(row, 1));
        n.setMoUserId(cell(row, 2));
        n.setNoteContent(cell(row, 3));
        n.setLastUpdatedAt(cell(row, 4));
        return n;
    }

    private static String cell(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }

    private static String nvl(String v) { return v == null ? "" : v; }
}
