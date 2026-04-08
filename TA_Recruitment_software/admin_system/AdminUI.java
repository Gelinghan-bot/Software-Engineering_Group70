package TA_Recruitment_software.admin_system;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

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

            JTable table = new JTable(model);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(500, 300));

            JPanel btnPanel = new JPanel();
            JButton approveBtn = new JButton("Approve Selected");
            JButton rejectBtn = new JButton("Reject Selected");

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

            JTable table = new JTable(model);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(600, 300));

            JPanel btnPanel = new JPanel();
            JButton toggleBtn = new JButton("Toggle Enable/Disable");
            JButton resetPwdBtn = new JButton("Reset Password");

            toggleBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String userId = (String) model.getValueAt(row, 0);
                    boolean currentStatus = (Boolean) model.getValueAt(row, 4);
                    context.getAdminService().setUserEnabled(token, userId, !currentStatus);
                    model.setValueAt(!currentStatus, row, 4);
                    JOptionPane.showMessageDialog(parent, "User status toggled!");
                }
            });

            resetPwdBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String userId = (String) model.getValueAt(row, 0);
                    String newPwd = JOptionPane.showInputDialog(parent, "Enter new password:");
                    if (newPwd != null && !newPwd.trim().isEmpty()) {
                        context.getAdminService().resetPassword(token, userId, newPwd);
                        JOptionPane.showMessageDialog(parent, "Password reset successfully!");
                    }
                }
            });

            btnPanel.add(toggleBtn);
            btnPanel.add(resetPwdBtn);
            panel.add(btnPanel, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(parent, panel, "Manage Users", JOptionPane.PLAIN_MESSAGE);

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
