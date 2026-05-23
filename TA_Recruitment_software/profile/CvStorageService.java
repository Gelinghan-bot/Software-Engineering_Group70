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

        try {
            long size = Files.size(source);
            if (size > ProfileConstants.MAX_CV_FILE_BYTES) {
                throw new AppException("CV file is too large. Maximum size is 10 MB.");
            }
        } catch (IOException e) {
            throw new AppException("Failed to read CV file.");
        }

        String fileName = source.getFileName().toString();
        if (!ProfileConstants.isAllowedCvExtension(fileName)) {
            throw new AppException("CV must be .pdf, .doc or .docx");
        }
        String lower = fileName.toLowerCase();
        String ext;
        if (lower.endsWith(ProfileConstants.CV_EXT_PDF)) {
            ext = "pdf";
        } else if (lower.endsWith(ProfileConstants.CV_EXT_DOC)) {
            ext = "doc";
        } else {
            ext = "docx";
        }

        Path userDir = DATA_DIR.resolve(Paths.get("cv", userId.trim()));
        Path target = userDir.resolve("cv." + ext);

        try {
            Files.createDirectories(userDir);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AppException("Failed to store CV file.");
        }

        if (!Files.isRegularFile(target)) {
            throw new AppException("Failed to store CV file.");
        }

        String normalized = target.toString().replace('\\', '/');
        if (!CvPathHelper.existsOnDisk(normalized)) {
            throw new AppException("Failed to store CV file.");
        }
        return normalized;
    }
}
