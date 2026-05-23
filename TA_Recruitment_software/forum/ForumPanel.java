package TA_Recruitment_software.forum;

import TA_Recruitment_software.RecruitmentSystemContext;
import TA_Recruitment_software.admin_system.foundation.UIStyle;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

public class ForumPanel extends JPanel {
    private static final String EMOJI_FONT_NAME = UIStyle.EMOJI_FONT;

    private JFrame parent;
    private RecruitmentSystemContext context;
    private String token;
    private Component previousCenterComponent;
    private TopicRepository topicRepository;
    private CommentRepository commentRepository;
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
        this.commentRepository = new CommentRepository();
        
        setLayout(new BorderLayout());
        setBackground(UIStyle.BG_PAGE);

        // Top Toolbar
        add(createToolbar(), BorderLayout.NORTH);

        mainAreaWrapper = new JPanel(new CardLayout());
        mainAreaWrapper.setBackground(UIStyle.BG_PAGE);

        // Main content area wrapper to simulate max width bounds and margins
        contentWrapper = new JPanel(new BorderLayout(20, 20));
        contentWrapper.setBackground(UIStyle.BG_PAGE);
        contentWrapper.setBorder(new EmptyBorder(25, 40, 25, 40));

        // Left Area: Topics List
        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(UIStyle.BG_PAGE);

        loadTopics();
        
        // Wrap Left Area in JScrollPane
        JScrollPane scrollLeft = new JScrollPane(leftPanel);
        UIStyle.styleScrollPane(scrollLeft);
        contentWrapper.add(scrollLeft, BorderLayout.CENTER);

