package TA_Recruitment_software.profile;

import TA_Recruitment_software.admin_system.foundation.AppException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class CvStorageService {
    private static final Path DATA_DIR = Paths.get("data");

    public String storeCvFile(String userId, String sourceFilePath) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new AppException("Invalid user.");
        }
        if (sourceFilePath == null || sourceFilePath.trim().isEmpty()) {
            throw new AppException("CV file path is required.");
        }

        Path source = Paths.get(sourceFilePath.trim());
        if (!Files.exists(source) || !Files.isRegularFile(source)) {
            throw new AppException("CV file does not exist: " + sourceFilePath);
        }

        String fileName = source.getFileName().toString();
        String lower = fileName.toLowerCase();
        String ext;
        if (lower.endsWith(".pdf")) {
            ext = "pdf";
        } else if (lower.endsWith(".doc")) {
            ext = "doc";
        } else if (lower.endsWith(".docx")) {
            ext = "docx";
        } else {
            throw new AppException("CV must be .pdf, .doc or .docx");
        }

        Path userDir = DATA_DIR.resolve(Paths.get("cv", userId.trim()));
        Path target = userDir.resolve("cv." + ext);

        try {
            Files.createDirectories(userDir);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AppException("Failed to store CV file.");
        }

        // Persist as a controlled, repo-relative path for better portability.
        String normalized = target.toString().replace('\\', '/');
        return normalized;
    }
}

