package TA_Recruitment_software.profile;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.auth.SessionManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * TA 默认求职信模板（每用户一份）。供后续岗位申请模块读取，本阶段不修改申请 UI。
 */
public class CoverLetterService {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_CONTENT_LEN = 5000;

    private final SessionManager sessionManager;

    public CoverLetterService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public Optional<CoverLetterTemplateStore.CoverLetterRecord> getTemplate(String token) {
        String userId = sessionManager.requireRole(token, Role.TA).getUserId();
        return CoverLetterTemplateStore.findByUserId(userId);
    }

    public void saveTemplate(String token, String content) {
        String userId = sessionManager.requireRole(token, Role.TA).getUserId();
        String checked = ValidationUtil.requireNotBlank(content, "Cover letter").trim();
        if (checked.length() > MAX_CONTENT_LEN) {
            throw new AppException("Cover letter exceeds max length " + MAX_CONTENT_LEN + ".");
        }
        CoverLetterTemplateStore.save(userId, checked, LocalDateTime.now().format(FORMAT));
    }

    /**
     * 供岗位申请流程读取（按 TA userId，不校验 token）。
     */
    public String getTemplateForApply(String taUserId) {
        return CoverLetterTemplateStore.findByUserId(taUserId)
            .map(CoverLetterTemplateStore.CoverLetterRecord::getContent)
            .orElse("");
    }

    public void assertTemplateExistsForApply(String taUserId) {
        String text = getTemplateForApply(taUserId);
        if (text == null || text.trim().isEmpty()) {
            throw new AppException("Cover letter template is empty. Please save a cover letter template first.");
        }
    }
}
