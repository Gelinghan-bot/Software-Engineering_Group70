package TA_Recruitment_software.mo_publish;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Position;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MoPublishUI {

    public static void showPublishDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as MO first.");
            return;
        }

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        JTextField courseField = new JTextField(20);
        JTextField deadlineField = new JTextField("YYYY-MM-DD", 20);
        JTextField workingHoursField = new JTextField(20);
        JTextArea jobDescriptionArea = new JTextArea(3, 20);
        JTextArea requirementsArea = new JTextArea(3, 20);

        formPanel.add(new JLabel("Course Name:")); formPanel.add(courseField);
        formPanel.add(new JLabel("Deadline:")); formPanel.add(deadlineField);
        formPanel.add(new JLabel("Working Hours:")); formPanel.add(workingHoursField);
        formPanel.add(new JLabel("Job Description:")); formPanel.add(new JScrollPane(jobDescriptionArea));
        formPanel.add(new JLabel("Requirements:")); formPanel.add(new JScrollPane(requirementsArea));

        int result = JOptionPane.showConfirmDialog(parent, formPanel, "Publish Position", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Position position = context.getMoPublishService().publishPosition(
                    token,
                    courseField.getText().trim(),
                    jobDescriptionArea.getText().trim(),
                    requirementsArea.getText().trim(),
                    deadlineField.getText().trim(),
                    workingHoursField.getText().trim()
                );
                JOptionPane.showMessageDialog(parent, "Published successfully! ID: " + position.getPositionId(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Publish failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void showMyPositionsDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as MO first.");
            return;
        }

        try {
            List<Position> positions = context.getMoPublishService().listMyPositions(token);
            DefaultTableModel model = new DefaultTableModel(new Object[]{"Position ID", "Course Name", "Deadline", "Status"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (Position p : positions) {
                model.addRow(new Object[]{p.getPositionId(), p.getCourseName(), p.getDeadline(), p.getStatus()});
            }

            JTable table = new JTable(model);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(600, 300));

            JPanel btnPanel = new JPanel();
            JButton updateTBtn = new JButton("Update Deadline");
            JButton closeBtn = new JButton("Close Position");

            updateTBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String posId = (String) model.getValueAt(row, 0);
                    String newDl = JOptionPane.showInputDialog(parent, "Enter new deadline (YYYY-MM-DD):");
                    if (newDl != null && !newDl.trim().isEmpty()) {
                        context.getMoPublishService().updateDeadline(token, posId, newDl);
                        model.setValueAt(newDl, row, 2);
                        JOptionPane.showMessageDialog(parent, "Deadline updated.");
                    }
                }
            });

            closeBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String posId = (String) model.getValueAt(row, 0);
                    context.getMoPublishService().closePosition(token, posId);
                    model.setValueAt(TA_Recruitment_software.admin_system.model.PositionStatus.CLOSED, row, 3);
                    JOptionPane.showMessageDialog(parent, "Position closed.");
                }
            });

            btnPanel.add(updateTBtn);
            btnPanel.add(closeBtn);
            panel.add(btnPanel, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(parent, panel, "My Published Positions", JOptionPane.PLAIN_MESSAGE);

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
