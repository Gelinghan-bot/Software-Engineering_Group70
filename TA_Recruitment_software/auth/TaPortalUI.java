package TA_Recruitment_software.auth;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.Role;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * TA portal UI: notifications, compliance summary, application history, PDF export.
 */
public final class TaPortalUI {
    private static final Color PRIMARY_GREEN = UIStyle.PRIMARY;
    private static final Color ERROR_RED = UIStyle.DANGER;
    private static final Font TITLE_FONT = UIStyle.FONT_TITLE;
    private static final Font LABEL_FONT = UIStyle.FONT_BODY;

    private TaPortalUI() {
    }

    /**
     * Hub menu for TA personal features (call from PERSONAL navigation after login).
     */
    public static void showTaHub(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as TA first.", "Not Logged In", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            context.getSessionManager().requireRole(token, Role.TA);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        TaPortalService portal = TaPortalService.fromContext(context);
        portal.syncNotifications(token);
        int unread = portal.countUnreadNotifications(token);
        TaComplianceService.TaComplianceSummary summary = portal.getCurrentComplianceSummary(token);

        String[] options = {
            "Notifications" + (unread > 0 ? " (" + unread + " unread)" : ""),
            "Application & Hiring Limits",
            "Application History (All Semesters)",
            "Export Profile + CV Link (PDF)",
            "Apply for Job (with limits check)",
            "Close"
        };
        int choice = JOptionPane.showOptionDialog(
            parent,
            "Semester: " + summary.getSemester() + "\n"
                + "Active applications: " + summary.getActiveApplications() + "/" + summary.getMaxActiveApplications() + "\n"
                + "Hired courses: " + summary.getHiredCourses() + "/" + summary.getMaxHiredCourses(),
            "TA Portal",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        );

        if (choice == 0) {
            showNotificationsDialog(parent, context, token);
        } else if (choice == 1) {
            showComplianceDialog(parent, context, token);
        } else if (choice == 2) {
            showApplicationHistoryDialog(parent, context, token);
        } else if (choice == 3) {
            exportProfilePdf(parent, context, token);
        } else if (choice == 4) {
            showApplyWithComplianceDialog(parent, context, token);
        }
    }

