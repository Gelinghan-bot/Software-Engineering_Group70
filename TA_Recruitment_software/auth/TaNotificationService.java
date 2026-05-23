package TA_Recruitment_software.auth;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.FileStorageUtil;
import TA_Recruitment_software.admin_system.foundation.IdGenerator;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.ApplicationRepository;
import TA_Recruitment_software.admin_system.repository.PositionRepository;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.ta_jobs.CurrentSemesterStore;
import TA_Recruitment_software.ta_jobs.PositionSemesterStore;
import TA_Recruitment_software.ta_jobs.TAJobService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class TaSchoolRules {
    static final int MAX_ACTIVE_APPLICATIONS_PER_SEMESTER = 8;
    static final int MAX_HIRED_COURSES_PER_SEMESTER = 3;

    private TaSchoolRules() {
    }
}

class TaNotification {
    private String notificationId;
    private String userId;
    private String applicationId;
    private String positionId;
    private String oldStatus;
    private String newStatus;
    private String message;
    private String createdAt;
    private boolean read;

    String getNotificationId() {
        return notificationId;
    }

    void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    String getUserId() {
        return userId;
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    String getApplicationId() {
        return applicationId;
    }

    void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    String getPositionId() {
        return positionId;
    }

    void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    String getOldStatus() {
        return oldStatus;
    }

    void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    String getNewStatus() {
        return newStatus;
    }

    void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    String getMessage() {
        return message;
    }

    void setMessage(String message) {
        this.message = message;
    }

    String getCreatedAt() {
        return createdAt;
    }

    void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    boolean isRead() {
        return read;
    }

    void setRead(boolean read) {
        this.read = read;
    }
}

final class TaNotificationStore {
    private static final String FILE_NAME = "ta_notifications.csv";
    private static final String HEADER =
        "notificationId,userId,applicationId,positionId,oldStatus,newStatus,message,createdAt,read";

    private TaNotificationStore() {
    }

    static List<TaNotification> findByUser(String userId) {
        List<TaNotification> result = new ArrayList<>();
        for (TaNotification n : findAll()) {
            if (userId.equals(n.getUserId())) {
                result.add(n);
            }
        }
        return result;
    }

    static List<TaNotification> findAll() {
        List<List<String>> rows = FileStorageUtil.readRows(FILE_NAME, HEADER);
        List<TaNotification> notifications = new ArrayList<>();
        for (List<String> row : rows) {
            notifications.add(fromRow(row));
        }
        return notifications;
    }

    static void save(TaNotification target) {
        List<TaNotification> all = findAll();
        boolean updated = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getNotificationId().equals(target.getNotificationId())) {
                all.set(i, target);
                updated = true;
                break;
            }
        }
        if (!updated) {
            all.add(target);
        }
        saveAll(all);
    }

    private static void saveAll(List<TaNotification> notifications) {
        List<List<String>> rows = new ArrayList<>();
        for (TaNotification n : notifications) {
            rows.add(toRow(n));
        }
        FileStorageUtil.writeRows(FILE_NAME, HEADER, rows);
    }

    private static List<String> toRow(TaNotification n) {
        List<String> row = new ArrayList<>();
        row.add(nvl(n.getNotificationId()));
        row.add(nvl(n.getUserId()));
        row.add(nvl(n.getApplicationId()));
        row.add(nvl(n.getPositionId()));
        row.add(nvl(n.getOldStatus()));
        row.add(nvl(n.getNewStatus()));
        row.add(nvl(n.getMessage()));
        row.add(nvl(n.getCreatedAt()));
        row.add(n.isRead() ? "true" : "false");
        return row;
    }

    private static TaNotification fromRow(List<String> row) {
        TaNotification n = new TaNotification();
        n.setNotificationId(cell(row, 0));
        n.setUserId(cell(row, 1));
        n.setApplicationId(cell(row, 2));
        n.setPositionId(cell(row, 3));
        n.setOldStatus(cell(row, 4));
        n.setNewStatus(cell(row, 5));
        n.setMessage(cell(row, 6));
        n.setCreatedAt(cell(row, 7));
        n.setRead("true".equalsIgnoreCase(cell(row, 8)));
        return n;
    }

    private static String cell(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}

final class TaApplicationStatusCheckpointStore {
    private static final String FILE_NAME = "ta_app_status_checkpoints.csv";
    private static final String HEADER = "userId,applicationId,lastStatus";

