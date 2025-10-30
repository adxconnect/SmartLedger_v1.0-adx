package src.UI;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Modern theme constants and utilities for Finance Manager UI.
 * Provides a consistent, modern look across all components.
 */
public class ModernTheme {
    
    // ========== DARK MODE STATE ==========
    private static boolean isDarkMode = false;
    
    // ========== COLOR PALETTE ==========
    
    // Primary Colors
    public static final Color PRIMARY = new Color(67, 97, 238);        // Modern Blue
    public static final Color PRIMARY_DARK = new Color(48, 73, 191);   // Darker Blue
    public static final Color PRIMARY_LIGHT = new Color(140, 158, 255); // Light Blue
    
    // Accent Colors
    public static final Color ACCENT = new Color(255, 107, 107);        // Coral Red
    public static final Color SUCCESS = new Color(46, 213, 115);        // Green
    public static final Color WARNING = new Color(255, 184, 34);        // Orange
    public static final Color INFO = new Color(52, 172, 224);           // Cyan
    public static final Color DANGER = new Color(255, 71, 87);          // Red
    
    // Light Mode Colors
    private static final Color LIGHT_BACKGROUND = new Color(248, 249, 252);    // Off-white
    private static final Color LIGHT_SURFACE = new Color(255, 255, 255);       // White
    private static final Color LIGHT_TEXT_PRIMARY = new Color(33, 37, 41);     // Almost Black
    private static final Color LIGHT_TEXT_SECONDARY = new Color(108, 117, 125); // Gray
    private static final Color LIGHT_BORDER = new Color(222, 226, 230);        // Light Border
    
    // Dark Mode Colors
    private static final Color DARK_BACKGROUND = new Color(18, 18, 18);        // Almost Black
    private static final Color DARK_SURFACE = new Color(30, 30, 30);           // Dark Gray
    private static final Color DARK_TEXT_PRIMARY = new Color(255, 255, 255);   // White
    private static final Color DARK_TEXT_SECONDARY = new Color(170, 170, 170); // Light Gray
    private static final Color DARK_BORDER = new Color(60, 60, 60);            // Dark Border
    
    // Dynamic Colors (switch based on mode)
    public static Color BACKGROUND = LIGHT_BACKGROUND;
    public static Color SURFACE = LIGHT_SURFACE;
    public static Color CARD_BG = LIGHT_SURFACE;
    public static final Color SIDEBAR_BG = new Color(45, 52, 68);       // Dark Gray
    
    // Text Colors
    public static Color TEXT_PRIMARY = LIGHT_TEXT_PRIMARY;
    public static Color TEXT_SECONDARY = LIGHT_TEXT_SECONDARY;
    public static final Color TEXT_LIGHT = new Color(173, 181, 189);    // Light Gray
    public static final Color TEXT_WHITE = new Color(255, 255, 255);    // White
    
    // Border & Shadow
    public static Color BORDER = LIGHT_BORDER;
    public static final Color SHADOW = new Color(0, 0, 0, 15);          // Subtle Shadow
    public static final Color HOVER_OVERLAY = new Color(0, 0, 0, 5);    // Hover Effect
    
    // ========== DIMENSIONS ==========
    
    public static final int BORDER_RADIUS = 12;
    public static final int CARD_RADIUS = 16;
    public static final int BUTTON_RADIUS = 8;
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 16;
    public static final int PADDING_LARGE = 24;
    public static final int SHADOW_SIZE = 4;
    
