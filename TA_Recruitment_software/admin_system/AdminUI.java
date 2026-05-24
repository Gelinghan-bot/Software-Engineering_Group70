package TA_Recruitment_software.admin_system;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import TA_Recruitment_software.admin_system.model.Role;
import TA_Recruitment_software.admin_system.model.User;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * UI module for Administrator user management and system features.
 * <p>
 * Provides interactive Swing components for:
 * <ul>
 *   <li>Reviewing and approving/rejecting pending user registrations</li>
 *   <li>Managing all users (search, filter by role/work/enabled status)</li>
 *   <li>Editing user information and resetting passwords</li>
 *   <li>Enabling/disabling user accounts</li>
 *   <li>Viewing TA work details (assigned positions, active count)</li>
 *   <li>Viewing MO work details (published positions, open count)</li>
 *   <li>Viewing poll results from the forum</li>
 * </ul>
 * </p>
 * <p>
 * Access is restricted to ADMIN role only.
 * </p>
 *
 * @author Group70
 * @see AdminService
 */
public class AdminUI {

    public static void showPendingUsersDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as ADMIN first.");
            return;
        }

        try {
            List<User> pendingUsers = context.getAdminService().listPendingUsers(token);
            if (pendingUsers.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No pending users.");
                return;
            }

            DefaultTableModel model = new DefaultTableModel(new Object[]{"User ID", "Account ID", "Name", "Role"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (User u : pendingUsers) {
                model.addRow(new Object[]{u.getUserId(), u.getAccountId(), u.getFullName(), u.getRole()});
            }

            JTable table = UIStyle.createStyledTable(model);
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setBackground(UIStyle.BG_PAGE);
            JScrollPane scrollPane = UIStyle.wrapTableInScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(500, 300));

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            btnPanel.setBackground(UIStyle.BG_PAGE);
            JButton approveBtn = UIStyle.createPrimaryButton("Approve Selected");
            JButton rejectBtn = UIStyle.createDangerButton("Reject Selected");

            approveBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String userId = (String) model.getValueAt(row, 0);
                    context.getAdminService().approveUser(token, userId, true);
                    JOptionPane.showMessageDialog(parent, "User Approved!");
                    model.removeRow(row);
                }
            });

            rejectBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String userId = (String) model.getValueAt(row, 0);
                    context.getAdminService().approveUser(token, userId, false);
                    JOptionPane.showMessageDialog(parent, "User Rejected!");
                    model.removeRow(row);
                }
            });

            btnPanel.add(approveBtn);
            btnPanel.add(rejectBtn);
            panel.add(btnPanel, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(parent, panel, "Pending Registration Requests", JOptionPane.PLAIN_MESSAGE);

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showManageUsersDialog(JFrame parent, RecruitmentSystemContext context, String token) {
        showManageUsersDialog(parent, context, token, null, null, null, null);
    }

    public static void showManageUsersDialog(JFrame parent, RecruitmentSystemContext context, String token,
                                             String filterRole, String filterKeyword) {
        showManageUsersDialog(parent, context, token, filterRole, filterKeyword, null, null);
    }

    public static void showManageUsersDialog(JFrame parent, RecruitmentSystemContext context, String token,
                                             String filterRole, String filterKeyword, String filterWorkCount,
                                             String filterEnabled) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as ADMIN first.");
            return;
        }
        
        try {
            List<User> allUsers = context.getAdminService().listAllUsers(token);

            // ---- Filter toolbar (multi-row GridBagLayout) ----
            JPanel filterPanel = new JPanel(new GridBagLayout());
            filterPanel.setBackground(UIStyle.BG_PAGE);
            filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(3, 5, 3, 5);
            g.fill = GridBagConstraints.HORIZONTAL;

            g.gridx = 0; g.gridy = 0;
            filterPanel.add(new JLabel("Role:"), g);
            g.gridx = 1;
            JComboBox<String> roleFilterCombo = new JComboBox<>(new String[]{"All Role", "TA", "MO", "ADMIN"});
            roleFilterCombo.setPreferredSize(new Dimension(100, 28));
            filterPanel.add(roleFilterCombo, g);

            g.gridx = 2;
            filterPanel.add(new JLabel("  Work:"), g);
            g.gridx = 3;
            JComboBox<String> workCountCombo = new JComboBox<>(new String[]{"All Work", "Has Work", "No Work"});
            workCountCombo.setPreferredSize(new Dimension(110, 28));
            filterPanel.add(workCountCombo, g);

            g.gridx = 4;
            filterPanel.add(new JLabel("  Enabled:"), g);
            g.gridx = 5;
            JComboBox<String> enabledCombo = new JComboBox<>(new String[]{"All Enabled", "Yes", "No"});
            enabledCombo.setPreferredSize(new Dimension(115, 28));
            filterPanel.add(enabledCombo, g);

            g.gridx = 0; g.gridy = 1; g.gridwidth = 1;
            filterPanel.add(new JLabel("Keyword:"), g);
            g.gridx = 1; g.gridwidth = 3;
            JTextField keywordField = UIStyle.createTextField(25);
            keywordField.setPreferredSize(new Dimension(280, 32));
            filterPanel.add(keywordField, g);

            g.gridx = 4; g.gridwidth = 1;
            JButton applyFilterBtn = UIStyle.createAccentButton("Search");
            applyFilterBtn.setPreferredSize(new Dimension(90, 28));
            filterPanel.add(applyFilterBtn, g);
            g.gridx = 5;
            JButton clearFilterBtn = UIStyle.createSecondaryButton("Reset");
            clearFilterBtn.setPreferredSize(new Dimension(80, 28));
            filterPanel.add(clearFilterBtn, g);

            // Summary label
            JLabel summaryLabel = new JLabel();
            summaryLabel.setFont(UIStyle.FONT_SMALL);
            summaryLabel.setForeground(UIStyle.TEXT_SECONDARY);
            g.gridx = 0; g.gridy = 2; g.gridwidth = 6;
            filterPanel.add(summaryLabel, g);

            // ---- Pre-fill filters from Home search ----
            if (filterRole != null) roleFilterCombo.setSelectedItem(filterRole);
            if (filterWorkCount != null) workCountCombo.setSelectedItem(filterWorkCount);
            if (filterEnabled != null) enabledCombo.setSelectedItem(filterEnabled);
            if (filterKeyword != null && !filterKeyword.isEmpty()) {
                keywordField.setText(filterKeyword);
                keywordField.setForeground(UIStyle.TEXT);
            }

            // ---- Table ----
            String[] cols = {"User ID", "Account", "Name", "Role", "Email", "Phone", "Major/Dept", "Work Count", "Enabled"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };

            java.util.function.Consumer<java.util.List<User>> rebuildTable = (userList) -> {
                model.setRowCount(0);
                for (User u : userList) {
                    // MO: show Department; TA/others: show Major, fallback to Department
                    String majorOrDept;
                    int workCount;
                    if (u.getRole() == Role.MO) {
                        majorOrDept = nvl(u.getDepartment());
                        workCount = context.getAdminService().getUserWorkCount(u.getUserId());
                    } else if (u.getRole() == Role.TA) {
                        majorOrDept = nvl(u.getMajor() != null && !u.getMajor().isEmpty() ? u.getMajor() : u.getDepartment());
                        workCount = context.getAdminService().getUserWorkCount(u.getUserId());
                    } else {
                        majorOrDept = nvl(u.getDepartment());
                        workCount = 0;
                    }
                    model.addRow(new Object[]{
                        u.getUserId(), u.getAccountId(), nvl(u.getFullName()), u.getRole(),
                        nvl(u.getEmail()), nvl(u.getPhone()),
                        majorOrDept,
                        workCount > 0 ? String.valueOf(workCount) : "-",
                        u.isEnabled() ? "Yes" : "No"
                    });
                }
                summaryLabel.setText("  Showing " + userList.size() + " of " + allUsers.size() + " users");
            };

            // Auto-apply filter if parameters were passed from Home search
            java.util.function.Supplier<java.util.List<User>> getFiltered = () -> {
                String selRole = (String) roleFilterCombo.getSelectedItem();
                String selWorkCount = (String) workCountCombo.getSelectedItem();
                String selEnabled = (String) enabledCombo.getSelectedItem();
                String kw = keywordField.getText().trim();
                return filterUsers(allUsers, context, selRole, selWorkCount, selEnabled, kw);
            };

            boolean hasFilter = filterRole != null || filterKeyword != null
                || filterWorkCount != null || filterEnabled != null;
            List<User> initialList = hasFilter ? getFiltered.get() : allUsers;
            rebuildTable.accept(initialList);

            JTable table = UIStyle.createStyledTable(model);
            JScrollPane scrollPane = UIStyle.wrapTableInScrollPane(table);

            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setBackground(UIStyle.BG_PAGE);
            panel.setBorder(UIStyle.pagePadding());
            panel.add(filterPanel, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);

            // ---- Filter actions (all combos + Enter key trigger filter) ----
            Runnable doFilter = () -> rebuildTable.accept(getFiltered.get());
            applyFilterBtn.addActionListener(ev -> doFilter.run());
            keywordField.addActionListener(ev -> doFilter.run());
            roleFilterCombo.addActionListener(ev -> doFilter.run());
            workCountCombo.addActionListener(ev -> doFilter.run());
            enabledCombo.addActionListener(ev -> doFilter.run());
            clearFilterBtn.addActionListener(ev -> {
                roleFilterCombo.setSelectedItem("All Role");
                workCountCombo.setSelectedItem("All Work");
                enabledCombo.setSelectedItem("All Enabled");
                keywordField.setText("");
                rebuildTable.accept(allUsers);
            });

            // ---- Bottom buttons ----
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            btnPanel.setBackground(UIStyle.BG_PAGE);
            JButton editBtn = UIStyle.createPrimaryButton("Edit Selected");
            JButton toggleBtn = UIStyle.createSecondaryButton("Toggle Enable/Disable");
            JButton workBtn = UIStyle.createAccentButton("Show Work");
            JButton backBtn = UIStyle.createSecondaryButton("\u2190 Back");

            editBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(parent, "Please select a user to edit."); return; }
                String userId = (String) model.getValueAt(row, 0);
                User selectedUser = allUsers.stream().filter(u -> u.getUserId().equals(userId)).findFirst().orElse(null);
                if (selectedUser != null) showEditUserDialog(parent, context, token, selectedUser, model, row);
            });

            toggleBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(parent, "Please select a user."); return; }
                String userId = (String) model.getValueAt(row, 0);
                boolean currentEnabled = "Yes".equals(model.getValueAt(row, 8));
                context.getAdminService().setUserEnabled(token, userId, !currentEnabled);
                model.setValueAt(!currentEnabled ? "Yes" : "No", row, 8);
                allUsers.stream().filter(u -> u.getUserId().equals(userId)).findFirst()
                    .ifPresent(u -> u.setEnabled(!currentEnabled));
                JOptionPane.showMessageDialog(parent, "User " + (currentEnabled ? "disabled" : "enabled") + "!");
            });

            workBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(parent, "Please select a user to view work."); return; }
                String userId = (String) model.getValueAt(row, 0);
                Role selectedRole = (Role) model.getValueAt(row, 3);
                if (selectedRole == Role.ADMIN) {
                    JOptionPane.showMessageDialog(parent, "Admin users have no work records.", "Show Work", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                User selectedUser = allUsers.stream().filter(u -> u.getUserId().equals(userId)).findFirst().orElse(null);
                if (selectedUser != null) showWorkDialog(parent, context, token, selectedUser);
            });

            btnPanel.add(editBtn);
            btnPanel.add(toggleBtn);
            btnPanel.add(workBtn);
            btnPanel.add(backBtn);
            panel.add(btnPanel, BorderLayout.SOUTH);

            // ---- Replace center content (like Manage Positions) ----
            Container contentPane = parent.getContentPane();
            BorderLayout layout = (BorderLayout) contentPane.getLayout();
            Component previousCenter = layout.getLayoutComponent(BorderLayout.CENTER);

            backBtn.addActionListener(e -> {
                contentPane.remove(panel);
                if (previousCenter != null) {
                    contentPane.add(previousCenter, BorderLayout.CENTER);
                }
                parent.revalidate();
                parent.repaint();
            });

            if (previousCenter != null) {
                contentPane.remove(previousCenter);
            }
            contentPane.add(panel, BorderLayout.CENTER);
            parent.revalidate();
            parent.repaint();

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Filter users by role, work count, enabled status, and keyword. */
    private static List<User> filterUsers(List<User> users, RecruitmentSystemContext context,
                                          String roleFilter, String workCountFilter,
                                          String enabledFilter, String keyword) {
        java.util.List<User> result = new java.util.ArrayList<>();
        String kw = (keyword == null) ? "" : keyword.toLowerCase().replace(" ", "");
        for (User u : users) {
            if (roleFilter != null && !"All Role".equals(roleFilter)) {
                if (!u.getRole().name().equalsIgnoreCase(roleFilter)) continue;
            }
            if (workCountFilter != null && !"All Work".equals(workCountFilter)) {
                int wc = context.getAdminService().getUserWorkCount(u.getUserId());
                if ("Has Work".equals(workCountFilter) && wc == 0) continue;
                if ("No Work".equals(workCountFilter) && wc > 0) continue;
            }
            if (enabledFilter != null && !"All Enabled".equals(enabledFilter)) {
                boolean wantEnabled = "Yes".equals(enabledFilter);
                if (u.isEnabled() != wantEnabled) continue;
            }
            if (!kw.isEmpty()) {
                boolean match = false;
                if (matchField(u.getFullName(), kw)) match = true;
                else if (matchField(u.getAccountId(), kw)) match = true;
                else if (matchField(u.getEmail(), kw)) match = true;
                else if (matchField(u.getPhone(), kw)) match = true;
                else if (matchField(u.getMajor(), kw)) match = true;
                else if (matchField(u.getDepartment(), kw)) match = true;
                else if (matchField(u.getStudentId(), kw)) match = true;
                else if (matchField(u.getUserId(), kw)) match = true;
                if (!match) continue;
            }
            result.add(u);
        }
        return result;
    }

    /** Match field value against keyword, ignoring spaces in both. */
    private static boolean matchField(String fieldValue, String keyword) {
        if (fieldValue == null || fieldValue.isEmpty()) return false;
        return fieldValue.toLowerCase().replace(" ", "").contains(keyword);
    }

    private static String nvl(String s) {
        return (s == null || s.isEmpty()) ? "-" : s;
    }

    public static void showEditUserDialog(JFrame parent, RecruitmentSystemContext context, String token, 
                                         User user, DefaultTableModel model, int tableRow) {
        JDialog dialog = new JDialog(parent, "Edit User - " + user.getAccountId(), true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(UIStyle.BG_PAGE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(UIStyle.BG_CARD);

        // Title
        JLabel titleLabel = new JLabel("Edit User Information", SwingConstants.CENTER);
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(UIStyle.PRIMARY);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIStyle.BG_CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        JLabel userIdLabel = new JLabel("User ID: " + user.getUserId());
        userIdLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(userIdLabel, gbc);

        JLabel accountLabel = new JLabel("Account ID: " + user.getAccountId());
        accountLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridy = 1;
        formPanel.add(accountLabel, gbc);

        JLabel roleLabel = new JLabel("Role: " + user.getRole());
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridy = 2;
        formPanel.add(roleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 3;
        JLabel fullNameLbl = new JLabel("Full Name:");
        formPanel.add(fullNameLbl, gbc);
        JTextField fullNameField = UIStyle.createTextField(20);
        fullNameField.setText(user.getFullName() != null ? user.getFullName() : "");
        gbc.gridx = 1;
        formPanel.add(fullNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        JLabel emailLbl = new JLabel("Email:");
        formPanel.add(emailLbl, gbc);
        JTextField emailField = UIStyle.createTextField(20);
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        JLabel phoneLbl = new JLabel("Phone:");
        formPanel.add(phoneLbl, gbc);
        JTextField phoneField = UIStyle.createTextField(20);
        phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        boolean isMO = user.getRole() == Role.MO;
        JLabel majorLbl = new JLabel(isMO ? "Department:" : "Major/Dept:");
        formPanel.add(majorLbl, gbc);
        JTextField majorField = UIStyle.createTextField(20);
        // MO: use department; TA/others: use major, fallback to department
        majorField.setText(isMO
            ? (user.getDepartment() != null ? user.getDepartment() : "")
            : (user.getMajor() != null ? user.getMajor() : (user.getDepartment() != null ? user.getDepartment() : "")));
        gbc.gridx = 1;
        formPanel.add(majorField, gbc);

        int nextRow = 7;
        JTextField skillsField = null;
        if (!isMO) {
            // Skills field only for TA (not relevant for MO)
            gbc.gridx = 0; gbc.gridy = nextRow;
            JLabel skillsLbl = new JLabel("Skills:");
            formPanel.add(skillsLbl, gbc);
            skillsField = UIStyle.createTextField(20);
            skillsField.setText(user.getSkills() != null ? user.getSkills() : "");
            gbc.gridx = 1;
            formPanel.add(skillsField, gbc);
            nextRow++;
        }

        // Password reset section
        gbc.gridx = 0; gbc.gridy = nextRow; gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        formPanel.add(sep, gbc);

        gbc.gridx = 0; gbc.gridy = nextRow + 1;
        JLabel pwdLbl = new JLabel("New Password (leave empty to keep current):");
        pwdLbl.setFont(new Font("Arial", Font.BOLD, 11));
        formPanel.add(pwdLbl, gbc);

        gbc.gridx = 0; gbc.gridy = nextRow + 2; gbc.gridwidth = 2;
        JPasswordField passwordField = UIStyle.createPasswordField(20);
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = nextRow + 3; gbc.gridwidth = 2;
        JLabel pwdHint = new JLabel("Password must contain uppercase, lowercase, digit, min 8 chars");
        pwdHint.setFont(new Font("Arial", Font.ITALIC, 10));
        pwdHint.setForeground(new Color(100, 100, 100));
        formPanel.add(pwdHint, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        JButton saveBtn = UIStyle.createPrimaryButton("Save Changes");
        JButton cancelBtn = UIStyle.createSecondaryButton("Cancel");

        final JTextField finalSkillsField = skillsField;
        saveBtn.addActionListener(e -> {
            try {
                String fullName = fullNameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String majorDept = majorField.getText().trim();
                String skills = (finalSkillsField != null) ? finalSkillsField.getText().trim() : "";
                String newPassword = new String(passwordField.getPassword());

                // MO: field goes to department; TA: field goes to major, also set department
                context.getAdminService().updateUserInfo(token, user.getUserId(), fullName, email, phone,
                    isMO ? "" : majorDept, isMO ? majorDept : majorDept, skills);

                if (!newPassword.isEmpty()) {
                    context.getAdminService().resetPassword(token, user.getUserId(), newPassword);
                    JOptionPane.showMessageDialog(dialog, "User information and password updated successfully!");
                } else {
                    JOptionPane.showMessageDialog(dialog, "User information updated successfully!");
                }

                // Update in-memory user object (same reference as in allUsers list)
                user.setFullName(fullName);
                user.setEmail(email);
                user.setPhone(phone);
                if (isMO) {
                    user.setDepartment(majorDept);
                } else {
                    user.setMajor(majorDept);
                    user.setDepartment(majorDept);
                }
                if (skills != null) user.setSkills(skills);

                // Refresh all affected columns in the table
                if (model != null && tableRow >= 0) {
                    model.setValueAt(fullName, tableRow, 2);
                    model.setValueAt(email, tableRow, 4);
                    model.setValueAt(phone, tableRow, 5);
                    model.setValueAt(majorDept, tableRow, 6);
                }

                dialog.dispose();
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    public static void showWorkDialog(JFrame parent, RecruitmentSystemContext context, String token, User user) {
        if (token == null) {
            JOptionPane.showMessageDialog(parent, "Please login as ADMIN first.");
            return;
        }

        try {
            Role role = user.getRole();
            String title;
            DefaultTableModel model;
            List<String[]> data;
            Color themeColor;
            String summaryHtml;

            if (role == Role.TA) {
                title = "TA Work Detail";
                themeColor = new Color(39, 174, 96);
                model = new DefaultTableModel(
                    new Object[]{"Position ID", "Job Title", "Work Location", "Job Type", "Application Status"}, 0) {
                    @Override public boolean isCellEditable(int row, int column) { return false; }
                };
                data = context.getAdminService().getTaWorkDetail(token, user.getUserId());
                for (String[] row : data) {
                    model.addRow(row);
                }
                int approvedCount = 0;
                for (String[] row : data) {
                    if ("APPROVED".equals(row[4]) || "HIRED".equals(row[4]) || "OFFERED".equals(row[4])) approvedCount++;
                }
                summaryHtml = "<html><b style='color:#27ae60'>TA</b> | "
                    + data.size() + " assigned position(s) | "
                    + approvedCount + " active</html>";
            } else if (role == Role.MO) {
                title = "MO Work Detail";
                themeColor = new Color(52, 120, 220);
                model = new DefaultTableModel(
                    new Object[]{"Position ID", "Job Title", "Status", "Headcount", "Deadline", "Location"}, 0) {
                    @Override public boolean isCellEditable(int row, int column) { return false; }
                };
                data = context.getAdminService().getMoWorkDetail(token, user.getUserId());
                for (String[] row : data) {
                    model.addRow(row);
                }
                int openCount = 0;
                for (String[] row : data) {
                    if ("OPEN".equals(row[2])) openCount++;
                }
                summaryHtml = "<html><b style='color:#3478DC'>MO</b> | "
                    + data.size() + " published position(s) | "
                    + openCount + " open</html>";
            } else {
                JOptionPane.showMessageDialog(parent, "No work data available for this role.", "Show Work", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No work records found for " + user.getFullName() + ".",
                    "Show Work", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // ---- Build UI ----
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setBackground(UIStyle.BG_PAGE);

            // Header with user info + stats
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(themeColor);
            headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

            JLabel nameLabel = new JLabel(user.getFullName());
            nameLabel.setFont(UIStyle.FONT_TITLE.deriveFont(18f));
            nameLabel.setForeground(Color.WHITE);
            headerPanel.add(nameLabel, BorderLayout.NORTH);

            JLabel roleAndStats = new JLabel(summaryHtml);
            roleAndStats.setFont(UIStyle.FONT_BODY);
            roleAndStats.setForeground(new Color(255, 255, 255, 220));
            headerPanel.add(roleAndStats, BorderLayout.SOUTH);

            panel.add(headerPanel, BorderLayout.NORTH);

            // Table
            JTable table = UIStyle.createStyledTable(model);
            JScrollPane scrollPane = UIStyle.wrapTableInScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(800, 380));

            JOptionPane.showMessageDialog(parent, panel, title + " - " + user.getFullName(), JOptionPane.PLAIN_MESSAGE);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showPollResultsDialog(JFrame parent) {
        TA_Recruitment_software.forum.PollRepository pollRepo = new TA_Recruitment_software.forum.PollRepository();
        java.util.Map<String, String> votes = pollRepo.loadVotes();
        if (votes.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No votes have been cast yet for the Poll of the Week.");
            return;
        }

        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        for (String subject : votes.values()) {
            counts.put(subject, counts.getOrDefault(subject, 0) + 1);
        }

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Subject", "Votes Cast"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (java.util.Map.Entry<String, Integer> entry : counts.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Poll of the Week: Which subject needs more TAs?"), BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(parent, panel, "Poll Results", JOptionPane.PLAIN_MESSAGE);
    }
}