    private TaApplicationStatusCheckpointStore() {
    }

    static Map<String, String> readForUser(String userId) {
        Map<String, String> map = new HashMap<>();
        List<List<String>> rows = FileStorageUtil.readRows(FILE_NAME, HEADER);
        for (List<String> row : rows) {
            if (row.size() >= 3 && userId.equals(row.get(0).trim())) {
                map.put(row.get(1).trim(), row.get(2).trim());
            }
        }
        return map;
    }

    static void writeForUser(String userId, Map<String, String> applicationIdToStatus) {
        List<List<String>> rows = FileStorageUtil.readRows(FILE_NAME, HEADER);
        rows.removeIf(row -> row.size() >= 1 && userId.equals(row.get(0).trim()));

        for (Map.Entry<String, String> entry : applicationIdToStatus.entrySet()) {
            List<String> row = List.of(userId, entry.getKey(), entry.getValue());
            rows.add(row);
        }
        FileStorageUtil.writeRows(FILE_NAME, HEADER, rows);
    }
}

/**
 * Detects application status changes and delivers in-app notifications to TAs.
 */
public class TaNotificationService {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ApplicationRepository applicationRepository;
    private final PositionRepository positionRepository;
    private final SessionManager sessionManager;

    public TaNotificationService(
        ApplicationRepository applicationRepository,
        PositionRepository positionRepository,
        SessionManager sessionManager
    ) {
        this.applicationRepository = applicationRepository;
        this.positionRepository = positionRepository;
        this.sessionManager = sessionManager;
    }

    public int syncNotificationsForUser(String userId) {
        Map<String, String> checkpoints = TaApplicationStatusCheckpointStore.readForUser(userId);
        Map<String, String> latest = new HashMap<>();
        int created = 0;

        for (Application app : applicationRepository.findByApplicant(userId)) {
            String appId = app.getApplicationId();
            String currentStatus = app.getStatus().name();
            latest.put(appId, currentStatus);

            String previous = checkpoints.get(appId);
            if (previous == null) {
                if (!"PENDING".equals(currentStatus) && !"SUBMITTED".equals(currentStatus)) {
                    createNotification(userId, app, "PENDING", currentStatus);
                    created++;
                }
                continue;
            }
            if (!previous.equals(currentStatus)) {
                createNotification(userId, app, previous, currentStatus);
                created++;
            }
        }

        TaApplicationStatusCheckpointStore.writeForUser(userId, latest);
        return created;
    }

    public int syncNotifications(String token) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        return syncNotificationsForUser(session.getUserId());
    }

    public List<TaNotification> listNotifications(String token, boolean unreadOnly) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        syncNotificationsForUser(session.getUserId());

        List<TaNotification> list = TaNotificationStore.findByUser(session.getUserId());
        if (unreadOnly) {
            list.removeIf(TaNotification::isRead);
        }
        list.sort(Comparator.comparing(TaNotification::getCreatedAt).reversed());
        return list;
    }

    public int countUnread(String token) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        syncNotificationsForUser(session.getUserId());
        int count = 0;
        for (TaNotification n : TaNotificationStore.findByUser(session.getUserId())) {
            if (!n.isRead()) {
                count++;
            }
        }
        return count;
    }

    public void markAsRead(String token, String notificationId) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        for (TaNotification n : TaNotificationStore.findByUser(session.getUserId())) {
            if (n.getNotificationId().equals(notificationId)) {
                n.setRead(true);
                TaNotificationStore.save(n);
                return;
            }
        }
    }

    public void markAllAsRead(String token) {
        SessionContext session = sessionManager.requireRole(token, Role.TA);
        for (TaNotification n : TaNotificationStore.findByUser(session.getUserId())) {
            if (!n.isRead()) {
                n.setRead(true);
                TaNotificationStore.save(n);
            }
        }
    }

    public void sendInvitationNotification(String taUserId, String invitationId,
                                            String positionId, String jobTitle, String customMessage) {
        TaNotification notification = new TaNotification();
        notification.setNotificationId(IdGenerator.nextId("NTF"));
        notification.setUserId(taUserId);
        notification.setApplicationId(invitationId);
        notification.setPositionId(positionId);
        notification.setOldStatus("INVITED");
        notification.setNewStatus("INVITED");
        notification.setCreatedAt(LocalDateTime.now().format(FORMAT));
        notification.setRead(false);

        StringBuilder message = new StringBuilder();
        message.append("[Invitation] You have been invited to apply for: ").append(jobTitle);
        if (customMessage != null && !customMessage.isEmpty()) {
            message.append(". Message: ").append(customMessage);
        }
        notification.setMessage(message.toString());
        TaNotificationStore.save(notification);
    }

    private void createNotification(String userId, Application app, String oldStatus, String newStatus) {
        Optional<Position> position = positionRepository.findById(app.getPositionId());
        String jobTitle = position.map(Position::getJobTitle).orElse(app.getPositionId());

        TaNotification notification = new TaNotification();
        notification.setNotificationId(IdGenerator.nextId("NTF"));
        notification.setUserId(userId);
        notification.setApplicationId(app.getApplicationId());
        notification.setPositionId(app.getPositionId());
        notification.setOldStatus(oldStatus);
        notification.setNewStatus(newStatus);
        notification.setCreatedAt(LocalDateTime.now().format(FORMAT));
        notification.setRead(false);

        String note = app.getStatusNote();
        StringBuilder message = new StringBuilder();
        message.append("Application ").append(app.getApplicationId());
        message.append(" (").append(jobTitle).append("): ");
        message.append(oldStatus).append(" → ").append(newStatus);
        if (note != null && !note.trim().isEmpty()) {
            message.append(". Note: ").append(note.trim());
        }
        notification.setMessage(message.toString());
        TaNotificationStore.save(notification);
    }
}

