package TA_Recruitment_software.profile;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import java.awt.*;
import java.io.File;
import javax.swing.*;

/**
 * UI module for user profile management features.
 * <p>
 * Provides interactive Swing components for:
 * <ul>
 *   <li>Viewing and updating personal profile information</li>
 *   <li>MO users can update: Full Name, Department, Email, Phone</li>
 *   <li>TA users can update: Major, Email, Phone, Skills, CV File</li>
 *   <li>TA users can upload CV files (.pdf/.doc/.docx) via file chooser</li>
 * </ul>
 * </p>
 *
 * @author Group70
 * @see ProfileService
 */
public class ProfileUI {

    public static void showUpdateProfileDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login first.");
            return;
        }
        
        try {
            User user = context.getAuthService().getUserByToken(token);
            boolean isMO = user.getRole() == Role.MO;

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(UIStyle.BG_CARD);
            formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            JTextField emailField = UIStyle.createTextField(20);
            emailField.setText(user.getEmail());
            JTextField phoneField = UIStyle.createTextField(20);
            phoneField.setText(user.getPhone());

            JTextField nameField = null;
            JTextField deptField = null;
            JTextField majorField = null;
            JTextArea skillsArea = null;
            JTextField cvField = null;

            int row = 0;

            if (isMO) {
                // === MO Profile: Full Name, Department, Email, Phone (matches registration) ===
                nameField = UIStyle.createTextField(20);
                nameField.setText(user.getFullName() != null ? user.getFullName() : "");
                addFieldRow(formPanel, gbc, row++, "Full Name", nameField);

                deptField = UIStyle.createTextField(20);
                deptField.setText(user.getDepartment() != null ? user.getDepartment() : "");
                addFieldRow(formPanel, gbc, row++, "Department", deptField);

                addFieldRow(formPanel, gbc, row++, "Email", emailField);
                addFieldRow(formPanel, gbc, row++, "Phone", phoneField);
            } else {
                // === TA Profile: Major, Email, Phone, Skills, CV (matches registration) ===
                majorField = UIStyle.createTextField(20);
                majorField.setText(user.getMajor());
                addFieldRow(formPanel, gbc, row++, "Major", majorField);
                addFieldRow(formPanel, gbc, row++, "Email", emailField);
                addFieldRow(formPanel, gbc, row++, "Phone", phoneField);

                skillsArea = new JTextArea(user.getSkills() == null ? "" : user.getSkills(), 3, 20);
                skillsArea.setFont(UIStyle.FONT_BODY);
                skillsArea.setLineWrap(true);
                skillsArea.setWrapStyleWord(true);

                gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
                JLabel skillsLbl = UIStyle.createFieldLabel("Skills");
                formPanel.add(skillsLbl, gbc);
                gbc.gridx = 1; gbc.weightx = 1.0;
                JScrollPane skillsScroll = new JScrollPane(skillsArea);
                skillsScroll.setPreferredSize(new Dimension(200, 60));
                UIStyle.styleScrollPane(skillsScroll);
                formPanel.add(skillsScroll, gbc);
                row++;

                final JTextField cvFieldFinal = UIStyle.createTextField(20);
                cvField = cvFieldFinal;
                cvField.setText(user.getCvFilePath());
                JButton chooseCvButton = UIStyle.createSecondaryButton("Choose...");
                chooseCvButton.addActionListener(e -> {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Choose CV (.pdf/.doc/.docx)");
                    int choice = chooser.showOpenDialog(parent);
                    if (choice == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        if (file != null) cvFieldFinal.setText(file.getAbsolutePath());
                    }
                });

                gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
                JLabel cvLbl = UIStyle.createFieldLabel("CV File");
                formPanel.add(cvLbl, gbc);
                gbc.gridx = 1; gbc.weightx = 1.0;
                JPanel cvPanel = new JPanel(new BorderLayout(8, 0));
                cvPanel.setBackground(UIStyle.BG_CARD);
                cvPanel.add(cvField, BorderLayout.CENTER);
                cvPanel.add(chooseCvButton, BorderLayout.EAST);
                formPanel.add(cvPanel, gbc);
                row++;
            }

            // --- Save logic ---
            final JTextField fNameField = nameField;
            final JTextField fDeptField = deptField;
            final JTextField fMajorField = majorField;
            final JTextArea fSkillsArea = skillsArea;
            final JTextField fCvField = cvField;

            String title = isMO ? "My Profile (MO)" : "My Profile (TA)";
            int result = JOptionPane.showConfirmDialog(parent, formPanel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                if (isMO) {
                    context.getProfileService().updateMOProfile(
                        token,
                        fNameField.getText().trim(),
                        fDeptField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim()
                    );
                } else {
                    context.getProfileService().updateProfile(
                        token,
                        fMajorField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        fSkillsArea.getText().trim()
                    );
                    String cvPath = fCvField.getText().trim();
                    if (!cvPath.isEmpty()) {
                        context.getProfileService().uploadCV(token, cvPath);
                    }
                }
                JOptionPane.showMessageDialog(parent, "Profile updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addFieldRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.0;
        JLabel lbl = UIStyle.createFieldLabel(label);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(field, gbc);
    }
}
