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
        List<TaNotification> notifs = portal.listNotifications(token, false);
        notifs.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Job", "Event", "Note", "Time", "Read"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (TaNotification n : notifs) {
            model.addRow(new Object[]{
                n.getJobTitle(),
                n.getMessage(),
                n.getNote(),
                n.getCreatedAt(),
                n.isRead() ? "✓" : "●"
            });
        }

        JTable table = UIStyle.createStyledTable(model);
        table.setRowHeight(22);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(44);

        // Highlight unread rows
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    boolean unread = "●".equals(model.getValueAt(row, 4));
                    setBackground(unread ? new Color(255, 248, 220) : Color.WHITE);
                    setForeground(unread ? new Color(40, 40, 40) : new Color(90, 90, 90));
                    setFont(unread ? LABEL_FONT.deriveFont(java.awt.Font.BOLD) : LABEL_FONT);
                }
                return this;
            }
        });

        JDialog dialog = new JDialog(parent, "Notifications", true);
        dialog.setSize(780, 440);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(UIStyle.BG_PAGE);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 8, 12));
        panel.setBackground(UIStyle.BG_PAGE);

        JLabel title = new JLabel("My Notifications  (double-click to view detail)", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_GREEN);
        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Double-click → detail dialog
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && row < notifs.size()) {
                        showNotificationDetail(dialog, notifs.get(row), portal, token, model, row);
                    }
                }
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.setBackground(UIStyle.BG_PAGE);
        JButton markReadBtn = UIStyle.createPrimaryButton("Mark Selected Read");
        JButton markAllBtn  = UIStyle.createAccentButton("Mark All Read");
        JButton refreshBtn  = UIStyle.createSecondaryButton("Refresh");
        JButton closeBtn    = UIStyle.createSecondaryButton("Close");

        markReadBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < notifs.size()) {
                TaNotification n = notifs.get(row);
                portal.markNotificationRead(token, n.getNotificationId());
                n.setRead(true);
                model.setValueAt("✓", row, 4);
                table.repaint();
            }
        });
        markAllBtn.addActionListener(e -> {
            portal.markAllNotificationsRead(token);
            for (int i = 0; i < notifs.size(); i++) {
                notifs.get(i).setRead(true);
                model.setValueAt("✓", i, 4);
            }
            table.repaint();
        });
        refreshBtn.addActionListener(e -> { dialog.dispose(); showNotificationsDialog(parent, context, token); });
        closeBtn.addActionListener(e -> dialog.dispose());

        buttons.add(markReadBtn);
        buttons.add(markAllBtn);
        buttons.add(refreshBtn);
        buttons.add(closeBtn);
        panel.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private static void showNotificationDetail(java.awt.Window owner, TaNotification n,
                                                TaPortalService portal, String token,
                                                DefaultTableModel model, int row) {
        JDialog detail = new JDialog(owner, "Notification Detail", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        detail.setSize(520, 340);
        detail.setLocationRelativeTo(owner);

        JPanel panel = new JPanel(new java.awt.GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 20, 8, 20));
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gc.insets = new java.awt.Insets(5, 4, 5, 8);

        String statusChange = n.getOldStatus() + " → " + n.getNewStatus();
        String[][] fields = {
            {"Job:",           n.getJobTitle().isEmpty() ? n.getPositionId() : n.getJobTitle()},
            {"Position ID:",   n.getPositionId()},
            {"Event:",         n.getMessage()},
            {"Status change:", statusChange},
            {"Note:",          n.getNote().isEmpty() ? "(none)" : n.getNote()},
            {"Time:",          n.getCreatedAt()},
            {"Read:",          n.isRead() ? "Yes" : "No"},
        };

        int r = 0;
        for (String[] pair : fields) {
            gc.gridx = 0; gc.gridy = r; gc.weightx = 0;
            JLabel lbl = new JLabel(pair[0]);
            lbl.setFont(LABEL_FONT.deriveFont(java.awt.Font.BOLD));
            panel.add(lbl, gc);

            gc.gridx = 1; gc.weightx = 1; gc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            JLabel val = new JLabel("<html>" + pair[1].replace("&","&amp;").replace("<","&lt;") + "</html>");
            val.setFont(LABEL_FONT);
            panel.add(val, gc);
            gc.fill = java.awt.GridBagConstraints.NONE;
            r++;
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        JButton markReadBtn = UIStyle.createPrimaryButton("Mark as Read");
        JButton closeBtn    = UIStyle.createSecondaryButton("Close");

        markReadBtn.setEnabled(!n.isRead());
        markReadBtn.addActionListener(e -> {
            portal.markNotificationRead(token, n.getNotificationId());
            n.setRead(true);
            model.setValueAt("✓", row, 4);
            markReadBtn.setEnabled(false);
        });
        closeBtn.addActionListener(e -> detail.dispose());

        btns.add(markReadBtn);
        btns.add(closeBtn);

        detail.setLayout(new BorderLayout());
        detail.add(panel, BorderLayout.CENTER);
        detail.add(btns, BorderLayout.SOUTH);
        detail.setVisible(true);
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