        // Right Area: Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIStyle.BG_PAGE);
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
        tb.setBackground(UIStyle.BG_CARD);
        tb.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, UIStyle.BORDER),
            new EmptyBorder(10, 30, 10, 30)
        ));

        // Left Title & New Topic Button
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        left.setBackground(UIStyle.BG_CARD);
        JLabel title = new JLabel("Forum Area");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.PRIMARY);
        title.setCursor(new Cursor(Cursor.HAND_CURSOR));
        title.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Reset all states
                currentFilterStrategy = FilterStrategy.ALL;
                currentSortStrategy = SortStrategy.DEFAULT;
                currentPage = 1;
                // Make sure we switch out of detail view back to lists
                ((CardLayout)mainAreaWrapper.getLayout()).show(mainAreaWrapper, "List");
                // Reload data
                loadTopics();
                refreshPollCard();
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                title.setText("<html><u>Forum Area</u></html>");
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                title.setText("Forum Area");
            }
        });
        
        JButton newTopicBtn = new JButton("Start New Topic");
        newTopicBtn.setBackground(UIStyle.PRIMARY);
        newTopicBtn.setForeground(Color.WHITE);
        newTopicBtn.setFont(UIStyle.FONT_BUTTON.deriveFont(13f));
        newTopicBtn.setPreferredSize(new Dimension(140, 36));
        newTopicBtn.setFocusPainted(false);
        newTopicBtn.setBorderPainted(false);
        newTopicBtn.setOpaque(true);
        newTopicBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newTopicBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                newTopicBtn.setBackground(UIStyle.PRIMARY_DARK);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                newTopicBtn.setBackground(UIStyle.PRIMARY);
            }
        });
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
        right.setBackground(UIStyle.BG_CARD);

        JTextField searchBox = UIStyle.createTextField(20);
        searchBox.setText("Search Topics");
        searchBox.setForeground(UIStyle.TEXT_SECONDARY);
        searchBox.setPreferredSize(new Dimension(220, 36));

        // Add a focus listener to clear text like a placeholder
        searchBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchBox.getText().equals("Search Topics")) {
                    searchBox.setText("");
                    searchBox.setForeground(UIStyle.TEXT_PRIMARY);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchBox.getText().isEmpty()) {
                    searchBox.setForeground(UIStyle.TEXT_SECONDARY);
                    searchBox.setText("Search Topics");
                }
            }
        });

        JButton findBtn = new JButton("Find");
        findBtn.setBackground(UIStyle.ACCENT_BLUE);
        findBtn.setForeground(Color.WHITE);
        findBtn.setFont(UIStyle.FONT_BUTTON.deriveFont(13f));
        findBtn.setPreferredSize(new Dimension(80, 36));
        findBtn.setFocusPainted(false);
        findBtn.setBorderPainted(false);
        findBtn.setOpaque(true);
        findBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        findBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                findBtn.setBackground(UIStyle.ACCENT_BLUE_DARK);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                findBtn.setBackground(UIStyle.ACCENT_BLUE);
            }
        });
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

    private enum FilterStrategy { ALL, MY_POSTS, LIKED, FAVORITES }
    private FilterStrategy currentFilterStrategy = FilterStrategy.ALL;

    private void loadTopics() {
        loadTopics(null);
    }

    private void loadTopics(String keyword) {
        leftPanel.removeAll();
        
        List<Topic> topics = topicRepository.getAllTopics();

        // 1. Filter
        List<Topic> filtered = new java.util.ArrayList<>();
        String loggedInUser = getLoggedInUsername();
        for (Topic t : topics) {
            // Apply keyword search
            if (keyword != null && !keyword.isEmpty()) {
                String lowerK = keyword.toLowerCase();
                if (!t.getTitle().toLowerCase().contains(lowerK) && 
                    !t.getContent().toLowerCase().contains(lowerK) && 
                    !t.getAuthorName().toLowerCase().contains(lowerK)) {
                    continue; // skip
                }
            }
            
            // Apply Dashboard filters
            if (currentFilterStrategy == FilterStrategy.MY_POSTS && !t.getAuthorName().equals(loggedInUser)) {
                continue;
            }
            if (currentFilterStrategy == FilterStrategy.LIKED && !t.getLikedByUsers().contains(loggedInUser)) {
                continue;
            }
            if (currentFilterStrategy == FilterStrategy.FAVORITES && !t.getFavoritedByUsers().contains(loggedInUser)) {
                continue;
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
        detailView.setBackground(UIStyle.BG_PAGE);
        detailView.setBorder(new EmptyBorder(25, 40, 25, 40));

        // Top bar for 'Back'
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setBackground(UIStyle.BG_PAGE);
        JButton backBtn = new JButton("\u2190 Back to Forum");
        backBtn.setFont(UIStyle.FONT_BODY_BOLD);
        backBtn.setBackground(new Color(236, 240, 241));
        backBtn.setForeground(UIStyle.TEXT_PRIMARY);
        backBtn.setFocusPainted(false);
        backBtn.setOpaque(true);
        backBtn.setBorder(new EmptyBorder(8, 15, 8, 15));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                backBtn.setBackground(new Color(222, 226, 230));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                backBtn.setBackground(new Color(236, 240, 241));
            }
        });
        backBtn.addActionListener(e -> {
            ((CardLayout)mainAreaWrapper.getLayout()).show(mainAreaWrapper, "List");
            mainAreaWrapper.remove(detailView); // Fix CardLayout memory leak and hidden component crashing
            mainAreaWrapper.revalidate();
            mainAreaWrapper.repaint();
            loadTopics(); // refresh likes/comments
        });
        topBar.add(backBtn);
        
        detailView.add(topBar, BorderLayout.NORTH);

        // Define unified scrollable container making text wrap height properly
        class ScrollablePanel extends JPanel implements Scrollable {
            public ScrollablePanel(LayoutManager l) { super(l); }
            @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
            @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
            @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return r.height; }
            @Override public boolean getScrollableTracksViewportWidth() { return true; }
            @Override public boolean getScrollableTracksViewportHeight() { return false; }
        }

        // Main content
        JPanel contentCard = new ScrollablePanel(new BorderLayout(15, 20));
        contentCard.setBackground(UIStyle.BG_CARD);
        contentCard.setBorder(new CompoundBorder(
            new LineBorder(UIStyle.BORDER, 1, true),
            new EmptyBorder(25, 25, 25, 25)
        ));

        // Header
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(UIStyle.BG_CARD);
        Color avatarColor = new Color(41, 128, 185); // simple fallback
        header.add(new AvatarIcon(topic.getAuthorName(), avatarColor), BorderLayout.WEST);

        JPanel headerInfo = new JPanel(new GridLayout(2, 1));
        headerInfo.setBackground(UIStyle.BG_CARD);
        JLabel titleLbl = new JLabel("<html><body style='width: 400px;'>" + topic.getTitle() + "</body></html>");
        titleLbl.setFont(UIStyle.FONT_HEADING.deriveFont(20f));
        titleLbl.setForeground(UIStyle.TEXT_PRIMARY);
        
        JLabel metaLbl = new JLabel("By " + topic.getAuthorName() + "  |  " + topic.getDateStr());
        metaLbl.setFont(UIStyle.FONT_SMALL);
        metaLbl.setForeground(UIStyle.TEXT_SECONDARY);
        
        headerInfo.add(titleLbl);
        headerInfo.add(metaLbl);
        header.add(headerInfo, BorderLayout.CENTER);
        contentCard.add(header, BorderLayout.NORTH);

        // We will stack bodyArea, ActionFooter, and CommentsPanel together without separate scroll bars
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setBackground(UIStyle.BG_CARD);

        // Body
        JTextArea bodyArea = new JTextArea(topic.getContent());
        bodyArea.setFont(UIStyle.FONT_BODY.deriveFont(15f));
        bodyArea.setForeground(UIStyle.TEXT_PRIMARY);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setEditable(false);
        bodyArea.setBackground(UIStyle.BG_CARD);
        bodyArea.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        centerContainer.add(bodyArea);

        // Interaction Footer
        JPanel actionFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        actionFooter.setBackground(UIStyle.BG_CARD);
        actionFooter.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, UIStyle.BORDER),
            new EmptyBorder(15, 0, 0, 0)
        ));

        String loggedInUser = getLoggedInUsername();

        JButton likeBtn = new JButton(topic.getLikedByUsers().contains(loggedInUser) && isLoggedIn() ? "\u2764\uFE0F Liked (" + topic.getLikes() + ")" : "\uD83D\uDC4D Like (" + topic.getLikes() + ")");
        JButton commentBtn = new JButton("\uD83D\uDCAC Comment (" + topic.getComments() + ")");
        JButton favBtn = new JButton(topic.getFavoritedByUsers().contains(loggedInUser) && isLoggedIn() ? "\uD83C\uDF1F Favorited (" + topic.getFavorites() + ")" : "\u2B50 Favorite (" + topic.getFavorites() + ")");

        likeBtn.setFont(UIStyle.FONT_EMOJI);
        commentBtn.setFont(UIStyle.FONT_EMOJI);
        favBtn.setFont(UIStyle.FONT_EMOJI);

        likeBtn.setBackground(new Color(240,250,240)); likeBtn.setForeground(new Color(39, 174, 96));
        commentBtn.setBackground(new Color(240,245,250)); commentBtn.setForeground(new Color(52, 152, 219));
        favBtn.setBackground(Color.WHITE); favBtn.setForeground(new Color(243, 156, 18));
        
        for (JButton b : new JButton[]{likeBtn, commentBtn, favBtn}) {
            b.setFocusPainted(false);
            b.setOpaque(true);
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
            likeBtn.setText(isLiked ? "\u2764\uFE0F Liked (" + topic.getLikes() + ")" : "\uD83D\uDC4D Like (" + topic.getLikes() + ")");
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
                    Comment nc = new Comment(topic.getId(), loggedInUser, input);
                    commentRepository.addComment(nc);
                    topic.incrementComments();
                    topicRepository.updateTopic(topic);
                    mainAreaWrapper.remove(detailView);
                    showTopicDetail(topic);
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
            favBtn.setText(isFav ? "\uD83C\uDF1F Favorited (" + topic.getFavorites() + ")" : "\u2B50 Favorite (" + topic.getFavorites() + ")");
        });

        actionFooter.add(likeBtn);
        actionFooter.add(commentBtn);
        actionFooter.add(favBtn);

        centerContainer.add(actionFooter);

        JPanel commentsPanel = buildCommentsPanel(topic);
        centerContainer.add(commentsPanel);

        contentCard.add(centerContainer, BorderLayout.CENTER);

        // Add contentCard to a unifying ScrollPane
        JScrollPane masterScroll = new JScrollPane(contentCard);
        UIStyle.styleScrollPane(masterScroll);
        masterScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        detailView.add(masterScroll, BorderLayout.CENTER);
        
        // Add to main layout and flip
        mainAreaWrapper.add(detailView, "Detail");
        ((CardLayout)mainAreaWrapper.getLayout()).show(mainAreaWrapper, "Detail");
    }

    private void showNewTopicDialog() {
        JDialog dialog = new JDialog(parent, "Start New Topic", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIStyle.BG_PAGE);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(UIStyle.BG_CARD);

        JTextField titleField = UIStyle.createTextField(30);
        titleField.setFont(UIStyle.FONT_BODY_BOLD);
        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(UIStyle.BG_CARD);
        titleWrap.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIStyle.BORDER), "Topic Title"));
        titleWrap.add(titleField, BorderLayout.CENTER);

        JTextArea contentArea = new JTextArea();
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(UIStyle.FONT_BODY);
        contentArea.setBackground(UIStyle.WHITE);
        contentArea.setBorder(UIStyle.inputBorder());
        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIStyle.BORDER), "Topic Content"));
        UIStyle.styleScrollPane(scrollPane);

        formPanel.add(titleWrap, BorderLayout.NORTH);
        formPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttonPanel.setBackground(UIStyle.BG_CARD);

        JButton postBtn = UIStyle.createPrimaryButton("Post Topic");
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

        JButton cancelBtn = UIStyle.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(postBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createTopicCard(Topic topic, Color avatarColor) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(UIStyle.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(UIStyle.BORDER, 1, true),
            new EmptyBorder(18, 20, 15, 20) // Top, Left, Bottom, Right Inner Padding
        ));
        
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showTopicDetail(topic);
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(UIStyle.BG_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(UIStyle.BG_CARD);
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
        title.setFont(UIStyle.FONT_BODY_BOLD.deriveFont(16f));
        title.setForeground(UIStyle.TEXT_PRIMARY);
        
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

        JLabel likesLbl = new JLabel(topic.getLikedByUsers().contains(loggedInUser) && isLoggedIn() ? "\u2764\uFE0F " + topic.getLikes() : "\uD83D\uDC4D " + topic.getLikes());
        likesLbl.setFont(UIStyle.FONT_EMOJI.deriveFont(13f));
        likesLbl.setForeground(UIStyle.PRIMARY);
        likesLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel commentsLbl = new JLabel("\uD83D\uDCAC " + topic.getComments());
        commentsLbl.setFont(UIStyle.FONT_EMOJI.deriveFont(13f));
        commentsLbl.setForeground(UIStyle.TEXT_SECONDARY);
        commentsLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel favLbl = new JLabel(topic.getFavoritedByUsers().contains(loggedInUser) && isLoggedIn() ? "\uD83C\uDF1F " + topic.getFavorites() : "\u2B50 " + topic.getFavorites());
        favLbl.setFont(UIStyle.FONT_EMOJI.deriveFont(13f));
        favLbl.setForeground(UIStyle.WARNING);
        favLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel dateLbl = new JLabel("\uD83D\uDD52 Posted: " + topic.getDateStr());
        dateLbl.setFont(UIStyle.FONT_SMALL);
        dateLbl.setForeground(UIStyle.TEXT_SECONDARY);
        
        // Fast interactions on list view
        likesLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!isLoggedIn()) { JOptionPane.showMessageDialog(ForumPanel.this, "Please login to interact."); return; }
                boolean isLiked = topic.toggleLike(loggedInUser);
                topicRepository.updateTopic(topic);
                likesLbl.setText(isLiked ? "\u2764\uFE0F " + topic.getLikes() : "\uD83D\uDC4D " + topic.getLikes());
                e.consume(); // stops bubbling to detail view
            }
        });
        
        favLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!isLoggedIn()) { JOptionPane.showMessageDialog(ForumPanel.this, "Please login to interact."); return; }
                boolean isFav = topic.toggleFavorite(loggedInUser);
                topicRepository.updateTopic(topic);
                favLbl.setText(isFav ? "\uD83C\uDF1F " + topic.getFavorites() : "\u2B50 " + topic.getFavorites());
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
                btn.setOpaque(true);
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

        class NotificationItem {
        String type;
        String byUser;
        String topicId;
        String topicTitle;
        String detailText;
        String dateStr;
        NotificationItem(String type, String byUser, String topicId, String topicTitle, String detailText, String dateStr) {
            this.type = type; this.byUser = byUser; this.topicId = topicId; this.topicTitle = topicTitle; this.detailText = detailText; this.dateStr = dateStr;
        }
    }

    private java.util.List<NotificationItem> getNotifications(String loggedInUser) {
        java.util.List<NotificationItem> notifs = new java.util.ArrayList<>();
        if (loggedInUser == null || loggedInUser.equals("Guest")) return notifs;
        
        java.util.List<Topic> allTopics = topicRepository.getAllTopics();
        for (Topic t : allTopics) {
            if (t.getAuthorName().equals(loggedInUser)) {
                for (String liker : t.getLikedByUsers()) {
                    if (!liker.equals(loggedInUser)) {
                        notifs.add(new NotificationItem("Like", liker, t.getId(), t.getTitle(), liker + " liked your topic.", t.getDateStr()));
                    }
                }
                java.util.List<Comment> comments = commentRepository.getCommentsByTopicId(t.getId());
                for (Comment c : comments) {
                    if (!c.getAuthorName().equals(loggedInUser)) {
                        notifs.add(new NotificationItem("Comment", c.getAuthorName(), t.getId(), t.getTitle(), c.getContent(), c.getDateStr()));
                    }
                }
            }
        }
        
        java.util.Collections.reverse(notifs);
        return notifs;
    }

    private void showMyMessagesDialog(java.util.List<NotificationItem> notifications) {
        JDialog dialog = new JDialog((java.awt.Frame)SwingUtilities.getWindowAncestor(this), "My Messages", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(new Color(250, 250, 250));
        JLabel title = new JLabel("📬 Notifications (" + notifications.size() + ")");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(title);
        dialog.add(header, BorderLayout.NORTH);
        
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        
        if (notifications.isEmpty()) {
            JLabel empty = new JLabel("No new messages yet.");
            empty.setBorder(new EmptyBorder(20, 20, 20, 20));
            listPanel.add(empty);
        } else {
            for (NotificationItem n : notifications) {
                JPanel item = new JPanel(new BorderLayout(10, 5));
                item.setBackground(Color.WHITE);
                item.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                    new EmptyBorder(15, 15, 15, 15)
                ));
                
                String icon = n.type.equals("Like") ? "[Like] " : "[Comment] ";
                JLabel topLbl = new JLabel("<html><b>" + icon + n.byUser + "</b> " + 
                                         (n.type.equals("Like") ? "liked your topic:" : "commented on your topic:") + "</html>");
                topLbl.setFont(UIStyle.FONT_BODY);
                
                JLabel topicLbl = new JLabel("Topic: " + n.topicTitle);
                topicLbl.setFont(new Font("Arial", Font.ITALIC, 12));
                topicLbl.setForeground(new Color(150, 150, 150));
                
                JTextArea detail = new JTextArea(n.detailText);
                detail.setLineWrap(true);
                detail.setWrapStyleWord(true);
                detail.setEditable(false);
                detail.setForeground(new Color(80, 80, 80));
                detail.setFont(new Font("Arial", Font.PLAIN, 13));
                detail.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 0));
                
                JLabel timeLbl = new JLabel(n.dateStr);
                timeLbl.setFont(new Font("Arial", Font.PLAIN, 11));
                timeLbl.setForeground(new Color(180, 180, 180));
                
                JPanel pNorth = new JPanel(new GridLayout(2, 1));
                pNorth.setBackground(Color.WHITE);
                pNorth.add(topLbl);
                pNorth.add(topicLbl);
                
                item.add(pNorth, BorderLayout.NORTH);
                item.add(detail, BorderLayout.CENTER);
                item.add(timeLbl, BorderLayout.SOUTH);
                
                item.setCursor(new Cursor(Cursor.HAND_CURSOR));
                item.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        dialog.dispose();
                        Topic t = topicRepository.getAllTopics().stream().filter(tx -> tx.getId().equals(n.topicId)).findFirst().orElse(null);
                        if (t != null) {
                            showTopicDetail(t);
                        }
                    }
                });
                
                listPanel.add(item);
            }
        }
        
        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scroll, BorderLayout.CENTER);
        
        dialog.setVisible(true);
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
            
            java.util.List<NotificationItem> notifs = getNotifications(username);
            int msgCount = notifs.size();
            JMenuItem msgItem = new JMenuItem("📬 My Messages (" + msgCount + ")");
            msgItem.addActionListener(e -> showMyMessagesDialog(notifs));
            JMenuItem allTopicsItem = new JMenuItem("🌐 All Topics (Reset Filter)");
            JMenuItem postItem = new JMenuItem("📝 My Published Topics");
            JMenuItem likesItem = new JMenuItem("❤️ Topics I Liked");
            JMenuItem favItem = new JMenuItem("⭐ My Favorites");
            JMenuItem commentsItem = new JMenuItem("💬 My Comments");
            
            Font itemFont = new Font(EMOJI_FONT_NAME, Font.PLAIN, 13);
            msgItem.setFont(itemFont); msgItem.setBackground(Color.WHITE);
            allTopicsItem.setFont(itemFont); allTopicsItem.setBackground(Color.WHITE);
            postItem.setFont(itemFont); postItem.setBackground(Color.WHITE);
            likesItem.setFont(itemFont); likesItem.setBackground(Color.WHITE);
            favItem.setFont(itemFont); favItem.setBackground(Color.WHITE);
            commentsItem.setFont(itemFont); commentsItem.setBackground(Color.WHITE);
            
            popupMenu.add(allTopicsItem);
            popupMenu.addSeparator();
            popupMenu.add(msgItem);
            popupMenu.add(postItem);
            popupMenu.add(likesItem);
            popupMenu.add(favItem);
            popupMenu.add(commentsItem);

            // Add Actions
            allTopicsItem.addActionListener(e -> {
                currentFilterStrategy = FilterStrategy.ALL;
                currentPage = 1;
                loadTopics();
            });
            postItem.addActionListener(e -> {
                currentFilterStrategy = FilterStrategy.MY_POSTS;
                currentPage = 1;
                loadTopics();
            });
            likesItem.addActionListener(e -> {
                currentFilterStrategy = FilterStrategy.LIKED;
                currentPage = 1;
                loadTopics();
            });
            favItem.addActionListener(e -> {
                currentFilterStrategy = FilterStrategy.FAVORITES;
                currentPage = 1;
                loadTopics();
            });

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
        badge.setFont(new Font(EMOJI_FONT_NAME, Font.PLAIN, 14));
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

    private PollRepository pollRepository = new PollRepository();
    private JPanel pollCardContainer;

    private JPanel createPollCard() {
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setBackground(new Color(245, 245, 245));

        pollCardContainer = new JPanel();
        pollCardContainer.setLayout(new BoxLayout(pollCardContainer, BoxLayout.Y_AXIS));
        pollCardContainer.setBackground(Color.WHITE);
        pollCardContainer.setBorder(new CompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(15, 20, 20, 20)
        ));

        buildPollCardContent();

        cardWrapper.add(pollCardContainer, BorderLayout.NORTH);
        return cardWrapper;
    }

    private void refreshPollCard() {
        if (pollCardContainer != null) {
            pollCardContainer.removeAll();
            buildPollCardContent();
            pollCardContainer.revalidate();
            pollCardContainer.repaint();
        }
    }

    private void buildPollCardContent() {
        JLabel title = new JLabel("Poll of the Week");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(new Color(51, 51, 51));
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(title);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        pollCardContainer.add(headerPanel);

        JLabel question = new JLabel("Which subject needs more TAs?");
        question.setFont(new Font("Arial", Font.PLAIN, 13));
        question.setForeground(new Color(70, 70, 70));
        
        JPanel qPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        qPanel.setBackground(Color.WHITE);
        qPanel.add(question);
        qPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        pollCardContainer.add(qPanel);

        String[] opts = {"Data Structures", "Computer Networks", "Software Engineering"};
        java.util.Map<String, String> votes = pollRepository.loadVotes();
        
        String loggedInUser = isLoggedIn() ? getLoggedInUsername() : null;
        String userVote = (loggedInUser != null) ? votes.get(loggedInUser) : null;
        
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        for (String v : votes.values()) counts.put(v, counts.getOrDefault(v, 0) + 1);

        ButtonGroup bg = new ButtonGroup();
        for(String opt : opts) {
            JPanel rPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            rPanel.setBackground(Color.WHITE);

            if (userVote != null) {
                // User has voted, show disabled radio button and vote counts
                JRadioButton rb = new JRadioButton(opt + " (" + counts.getOrDefault(opt, 0) + " votes)");
                rb.setBackground(Color.WHITE);
                rb.setFont(new Font("Arial", Font.PLAIN, 13));
                if (opt.equals(userVote)) {
                    rb.setSelected(true);
                    rb.setForeground(new Color(39, 174, 96)); // highlight their choice
                } else {
                    rb.setForeground(new Color(150, 150, 150));
                }
                rb.setEnabled(false);
                rPanel.add(rb);
            } else {
                JRadioButton rb = new JRadioButton(opt);
                rb.setBackground(Color.WHITE);
                rb.setFont(new Font("Arial", Font.PLAIN, 13));
                rb.setForeground(new Color(80, 80, 80));
                rb.setFocusPainted(false);
                bg.add(rb);
                rPanel.add(rb);
            }
            pollCardContainer.add(rPanel);
            pollCardContainer.add(Box.createVerticalStrut(5));
        }

        if (userVote == null) {
            JButton voteBtn = new JButton("Submit Vote");
            voteBtn.setBackground(new Color(155, 89, 182)); 
            voteBtn.setForeground(Color.WHITE);
            voteBtn.setFont(new Font("Arial", Font.BOLD, 12));
            voteBtn.setFocusPainted(false);
            voteBtn.setBorderPainted(false);
            voteBtn.setOpaque(true);
            
            voteBtn.addActionListener(e -> {
                if (!isLoggedIn()) {
                    JOptionPane.showMessageDialog(this, "Please login first to vote.", "Vote", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String selectedOpt = null;
                for (java.util.Enumeration<AbstractButton> buttons = bg.getElements(); buttons.hasMoreElements();) {
                    AbstractButton button = buttons.nextElement();
                    if (button.isSelected()) {
                        selectedOpt = button.getText();
                        break;
                    }
                }
                if (selectedOpt == null) {
                    JOptionPane.showMessageDialog(this, "Please select an option.", "Vote", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                pollRepository.saveVote(loggedInUser, selectedOpt);
                JOptionPane.showMessageDialog(this, "Vote submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshPollCard();
            });

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
            btnPanel.setBackground(Color.WHITE);
            btnPanel.add(voteBtn);
            pollCardContainer.add(btnPanel);
        } else {
            JLabel thxLbl = new JLabel("Thanks for voting!");
            thxLbl.setFont(new Font("Arial", Font.ITALIC, 12));
            thxLbl.setForeground(new Color(150, 150, 150));
            JPanel thxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
            thxPanel.setBackground(Color.WHITE);
            thxPanel.add(thxLbl);
            pollCardContainer.add(thxPanel);
        }
    }

    private JPanel buildCommentsPanel(Topic topic) {
        JPanel commentsContainer = new JPanel();
        commentsContainer.setLayout(new BoxLayout(commentsContainer, BoxLayout.Y_AXIS));
        commentsContainer.setBackground(Color.WHITE);
        commentsContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLbl = new JLabel("Comments (" + topic.getComments() + ")");
        titleLbl.setFont(new Font("Arial", Font.BOLD, 14));
        titleLbl.setForeground(new Color(100, 100, 100));
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLbl);
        commentsContainer.add(titlePanel);

        List<Comment> comments = commentRepository.getCommentsByTopicId(topic.getId());
        if (comments.isEmpty()) {
            JLabel noComm = new JLabel("No comments yet. Be the first to comment!");
            noComm.setFont(new Font("Arial", Font.ITALIC, 13));
            noComm.setForeground(new Color(170, 170, 170));
            JPanel temp = new JPanel(new FlowLayout(FlowLayout.LEFT));
            temp.setBackground(Color.WHITE);
            temp.add(noComm);
            commentsContainer.add(temp);
        } else {
            for (Comment c : comments) {
                JPanel item = new JPanel(new BorderLayout(5, 5));
                item.setBackground(new Color(250, 250, 250));
                item.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, new Color(230,230,230)),
                    new EmptyBorder(10, 10, 10, 10)
                ));

                JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                top.setBackground(new Color(250, 250, 250));
                JLabel authorLbl = new JLabel(c.getAuthorName());
                authorLbl.setFont(new Font("Arial", Font.BOLD, 12));
                authorLbl.setForeground(new Color(51, 51, 51));
                JLabel dateLbl = new JLabel(c.getDateStr());
                dateLbl.setFont(new Font("Arial", Font.PLAIN, 11));
                dateLbl.setForeground(new Color(150, 150, 150));
                top.add(authorLbl);
                top.add(dateLbl);

                JTextArea cText = new JTextArea(c.getContent());
                cText.setFont(new Font("Arial", Font.PLAIN, 13));
                cText.setForeground(new Color(80,80,80));
                cText.setEditable(false);
                cText.setBackground(new Color(250, 250, 250));
                cText.setLineWrap(true);
                cText.setWrapStyleWord(true);

                item.add(top, BorderLayout.NORTH);
                item.add(cText, BorderLayout.CENTER);
                commentsContainer.add(item);
            }
        }
        return commentsContainer;
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
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

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


