package TA_Recruitment_software.ta_jobs;

import TA_Recruitment_software.admin_system.foundation.AppException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Best-effort CV text extraction for matching (no external libraries).
 * DOCX is read from the embedded XML; PDF uses heuristic string extraction.
 */
public final class CvTextExtractor {

    private CvTextExtractor() {}

    public static String extractText(Path cvPath) {
        if (cvPath == null) {
            return "";
        }
        Path path = cvPath.toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) {
            throw new AppException("CV file not found at: " + path);
        }
        String name = path.getFileName().toString().toLowerCase();
        try {
            if (name.endsWith(".docx")) {
                return extractDocx(path);
            }
            if (name.endsWith(".pdf")) {
                return extractPdfHeuristic(path);
            }
            if (name.endsWith(".doc")) {
                return "";
            }
            return "";
        } catch (IOException e) {
            throw new AppException("Failed to read CV file.");
        }
    }

    private static String extractDocx(Path path) throws IOException {
        try (ZipFile zip = new ZipFile(path.toFile())) {
            ZipEntry entry = zip.getEntry("word/document.xml");
            if (entry == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(' ');
                }
            }
            String xml = sb.toString();
            xml = xml.replaceAll("<w:tab[^/]*/>", " ");
            xml = xml.replaceAll("<w:br[^/]*/>", "\n");
            xml = xml.replaceAll("<[^>]+>", " ");
            xml = decodeBasicXmlEntities(xml);
            return collapseWhitespace(xml);
        }
    }

    private static String decodeBasicXmlEntities(String s) {
        return s.replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&apos;", "'");
    }

    private static String extractPdfHeuristic(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        String raw = new String(bytes, StandardCharsets.ISO_8859_1);
        StringBuilder fromParens = new StringBuilder();
        Matcher m = Pattern.compile("\\(([^\\)]{1,500})\\)").matcher(raw);
        while (m.find()) {
            String chunk = unescapePdfString(m.group(1));
            if (chunk.chars().filter(Character::isLetter).count() >= 3) {
                fromParens.append(chunk).append(' ');
            }
        }
        if (fromParens.length() > 80) {
            return collapseWhitespace(fromParens.toString());
        }
        Matcher word = Pattern.compile("[A-Za-z][A-Za-z0-9+#.\\-]{2,}").matcher(raw);
        StringBuilder ascii = new StringBuilder();
        while (word.find()) {
            ascii.append(word.group()).append(' ');
        }
        return collapseWhitespace(ascii.toString());
    }

    private static String unescapePdfString(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(i + 1);
                if (n == 'n') {
                    out.append('\n');
                    i++;
                } else if (n == 'r') {
                    out.append('\r');
                    i++;
                } else if (n == 't') {
                    out.append('\t');
                    i++;
                } else if (n == '(' || n == ')' || n == '\\') {
                    out.append(n);
                    i++;
                } else {
                    out.append(n);
                    i++;
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static String collapseWhitespace(String s) {
        return s.replaceAll("\\s+", " ").trim();
    }
}