class TaApplicationHistoryEntry {
    private final String applicationId;
    private final String positionId;
    private final String jobTitle;
    private final String semester;
    private final String status;
    private final String submissionTime;
    private final String updatedTime;
    private final String statusNote;

    TaApplicationHistoryEntry(
        String applicationId,
        String positionId,
        String jobTitle,
        String semester,
        String status,
        String submissionTime,
        String updatedTime,
        String statusNote
    ) {
        this.applicationId = applicationId;
        this.positionId = positionId;
        this.jobTitle = jobTitle;
        this.semester = semester;
        this.status = status;
        this.submissionTime = submissionTime;
        this.updatedTime = updatedTime;
        this.statusNote = statusNote;
    }

    String getApplicationId() {
        return applicationId;
    }

    String getPositionId() {
        return positionId;
    }

    String getJobTitle() {
        return jobTitle;
    }

    String getSemester() {
        return semester;
    }

    String getStatus() {
        return status;
    }

    String getSubmissionTime() {
        return submissionTime;
    }

    String getUpdatedTime() {
        return updatedTime;
    }

    String getStatusNote() {
        return statusNote;
    }
}

class TaComplianceService {
    private static final Set<ApplicationStatus> ACTIVE_APPLICATION_STATUSES = EnumSet.of(
        ApplicationStatus.PENDING,
        ApplicationStatus.SUBMITTED,
        ApplicationStatus.SHORTLISTED,
        ApplicationStatus.INTERVIEWED,
        ApplicationStatus.OFFERED,
        ApplicationStatus.APPROVED
    );

    private static final Set<ApplicationStatus> HIRED_STATUSES = EnumSet.of(
        ApplicationStatus.HIRED,
        ApplicationStatus.OFFERED,
        ApplicationStatus.APPROVED
    );

    private final ApplicationRepository applicationRepository;
    private final PositionRepository positionRepository;

    TaComplianceService(ApplicationRepository applicationRepository, PositionRepository positionRepository) {
        this.applicationRepository = applicationRepository;
        this.positionRepository = positionRepository;
    }

    void validateCanApply(String taUserId, String positionId) {
        String semester = resolveSemesterForPosition(positionId);
        if (semester == null || semester.isEmpty()) {
            throw new AppException("This position has no semester tag and cannot be applied to.");
        }

        int activeCount = countApplicationsInSemester(taUserId, semester, ACTIVE_APPLICATION_STATUSES);
        if (activeCount >= TaSchoolRules.MAX_ACTIVE_APPLICATIONS_PER_SEMESTER) {
            throw new AppException(
                "Application limit reached: you may have at most "
                    + TaSchoolRules.MAX_ACTIVE_APPLICATIONS_PER_SEMESTER
                    + " active applications in semester " + semester + "."
            );
        }

        int hiredCount = countApplicationsInSemester(taUserId, semester, HIRED_STATUSES);
        if (hiredCount >= TaSchoolRules.MAX_HIRED_COURSES_PER_SEMESTER) {
            throw new AppException(
                "Hiring limit reached: you are already assigned to "
                    + TaSchoolRules.MAX_HIRED_COURSES_PER_SEMESTER
                    + " course(s) in semester " + semester
                    + ". School policy allows at most "
                    + TaSchoolRules.MAX_HIRED_COURSES_PER_SEMESTER + " TA employments per semester."
            );
        }
    }

