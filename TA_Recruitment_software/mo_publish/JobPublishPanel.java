package TA_Recruitment_software.mo_publish;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Position;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class JobPublishPanel extends JPanel {
    private RecruitmentSystemContext context;
    private String token;
    private JFrame parent;
    private Component previousCenterComponent;

    public JobPublishPanel(JFrame parent, RecruitmentSystemContext context, String token, Component previousCenterComponent) {
        this.parent = parent;
        this.context = context;
        this.token = token;
        this.previousCenterComponent = previousCenterComponent;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(40, 100, 0, 100));

        JLabel titleLabel = new JLabel("Job Details");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(60, 60, 60));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleLine = new JPanel();
        titleLine.setBackground(new Color(39, 174, 96)); // Green underline
        titleLine.setPreferredSize(new Dimension(120, 3));
        titleLine.setMaximumSize(new Dimension(120, 3));
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

        JTextField jobTitleField = createTextField("Enter your job title");
        
        JComboBox<String> gradeCombo = new JComboBox<>(new String[]{
            "Year1", "Year2", "Year3", "Year4"
        });
        gradeCombo.setPreferredSize(new Dimension(800, 40));
        gradeCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        gradeCombo.setBackground(Color.WHITE);
        
        JComboBox<String> majorCombo = new JComboBox<>(new String[]{
            "IOT", "EE", "IST", "TEM"
        });
        majorCombo.setPreferredSize(new Dimension(800, 40));
        majorCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        majorCombo.setBackground(Color.WHITE);
        
        JComboBox<String> jobTypeCombo = new JComboBox<>(new String[]{
            "Daily attendance",
            "grading",
            "invigilation",
            "Assess",
            "and other tasks"
        });
        jobTypeCombo.setPreferredSize(new Dimension(800, 40));
        jobTypeCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        jobTypeCombo.setBackground(Color.WHITE);

        JTextArea jobDescArea = new JTextArea();
        JScrollPane jobDescScroll = createTextAreaScroll("Enter your job description", jobDescArea);
        JTextArea jobReqArea = new JTextArea();
        JScrollPane jobReqScroll = createTextAreaScroll("Enter your job requirements", jobReqArea);
        JTextField interviewLocField = createTextField("Enter your interview location");
        JTextField closingDateField = createTextField("yyyy-mm-dd");
        JTextField headcountField = createTextField("Enter headcount (e.g. 2)");

        String currentYear = String.valueOf(java.time.LocalDate.now().getYear());
        String nextYear = String.valueOf(java.time.LocalDate.now().getYear() + 1);
        String nextNextYear = String.valueOf(java.time.LocalDate.now().getYear() + 2);
        JComboBox<String> semesterBox = new JComboBox<>(new String[]{
            currentYear + "-Spring",
            currentYear + "-Fall",
            nextYear + "-Spring",
            nextYear + "-Fall",
            nextNextYear + "-Spring",
            nextNextYear + "-Fall"
        });
        semesterBox.setSelectedItem(TA_Recruitment_software.ta_jobs.CurrentSemesterStore.readCurrentSemester());
        semesterBox.setPreferredSize(new Dimension(800, 40));
        semesterBox.setFont(new Font("Arial", Font.PLAIN, 14));

        int row = 0;
        addFormField(formCenter, "JOB TITLE", jobTitleField, gbc, row++);
        addFormField(formCenter, "GRADE", gradeCombo, gbc, row++);
        addFormField(formCenter, "MAJOR", majorCombo, gbc, row++);
        addFormField(formCenter, "JOB TYPE", jobTypeCombo, gbc, row++);
        addTextAreaField(formCenter, "JOB DESCRIPTION", jobDescScroll, gbc, row++);
        addTextAreaField(formCenter, "Job Requirements", jobReqScroll, gbc, row++);
        addFormField(formCenter, "Interview Location", interviewLocField, gbc, row++);
        addFormField(formCenter, "CLOSING DATE", closingDateField, gbc, row++);        addFormField(formCenter, "HEADCOUNT", headcountField, gbc, row++);
        // Semester selector
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel semLabel = new JLabel("SEMESTER");
        semLabel.setFont(new Font("Arial", Font.BOLD, 14));
        semLabel.setForeground(new Color(80, 80, 80));
        formCenter.add(semLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formCenter.add(semesterBox, gbc);
        row++;

        // Submit Button & Cancel Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.setBackground(new Color(224, 224, 224));
        cancelBtn.setOpaque(true);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> goBack());

        JButton submitBtn = new JButton("Submit");
        submitBtn.setPreferredSize(new Dimension(100, 40));
        submitBtn.setBackground(new Color(39, 174, 96));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setOpaque(true);
        submitBtn.setFocusPainted(false);
        submitBtn.addActionListener(e -> {
            try {
                String title = jobTitleField.getText().trim();
                if (title.equals("Enter your job title")) title = "";

                String rawDesc = jobDescArea.getText().trim();
                if (rawDesc.equals("Enter your job description")) rawDesc = "";
                String safeDesc = rawDesc.replaceAll("[\\r\\n]+", "  ");

                String rawReq = jobReqArea.getText().trim();
                if (rawReq.equals("Enter your job requirements")) rawReq = "";
                String safeReq = rawReq.replaceAll("[\\r\\n]+", "  ");

                String interviewLoc = interviewLocField.getText().trim();
                if (interviewLoc.equals("Enter your interview location")) {
                    interviewLoc = "";
                }

                String deadline = closingDateField.getText().trim();
                if (deadline.equals("yyyy-mm-dd")) deadline = "";

                String hcStr = headcountField.getText().trim();
                if (hcStr.equals("Enter headcount (e.g. 2)")) hcStr = "";
                int headcount = 0;
                try {
                    headcount = Integer.parseInt(hcStr);
                } catch (NumberFormatException nfe) {
                    throw new AppException("Invalid headcount format. Must be an integer.");
                }

                // Map fields to our backend structure referenced in MOPublishService.java
                Position position = context.getMoPublishService().publishPosition(
                    token,
                    title,
                    (String) gradeCombo.getSelectedItem(),
                    (String) majorCombo.getSelectedItem(),
                    (String) jobTypeCombo.getSelectedItem(),
                    safeDesc,
                    safeReq,
                    interviewLoc,
                    deadline,
                    (String) semesterBox.getSelectedItem(),
                    headcount
                );
                JOptionPane.showMessageDialog(parent, "Published successfully! ID: " + position.getPositionId(), "Success", JOptionPane.INFORMATION_MESSAGE);
                goBack();
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Publish failed", JOptionPane.ERROR_MESSAGE);
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
        // Increase the vertical scroll bar sensitivity by returning unit increment
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
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

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(800, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(5, 15, 5, 15)
        ));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setForeground(new Color(150, 150, 150));
        field.setText(placeholder);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setForeground(new Color(150, 150, 150));
                    field.setText(placeholder);
                }
            }
        });
        return field;
    }

    private JScrollPane createTextAreaScroll(String placeholder, JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Arial", Font.PLAIN, 14));
        area.setForeground(new Color(150, 150, 150));
        area.setText(placeholder);
        
        area.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (area.getText().equals(placeholder)) {
                    area.setText("");
                    area.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (area.getText().isEmpty()) {
                    area.setForeground(new Color(150, 150, 150));
                    area.setText(placeholder);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(800, 120));
        scroll.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(5, 15, 5, 15)
        ));
        return scroll;
    }
}