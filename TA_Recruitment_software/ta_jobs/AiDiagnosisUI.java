package TA_Recruitment_software.ta_jobs;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import TA_Recruitment_software.admin_system.model.Position;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public final class AiDiagnosisUI {

    private AiDiagnosisUI() {}

    public static void show(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as TA first.", "AI Diagnosis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Cursor prev = parent.getCursor();
        parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<List<JobMatchResult>, Void> worker = new SwingWorker<List<JobMatchResult>, Void>() {
            @Override
            protected List<JobMatchResult> doInBackground() {
                return context.getAiDiagnosisService().diagnoseAndRecommend(token);
            }

            @Override
            protected void done() {
                parent.setCursor(prev != null ? prev : Cursor.getDefaultCursor());
                try {
                    List<JobMatchResult> list = get();
                    showResultsDialog(parent, context, token, list);
                } catch (Exception ex) {
                    Throwable cause = ex;
                    if (ex.getCause() != null) {
                        cause = ex.getCause();
                    }
                    String msg = cause instanceof AppException ? cause.getMessage() : "Analysis failed.";
                    JOptionPane.showMessageDialog(parent, msg, "AI Diagnosis", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private static void showResultsDialog(JFrame parent, RecruitmentSystemContext context, String token, List<JobMatchResult> results) {
        if (results == null || results.isEmpty()) {
            JOptionPane.showMessageDialog(
                parent,
                "No open TA positions match the current semester filter, or there are no jobs to recommend.",
                "AI Diagnosis",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        JDialog dlg = new JDialog(parent, "AI Diagnosis — Recommended Positions", true);
        dlg.getContentPane().setBackground(UIStyle.BG_PAGE);
        dlg.setLayout(new BorderLayout(8, 8));

        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Match %", "Position ID", "Job Title", "Reason"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (JobMatchResult r : results) {
            Position p = r.getPosition();
            model.addRow(new Object[]{
                r.getScorePercent(),
                p.getPositionId(),
                p.getJobTitle(),
                r.getReason()
            });
        }

        JTable table = UIStyle.createStyledTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(360);
        JScrollPane scroll = UIStyle.wrapTableInScrollPane(table);
        scroll.setPreferredSize(new Dimension(880, 320));
        dlg.add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        south.setBackground(UIStyle.BG_PAGE);
        JButton detailBtn = UIStyle.createPrimaryButton("View details & Apply");
        JButton closeBtn = UIStyle.createSecondaryButton("Close");
        south.add(detailBtn);
        south.add(closeBtn);
        dlg.add(south, BorderLayout.SOUTH);

        dlg.add(PositionDetailUI.createDoubleClickHintLabel(
            "Tip: Double-click a recommended job to view details and apply."
        ), BorderLayout.NORTH);

        Runnable openDetail = () -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dlg, "Please select a position.", "AI Diagnosis", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JobMatchResult match = results.get(row);
            dlg.dispose();
            boolean alreadyApplied = false;
            try {
                for (TA_Recruitment_software.admin_system.model.Application a : context.getTaJobService().listMyApplications(token)) {
                    if (a.getPositionId().equals(match.getPosition().getPositionId())) {
                        alreadyApplied = true;
                        break;
                    }
                }
            } catch (AppException ignored) {
            }
            PositionDetailUI.show(
                parent,
                null,
                context,
                token,
                match.getPosition(),
                true,
                alreadyApplied,
                match.getScorePercent(),
                match.getReason(),
                null
            );
        };

        detailBtn.addActionListener(e -> openDetail.run());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openDetail.run();
                }
            }
        });
        closeBtn.addActionListener(e -> dlg.dispose());

        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }
}
