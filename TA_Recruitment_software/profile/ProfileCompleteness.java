package TA_Recruitment_software.profile;

import TA_Recruitment_software.admin_system.model.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TA 资料完整度：与 {@link TA_Recruitment_software.ta_jobs.TAJobService} 申请前校验对齐
 * （email、phone、major、cv）；skills 计入完整度百分比但不阻塞申请。
 */
public final class ProfileCompleteness {

    private final int percent;
    private final List<String> missingFields;
    private final boolean readyToApply;

    private ProfileCompleteness(int percent, List<String> missingFields, boolean readyToApply) {
        this.percent = percent;
        this.missingFields = Collections.unmodifiableList(new ArrayList<>(missingFields));
        this.readyToApply = readyToApply;
    }

    public static ProfileCompleteness fromUser(User user) {
        List<String> missing = new ArrayList<>();
        int filled = 0;
        int total = 5;

        if (isBlank(user.getEmail())) {
            missing.add("email");
        } else {
            filled++;
        }
        if (isBlank(user.getPhone())) {
            missing.add("phone");
        } else {
            filled++;
        }
        if (isBlank(user.getMajor())) {
            missing.add("major");
        } else {
            filled++;
        }
        if (isBlank(user.getSkills())) {
            missing.add("skills");
        } else {
            filled++;
        }
        CvPathHelper.CvStatus cvStatus = CvPathHelper.statusOfStoredPath(user.getCvFilePath());
        if (cvStatus == CvPathHelper.CvStatus.EMPTY) {
            missing.add("cv");
        } else if (cvStatus == CvPathHelper.CvStatus.MISSING_ON_DISK) {
            missing.add("cv (file missing on disk)");
        } else {
            filled++;
        }

        int pct = total == 0 ? 0 : (filled * 100) / total;
        // 与 TAJobService.validateProfileBeforeApply 一致：仅要求路径非空，不校验磁盘存在
        boolean ready = !isBlank(user.getEmail())
            && !isBlank(user.getPhone())
            && !isBlank(user.getMajor())
            && cvStatus != CvPathHelper.CvStatus.EMPTY;
        return new ProfileCompleteness(pct, missing, ready);
    }

    public int getPercent() {
        return percent;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public boolean isReadyToApply() {
        return readyToApply;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
