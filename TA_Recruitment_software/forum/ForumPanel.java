package TA_Recruitment_software.forum;

import TA_Recruitment_software.RecruitmentSystemContext;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.util.List;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.UserRepository;

public class ForumPanel extends JPanel {
    private JFrame parent;
    private RecruitmentSystemContext context;
    private String token;
    private Component previousCenterComponent;
    private TopicRepository topicRepository;
    private JPanel leftPanel; // Declare at class level so we can update it

    public ForumPanel(JFrame parent, RecruitmentSystemContext context, String token, Component prev) {
        this.parent = parent;
        this.context = context;
        this.token = token;
        this.previousCenterComponent = prev;
        this.topicRepository = new TopicRepository();
        
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // Top Toolbar
        add(createToolbar(), BorderLayout.NORTH);

        // Main content area wrapper to simulate max width bounds and margins
        JPanel contentWrapper = new JPanel(new BorderLayout(20, 20));
        contentWrapper.setBackground(new Color(245, 245, 245));
        contentWrapper.setBorder(new EmptyBorder(25, 40, 25, 40));

        // Left Area: Topics List
        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(245, 245, 245));

        loadTopics();
        
        // Wrap Left Area in JScrollPane
        JScrollPane scrollLeft = new JScrollPane(leftPanel);
        scrollLeft.setBorder(null);
        scrollLeft.setBackground(new Color(245, 245, 245));
        scrollLeft.getVerticalScrollBar().setUnitIncrement(16);
        contentWrapper.add(scrollLeft, BorderLayout.CENTER);

        // Right Area: Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(245, 245, 245));
        sidebar.setPreferredSize(new Dimension(300, 0)); // Fixed width for sidebar

        sidebar.add(createCategoriesCard());
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(createPollCard());

        contentWrapper.add(sidebar, BorderLayout.EAST);

