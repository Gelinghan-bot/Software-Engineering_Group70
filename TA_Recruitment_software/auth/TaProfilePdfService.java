package TA_Recruitment_software.auth;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.profile.CvPathHelper;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Exports TA profile fields and CV file path/link into a single PDF for external sharing.
 */
public class TaProfilePdfService {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public TaProfilePdfService(UserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    public Path exportProfilePdf(String token, Path outputFile) {
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
            Path cv = CvPathHelper.tryResolve(cvPath).orElse(Paths.get(cvPath.trim()));
            String link = cv.toAbsolutePath().toUri().toString();
            boolean exists = CvPathHelper.statusOfStoredPath(cvPath) == CvPathHelper.CvStatus.EXISTS;
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
