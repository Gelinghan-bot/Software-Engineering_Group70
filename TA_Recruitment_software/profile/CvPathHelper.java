package TA_Recruitment_software.profile;

import TA_Recruitment_software.ta_jobs.CvPathResolver;
import java.nio.file.Path;
import java.util.Optional;

/**
 * 统一解析 users.csv 中持久化的 CV 路径，封装 {@link CvPathResolver}。
 */
public final class CvPathHelper {

    public enum CvStatus {
        /** 未上传或路径为空 */
        EMPTY,
        /** 路径已记录且磁盘上可读 */
        EXISTS,
        /** 路径已记录但当前工作目录下找不到文件 */
        MISSING_ON_DISK
    }

    private CvPathHelper() {
    }

    public static CvStatus statusOfStoredPath(String storedCvPath) {
        if (storedCvPath == null || storedCvPath.trim().isEmpty()) {
            return CvStatus.EMPTY;
        }
        return tryResolve(storedCvPath).isPresent() ? CvStatus.EXISTS : CvStatus.MISSING_ON_DISK;
    }

    public static Optional<Path> tryResolve(String storedCvPath) {
        return CvPathResolver.tryResolveCvFile(storedCvPath);
    }

    public static boolean existsOnDisk(String storedCvPath) {
        return statusOfStoredPath(storedCvPath) == CvStatus.EXISTS;
    }
}