    TaComplianceSummary getComplianceSummary(String taUserId, String semester) {
        int active = countApplicationsInSemester(taUserId, semester, ACTIVE_APPLICATION_STATUSES);
        int hired = countApplicationsInSemester(taUserId, semester, HIRED_STATUSES);
        return new TaComplianceSummary(
            semester,
            active,
            TaSchoolRules.MAX_ACTIVE_APPLICATIONS_PER_SEMESTER,
            hired,
            TaSchoolRules.MAX_HIRED_COURSES_PER_SEMESTER
        );
    }

    TaComplianceSummary getCurrentSemesterSummary(String taUserId) {
        return getComplianceSummary(taUserId, CurrentSemesterStore.readCurrentSemester());
    }

    private int countApplicationsInSemester(String taUserId, String semester, Set<ApplicationStatus> statuses) {
        Map<String, String> semesterByPosition = PositionSemesterStore.readAll();
        int count = 0;
        for (Application app : applicationRepository.findByApplicant(taUserId)) {
            if (!statuses.contains(app.getStatus())) {
                continue;
            }
            String appSemester = semesterByPosition.get(app.getPositionId());
            if (semester.equals(appSemester)) {
                count++;
            }
        }
        return count;
    }

    private String resolveSemesterForPosition(String positionId) {
        Map<String, String> semesterByPosition = PositionSemesterStore.readAll();
        String semester = semesterByPosition.get(positionId);
        if (semester != null && !semester.isEmpty()) {
            return semester;
        }
        Position position = positionRepository.findById(positionId).orElse(null);
        return position != null ? CurrentSemesterStore.readCurrentSemester() : null;
    }

    static final class TaComplianceSummary {
        private final String semester;
        private final int activeApplications;
        private final int maxActiveApplications;
        private final int hiredCourses;
        private final int maxHiredCourses;

        TaComplianceSummary(
            String semester,
            int activeApplications,
            int maxActiveApplications,
            int hiredCourses,
            int maxHiredCourses
        ) {
            this.semester = semester;
            this.activeApplications = activeApplications;
            this.maxActiveApplications = maxActiveApplications;
            this.hiredCourses = hiredCourses;
            this.maxHiredCourses = maxHiredCourses;
        }

        String getSemester() {
            return semester;
        }

        int getActiveApplications() {
            return activeApplications;
        }

        int getMaxActiveApplications() {
            return maxActiveApplications;
        }

        int getHiredCourses() {
            return hiredCourses;
        }

        int getMaxHiredCourses() {
            return maxHiredCourses;
        }

        boolean canApplyMore() {
            return activeApplications < maxActiveApplications && hiredCourses < maxHiredCourses;
        }
    }
}

class TaApplicationHistoryService {
    private final ApplicationRepository applicationRepository;
    private final PositionRepository positionRepository;
    private final SessionManager sessionManager;

    TaApplicationHistoryService(
        ApplicationRepository applicationRepository,
        PositionRepository positionRepository,
        SessionManager sessionManager
    ) {
        this.applicationRepository = applicationRepository;
        this.positionRepository = positionRepository;
        this.sessionManager = sessionManager;
    }

    List<TaApplicationHistoryEntry> listFullHistory(String token) {
        String userId = sessionManager.requireRole(token, Role.TA).getUserId();
        Map<String, String> semesterByPosition = PositionSemesterStore.readAll();

        List<TaApplicationHistoryEntry> entries = new ArrayList<>();
        for (Application app : applicationRepository.findByApplicant(userId)) {
            Optional<Position> positionOpt = positionRepository.findById(app.getPositionId());
            String jobTitle = positionOpt.map(Position::getJobTitle).orElse("(unknown position)");
            String semester = semesterByPosition.getOrDefault(app.getPositionId(), "-");

            entries.add(new TaApplicationHistoryEntry(
                app.getApplicationId(),
                app.getPositionId(),
                jobTitle,
                semester,
                app.getStatus().name(),
                app.getSubmissionTime(),
                app.getUpdatedTime(),
                app.getStatusNote()
            ));
        }

        entries.sort(Comparator.comparing(TaApplicationHistoryEntry::getSubmissionTime).reversed());
        return entries;
    }

