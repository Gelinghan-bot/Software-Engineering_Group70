package TA_Recruitment_software.profile;

/**
 * TA 资料模块常量，与 {@link TA_Recruitment_software.admin_system.foundation.ValidationUtil} 字段长度对齐。
 */
public final class ProfileConstants {

    /** 与 ValidationUtil.sanitizeText(..., "Major", 80) 一致 */
    public static final int MAX_MAJOR_LEN = 80;

    /** 与 ValidationUtil.sanitizeText(..., "Skills", 200) 一致 */
    public static final int MAX_SKILLS_LEN = 200;

    /** CV 上传最大体积（字节），约 10MB */
    public static final long MAX_CV_FILE_BYTES = 10L * 1024 * 1024;

    public static final String CV_EXT_PDF = ".pdf";
    public static final String CV_EXT_DOC = ".doc";
    public static final String CV_EXT_DOCX = ".docx";

    private ProfileConstants() {
    }

    public static boolean isAllowedCvExtension(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase();
        return lower.endsWith(CV_EXT_PDF) || lower.endsWith(CV_EXT_DOC) || lower.endsWith(CV_EXT_DOCX);
    }
}
