package TA_Recruitment_software.profile;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.UserRepository;
import TA_Recruitment_software.auth.SessionManager;
import TA_Recruitment_software.auth.TaProfilePdfService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * 资料导出门面：委托 PDF 摘要导出，并可选择将 CV 原文件复制到目标目录。
 */
public class ProfileExportFacade {

    private final ProfileService profileService;
    private final TaProfilePdfService pdfService;

    public ProfileExportFacade(
        UserRepository userRepository,
        SessionManager sessionManager,
        ProfileService profileService
    ) {
        this.profileService = profileService;
        this.pdfService = new TaProfilePdfService(userRepository, sessionManager);
    }

    public Path exportProfilePdf(String token, Path outputFile) {
        return pdfService.exportProfilePdf(token, outputFile);
    }

    /**
     * 将当前 TA 的 CV 原文件复制到目标目录（文件名与源文件一致）。
     *
     * @return 复制后的路径；无 CV 或文件缺失时返回 empty
     */
    public Optional<Path> copyCvTo(String token, Path targetDir) {
        User user = profileService.getMyProfile(token);
        String stored = user.getCvFilePath();
        if (stored == null || stored.trim().isEmpty()) {
            return Optional.empty();
        }
        Optional<Path> resolved = CvPathHelper.tryResolve(stored);
        if (resolved.isEmpty()) {
            return Optional.empty();
        }
        Path source = resolved.get();
        try {
            Files.createDirectories(targetDir);
            Path dest = targetDir.resolve(source.getFileName().toString());
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            return Optional.of(dest);
        } catch (IOException e) {
            throw new AppException("Failed to copy CV file: " + e.getMessage());
        }
    }
}
