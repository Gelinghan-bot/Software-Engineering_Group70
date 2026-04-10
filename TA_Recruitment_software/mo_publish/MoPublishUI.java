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
            DefaultTableModel model = new DefaultTableModel(new Object[]{"Position ID", "Course Name", "Deadline", "Status"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (Position p : positions) {
                model.addRow(new Object[]{p.getPositionId(), p.getCourseName(), p.getDeadline(), p.getStatus()});
            }

            JTable table = new JTable(model);
            JPanel panel = new JPanel(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
            panel.add(scrollPane, BorderLayout.CENTER);
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