        add(contentWrapper, BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel tb = new JPanel(new BorderLayout());
        tb.setBackground(Color.WHITE);
        tb.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1), 
            new EmptyBorder(12, 40, 12, 40)
        ));

        // Left Title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        left.setBackground(Color.WHITE);
        JLabel title = new JLabel("System Forum");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(39, 174, 96));
        left.add(title);

        // Right Actions (Search & New Topic)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setBackground(Color.WHITE);

        JTextField searchBox = new JTextField("Search Topics");
        searchBox.setPreferredSize(new Dimension(250, 36));
        searchBox.setFont(new Font("Arial", Font.PLAIN, 14));
        searchBox.setForeground(Color.GRAY);
        searchBox.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(0, 10, 0, 10)
        ));

        JButton newTopicBtn = new JButton("Start New Topic");
        newTopicBtn.setBackground(new Color(39, 174, 96));
        newTopicBtn.setForeground(Color.WHITE);
        newTopicBtn.setFont(new Font("Arial", Font.BOLD, 13));
        newTopicBtn.setPreferredSize(new Dimension(140, 36));
        newTopicBtn.setFocusPainted(false);
        newTopicBtn.setBorderPainted(false);
        newTopicBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newTopicBtn.addActionListener(e -> {
            if (token == null) {
                JOptionPane.showMessageDialog(this, "Please login to post a topic.", "Wait", JOptionPane.WARNING_MESSAGE);
                return;
            }
            showNewTopicDialog();
        });

        right.add(searchBox);
        right.add(newTopicBtn);

        tb.add(left, BorderLayout.WEST);
        tb.add(right, BorderLayout.EAST);
        return tb;
    }

    private void loadTopics() {
        leftPanel.removeAll();
        
        List<Topic> topics = topicRepository.getAllTopics();
        
        // Add default mock topics if empty for show
        if (topics.isEmpty()) {
            topicRepository.addTopic(new Topic("Alice Smith", "10 Tips Unaware For New Teaching Assistants", "Today, we're looking at particularly interesting stories from our experienced TAs. The university added a new location-based feature on Wednesday that uses classroom maps... Helping students interact effectively in practical sessions."));
            topicRepository.addTopic(new Topic("Bob Johnson", "Typography helps you engage your audience effectively", "Typography helps you engage your audience and establish a distinct, unique personality on your presentation slides. Knowing how to use fonts to build character in your design is a powerful skill, and exploring the history and use of typefaces..."));
            topicRepository.addTopic(new Topic("Charlie Dan", "How to structure the final grading sheet", "When dealing with over 100 students, the grading sheet can become a mess. I found that grouping by student ID and creating an automated script in Python significantly reduces workload and minimizes mistakes. Here is how you can set it up..."));
            topics = topicRepository.getAllTopics();
        }

        // Colors to cycle through for avatars
        Color[] colors = {new Color(41, 128, 185), new Color(192, 57, 43), new Color(39, 174, 96), new Color(142, 68, 173), new Color(243, 156, 18)};
        int colorIdx = 0;

        for (int i = topics.size() - 1; i >= 0; i--) { // Reverse order to show newest first
            Topic t = topics.get(i);
            leftPanel.add(createTopicCard(
                t.getAuthorName(), colors[colorIdx % colors.length], 
                t.getTitle(), t.getContent(), 
                String.valueOf(t.getLikes()), String.valueOf(t.getComments()), t.getDateStr()
            ));
            leftPanel.add(Box.createVerticalStrut(15));
            colorIdx++;
        }

        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(createPaginationPanel());

        leftPanel.revalidate();
        leftPanel.repaint();
    }

    private void showNewTopicDialog() {
        JDialog dialog = new JDialog(parent, "Start New Topic", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        JTextField titleField = new JTextField();
        titleField.setBorder(BorderFactory.createTitledBorder("Topic Title"));
        titleField.setFont(new Font("Arial", Font.BOLD, 14));

        JTextArea contentArea = new JTextArea();
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Topic Content"));

        formPanel.add(titleField, BorderLayout.NORTH);
        formPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton postBtn = new JButton("Post Topic");
        postBtn.setBackground(new Color(39, 174, 96));
        postBtn.setForeground(Color.WHITE);
        postBtn.setFocusPainted(false);
        postBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            if (title.isEmpty() || content.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Title and Content cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String username = "Anonymous";
            try {
                TA_Recruitment_software.auth.SessionContext sessionCtx = context.getSessionManager().requireSession(token);
                username = sessionCtx.getFullName();
                if (username == null || username.trim().isEmpty()) {
                    username = sessionCtx.getAccountId();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Session error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Topic newTopic = new Topic(username, title, content);
            topicRepository.addTopic(newTopic);
            
            loadTopics(); // Refresh UI
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Topic posted successfully!");
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(postBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createTopicCard(String authorName, Color avatarColor, String titleStr, String bodyStr, String likes, String comments, String dateStr) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(20, 20, 15, 20) // Top, Left, Bottom, Right Inner Padding
        ));

        // Left Side: Avatar Column
        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setBackground(Color.WHITE);
        
        // Custom component mapping username initials to a colored circle profile
        AvatarIcon icon = new AvatarIcon(authorName, avatarColor);
        avatarPanel.add(icon, BorderLayout.NORTH);

        // Middle Content Area
        JPanel contentArea = new JPanel(new BorderLayout(0, 8));
        contentArea.setBackground(Color.WHITE);

        JLabel title = new JLabel(titleStr);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(new Color(51, 51, 51));
        
        JTextArea body = new JTextArea(bodyStr);
        body.setWrapStyleWord(true);
        body.setLineWrap(true);
        body.setEditable(false);
        body.setFocusable(false);
        body.setFont(new Font("Arial", Font.PLAIN, 13));
        body.setForeground(new Color(110, 110, 110));
        body.setBorder(null);

        contentArea.add(title, BorderLayout.NORTH);
        contentArea.add(body, BorderLayout.CENTER);

        // Bottom Footer (Likes, Comments, Date)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel likesLbl = new JLabel("👍 " + likes);
        likesLbl.setFont(new Font("Arial", Font.BOLD, 12));
        likesLbl.setForeground(new Color(39, 174, 96));
        likesLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel commentsLbl = new JLabel("💬 " + comments);
        commentsLbl.setFont(new Font("Arial", Font.BOLD, 12));
        commentsLbl.setForeground(new Color(150, 150, 150));
        commentsLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel dateLbl = new JLabel("🕒 Posted on: " + dateStr);
        dateLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLbl.setForeground(new Color(170, 170, 170));

        footer.add(likesLbl);
        footer.add(commentsLbl);
        footer.add(dateLbl);

        contentArea.add(footer, BorderLayout.SOUTH);

        card.add(avatarPanel, BorderLayout.WEST);
        card.add(contentArea, BorderLayout.CENTER);

        // Wrap to stop vertical stretching
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(new Color(245, 245, 245));
        wrap.add(card, BorderLayout.NORTH);
        
        return wrap;
    }

    private JPanel createPaginationPanel() {
        JPanel pagWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pagWrap.setBackground(new Color(245, 245, 245));
        
        String[] controls = {"<", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", ">"};
        for (String c : controls) {
            JButton btn = new JButton(c);
            btn.setFont(new Font("Arial", Font.PLAIN, 12));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setPreferredSize(new Dimension(35, 30));
            btn.setMargin(new Insets(0, 0, 0, 0));
            
            if ("7".equals(c)) { // Mock current active page
                btn.setBackground(new Color(39, 174, 96));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(new Color(100, 100, 100));
            }
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            pagWrap.add(btn);
        }
        return pagWrap;
    }

    private JPanel createCategoriesCard() {
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setBackground(new Color(245, 245, 245));
        
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(15, 20, 20, 20)
        ));

        JLabel title = new JLabel("Categories");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(new Color(51, 51, 51));
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(title);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        card.add(headerPanel);

        // Mock Categories similar to screenshot mapped to project
        card.add(createCategoryItem("General Discussion", "20"));
        card.add(createCategoryItem("Course Assignments", "10"));
        card.add(createCategoryItem("Tool Resources & Links", "30"));
        card.add(createCategoryItem("Looking for Co-TAs", "35"));
        card.add(createCategoryItem("Stupid Bugs & Solves", "41"));
        card.add(createCategoryItem("Official Announcements", "5"));

        cardWrapper.add(card, BorderLayout.NORTH);
        return cardWrapper;
    }

    private JPanel createCategoryItem(String titleStr, String countStr) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 35));
        panel.setBorder(new EmptyBorder(8, 0, 8, 0));
        
        JLabel title = new JLabel(titleStr);
        title.setFont(new Font("Arial", Font.PLAIN, 13));
        title.setForeground(new Color(90, 90, 90));
        title.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // A mini-badge for thread count visualization
        JLabel badge = new JLabel(countStr, SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBackground(new Color(220, 220, 220));
        badge.setForeground(new Color(100, 100, 100));
        badge.setFont(new Font("Arial", Font.BOLD, 11));
        badge.setPreferredSize(new Dimension(30, 20));
        
        panel.add(title, BorderLayout.CENTER);
        panel.add(badge, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createPollCard() {
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setBackground(new Color(245, 245, 245));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(15, 20, 20, 20)
        ));

        JLabel title = new JLabel("Poll of the Week");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(new Color(51, 51, 51));
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(title);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(headerPanel);

        JLabel question = new JLabel("Which subject needs more TAs?");
        question.setFont(new Font("Arial", Font.PLAIN, 13));
        question.setForeground(new Color(70, 70, 70));
        
        JPanel qPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        qPanel.setBackground(Color.WHITE);
        qPanel.add(question);
        qPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        card.add(qPanel);

        // Options
        ButtonGroup bg = new ButtonGroup();
        String[] opts = {"Data Structures", "Computer Networks", "Software Engineering"};
        for(String opt : opts) {
            JRadioButton rb = new JRadioButton(opt);
            rb.setBackground(Color.WHITE);
            rb.setFont(new Font("Arial", Font.PLAIN, 13));
            rb.setForeground(new Color(80, 80, 80));
            rb.setFocusPainted(false);
            bg.add(rb);
            
            JPanel rPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            rPanel.setBackground(Color.WHITE);
            rPanel.add(rb);
            card.add(rPanel);
            card.add(Box.createVerticalStrut(5));
        }

        JButton voteBtn = new JButton("Submit Vote");
        voteBtn.setBackground(new Color(155, 89, 182)); // Purple theme for vote button
        voteBtn.setForeground(Color.WHITE);
        voteBtn.setFont(new Font("Arial", Font.BOLD, 12));
        voteBtn.setFocusPainted(false);
        voteBtn.setBorderPainted(false);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(voteBtn);
        card.add(btnPanel);

        cardWrapper.add(card, BorderLayout.NORTH);
        return cardWrapper;
    }

    // Custom Component drawing the Avatar initials
    class AvatarIcon extends JPanel {
        private String initText;
        private Color bgColor;

        public AvatarIcon(String name, Color bg) {
            this.initText = (name != null && !name.trim().isEmpty()) ? name.substring(0, 1).toUpperCase() : "?";
            this.bgColor = bg;
            setPreferredSize(new Dimension(50, 50));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw Circle
            g2.setColor(bgColor);
            g2.fillOval(0, 0, 48, 48);

            // Draw Initials
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            FontMetrics fm = g2.getFontMetrics();
            int x = (48 - fm.stringWidth(initText)) / 2;
            int y = (48 - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(initText, x, y);

            // Draw small online/status indicator (little dot)
            g2.setColor(new Color(46, 204, 113)); // Green dot
            g2.fillOval(35, 35, 12, 12);
            g2.setColor(Color.WHITE); // White border around dot
            g2.drawOval(35, 35, 12, 12);

            g2.dispose();
        }
    }
}