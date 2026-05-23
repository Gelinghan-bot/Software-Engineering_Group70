package TA_Recruitment_software.admin_system;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import TA_Recruitment_software.admin_system.model.TaWorkloadSummary;
import TA_Recruitment_software.admin_system.model.User;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminUI {

    public static void showPendingUsersDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as ADMIN first.");
            return;
        }

        try {
            List<User> pendingUsers = context.getAdminService().listPendingUsers(token);
            if (pendingUsers.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No pending users.");
                return;
            }

            DefaultTableModel model = new DefaultTableModel(new Object[]{"User ID", "Account ID", "Name", "Role"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (User u : pendingUsers) {
                model.addRow(new Object[]{u.getUserId(), u.getAccountId(), u.getFullName(), u.getRole()});
            }

            JTable table = UIStyle.createStyledTable(model);
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setBackground(UIStyle.BG_PAGE);
            JScrollPane scrollPane = UIStyle.wrapTableInScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(500, 300));

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            btnPanel.setBackground(UIStyle.BG_PAGE);
            JButton approveBtn = UIStyle.createPrimaryButton("Approve Selected");
            JButton rejectBtn = UIStyle.createDangerButton("Reject Selected");

            approveBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String userId = (String) model.getValueAt(row, 0);
                    context.getAdminService().approveUser(token, userId, true);
                    JOptionPane.showMessageDialog(parent, "User Approved!");
                    model.removeRow(row);
                }
            });

            rejectBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String userId = (String) model.getValueAt(row, 0);
                    context.getAdminService().approveUser(token, userId, false);
                    JOptionPane.showMessageDialog(parent, "User Rejected!");
                    model.removeRow(row);
                }
            });

            btnPanel.add(approveBtn);
            btnPanel.add(rejectBtn);
            panel.add(btnPanel, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(parent, panel, "Pending Registration Requests", JOptionPane.PLAIN_MESSAGE);

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showManageUsersDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as ADMIN first.");
            return;
        }
        
        try {
            List<User> users = context.getAdminService().listAllUsers(token);
            DefaultTableModel model = new DefaultTableModel(new Object[]{"User ID", "Account ID", "Name", "Role", "Enabled"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (User u : users) {
                model.addRow(new Object[]{u.getUserId(), u.getAccountId(), u.getFullName(), u.getRole(), u.isEnabled()});
            }

            JTable table = UIStyle.createStyledTable(model);
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setBackground(UIStyle.BG_PAGE);
            JScrollPane scrollPane = UIStyle.wrapTableInScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(700, 350));

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            btnPanel.setBackground(UIStyle.BG_PAGE);
            JButton editBtn = UIStyle.createPrimaryButton("Edit Selected");
            JButton toggleBtn = UIStyle.createSecondaryButton("Toggle Enable/Disable");
            JButton workloadBtn = UIStyle.createAccentButton("Show TA Workload");

            editBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String userId = (String) model.getValueAt(row, 0);
                    User selectedUser = context.getAdminService().listAllUsers(token).stream()
                        .filter(u -> u.getUserId().equals(userId)).findFirst().orElse(null);
                    if (selectedUser != null) {
                        showEditUserDialog(parent, context, token, selectedUser, model, row);
                    }
                } else {
                    JOptionPane.showMessageDialog(parent, "Please select a user to edit.");
                }
            });

            toggleBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String userId = (String) model.getValueAt(row, 0);
                    boolean currentStatus = (Boolean) model.getValueAt(row, 4);
                    context.getAdminService().setUserEnabled(token, userId, !currentStatus);
                    model.setValueAt(!currentStatus, row, 4);
                    JOptionPane.showMessageDialog(parent, "User status updated!");
                } else {
                    JOptionPane.showMessageDialog(parent, "Please select a user.");
                }
            });

            workloadBtn.addActionListener(e -> showTaWorkloadDialog(parent, context, token));

            btnPanel.add(editBtn);
            btnPanel.add(toggleBtn);
            btnPanel.add(workloadBtn);
            panel.add(btnPanel, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(parent, panel, "Manage Users", JOptionPane.PLAIN_MESSAGE);

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showEditUserDialog(JFrame parent, RecruitmentSystemContext context, String token, 
                                         User user, DefaultTableModel model, int tableRow) {
        JDialog dialog = new JDialog(parent, "Edit User - " + user.getAccountId(), true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(UIStyle.BG_PAGE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(UIStyle.BG_CARD);

        // Title
        JLabel titleLabel = new JLabel("Edit User Information", SwingConstants.CENTER);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(UIStyle.PRIMARY);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIStyle.BG_CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        JLabel userIdLabel = new JLabel("User ID: " + user.getUserId());
        userIdLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(userIdLabel, gbc);

        JLabel accountLabel = new JLabel("Account ID: " + user.getAccountId());
        accountLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridy = 1;
        formPanel.add(accountLabel, gbc);

        JLabel roleLabel = new JLabel("Role: " + user.getRole());
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridy = 2;
        formPanel.add(roleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 3;
        JLabel fullNameLbl = new JLabel("Full Name:");
        formPanel.add(fullNameLbl, gbc);
        JTextField fullNameField = UIStyle.createTextField(20);
        fullNameField.setText(user.getFullName() != null ? user.getFullName() : "");
        gbc.gridx = 1;
        formPanel.add(fullNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        JLabel emailLbl = new JLabel("Email:");
        formPanel.add(emailLbl, gbc);
        JTextField emailField = UIStyle.createTextField(20);
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        JLabel phoneLbl = new JLabel("Phone:");
        formPanel.add(phoneLbl, gbc);
        JTextField phoneField = UIStyle.createTextField(20);
        phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        JLabel majorLbl = new JLabel("Major/Dept:");
        formPanel.add(majorLbl, gbc);
        JTextField majorField = UIStyle.createTextField(20);
        majorField.setText(user.getMajor() != null ? user.getMajor() : (user.getDepartment() != null ? user.getDepartment() : ""));
        gbc.gridx = 1;
        formPanel.add(majorField, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        JLabel skillsLbl = new JLabel("Skills:");
        formPanel.add(skillsLbl, gbc);
        JTextField skillsField = UIStyle.createTextField(20);
        skillsField.setText(user.getSkills() != null ? user.getSkills() : "");
        gbc.gridx = 1;
        formPanel.add(skillsField, gbc);

        // Password reset section
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        formPanel.add(sep, gbc);

        gbc.gridx = 0; gbc.gridy = 9;
        JLabel pwdLbl = new JLabel("New Password (leave empty to keep current):");
        pwdLbl.setFont(new Font("Arial", Font.BOLD, 11));
        formPanel.add(pwdLbl, gbc);

        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        JPasswordField passwordField = UIStyle.createPasswordField(20);
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 2;
        JLabel pwdHint = new JLabel("Password must contain uppercase, lowercase, digit, min 8 chars");
        pwdHint.setFont(new Font("Arial", Font.ITALIC, 10));
        pwdHint.setForeground(new Color(100, 100, 100));
        formPanel.add(pwdHint, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(new Color(39, 174, 96));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setOpaque(true);
        saveBtn.setFont(new Font("Arial", Font.BOLD, 12));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 12));

        saveBtn.addActionListener(e -> {
            try {
                String fullName = fullNameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String major = majorField.getText().trim();
                String skills = skillsField.getText().trim();
                String newPassword = new String(passwordField.getPassword());

                context.getAdminService().updateUserInfo(token, user.getUserId(), fullName, email, phone, major, major, skills);

                if (!newPassword.isEmpty()) {
                    context.getAdminService().resetPassword(token, user.getUserId(), newPassword);
                    JOptionPane.showMessageDialog(dialog, "User information and password updated successfully!");
                } else {
                    JOptionPane.showMessageDialog(dialog, "User information updated successfully!");
                }

                if (model != null && tableRow >= 0) {
                    model.setValueAt(fullName, tableRow, 2);
                }

                dialog.dispose();
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    public static void showTaWorkloadDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as ADMIN first.");
            return;
        }

        try {
            List<TaWorkloadSummary> workloads = context.getAdminService().listTaWorkloadSummary(token);
            if (workloads.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No TA workload data available.");
                return;
            }

            DefaultTableModel model = new DefaultTableModel(
                new Object[]{"User ID", "Account ID", "Name", "Assigned Positions", "Total Hours"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };

            for (TaWorkloadSummary workload : workloads) {
                model.addRow(new Object[]{
                    workload.getUserId(),
                    workload.getAccountId(),
                    workload.getFullName(),
                    workload.getAssignedPositionCount(),
                    workload.getTotalAssignedHours()
                });
            }

            JTable table = new JTable(model);
            JPanel panel = new JPanel(new BorderLayout());
            JScrollPane workloadScrollPane = new JScrollPane(table);
            workloadScrollPane.getVerticalScrollBar().setUnitIncrement(16);
            workloadScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
            panel.add(workloadScrollPane, BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(700, 350));
            JOptionPane.showMessageDialog(parent, panel, "TA Workload Summary", JOptionPane.PLAIN_MESSAGE);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showPollResultsDialog(JFrame parent) {
        TA_Recruitment_software.forum.PollRepository pollRepo = new TA_Recruitment_software.forum.PollRepository();
        java.util.List<String> votes = pollRepo.loadVotes();
        if (votes.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No votes have been cast yet for the Poll of the Week.");
            return;
        }

        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        for (String subject : votes) {
            counts.put(subject, counts.getOrDefault(subject, 0) + 1);
        }

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Subject", "Votes Cast"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (java.util.Map.Entry<String, Integer> entry : counts.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Poll of the Week: Which subject needs more TAs?"), BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(parent, panel, "Poll Results", JOptionPane.PLAIN_MESSAGE);
    }
}
