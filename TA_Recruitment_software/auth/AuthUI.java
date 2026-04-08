package TA_Recruitment_software.auth;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.User;
import javax.swing.*;
import java.awt.*;

public class AuthUI {

    public static String[] showLoginDialog(JFrame parent, RecruitmentSystemContext context) {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField accountField = new JTextField();
        JPasswordField passField = new JPasswordField();
        panel.add(new JLabel("Account ID:"));
        panel.add(accountField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String account = accountField.getText().trim();
                String pass = new String(passField.getPassword());
                String token = context.getAuthService().login(account, pass);
                // Also get the user role to return
                User u = context.getAuthService().getUserByToken(token);
                JOptionPane.showMessageDialog(parent, "Login Successful! Role: " + u.getRole().name());
                return new String[]{token, u.getRole().name()};
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    public static void showRegisterDialog(JFrame parent, RecruitmentSystemContext context) {
        String[] options = {"Register as TA", "Register as MO", "Cancel"};
        int choice = JOptionPane.showOptionDialog(parent, "Select account type to register:", "Register",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            registerTA(parent, context);
        } else if (choice == 1) {
            registerMO(parent, context);
        }
    }

    private static void registerTA(JFrame parent, RecruitmentSystemContext context) {
        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        JTextField accountField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JTextField nameField = new JTextField();
        JTextField studentIdField = new JTextField();
        JTextField majorField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();

        panel.add(new JLabel("Account ID:")); panel.add(accountField);
        panel.add(new JLabel("Password:")); panel.add(passField);
        panel.add(new JLabel("Full Name:")); panel.add(nameField);
        panel.add(new JLabel("Student ID:")); panel.add(studentIdField);
        panel.add(new JLabel("Major:")); panel.add(majorField);
        panel.add(new JLabel("Email:")); panel.add(emailField);
        panel.add(new JLabel("Phone:")); panel.add(phoneField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "TA Registration", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                context.getAuthService().registerTA(
                    accountField.getText().trim(),
                    new String(passField.getPassword()),
                    nameField.getText().trim(),
                    studentIdField.getText().trim(),
                    majorField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim()
                );
                JOptionPane.showMessageDialog(parent, "Registration successful! Pending admin approval.");
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void registerMO(JFrame parent, RecruitmentSystemContext context) {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        JTextField accountField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JTextField nameField = new JTextField();
        JTextField deptField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();

        panel.add(new JLabel("Account ID:")); panel.add(accountField);
        panel.add(new JLabel("Password:")); panel.add(passField);
        panel.add(new JLabel("Full Name:")); panel.add(nameField);
        panel.add(new JLabel("Department:")); panel.add(deptField);
        panel.add(new JLabel("Email:")); panel.add(emailField);
        panel.add(new JLabel("Phone:")); panel.add(phoneField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "MO Registration", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                context.getAuthService().registerMO(
                    accountField.getText().trim(),
                    new String(passField.getPassword()),
                    nameField.getText().trim(),
                    deptField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim()
                );
                JOptionPane.showMessageDialog(parent, "Registration successful! Pending admin approval.");
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
