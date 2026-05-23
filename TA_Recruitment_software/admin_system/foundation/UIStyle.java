package TA_Recruitment_software.admin_system.foundation;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Centralized modern FLAT UI styling for the JobHere platform.
 * Pure flat design — no gradients, consistent white inputs, rounded corners.
 * Full Chinese (中文) compatibility via platform-appropriate fonts.
 */
public final class UIStyle {

    private UIStyle() {}

    // ==================== Flat Color Palette ====================
    /** 主色 — 柔和绿色 */
    public static final Color PRIMARY       = new Color(46, 160, 67);
    /** 主色深色 */
    public static final Color PRIMARY_DARK  = new Color(36, 130, 54);
    /** 主色极浅背景 */
    public static final Color PRIMARY_BG    = new Color(230, 248, 233);

    /** 强调蓝 */
    public static final Color ACCENT_BLUE   = new Color(52, 120, 220);
    /** 强调蓝深色 */
    public static final Color ACCENT_BLUE_DARK = new Color(40, 100, 190);

    /** 文字 — 主要 */
    public static final Color TEXT          = new Color(30, 30, 30);
    /** 文字 — 次要/placeholder */
    public static final Color TEXT_HINT     = new Color(150, 150, 155);
    /** 文字 — 禁用 */
    public static final Color TEXT_DISABLED = new Color(180, 180, 185);

    /** 表面 — 页面底色 */
    public static final Color SURFACE       = new Color(245, 245, 248);
    /** 表面 — 卡片/输入框白 */
    public static final Color WHITE         = Color.WHITE;
    /** 悬停 */
    public static final Color HOVER         = new Color(240, 242, 245);

    /** 边框 */
    public static final Color BORDER        = new Color(210, 214, 220);
    /** 边框 — 聚焦 */
    public static final Color BORDER_FOCUS  = new Color(46, 160, 67);

    /** 语义 — 错误 */
    public static final Color DANGER        = new Color(220, 60, 60);
    /** 语义 — 成功 */
    public static final Color SUCCESS       = new Color(46, 160, 67);
    /** 语义 — 警告 */
    public static final Color WARNING       = new Color(240, 150, 20);

    // Keep old names for backward compat
    public static final Color TEXT_PRIMARY = TEXT;
    public static final Color TEXT_SECONDARY = TEXT_HINT;
    public static final Color BG_PAGE = SURFACE;
    public static final Color BG_CARD = WHITE;
    public static final Color BG_HOVER = HOVER;
    public static final Color BORDER_FOCUS_OLD = BORDER_FOCUS;
    public static final Color INFO = ACCENT_BLUE;

    // ==================== Rounded Border ====================
    private static final int RADIUS = 10;

    /** 圆角边框 — 标准输入框 */
    public static Border inputBorder() {
        return new CompoundBorder(
            new RoundedBorder(BORDER, RADIUS),
            new EmptyBorder(7, 12, 7, 12)
        );
    }

    /** 圆角边框 — 聚焦 */
    public static Border inputBorderFocused() {
        return new CompoundBorder(
            new RoundedBorder(BORDER_FOCUS, RADIUS),
            new EmptyBorder(7, 12, 7, 12)
        );
    }

    /** 自定义圆角线条边框 */
    private static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        RoundedBorder(Color c, int r) { this.color = c; this.radius = r; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }

    // ==================== Font System (中文兼容) ====================
    public static String getChineseFontFamily() {
        String os = System.getProperty("os.name", "").toLowerCase();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (os.contains("win")) {
            for (String f : new String[]{"Microsoft YaHei","微软雅黑","SimHei","SimSun"})
                for (String a : ge.getAvailableFontFamilyNames())
                    if (a.equals(f)) return f;
            return "Arial";
        }
        if (os.contains("mac")) return "PingFang SC";
        return "Noto Sans CJK SC";
    }

