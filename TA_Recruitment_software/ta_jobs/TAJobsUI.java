package TA_Recruitment_software.ta_jobs;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import TA_Recruitment_software.admin_system.model.Application;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.Role;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
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

            TA_Recruitment_software.admin_system.model.Role userRole = null;
            if (token != null) {
                try {
                    userRole = context.getSessionManager().requireSession(token).getRole();
                } catch (AppException ignored) {}
            }
            final boolean isTA = (userRole == TA_Recruitment_software.admin_system.model.Role.TA);

            Set<String> alreadyApplied = new HashSet<>();
            if (isTA && token != null) {
                try {
                    for (Application a : context.getTaJobService().listMyApplications(token)) {
                        alreadyApplied.add(a.getPositionId());
                    }
                } catch (AppException ignored) {
                    // Not TA or invalid session
                }
            }

            java.util.Map<String, Long> hiredCountMap = new TA_Recruitment_software.admin_system.repository.ApplicationRepository().findAll().stream()
                .filter(a -> a.getStatus() == TA_Recruitment_software.admin_system.model.ApplicationStatus.HIRED)
                .collect(java.util.stream.Collectors.groupingBy(Application::getPositionId, java.util.stream.Collectors.counting()));

            java.util.List<String> colNames = new java.util.ArrayList<>(java.util.Arrays.asList(
                "Position ID", "Job Title", "Grade", "Major", "Job Type", "Semester", "Deadline", "Status", "Headcount"
            ));
            if (isTA) {
                colNames.add("Already applied");
            }

            DefaultTableModel model = new DefaultTableModel(colNames.toArray(), 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            
            Map<String, Position> positionById = new HashMap<>();
            for (Position p : positions) {
                positionById.put(p.getPositionId(), p);
                long hiredCount = hiredCountMap.getOrDefault(p.getPositionId(), 0L);
                String headcountDisplay = hiredCount + "/" + p.getHeadcount();
                String semLabel = PositionSemesterStore.labelFor(p.getPositionId(), semesterByPositionId);
                java.util.List<Object> rowData = new java.util.ArrayList<>(java.util.Arrays.asList(
                    p.getPositionId(),
                    p.getJobTitle(),
                    p.getGrade(),
                    p.getMajor(),
                    p.getJobType(),
                    semLabel,
                    p.getDeadline(),
                    p.getStatus(),
                    headcountDisplay
                ));
                if (isTA) {
                    rowData.add(alreadyApplied.contains(p.getPositionId()) ? "Yes" : "No");
                }
                model.addRow(rowData.toArray());
            }

            JTable table = UIStyle.createStyledTable(model);
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setBackground(UIStyle.BG_PAGE);
            panel.setBorder(UIStyle.pagePadding());
            panel.add(PositionDetailUI.createDoubleClickHintLabel(
                "Tip: Double-click a job row to view full position details."
            ), BorderLayout.NORTH);
            JScrollPane scrollPane = UIStyle.wrapTableInScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);

            Container contentPane = parent.getContentPane();
            BorderLayout layout = (BorderLayout) contentPane.getLayout();
            Component previousCenter = layout.getLayoutComponent(BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            btnPanel.setBackground(UIStyle.BG_PAGE);
            JButton applyBtn = UIStyle.createPrimaryButton("Apply for Selected Job");
            JButton backBtn = UIStyle.createSecondaryButton("Back");

            applyBtn.addActionListener(e -> {
                if (token == null) {
                    JOptionPane.showMessageDialog(parent, "Please login as TA first.");
                    return;
                }
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String posId = (String) model.getValueAt(row, 0);
                    if (alreadyApplied.contains(posId)) {
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
                        alreadyApplied.add(posId);
                        if (isTA) {
                            model.setValueAt("Yes", row, colNames.indexOf("Already applied"));
                        }
                        JOptionPane.showMessageDialog(parent, "Applied successfully!");
                    } catch (AppException ex) {
                        JOptionPane.showMessageDialog(parent, ex.getMessage(), "Apply failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            if (userRole == null || isTA) {
                btnPanel.add(applyBtn);
            }
            btnPanel.add(backBtn);
            
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(UIStyle.BG_PAGE);
            bottomPanel.add(btnPanel, BorderLayout.WEST);

            if (userRole == TA_Recruitment_software.admin_system.model.Role.MO) {
                JPanel rightBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
                rightBtnPanel.setBackground(UIStyle.BG_PAGE);
                JButton cloneBtn = UIStyle.createPrimaryButton("One-click Clone Position");
                cloneBtn.setBackground(new Color(108, 92, 231)); 
                cloneBtn.addActionListener(e -> {
                    int r = table.getSelectedRow();
                    if (r < 0) {
                        JOptionPane.showMessageDialog(parent, "Please select a position to clone.", "Clone Position", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String pId = (String) model.getValueAt(r, 0);
                    Position pToClone = positionById.get(pId);
                    if (pToClone != null) {
                        try {
                            String sem = semesterByPositionId.get(pId);
                            context.getMoPublishService().publishPosition(
                                token,
                                pToClone.getJobTitle() + " (Copy)",
                                pToClone.getGrade(),
                                pToClone.getMajor(),
                                pToClone.getJobType(),
                                pToClone.getJobDescription(),
                                pToClone.getRequirements(),
                                pToClone.getInterviewLocation(),
                                pToClone.getDeadline(),
                                sem,
                                pToClone.getHeadcount()
                            );
                            JOptionPane.showMessageDialog(parent, "Position cloned successfully!");
                            backBtn.doClick(); 
                            TAJobsUI.showFilteredJobs(parent, context, token, gradeFilter, majorFilter, categoryFilter);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(parent, "Clone failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                rightBtnPanel.add(cloneBtn);
                bottomPanel.add(rightBtnPanel, BorderLayout.EAST);
            }

            panel.add(bottomPanel, BorderLayout.SOUTH);

            final int appliedColIndex = colNames.indexOf("Already applied");
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() != 2) {
                        return;
                    }
                    int row = table.rowAtPoint(e.getPoint());
                    if (row < 0) {
                        return;
                    }
                    String posId = (String) model.getValueAt(row, 0);
                    Position pos = positionById.get(posId);
                    if (pos == null) {
                        return;
                    }
                    boolean applied = alreadyApplied.contains(posId);
                    PositionDetailUI.show(
                        parent,
                        panel,
                        context,
                        token,
                        pos,
                        isTA,
                        applied,
                        null,
                        null,
                        () -> {
                            alreadyApplied.add(posId);
                            if (isTA && appliedColIndex >= 0) {
                                model.setValueAt("Yes", row, appliedColIndex);
                            }
                        }
                    );
                }
            });

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

            JTable table = UIStyle.createStyledTable(model);
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setBackground(UIStyle.BG_PAGE);
            panel.setBorder(UIStyle.pagePadding());
            JScrollPane scrollPane = UIStyle.wrapTableInScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);

            Container contentPane = parent.getContentPane();
            BorderLayout layout = (BorderLayout) contentPane.getLayout();
            Component previousCenter = layout.getLayoutComponent(BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            btnPanel.setBackground(UIStyle.BG_PAGE);
            JButton backBtn = UIStyle.createSecondaryButton("Back");
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

    /**
     * TA homepage "AI Diagnosis" control (visibility updated after login/logout).
     */
    public static JButton createAiDiagnosisHomeButton(
        JFrame parent,
        RecruitmentSystemContext context,
        Supplier<String> tokenSupplier,
        Supplier<Role> roleSupplier
    ) {
        JButton btn = UIStyle.createPrimaryButton("Diagnosis CV & Recommendation");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(300, 45));
        btn.setPreferredSize(new Dimension(300, 45));
        btn.setVisible(false);
        btn.addActionListener(e -> {
            Role r = roleSupplier.get();
            String t = tokenSupplier.get();
            if (r != Role.TA || t == null) {
                JOptionPane.showMessageDialog(parent, "Please login as TA to use Diagnosis CV & Recommendation.", "Diagnosis CV & Recommendation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            AiDiagnosisUI.show(parent, context, t);
        });
        return btn;
    }

    public static void updateAiDiagnosisHomeButton(JButton btn, Role role, String token) {
        if (btn == null) {
            return;
        }
        btn.setVisible(role == Role.TA && token != null);
    }
}
