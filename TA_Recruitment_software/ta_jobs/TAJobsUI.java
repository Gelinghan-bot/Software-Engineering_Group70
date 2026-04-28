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
        showFilteredJobs(parent, context, token, "All Grades", "All majors", "All Categories");
    }

    public static void showFilteredJobs(JFrame parent, RecruitmentSystemContext context, String token, String gradeFilter, String majorFilter, String categoryFilter) {
        try {
            String currentSemester = CurrentSemesterStore.readCurrentSemester();
            Map<String, String> semesterByPositionId = PositionSemesterStore.readAll();
            List<Position> positions = context.getTaJobService().listAvailableJobs();
            
            positions = positions.stream().filter(p -> {
                boolean matchGrade = "All Grades".equals(gradeFilter) || gradeFilter.equals(p.getGrade());
                boolean matchMajor = "All majors".equals(majorFilter) || majorFilter.equals(p.getMajor());
                boolean matchCategory = "All Categories".equals(categoryFilter) || categoryFilter.equals(p.getJobType());
                return matchGrade && matchMajor && matchCategory;
            }).collect(java.util.stream.Collectors.toList());

            if (positions.isEmpty()) {
                JOptionPane.showMessageDialog(
                    parent,
                    "No open positions found matching your criteria for semester: " + currentSemester
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
                new Object[]{"Position ID", "Job Title", "Grade", "Major", "Job Type", "Semester", "Deadline", "Status", "Already applied"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (Position p : positions) {
                String applied = alreadyApplied.contains(p.getPositionId()) ? "Yes" : "No";
                String semLabel = PositionSemesterStore.labelFor(p.getPositionId(), semesterByPositionId);
                model.addRow(new Object[]{
                    p.getPositionId(),
                    p.getJobTitle(),
                    p.getGrade(),
                    p.getMajor(),
                    p.getJobType(),
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

            Container contentPane = parent.getContentPane();
            BorderLayout layout = (BorderLayout) contentPane.getLayout();
            Component previousCenter = layout.getLayoutComponent(BorderLayout.CENTER);

            JPanel btnPanel = new JPanel();
            JButton applyBtn = new JButton("Apply for Selected Job");
            JButton backBtn = new JButton("Back");

            applyBtn.addActionListener(e -> {
                if (token == null) {
                    JOptionPane.showMessageDialog(parent, "Please login as TA first.");
                    return;
                }
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String posId = (String) model.getValueAt(row, 0);
                    if ("Yes".equals(model.getValueAt(row, 8))) {
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
                        model.setValueAt("Yes", row, 8);
                        JOptionPane.showMessageDialog(parent, "Applied successfully!");
                    } catch (AppException ex) {
                        JOptionPane.showMessageDialog(parent, ex.getMessage(), "Apply failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            btnPanel.add(applyBtn);
            btnPanel.add(backBtn);
            panel.add(btnPanel, BorderLayout.SOUTH);

            backBtn.addActionListener(e -> {
                contentPane.remove(panel);
                if (previousCenter != null) {
                    contentPane.add(previousCenter, BorderLayout.CENTER);
                }
                parent.revalidate();
                parent.repaint();
            });

            if (previousCenter != null) {
                contentPane.remove(previousCenter);
            }
            contentPane.add(panel, BorderLayout.CENTER);
            parent.revalidate();
            parent.repaint();

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

            Container contentPane = parent.getContentPane();
            BorderLayout layout = (BorderLayout) contentPane.getLayout();
            Component previousCenter = layout.getLayoutComponent(BorderLayout.CENTER);

            JPanel btnPanel = new JPanel();
            JButton backBtn = new JButton("Back");
            btnPanel.add(backBtn);
            panel.add(btnPanel, BorderLayout.SOUTH);

            backBtn.addActionListener(e -> {
                contentPane.remove(panel);
                if (previousCenter != null) {
                    contentPane.add(previousCenter, BorderLayout.CENTER);
                }
                parent.revalidate();
                parent.repaint();
            });

            if (previousCenter != null) {
                contentPane.remove(previousCenter);
            }
            contentPane.add(panel, BorderLayout.CENTER);
            parent.revalidate();
            parent.repaint();

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
