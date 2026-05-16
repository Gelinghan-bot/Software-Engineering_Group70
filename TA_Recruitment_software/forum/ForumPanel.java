package TA_Recruitment_software.forum;

import TA_Recruitment_software.RecruitmentSystemContext;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import java.util.List;
import TA_Recruitment_software.admin_system.model.User;
import TA_Recruitment_software.admin_system.repository.UserRepository;

public class ForumPanel extends JPanel {
    private JFrame parent;
    private RecruitmentSystemContext context;
    private String token;
    private Component previousCenterComponent;
    private TopicRepository topicRepository;
    private JPanel leftPanel; 
    private JPanel mainAreaWrapper; // Wrapper to switch between list and detail views
    private JPanel contentWrapper; // The original list wrapper
    private int currentPage = 1;
    private int topicsPerPage = 4;

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

        mainAreaWrapper = new JPanel(new CardLayout());
        mainAreaWrapper.setBackground(new Color(245, 245, 245));

        // Main content area wrapper to simulate max width bounds and margins
        contentWrapper = new JPanel(new BorderLayout(20, 20));
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

        sidebar.add(Box.createVerticalStrut(10)); // Move panels down slightly
        sidebar.add(createUserDashboardCard());
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(createRecommendationsCard());
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(createPollCard());

        contentWrapper.add(sidebar, BorderLayout.EAST);

