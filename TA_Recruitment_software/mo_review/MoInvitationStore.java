package TA_Recruitment_software.mo_review;

import TA_Recruitment_software.admin_system.foundation.FileStorageUtil;
import java.util.ArrayList;
import java.util.List;

final class MoInvitationStore {
    private static final String FILE_NAME = "mo_invitations.csv";
    private static final String HEADER = "invitationId,moUserId,taUserId,positionId,message,createdAt";

    private MoInvitationStore() {}

    static void save(MoInvitation inv) {
        FileStorageUtil.appendRow(FILE_NAME, HEADER, toRow(inv));
    }

    static List<MoInvitation> findAll() {
        List<MoInvitation> result = new ArrayList<>();
        for (List<String> row : FileStorageUtil.readRows(FILE_NAME, HEADER)) {
            result.add(fromRow(row));
        }
        return result;
    }

    private static List<String> toRow(MoInvitation inv) {
        List<String> row = new ArrayList<>();
        row.add(nvl(inv.getInvitationId()));
        row.add(nvl(inv.getMoUserId()));
        row.add(nvl(inv.getTaUserId()));
        row.add(nvl(inv.getPositionId()));
        row.add(nvl(inv.getMessage()));
        row.add(nvl(inv.getCreatedAt()));
        return row;
    }

    private static MoInvitation fromRow(List<String> row) {
        MoInvitation inv = new MoInvitation();
        inv.setInvitationId(cell(row, 0));
        inv.setMoUserId(cell(row, 1));
        inv.setTaUserId(cell(row, 2));
        inv.setPositionId(cell(row, 3));
        inv.setMessage(cell(row, 4));
        inv.setCreatedAt(cell(row, 5));
        return inv;
    }

    private static String cell(List<String> row, int i) { return i < row.size() ? row.get(i) : ""; }
    private static String nvl(String v) { return v == null ? "" : v; }
}
