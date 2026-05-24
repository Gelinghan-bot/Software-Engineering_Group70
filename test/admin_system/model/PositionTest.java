package admin_system.model;

import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.PositionStatus;

/**
 * Unit tests for Position class.
 * Tests getter/setter methods for all position properties.
 */
public class PositionTest {
    
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Position Model Unit Tests");
        System.out.println("========================================\n");
        
        testPositionId();
        testJobTitle();
        testJobType();
        testHeadcount();
        testGrade();
        testMajor();
        testResponsibleMO();
        testJobDescription();
        testRequirements();
        testDeadline();
        testStatus();
        
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

    private static void testPositionId() {
        try {
            Position pos = new Position();
            pos.setPositionId("POS-001");
            assert pos.getPositionId().equals("POS-001") : "PositionId should match";
            pass("testPositionId");
        } catch (Exception e) {
            fail("testPositionId", e);
        }
    }

    private static void testJobTitle() {
        try {
            Position pos = new Position();
            pos.setJobTitle("Software Engineering TA");
            assert pos.getJobTitle().equals("Software Engineering TA") : "JobTitle should match";
            pass("testJobTitle");
        } catch (Exception e) {
            fail("testJobTitle", e);
        }
    }

    private static void testJobType() {
        try {
            Position pos = new Position();
            pos.setJobType("Teaching");
            assert pos.getJobType().equals("Teaching") : "JobType should match";
            pass("testJobType");
        } catch (Exception e) {
            fail("testJobType", e);
        }
    }

    private static void testHeadcount() {
        try {
            Position pos = new Position();
            pos.setHeadcount(3);
            assert pos.getHeadcount() == 3 : "Headcount should be 3";
            pass("testHeadcount");
        } catch (Exception e) {
            fail("testHeadcount", e);
        }
    }

    private static void testGrade() {
        try {
            Position pos = new Position();
            pos.setGrade("Grade A");
            assert pos.getGrade().equals("Grade A") : "Grade should match";
            pass("testGrade");
        } catch (Exception e) {
            fail("testGrade", e);
        }
    }

    private static void testMajor() {
        try {
            Position pos = new Position();
            pos.setMajor("Computer Science");
            assert pos.getMajor().equals("Computer Science") : "Major should match";
            pass("testMajor");
        } catch (Exception e) {
            fail("testMajor", e);
        }
    }

    private static void testResponsibleMO() {
        try {
            Position pos = new Position();
            pos.setResponsibleMO("Dr. Smith");
            assert pos.getResponsibleMO().equals("Dr. Smith") : "ResponsibleMO should match";
            pass("testResponsibleMO");
        } catch (Exception e) {
            fail("testResponsibleMO", e);
        }
    }

    private static void testJobDescription() {
        try {
            Position pos = new Position();
            pos.setJobDescription("Assist with teaching and grading");
            assert pos.getJobDescription().equals("Assist with teaching and grading") : "JobDescription should match";
            pass("testJobDescription");
        } catch (Exception e) {
            fail("testJobDescription", e);
        }
    }

    private static void testRequirements() {
        try {
            Position pos = new Position();
            pos.setRequirements("Java programming experience required");
            assert pos.getRequirements().equals("Java programming experience required") : "Requirements should match";
            pass("testRequirements");
        } catch (Exception e) {
            fail("testRequirements", e);
        }
    }

    private static void testDeadline() {
        try {
            Position pos = new Position();
            pos.setDeadline("2026-06-30");
            assert pos.getDeadline().equals("2026-06-30") : "Deadline should match";
            pass("testDeadline");
        } catch (Exception e) {
            fail("testDeadline", e);
        }
    }

    private static void testStatus() {
        try {
            Position pos = new Position();
            pos.setStatus(PositionStatus.OPEN);
            assert pos.getStatus() == PositionStatus.OPEN : "Status should be OPEN";
            pass("testStatus");
        } catch (Exception e) {
            fail("testStatus", e);
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