    public static void showNotificationsDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        TaPortalService portal = TaPortalService.fromContext(context);
        portal.syncNotifications(token);
        List<TaNotification> notifications = portal.listNotifications(token, false);

        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Message", "Time", "Read"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (TaNotification n : notifications) {
            model.addRow(new Object[]{
                n.getNotificationId(),
                n.getMessage(),
                n.getCreatedAt(),
                n.isRead() ? "Yes" : "No"
            });
        }

        JTable table = UIStyle.createStyledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(420);

        JDialog dialog = new JDialog(parent, "Application Notifications", true);
        dialog.setSize(720, 420);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(UIStyle.BG_PAGE);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(UIStyle.BG_PAGE);

        JLabel title = new JLabel("Application Status Notifications", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_GREEN);
        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.setBackground(UIStyle.BG_PAGE);
        JButton markReadBtn = UIStyle.createPrimaryButton("Mark Selected Read");
        JButton markAllBtn = UIStyle.createAccentButton("Mark All Read");
        JButton refreshBtn = UIStyle.createSecondaryButton("Refresh");
        JButton closeBtn = UIStyle.createSecondaryButton("Close");

        markReadBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String id = (String) model.getValueAt(row, 0);
                portal.markNotificationRead(token, id);
                model.setValueAt("Yes", row, 3);
            }
        });
        markAllBtn.addActionListener(e -> {
            portal.markAllNotificationsRead(token);
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt("Yes", i, 3);
            }
        });
        refreshBtn.addActionListener(e -> {
            dialog.dispose();
            showNotificationsDialog(parent, context, token);
        });
        closeBtn.addActionListener(e -> dialog.dispose());

        buttons.add(markReadBtn);
        buttons.add(markAllBtn);
        buttons.add(refreshBtn);
        buttons.add(closeBtn);
        panel.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    public static void showComplianceDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        TaComplianceService.TaComplianceSummary s = TaPortalService.fromContext(context).getCurrentComplianceSummary(token);
        String message = "School rules (current semester: " + s.getSemester() + ")\n\n"
            + "Active applications: " + s.getActiveApplications() + " / " + s.getMaxActiveApplications() + "\n"
            + "Hired / offered courses: " + s.getHiredCourses() + " / " + s.getMaxHiredCourses() + "\n\n"
            + (s.canApplyMore()
            ? "You may still apply for more positions within these limits."
            : "You have reached a limit. New applications may be blocked.");
        JOptionPane.showMessageDialog(parent, message, "Application & Hiring Limits", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showApplicationHistoryDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        TaPortalService portal = TaPortalService.fromContext(context);
        List<String> semesters = portal.listHistorySemesters(token);
        String[] filterOptions = new String[semesters.size() + 1];
        filterOptions[0] = "All Semesters";
        for (int i = 0; i < semesters.size(); i++) {
            filterOptions[i + 1] = semesters.get(i);
        }
        String selected = (String) JOptionPane.showInputDialog(
            parent,
            "Filter by semester:",
            "Application History",
            JOptionPane.QUESTION_MESSAGE,
            null,
            filterOptions,
            filterOptions[0]
        );
        if (selected == null) {
            return;
        }

        List<TaApplicationHistoryEntry> entries = "All Semesters".equals(selected)
            ? portal.listApplicationHistory(token)
            : portal.listApplicationHistoryBySemester(token, selected);

        if (entries.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No application records found.", "History", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"App ID", "Position", "Job Title", "Semester", "Status", "Submitted", "Updated", "Note"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (TaApplicationHistoryEntry e : entries) {
            model.addRow(new Object[]{
                e.getApplicationId(),
                e.getPositionId(),
                e.getJobTitle(),
                e.getSemester(),
                e.getStatus(),
                e.getSubmissionTime(),
                e.getUpdatedTime(),
                e.getStatusNote() == null ? "" : e.getStatusNote()
            });
        }

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(900, 360));
        JOptionPane.showMessageDialog(parent, scroll, "Application History — " + selected, JOptionPane.PLAIN_MESSAGE);
    }

    public static void exportProfilePdf(JFrame parent, RecruitmentSystemContext context, String token) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Profile PDF");
        chooser.setSelectedFile(new java.io.File("ta_profile_export.pdf"));
        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Path saved = TaPortalService.fromContext(context).exportProfilePdf(token, chooser.getSelectedFile().toPath());
            JOptionPane.showMessageDialog(
                parent,
                "Profile exported successfully:\n" + saved.toAbsolutePath(),
                "PDF Export",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showApplyWithComplianceDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        String positionId = JOptionPane.showInputDialog(parent, "Enter Position ID to apply:", "Apply with Compliance Check", JOptionPane.QUESTION_MESSAGE);
        if (positionId == null || positionId.trim().isEmpty()) {
            return;
        }
        try {
            Application app = TaPortalService.fromContext(context).applyForJobWithCompliance(token, positionId.trim());
            JOptionPane.showMessageDialog(
                parent,
                "Applied successfully.\nApplication ID: " + app.getApplicationId() + "\nStatus: " + app.getStatus(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Apply Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Called after TA login to sync notifications and optionally alert unread count.
     */
    static void onTaLogin(JFrame parent, RecruitmentSystemContext context, String token) {
        try {
            TaPortalService portal = TaPortalService.fromContext(context);
            portal.syncNotifications(token);
            int unread = portal.countUnreadNotifications(token);
            if (unread > 0) {
                int open = JOptionPane.showConfirmDialog(
                    parent,
                    "You have " + unread + " unread application notification(s).\nOpen notifications now?",
                    "New Notifications",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
                );
                if (open == JOptionPane.YES_OPTION) {
                    showNotificationsDialog(parent, context, token);
                }
            }
        } catch (AppException ignored) {
            // Not a TA session
        }
    }
}