    public static final Font FONT_BODY;
    public static final Font FONT_BODY_BOLD;
    public static final Font FONT_SMALL;
    public static final Font FONT_TITLE;
    public static final Font FONT_HEADING;
    public static final Font FONT_BUTTON;
    public static final String EMOJI_FONT;

    static {
        String cjk = getChineseFontFamily();
        FONT_BODY       = new Font(cjk, Font.PLAIN, 14);
        FONT_BODY_BOLD  = new Font(cjk, Font.BOLD, 14);
        FONT_SMALL      = new Font(cjk, Font.PLAIN, 12);
        FONT_TITLE      = new Font(cjk, Font.BOLD, 20);
        FONT_HEADING    = new Font(cjk, Font.BOLD, 24);
        FONT_BUTTON     = new Font(cjk, Font.BOLD, 14);
        EMOJI_FONT      = System.getProperty("os.name","").toLowerCase().contains("mac")
                            ? "Apple Color Emoji" : "Segoe UI Emoji";
    }

    // ==================== Global Setup ====================
    public static void setupGlobalStyle() {
        // Use system L&F for native flat feel — no Nimbus gradients
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Flat overrides
        UIManager.put("Panel.background", SURFACE);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("Button.focus", new Color(0,0,0,0));
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.font", FONT_BUTTON);

        // ScrollBar — thin & flat
        UIManager.put("ScrollBar.thumb", new Color(195,200,208));
        UIManager.put("ScrollBar.track", SURFACE);
        UIManager.put("ScrollBar.width", 10);

        // Table
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Table.background", WHITE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", PRIMARY_BG);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("Table.rowHeight", 36);

        // Labels
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("Label.foreground", TEXT);

        // Text components — ALL white background
        UIManager.put("TextField.background", WHITE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("TextArea.background", WHITE);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("TextArea.font", FONT_BODY);
        UIManager.put("ComboBox.background", WHITE);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.font", FONT_BODY);

        // ToolTip
        UIManager.put("ToolTip.font", FONT_SMALL);
        UIManager.put("ToolTip.background", new Color(50,50,55));
        UIManager.put("ToolTip.foreground", Color.WHITE);
    }

    // ==================== Button Factories (pure flat) ====================

