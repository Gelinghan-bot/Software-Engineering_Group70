package TA_Recruitment_software.ta_jobs;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.Position;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TAJobsUI {

    public static void showAvailableJobs(JFrame parent, RecruitmentSystemContext context, String token) {
        try {
            List<Position> positions = context.getTaJobService().listAvailableJobs();
            if (positions.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No jobs available currently.");
                return;
            }

            DefaultTableModel model = new DefaultTableModel(new Object[]{"Position ID", "Course", "Deadline", "Status"}, 0) {
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
            JButton applyBtn = new JButton("Apply for Selected Job");

            applyBtn.addActionListener(e -> {
                if (token == null) {
                    JOptionPane.showMessageDialog(parent, "Please login as TA first.");
                    return;
                }
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String posId = (String) model.getValueAt(row, 0);
                    context.getTaJobService().applyForJob(token, posId);
                    JOptionPane.showMessageDialog(parent, "Applied successfully!");
                }
            });

            btnPanel.add(applyBtn);
            panel.add(btnPanel, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(parent, panel, "Available Jobs", JOptionPane.PLAIN_MESSAGE);

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showMyApplications(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as TA first.");
            return;
        }

        try {
            List<Application> apps = context.getTaJobService().listMyApplications(token);
            if (apps.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "You have not applied for any jobs.");
                return;
            }

            DefaultTableModel model = new DefaultTableModel(new Object[]{"App ID", "Position ID", "Status", "Apply Time"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (Application a : apps) {
                model.addRow(new Object[]{a.getApplicationId(), a.getPositionId(), a.getStatus(), a.getSubmissionTime()});
            }

            JTable table = new JTable(model);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(600, 200));

            JOptionPane.showMessageDialog(parent, panel, "My Applications", JOptionPane.PLAIN_MESSAGE);

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}