    // ========== FONTS ==========
    
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_LOGO = new Font("Segoe UI", Font.BOLD, 28);
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Creates a modern rounded border with shadow effect
     */
    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            new RoundedBorder(CARD_RADIUS, BORDER),
            BorderFactory.createEmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        );
    }
    
    /**
     * Creates a modern button with rounded corners
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, PRIMARY, TEXT_WHITE, PRIMARY_DARK);
        return btn;
    }
    
    /**
     * Creates a secondary/outline button
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, SURFACE, PRIMARY, PRIMARY_LIGHT);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BUTTON_RADIUS, PRIMARY),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        return btn;
    }
    
    /**
     * Creates a success button (green)
     */
    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, SUCCESS, TEXT_WHITE, new Color(35, 170, 92));
        return btn;
    }
    
    /**
     * Creates a danger button (red)
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, DANGER, TEXT_WHITE, new Color(204, 57, 70));
        return btn;
    }
    
    /**
     * Creates a warning button (orange)
     */
    public static JButton createWarningButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, WARNING, TEXT_PRIMARY, new Color(204, 147, 27));
        return btn;
    }
    
    /**
     * Internal method to style buttons consistently
     */
    private static void styleButton(JButton btn, Color bg, Color fg, Color hoverBg) {
        btn.setFont(FONT_BUTTON);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Add hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hoverBg);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        
        // Override paint for rounded corners
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded background
                g2.setColor(btn.getBackground());
                g2.fillRoundRect(0, 0, btn.getWidth(), btn.getHeight(), BUTTON_RADIUS, BUTTON_RADIUS);
                
                // Draw text
                g2.setColor(btn.getForeground());
                g2.setFont(btn.getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (btn.getWidth() - fm.stringWidth(btn.getText())) / 2;
                int textY = (btn.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(btn.getText(), textX, textY);
                
                g2.dispose();
            }
        });
    }
    
    /**
     * Creates a modern styled card panel
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(createCardBorder());
        return panel;
    }
    
    /**
     * Creates a gradient panel
     */
    public static JPanel createGradientPanel(Color color1, Color color2) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS);
                g2d.dispose();
            }
        };
    }
    
    /**
     * Styles a text field with modern appearance
     */
    public static void styleTextField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(SURFACE);
        field.setOpaque(true);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BUTTON_RADIUS, BORDER),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setCaretColor(PRIMARY);
        field.setSelectionColor(PRIMARY_LIGHT);
        field.setSelectedTextColor(TEXT_PRIMARY);
    }
    
    /**
     * Styles a combo box with modern appearance
     */
    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(FONT_BODY);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(SURFACE);
        combo.setOpaque(true);
        combo.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BUTTON_RADIUS, BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        // Style the renderer
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(FONT_BODY);
                if (isSelected) {
                    setBackground(PRIMARY_LIGHT);
                    setForeground(TEXT_PRIMARY);
                } else {
                    setBackground(SURFACE);
                    setForeground(TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
    }
    
    /**
     * Styles a table with modern appearance
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(SURFACE);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER);
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        // Style header
        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(FONT_HEADER);
            table.getTableHeader().setForeground(TEXT_PRIMARY);
            table.getTableHeader().setBackground(BACKGROUND);
            table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER));
        }
    }
    
    /**
     * Custom rounded border
     */
    static class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color color;
        
        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = 2;
            return insets;
        }
    }
    
    /**
     * Creates an icon label with text (for menu items)
     */
    public static JLabel createIconLabel(String text, String emoji) {
        JLabel label = new JLabel(emoji + "  " + text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }
    
    /**
     * Adds a subtle shadow effect to a component
     */
    public static void addShadow(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SHADOW, 1),
            component.getBorder()
        ));
    }
    
    // ========== DARK MODE METHODS ==========
    
    /**
     * Toggles between light and dark mode
     */
    public static void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        updateColors();
    }
    
    /**
     * Check if dark mode is enabled
     */
    public static boolean isDarkMode() {
        return isDarkMode;
    }
    
    /**
     * Set dark mode state
     */
    public static void setDarkMode(boolean enabled) {
        isDarkMode = enabled;
        updateColors();
    }
    
    /**
     * Update all dynamic colors based on current mode
     */
    private static void updateColors() {
        if (isDarkMode) {
            BACKGROUND = DARK_BACKGROUND;
            SURFACE = DARK_SURFACE;
            CARD_BG = DARK_SURFACE;
            TEXT_PRIMARY = DARK_TEXT_PRIMARY;
            TEXT_SECONDARY = DARK_TEXT_SECONDARY;
            BORDER = DARK_BORDER;
        } else {
            BACKGROUND = LIGHT_BACKGROUND;
            SURFACE = LIGHT_SURFACE;
            CARD_BG = LIGHT_SURFACE;
            TEXT_PRIMARY = LIGHT_TEXT_PRIMARY;
            TEXT_SECONDARY = LIGHT_TEXT_SECONDARY;
            BORDER = LIGHT_BORDER;
        }
    }
    
    /**
     * Creates a round dark mode toggle button
     */
    public static JButton createDarkModeToggleButton() {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(44, 44));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Toggle Dark Mode");
        
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw circular background
                if (isDarkMode) {
                    g2.setColor(new Color(255, 193, 7)); // Yellow for dark mode
                } else {
                    g2.setColor(new Color(45, 52, 68)); // Dark for light mode
                }
                g2.fillOval(4, 4, 36, 36);
                
                // Draw icon (sun/moon)
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                String icon = isDarkMode ? "☀" : "☾";
                FontMetrics fm = g2.getFontMetrics();
                int x = (c.getWidth() - fm.stringWidth(icon)) / 2;
                int y = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(icon, x, y);
                
                g2.dispose();
            }
        });
        
        return btn;
    }
}
