package TA_Recruitment_software.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal PDF writer (no external libraries) for profile + resume link export.
 */
final class SimplePdfWriter {
    private final List<String> lines = new ArrayList<>();
    private int y = 780;

    void addTitle(String text) {
        lines.add(text);
        y -= 28;
    }

    void addLine(String text) {
        if (text == null) {
            text = "";
        }
        for (String part : wrap(text, 90)) {
            lines.add(part);
            y -= 16;
            if (y < 60) {
                lines.add("...(content truncated)");
                return;
            }
        }
    }

    void addBlankLine() {
        y -= 12;
    }

    void write(Path outputFile) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 12 Tf\n");
        int cursorY = 780;
        for (String line : lines) {
            content.append("1 0 0 1 50 ").append(cursorY).append(" Tm\n");
            content.append("(").append(escapePdf(line)).append(") Tj\n");
            cursorY -= 16;
        }
        content.append("ET\n");

        byte[] streamBytes = content.toString().getBytes(StandardCharsets.US_ASCII);
        int streamLen = streamBytes.length;

        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        offsets.add(pdf.length());
        pdf.append("1 0 obj<< /Type /Catalog /Pages 2 0 R >>endobj\n");

        offsets.add(pdf.length());
        pdf.append("2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1 >>endobj\n");

        offsets.add(pdf.length());
        pdf.append("3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] ");
        pdf.append("/Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>endobj\n");

        offsets.add(pdf.length());
        pdf.append("4 0 obj<< /Length ").append(streamLen).append(" >>stream\n");
        pdf.append(content);
        pdf.append("endstream\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("5 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>endobj\n");

        int xrefPos = pdf.length();
        pdf.append("xref\n0 6\n");
        pdf.append("0000000000 65535 f \n");
        for (int i = 1; i < offsets.size(); i++) {
            pdf.append(String.format("%010d", offsets.get(i))).append(" 00000 n \n");
        }
        pdf.append("trailer<< /Size 6 /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefPos).append("\n%%EOF");

        Path parent = outputFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(outputFile, pdf.toString().getBytes(StandardCharsets.US_ASCII));
    }

    private static String escapePdf(String text) {
        String ascii = toAsciiPrintable(text);
        return ascii.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    /** PDF stream uses US_ASCII; replace unsupported characters to avoid corrupt output. */
    private static String toAsciiPrintable(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (c >= 32 && c <= 126) {
                sb.append(c);
            } else if (c == '\n' || c == '\r' || c == '\t') {
                sb.append(' ');
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }

    private static List<String> wrap(String text, int maxLen) {
        List<String> parts = new ArrayList<>();
        if (text.length() <= maxLen) {
            parts.add(text);
            return parts;
        }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxLen, text.length());
            parts.add(text.substring(start, end));
            start = end;
        }
        return parts;
    }

    private SimplePdfWriter() {
    }

    static SimplePdfWriter create() {
        return new SimplePdfWriter();
    }
}
