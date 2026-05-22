package TA_Recruitment_software.ta_jobs;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Position;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Full-page position detail view in the main window (replaces {@link BorderLayout#CENTER}).
 */
public final class PositionDetailUI {

    private PositionDetailUI() {}

    /** Hint label for job tables: double-click opens the detail page. */
    public static JLabel createDoubleClickHintLabel(String message) {
        JLabel hint = new JLabel(message);
        hint.setFont(new Font("Arial", Font.BOLD, 13));
        hint.setForeground(new Color(39, 174, 96));
        hint.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        return hint;
    }

    public static void show(
        JFrame parent,
        Component returnToCenter,
        RecruitmentSystemContext context,
        String token,
        Position position,
        boolean showApplyButton,
        boolean alreadyApplied,
        Integer matchPercent,
        String matchReason,
        Runnable onApplySuccess
    ) {
        if (position == null) {
            return;
        }

        Container contentPane = parent.getContentPane();
        Component currentCenter = ((BorderLayout) contentPane.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel(nvl(position.getJobTitle()));
        title.setFont(new Font("Arial", Font.BOLD, 22));
        page.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(4, 0, 4, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;

        String currentSemester = CurrentSemesterStore.readCurrentSemester();
        Map<String, String> semesterByPositionId = PositionSemesterStore.readAll();
        String semLabel = PositionSemesterStore.labelFor(position.getPositionId(), semesterByPositionId);

        if (matchPercent != null) {
            addField(body, gbc, "Match score", matchPercent + "%");
            if (matchReason != null && !matchReason.trim().isEmpty()) {
                addField(body, gbc, "Match reason", matchReason);
            }
        }

        addField(body, gbc, "Position ID", position.getPositionId());
        addField(body, gbc, "Semester", semLabel + " (current: " + currentSemester + ")");
        addField(body, gbc, "Grade", nvl(position.getGrade()));
        addField(body, gbc, "Major", nvl(position.getMajor()));
        addField(body, gbc, "Job type", nvl(position.getJobType()));
        addField(body, gbc, "Responsible MO", nvl(position.getResponsibleMO()));
        addField(body, gbc, "Deadline", nvl(position.getDeadline()));
        addField(body, gbc, "Status", position.getStatus() != null ? position.getStatus().name() : "");
        addField(body, gbc, "Headcount", String.valueOf(position.getHeadcount()));
        addField(body, gbc, "Interview location", nvl(position.getInterviewLocation()));

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        addTextBlock(body, gbc, "Description", nvl(position.getJobDescription()));
        addTextBlock(body, gbc, "Requirements", nvl(position.getRequirements()));

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(16);
        page.add(bodyScroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton backBtn = new JButton("Back to list");
        south.add(backBtn);

        if (showApplyButton) {
            JButton applyBtn = new JButton(alreadyApplied ? "Already applied" : "Apply for this position");
            applyBtn.setEnabled(!alreadyApplied);
            applyBtn.addActionListener(e -> {
                if (token == null) {
                    JOptionPane.showMessageDialog(parent, "Please login as TA first.");
                    return;
                }
                try {
                    context.getTaJobService().applyForJob(token, position.getPositionId());
                    JOptionPane.showMessageDialog(parent, "Applied successfully!", "Apply", JOptionPane.INFORMATION_MESSAGE);
                    applyBtn.setText("Already applied");
                    applyBtn.setEnabled(false);
                    if (onApplySuccess != null) {
                        onApplySuccess.run();
                    }
                } catch (AppException ex) {
                    JOptionPane.showMessageDialog(parent, ex.getMessage(), "Apply failed", JOptionPane.ERROR_MESSAGE);
                }
            });
            south.add(applyBtn);
        }
        south.add(backBtn);
        page.add(south, BorderLayout.SOUTH);

        Runnable goBack = () -> {
            contentPane.remove(page);
            if (returnToCenter != null) {
                contentPane.add(returnToCenter, BorderLayout.CENTER);
            } else if (currentCenter != null) {
                contentPane.add(currentCenter, BorderLayout.CENTER);
            }
            parent.revalidate();
            parent.repaint();
        };
        backBtn.addActionListener(e -> goBack.run());

        if (currentCenter != null) {
            contentPane.remove(currentCenter);
        }
        contentPane.add(page, BorderLayout.CENTER);
        parent.revalidate();
        parent.repaint();
    }

    private static void addField(JPanel body, GridBagConstraints gbc, String label, String value) {
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        body.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        body.add(new JLabel(nvl(value)), gbc);
        gbc.gridx = 0;
        gbc.weightx = 0.0;
    }

    private static void addTextBlock(JPanel body, GridBagConstraints gbc, String label, String text) {
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        body.add(lbl, gbc);
        gbc.gridy++;
        JTextArea area = new JTextArea(nvl(text), 6, 60);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(new Font("Arial", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        body.add(area, gbc);
        gbc.gridwidth = 1;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
