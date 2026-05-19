package TA_Recruitment_software.ta_jobs;

import TA_Recruitment_software.admin_system.model.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds a normalized skill / keyword set from CV text and profile fields.
 */
public final class ApplicantSkillIndex {

    private static final Pattern SPLIT_SKILLS = Pattern.compile("[,;，；、/\n\r|]+");
    private static final Pattern WORD = Pattern.compile("[A-Za-z][A-Za-z0-9+#.\\-]{1,}");
    private static final Set<String> STOP = buildStopwords();

    private ApplicantSkillIndex() {}

    public static Set<String> buildFromCvAndProfile(String cvPlainText, User user) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        if (user.getSkills() != null && !user.getSkills().trim().isEmpty()) {
            for (String part : SPLIT_SKILLS.split(user.getSkills().trim())) {
                addToken(tokens, part);
            }
        }
        if (user.getMajor() != null && !user.getMajor().trim().isEmpty()) {
            addToken(tokens, user.getMajor().trim());
        }
        if (cvPlainText != null && !cvPlainText.isEmpty()) {
            for (String part : SPLIT_SKILLS.split(cvPlainText)) {
                addToken(tokens, part);
            }
            Matcher wm = WORD.matcher(cvPlainText);
            while (wm.find()) {
                addToken(tokens, wm.group());
            }
        }
        return tokens;
    }

    private static void addToken(Set<String> tokens, String raw) {
        if (raw == null) {
            return;
        }
        String t = raw.trim().toLowerCase(Locale.ROOT);
        if (t.length() < 2) {
            return;
        }
        if (STOP.contains(t)) {
            return;
        }
        tokens.add(t);
    }

    public static List<String> matchedInCorpus(Set<String> applicantTokens, String corpusLower) {
        if (corpusLower == null || corpusLower.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> hits = new ArrayList<>();
        for (String token : applicantTokens) {
            if (token.length() < 2) {
                continue;
            }
            if (STOP.contains(token)) {
                continue;
            }
            if (corpusLower.contains(token)) {
                hits.add(token);
            }
        }
        return hits;
    }

    private static Set<String> buildStopwords() {
        String[] words = {
            "the", "and", "for", "with", "from", "this", "that", "have", "has", "are", "was", "were",
            "will", "your", "you", "our", "not", "can", "may", "any", "all", "per", "via", "etc", "pdf",
            "page", "font", "obj", "endobj", "stream", "endstream", "xref", "trailer", "startxref"
        };
        Set<String> s = new LinkedHashSet<>();
        for (String w : words) {
            s.add(w);
        }
        return s;
    }
}
