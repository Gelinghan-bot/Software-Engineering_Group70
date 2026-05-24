package mo;

import TA_Recruitment_software.mo_review.InterviewNote;

/**
 * Unit tests for InterviewNote class.
 * Tests getter/setter methods for all note properties.
 */
public class InterviewNoteTest {
    
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("InterviewNote Unit Tests");
        System.out.println("========================================\n");
        
        testNoteId();
        testApplicationId();
        testMoUserId();
        testNoteContent();
        testLastUpdatedAt();
        
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

    private static void testNoteId() {
        try {
            InterviewNote note = new InterviewNote();
            note.setNoteId("NOTE001");
            assert "NOTE001".equals(note.getNoteId()) : "NoteId should match";
            pass("testNoteId");
        } catch (Exception e) {
            fail("testNoteId", e);
        }
    }

    private static void testApplicationId() {
        try {
            InterviewNote note = new InterviewNote();
            note.setApplicationId("APP001");
            assert "APP001".equals(note.getApplicationId()) : "ApplicationId should match";
            pass("testApplicationId");
        } catch (Exception e) {
            fail("testApplicationId", e);
        }
    }

    private static void testMoUserId() {
        try {
            InterviewNote note = new InterviewNote();
            note.setMoUserId("MO000001");
            assert "MO000001".equals(note.getMoUserId()) : "MoUserId should match";
            pass("testMoUserId");
        } catch (Exception e) {
            fail("testMoUserId", e);
        }
    }

    private static void testNoteContent() {
        try {
            InterviewNote note = new InterviewNote();
            note.setNoteContent("Good communication skills, solid Java knowledge.");
            assert note.getNoteContent().equals("Good communication skills, solid Java knowledge.") : "NoteContent should match";
            pass("testNoteContent");
        } catch (Exception e) {
            fail("testNoteContent", e);
        }
    }

    private static void testLastUpdatedAt() {
        try {
            InterviewNote note = new InterviewNote();
            note.setLastUpdatedAt("2026-05-25 10:30:00");
            assert note.getLastUpdatedAt().equals("2026-05-25 10:30:00") : "LastUpdatedAt should match";
            pass("testLastUpdatedAt");
        } catch (Exception e) {
            fail("testLastUpdatedAt", e);
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
