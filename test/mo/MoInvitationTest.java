package mo;

import TA_Recruitment_software.mo_review.MoInvitation;

/**
 * Unit tests for MoInvitation class.
 * Tests getter/setter methods for all invitation properties.
 */
public class MoInvitationTest {
    
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("MoInvitation Unit Tests");
        System.out.println("========================================\n");
        
        testInvitationId();
        testMoUserId();
        testTaUserId();
        testPositionId();
        testMessage();
        testCreatedAt();
        
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

    private static void testInvitationId() {
        try {
            MoInvitation inv = new MoInvitation();
            inv.setInvitationId("INV001");
            assert "INV001".equals(inv.getInvitationId()) : "InvitationId should match";
            pass("testInvitationId");
        } catch (Exception e) {
            fail("testInvitationId", e);
        }
    }

    private static void testMoUserId() {
        try {
            MoInvitation inv = new MoInvitation();
            inv.setMoUserId("MO000001");
            assert "MO000001".equals(inv.getMoUserId()) : "MoUserId should match";
            pass("testMoUserId");
        } catch (Exception e) {
            fail("testMoUserId", e);
        }
    }

    private static void testTaUserId() {
        try {
            MoInvitation inv = new MoInvitation();
            inv.setTaUserId("TA000001");
            assert "TA000001".equals(inv.getTaUserId()) : "TaUserId should match";
            pass("testTaUserId");
        } catch (Exception e) {
            fail("testTaUserId", e);
        }
    }

    private static void testPositionId() {
        try {
            MoInvitation inv = new MoInvitation();
            inv.setPositionId("POS001");
            assert "POS001".equals(inv.getPositionId()) : "PositionId should match";
            pass("testPositionId");
        } catch (Exception e) {
            fail("testPositionId", e);
        }
    }

    private static void testMessage() {
        try {
            MoInvitation inv = new MoInvitation();
            inv.setMessage("We would like to invite you for an interview.");
            assert inv.getMessage().equals("We would like to invite you for an interview.") : "Message should match";
            pass("testMessage");
        } catch (Exception e) {
            fail("testMessage", e);
        }
    }

    private static void testCreatedAt() {
        try {
            MoInvitation inv = new MoInvitation();
            inv.setCreatedAt("2026-05-25 14:00:00");
            assert inv.getCreatedAt().equals("2026-05-25 14:00:00") : "CreatedAt should match";
            pass("testCreatedAt");
        } catch (Exception e) {
            fail("testCreatedAt", e);
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
