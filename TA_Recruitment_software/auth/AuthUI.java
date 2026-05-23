package TA_Recruitment_software.auth;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Authentication UI module providing Login, Registration (TA/MO),
 * Change Password, and Logout dialogs.
 *
 * Aligned with Backlog TA-01 (Account Registration) and TA-02 (Login & Session).
 */
public class AuthUI {

    // ========================= Brand Colors (using UIStyle) =========================
    private static final Color PRIMARY_GREEN = UIStyle.PRIMARY;
    private static final Color DARK_TEXT = UIStyle.TEXT_PRIMARY;
    private static final Color ERROR_RED = UIStyle.DANGER;
    private static final Color SUCCESS_GREEN = UIStyle.SUCCESS;
    private static final Color WARN_ORANGE = UIStyle.WARNING;
    private static final Font TITLE_FONT = UIStyle.FONT_TITLE;
    private static final Font LABEL_FONT = UIStyle.FONT_BODY;
    private static final Font FIELD_FONT = UIStyle.FONT_BODY;
    private static final Font BUTTON_FONT = UIStyle.FONT_BUTTON;
    private static final Font HINT_FONT = UIStyle.FONT_SMALL;

    // ========================= Login Dialog =========================

    /**
     * Show the login dialog. Returns [token, roleName] on success, null on cancel/failure.
     * If there are valid persistent sessions, shows a dialog to choose which one to use.
     */
    public static String[] showLoginDialog(JFrame parent, RecruitmentSystemContext context) {
        java.util.Map<String, TA_Recruitment_software.auth.SessionContext> validSessions = getValidPersistentSessions(context);

        JDialog dialog = new JDialog(parent, "Login - JobHere", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Welcome Back", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_GREEN);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = createGBC();

        JTextField accountField = createStyledTextField();
        JPasswordField passField = createStyledPasswordField();
        JCheckBox showPassCheck = new JCheckBox("Show password");
        showPassCheck.setFont(HINT_FONT);
        showPassCheck.setBackground(Color.WHITE);
        showPassCheck.addActionListener(e -> {
            passField.setEchoChar(showPassCheck.isSelected() ? (char) 0 : '\u2022');
        });

        // Login history dropdown
        java.util.List<String> historyAccounts = LoginHistoryStore.getAllAccounts();
        String[] historyArray = new String[historyAccounts.size() + 1];
        historyArray[0] = "-- Select from history --";
        for (int i = 0; i < historyAccounts.size(); i++) {
            historyArray[i + 1] = historyAccounts.get(i);
        }
        JComboBox<String> historyCombo = new JComboBox<>(historyArray);
        historyCombo.setFont(FIELD_FONT);
        historyCombo.setEnabled(historyAccounts.size() > 0);
        historyCombo.addActionListener(e -> {
            int idx = historyCombo.getSelectedIndex();
            if (idx > 0 && idx < historyArray.length) {
                accountField.setText(historyArray[idx]);
                passField.requestFocus();
            }
        });

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(HINT_FONT);
        statusLabel.setForeground(ERROR_RED);

        addFormRow(formPanel, gbc, 0, "Recent Accounts", historyCombo);
        addFormRow(formPanel, gbc, 1, "Account ID", accountField);
        addFormRow(formPanel, gbc, 2, "Password", passField);

        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(showPassCheck, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(statusLabel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton loginBtn = createPrimaryButton("Login");
        JButton loginInBtn = createAccentButton("Resume Session");
        JButton clearHistoryBtn = createSecondaryButton("Clear History");
        JButton cancelBtn = createSecondaryButton("Cancel");

        final String[][] result = {null};

        loginBtn.addActionListener(e -> {
            try {
                String account = accountField.getText().trim();
                String pass = new String(passField.getPassword());
                String token = context.getAuthService().login(account, pass);
                User u = context.getAuthService().getUserByToken(token);
                result[0] = new String[]{token, u.getRole().name()};
                dialog.dispose();
                JOptionPane.showMessageDialog(parent,
                    "Login successful!\nWelcome, " + u.getFullName() + " (" + u.getRole().name() + ")",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                if (u.getRole() == Role.TA) {
                    TaPortalUI.onTaLogin(parent, context, token);
                }
            } catch (AppException ex) {
                statusLabel.setText(ex.getMessage());
            }
        });

        loginInBtn.addActionListener(e -> {
            if (validSessions.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No saved login sessions available.", "No Sessions", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String[] autoLoginResult = showAutoLoginDialog(parent, context, validSessions);
            if (autoLoginResult != null) {
                result[0] = autoLoginResult;
                dialog.dispose();
            }
        });

        clearHistoryBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(parent,
                "Are you sure you want to clear login history?",
                "Clear History", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                LoginHistoryStore.clearAll();
                JOptionPane.showMessageDialog(parent, "Login history cleared!");
                dialog.dispose();
                showLoginDialog(parent, context);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        // Enter key triggers login
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginBtn.doClick();
                }
            }
        };
        accountField.addKeyListener(enterKeyListener);
        passField.addKeyListener(enterKeyListener);

        buttonPanel.add(loginBtn);
        buttonPanel.add(loginInBtn);
        buttonPanel.add(clearHistoryBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);

        return result[0];
    }

    // ========================= Register Dialog =========================

    /**
     * Show the registration type selection dialog, then redirect to TA or MO registration.
     */
    public static void showRegisterDialog(JFrame parent, RecruitmentSystemContext context) {
        String[] options = {"Register as TA", "Register as MO", "Cancel"};
        int choice = JOptionPane.showOptionDialog(parent,
            "Select your role to begin registration.\nYour account will need admin approval before you can login.",
            "Register - JobHere",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            registerTA(parent, context);
        } else if (choice == 1) {
            registerMO(parent, context);
        }
    }

    // ========================= TA Registration =========================

    private static void registerTA(JFrame parent, RecruitmentSystemContext context) {
        JDialog dialog = new JDialog(parent, "TA Registration - JobHere", true);
        dialog.setSize(500, 580);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(new EmptyBorder(15, 25, 15, 25));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Teaching Assistant Registration", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_GREEN);
        titleLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Account Information", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12), DARK_TEXT),
            new EmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = createGBC();

        JTextField accountField = createStyledTextField();
        JPasswordField passField = createStyledPasswordField();
        JPasswordField confirmPassField = createStyledPasswordField();
        JTextField nameField = createStyledTextField();
        JTextField studentIdField = createStyledTextField();
        JTextField majorField = createStyledTextField();
        JTextField emailField = createStyledTextField();
        JTextField phoneField = createStyledTextField();

        JLabel passwordStrengthLabel = new JLabel(" ");
        passwordStrengthLabel.setFont(HINT_FONT);

        // Update password strength indicator as user types
        addPasswordStrengthListener(passField, passwordStrengthLabel);

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(HINT_FONT);
        statusLabel.setForeground(ERROR_RED);

        addFormRow(formPanel, gbc, 0, "Account ID *", accountField);
        addFormRow(formPanel, gbc, 1, "Password *", passField);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordStrengthLabel, gbc);

        addFormRow(formPanel, gbc, 3, "Confirm Password *", confirmPassField);
        addFormRow(formPanel, gbc, 4, "Full Name *", nameField);
        addFormRow(formPanel, gbc, 5, "Student ID *", studentIdField);
        addFormRow(formPanel, gbc, 6, "Major *", majorField);
        addFormRow(formPanel, gbc, 7, "Email *", emailField);
        addFormRow(formPanel, gbc, 8, "Phone *", phoneField);

        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(statusLabel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton registerBtn = createPrimaryButton("Register");
        JButton cancelBtn = createSecondaryButton("Cancel");

        registerBtn.addActionListener(e -> {
            try {
                context.getAuthService().registerTA(
                    accountField.getText().trim(),
                    new String(passField.getPassword()),
                    new String(confirmPassField.getPassword()),
                    nameField.getText().trim(),
                    studentIdField.getText().trim(),
                    majorField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim()
                );
                dialog.dispose();
                JOptionPane.showMessageDialog(parent,
                    "Registration successful!\n\nYour account is pending admin approval.\n"
                    + "You will be able to login once approved.",
                    "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (AppException ex) {
                statusLabel.setText(ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    // ========================= MO Registration =========================

    private static void registerMO(JFrame parent, RecruitmentSystemContext context) {
        JDialog dialog = new JDialog(parent, "MO Registration - JobHere", true);
        dialog.setSize(500, 530);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(new EmptyBorder(15, 25, 15, 25));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Module Officer Registration", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_GREEN);
        titleLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Account Information", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12), DARK_TEXT),
            new EmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = createGBC();

        JTextField accountField = createStyledTextField();
        JPasswordField passField = createStyledPasswordField();
        JPasswordField confirmPassField = createStyledPasswordField();
        JTextField nameField = createStyledTextField();
        JTextField deptField = createStyledTextField();
        JTextField emailField = createStyledTextField();
        JTextField phoneField = createStyledTextField();

        JLabel passwordStrengthLabel = new JLabel(" ");
        passwordStrengthLabel.setFont(HINT_FONT);

        addPasswordStrengthListener(passField, passwordStrengthLabel);

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(HINT_FONT);
        statusLabel.setForeground(ERROR_RED);

        addFormRow(formPanel, gbc, 0, "Account ID *", accountField);
        addFormRow(formPanel, gbc, 1, "Password *", passField);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordStrengthLabel, gbc);

        addFormRow(formPanel, gbc, 3, "Confirm Password *", confirmPassField);
        addFormRow(formPanel, gbc, 4, "Full Name *", nameField);
        addFormRow(formPanel, gbc, 5, "Department *", deptField);
        addFormRow(formPanel, gbc, 6, "Email *", emailField);
        addFormRow(formPanel, gbc, 7, "Phone *", phoneField);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(statusLabel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton registerBtn = createPrimaryButton("Register");
        JButton cancelBtn = createSecondaryButton("Cancel");

        registerBtn.addActionListener(e -> {
            try {
                context.getAuthService().registerMO(
                    accountField.getText().trim(),
                    new String(passField.getPassword()),
                    new String(confirmPassField.getPassword()),
                    nameField.getText().trim(),
                    deptField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim()
                );
                dialog.dispose();
                JOptionPane.showMessageDialog(parent,
                    "Registration successful!\n\nYour account is pending admin approval.\n"
                    + "You will be able to login once approved.",
                    "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (AppException ex) {
                statusLabel.setText(ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    // ========================= Change Password Dialog =========================

    /**
     * Show the change password dialog for a logged-in user.
     */
    public static void showChangePasswordDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null || !context.getAuthService().isLoggedIn(token)) {
            JOptionPane.showMessageDialog(parent, "Please login first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(parent, "Change Password - JobHere", true);
        dialog.setSize(440, 350);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Change Password", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_GREEN);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = createGBC();

        JPasswordField oldPassField = createStyledPasswordField();
        JPasswordField newPassField = createStyledPasswordField();
        JPasswordField confirmPassField = createStyledPasswordField();

        JLabel passwordStrengthLabel = new JLabel(" ");
        passwordStrengthLabel.setFont(HINT_FONT);

        addPasswordStrengthListener(newPassField, passwordStrengthLabel);

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(HINT_FONT);
        statusLabel.setForeground(ERROR_RED);

        addFormRow(formPanel, gbc, 0, "Current Password", oldPassField);
        addFormRow(formPanel, gbc, 1, "New Password", newPassField);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordStrengthLabel, gbc);

        addFormRow(formPanel, gbc, 3, "Confirm New Password", confirmPassField);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(statusLabel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton changeBtn = createPrimaryButton("Change Password");
        JButton cancelBtn = createSecondaryButton("Cancel");

        changeBtn.addActionListener(e -> {
            try {
                context.getAuthService().changePassword(
                    token,
                    new String(oldPassField.getPassword()),
                    new String(newPassField.getPassword()),
                    new String(confirmPassField.getPassword())
                );
                dialog.dispose();
                JOptionPane.showMessageDialog(parent,
                    "Password changed successfully!\nPlease login again with your new password.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (AppException ex) {
                statusLabel.setText(ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(changeBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * Open TA portal (notifications, limits, history, PDF export).
     * Wire this from Home PERSONAL menu: AuthUI.showTaPortal(parent, context, token)
     */
    public static void showTaPortal(JFrame parent, RecruitmentSystemContext context, String token) {
        TaPortalUI.showTaHub(parent, context, token);
    }

    // ========================= Logout =========================

    /**
     * Perform logout and show confirmation.
     */
    public static void performLogout(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "You are not logged in.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        context.getAuthService().logout(token);
        JOptionPane.showMessageDialog(parent,
            "You have been logged out successfully.",
            "Logged Out", JOptionPane.INFORMATION_MESSAGE);
    }

    // ========================= UI Helper Methods =========================

    private static JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(FIELD_FONT);
        field.setPreferredSize(new Dimension(250, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(2, 8, 2, 8)
        ));
        return field;
    }

    private static JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(FIELD_FONT);
        field.setPreferredSize(new Dimension(250, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(2, 8, 2, 8)
        ));
        return field;
    }

    private static JButton createPrimaryButton(String text) {
        return UIStyle.createPrimaryButton(text);
    }

    private static JButton createSecondaryButton(String text) {
        return UIStyle.createSecondaryButton(text);
    }

    private static JButton createAccentButton(String text) {
        return UIStyle.createAccentButton(text);
    }

    private static GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private static void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        JLabel jLabel = new JLabel(label + ":");
        jLabel.setFont(LABEL_FONT);
        jLabel.setForeground(DARK_TEXT);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.3;
        panel.add(jLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    /**
     * Attach a DocumentListener to a password field to update strength indicator.
     * Uses DocumentListener instead of KeyListener to avoid interfering with keyboard input.
     */
    private static void addPasswordStrengthListener(JPasswordField field, JLabel label) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { update(); }
            @Override
            public void removeUpdate(DocumentEvent e) { update(); }
            @Override
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() {
                updatePasswordStrength(new String(field.getPassword()), label);
            }
        });
    }

    /**
     * Update the password strength indicator label based on the current password text.
     */
    private static void updatePasswordStrength(String password, JLabel label) {
        if (password.isEmpty()) {
            label.setText(" ");
            return;
        }

        int score = 0;
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[^A-Za-z0-9].*")) score++;

        if (score <= 2) {
            label.setText("Strength: Weak");
            label.setForeground(ERROR_RED);
        } else if (score <= 4) {
            label.setText("Strength: Medium");
            label.setForeground(WARN_ORANGE);
        } else {
            label.setText("Strength: Strong");
            label.setForeground(SUCCESS_GREEN);
        }
    }

    /**
     * Get all valid persistent sessions that haven't expired.
     */
    private static java.util.Map<String, TA_Recruitment_software.auth.SessionContext> getValidPersistentSessions(RecruitmentSystemContext context) {
        return context.getSessionManager().getAllValidSessions();
    }

    /**
     * Show dialog to choose from valid persistent sessions for auto-login.
     */
    private static String[] showAutoLoginDialog(JFrame parent, RecruitmentSystemContext context,
                                               java.util.Map<String, TA_Recruitment_software.auth.SessionContext> validSessions) {
        if (validSessions.isEmpty()) {
            return null;
        }

        JDialog dialog = new JDialog(parent, "Resume Session - JobHere", true);
        dialog.setSize(480, 250);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Welcome Back!", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_GREEN);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Session list
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        String[] sessionOptions = new String[validSessions.size() + 1];
        java.util.List<String> tokens = new java.util.ArrayList<>(validSessions.keySet());
        sessionOptions[0] = "-- Choose a session to resume --";
        int i = 1;
        for (TA_Recruitment_software.auth.SessionContext ctx : validSessions.values()) {
            sessionOptions[i] = ctx.getFullName() + " (" + ctx.getAccountId() + ") - " + ctx.getRole();
            i++;
        }

        JComboBox<String> sessionCombo = new JComboBox<>(sessionOptions);
        sessionCombo.setFont(LABEL_FONT);
        sessionCombo.setSelectedIndex(0);

        centerPanel.add(sessionCombo, BorderLayout.CENTER);

        // Message
        JLabel messageLabel = new JLabel("<html>Select a session to continue, or login with a different account.</html>", SwingConstants.CENTER);
        messageLabel.setFont(LABEL_FONT);
        messageLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        centerPanel.add(messageLabel, BorderLayout.NORTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 2));
        buttonPanel.setBackground(Color.WHITE);

        JButton resumeBtn = UIStyle.createPrimaryButton("Resume Selected Session");
        JButton otherAccountBtn = UIStyle.createSecondaryButton("Login Other Account");

        final String[] result = new String[2];

        resumeBtn.addActionListener(e -> {
            int selectedIndex = sessionCombo.getSelectedIndex();
            if (selectedIndex > 0 && selectedIndex <= tokens.size()) {
                String selectedToken = tokens.get(selectedIndex - 1);
                TA_Recruitment_software.auth.SessionContext ctx = validSessions.get(selectedToken);
                result[0] = selectedToken;
                result[1] = ctx.getRole().name();
                dialog.dispose();
            }
        });

        otherAccountBtn.addActionListener(e -> {
            dialog.dispose();
        });

        buttonPanel.add(resumeBtn);
        buttonPanel.add(otherAccountBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);

        return result[0] != null ? result : null;
    }
}
