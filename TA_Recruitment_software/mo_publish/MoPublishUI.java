package TA_Recruitment_software.mo_publish;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import TA_Recruitment_software.admin_system.model.Position;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * UI module for Module Organizer position publishing and management features.
 * <p>
 * Provides interactive Swing components for:
 * <ul>
 *   <li>Publishing new TA job positions</li>
 *   <li>Viewing all positions published by the current MO</li>
 *   <li>Modifying job details (title, deadline, headcount, etc.)</li>
 *   <li>Closing positions when hiring is complete</li>
 * </ul>
 * </p>
 * <p>
 * Access is restricted to MO and ADMIN roles only.
 * </p>
 *
 * @author Group70
 * @see MOPublishService
 * @see JobPublishPanel
 * @see JobEditPanel
 */
public class MoPublishUI {

    public static void showPublishDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as MO or ADMIN.");
            return;
        }

        try {
            TA_Recruitment_software.admin_system.model.Role role = context.getSessionManager().requireSession(token).getRole();
            if (role != TA_Recruitment_software.admin_system.model.Role.MO && role != TA_Recruitment_software.admin_system.model.Role.ADMIN) {
                throw new AppException("Permission denied. Only MO and ADMIN can publish positions.");
            }
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Container contentPane = parent.getContentPane();
        BorderLayout layout = (BorderLayout) contentPane.getLayout();
        Component centerComponent = layout.getLayoutComponent(BorderLayout.CENTER);
        
        if (centerComponent instanceof JobPublishPanel) {
            return; // Already showing this panel
        }
        
        JobPublishPanel publishPanel = new JobPublishPanel(parent, context, token, centerComponent);
        
        if (centerComponent != null) {
            contentPane.remove(centerComponent);
        }
        contentPane.add(publishPanel, BorderLayout.CENTER);
        parent.revalidate();
        parent.repaint();
    }

    public static void showMyPositionsDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as MO or ADMIN.");
            return;
        }

        try {
            TA_Recruitment_software.admin_system.model.Role role = context.getSessionManager().requireSession(token).getRole();
            if (role != TA_Recruitment_software.admin_system.model.Role.MO && role != TA_Recruitment_software.admin_system.model.Role.ADMIN) {
                throw new AppException("Permission denied. Only MO and ADMIN can manage positions.");
            }
            List<Position> positions = context.getMoPublishService().listMyPositions(token);
            java.util.Map<String, Long> hiredCountMap = new TA_Recruitment_software.admin_system.repository.ApplicationRepository().findAll().stream()
                .filter(a -> a.getStatus() == TA_Recruitment_software.admin_system.model.ApplicationStatus.HIRED)
                .collect(java.util.stream.Collectors.groupingBy(TA_Recruitment_software.admin_system.model.Application::getPositionId, java.util.stream.Collectors.counting()));

            DefaultTableModel model = new DefaultTableModel(new Object[]{"Position ID", "Job Title", "Deadline", "Status", "Headcount"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (Position p : positions) {
                long hiredCount = hiredCountMap.getOrDefault(p.getPositionId(), 0L);
                String headcountDisplay = hiredCount + "/" + p.getHeadcount();
                model.addRow(new Object[]{p.getPositionId(), p.getJobTitle(), p.getDeadline(), p.getStatus(), headcountDisplay});
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
            JButton updateTBtn = UIStyle.createAccentButton("Modify Job Details");
            JButton closeBtn = UIStyle.createDangerButton("Close Position");
            JButton backBtn = UIStyle.createSecondaryButton("Back");

            updateTBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    Position selectedPos = positions.get(row);
                    
                    JobEditPanel editPanel = new JobEditPanel(parent, context, token, panel, selectedPos, () -> {
                        // This callback updates the table row with potentially new data (like Title and Deadline)
                        model.setValueAt(selectedPos.getJobTitle(), row, 1);
                        model.setValueAt(selectedPos.getDeadline(), row, 2);
                        
                        long updatedHiredCount = new TA_Recruitment_software.admin_system.repository.ApplicationRepository().findAll().stream()
                            .filter(a -> a.getStatus() == TA_Recruitment_software.admin_system.model.ApplicationStatus.HIRED && a.getPositionId().equals(selectedPos.getPositionId()))
                            .count();
                        model.setValueAt(updatedHiredCount + "/" + selectedPos.getHeadcount(), row, 4);
                    });
                    
                    contentPane.remove(panel);
                    contentPane.add(editPanel, BorderLayout.CENTER);
                    parent.revalidate();
                    parent.repaint();
                } else {
                    JOptionPane.showMessageDialog(parent, "Please select a position to modify.");
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