    List<TaApplicationHistoryEntry> listBySemester(String token, String semesterFilter) {
        if (semesterFilter == null || semesterFilter.trim().isEmpty() || "All".equalsIgnoreCase(semesterFilter.trim())) {
            return listFullHistory(token);
        }
        String filter = semesterFilter.trim();
        List<TaApplicationHistoryEntry> filtered = new ArrayList<>();
        for (TaApplicationHistoryEntry entry : listFullHistory(token)) {
            if (filter.equals(entry.getSemester())) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    List<String> listDistinctSemesters(String token) {
        List<String> semesters = new ArrayList<>();
        for (TaApplicationHistoryEntry entry : listFullHistory(token)) {
            String sem = entry.getSemester();
            if (sem != null && !sem.isEmpty() && !"-".equals(sem) && !semesters.contains(sem)) {
                semesters.add(sem);
            }
        }
        semesters.sort(String::compareTo);
        return semesters;
    }
}

final class SimplePdfWriter {
    private final List<String> lines = new ArrayList<>();
    private int y = 780;

    void addTitle(String text) {
        lines.add(text);
        y -= 28;
    }

    void addLine(String text) {
        if (text == null) {
            text = "";
        }
        for (String part : wrap(text, 90)) {
            lines.add(part);
            y -= 16;
            if (y < 60) {
                lines.add("...(content truncated)");
                return;
            }
        }
    }

    void addBlankLine() {
        y -= 12;
    }

    void write(Path outputFile) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 12 Tf\n");
        int cursorY = 780;
        for (String line : lines) {
            content.append("1 0 0 1 50 ").append(cursorY).append(" Tm\n");
            content.append("(").append(escapePdf(line)).append(") Tj\n");
            cursorY -= 16;
        }
        content.append("ET\n");

        byte[] streamBytes = content.toString().getBytes(StandardCharsets.US_ASCII);
        int streamLen = streamBytes.length;

        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        offsets.add(pdf.length());
        pdf.append("1 0 obj<< /Type /Catalog /Pages 2 0 R >>endobj\n");

        offsets.add(pdf.length());
        pdf.append("2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1 >>endobj\n");

        offsets.add(pdf.length());
        pdf.append("3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] ");
        pdf.append("/Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>endobj\n");

        offsets.add(pdf.length());
        pdf.append("4 0 obj<< /Length ").append(streamLen).append(" >>stream\n");
        pdf.append(content);
        pdf.append("endstream\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("5 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>endobj\n");

        int xrefPos = pdf.length();
        pdf.append("xref\n0 6\n");
        pdf.append("0000000000 65535 f \n");
        for (int i = 1; i < offsets.size(); i++) {
            pdf.append(String.format("%010d", offsets.get(i))).append(" 00000 n \n");
        }
        pdf.append("trailer<< /Size 6 /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefPos).append("\n%%EOF");

        Path parent = outputFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(outputFile, pdf.toString().getBytes(StandardCharsets.US_ASCII));
    }

    private static String escapePdf(String text) {
        String ascii = toAsciiPrintable(text);
        return ascii.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private static String toAsciiPrintable(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (c >= 32 && c <= 126) {
                sb.append(c);
            } else if (c == '\n' || c == '\r' || c == '\t') {
                sb.append(' ');
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }

    private static List<String> wrap(String text, int maxLen) {
        List<String> parts = new ArrayList<>();
        if (text.length() <= maxLen) {
            parts.add(text);
            return parts;
        }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxLen, text.length());
            parts.add(text.substring(start, end));
            start = end;
        }
        return parts;
    }

    private SimplePdfWriter() {
    }

    static SimplePdfWriter create() {
        return new SimplePdfWriter();
    }
}

class TaProfilePdfService {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    TaProfilePdfService(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    Path exportProfilePdf(String token, Path outputFile) {
        User user = userRepository.findByUserId(sessionManager.requireRole(token, Role.TA).getUserId())
            .orElseThrow(() -> new AppException("User not found."));

        if (outputFile == null) {
            throw new AppException("Please choose an output PDF path.");
        }
        String fileName = outputFile.getFileName().toString().toLowerCase();
        if (!fileName.endsWith(".pdf")) {
            outputFile = outputFile.resolveSibling(outputFile.getFileName().toString() + ".pdf");
        }

        SimplePdfWriter writer = SimplePdfWriter.create();
        writer.addTitle("TA Profile Export - JobHere");
        writer.addLine("Generated: " + LocalDateTime.now().format(FORMAT));
        writer.addBlankLine();

        writer.addLine("Full Name: " + safe(user.getFullName()));
        writer.addLine("Account ID: " + safe(user.getAccountId()));
        writer.addLine("Student ID: " + safe(user.getStudentId()));
        writer.addLine("Major: " + safe(user.getMajor()));
        writer.addLine("Email: " + safe(user.getEmail()));
        writer.addLine("Phone: " + safe(user.getPhone()));
        writer.addLine("Skills: " + safe(user.getSkills()));
        writer.addBlankLine();

        String cvPath = user.getCvFilePath();
        if (cvPath == null || cvPath.trim().isEmpty()) {
            writer.addLine("Resume / CV: (not uploaded)");
        } else {
            Path cv = Paths.get(cvPath.trim());
            String link = cv.toAbsolutePath().toUri().toString();
            boolean exists = Files.exists(cv) && Files.isRegularFile(cv);
            writer.addLine("Resume / CV file path: " + cv.toAbsolutePath());
            writer.addLine("Resume link (file URI): " + link);
            writer.addLine("CV on disk: " + (exists ? "available" : "missing — re-upload in profile"));
        }

        try {
            writer.write(outputFile);
        } catch (Exception e) {
            throw new AppException("Failed to write PDF: " + e.getMessage());
        }
        return outputFile;
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}

class TaPortalService {
    private final SessionManager sessionManager;
    private final TaNotificationService notificationService;
    private final TaComplianceService complianceService;
    private final TaApplicationHistoryService historyService;
    private final TaProfilePdfService profilePdfService;
    private final TAJobService taJobService;

    TaPortalService(
        UserRepository userRepository,
        ApplicationRepository applicationRepository,
        PositionRepository positionRepository,
        SessionManager sessionManager,
        TAJobService taJobService
    ) {
        this.sessionManager = sessionManager;
        this.notificationService = new TaNotificationService(applicationRepository, positionRepository, sessionManager);
        this.complianceService = new TaComplianceService(applicationRepository, positionRepository);
        this.historyService = new TaApplicationHistoryService(applicationRepository, positionRepository, sessionManager);
        this.profilePdfService = new TaProfilePdfService(userRepository, sessionManager);
        this.taJobService = taJobService;
    }

    static TaPortalService fromContext(RecruitmentSystemContext context) {
        return new TaPortalService(
            new UserRepository(),
            new ApplicationRepository(),
            new PositionRepository(),
            context.getSessionManager(),
            context.getTaJobService()
        );
    }

    int syncNotifications(String token) {
        return notificationService.syncNotifications(token);
    }

    int countUnreadNotifications(String token) {
        return notificationService.countUnread(token);
    }

    List<TaNotification> listNotifications(String token, boolean unreadOnly) {
        return notificationService.listNotifications(token, unreadOnly);
    }

    void markNotificationRead(String token, String notificationId) {
        notificationService.markAsRead(token, notificationId);
    }

    void markAllNotificationsRead(String token) {
        notificationService.markAllAsRead(token);
    }

    TaComplianceService.TaComplianceSummary getCurrentComplianceSummary(String token) {
        String userId = sessionManager.requireRole(token, Role.TA).getUserId();
        return complianceService.getCurrentSemesterSummary(userId);
    }

    List<TaApplicationHistoryEntry> listApplicationHistory(String token) {
        return historyService.listFullHistory(token);
    }

    List<TaApplicationHistoryEntry> listApplicationHistoryBySemester(String token, String semester) {
        return historyService.listBySemester(token, semester);
    }

    List<String> listHistorySemesters(String token) {
        return historyService.listDistinctSemesters(token);
    }

    Path exportProfilePdf(String token, Path outputFile) {
        return profilePdfService.exportProfilePdf(token, outputFile);
    }

    Application applyForJobWithCompliance(String token, String positionId) {
        String userId = sessionManager.requireRole(token, Role.TA).getUserId();
        complianceService.validateCanApply(userId, positionId);
        return taJobService.applyForJob(token, positionId);
    }
}
