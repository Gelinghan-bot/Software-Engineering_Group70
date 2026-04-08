package TA_Recruitment_software.admin_system.foundation;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern ACCOUNT_ID_PATTERN = Pattern.compile("[A-Za-z0-9_.@-]{4,40}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+0-9 -]{6,20}$");

    private ValidationUtil() {
    }

    public static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new AppException(fieldName + " is required.");
        }
        return value.trim();
    }

    public static String validateAccountId(String value) {
        String accountId = requireNotBlank(value, "Account ID");
        if (!ACCOUNT_ID_PATTERN.matcher(accountId).matches()) {
            throw new AppException("Account ID must be 4-40 chars and only contain letters, numbers, _, ., @, -");
        }
        return accountId;
    }

    public static String validatePassword(String value) {
        String password = requireNotBlank(value, "Password");
        if (password.length() < 8) {
            throw new AppException("Password length must be at least 8.");
        }
        return password;
    }

    public static String validateName(String value, String fieldName) {
        String name = requireNotBlank(value, fieldName);
        if (name.length() > 80) {
            throw new AppException(fieldName + " is too long.");
        }
        rejectLineBreak(name, fieldName);
        return name;
    }

    public static String validateEmail(String value, boolean required) {
        if (value == null || value.trim().isEmpty()) {
            if (required) {
                throw new AppException("Email is required.");
            }
            return "";
        }
        String email = value.trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new AppException("Invalid email format.");
        }
        return email;
    }

    public static String validatePhone(String value, boolean required) {
        if (value == null || value.trim().isEmpty()) {
            if (required) {
                throw new AppException("Phone is required.");
            }
            return "";
        }
        String phone = value.trim();
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new AppException("Invalid phone format.");
        }
        return phone;
    }

    public static String sanitizeText(String value, String fieldName, int maxLen) {
        if (value == null) {
            return "";
        }
        String cleaned = value.trim();
        rejectLineBreak(cleaned, fieldName);
        if (cleaned.length() > maxLen) {
            throw new AppException(fieldName + " exceeds max length " + maxLen + ".");
        }
        return cleaned;
    }

    public static LocalDate validateDate(String value, String fieldName) {
        String text = requireNotBlank(value, fieldName);
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new AppException(fieldName + " must use format YYYY-MM-DD.");
        }
    }

    public static void ensureTodayOrFuture(LocalDate date, String fieldName) {
        if (date.isBefore(LocalDate.now())) {
            throw new AppException(fieldName + " cannot be in the past.");
        }
    }

    private static void rejectLineBreak(String value, String fieldName) {
        if (value.contains("\n") || value.contains("\r")) {
            throw new AppException(fieldName + " cannot contain line breaks.");
        }
    }
}
