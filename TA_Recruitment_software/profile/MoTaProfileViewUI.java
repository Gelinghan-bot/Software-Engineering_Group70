package TA_Recruitment_software.profile;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.User;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Desktop;
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

            JTable table = new JTable(model);
            table.setFont(new Font("Arial", Font.PLAIN, 13));
            table.setRowHeight(22);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(880, 320));

            JPanel south = new JPanel();
            JButton viewBtn = new JButton("View profile & CV");
            JButton openCvBtn = new JButton("Open CV file");
            south.add(viewBtn);
            south.add(openCvBtn);

            JDialog dialog = new JDialog(parent, "TA profiles (MO view)", true);
            dialog.setLayout(new BorderLayout(8, 8));
            dialog.add(new JLabel("Approved TAs — select a row, then choose an action."), BorderLayout.NORTH);
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

        JPanel form = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        addRow(form, "User ID", userIdF);
        addRow(form, "Account", accountF);
        addRow(form, "Full name", nameF);
        addRow(form, "Student ID", studentF);
        addRow(form, "Major", majorF);
        addRow(form, "Email", emailF);
        addRow(form, "Phone", phoneF);
        form.add(new JLabel("Skills:"));
        form.add(new JScrollPane(skillsA));
        addRow(form, "CV path", cvPathF);
        addRow(form, "CV status", cvStatusF);

        JPanel south = new JPanel();
        JButton openBtn = new JButton("Open CV with system viewer");
        south.add(openBtn);
        openBtn.addActionListener(e -> openCvFile(parent, ta));

        JDialog d = new JDialog(parent, "TA profile — " + ta.getFullName(), true);
        d.setLayout(new BorderLayout());
        d.add(form, BorderLayout.CENTER);
        d.add(south, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }

    private static void addRow(JPanel form, String label, JTextField field) {
        form.add(new JLabel(label + ":"));
        form.add(field);
    }

    private static JTextField ro(String text) {
        JTextField f = new JTextField(text == null ? "" : text);
        f.setEditable(false);
        return f;
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
