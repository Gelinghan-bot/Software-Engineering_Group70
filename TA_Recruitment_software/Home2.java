package TA_Recruitment_software;

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

    public Home2() {
        this.context = new RecruitmentSystemContext();

        setTitle("JobHere - Unified Platform");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top Navigation Bar
        refreshHeaderPanel();

        // Main Background Panel
        bgPanel = new BackgroundPanel("HomeBackGround.png"); 
        bgPanel.setLayout(new GridBagLayout());
        
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Subtitle text
        JLabel subtitleLabel = new JLabel("BUPT International School");
        subtitleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Main title text
        JLabel titleLabel = new JLabel("FIND OUR Teaching Assistants JOBS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Search Bar Panel
        JPanel searchPanel = new JPanel();
        searchPanel.setOpaque(false);
        searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));

        String[] grades = {"All Grades", "Year1", "Year2", "Year3", "Year4"};
        JComboBox<String> gradeCombo = new JComboBox<>(grades);
        gradeCombo.setPreferredSize(new Dimension(180, 45));
        gradeCombo.setBackground(Color.WHITE);

        String[] majors = {"All majors", "IOT", "EE", "IST", "TEM"};
        JComboBox<String> majorCombo = new JComboBox<>(majors);
        majorCombo.setPreferredSize(new Dimension(180, 45));
        majorCombo.setBackground(Color.WHITE);

        String[] categories = {"All Categories", "Daily attendance", "grading", "invigilation", "Assess", "and other tasks"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setPreferredSize(new Dimension(180, 45));
        categoryCombo.setBackground(Color.WHITE);

        JButton searchBtn = new JButton("SEARCH");
        searchBtn.setBackground(new Color(39, 174, 96));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setBorderPainted(false);
        searchBtn.setFont(new Font("Arial", Font.BOLD, 14));
        searchBtn.setPreferredSize(new Dimension(130, 45));
        
        searchBtn.addActionListener(e -> {
            String selectedGrade = (String) gradeCombo.getSelectedItem();
            String selectedMajor = (String) majorCombo.getSelectedItem();
            String selectedCategory = (String) categoryCombo.getSelectedItem();
            TA_Recruitment_software.ta_jobs.TAJobsUI.showFilteredJobs(this, context, currentToken, selectedGrade, selectedMajor, selectedCategory);
        });

        searchPanel.add(gradeCombo);
        searchPanel.add(majorCombo);
        searchPanel.add(categoryCombo);
        searchPanel.add(searchBtn);

        // Add to content panel
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(searchPanel);

        bgPanel.add(contentPanel);
        
        add(bgPanel, BorderLayout.CENTER);
    }

    private void refreshHeaderPanel() {
        BorderLayout layout = (BorderLayout) getContentPane().getLayout();
        Component top = layout.getLayoutComponent(BorderLayout.NORTH);
        if (top != null) {
            getContentPane().remove(top);
        }
        JPanel headerPanel = createHeaderPanel();
        getContentPane().add(headerPanel, BorderLayout.NORTH);
        revalidate();
        repaint();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(1200, 75));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15));
        logoPanel.setBackground(Color.WHITE);
        JLabel logoJob = new JLabel("Job");
        logoJob.setFont(new Font("Arial", Font.BOLD, 28));
        logoJob.setForeground(new Color(39, 174, 96));
        JLabel logoHere = new JLabel("Here");
        logoHere.setFont(new Font("Arial", Font.BOLD, 28));
        logoHere.setForeground(new Color(51, 51, 51));
        logoPanel.add(logoJob);
        logoPanel.add(logoHere);

        // Navigation Links
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        navPanel.setBackground(Color.WHITE);
        
        String[] links;
        if (currentRole == TA_Recruitment_software.admin_system.model.Role.TA || currentRole == TA_Recruitment_software.admin_system.model.Role.MO || currentRole == null) {
            links = new String[]{"HOME", "JOBS", "FORUM", "CONTACT", "PERSONAL"};
        } else if (currentRole == TA_Recruitment_software.admin_system.model.Role.ADMIN) {
            links = new String[]{"HOME", "USERS", "JOBS", "FORUM", "WORKLOAD", "SETTINGS"};
        } else {
            // Keep default/other roles unchanged for now
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
                subItems.put("Settings", () -> JOptionPane.showMessageDialog(this, "Settings feature coming soon."));
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
                    subItems.put("My Resume", () -> TA_Recruitment_software.profile.ProfileUI.showUpdateProfileDialog(this, context, currentToken));
                    subItems.put("Settings", () -> JOptionPane.showMessageDialog(this, "Settings feature coming soon."));
                    subItems.put("EXIT (Logout)", () -> logout());
                } else {
                    subItems.put("TA Portal (Notifications / Limits / History / PDF)", () -> TA_Recruitment_software.auth.AuthUI.showTaPortal(this, context, currentToken));
                    subItems.put("My Resume (Update Profile)", () -> TA_Recruitment_software.profile.ProfileUI.showUpdateProfileDialog(this, context, currentToken));
                    subItems.put("My Applications", () -> TA_Recruitment_software.ta_jobs.TAJobsUI.showMyApplications(this, context, currentToken));
                    subItems.put("AI Recommendation", () -> JOptionPane.showMessageDialog(this, "Recommendation records feature coming soon."));
                    subItems.put("Settings", () -> JOptionPane.showMessageDialog(this, "Settings feature coming soon."));
                    subItems.put("EXIT (Logout)", () -> logout());
                }
            }
            NavLabel linkLabel = new NavLabel(link, subItems, mainAction);
            navPanel.add(linkLabel);
        }

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        // Add Session Label
        if (currentToken != null && currentRole != null) {
            sessionLabel.setText("Logged in: " + currentRole.name());
        } else {
            sessionLabel.setText("");
        }
        
        sessionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        sessionLabel.setForeground(new Color(65, 65, 65));
        sessionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        buttonPanel.add(sessionLabel);

        SlideButton registerBtn = new SlideButton(
            "Register",
            new Color(224, 224, 224), // normal bg
            new Color(39, 174, 96),   // hover bg (green)
            new Color(51, 51, 51),    // normal text
            Color.WHITE               // hover text
        );
        registerBtn.setPreferredSize(new Dimension(100, 40));
        registerBtn.setFont(new Font("Arial", Font.BOLD, 15));
        registerBtn.addActionListener(e -> TA_Recruitment_software.auth.AuthUI.showRegisterDialog(this, context));

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(39, 174, 96));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setPreferredSize(new Dimension(95, 40));
        loginBtn.setFont(new Font("Arial", Font.BOLD, 15));
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
            try {
                backgroundImage = ImageIO.read(new File(imagePath));
            } catch (IOException e) {
                // If image not found, we use a fallback gradient or solid color
                setBackground(new Color(113, 203, 202));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
            } else {
                // Draw a fallback semi-transparent color if image is not loaded
                Graphics2D g2d = (Graphics2D) g;
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
            setFont(new Font("Arial", Font.BOLD, 16));
            setForeground(new Color(51, 51, 51));
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
                popup.setBackground(Color.WHITE);
                popup.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224)));
                
                for (Map.Entry<String, Runnable> entry : subItems.entrySet()) {
                    String sub = entry.getKey();
                    Runnable action = entry.getValue();
                    
                    JMenuItem item = new JMenuItem(sub);
                    item.setBackground(Color.WHITE);
                    item.setFont(new Font("Arial", Font.PLAIN, 14));
                    
                    // 动态计算宽度：最少150，长文本则自适应扩展宽度，同时预留左右边距
                    int prefWidth = item.getPreferredSize().width + 30;
                    item.setPreferredSize(new Dimension(Math.max(150, prefWidth), 40));
                    
                    item.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    item.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

                    // Hover effect for submenu items
                    item.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseEntered(java.awt.event.MouseEvent e) {
                            item.setForeground(new Color(39, 174, 96));
                        }
                        @Override
                        public void mouseExited(java.awt.event.MouseEvent e) {
                            item.setForeground(Color.BLACK);
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
        SwingUtilities.invokeLater(() -> {
            new Home2().setVisible(true);
        });
    }
}