        mainAreaWrapper.add(contentWrapper, "List");
        add(mainAreaWrapper, BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel tb = new JPanel(new BorderLayout());
        tb.setBackground(Color.WHITE);
        tb.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1), 
            new EmptyBorder(12, 40, 12, 40)
        ));

        // Left Title & New Topic Button
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        left.setBackground(Color.WHITE);
        JLabel title = new JLabel("System Forum");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(39, 174, 96));
        
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

        left.add(title);
        left.add(newTopicBtn);

        // Right Actions (Search Box & Find Button)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Color.WHITE);

        JTextField searchBox = new JTextField("Search Topics");
        searchBox.setPreferredSize(new Dimension(250, 36));
        searchBox.setFont(new Font("Arial", Font.PLAIN, 14));
        searchBox.setForeground(Color.GRAY);
        searchBox.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(0, 10, 0, 10)
        ));

        // Add a focus listener to clear text like a placeholder
        searchBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchBox.getText().equals("Search Topics")) {
                    searchBox.setText("");
                    searchBox.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchBox.getText().isEmpty()) {
                    searchBox.setForeground(Color.GRAY);
                    searchBox.setText("Search Topics");
                }
            }
        });

        JButton findBtn = new JButton("Find");
        findBtn.setBackground(new Color(52, 152, 219));
        findBtn.setForeground(Color.WHITE);
        findBtn.setFont(new Font("Arial", Font.BOLD, 13));
        findBtn.setPreferredSize(new Dimension(90, 36));
        findBtn.setFocusPainted(false);
        findBtn.setBorderPainted(false);
        findBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        findBtn.addActionListener(e -> {
            String q = searchBox.getText().trim();
            if (q.equals("Search Topics") || q.isEmpty()) {
                loadTopics(null);
            } else {
                loadTopics(q);
            }
        });

        right.add(searchBox);
        right.add(findBtn);

        tb.add(left, BorderLayout.WEST);
        tb.add(right, BorderLayout.EAST);
        return tb;
    }

    private boolean isLoggedIn() {
        return token != null;
    }

    private String getLoggedInUsername() {
        if (token != null) {
            try {
                TA_Recruitment_software.auth.SessionContext sessionCtx = context.getSessionManager().requireSession(token);
                String username = sessionCtx.getFullName();
                if (username == null || username.trim().isEmpty()) {
                    username = sessionCtx.getAccountId();
                }
                return username;
            } catch (Exception ex) {}
        }
        return "Guest";
    }

    private enum SortStrategy { DEFAULT, LIKES, FAVORITES, COMMENTS, DATE }
    private SortStrategy currentSortStrategy = SortStrategy.DEFAULT;

    private void loadTopics() {
        loadTopics(null);
    }

    private void loadTopics(String keyword) {
        leftPanel.removeAll();
        
        List<Topic> topics = topicRepository.getAllTopics();

        // 1. Filter
        List<Topic> filtered = new java.util.ArrayList<>();
        for (Topic t : topics) {
            if (keyword != null && !keyword.isEmpty()) {
                String lowerK = keyword.toLowerCase();
                if (!t.getTitle().toLowerCase().contains(lowerK) && 
                    !t.getContent().toLowerCase().contains(lowerK) && 
                    !t.getAuthorName().toLowerCase().contains(lowerK)) {
                    continue; // skip
                }
            }
            filtered.add(t);
        }

        // 2. Sort
        if (currentSortStrategy == SortStrategy.LIKES) {
            filtered.sort((t1, t2) -> Integer.compare(t2.getLikes(), t1.getLikes()));
        } else if (currentSortStrategy == SortStrategy.FAVORITES) {
            filtered.sort((t1, t2) -> Integer.compare(t2.getFavorites(), t1.getFavorites()));
        } else if (currentSortStrategy == SortStrategy.COMMENTS) {
            filtered.sort((t1, t2) -> Integer.compare(t2.getComments(), t1.getComments()));
        } else {
            // DATE / DEFAULT: newest first (reverse chronological)
            java.util.Collections.reverse(filtered);
        }

        // 3. Paginate
        int totalFiltered = filtered.size();
        int totalPages = (int) Math.ceil((double) totalFiltered / topicsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int startIndex = (currentPage - 1) * topicsPerPage;
        int endIndex = Math.min(startIndex + topicsPerPage, totalFiltered);

        // Colors to cycle through for avatars
        Color[] colors = {new Color(41, 128, 185), new Color(192, 57, 43), new Color(39, 174, 96), new Color(142, 68, 173), new Color(243, 156, 18)};

        for (int i = startIndex; i < endIndex; i++) {
            Topic t = filtered.get(i);
            int colorIdx = Math.abs(t.getAuthorName().hashCode()) % colors.length;
            
            JPanel card = createTopicCard(t, colors[colorIdx]);
            leftPanel.add(card);
            leftPanel.add(Box.createVerticalStrut(15));
        }

        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(createPaginationPanel(totalPages));

        leftPanel.revalidate();
        leftPanel.repaint();
    }

    private void showTopicDetail(Topic topic) {
        JPanel detailView = new JPanel(new BorderLayout(20, 20));
        detailView.setBackground(new Color(245, 245, 245));
        detailView.setBorder(new EmptyBorder(25, 40, 25, 40));

        // Top bar for 'Back'
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setBackground(new Color(245, 245, 245));
        JButton backBtn = new JButton("← Back to Forum");
        backBtn.setFont(new Font("Arial", Font.BOLD, 13));
        backBtn.setBackground(new Color(230, 230, 230));
        backBtn.setForeground(new Color(60, 60, 60));
        backBtn.setFocusPainted(false);
        backBtn.setBorder(new EmptyBorder(8, 15, 8, 15));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            ((CardLayout)mainAreaWrapper.getLayout()).show(mainAreaWrapper, "List");
            mainAreaWrapper.remove(detailView); // Fix CardLayout memory leak and hidden component crashing
            mainAreaWrapper.revalidate();
            mainAreaWrapper.repaint();
            loadTopics(); // refresh likes/comments
        });
        topBar.add(backBtn);
        
        detailView.add(topBar, BorderLayout.NORTH);

        // Main content
        JPanel contentCard = new JPanel(new BorderLayout(15, 20));
        contentCard.setBackground(Color.WHITE);
        contentCard.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(30, 30, 30, 30)
        ));

        // Header
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(Color.WHITE);
        Color avatarColor = new Color(41, 128, 185); // simple fallback
        header.add(new AvatarIcon(topic.getAuthorName(), avatarColor), BorderLayout.WEST);

        JPanel headerInfo = new JPanel(new GridLayout(2, 1));
        headerInfo.setBackground(Color.WHITE);
        JLabel titleLbl = new JLabel("<html><body style='width: 400px;'>" + topic.getTitle() + "</body></html>");
        titleLbl.setFont(new Font("Arial", Font.BOLD, 22));
        titleLbl.setForeground(new Color(51, 51, 51));
        
        JLabel metaLbl = new JLabel("By " + topic.getAuthorName() + "  |  " + topic.getDateStr());
        metaLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        metaLbl.setForeground(new Color(150, 150, 150));
        
        headerInfo.add(titleLbl);
        headerInfo.add(metaLbl);
        header.add(headerInfo, BorderLayout.CENTER);
        contentCard.add(header, BorderLayout.NORTH);

        // Body
        JTextArea bodyArea = new JTextArea(topic.getContent());
        bodyArea.setFont(new Font("Arial", Font.PLAIN, 15));
        bodyArea.setForeground(new Color(80, 80, 80));
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setEditable(false);
        bodyArea.setBorder(new EmptyBorder(10, 0, 10, 0));
        contentCard.add(new JScrollPane(bodyArea), BorderLayout.CENTER);

        // Interaction Footer
        JPanel actionFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        actionFooter.setBackground(Color.WHITE);
        actionFooter.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(240, 240, 240)),
            new EmptyBorder(15, 0, 0, 0)
        ));

        String loggedInUser = getLoggedInUsername();

        JButton likeBtn = new JButton(topic.getLikedByUsers().contains(loggedInUser) && isLoggedIn() ? "❤️ Liked (" + topic.getLikes() + ")" : "👍 Like (" + topic.getLikes() + ")");
        JButton commentBtn = new JButton("💬 Comment (" + topic.getComments() + ")");
        JButton favBtn = new JButton(topic.getFavoritedByUsers().contains(loggedInUser) && isLoggedIn() ? "🌟 Favorited (" + topic.getFavorites() + ")" : "⭐ Favorite (" + topic.getFavorites() + ")");

        Font actionFont = new Font("Segoe UI Emoji", Font.PLAIN, 14);
        likeBtn.setFont(actionFont);
        commentBtn.setFont(actionFont);
        favBtn.setFont(actionFont);

        likeBtn.setBackground(new Color(240,250,240)); likeBtn.setForeground(new Color(39, 174, 96));
        commentBtn.setBackground(new Color(240,245,250)); commentBtn.setForeground(new Color(52, 152, 219));
        favBtn.setBackground(Color.WHITE); favBtn.setForeground(new Color(243, 156, 18));
        
        for (JButton b : new JButton[]{likeBtn, commentBtn, favBtn}) {
            b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220)), new EmptyBorder(8, 15, 8, 15)));
        }

        likeBtn.addActionListener(e -> {
            if (!isLoggedIn()) {
                JOptionPane.showMessageDialog(this, "Please login first to interact with topics.", "Login Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean isLiked = topic.toggleLike(loggedInUser);
            topicRepository.updateTopic(topic);
            likeBtn.setText(isLiked ? "❤️ Liked (" + topic.getLikes() + ")" : "👍 Like (" + topic.getLikes() + ")");
        });
        
        commentBtn.addActionListener(e -> {
            if (!isLoggedIn()) {
                JOptionPane.showMessageDialog(this, "Please login first to leave a comment.", "Login Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Text area for multi-line input
            JTextArea textArea = new JTextArea(5, 30);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("Arial", Font.PLAIN, 14));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Your Comment"));
            
            int result = JOptionPane.showConfirmDialog(this, scrollPane, "Add Comment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String input = textArea.getText();
                if (input != null && !input.trim().isEmpty()) {
                    topic.incrementComments();
                    topicRepository.updateTopic(topic);
                    commentBtn.setText("💬 Comment (" + topic.getComments() + ")");
                    // Here you would optimally add logic to save the explicit comment text in a Comment object/file,
                    // but for this UI interaction logic we increment the numeric interaction immediately.
                    JOptionPane.showMessageDialog(this, "Comment successfully posted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        favBtn.addActionListener(e -> {
            if (!isLoggedIn()) {
                JOptionPane.showMessageDialog(this, "Please login first to favorite topics.", "Login Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean isFav = topic.toggleFavorite(loggedInUser);
            topicRepository.updateTopic(topic);
            favBtn.setText(isFav ? "🌟 Favorited (" + topic.getFavorites() + ")" : "⭐ Favorite (" + topic.getFavorites() + ")");
        });

        actionFooter.add(likeBtn);
        actionFooter.add(commentBtn);
        actionFooter.add(favBtn);

        contentCard.add(actionFooter, BorderLayout.SOUTH);

        detailView.add(contentCard, BorderLayout.CENTER);
        
        // Add to main layout and flip
        mainAreaWrapper.add(detailView, "Detail");
        ((CardLayout)mainAreaWrapper.getLayout()).show(mainAreaWrapper, "Detail");
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

    private JPanel createTopicCard(Topic topic, Color avatarColor) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(20, 20, 15, 20) // Top, Left, Bottom, Right Inner Padding
        ));
        
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showTopicDetail(topic);
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(250, 250, 250));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        // Left Side: Avatar Column
        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setOpaque(false);
        
        // Custom component mapping username initials to a colored circle profile
        AvatarIcon icon = new AvatarIcon(topic.getAuthorName(), avatarColor);
        avatarPanel.add(icon, BorderLayout.NORTH);

        // Middle Content Area
        JPanel contentArea = new JPanel(new BorderLayout(0, 8));
        contentArea.setOpaque(false);

        JLabel title = new JLabel(topic.getTitle());
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(new Color(51, 51, 51));
        
        String bodyText = topic.getContent();
        if (bodyText.length() > 200) {
            bodyText = bodyText.substring(0, 200) + "..."; // Trim long text for summary
        }
        
        JTextArea body = new JTextArea(bodyText);
        body.setWrapStyleWord(true);
        body.setLineWrap(true);
        body.setEditable(false);
        body.setFocusable(false);
        body.setOpaque(false);
        body.setFont(new Font("Arial", Font.PLAIN, 13));
        body.setForeground(new Color(110, 110, 110));
        body.setBorder(null);

        contentArea.add(title, BorderLayout.NORTH);
        contentArea.add(body, BorderLayout.CENTER);

        // Bottom Footer (Likes, Comments, Date)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        String loggedInUser = getLoggedInUsername();

        JLabel likesLbl = new JLabel(topic.getLikedByUsers().contains(loggedInUser) && isLoggedIn() ? "❤️ " + topic.getLikes() : "👍 " + topic.getLikes());
        likesLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        likesLbl.setForeground(new Color(39, 174, 96));
        likesLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel commentsLbl = new JLabel("💬 " + topic.getComments());
        commentsLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        commentsLbl.setForeground(new Color(150, 150, 150));
        commentsLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel favLbl = new JLabel(topic.getFavoritedByUsers().contains(loggedInUser) && isLoggedIn() ? "🌟 " + topic.getFavorites() : "⭐ " + topic.getFavorites());
        favLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        favLbl.setForeground(new Color(243, 156, 18));
        favLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel dateLbl = new JLabel("🕒 Posted on: " + topic.getDateStr());
        dateLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLbl.setForeground(new Color(170, 170, 170));
        
        // Fast interactions on list view
        likesLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!isLoggedIn()) { JOptionPane.showMessageDialog(ForumPanel.this, "Please login to interact."); return; }
                boolean isLiked = topic.toggleLike(loggedInUser);
                topicRepository.updateTopic(topic);
                likesLbl.setText(isLiked ? "❤️ " + topic.getLikes() : "👍 " + topic.getLikes());
                e.consume(); // stops bubbling to detail view
            }
        });
        
        favLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!isLoggedIn()) { JOptionPane.showMessageDialog(ForumPanel.this, "Please login to interact."); return; }
                boolean isFav = topic.toggleFavorite(loggedInUser);
                topicRepository.updateTopic(topic);
                favLbl.setText(isFav ? "🌟 " + topic.getFavorites() : "⭐ " + topic.getFavorites());
                e.consume(); 
            }
        });
        
        commentsLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Clicking comment on card directly takes them to detail view essentially
                // so we do nothing here, letting it bubble up.
            }
        });

        footer.add(likesLbl);
        footer.add(commentsLbl);
        footer.add(favLbl);
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

    private JPanel createPaginationPanel(int totalPages) {
        JPanel pagWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pagWrap.setBackground(new Color(245, 245, 245));
        
        // Previous Data
        JButton prevBtn = createPageBtn("<", currentPage > 1);
        prevBtn.addActionListener(e -> { 
            if (currentPage > 1) { 
                currentPage--; 
                loadTopics(); 
            } 
        });
        pagWrap.add(prevBtn);
        
        int maxPagesToShow = 5;
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + maxPagesToShow - 1);
        
        // Adjust start if end hit the boundary
        startPage = Math.max(1, endPage - maxPagesToShow + 1);

        for (int i = startPage; i <= endPage; i++) {
            final int pageNum = i;
            JButton btn = createPageBtn(String.valueOf(i), true);
            if (pageNum == currentPage) {
                btn.setBackground(new Color(39, 174, 96));
                btn.setForeground(Color.WHITE);
            }
            btn.addActionListener(e -> { 
                currentPage = pageNum; 
                loadTopics(); 
            });
            pagWrap.add(btn);
        }
        
        JButton nextBtn = createPageBtn(">", currentPage < totalPages);
        nextBtn.addActionListener(e -> { 
            if (currentPage < totalPages) { 
                currentPage++; 
                loadTopics(); 
            } 
        });
        pagWrap.add(nextBtn);
        
        return pagWrap;
    }

    private JButton createPageBtn(String text, boolean enabled) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(35, 30));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(100, 100, 100));
        btn.setCursor(enabled ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
        btn.setEnabled(enabled);
        return btn;
    }

    private JPanel createUserDashboardCard() {
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setBackground(new Color(245, 245, 245));
        
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(15, 20, 15, 20)
        ));

        // Attempt to get user info
        String username = "Guest";
        boolean isLoggedIn = false;
        if (token != null) {
            try {
                TA_Recruitment_software.auth.SessionContext sessionCtx = context.getSessionManager().requireSession(token);
                username = sessionCtx.getFullName();
                if (username == null || username.trim().isEmpty()) {
                    username = sessionCtx.getAccountId();
                }
                isLoggedIn = true;
            } catch (Exception ex) {
                // Not logged in properly
            }
        }

        JLabel title = new JLabel("Welcome, " + username);
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(new Color(51, 51, 51));
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(title);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(headerPanel);

        if (isLoggedIn) {
            // Create a label that shows a popup menu on hover
            JLabel myHubLbl = new JLabel("🧩 My Dashboard ▾");
            myHubLbl.setFont(new Font("Arial", Font.BOLD, 13));
            myHubLbl.setForeground(new Color(39, 174, 96));
            myHubLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Construct the popup menu
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.setBorder(new LineBorder(new Color(210, 210, 210), 1));
            popupMenu.setBackground(Color.WHITE);
            
            JMenuItem msgItem = new JMenuItem("📬 My Messages (0)");
            JMenuItem postItem = new JMenuItem("📝 My Published Topics");
            JMenuItem likesItem = new JMenuItem("❤️ Topics I Liked");
            JMenuItem favItem = new JMenuItem("⭐ My Favorites");
            JMenuItem commentsItem = new JMenuItem("💬 My Comments");
            
            Font itemFont = new Font("Segoe UI Emoji", Font.PLAIN, 13);
            msgItem.setFont(itemFont); msgItem.setBackground(Color.WHITE);
            postItem.setFont(itemFont); postItem.setBackground(Color.WHITE);
            likesItem.setFont(itemFont); likesItem.setBackground(Color.WHITE);
            favItem.setFont(itemFont); favItem.setBackground(Color.WHITE);
            commentsItem.setFont(itemFont); commentsItem.setBackground(Color.WHITE);
            
            popupMenu.add(msgItem);
            popupMenu.add(postItem);
            popupMenu.add(likesItem);
            popupMenu.add(favItem);
            popupMenu.add(commentsItem);

            myHubLbl.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    popupMenu.show(myHubLbl, 0, myHubLbl.getHeight());
                }
            });
            
            JPanel hubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            hubPanel.setBackground(Color.WHITE);
            hubPanel.add(myHubLbl);
            card.add(hubPanel);
            
        } else {
            JLabel loginHint = new JLabel("Login to see your activities.");
            loginHint.setFont(new Font("Arial", Font.ITALIC, 12));
            loginHint.setForeground(new Color(150, 150, 150));
            
            JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            hintPanel.setBackground(Color.WHITE);
            hintPanel.add(loginHint);
            card.add(hintPanel);
        }

        cardWrapper.add(card, BorderLayout.NORTH);
        return cardWrapper;
    }

    private JPanel createRecommendationsCard() {
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setBackground(new Color(245, 245, 245));
        
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(15, 20, 20, 20)
        ));

        JLabel title = new JLabel("Guess what you like");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(new Color(51, 51, 51));
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(title);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        card.add(headerPanel);

        // Tags
        card.add(createActionItem("Default Sort", "⭐", () -> {
            currentSortStrategy = SortStrategy.DEFAULT;
            loadTopics();
        }));
        card.add(createActionItem("Sort by Likes", "❤️", () -> {
            currentSortStrategy = SortStrategy.LIKES;
            loadTopics();
        }));
        card.add(createActionItem("Sort by Favorites", "🌟", () -> {
            currentSortStrategy = SortStrategy.FAVORITES;
            loadTopics();
        }));
        card.add(createActionItem("Sort by Comments", "📝", () -> {
            currentSortStrategy = SortStrategy.COMMENTS;
            loadTopics();
        }));
        card.add(createActionItem("Sort by Date", "🕒", () -> {
            currentSortStrategy = SortStrategy.DATE;
            loadTopics();
        }));

        cardWrapper.add(card, BorderLayout.NORTH);
        return cardWrapper;
    }

    private JPanel createActionItem(String titleStr, String iconStr, Runnable action) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 35));
        panel.setBorder(new EmptyBorder(8, 0, 8, 0));
        
        JLabel title = new JLabel(titleStr);
        title.setFont(new Font("Arial", Font.PLAIN, 13));
        title.setForeground(new Color(90, 90, 90));
        title.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel badge = new JLabel(iconStr, SwingConstants.CENTER);
        badge.setForeground(new Color(100, 100, 100));
        badge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        badge.setPreferredSize(new Dimension(30, 20));
        
        panel.add(title, BorderLayout.CENTER);
        panel.add(badge, BorderLayout.EAST);
        
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (action != null) action.run();
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                title.setForeground(new Color(39, 174, 96));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                title.setForeground(new Color(90, 90, 90));
            }
        });
        
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