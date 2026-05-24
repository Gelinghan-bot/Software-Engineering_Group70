package ta_jobs;

import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.PositionStatus;
import TA_Recruitment_software.ta_jobs.JobMatchResult;

/**
 * Unit tests for JobMatchResult class.
 * Tests the core job match result data holder functionality.
 */
public class JobMatchResultTest {
    
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("JobMatchResult Unit Tests");
        System.out.println("========================================\n");
        
        testBasicMatchResult();
        testScoreBoundaryClamping();
        testNullPosition();
        
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

    private static void testBasicMatchResult() {
        try {
            Position pos = createTestPosition("POS001", "Java TA");
            JobMatchResult result = new JobMatchResult(pos, 85, "Good Java skills match");
            
            assert result.getPosition() == pos : "Position should match";
            assert result.getScorePercent() == 85 : "Score should be 85";
            assert result.getReason().equals("Good Java skills match") : "Reason should match";
            
            pass("testBasicMatchResult");
        } catch (Exception e) {
            fail("testBasicMatchResult", e);
        }
    }

    private static void testScoreBoundaryClamping() {
        try {
            Position pos = createTestPosition("POS002", "Python TA");
            
            // Test score clamping: score > 100 should be clamped to 100
            JobMatchResult highScore = new JobMatchResult(pos, 150, "Perfect match");
            assert highScore.getScorePercent() == 100 : "Score should be clamped to 100";
            
            // Test score clamping: score < 0 should be clamped to 0
            JobMatchResult lowScore = new JobMatchResult(pos, -20, "No match");
            assert lowScore.getScorePercent() == 0 : "Score should be clamped to 0";
            
            pass("testScoreBoundaryClamping");
        } catch (Exception e) {
            fail("testScoreBoundaryClamping", e);
        }
    }

    private static void testNullPosition() {
        try {
            JobMatchResult result = new JobMatchResult(null, 50, "No position");
            
            assert result.getPosition() == null : "Position should be null";
            assert result.getScorePercent() == 50 : "Score should be 50";
            assert result.getReason().equals("No position") : "Reason should match";
            
            pass("testNullPosition");
        } catch (Exception e) {
            fail("testNullPosition", e);
        }
    }

    private static Position createTestPosition(String id, String title) {
        Position pos = new Position();
        pos.setPositionId(id);
        pos.setJobTitle(title);
        pos.setJobType("Grading");
        pos.setGrade("Year 2");
        pos.setMajor("Computer Science");
        pos.setResponsibleMO("Test MO");
        pos.setJobDescription("Test job");
        pos.setRequirements("Java skills");
        pos.setDeadline("2026-12-31");
        pos.setStatus(PositionStatus.OPEN);
        pos.setHeadcount(2);
        return pos;
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
