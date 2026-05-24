package admin_system.model;

import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.ApprovalStatus;
import TA_Recruitment_software.admin_system.model.User;

/**
 * Unit tests for User class.
 * Tests getter/setter methods for all user properties.
 */
public class UserTest {
    
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("User Model Unit Tests");
        System.out.println("========================================\n");
        
        testUserId();
        testRole();
        testAccountId();
        testFullName();
        testStudentId();
        testEmail();
        testPhone();
        testSkills();
        testApprovalStatus();
        testEnabled();
        testPasswordHash();
        testDepartment();
        testCvFilePath();
        
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

    private static void testUserId() {
        try {
            User user = new User();
            user.setUserId("test-user-123");
            assert user.getUserId().equals("test-user-123") : "UserId should match";
            pass("testUserId");
        } catch (Exception e) {
            fail("testUserId", e);
        }
    }

    private static void testRole() {
        try {
            User user = new User();
            user.setRole(Role.TA);
            assert user.getRole() == Role.TA : "Role should be TA";
            pass("testRole");
        } catch (Exception e) {
            fail("testRole", e);
        }
    }

    private static void testAccountId() {
        try {
            User user = new User();
            user.setAccountId("test_account");
            assert user.getAccountId().equals("test_account") : "AccountId should match";
            pass("testAccountId");
        } catch (Exception e) {
            fail("testAccountId", e);
        }
    }

    private static void testFullName() {
        try {
            User user = new User();
            user.setFullName("John Doe");
            assert user.getFullName().equals("John Doe") : "FullName should match";
            pass("testFullName");
        } catch (Exception e) {
            fail("testFullName", e);
        }
    }

    private static void testStudentId() {
        try {
            User user = new User();
            user.setStudentId("20240001");
            assert user.getStudentId().equals("20240001") : "StudentId should match";
            pass("testStudentId");
        } catch (Exception e) {
            fail("testStudentId", e);
        }
    }

    private static void testEmail() {
        try {
            User user = new User();
            user.setEmail("test@example.com");
            assert user.getEmail().equals("test@example.com") : "Email should match";
            pass("testEmail");
        } catch (Exception e) {
            fail("testEmail", e);
        }
    }

    private static void testPhone() {
        try {
            User user = new User();
            user.setPhone("13812345678");
            assert user.getPhone().equals("13812345678") : "Phone should match";
            pass("testPhone");
        } catch (Exception e) {
            fail("testPhone", e);
        }
    }

    private static void testSkills() {
        try {
            User user = new User();
            user.setSkills("Java, Python, SQL");
            assert user.getSkills().equals("Java, Python, SQL") : "Skills should match";
            pass("testSkills");
        } catch (Exception e) {
            fail("testSkills", e);
        }
    }

    private static void testApprovalStatus() {
        try {
            User user = new User();
            user.setApprovalStatus(ApprovalStatus.PENDING);
            assert user.getApprovalStatus() == ApprovalStatus.PENDING : "ApprovalStatus should be PENDING";
            pass("testApprovalStatus");
        } catch (Exception e) {
            fail("testApprovalStatus", e);
        }
    }

    private static void testEnabled() {
        try {
            User user = new User();
            user.setEnabled(true);
            assert user.isEnabled() : "Enabled should be true";
            user.setEnabled(false);
            assert !user.isEnabled() : "Enabled should be false";
            pass("testEnabled");
        } catch (Exception e) {
            fail("testEnabled", e);
        }
    }

    private static void testPasswordHash() {
        try {
            User user = new User();
            user.setPasswordHash("hashed_password_123");
            assert user.getPasswordHash().equals("hashed_password_123") : "PasswordHash should match";
            pass("testPasswordHash");
        } catch (Exception e) {
            fail("testPasswordHash", e);
        }
    }

    private static void testDepartment() {
        try {
            User user = new User();
            user.setDepartment("EECS");
            assert user.getDepartment().equals("EECS") : "Department should match";
            pass("testDepartment");
        } catch (Exception e) {
            fail("testDepartment", e);
        }
    }

    private static void testCvFilePath() {
        try {
            User user = new User();
            user.setCvFilePath("data/cv/test.pdf");
            assert user.getCvFilePath().equals("data/cv/test.pdf") : "CvFilePath should match";
            pass("testCvFilePath");
        } catch (Exception e) {
            fail("testCvFilePath", e);
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
}
