package TA_Recruitment_software.mo_publish;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
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
        setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(40, 100, 0, 100));

        JLabel titleLabel = new JLabel("Modify Job Details (Editing: " + position.getPositionId() + ")");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(60, 60, 60));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleLine = new JPanel();
        titleLine.setBackground(new Color(52, 152, 219)); // Blue underline to differentiate from green publish
        titleLine.setPreferredSize(new Dimension(150, 3));
        titleLine.setMaximumSize(new Dimension(150, 3));
        titleLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel fullLine = new JPanel();
        fullLine.setBackground(new Color(224, 224, 224));
        fullLine.setPreferredSize(new Dimension(3000, 1));
        fullLine.setMaximumSize(new Dimension(3000, 1));
        fullLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(titleLine);
        headerPanel.add(fullLine);

        add(headerPanel, BorderLayout.NORTH);

        // Form Area
        JPanel formCenter = new JPanel(new GridBagLayout());
        formCenter.setBackground(Color.WHITE);
        formCenter.setBorder(new EmptyBorder(20, 100, 40, 100));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 0, 15, 40);

        JTextField jobTitleField = createTextField(position.getJobTitle());
        
        JComboBox<String> gradeCombo = new JComboBox<>(new String[]{"Year1", "Year2", "Year3", "Year4"});
        gradeCombo.setSelectedItem(position.getGrade() != null ? position.getGrade() : "Year1");
        gradeCombo.setPreferredSize(new Dimension(800, 40));
        gradeCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        gradeCombo.setBackground(Color.WHITE);
        
        JComboBox<String> majorCombo = new JComboBox<>(new String[]{"IOT", "EE", "IST", "TEM"});
        majorCombo.setSelectedItem(position.getMajor() != null ? position.getMajor() : "IOT");
        majorCombo.setPreferredSize(new Dimension(800, 40));
        majorCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        majorCombo.setBackground(Color.WHITE);
        
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

        jobTypeCombo.setPreferredSize(new Dimension(800, 40));
        jobTypeCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        jobTypeCombo.setBackground(Color.WHITE);

        JTextArea jobDescArea = new JTextArea();
        JScrollPane jobDescScroll = createTextAreaScroll(position.getJobDescription(), jobDescArea);
        JTextArea jobReqArea = new JTextArea();
        JScrollPane jobReqScroll = createTextAreaScroll(position.getRequirements(), jobReqArea);
        JTextField interviewLocField = createTextField(position.getInterviewLocation());
        JTextField closingDateField = createTextField(position.getDeadline());

        int row = 0;
        addFormField(formCenter, "JOB TITLE", jobTitleField, gbc, row++);
        addFormField(formCenter, "GRADE", gradeCombo, gbc, row++);
        addFormField(formCenter, "MAJOR", majorCombo, gbc, row++);
        addFormField(formCenter, "JOB TYPE", jobTypeCombo, gbc, row++);
        addTextAreaField(formCenter, "JOB DESCRIPTION", jobDescScroll, gbc, row++);
        addTextAreaField(formCenter, "Job Requirements", jobReqScroll, gbc, row++);
        addFormField(formCenter, "Interview Location", interviewLocField, gbc, row++);
        addFormField(formCenter, "CLOSING DATE", closingDateField, gbc, row++);

        // Submit Button & Cancel Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.setBackground(new Color(224, 224, 224));
        cancelBtn.setOpaque(true);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> goBack());

        JButton submitBtn = new JButton("Save Changes");
        submitBtn.setPreferredSize(new Dimension(130, 40));
        submitBtn.setBackground(new Color(52, 152, 219));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setOpaque(true);
        submitBtn.setFocusPainted(false);
        submitBtn.addActionListener(e -> {
            try {
                String safeDesc = jobDescArea.getText().trim().replaceAll("[\\r\\n]+", "  ");
                String safeReq = jobReqArea.getText().trim().replaceAll("[\\r\\n]+", "  ");
                String interviewLoc = interviewLocField.getText().trim();
                
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
                    closingDateField.getText().trim()
                );

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
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
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
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(80, 80, 80));
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
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(80, 80, 80));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(scrollPane, gbc);
    }

    private JTextField createTextField(String value) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(800, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(5, 15, 5, 15)
        ));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setForeground(new Color(60, 60, 60));
        field.setText(value);
        return field;
    }

    private JScrollPane createTextAreaScroll(String value, JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Arial", Font.PLAIN, 14));
        area.setForeground(new Color(60, 60, 60));
        area.setText(value != null ? value : "");
        
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(800, 120));
        scroll.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(5, 15, 5, 15)
        ));
        return scroll;
    }
}
