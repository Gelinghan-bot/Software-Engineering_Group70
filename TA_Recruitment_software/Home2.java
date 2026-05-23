package TA_Recruitment_software;

import TA_Recruitment_software.admin_system.foundation.UIStyle;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Home2 extends JFrame {
    private final RecruitmentSystemContext context;
    private String currentToken;
    private TA_Recruitment_software.admin_system.model.Role currentRole;

    private JLabel sessionLabel = new JLabel("");
    private BackgroundPanel bgPanel;
    private JButton aiDiagnosisHomeButton;

    private JComboBox<String> gradeCombo;
    private JComboBox<String> majorCombo;
    private JComboBox<String> categoryCombo;
    private JButton searchBtn;
    private JTextField courseNameField;
    private JPanel searchPanel;
    private JPanel aiPublishPanel;
    private JButton aiPublishBtn;
    private JLabel moInstructionLabel;

    public Home2() {
        UIStyle.setupGlobalStyle();
        this.context = new RecruitmentSystemContext();

        setTitle("JobHere - Unified Platform");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main Background Panel
        bgPanel = new BackgroundPanel("HomeBackGround.png"); 
        bgPanel.setLayout(new GridBagLayout());
        
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Subtitle text
        JLabel subtitleLabel = new JLabel("BUPT International School");
        subtitleLabel.setFont(UIStyle.FONT_TITLE);
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Main title text
        JLabel titleLabel = new JLabel("FIND OUR Teaching Assistants JOBS");
        titleLabel.setFont(UIStyle.FONT_HEADING.deriveFont(48f));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Search Bar Panel
        searchPanel = new JPanel();
        searchPanel.setOpaque(false);
        searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));

        String[] grades = {"All Grades", "Year1", "Year2", "Year3", "Year4"};
        gradeCombo = new JComboBox<>(grades);
        gradeCombo.setPreferredSize(new Dimension(180, 45));
        gradeCombo.setBackground(Color.WHITE);

        String[] majors = {"All majors", "IOT", "EE", "IST", "TEM"};
        majorCombo = new JComboBox<>(majors);
        majorCombo.setPreferredSize(new Dimension(180, 45));
        majorCombo.setBackground(Color.WHITE);

        String[] categories = {"All Categories", "Daily attendance", "grading", "invigilation", "Assess", "and other tasks"};
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setPreferredSize(new Dimension(180, 45));
        categoryCombo.setBackground(Color.WHITE);

        searchBtn = new JButton("SEARCH");
        searchBtn.setBackground(UIStyle.PRIMARY);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setOpaque(true);
        searchBtn.setFocusPainted(false);
        searchBtn.setBorderPainted(false);
        searchBtn.setFont(UIStyle.FONT_BUTTON);
        searchBtn.setPreferredSize(new Dimension(130, 45));
        searchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                searchBtn.setBackground(UIStyle.PRIMARY_DARK);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                searchBtn.setBackground(UIStyle.PRIMARY);
            }
        });
        
        courseNameField = UIStyle.createTextField(20);
        courseNameField.setText("Course Name (e.g. Intro to Java)");
        courseNameField.setPreferredSize(new Dimension(200, 45));
        courseNameField.setForeground(UIStyle.TEXT_HINT);
        courseNameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (courseNameField.getText().equals("Course Name (e.g. Intro to Java)")) {
                    courseNameField.setText("");
                    courseNameField.setForeground(UIStyle.TEXT);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (courseNameField.getText().isEmpty()) {
                    courseNameField.setForeground(UIStyle.TEXT_HINT);
                    courseNameField.setText("Course Name (e.g. Intro to Java)");
                }
            }
        });

        searchPanel.add(gradeCombo);
        searchPanel.add(majorCombo);
        searchPanel.add(categoryCombo);
        searchPanel.add(searchBtn); // added conditionally later

        aiPublishPanel = new JPanel();
        aiPublishPanel.setOpaque(false);
        aiPublishPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
        
        aiPublishBtn = new JButton("AI-Autofill Publish");
        aiPublishBtn.setBackground(new Color(108, 92, 231)); 
        aiPublishBtn.setForeground(Color.WHITE);
        aiPublishBtn.setOpaque(true);
        aiPublishBtn.setFocusPainted(false);
        aiPublishBtn.setBorderPainted(false);
        aiPublishBtn.setFont(new Font("Arial", Font.BOLD, 14));
        aiPublishBtn.setPreferredSize(new Dimension(170, 45));
        
        aiPublishPanel.add(aiPublishBtn);
        aiPublishPanel.setVisible(false);

        moInstructionLabel = new JLabel("Quickly Post your Job:");
        moInstructionLabel.setFont(new Font("Arial", Font.BOLD, 26));
        moInstructionLabel.setForeground(new Color(0x081236));
        moInstructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        moInstructionLabel.setVisible(false);

        // Add to content panel
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(50)); // increased strut to move components down
        contentPanel.add(moInstructionLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(searchPanel);
        contentPanel.add(aiPublishPanel);
        contentPanel.add(Box.createVerticalStrut(16));
        aiDiagnosisHomeButton = TA_Recruitment_software.ta_jobs.TAJobsUI.createAiDiagnosisHomeButton(this, context, () -> currentToken, () -> currentRole);
        contentPanel.add(aiDiagnosisHomeButton);

        bgPanel.add(contentPanel);
        
        add(bgPanel, BorderLayout.CENTER);
        refreshHeaderPanel();
    }

    private void refreshHeaderPanel() {
        BorderLayout layout = (BorderLayout) getContentPane().getLayout();
        Component top = layout.getLayoutComponent(BorderLayout.NORTH);
        if (top != null) {
            getContentPane().remove(top);
        }
        JPanel headerPanel = createHeaderPanel();
        getContentPane().add(headerPanel, BorderLayout.NORTH);
        TA_Recruitment_software.ta_jobs.TAJobsUI.updateAiDiagnosisHomeButton(aiDiagnosisHomeButton, currentRole, currentToken);
        
        // When not logged in, hide search and AI controls
        if (currentToken == null || currentRole == null) {
            searchPanel.setVisible(false);
            aiPublishPanel.setVisible(false);
            moInstructionLabel.setVisible(false);
            aiDiagnosisHomeButton.setVisible(false);
            revalidate();
            repaint();
            return;
        }
        searchPanel.setVisible(true);
        
        // Update Search Panel UI dynamically based on Role
        for (java.awt.event.ActionListener al : searchBtn.getActionListeners()) {
            searchBtn.removeActionListener(al);
        }
        for (java.awt.event.ActionListener al : aiPublishBtn.getActionListeners()) {
            aiPublishBtn.removeActionListener(al);
        }

        if (currentRole == TA_Recruitment_software.admin_system.model.Role.MO) {
            if ("All Categories".equals(categoryCombo.getItemAt(0))) {
                categoryCombo.removeItemAt(0);
                categoryCombo.insertItemAt("Job Type", 0);
                categoryCombo.setSelectedIndex(0);
            }
            moInstructionLabel.setVisible(true);
            searchPanel.remove(searchBtn);
            if (courseNameField.getParent() == null) {
                searchPanel.add(courseNameField);
            }
            aiPublishPanel.setVisible(true);

            aiPublishBtn.addActionListener(e -> {
                String selectedGrade = (String) gradeCombo.getSelectedItem();
                String selectedMajor = (String) majorCombo.getSelectedItem();
                String selectedCategory = (String) categoryCombo.getSelectedItem();
                String courseName = courseNameField.getText().equals("Course Name (e.g. Intro to Java)") ? "" : courseNameField.getText();
                
                Component center = ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
                TA_Recruitment_software.mo_publish.JobPublishPanel publishPanel = 
                        new TA_Recruitment_software.mo_publish.JobPublishPanel(this, context, currentToken, center);
                
                getContentPane().remove(center);
                getContentPane().add(publishPanel, BorderLayout.CENTER);
                revalidate();
                repaint();
                
                publishPanel.setInitialDataAndTriggerAI(selectedGrade, selectedMajor, selectedCategory, courseName);
            });
        } else {
            if ("Job Type".equals(categoryCombo.getItemAt(0))) {
                categoryCombo.removeItemAt(0);
                categoryCombo.insertItemAt("All Categories", 0);
                categoryCombo.setSelectedIndex(0);
            }
            moInstructionLabel.setVisible(false);
            searchPanel.remove(courseNameField);
            if (searchBtn.getParent() == null) {
                searchPanel.add(searchBtn);
            }
            aiPublishPanel.setVisible(false);

            searchBtn.addActionListener(e -> {
                String selectedGrade = (String) gradeCombo.getSelectedItem();
                String selectedMajor = (String) majorCombo.getSelectedItem();
                String selectedCategory = (String) categoryCombo.getSelectedItem();
                TA_Recruitment_software.ta_jobs.TAJobsUI.showFilteredJobs(this, context, currentToken, selectedGrade, selectedMajor, selectedCategory);
            });
        }

        searchPanel.revalidate();
        searchPanel.repaint();
        revalidate();
        repaint();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIStyle.BG_CARD);
        headerPanel.setPreferredSize(new Dimension(1200, 72));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 40, 8, 40));
        headerPanel.setBorder(new javax.swing.border.CompoundBorder(
            new javax.swing.border.MatteBorder(0, 0, 1, 0, UIStyle.BORDER),
            BorderFactory.createEmptyBorder(8, 40, 8, 40)
        ));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        logoPanel.setBackground(UIStyle.BG_CARD);
        JLabel logoJob = new JLabel("Job");
        logoJob.setFont(UIStyle.FONT_HEADING.deriveFont(26f));
        logoJob.setForeground(UIStyle.PRIMARY);
        JLabel logoHere = new JLabel("Here");
        logoHere.setFont(UIStyle.FONT_HEADING.deriveFont(26f));
        logoHere.setForeground(UIStyle.TEXT_PRIMARY);
        logoPanel.add(logoJob);
        logoPanel.add(logoHere);

        // Navigation Links
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 18));
        navPanel.setBackground(UIStyle.BG_CARD);
        
        String[] links;
        // When not logged in, only show HOME
        if (currentToken == null || currentRole == null) {
            links = new String[]{"HOME"};
        } else if (currentRole == TA_Recruitment_software.admin_system.model.Role.TA || currentRole == TA_Recruitment_software.admin_system.model.Role.MO) {
            links = new String[]{"HOME", "JOBS", "FORUM", "CONTACT", "PERSONAL"};
        } else if (currentRole == TA_Recruitment_software.admin_system.model.Role.ADMIN) {
            links = new String[]{"HOME", "USERS", "JOBS", "FORUM", "WORKLOAD", "SETTINGS"};
        } else {
            links = new String[]{"HOME", "JOBS", "FORUM", "CONTACT", "ADMIN", "PERSONAL"};
        }

        for (String link : links) {
            Map<String, Runnable> subItems = new LinkedHashMap<>();
            Runnable mainAction = null;
            if (link.equals("HOME")) {
                mainAction = this::showHomeView;
            } else if (link.equals("FORUM")) {
                if (currentRole == TA_Recruitment_software.admin_system.model.Role.ADMIN) {
                    subItems.put("Forum Area", () -> TA_Recruitment_software.forum.ForumUI.showForum(this, context, currentToken));
                    subItems.put("View Poll Results", () -> TA_Recruitment_software.admin_system.AdminUI.showPollResultsDialog(this));
                } else {
                    mainAction = () -> TA_Recruitment_software.forum.ForumUI.showForum(this, context, currentToken);
                }
            } else if (link.equals("USERS")) {
                subItems.put("Approve Registration", () -> TA_Recruitment_software.admin_system.AdminUI.showPendingUsersDialog(this, context, currentToken));
                subItems.put("Manage Users", () -> TA_Recruitment_software.admin_system.AdminUI.showManageUsersDialog(this, context, currentToken));
            } else if (link.equals("WORKLOAD")) {
                subItems.put("TA Workload", () -> TA_Recruitment_software.admin_system.AdminUI.showTaWorkloadDialog(this, context, currentToken));
            } else if (link.equals("SETTINGS")) {
                subItems.put("EXIT (Logout)", () -> logout());
            } else if (link.equals("JOBS")) {
                if (currentRole == TA_Recruitment_software.admin_system.model.Role.TA || currentRole == null) {
                    mainAction = () -> TA_Recruitment_software.ta_jobs.TAJobsUI.showAvailableJobs(this, context, currentToken);
                } else if (currentRole == TA_Recruitment_software.admin_system.model.Role.ADMIN) {
                    subItems.put("Post a Job", () -> TA_Recruitment_software.mo_publish.MoPublishUI.showPublishDialog(this, context, currentToken));
                    subItems.put("Manage Positions", () -> TA_Recruitment_software.mo_publish.MoPublishUI.showMyPositionsDialog(this, context, currentToken));
                    subItems.put("Review Applications", () -> TA_Recruitment_software.mo_review.MoReviewUI.showReviewDialog(this, context, currentToken));
                } else {
                    subItems.put("Job Details", () -> TA_Recruitment_software.ta_jobs.TAJobsUI.showAvailableJobs(this, context, currentToken));
                    subItems.put("Post a Job (MO)", () -> TA_Recruitment_software.mo_publish.MoPublishUI.showPublishDialog(this, context, currentToken));
                    subItems.put("Manage My Positions (MO)", () -> TA_Recruitment_software.mo_publish.MoPublishUI.showMyPositionsDialog(this, context, currentToken));
                    subItems.put("Review Applications (MO)", () -> TA_Recruitment_software.mo_review.MoReviewUI.showReviewDialog(this, context, currentToken));
                    subItems.put("View TA Profiles & CVs (MO)", () -> TA_Recruitment_software.profile.MoTaProfileViewUI.showDialog(this, context, currentToken));
                }
            } else if (link.equals("CONTACT")) {
                subItems.put("Contact with MO", () -> JOptionPane.showMessageDialog(this, "MO Contact Info: mo_support@bupt.edu"));
                subItems.put("Contact with us", () -> JOptionPane.showMessageDialog(this, "Admin Contact Info: support@jobhere.com"));
            } else if (link.equals("ADMIN")) {
                subItems.put("Approve Registration", () -> TA_Recruitment_software.admin_system.AdminUI.showPendingUsersDialog(this, context, currentToken));
                subItems.put("Manage Users", () -> TA_Recruitment_software.admin_system.AdminUI.showManageUsersDialog(this, context, currentToken));
                subItems.put("TA Workload", () -> TA_Recruitment_software.admin_system.AdminUI.showTaWorkloadDialog(this, context, currentToken));
                subItems.put("View Poll Results", () -> TA_Recruitment_software.admin_system.AdminUI.showPollResultsDialog(this));
                subItems.put("Post a Job", () -> TA_Recruitment_software.mo_publish.MoPublishUI.showPublishDialog(this, context, currentToken));
                subItems.put("Manage Positions", () -> TA_Recruitment_software.mo_publish.MoPublishUI.showMyPositionsDialog(this, context, currentToken));
                subItems.put("Review Applications", () -> TA_Recruitment_software.mo_review.MoReviewUI.showReviewDialog(this, context, currentToken));
            } else if (link.equals("PERSONAL")) {
                if (currentRole == null) {
                    mainAction = () -> JOptionPane.showMessageDialog(this, "Please login first to use personal features.", "Not Logged In", JOptionPane.WARNING_MESSAGE);
                } else if (currentRole == TA_Recruitment_software.admin_system.model.Role.MO) {
                    subItems.put("My Profile", () -> TA_Recruitment_software.profile.ProfileUI.showUpdateProfileDialog(this, context, currentToken));
                    subItems.put("EXIT (Logout)", () -> logout());
                } else {
                    subItems.put("TA Portal (Notifications / Limits / History / PDF)", () -> TA_Recruitment_software.auth.AuthUI.showTaPortal(this, context, currentToken));
                    subItems.put("My Profile", () -> TA_Recruitment_software.profile.ProfileUI.showUpdateProfileDialog(this, context, currentToken));
                    subItems.put("My Applications", () -> TA_Recruitment_software.ta_jobs.TAJobsUI.showMyApplications(this, context, currentToken));
                    subItems.put("EXIT (Logout)", () -> logout());
                }
            }
            NavLabel linkLabel = new NavLabel(link, subItems, mainAction);
            navPanel.add(linkLabel);
        }

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttonPanel.setBackground(UIStyle.BG_CARD);
        
        // Add Session Label
        if (currentToken != null && currentRole != null) {
            sessionLabel.setText("Logged in: " + currentRole.name());
        } else {
            sessionLabel.setText("");
        }
        
        sessionLabel.setFont(UIStyle.FONT_BODY_BOLD);
        sessionLabel.setForeground(UIStyle.TEXT_SECONDARY);
        sessionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        buttonPanel.add(sessionLabel);

        JButton registerBtn = UIStyle.createSecondaryButton("Register");
        registerBtn.setFont(UIStyle.FONT_BUTTON.deriveFont(14f));
        registerBtn.addActionListener(e -> TA_Recruitment_software.auth.AuthUI.showRegisterDialog(this, context));

        // Login / Switch Account button — text changes after login
        JButton loginBtn = UIStyle.createPrimaryButton(
            currentToken != null ? "Switch Account" : "Login"
        );
        loginBtn.setFont(UIStyle.FONT_BUTTON.deriveFont(13f));
        loginBtn.addActionListener(e -> {
            String[] result = TA_Recruitment_software.auth.AuthUI.showLoginDialog(this, context);
            if (result != null && result.length == 2) {
                this.currentToken = result[0];
                this.currentRole = TA_Recruitment_software.admin_system.model.Role.valueOf(result[1]);
                refreshHeaderPanel();
            }
        });

        buttonPanel.add(registerBtn);
        buttonPanel.add(loginBtn);

        headerPanel.add(logoPanel, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.CENTER);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    public void showHomeView() {
        Component center = ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (center != bgPanel) {
            if (center != null) {
                getContentPane().remove(center);
            }
            getContentPane().add(bgPanel, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }

    private void logout() {
        this.currentToken = null;
        this.currentRole = null;
        refreshHeaderPanel();
        showHomeView();
        JOptionPane.showMessageDialog(this, "Logged out successfully!");
    }

    // Custom panel to draw background image
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            // Try the given path first (works when CWD is TA_Recruitment_software, e.g. Windows RunApp.bat)
            // then try prefixed with the subdirectory (works when CWD is project root, e.g. Mac/PyCharm)
            String[] candidates = {imagePath, "TA_Recruitment_software/" + imagePath};
            for (String path : candidates) {
                File f = new File(path);
                if (f.exists()) {
                    try {
                        backgroundImage = ImageIO.read(f);
                        break;
                    } catch (IOException e) {
                        // try next candidate
                    }
                }
            }
            if (backgroundImage == null) {
                setBackground(new Color(113, 203, 202));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
            } else {
                g2d.setColor(new Color(113, 203, 202));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    // Custom button for animated hover effect
    class SlideButton extends JButton {
        private Color normalBg;
        private Color hoverBg;
        private Color normalFg;
        private Color hoverFg;
        private int fillWidth = 0;
        private Timer timer;
        private boolean isHovering = false;

        public SlideButton(String text, Color normalBg, Color hoverBg, Color normalFg, Color hoverFg) {
            super(text);
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.normalFg = normalFg;
            this.hoverFg = hoverFg;

            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            timer = new Timer(10, e -> {
                if (isHovering) {
                    if (fillWidth < getWidth()) {
                        fillWidth += 8; // animation sliding speed
                        if (fillWidth > getWidth()) fillWidth = getWidth();
                        repaint();
                    } else {
                        timer.stop();
                    }
                } else {
                    if (fillWidth > 0) {
                        fillWidth -= 8;
                        if (fillWidth < 0) fillWidth = 0;
                        repaint();
                    } else {
                        timer.stop();
                    }
                }
            });

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    isHovering = true;
                    timer.start();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    isHovering = false;
                    timer.start();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Draw normal background
            g2.setColor(normalBg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw slide over background
            g2.setColor(hoverBg);
            g2.fillRect(0, 0, fillWidth, getHeight());

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int stringWidth = fm.stringWidth(getText());
            int stringHeight = fm.getAscent();
            int x = (getWidth() - stringWidth) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + stringHeight;

            // Draw normal text for the un-filled area
            g2.setClip(fillWidth, 0, getWidth() - fillWidth, getHeight());
            g2.setColor(normalFg);
            g2.drawString(getText(), x, y);

            // Draw hover text over the filled area
            g2.setClip(0, 0, fillWidth, getHeight());
            g2.setColor(hoverFg);
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    // Custom label for Navigation with Hover and Popup menu
    class NavLabel extends JLabel {
        private JPopupMenu popup;
        private boolean isHovered = false;

        public NavLabel(String text, Map<String, Runnable> subItems, Runnable mainAction) {
            super(text);
            setFont(UIStyle.FONT_BODY_BOLD.deriveFont(15f));
            setForeground(UIStyle.TEXT_PRIMARY);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            // Provide bottom padding to leave visual space for the small green bar
            setBorder(BorderFactory.createEmptyBorder(5, 5, 12, 5)); 

            if (mainAction != null) {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        mainAction.run();
                    }
                });
            }

            if (subItems != null && !subItems.isEmpty()) {
                popup = new JPopupMenu();
                popup.setBackground(UIStyle.BG_CARD);
                popup.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER));
                
                for (Map.Entry<String, Runnable> entry : subItems.entrySet()) {
                    String sub = entry.getKey();
                    Runnable action = entry.getValue();
                    
                    JMenuItem item = new JMenuItem(sub);
                    item.setBackground(UIStyle.BG_CARD);
                    item.setFont(UIStyle.FONT_BODY);
                    // Tight margin — no icon-area gap, just even padding
                    item.setMargin(new Insets(6, 12, 6, 12));
                    item.setIconTextGap(0);
                    item.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    // Hover effect for submenu items
                    item.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseEntered(java.awt.event.MouseEvent e) {
                            item.setForeground(UIStyle.PRIMARY);
                        }
                        @Override
                        public void mouseExited(java.awt.event.MouseEvent e) {
                            item.setForeground(UIStyle.TEXT_PRIMARY);
                            // 关键修复：当鼠标离开子菜单项时，立刻触发范围检测
                            Timer timer = new Timer(150, evt -> checkMouseExit());
                            timer.setRepeats(false);
                            timer.start();
                        }
                    });
                    
                    if (action != null) {
                        item.addActionListener(e -> {
                            isHovered = false;
                            popup.setVisible(false);
                            repaint();
                            action.run();
                        });
                    }
                    popup.add(item);
                }
            }

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    isHovered = true;
                    repaint();
                    if (popup != null && !popup.isVisible()) {
                        // show popup horizontally centered below the label
                        popup.show(NavLabel.this, (getWidth() / 2) - 75, getHeight());
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    // Delay hiding to allow mouse to move to popup
                    Timer timer = new Timer(150, evt -> checkMouseExit());
                    timer.setRepeats(false);
                    timer.start();
                }
            });

            if (popup != null) {
                popup.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        Timer timer = new Timer(150, evt -> checkMouseExit());
                        timer.setRepeats(false);
                        timer.start();
                    }
                });
            }
        }

        private void checkMouseExit() {
            try {
                Point mousePos = MouseInfo.getPointerInfo().getLocation();
                boolean inLabel = false;
                boolean inPopup = false;

                if (isShowing()) {
                    Point labelLoc = getLocationOnScreen();
                    Rectangle labelRect = new Rectangle(labelLoc, getSize());
                    if (labelRect.contains(mousePos)) inLabel = true;
                }

                if (popup != null && popup.isShowing()) {
                    Point popupLoc = popup.getLocationOnScreen();
                    Rectangle popupRect = new Rectangle(popupLoc, popup.getSize());
                    if (popupRect.contains(mousePos)) inPopup = true;
                }

                if (!inLabel && !inPopup) {
                    isHovered = false;
                    if (popup != null) popup.setVisible(false);
                    repaint();
                }
            } catch (Exception ex) {
                // Ignore exceptions if component is hidden
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            boolean active = isHovered || (popup != null && popup.isVisible());
            setForeground(active ? new Color(39, 174, 96) : new Color(51, 51, 51));

            super.paintComponent(g);

            // Draw a small vertical green bar below the text when hovered
            if (active) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(39, 174, 96));
                int barW = 4;
                int barH = 10;
                int bx = (getWidth() - barW) / 2;
                int by = getHeight() - barH;
                g2.fillRect(bx, by, barW, barH);
            }
        }
    }

    public static void main(String[] args) {
        if (System.getProperty("os.name", "").toLowerCase().contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "JobHere");
            System.setProperty("apple.awt.application.appearance", "system");
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        }
        SwingUtilities.invokeLater(() -> {
            new Home2().setVisible(true);
        });
    }
}