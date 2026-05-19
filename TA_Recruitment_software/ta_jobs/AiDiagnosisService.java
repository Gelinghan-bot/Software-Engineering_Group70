package TA_Recruitment_software.ta_jobs;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.profile.ProfileService;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AiDiagnosisService {
    private final ProfileService profileService;
    private final TAJobService taJobService;

    public AiDiagnosisService(ProfileService profileService, TAJobService taJobService) {
        this.profileService = profileService;
        this.taJobService = taJobService;
    }

    /**
     * Reads CV (and profile skills), scores each open TA position, returns sorted recommendations.
     */
    public List<JobMatchResult> diagnoseAndRecommend(String token) {
        User user = profileService.getMyProfile(token);
        if (user.getCvFilePath() == null || user.getCvFilePath().trim().isEmpty()) {
            throw new AppException("Please upload your CV in Update Profile before using AI Diagnosis.");
        }
        String storedCv = user.getCvFilePath().trim();
        Optional<Path> cvPathOpt = CvPathResolver.tryResolveCvFile(storedCv);
        boolean cvFileMissing = cvPathOpt.isEmpty();
        String cvText = "";
        if (cvPathOpt.isPresent()) {
            cvText = CvTextExtractor.extractText(cvPathOpt.get());
        }
        boolean extractionLimited = cvPathOpt.isPresent() && cvText.trim().length() < 30;

        Set<String> applicantTokens = ApplicantSkillIndex.buildFromCvAndProfile(cvText, user);
        if (applicantTokens.isEmpty()) {
            throw new AppException("No skill keywords could be derived from your CV or profile. "
                + (cvFileMissing
                ? "Your CV file was not found on disk at the saved path — please upload the CV again in Update Profile, "
                    + "or fill in the Skills field so matching can run without the file.\nSaved path: " + storedCv
                : "Please add Skills in your profile or use a DOCX/PDF CV with readable text."));
        }

        List<Position> open = taJobService.listAvailableJobs();
        if (open.isEmpty()) {
            return Collections.emptyList();
        }

        List<JobMatchResult> results = new ArrayList<>();
        for (Position p : open) {
            String corpus = buildCorpus(p).toLowerCase(Locale.ROOT);
            List<String> hits = ApplicantSkillIndex.matchedInCorpus(applicantTokens, corpus);
            int skillScore = scoreFromHits(applicantTokens.size(), hits.size());
            int majorBonus = majorMatches(user.getMajor(), p.getMajor()) ? 15 : 0;
            int raw = skillScore + majorBonus;
            int score = Math.min(100, raw);
            String reason = buildReason(hits, majorBonus > 0, extractionLimited);
            results.add(new JobMatchResult(p, score, reason));
        }
        results.sort(Comparator.comparingInt(JobMatchResult::getScorePercent).reversed());
        if (cvFileMissing && !results.isEmpty()) {
            JobMatchResult top = results.get(0);
            String prefix =
                "Note: CV file not found on disk at \"" + storedCv + "\". "
                    + "Typical causes: the file was never uploaded on this PC, it was deleted, or it is not tracked in Git "
                    + "(local uploads stay under data/cv/). Re-upload via Personal → My Resume. "
                    + "Scores below use your profile (major/skills) only.\n\n";
            results.set(0, new JobMatchResult(top.getPosition(), top.getScorePercent(), prefix + top.getReason()));
        }
        return results;
    }

    private static String buildCorpus(Position p) {
        return String.join("\n",
            nz(p.getJobTitle()),
            nz(p.getGrade()),
            nz(p.getMajor()),
            nz(p.getJobType()),
            nz(p.getJobDescription()),
            nz(p.getRequirements()),
            nz(p.getInterviewLocation())
        );
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static boolean majorMatches(String userMajor, String posMajor) {
        if (userMajor == null || posMajor == null) {
            return false;
        }
        String u = userMajor.trim().toLowerCase(Locale.ROOT);
        String v = posMajor.trim().toLowerCase(Locale.ROOT);
        return !u.isEmpty() && u.equals(v);
    }

    private static int scoreFromHits(int applicantCount, int hitCount) {
        if (applicantCount <= 0 || hitCount <= 0) {
            return 0;
        }
        double ratio = (double) hitCount / (double) applicantCount;
        return (int) Math.round(Math.min(1.0, ratio * 1.2) * 85.0);
    }

    private static String buildReason(List<String> hits, boolean majorMatch, boolean extractionLimited) {
        StringBuilder sb = new StringBuilder();
        if (extractionLimited) {
            sb.append("CV text extraction was limited; matching used profile Skills/Major plus any readable CV text. ");
        }
        if (majorMatch) {
            sb.append("Major aligns with the position (+). ");
        }
        if (hits.isEmpty()) {
            sb.append("No strong keyword overlap with requirements.");
        } else {
            String top = hits.stream().limit(12).collect(Collectors.joining(", "));
            sb.append("Overlapping skills/topics: ").append(top);
            if (hits.size() > 12) {
                sb.append(", …");
            }
        }
        return sb.toString().trim();
    }
}
