package TA_Recruitment_software.ta_jobs;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Position;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
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

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(360);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(880, 320));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        dlg.add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton detailBtn = new JButton("View details & Apply");
        JButton closeBtn = new JButton("Close");
        south.add(detailBtn);
        south.add(closeBtn);
        dlg.add(south, BorderLayout.SOUTH);

        JLabel hint = new JLabel("Double-click a row for details and to apply.");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        dlg.add(hint, BorderLayout.NORTH);

        Runnable openDetail = () -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dlg, "Please select a position.", "AI Diagnosis", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JobMatchResult match = results.get(row);
            showDetailAndApply(dlg, context, token, match);
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

    private static void showDetailAndApply(JDialog owner, RecruitmentSystemContext context, String token, JobMatchResult match) {
        Position p = match.getPosition();
        String currentSemester = CurrentSemesterStore.readCurrentSemester();
        Map<String, String> semesterByPositionId = PositionSemesterStore.readAll();
        String semLabel = PositionSemesterStore.labelFor(p.getPositionId(), semesterByPositionId);

        StringBuilder sb = new StringBuilder();
        sb.append("Match score: ").append(match.getScorePercent()).append("%\n");
        sb.append("Reason: ").append(match.getReason()).append("\n\n");
        sb.append("=== Position ===\n");
        sb.append("Position ID: ").append(p.getPositionId()).append("\n");
        sb.append("Job Title: ").append(nvl(p.getJobTitle())).append("\n");
        sb.append("Semester: ").append(semLabel).append(" (current: ").append(currentSemester).append(")\n");
        sb.append("Grade: ").append(nvl(p.getGrade())).append("\n");
        sb.append("Major: ").append(nvl(p.getMajor())).append("\n");
        sb.append("Job Type: ").append(nvl(p.getJobType())).append("\n");
        sb.append("Deadline: ").append(nvl(p.getDeadline())).append("\n");
        sb.append("Status: ").append(p.getStatus()).append("\n");
        sb.append("Interview location: ").append(nvl(p.getInterviewLocation())).append("\n\n");
        sb.append("Description:\n").append(nvl(p.getJobDescription())).append("\n\n");
        sb.append("Requirements:\n").append(nvl(p.getRequirements())).append("\n");

        JTextArea area = new JTextArea(sb.toString(), 22, 64);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(area);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(sp, BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton apply = new JButton("Apply for this position");
        JButton cancel = new JButton("Cancel");
        bp.add(apply);
        bp.add(cancel);
        panel.add(bp, BorderLayout.SOUTH);

        JDialog detail = new JDialog(owner, "Position details", true);
        detail.setContentPane(panel);
        detail.pack();
        detail.setLocationRelativeTo(owner);

        apply.addActionListener(e -> {
            try {
                context.getTaJobService().applyForJob(token, p.getPositionId());
                JOptionPane.showMessageDialog(detail, "Applied successfully!", "Apply", JOptionPane.INFORMATION_MESSAGE);
                detail.dispose();
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(detail, ex.getMessage(), "Apply failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> detail.dispose());

        detail.setVisible(true);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
