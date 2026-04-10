package TA_Recruitment_software.ta_jobs;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.Position;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TAJobsUI {

    public static void showAvailableJobs(JFrame parent, RecruitmentSystemContext context, String token) {
        try {
            String currentSemester = CurrentSemesterStore.readCurrentSemester();
            Map<String, String> semesterByPositionId = PositionSemesterStore.readAll();
            List<Position> positions = context.getTaJobService().listAvailableJobs();
            if (positions.isEmpty()) {
                JOptionPane.showMessageDialog(
                    parent,
                    "No open positions for current semester: " + currentSemester
                        + "\n(Tip: tag each visible positionId in ta_jobs/data/position_semesters.csv.)"
                );
                return;
            }

            Set<String> alreadyApplied = new HashSet<>();
            if (token != null) {
                try {
                    for (Application a : context.getTaJobService().listMyApplications(token)) {
                        alreadyApplied.add(a.getPositionId());
                    }
                } catch (AppException ignored) {
                    // Not TA or invalid session
                }
            }

            DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Position ID", "Job Title", "Semester", "Deadline", "Status", "Already applied"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (Position p : positions) {
                String applied = alreadyApplied.contains(p.getPositionId()) ? "Yes" : "No";
                String semLabel = PositionSemesterStore.labelFor(p.getPositionId(), semesterByPositionId);
                model.addRow(new Object[]{
                    p.getPositionId(),
                    p.getJobTitle(),
                    semLabel,
                    p.getDeadline(),
                    p.getStatus(),
                    applied
                });
            }

            JTable table = new JTable(model);
            JPanel panel = new JPanel(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(760, 320));

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
                    if ("Yes".equals(model.getValueAt(row, 5))) {
                        JOptionPane.showMessageDialog(
                            parent,
                            "You have already applied for this position.",
                            "Duplicate application",
                            JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    try {
                        context.getTaJobService().applyForJob(token, posId);
                        model.setValueAt("Yes", row, 5);
                        JOptionPane.showMessageDialog(parent, "Applied successfully!");
                    } catch (AppException ex) {
                        JOptionPane.showMessageDialog(parent, ex.getMessage(), "Apply failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            btnPanel.add(applyBtn);
            panel.add(btnPanel, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(
                parent,
                panel,
                "Available Jobs (semester: " + currentSemester + ")",
                JOptionPane.PLAIN_MESSAGE
            );

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
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(600, 200));

            JOptionPane.showMessageDialog(parent, panel, "My Applications", JOptionPane.PLAIN_MESSAGE);

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
