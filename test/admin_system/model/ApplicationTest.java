package admin_system.model;

import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;

/**
 * Unit tests for Application class.
 * Tests getter/setter methods for all application properties.
 */
public class ApplicationTest {
    
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Application Model Unit Tests");
        System.out.println("========================================\n");
        
        testApplicationId();
        testApplicantUserId();
        testPositionId();
        testStatus();
        testSubmissionTime();
        testUpdatedTime();
        testStatusNote();
        
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

    private static void testApplicationId() {
        try {
            Application app = new Application();
            app.setApplicationId("APP-001");
            assert app.getApplicationId().equals("APP-001") : "ApplicationId should match";
            pass("testApplicationId");
        } catch (Exception e) {
            fail("testApplicationId", e);
        }
    }

    private static void testApplicantUserId() {
        try {
            Application app = new Application();
            app.setApplicantUserId("USER-123");
            assert app.getApplicantUserId().equals("USER-123") : "ApplicantUserId should match";
            pass("testApplicantUserId");
        } catch (Exception e) {
            fail("testApplicantUserId", e);
        }
    }

    private static void testPositionId() {
        try {
            Application app = new Application();
            app.setPositionId("POS-001");
            assert app.getPositionId().equals("POS-001") : "PositionId should match";
            pass("testPositionId");
        } catch (Exception e) {
            fail("testPositionId", e);
        }
    }

    private static void testStatus() {
        try {
            Application app = new Application();
            app.setStatus(ApplicationStatus.PENDING);
            assert app.getStatus() == ApplicationStatus.PENDING : "Status should be PENDING";
            pass("testStatus");
        } catch (Exception e) {
            fail("testStatus", e);
        }
    }

    private static void testSubmissionTime() {
        try {
            Application app = new Application();
            app.setSubmissionTime("2026-05-20 10:30:00");
            assert app.getSubmissionTime().equals("2026-05-20 10:30:00") : "SubmissionTime should match";
            pass("testSubmissionTime");
        } catch (Exception e) {
            fail("testSubmissionTime", e);
        }
    }

    private static void testUpdatedTime() {
        try {
            Application app = new Application();
            app.setUpdatedTime("2026-05-21 14:00:00");
            assert app.getUpdatedTime().equals("2026-05-21 14:00:00") : "UpdatedTime should match";
            pass("testUpdatedTime");
        } catch (Exception e) {
            fail("testUpdatedTime", e);
        }
    }

    private static void testStatusNote() {
        try {
            Application app = new Application();
            app.setStatusNote("Approved by admin");
            assert app.getStatusNote().equals("Approved by admin") : "StatusNote should match";
            pass("testStatusNote");
        } catch (Exception e) {
            fail("testStatusNote", e);
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
