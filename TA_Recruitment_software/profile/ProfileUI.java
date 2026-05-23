package TA_Recruitment_software.profile;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import TA_Recruitment_software.admin_system.model.User;
import java.awt.*;
import java.io.File;
import javax.swing.*;

public class ProfileUI {

    public static void showUpdateProfileDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login first.");
            return;
        }
        
        try {
            User user = context.getAuthService().getUserByToken(token);

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(UIStyle.BG_CARD);
            formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            JTextField majorField = UIStyle.createTextField(20);
            majorField.setText(user.getMajor());
            JTextField emailField = UIStyle.createTextField(20);
            emailField.setText(user.getEmail());
            JTextField phoneField = UIStyle.createTextField(20);
            phoneField.setText(user.getPhone());
            JTextArea skillsArea = new JTextArea(user.getSkills() == null ? "" : user.getSkills(), 3, 20);
            skillsArea.setFont(UIStyle.FONT_BODY);
            skillsArea.setLineWrap(true);
            skillsArea.setWrapStyleWord(true);
            JTextField cvField = UIStyle.createTextField(20);
            cvField.setText(user.getCvFilePath());

            JButton chooseCvButton = UIStyle.createSecondaryButton("Choose...");
            chooseCvButton.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("\u9009\u62E9\u7B80\u5386 (.pdf/.doc/.docx)");
                int choice = chooser.showOpenDialog(parent);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    if (file != null) {
                        cvField.setText(file.getAbsolutePath());
                    }
                }
            });

            int row = 0;
            addFieldRow(formPanel, gbc, row++, "\u4E13\u4E1A:", majorField);
            addFieldRow(formPanel, gbc, row++, "\u90AE\u7BB1:", emailField);
            addFieldRow(formPanel, gbc, row++, "\u7535\u8BDD:", phoneField);

            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
            JLabel skillsLbl = UIStyle.createFieldLabel("\u6280\u80FD\u4E13\u957F");
            formPanel.add(skillsLbl, gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            JScrollPane skillsScroll = new JScrollPane(skillsArea);
            skillsScroll.setPreferredSize(new Dimension(200, 60));
            UIStyle.styleScrollPane(skillsScroll);
            formPanel.add(skillsScroll, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
            JLabel cvLbl = UIStyle.createFieldLabel("\u7B80\u5386\u6587\u4EF6");
            formPanel.add(cvLbl, gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            JPanel cvPanel = new JPanel(new BorderLayout(8, 0));
            cvPanel.setBackground(UIStyle.BG_CARD);
            cvPanel.add(cvField, BorderLayout.CENTER);
            cvPanel.add(chooseCvButton, BorderLayout.EAST);
            formPanel.add(cvPanel, gbc);

            int result = JOptionPane.showConfirmDialog(parent, formPanel, "\u66F4\u65B0\u4E2A\u4EBA\u8D44\u6599", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                context.getProfileService().updateProfile(
                    token,
                    majorField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    skillsArea.getText().trim()
                );
                String cvPath = cvField.getText().trim();
                String storedPath = null;
                if (!cvPath.isEmpty()) {
                    User updated = context.getProfileService().uploadCV(token, cvPath);
                    storedPath = updated.getCvFilePath();
                }
                String message = "\u4E2A\u4EBA\u8D44\u6599\u66F4\u65B0\u6210\u529F!";
                if (storedPath != null && !storedPath.trim().isEmpty()) {
                    message += "\n\u7B80\u5386\u5B58\u50A8\u4F4D\u7F6E: " + storedPath;
                }
                JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addFieldRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.0;
        JLabel lbl = UIStyle.createFieldLabel(label.replace(":", ""));
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(field, gbc);
    }
}