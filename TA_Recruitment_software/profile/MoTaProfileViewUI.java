package TA_Recruitment_software.profile;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import TA_Recruitment_software.admin_system.model.User;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public final class MoTaProfileViewUI {

    private MoTaProfileViewUI() {
    }

    public static void showDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null || token.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Please login as MO first.", "MO only", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            List<User> tas = context.getMoTaProfileViewService().listApprovedTas(token);
            if (tas.isEmpty()) {
                JOptionPane.showMessageDialog(
                    parent,
                    "No approved TA accounts to display.",
                    "TA directory",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            DefaultTableModel model = new DefaultTableModel(
                new Object[]{"User ID", "Account", "Full name", "Major", "Email", "Has CV"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            for (User u : tas) {
                String cv = u.getCvFilePath();
                String hasCv = (cv != null && !cv.trim().isEmpty()) ? "Yes" : "No";
                model.addRow(new Object[]{
                    u.getUserId(),
                    u.getAccountId(),
                    n(u.getFullName()),
                    n(u.getMajor()),
                    n(u.getEmail()),
                    hasCv
                });
            }

            JTable table = UIStyle.createStyledTable(model);
            JScrollPane scroll = UIStyle.wrapTableInScrollPane(table);
            scroll.setPreferredSize(new Dimension(880, 320));

            JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
            south.setBackground(UIStyle.BG_PAGE);
            JButton viewBtn = UIStyle.createPrimaryButton("View profile & CV");
            JButton openCvBtn = UIStyle.createAccentButton("Open CV file");
            JButton inviteBtn = UIStyle.createSecondaryButton("Send Interview Invitation");
            south.add(viewBtn);
            south.add(openCvBtn);
            south.add(inviteBtn);

            JDialog dialog = new JDialog(parent, "TA profiles (MO view)", true);
            dialog.getContentPane().setBackground(UIStyle.BG_PAGE);
            dialog.setLayout(new BorderLayout(8, 8));
            JLabel headerLbl = new JLabel("Approved TAs — select a row, then choose an action.");
            headerLbl.setFont(UIStyle.FONT_BODY_BOLD);
            headerLbl.setForeground(UIStyle.TEXT_PRIMARY);
            headerLbl.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));
            dialog.add(headerLbl, BorderLayout.NORTH);
            dialog.add(scroll, BorderLayout.CENTER);
            dialog.add(south, BorderLayout.SOUTH);
            dialog.pack();
            dialog.setLocationRelativeTo(parent);

            viewBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Please select a TA row first.");
                    return;
                }
                String taUserId = (String) model.getValueAt(row, 0);
                try {
                    User ta = context.getMoTaProfileViewService().getTaProfileForMo(token, taUserId);
                    showDetailDialog(parent, ta);
                } catch (AppException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            openCvBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Please select a TA row first.");
                    return;
                }
                String taUserId = (String) model.getValueAt(row, 0);
                try {
                    User ta = context.getMoTaProfileViewService().getTaProfileForMo(token, taUserId);
                    openCvFile(parent, ta);
                } catch (AppException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            inviteBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Please select a TA row first to send an invitation.");
                    return;
                }
                String taUserId = (String) model.getValueAt(row, 0);
                String taName = (String) model.getValueAt(row, 2);
                
                try {
                    // Fetch MO's positions
                    List<TA_Recruitment_software.admin_system.model.Position> myPositions = context.getMoPublishService().listMyPositions(token);
                    
                    if (myPositions.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "You haven't published any positions yet. Cannot send invitation.");
                        return;
                    }

                    // Filter only open positions
                    List<TA_Recruitment_software.admin_system.model.Position> openPositions = new java.util.ArrayList<>();
                    for (TA_Recruitment_software.admin_system.model.Position p : myPositions) {
                        if (p.getStatus() == TA_Recruitment_software.admin_system.model.PositionStatus.OPEN) {
                            openPositions.add(p);
                        }
                    }

                    if (openPositions.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "You don't have any OPEN positions. Cannot send invitation.");
                        return;
                    }

                    javax.swing.JComboBox<String> positionCombo = new javax.swing.JComboBox<>();
                    for (TA_Recruitment_software.admin_system.model.Position p : openPositions) {
                        positionCombo.addItem(p.getJobTitle() + " (" + p.getPositionId() + ")");
                    }

                    // Show a dialog for the MO to type their message and select position
                    JPanel invitePanel = new JPanel(new BorderLayout(0, 5));
                    
                    JPanel topPanel = new JPanel(new BorderLayout());
                    topPanel.add(new JLabel("Select Position: "), BorderLayout.WEST);
                    topPanel.add(positionCombo, BorderLayout.CENTER);
                    invitePanel.add(topPanel, BorderLayout.NORTH);

                    JTextArea msgArea = new JTextArea(8, 30);
                    msgArea.setLineWrap(true);
                    msgArea.setWrapStyleWord(true);
                    msgArea.setText("Dear " + taName + ",\n\nI reviewed your CV and would like to invite you for an interview for the selected position. Please let me know your availability.\n\nBest, MO");
                    JScrollPane msgScroll = new JScrollPane(msgArea);
                    invitePanel.add(msgScroll, BorderLayout.CENTER);
                    
                    int option = JOptionPane.showConfirmDialog(dialog, invitePanel, "Send Interview Invitation to " + taName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (option == JOptionPane.OK_OPTION) {
                        String message = msgArea.getText().trim();
                        int selectedIndex = positionCombo.getSelectedIndex();
                        
                        if (selectedIndex >= 0 && !message.isEmpty()) {
                            TA_Recruitment_software.admin_system.model.Position selectedPos = openPositions.get(selectedIndex);
                            // Simulating successful operation here
                            JOptionPane.showMessageDialog(dialog, "Interview invitation for position '" + selectedPos.getJobTitle() + "' sent to " + taName + " successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Message cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (AppException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            dialog.setVisible(true);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showDetailDialog(JFrame parent, User ta) {
        JTextField userIdF = ro(ta.getUserId());
        JTextField accountF = ro(ta.getAccountId());
        JTextField nameF = ro(ta.getFullName());
        JTextField studentF = ro(ta.getStudentId());
        JTextField majorF = ro(ta.getMajor());
        JTextField emailF = ro(ta.getEmail());
        JTextField phoneF = ro(ta.getPhone());
        JTextArea skillsA = new JTextArea(n(ta.getSkills()), 4, 28);
        skillsA.setEditable(false);
        skillsA.setLineWrap(true);
        skillsA.setWrapStyleWord(true);
        JTextField cvPathF = ro(ta.getCvFilePath() == null ? "" : ta.getCvFilePath());

        Path cvPath = resolveCvPath(ta.getCvFilePath());
        boolean cvOk = cvPath != null && Files.isRegularFile(cvPath);
        JTextField cvStatusF = ro(cvOk ? "File exists (can open)" : (ta.getCvFilePath() == null || ta.getCvFilePath().trim().isEmpty()
            ? "No CV uploaded"
            : "Path recorded but file missing on disk"));

        JPanel form = new JPanel(new java.awt.GridLayout(0, 2, 8, 8));
        form.setBackground(UIStyle.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        addRow(form, "User ID", userIdF);
        addRow(form, "Account", accountF);
        addRow(form, "Full name", nameF);
        addRow(form, "Student ID", studentF);
        addRow(form, "Major", majorF);
        addRow(form, "Email", emailF);
        addRow(form, "Phone", phoneF);
        JLabel skillsLabel = UIStyle.createFieldLabel("Skills");
        form.add(skillsLabel);
        JScrollPane skillsPane = new JScrollPane(skillsA);
        UIStyle.styleScrollPane(skillsPane);
        form.add(skillsPane);
        addRow(form, "CV path", cvPathF);
        addRow(form, "CV status", cvStatusF);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        south.setBackground(UIStyle.BG_PAGE);
        JButton openBtn = UIStyle.createAccentButton("Open CV with system viewer");
        south.add(openBtn);
        openBtn.addActionListener(e -> openCvFile(parent, ta));

        JDialog d = new JDialog(parent, "TA profile — " + ta.getFullName(), true);
        d.getContentPane().setBackground(UIStyle.BG_PAGE);
        d.setLayout(new BorderLayout());
        d.add(form, BorderLayout.CENTER);
        d.add(south, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }

    private static void addRow(JPanel form, String label, JTextField field) {
        JLabel lbl = UIStyle.createFieldLabel(label);
        form.add(lbl);
        form.add(field);
    }

    private static JTextField ro(String text) {
        return UIStyle.createReadOnlyField(text);
    }

    private static String n(String s) {
        return s == null ? "" : s;
    }

    private static Path resolveCvPath(String stored) {
        if (stored == null || stored.trim().isEmpty()) {
            return null;
        }
        Path p = Paths.get(stored.trim());
        return p;
    }

    private static void openCvFile(JFrame parent, User ta) {
        String stored = ta.getCvFilePath();
        if (stored == null || stored.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "This TA has not uploaded a CV yet.");
            return;
        }
        Path path = resolveCvPath(stored);
        if (!Files.isRegularFile(path)) {
            JOptionPane.showMessageDialog(
                parent,
                "CV file not found at:\n" + path.toAbsolutePath(),
                "Missing file",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(parent, "Desktop API not supported on this environment.");
            return;
        }
        try {
            Desktop.getDesktop().open(path.toFile());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                parent,
                "Could not open file: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
