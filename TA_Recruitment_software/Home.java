package TA_Recruitment_software;

import TA_Recruitment_software.admin_system.foundation.AppException;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.Role;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class Home extends JFrame {
    private final RecruitmentSystemContext context;

    private String moToken;

    private final JLabel sessionLabel = new JLabel("Not logged in");

    private final JTextField accountField = new JTextField(14);
    private final JPasswordField passwordField = new JPasswordField(14);

    private final JTextField courseField = new JTextField(20);
    private final JTextField deadlineField = new JTextField(20);
    private final JTextField workingHoursField = new JTextField(20);
    private final JTextArea jobDescriptionArea = new JTextArea(3, 20);
    private final JTextArea requirementsArea = new JTextArea(3, 20);

    private final JTextField newDeadlineField = new JTextField(12);

    private final JButton publishButton = new JButton("Publish Position");
    private final JButton refreshButton = new JButton("Refresh My Positions");
    private final JButton updateDeadlineButton = new JButton("Update Deadline");
    private final JButton closePositionButton = new JButton("Close Position");

    private final DefaultTableModel positionTableModel = new DefaultTableModel(
        new Object[] {"Position ID", "Course", "Deadline", "Hours", "Status"},
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable positionTable = new JTable(positionTableModel);

    public Home() {
        this.context = new RecruitmentSystemContext();

        setTitle("JobHere - MO Publish Module");
        setSize(1200, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        BackgroundPanel backgroundPanel = new BackgroundPanel("TA_Recruitment_software/HomeBackGround.png");
        backgroundPanel.setLayout(new GridBagLayout());
        backgroundPanel.add(createBodyPanel());
        add(backgroundPanel, BorderLayout.CENTER);

        bindActions();
        setModuleControlsEnabled(false);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(1200, 75));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        logoPanel.setBackground(Color.WHITE);
        JLabel logoJob = new JLabel("Job");
        logoJob.setFont(new Font("Arial", Font.BOLD, 28));
        logoJob.setForeground(new Color(39, 174, 96));
        JLabel logoHere = new JLabel("Here");
        logoHere.setFont(new Font("Arial", Font.BOLD, 28));
        logoHere.setForeground(new Color(51, 51, 51));
        logoPanel.add(logoJob);
        logoPanel.add(logoHere);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        navPanel.setBackground(Color.WHITE);
        navPanel.add(navLabel("MO PUBLISH HOME"));
        navPanel.add(navLabel("POST POSITION"));
        navPanel.add(navLabel("MY POSITIONS"));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 18));
        rightPanel.setBackground(Color.WHITE);
        JLabel roleBadge = new JLabel("Role: Module Organizer");
        roleBadge.setOpaque(true);
        roleBadge.setBackground(new Color(233, 247, 239));
        roleBadge.setForeground(new Color(24, 123, 72));
        roleBadge.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        sessionLabel.setFont(new Font("Arial", Font.BOLD, 13));
        sessionLabel.setForeground(new Color(65, 65, 65));

        rightPanel.add(roleBadge);
        rightPanel.add(sessionLabel);

        headerPanel.add(logoPanel, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.CENTER);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JLabel navLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 15));
        label.setForeground(new Color(51, 51, 51));
        return label;
    }

    private JPanel createBodyPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        wrapper.add(createLoginCard());
        wrapper.add(Box.createVerticalStrut(15));
        wrapper.add(createPublishCard());
        wrapper.add(Box.createVerticalStrut(15));
        wrapper.add(createPositionTableCard());

        return wrapper;
    }

    private JPanel createLoginCard() {
        JPanel card = baseCard();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 15));

        JLabel title = new JLabel("MO Login (approved account required)");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(new Color(39, 174, 96));

        JButton loginButton = new JButton("MO Login");
        loginButton.setBackground(new Color(39, 174, 96));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        loginButton.addActionListener(e -> doLogin());

        card.add(title);
        card.add(new JLabel("Account:"));
        card.add(accountField);
        card.add(new JLabel("Password:"));
        card.add(passwordField);
        card.add(loginButton);
        return card;
    }

    private JPanel createPublishCard() {
        JPanel card = baseCard();
        card.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("MO Publish Module");
        title.setFont(new Font("Arial", Font.BOLD, 17));
        title.setForeground(new Color(39, 174, 96));
        card.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.setOpaque(false);
        formPanel.add(new JLabel("Course Name:"));
        formPanel.add(courseField);
        formPanel.add(new JLabel("Deadline (YYYY-MM-DD):"));
        formPanel.add(deadlineField);
        formPanel.add(new JLabel("Working Hours:"));
        formPanel.add(workingHoursField);
        formPanel.add(new JLabel("Job Description:"));
        formPanel.add(new JScrollPane(jobDescriptionArea));
        formPanel.add(new JLabel("Requirements:"));
        formPanel.add(new JScrollPane(requirementsArea));

        card.add(formPanel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        actions.setOpaque(false);
        actions.add(publishButton);
        actions.add(refreshButton);
        actions.add(new JLabel("New deadline:"));
        actions.add(newDeadlineField);
        actions.add(updateDeadlineButton);
        actions.add(closePositionButton);

        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createPositionTableCard() {
        JPanel card = baseCard();
        card.setLayout(new BorderLayout(10, 8));

        JLabel title = new JLabel("My Published Positions (select one row for deadline update/close)");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(new Color(39, 174, 96));
        card.add(title, BorderLayout.NORTH);

        positionTable.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(positionTable);
        scrollPane.setPreferredSize(new Dimension(1060, 270));
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel baseCard() {
        JPanel card = new JPanel();
        card.setBackground(new Color(255, 255, 255, 230));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 225, 225)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return card;
    }

    private void bindActions() {
        publishButton.addActionListener(e -> publishPosition());
        refreshButton.addActionListener(e -> refreshMyPositions());
        updateDeadlineButton.addActionListener(e -> updateDeadline());
        closePositionButton.addActionListener(e -> closePosition());
    }

    private void doLogin() {
        try {
            String token = context.getAuthService().login(accountField.getText(), new String(passwordField.getPassword()));
            context.getSessionManager().requireRole(token, Role.MO);
            this.moToken = token;
            setModuleControlsEnabled(true);
            sessionLabel.setText("Logged in as MO");
            refreshMyPositions();
            JOptionPane.showMessageDialog(this, "MO login successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void publishPosition() {
        if (!requireLogin()) {
            return;
        }
        try {
            Position position = context.getMoPublishService().publishPosition(
                moToken,
                courseField.getText(),
                jobDescriptionArea.getText(),
                requirementsArea.getText(),
                deadlineField.getText(),
                workingHoursField.getText()
            );
            refreshMyPositions();
            JOptionPane.showMessageDialog(
                this,
                "Published successfully. Position ID: " + position.getPositionId(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Publish failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshMyPositions() {
        if (!requireLogin()) {
            return;
        }
        try {
            List<Position> positions = context.getMoPublishService().listMyPositions(moToken);
            positionTableModel.setRowCount(0);
            for (Position position : positions) {
                positionTableModel.addRow(new Object[] {
                    position.getPositionId(),
                    position.getCourseName(),
                    position.getDeadline(),
                    position.getWorkingHours(),
                    position.getStatus().name()
                });
            }
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Refresh failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDeadline() {
        if (!requireLogin()) {
            return;
        }
        String selectedPositionId = selectedPositionId();
        if (selectedPositionId == null) {
            JOptionPane.showMessageDialog(this, "Please select a position row first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            context.getMoPublishService().updateDeadline(moToken, selectedPositionId, newDeadlineField.getText());
            refreshMyPositions();
            JOptionPane.showMessageDialog(this, "Deadline updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void closePosition() {
        if (!requireLogin()) {
            return;
        }
        String selectedPositionId = selectedPositionId();
        if (selectedPositionId == null) {
            JOptionPane.showMessageDialog(this, "Please select a position row first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            context.getMoPublishService().closePosition(moToken, selectedPositionId);
            refreshMyPositions();
            JOptionPane.showMessageDialog(this, "Position closed.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Close failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String selectedPositionId() {
        int row = positionTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        Object value = positionTableModel.getValueAt(row, 0);
        return value == null ? null : value.toString();
    }

    private boolean requireLogin() {
        if (moToken == null || moToken.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please login as MO first.", "No session", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void setModuleControlsEnabled(boolean enabled) {
        courseField.setEnabled(enabled);
        deadlineField.setEnabled(enabled);
        workingHoursField.setEnabled(enabled);
        jobDescriptionArea.setEnabled(enabled);
        requirementsArea.setEnabled(enabled);
        newDeadlineField.setEnabled(enabled);
        publishButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
        updateDeadlineButton.setEnabled(enabled);
        closePositionButton.setEnabled(enabled);
    }

    private static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        private BackgroundPanel(String imagePath) {
            try {
                backgroundImage = ImageIO.read(new File(imagePath));
            } catch (IOException e) {
                setBackground(new Color(113, 203, 202));
            }
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(new Color(113, 203, 202));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Home().setVisible(true));
    }
}