    private static JButton flatButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(FONT_BUTTON);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Robust text measurement using a lightweight canvas
        FontMetrics fm = new Canvas().getFontMetrics(FONT_BUTTON);
        int pad = 36;
        int w = Math.max(fm.stringWidth(text) + pad, 110);
        int h = 34;
        b.setPreferredSize(new Dimension(w, h));
        b.setMinimumSize(new Dimension(w, h));
        // Hover: subtle brightness shift
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(lightenOrDarken(bg, fg == Color.WHITE ? -0.08f : 0.06f));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(bg);
            }
        });
        return b;
    }

    private static Color lightenOrDarken(Color c, float amount) {
        int r = clamp((int)(c.getRed()   * (1 + amount) + (amount > 0 ? 40 : -30)));
        int g = clamp((int)(c.getGreen() * (1 + amount) + (amount > 0 ? 40 : -30)));
        int b = clamp((int)(c.getBlue()  * (1 + amount) + (amount > 0 ? 40 : -30)));
        return new Color(r, g, b);
    }
    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public static JButton createPrimaryButton(String text) {
        return flatButton(text, PRIMARY, Color.WHITE);
    }
    public static JButton createSecondaryButton(String text) {
        return flatButton(text, new Color(218, 222, 228), TEXT);
    }
    public static JButton createDangerButton(String text) {
        return flatButton(text, DANGER, Color.WHITE);
    }
    public static JButton createAccentButton(String text) {
        return flatButton(text, ACCENT_BLUE, Color.WHITE);
    }

    // ==================== Input Factories (white bg, rounded) ====================

    public static JTextField createTextField(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(FONT_BODY);
        f.setBackground(WHITE);
        f.setForeground(TEXT);
        f.setBorder(inputBorder());
        // On focus: green border
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(inputBorderFocused());
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(inputBorder());
            }
        });
        return f;
    }

    public static JTextField createTextField() { return createTextField(20); }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField f = new JPasswordField(columns);
        f.setFont(FONT_BODY);
        f.setBackground(WHITE);
        f.setForeground(TEXT);
        f.setBorder(inputBorder());
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(inputBorderFocused());
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(inputBorder());
            }
        });
        return f;
    }

    public static JPasswordField createPasswordField() { return createPasswordField(20); }

    /** Read-only field — SAME white background as editable ones, just non-editable */
    public static JTextField createReadOnlyField(String text) {
        JTextField f = createTextField(20);
        f.setText(text != null ? text : "");
        f.setEditable(false);
        f.setBackground(WHITE);
        f.setForeground(TEXT);
        return f;
    }

    /** TextArea in a flat scroll pane */
    public static JScrollPane createTextAreaScroll(String placeholder, JTextArea area) {
        area.setText(placeholder);
        area.setFont(FONT_BODY);
        area.setForeground(TEXT_HINT);
        area.setBackground(WHITE);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));

        area.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (area.getText().equals(placeholder)) {
                    area.setText("");
                    area.setForeground(TEXT);
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (area.getText().trim().isEmpty()) {
                    area.setText(placeholder);
                    area.setForeground(TEXT_HINT);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(600, 100));
        scroll.setBorder(new CompoundBorder(
            new RoundedBorder(BORDER, RADIUS),
            new EmptyBorder(0, 0, 0, 0)
        ));
        styleScrollPane(scroll);
        return scroll;
    }

    // ==================== ScrollPane ====================
    public static void styleScrollPane(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null);
        scroll.setBackground(SURFACE);
        scroll.getViewport().setBackground(WHITE);
    }

    // ==================== Table ====================
    public static JTable createStyledTable(javax.swing.table.TableModel model) {
        JTable table = new JTable(model);
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setGridColor(BORDER);
        table.setSelectionBackground(PRIMARY_BG);
        table.setSelectionForeground(TEXT);
        table.getTableHeader().setFont(FONT_BODY_BOLD);
        table.getTableHeader().setBackground(WHITE);
        table.getTableHeader().setForeground(TEXT);
        table.setIntercellSpacing(new Dimension(0, 0));
        return table;
    }

    public static JScrollPane wrapTableInScrollPane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        styleScrollPane(scroll);
        scroll.setBorder(new RoundedBorder(BORDER, RADIUS));
        scroll.getViewport().setBackground(WHITE);
        return scroll;
    }

    // ==================== Labels ====================
    public static JLabel createSectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(PRIMARY);
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        return l;
    }

    public static JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text + ":");
        l.setFont(FONT_BODY_BOLD);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel createHintLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_HINT);
        return l;
    }

    public static JLabel createTipLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY_BOLD);
        l.setForeground(PRIMARY);
        l.setBorder(new EmptyBorder(10, 12, 6, 12));
        return l;
    }

    // ==================== Layout helpers ====================
    public static EmptyBorder pagePadding() {
        return new EmptyBorder(20, 30, 20, 30);
    }

    public static CompoundBorder cardBorder() {
        return new CompoundBorder(
            new RoundedBorder(BORDER, RADIUS),
            new EmptyBorder(16, 20, 16, 20)
        );
    }

    public static JPanel createPageHeader(String title, Color accentColor) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(WHITE);
        header.setBorder(new EmptyBorder(20, 30, 0, 30));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_HEADING);
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel accentLine = new JPanel();
        accentLine.setBackground(accentColor != null ? accentColor : PRIMARY);
        accentLine.setPreferredSize(new Dimension(60, 3));
        accentLine.setMaximumSize(new Dimension(60, 3));
        accentLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel divider = new JPanel();
        divider.setBackground(BORDER);
        divider.setPreferredSize(new Dimension(Short.MAX_VALUE, 1));
        divider.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(8));
        header.add(accentLine);
        header.add(divider);

        return header;
    }

    /** Create combo box with flat style */
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(WHITE);
        cb.setForeground(TEXT);
        return cb;
    }
}
