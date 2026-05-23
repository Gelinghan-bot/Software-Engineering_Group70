package TA_Recruitment_software.mo_publish;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import TA_Recruitment_software.admin_system.model.Position;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class JobEditPanel extends JPanel {
    private RecruitmentSystemContext context;
    private String token;
    private JFrame parent;
    private Component previousCenterComponent;
    private Position position;
    private Runnable onSaved;

    public JobEditPanel(JFrame parent, RecruitmentSystemContext context, String token, Component previousCenterComponent, Position position, Runnable onSaved) {
        this.parent = parent;
        this.context = context;
        this.token = token;
        this.previousCenterComponent = previousCenterComponent;
        this.position = position;
        this.onSaved = onSaved;

        setLayout(new BorderLayout());
        setBackground(UIStyle.BG_PAGE);

        // Header using UIStyle
        JPanel headerPanel = UIStyle.createPageHeader("Modify Job Details (Editing: " + position.getPositionId() + ")", UIStyle.ACCENT_BLUE);
        add(headerPanel, BorderLayout.NORTH);

        // Form Area
        JPanel formCenter = new JPanel(new GridBagLayout());
        formCenter.setBackground(UIStyle.BG_CARD);
        formCenter.setBorder(UIStyle.pagePadding());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 0, 15, 40);

        JTextField jobTitleField = createTextField(position.getJobTitle());
        
        JComboBox<String> gradeCombo = new JComboBox<>(new String[]{"Year1", "Year2", "Year3", "Year4"});
        gradeCombo.setSelectedItem(position.getGrade() != null ? position.getGrade() : "Year1");
        styleCombo(gradeCombo);
        
        JComboBox<String> majorCombo = new JComboBox<>(new String[]{"IOT", "EE", "IST", "TEM"});
        majorCombo.setSelectedItem(position.getMajor() != null ? position.getMajor() : "IOT");
        styleCombo(majorCombo);
        
        JComboBox<String> jobTypeCombo = new JComboBox<>(new String[]{
            "Daily attendance", "grading", "invigilation", "Assess", "and other tasks"
        });
        boolean foundType = false;
        for (int i=0; i<jobTypeCombo.getItemCount(); i++) {
            if (jobTypeCombo.getItemAt(i).equalsIgnoreCase(position.getJobType())) {
                jobTypeCombo.setSelectedIndex(i);
                foundType=true; break;
            }
        }
        if(!foundType) jobTypeCombo.setSelectedItem("grading");
        styleCombo(jobTypeCombo);

        JTextArea jobDescArea = new JTextArea();
        JScrollPane jobDescScroll = createTextAreaScroll(position.getJobDescription(), jobDescArea);
        JTextArea jobReqArea = new JTextArea();
        JScrollPane jobReqScroll = createTextAreaScroll(position.getRequirements(), jobReqArea);
        JTextField interviewLocField = createTextField(position.getInterviewLocation());
        JTextField closingDateField = createTextField(position.getDeadline());
        JTextField headcountField = createTextField(String.valueOf(position.getHeadcount()));

        int row = 0;
        addFormField(formCenter, "JOB TITLE", jobTitleField, gbc, row++);
        addFormField(formCenter, "GRADE", gradeCombo, gbc, row++);
        addFormField(formCenter, "MAJOR", majorCombo, gbc, row++);
        addFormField(formCenter, "JOB TYPE", jobTypeCombo, gbc, row++);
        addTextAreaField(formCenter, "JOB DESCRIPTION", jobDescScroll, gbc, row++);
        addTextAreaField(formCenter, "Job Requirements", jobReqScroll, gbc, row++);
        addFormField(formCenter, "Interview Location", interviewLocField, gbc, row++);
        addFormField(formCenter, "CLOSING DATE", closingDateField, gbc, row++);        addFormField(formCenter, "HEADCOUNT", headcountField, gbc, row++);
        // Submit Button & Cancel Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(UIStyle.BG_CARD);
        
        JButton cancelBtn = UIStyle.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> goBack());

        JButton submitBtn = new JButton("Save Changes");
        submitBtn.setPreferredSize(new Dimension(130, 38));
        submitBtn.setBackground(UIStyle.ACCENT_BLUE);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setOpaque(true);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setFont(UIStyle.FONT_BUTTON);
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                submitBtn.setBackground(UIStyle.ACCENT_BLUE_DARK);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                submitBtn.setBackground(UIStyle.ACCENT_BLUE);
            }
        });
        submitBtn.addActionListener(e -> {
            try {
                String safeDesc = jobDescArea.getText().trim().replaceAll("[\\r\\n]+", "  ");
                String safeReq = jobReqArea.getText().trim().replaceAll("[\\r\\n]+", "  ");
                String interviewLoc = interviewLocField.getText().trim();
                
                int headcount = 0;
                try {
                    headcount = Integer.parseInt(headcountField.getText().trim());
                } catch (NumberFormatException nfe) {
                    throw new AppException("Invalid headcount format. Must be an integer.");
                }

                context.getMoPublishService().updatePositionDetails(
                    token,
                    position.getPositionId(),
                    jobTitleField.getText().trim(),
                    (String) gradeCombo.getSelectedItem(),
                    (String) majorCombo.getSelectedItem(),
                    (String) jobTypeCombo.getSelectedItem(),
                    safeDesc,
                    safeReq,
                    interviewLoc,
                    closingDateField.getText().trim(),
                    headcount
                );
                
                // Update the bound position object internally so table reflects correctly
                position.setJobTitle(jobTitleField.getText().trim());
                position.setDeadline(closingDateField.getText().trim());
                position.setHeadcount(headcount);

                JOptionPane.showMessageDialog(this, "Job details modified successfully!");
                if (onSaved != null) {
                    onSaved.run(); // Triggers table refresh
                }
                goBack();
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(submitBtn);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        formCenter.add(buttonPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(formCenter);
        UIStyle.styleScrollPane(scrollPane);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(800, 38));
        combo.setFont(UIStyle.FONT_BODY);
        combo.setBackground(UIStyle.BG_CARD);
    }

    private JTextField createTextField(String text) {
        JTextField field = UIStyle.createTextField(30);
        if (text != null) field.setText(text);
        return field;
    }

    private void goBack() {
        Container contentPane = parent.getContentPane();
        BorderLayout layout = (BorderLayout) contentPane.getLayout();
        Component center = layout.getLayoutComponent(BorderLayout.CENTER);
        if (center != null) {
            contentPane.remove(center);
        }
        if (previousCenterComponent != null) {
            contentPane.add(previousCenterComponent, BorderLayout.CENTER);
        }
        parent.revalidate();
        parent.repaint();
    }

    private void addFormField(JPanel panel, String labelText, JComponent field, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel label = UIStyle.createFieldLabel(labelText);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private void addTextAreaField(JPanel panel, String labelText, JScrollPane scrollPane, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel label = UIStyle.createFieldLabel(labelText);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(scrollPane, gbc);
    }

    private JScrollPane createTextAreaScroll(String value, JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(UIStyle.FONT_BODY);
        area.setForeground(UIStyle.TEXT_PRIMARY);
        area.setText(value != null ? value : "");
        
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(800, 120));
        scroll.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIStyle.BORDER, 1, true),
            new EmptyBorder(5, 15, 5, 15)
        ));
        UIStyle.styleScrollPane(scroll);
        return scroll;
    }
}
