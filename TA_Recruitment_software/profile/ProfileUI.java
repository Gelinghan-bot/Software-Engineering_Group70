package TA_Recruitment_software.profile;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.User;
import java.awt.*;
import javax.swing.*;

public class ProfileUI {

    public static void showUpdateProfileDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login first.");
            return;
        }
        
        try {
            User user = context.getAuthService().getUserByToken(token);

            JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
            JTextField majorField = new JTextField(user.getMajor(), 20);
            JTextField emailField = new JTextField(user.getEmail(), 20);
            JTextField phoneField = new JTextField(user.getPhone(), 20);
            JTextArea skillsArea = new JTextArea(user.getSkills() == null ? "" : user.getSkills(), 3, 20);
            JTextField cvField = new JTextField(user.getCvFilePath(), 20);

            formPanel.add(new JLabel("Major:")); formPanel.add(majorField);
            formPanel.add(new JLabel("Email:")); formPanel.add(emailField);
            formPanel.add(new JLabel("Phone:")); formPanel.add(phoneField);
            JScrollPane skillsScroll = new JScrollPane(skillsArea);
            skillsScroll.getVerticalScrollBar().setUnitIncrement(16);
            skillsScroll.getHorizontalScrollBar().setUnitIncrement(16);
            formPanel.add(new JLabel("Skills:")); formPanel.add(skillsScroll);
            formPanel.add(new JLabel("CV Path:")); formPanel.add(cvField);

            int result = JOptionPane.showConfirmDialog(parent, formPanel, "Update Profile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                context.getProfileService().updateProfile(
                    token,
                    majorField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    skillsArea.getText().trim()
                );
                String cvPath = cvField.getText().trim();
                if (!cvPath.isEmpty()) {
                    context.getProfileService().uploadCV(token, cvPath);
                }
                JOptionPane.showMessageDialog(parent, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}