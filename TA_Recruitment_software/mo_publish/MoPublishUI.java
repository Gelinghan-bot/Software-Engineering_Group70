package TA_Recruitment_software.mo_publish;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Position;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

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
            DefaultTableModel model = new DefaultTableModel(new Object[]{"Position ID", "Job Title", "Deadline", "Status"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (Position p : positions) {
                model.addRow(new Object[]{p.getPositionId(), p.getJobTitle(), p.getDeadline(), p.getStatus()});
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
            JButton updateTBtn = new JButton("Modify Job Details");
            JButton closeBtn = new JButton("Close Position");
            JButton backBtn = new JButton("Back");

            updateTBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    Position selectedPos = positions.get(row);
                    
                    JobEditPanel editPanel = new JobEditPanel(parent, context, token, panel, selectedPos, () -> {
                        // This callback updates the table row with potentially new data (like Title and Deadline)
                        model.setValueAt(selectedPos.getJobTitle(), row, 1);
                        model.setValueAt(selectedPos.getDeadline(), row, 2);
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
