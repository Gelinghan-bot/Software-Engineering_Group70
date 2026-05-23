package TA_Recruitment_software.mo_publish;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import TA_Recruitment_software.admin_system.model.Position;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class JobPublishPanel extends JPanel {
    private RecruitmentSystemContext context;
    private String token;
    private JFrame parent;
    private Component previousCenterComponent;

    private JTextField jobTitleField;
    private JComboBox<String> gradeCombo;
    private JComboBox<String> majorCombo;
    private JComboBox<String> jobTypeCombo;
    private JTextArea jobDescArea;
    private JTextArea jobReqArea;
    private MOPublishService moPublishService;

    public JobPublishPanel(JFrame parent, RecruitmentSystemContext context, String token, Component previousCenterComponent) {
        this.parent = parent;
        this.context = context;
        this.token = token;
        this.previousCenterComponent = previousCenterComponent;
        
        // initialize service
        this.moPublishService = context.getMoPublishService();

        setLayout(new BorderLayout());
        setBackground(UIStyle.BG_PAGE);

        // Header using UIStyle
        JPanel headerPanel = UIStyle.createPageHeader("Job Details", UIStyle.PRIMARY);
        add(headerPanel, BorderLayout.NORTH);

        // Form Area
        JPanel formCenter = new JPanel(new GridBagLayout());
        formCenter.setBackground(UIStyle.BG_CARD);
        formCenter.setBorder(UIStyle.pagePadding());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 0, 15, 40);

        jobTitleField = createTextField("Enter your job title");
        
        gradeCombo = new JComboBox<>(new String[]{"Year1", "Year2", "Year3", "Year4"});
        styleCombo(gradeCombo);
        
        majorCombo = new JComboBox<>(new String[]{"IOT", "EE", "IST", "TEM"});
        styleCombo(majorCombo);
        
        jobTypeCombo = new JComboBox<>(new String[]{
            "Daily attendance", "grading", "invigilation", "Assess", "and other tasks"
        });
        styleCombo(jobTypeCombo);

        jobDescArea = new JTextArea();
        JScrollPane jobDescScroll = createTextAreaScroll("Enter your job description", jobDescArea);
        jobReqArea = new JTextArea();
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
        styleCombo(semesterBox);

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
        JLabel semLabel = UIStyle.createFieldLabel("SEMESTER");
        formCenter.add(semLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formCenter.add(semesterBox, gbc);
        row++;

        // Submit Button & Cancel Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(UIStyle.BG_CARD);
        
        JButton cancelBtn = UIStyle.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> goBack());

        JButton submitBtn = UIStyle.createPrimaryButton("Submit");
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

        JScrollPane scrollPane = createFormScrollPane(formCenter);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setInitialDataAndTriggerAI(String grade, String major, String jobType, String courseName) {
        if (grade != null && !grade.equals("All Grades")) gradeCombo.setSelectedItem(grade);
        if (major != null && !major.equals("All majors")) majorCombo.setSelectedItem(major);
        if (jobType != null && !jobType.equals("All Categories")) jobTypeCombo.setSelectedItem(jobType);

        generateAITemplate(courseName);
    }

    private void generateAITemplate(String courseName) {
        JDialog loadingDialog = new JDialog(parent, "AI Assistant", true);
        loadingDialog.setLayout(new BorderLayout(10, 10));
        loadingDialog.setSize(350, 120);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JLabel msgLabel = new JLabel("AI Assistant is generating the job details, please wait...", SwingConstants.CENTER);
        msgLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        p.add(msgLabel, BorderLayout.CENTER);
        p.add(progressBar, BorderLayout.SOUTH);
        loadingDialog.add(p);

        String selGrade = gradeCombo.getSelectedItem() != null ? gradeCombo.getSelectedItem().toString() : "";
        String selMajor = majorCombo.getSelectedItem() != null ? majorCombo.getSelectedItem().toString() : "";
        String selJobType = jobTypeCombo.getSelectedItem() != null ? jobTypeCombo.getSelectedItem().toString() : "";

        SwingWorker<java.util.Map<String, String>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.Map<String, String> doInBackground() throws Exception {
                return moPublishService.generateAITemplate(selGrade, selMajor, selJobType, courseName);
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    java.util.Map<String, String> result = get();
                    if (result != null) {
                        if (result.containsKey("title") && !result.get("title").isEmpty()) {
                            jobTitleField.setText(result.get("title"));
                        }
                        if (result.containsKey("desc") && !result.get("desc").isEmpty()) {
                            jobDescArea.setText(result.get("desc"));
                        }
                        if (result.containsKey("req") && !result.get("req").isEmpty()) {
                            jobReqArea.setText(result.get("req"));
                        }
                        JOptionPane.showMessageDialog(JobPublishPanel.this, 
                            "AI generation complete. Please review and fill in the remaining details (Headcount, Location, Date).", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(JobPublishPanel.this, 
                        "AI generation failed: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        loadingDialog.setVisible(true);
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

    private void styleCombo(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(800, 38));
        combo.setFont(UIStyle.FONT_BODY);
        combo.setBackground(UIStyle.BG_CARD);
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = UIStyle.createTextField(30);
        field.setForeground(UIStyle.TEXT_SECONDARY);
        field.setText(placeholder);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(UIStyle.TEXT_PRIMARY);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setForeground(UIStyle.TEXT_SECONDARY);
                    field.setText(placeholder);
                }
            }
        });
        return field;
    }

    private JScrollPane createTextAreaScroll(String placeholder, JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(UIStyle.FONT_BODY);
        area.setForeground(UIStyle.TEXT_SECONDARY);
        area.setText(placeholder);
        
        area.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (area.getText().equals(placeholder)) {
                    area.setText("");
                    area.setForeground(UIStyle.TEXT_PRIMARY);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (area.getText().isEmpty()) {
                    area.setForeground(UIStyle.TEXT_SECONDARY);
                    area.setText(placeholder);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(800, 120));
        scroll.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIStyle.BORDER, 1, true),
            new EmptyBorder(5, 15, 5, 15)
        ));
        UIStyle.styleScrollPane(scroll);
        return scroll;
    }

    private JScrollPane createFormScrollPane(JPanel formCenter) {
        JScrollPane scrollPane = new JScrollPane(formCenter);
        UIStyle.styleScrollPane(scrollPane);
        scrollPane.setBorder(null);
        return scrollPane;
    }
}