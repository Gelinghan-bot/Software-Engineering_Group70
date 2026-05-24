import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.ValidationUtil;

import java.time.LocalDate;

/**
 * Unit tests for ValidationUtil class.
 * Run this class as a regular Java application to execute all tests.
 * 
 * @author Group70
 */
public class ValidationUtilTest {
    
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("ValidationUtil Unit Tests");
        System.out.println("========================================\n");
        
        testRequireNotBlank_Valid();
        testRequireNotBlank_Null();
        testRequireNotBlank_Empty();
        testValidateAccountId_Valid();
        testValidateAccountId_TooShort();
        testValidatePassword_Valid();
        testValidatePassword_TooShort();
        testValidateEmail_Valid();
        testValidateEmail_Invalid();
        testValidatePhone_Valid();
        testValidatePhone_Invalid();
        testValidateDate_Valid();
        testValidateDate_InvalidFormat();
        testSanitizeText_Valid();
        
        System.out.println("\n========================================");
        System.out.println("Test Summary");
        System.out.println("========================================");
        System.out.println("Total: " + (passed + failed));
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("========================================");
        
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testRequireNotBlank_Valid() {
        try {
            String result = ValidationUtil.requireNotBlank("  Hello World  ", "Test");
            assert result.equals("Hello World") : "Should trim whitespace";
            pass("requireNotBlank_Valid");
        } catch (Exception e) {
            fail("requireNotBlank_Valid", e);
        }
    }

    private static void testRequireNotBlank_Null() {
        try {
            ValidationUtil.requireNotBlank(null, "Test");
            fail("requireNotBlank_Null", "Should throw AppException");
        } catch (AppException e) {
            pass("requireNotBlank_Null");
        } catch (Exception e) {
            fail("requireNotBlank_Null", e);
        }
    }

    private static void testRequireNotBlank_Empty() {
        try {
            ValidationUtil.requireNotBlank("  ", "Test");
            fail("requireNotBlank_Empty", "Should throw AppException");
        } catch (AppException e) {
            pass("requireNotBlank_Empty");
        } catch (Exception e) {
            fail("requireNotBlank_Empty", e);
        }
    }

    private static void testValidateAccountId_Valid() {
        try {
            String result = ValidationUtil.validateAccountId("user@test.com");
            assert result.equals("user@test.com");
            pass("validateAccountId_Valid");
        } catch (Exception e) {
            fail("validateAccountId_Valid", e);
        }
    }

    private static void testValidateAccountId_TooShort() {
        try {
            ValidationUtil.validateAccountId("ab");
            fail("validateAccountId_TooShort", "Should throw AppException");
        } catch (AppException e) {
            pass("validateAccountId_TooShort");
        } catch (Exception e) {
            fail("validateAccountId_TooShort", e);
        }
    }

    private static void testValidatePassword_Valid() {
        try {
            String result = ValidationUtil.validatePassword("MyPassword123");
            assert result.equals("MyPassword123");
            pass("validatePassword_Valid");
        } catch (Exception e) {
            fail("validatePassword_Valid", e);
        }
    }

    private static void testValidatePassword_TooShort() {
        try {
            ValidationUtil.validatePassword("Ab1");
            fail("validatePassword_TooShort", "Should throw AppException");
        } catch (AppException e) {
            pass("validatePassword_TooShort");
        } catch (Exception e) {
            fail("validatePassword_TooShort", e);
        }
    }

    private static void testValidateEmail_Valid() {
        try {
            String result = ValidationUtil.validateEmail("test@example.com", true);
            assert result.equals("test@example.com");
            pass("validateEmail_Valid");
        } catch (Exception e) {
            fail("validateEmail_Valid", e);
        }
    }

    private static void testValidateEmail_Invalid() {
        try {
            ValidationUtil.validateEmail("invalid-email", true);
            fail("validateEmail_Invalid", "Should throw AppException");
        } catch (AppException e) {
            pass("validateEmail_Invalid");
        } catch (Exception e) {
            fail("validateEmail_Invalid", e);
        }
    }

    private static void testValidatePhone_Valid() {
        try {
            String result = ValidationUtil.validatePhone("13812345678", true);
            assert result.equals("13812345678");
            pass("validatePhone_Valid");
        } catch (Exception e) {
            fail("validatePhone_Valid", e);
        }
    }

    private static void testValidatePhone_Invalid() {
        try {
            ValidationUtil.validatePhone("abc", true);
            fail("validatePhone_Invalid", "Should throw AppException");
        } catch (AppException e) {
            pass("validatePhone_Invalid");
        } catch (Exception e) {
            fail("validatePhone_Invalid", e);
        }
    }

    private static void testValidateDate_Valid() {
        try {
            LocalDate result = ValidationUtil.validateDate("2026-12-31", "Deadline");
            assert result.getYear() == 2026;
            pass("validateDate_Valid");
        } catch (Exception e) {
            fail("validateDate_Valid", e);
        }
    }

    private static void testValidateDate_InvalidFormat() {
        try {
            ValidationUtil.validateDate("31-12-2026", "Deadline");
            fail("validateDate_InvalidFormat", "Should throw AppException");
        } catch (AppException e) {
            pass("validateDate_InvalidFormat");
        } catch (Exception e) {
            fail("validateDate_InvalidFormat", e);
        }
    }

    private static void testSanitizeText_Valid() {
        try {
            String result = ValidationUtil.sanitizeText("  Hello  ", "Test", 100);
            assert result.equals("Hello");
            pass("sanitizeText_Valid");
        } catch (Exception e) {
            fail("sanitizeText_Valid", e);
        }
    }

    private static void pass(String testName) {
        System.out.println("[PASS] " + testName);
        passed++;
    }

    private static void fail(String testName, Exception e) {
        System.out.println("[FAIL] " + testName + " - " + e.getMessage());
        failed++;
    }

    private static void fail(String testName, String message) {
        System.out.println("[FAIL] " + testName + " - " + message);
        failed++;
    }
}
