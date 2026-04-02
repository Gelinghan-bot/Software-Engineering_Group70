package TA_Recruitment_software.mo_review;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.ApplicationStatus;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MoReviewUI {

    public static void showReviewDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as MO first.");
            return;
        }

        try {
            List<Application> applications = context.getMoReviewService().listAllApplicationsOfMyPositions(token);
            if (applications.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No applications received yet.");
                return;
            }

            DefaultTableModel model = new DefaultTableModel(new Object[]{"App ID", "Position ID", "Applicant ID", "Status", "Apply Time"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (Application a : applications) {
                model.addRow(new Object[]{a.getApplicationId(), a.getPositionId(), a.getApplicantUserId(), a.getStatus(), a.getSubmissionTime()});
            }

            JTable table = new JTable(model);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(700, 300));

            JPanel btnPanel = new JPanel();
            JButton acceptBtn = new JButton("Approve");
            JButton rejectBtn = new JButton("Reject");
            JButton hiredBtn = new JButton("Hired");

            acceptBtn.addActionListener(e -> updateStatus(parent, context, token, table, model, ApplicationStatus.APPROVED));
            rejectBtn.addActionListener(e -> updateStatus(parent, context, token, table, model, ApplicationStatus.REJECTED));
            hiredBtn.addActionListener(e -> updateStatus(parent, context, token, table, model, ApplicationStatus.HIRED));

            btnPanel.add(acceptBtn);
            btnPanel.add(rejectBtn);
            btnPanel.add(hiredBtn);
            panel.add(btnPanel, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(parent, panel, "Review Applications", JOptionPane.PLAIN_MESSAGE);

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void updateStatus(JFrame parent, RecruitmentSystemContext context, String token, JTable table, DefaultTableModel model, ApplicationStatus status) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            try {
                String appId = (String) model.getValueAt(row, 0);
                context.getMoReviewService().updateApplicationStatus(token, appId, status);
                model.setValueAt(status, row, 3);
                JOptionPane.showMessageDialog(parent, "Application status updated to " + status.name());
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
