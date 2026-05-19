package TA_Recruitment_software.ta_jobs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Resolves persisted CV paths ({@code data/cv/...}) against {@code user.dir}, which may be the
 * module folder, repo root, or an IDE run working directory.
 */
public final class CvPathResolver {

    private CvPathResolver() {}

    /**
     * @param storedPath value from {@code users.csv} (often {@code data/cv/<userId>/cv.ext})
     */
    public static Optional<Path> tryResolveCvFile(String storedPath) {
        if (storedPath == null || storedPath.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalized = storedPath.trim().replace('\\', '/');
        Path raw = Paths.get(normalized);

        if (raw.isAbsolute()) {
            return Files.isRegularFile(raw) ? Optional.of(raw.normalize()) : Optional.empty();
        }

        Path start = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path p = start;
        for (int i = 0; i < 10; i++) {
            Path direct = p.resolve(raw).normalize();
            if (Files.isRegularFile(direct)) {
                return Optional.of(direct);
            }
            Path nested = p.resolve("TA_Recruitment_software").resolve(raw).normalize();
            if (Files.isRegularFile(nested)) {
                return Optional.of(nested);
            }
            if (p.getParent() == null) {
                break;
            }
            p = p.getParent();
        }
        return Optional.empty();
    }
}
