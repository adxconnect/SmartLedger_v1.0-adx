package src.UI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

import src.UI.ModernIcons.IconType;

import javax.swing.plaf.*;

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
    private static final Color DARK_BORDER = new Color(100, 100, 100);         // Lighter Border for visibility
    
    // Table Colors - Light Mode
    private static final Color LIGHT_TABLE_BG = new Color(255, 255, 255);      // White
    private static final Color LIGHT_TABLE_ROW_EVEN = new Color(249, 250, 251); // Very light gray
    private static final Color LIGHT_TABLE_ROW_ODD = new Color(255, 255, 255);  // White
    private static final Color LIGHT_TABLE_HEADER = new Color(243, 244, 246);   // Light gray
    private static final Color LIGHT_TABLE_BORDER = new Color(229, 231, 235);   // Border gray
    private static final Color LIGHT_TABLE_TEXT = new Color(33, 37, 41);        // Dark text
    private static final Color LIGHT_TABLE_SELECTION = new Color(219, 234, 254); // Light blue
    
    // Table Colors - Dark Mode
    private static final Color DARK_TABLE_BG = new Color(30, 30, 30);          // Dark background
    private static final Color DARK_TABLE_ROW_EVEN = new Color(35, 35, 35);    // Slightly lighter
    private static final Color DARK_TABLE_ROW_ODD = new Color(30, 30, 30);     // Darker
    private static final Color DARK_TABLE_HEADER = new Color(40, 40, 40);      // Header dark
    private static final Color DARK_TABLE_BORDER = new Color(50, 50, 50);      // Border dark
    private static final Color DARK_TABLE_TEXT = new Color(255, 255, 255);     // White text
    private static final Color DARK_TABLE_SELECTION = new Color(60, 60, 60);   // Selection gray
    
    // Dynamic Colors (switch based on mode)
    public static Color BACKGROUND = LIGHT_BACKGROUND;
    public static Color SURFACE = LIGHT_SURFACE;
    public static Color CARD_BG = LIGHT_SURFACE;
    public static final Color SIDEBAR_BG = new Color(45, 52, 68);       // Dark Gray
    
    // Dynamic Table Colors
    public static Color TABLE_BG = LIGHT_TABLE_BG;
    public static Color TABLE_ROW_EVEN = LIGHT_TABLE_ROW_EVEN;
    public static Color TABLE_ROW_ODD = LIGHT_TABLE_ROW_ODD;
    public static Color TABLE_HEADER = LIGHT_TABLE_HEADER;
    public static Color TABLE_BORDER = LIGHT_TABLE_BORDER;
    public static Color TABLE_TEXT = LIGHT_TABLE_TEXT;
    public static Color TABLE_SELECTION = LIGHT_TABLE_SELECTION;
    
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
    public static final int BUTTON_RADIUS = 25;
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 16;
    public static final int PADDING_LARGE = 24;
    public static final int SHADOW_SIZE = 4;
    
    // ========== FONTS ==========
    // ========== FONTS ==========

public static final Font FONT_TITLE = new Font("Poppins", Font.BOLD, 24);
public static final Font FONT_SUBTITLE = new Font("Poppins", Font.BOLD, 18);
public static final Font FONT_HEADER = new Font("Poppins", Font.BOLD, 16);
public static final Font FONT_BODY = new Font("Poppins", Font.PLAIN, 14);
public static final Font FONT_SMALL = new Font("Poppins", Font.PLAIN, 12);
public static final Font FONT_BUTTON = new Font("Poppins", Font.BOLD, 14);
public static final Font FONT_LOGO = new Font("Poppins", Font.BOLD, 28);
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
        btn.putClientProperty("buttonType", "primary");
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
        btn.putClientProperty("buttonType", "secondary");
        return btn;
    }
    
    /**
     * Creates a success button (green)
     */
    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, SUCCESS, TEXT_WHITE, new Color(35, 170, 92));
        btn.putClientProperty("buttonType", "success");
        return btn;
    }
    
    /**
     * Creates a danger button (red)
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, DANGER, TEXT_WHITE, new Color(204, 57, 70));
        btn.putClientProperty("buttonType", "danger");
        return btn;
    }
    
    /**
     * Creates a warning button (orange)
     */
    public static JButton createWarningButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, WARNING, TEXT_PRIMARY, new Color(204, 147, 27));
        btn.putClientProperty("buttonType", "warning");
        return btn;
    }
    
    /**
     * Refreshes button colors based on its stored button type.
     * Call this after theme changes to update button appearance.
     */
    public static void refreshButtonColors(JButton btn) {
        if (btn == null) return;
        
        String buttonType = (String) btn.getClientProperty("buttonType");
        if (buttonType == null) return;
        
        Color newBg, newFg, newHoverBg;
        
        switch (buttonType) {
            case "primary":
                newBg = PRIMARY;
                newFg = TEXT_WHITE;
                newHoverBg = PRIMARY_DARK;
                break;
            case "secondary":
                newBg = SURFACE;
                newFg = PRIMARY;
                newHoverBg = PRIMARY_LIGHT;
                break;
            case "success":
                newBg = SUCCESS;
                newFg = TEXT_WHITE;
                newHoverBg = new Color(35, 170, 92);
                break;
            case "danger":
                newBg = DANGER;
                newFg = TEXT_WHITE;
                newHoverBg = new Color(204, 57, 70);
                break;
            case "warning":
                newBg = WARNING;
                newFg = TEXT_PRIMARY;
                newHoverBg = new Color(204, 147, 27);
                break;
            default:
                return;
        }
        
        // Update button colors
        btn.setBackground(newBg);
        btn.setForeground(newFg);
        
        // Update client properties for mouse listeners
        btn.putClientProperty("originalBg", newBg);
        btn.putClientProperty("originalFg", newFg);
        btn.putClientProperty("originalHoverBg", newHoverBg);
        
        // Force repaint
        btn.repaint();
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
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        
        // Store original colors as client properties for theme switching
        btn.putClientProperty("originalBg", bg);
        btn.putClientProperty("originalFg", fg);
        btn.putClientProperty("originalHoverBg", hoverBg);
        
        // Track hover state for shadow effect
        final boolean[] isHovered = {false};
        
        // Add hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Get current hover color from client property
                Color currentHoverBg = (Color) btn.getClientProperty("originalHoverBg");
                if (currentHoverBg != null) {
                    btn.setBackground(currentHoverBg);
                }
                isHovered[0] = true;
                btn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Get current background color from client property
                Color currentBg = (Color) btn.getClientProperty("originalBg");
                if (currentBg != null) {
                    btn.setBackground(currentBg);
                }
                isHovered[0] = false;
                btn.repaint();
            }
        });
        
        // Override paint for rounded corners with shadow
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow for hovering effect (theme-aware)
                int shadowOffset = isHovered[0] ? 8 : 6;
                int shadowBlur = isHovered[0] ? 16 : 12;
                
                // Use theme-aware shadow color
                // Dark mode: use lighter shadow for visibility
                // Light mode: use darker shadow for depth
                int shadowBase = isDarkMode ? 255 : 0;
                int shadowAlphaMultiplier = isDarkMode ? 30 : 20;
                
                // Draw multiple layers for shadow blur effect
                for (int i = 0; i < shadowBlur; i++) {
                    int alpha = (int) (shadowAlphaMultiplier * (1 - (i / (double) shadowBlur)));
                    g2.setColor(new Color(shadowBase, shadowBase, shadowBase, alpha));
                    g2.fillRoundRect(
                        i/2, 
                        i/2 + shadowOffset/2, 
                        btn.getWidth() - i, 
                        btn.getHeight() - i, 
                        BUTTON_RADIUS + i/2, 
                        BUTTON_RADIUS + i/2
                    );
                }
                
                // Draw rounded background - ALWAYS use client property for reliability
                Color bgColor = (Color) btn.getClientProperty("originalBg");
                if (bgColor == null) {
                    bgColor = btn.getBackground(); // Fallback to getBackground()
                }
                if (bgColor != null) {
                    g2.setColor(bgColor);
                    g2.fillRoundRect(0, 0, btn.getWidth(), btn.getHeight(), BUTTON_RADIUS, BUTTON_RADIUS);
                }
                
                // Draw text - ALWAYS use client property for reliability
                Color fgColor = (Color) btn.getClientProperty("originalFg");
                if (fgColor == null) {
                    fgColor = btn.getForeground(); // Fallback to getForeground()
                }
                if (fgColor != null) {
                    g2.setColor(fgColor);
                    g2.setFont(btn.getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = (btn.getWidth() - fm.stringWidth(btn.getText())) / 2;
                    int textY = (btn.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(btn.getText(), textX, textY);
                }
                
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
     * Styles a scroll pane with modern slim scrollbars
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(null);
        scrollPane.setBackground(SURFACE);
        scrollPane.getViewport().setBackground(SURFACE);
        
        // Style vertical scrollbar
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            private final int THUMB_WIDTH = 8;
            private final int THUMB_RADIUS = 4;
            
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(150, 150, 150, 100);
                this.thumbDarkShadowColor = new Color(150, 150, 150, 100);
                this.thumbHighlightColor = new Color(150, 150, 150, 150);
                this.trackColor = SURFACE;
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
            
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 0)); // Transparent track
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }
            
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Center the thumb and make it slim
                int x = thumbBounds.x + (thumbBounds.width - THUMB_WIDTH) / 2;
                int y = thumbBounds.y + 2;
                int width = THUMB_WIDTH;
                int height = thumbBounds.height - 4;
                
                // Draw rounded thumb
                if (isDragging) {
                    g2.setColor(new Color(100, 100, 100, 180));
                } else if (isThumbRollover()) {
                    g2.setColor(new Color(120, 120, 120, 150));
                } else {
                    g2.setColor(new Color(150, 150, 150, 120));
                }
                
                g2.fillRoundRect(x, y, width, height, THUMB_RADIUS, THUMB_RADIUS);
                g2.dispose();
            }
            
            @Override
            protected void setThumbBounds(int x, int y, int width, int height) {
                super.setThumbBounds(x, y, width, height);
                scrollbar.repaint();
            }
        });
        
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        
        // Style horizontal scrollbar (only if it exists)
        if (scrollPane.getHorizontalScrollBar() != null) {
            scrollPane.getHorizontalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            private final int THUMB_HEIGHT = 8;
            private final int THUMB_RADIUS = 4;
            
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(150, 150, 150, 100);
                this.thumbDarkShadowColor = new Color(150, 150, 150, 100);
                this.thumbHighlightColor = new Color(150, 150, 150, 150);
                this.trackColor = SURFACE;
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
            
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 0)); // Transparent track
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }
            
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Center the thumb and make it slim
                int x = thumbBounds.x + 2;
                int y = thumbBounds.y + (thumbBounds.height - THUMB_HEIGHT) / 2;
                int width = thumbBounds.width - 4;
                int height = THUMB_HEIGHT;
                
                // Draw rounded thumb
                if (isDragging) {
                    g2.setColor(new Color(100, 100, 100, 180));
                } else if (isThumbRollover()) {
                    g2.setColor(new Color(120, 120, 120, 150));
                } else {
                    g2.setColor(new Color(150, 150, 150, 120));
                }
                
                g2.fillRoundRect(x, y, width, height, THUMB_RADIUS, THUMB_RADIUS);
                g2.dispose();
            }
            
            @Override
            protected void setThumbBounds(int x, int y, int width, int height) {
                super.setThumbBounds(x, y, width, height);
                scrollbar.repaint();
            }
            });
            
            scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
            scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 12));
        }
    }
    
    /**
     * Styles a combo box with modern appearance and custom scrollbar
     */
    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Poppins", Font.PLAIN, 14));
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(SURFACE);
        // Only set height if width is not already set
        if (combo.getPreferredSize().width == 0) {
            combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, 42));
        }
        
        // Custom UI for fully rounded appearance with modern popup
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        // Draw modern chevron arrow
                        g2.setColor(TEXT_PRIMARY);
                        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        int size = 6;
                        int x = getWidth() / 2;
                        int y = getHeight() / 2 - 2;
                        g2.drawLine(x - size, y, x, y + size);
                        g2.drawLine(x, y + size, x + size, y);
                        g2.dispose();
                    }
                };
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setContentAreaFilled(false);
                button.setFocusPainted(false);
                button.setPreferredSize(new Dimension(40, 40));
                return button;
            }
            
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // Don't paint background - let the combo box background show through
            }
            
            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                @SuppressWarnings("unchecked")
                ListCellRenderer<Object> renderer = (ListCellRenderer<Object>) comboBox.getRenderer();
                Component c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
                c.setFont(new Font("Poppins", Font.PLAIN, 14));
                
                // Ensure proper bounds for text rendering
                currentValuePane.paintComponent(g, c, comboBox, bounds.x, bounds.y, bounds.width, bounds.height, false);
            }
            
            @Override
            protected ComboPopup createPopup() {
                BasicComboPopup popup = (BasicComboPopup) super.createPopup();
                
                // Modern rounded border with shadow effect
                popup.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(12, BORDER),
                    BorderFactory.createEmptyBorder(8, 0, 8, 0)
                ));
                
                // Style the popup list
                JList<?> list = popup.getList();
                list.setBackground(SURFACE);
                list.setForeground(TEXT_PRIMARY);
                list.setFont(new Font("Poppins", Font.PLAIN, 14));
                list.setSelectionBackground(PRIMARY);
                list.setSelectionForeground(TEXT_WHITE);
                list.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
                list.setFixedCellHeight(40);
                
                // Apply modern scrollbar to the popup
                JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
                ModernTheme.styleScrollPane(scrollPane);
                
                return popup;
            }
        });
        
        combo.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BUTTON_RADIUS, BORDER),
            BorderFactory.createEmptyBorder(10, 16, 10, 10)
        ));
        
        // Style the renderer with better padding and modern look
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(new Font("Poppins", Font.PLAIN, 14));
                
                if (index == -1) {
                    // Selected item in the combo box
                    label.setForeground(TEXT_PRIMARY);
                    label.setBackground(SURFACE);
                } else if (isSelected) {
                    // Selected item in dropdown
                    label.setBackground(PRIMARY);
                    label.setForeground(TEXT_WHITE);
                } else {
                    // Normal items in dropdown
                    label.setBackground(SURFACE);
                    label.setForeground(TEXT_PRIMARY);
                }
                
                label.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
                label.setOpaque(true);
                return label;
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
        table.setShowVerticalLines(true);  // Show vertical lines to separate columns
        table.setIntercellSpacing(new Dimension(1, 0));  // Add 1px spacing between columns
        
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
            // Use thicker stroke and adjust coordinates for complete rounded borders
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x + 1, y + 1, width - 2, height - 2, radius, radius);
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
     * Creates a bold placeholder label that adapts to theme
     */
    public static JLabel createPlaceholderLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(FONT_HEADER); // Bold font for prominence
        // Use primary text color but make it bold for visibility
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
            
            // Update table colors for dark mode
            TABLE_BG = DARK_TABLE_BG;
            TABLE_ROW_EVEN = DARK_TABLE_ROW_EVEN;
            TABLE_ROW_ODD = DARK_TABLE_ROW_ODD;
            TABLE_HEADER = DARK_TABLE_HEADER;
            TABLE_BORDER = DARK_TABLE_BORDER;
            TABLE_TEXT = DARK_TABLE_TEXT;
            TABLE_SELECTION = DARK_TABLE_SELECTION;
        } else {
            BACKGROUND = LIGHT_BACKGROUND;
            SURFACE = LIGHT_SURFACE;
            CARD_BG = LIGHT_SURFACE;
            TEXT_PRIMARY = LIGHT_TEXT_PRIMARY;
            TEXT_SECONDARY = LIGHT_TEXT_SECONDARY;
            BORDER = LIGHT_BORDER;
            
            // Update table colors for light mode
            TABLE_BG = LIGHT_TABLE_BG;
            TABLE_ROW_EVEN = LIGHT_TABLE_ROW_EVEN;
            TABLE_ROW_ODD = LIGHT_TABLE_ROW_ODD;
            TABLE_HEADER = LIGHT_TABLE_HEADER;
            TABLE_BORDER = LIGHT_TABLE_BORDER;
            TABLE_TEXT = LIGHT_TABLE_TEXT;
            TABLE_SELECTION = LIGHT_TABLE_SELECTION;
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
                    g2.setColor(new Color(255, 193, 7)); // Yellow for dark mode (sun)
                } else {
                    g2.setColor(new Color(45, 52, 68)); // Dark for light mode (moon)
                }
                g2.fillOval(4, 4, 36, 36);
                
                // Draw icon programmatically
                g2.setColor(Color.WHITE);
                int centerX = c.getWidth() / 2;
                int centerY = c.getHeight() / 2;
                
                if (isDarkMode) {
                    // Draw sun icon
                    // Center circle
                    g2.fillOval(centerX - 6, centerY - 6, 12, 12);
                    // Sun rays (8 rays)
                    g2.setStroke(new BasicStroke(2f));
                    for (int i = 0; i < 8; i++) {
                        double angle = Math.PI * 2 * i / 8;
                        int x1 = centerX + (int)(Math.cos(angle) * 10);
                        int y1 = centerY + (int)(Math.sin(angle) * 10);
                        int x2 = centerX + (int)(Math.cos(angle) * 15);
                        int y2 = centerY + (int)(Math.sin(angle) * 15);
                        g2.drawLine(x1, y1, x2, y2);
                    }
                } else {
                    // Draw moon icon (crescent)
                    g2.fillOval(centerX - 8, centerY - 8, 16, 16);
                    g2.setColor(new Color(45, 52, 68));
                    g2.fillOval(centerX - 4, centerY - 8, 16, 16);
                }
                
                g2.dispose();
            }
        });
        
        return btn;
    }
    // --- PASTE THIS NEW METHOD INSIDE YOUR ModernTheme.java FILE ---

/**
 * Styles a JToggleButton to be used in the sidebar with ultra-modern design.
 * @param button The button to style.
 * @param iconType The icon to use (can be null if just refreshing theme).
 */
public static void styleSidebarButton(JToggleButton button, ModernIcons.IconType iconType) {
    button.setFont(new Font(FONT_BODY.getFamily(), Font.PLAIN, 14));
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setOpaque(false);
    button.setHorizontalAlignment(SwingConstants.LEFT);
    button.setVerticalAlignment(SwingConstants.CENTER);
    button.setVerticalTextPosition(SwingConstants.CENTER);
    button.setHorizontalTextPosition(SwingConstants.RIGHT);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    button.setMinimumSize(new Dimension(200, 44));
    button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
    button.setPreferredSize(new Dimension(200, 44));
    button.setIconTextGap(12);

    // Store the icon type
    if (iconType != null) {
        button.putClientProperty("iconType", iconType);
    }
    
    // Custom painting for modern gradient effect
    button.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI() {
        @Override
        public void paint(Graphics g, JComponent c) {
            JToggleButton btn = (JToggleButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = btn.getWidth();
            int height = btn.getHeight();
            int arc = 12; // Pill-shaped rounded corners
            
            if (btn.isSelected()) {
                // Selected state - Modern gradient blue background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(67, 97, 238),
                    width, 0, new Color(85, 115, 255)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(5, 2, width - 10, height - 4, arc, arc);
                
                // Add subtle inner glow
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(6, 3, width - 12, height / 2 - 2, arc, arc);
                
            } else {
                // Hover state
                if (btn.getModel().isRollover()) {
                    g2.setColor(isDarkMode ? new Color(55, 55, 55, 180) : new Color(240, 242, 245, 200));
                    g2.fillRoundRect(5, 2, width - 10, height - 4, arc, arc);
                }
            }
            
            g2.dispose();
            super.paint(g, c);
        }
    });
    
    // Update button appearance based on selection
    updateSidebarButtonState(button);
    
    // Add listener to update appearance when selection changes
    if (button.getClientProperty("sidebarListenerAttached") == null) {
        button.addItemListener(e -> updateSidebarButtonState(button));
        button.putClientProperty("sidebarListenerAttached", Boolean.TRUE);
    }
}

/**
 * Forces a sidebar button to refresh its icon/text colors without reattaching listeners.
 */
public static void refreshSidebarButton(JToggleButton button) {
    updateSidebarButtonState(button);
}

/**
 * Updates sidebar button appearance based on its selection state
 */
private static void updateSidebarButtonState(JToggleButton button) {
    ModernIcons.IconType iconType = (ModernIcons.IconType) button.getClientProperty("iconType");
    
    if (button.isSelected()) {
        // Selected state - Pure white text and icon on gradient blue
        button.setForeground(new Color(255, 255, 255)); // Pure white
        
        if (iconType != null) {
            // Larger white icon for emphasis (24px)
            Icon selectedIcon = ModernIcons.create(iconType, new Color(255, 255, 255), 24);
            button.setIcon(selectedIcon);
        }
        
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        
    } else {
        // Normal state - High contrast colors for visibility
        // Light mode: Dark gray text/icons
        // Dark mode: Very light gray text/icons
        Color normalColor = isDarkMode ? new Color(220, 220, 220) : new Color(60, 60, 60);
        button.setForeground(normalColor);
        
        if (iconType != null) {
            // Standard size icon with high visibility (22px)
            Icon normalIcon = ModernIcons.create(iconType, normalColor, 22);
            button.setIcon(normalIcon);
        }
        
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
    }
    
    button.repaint();
}

    /**
     * Styles a JTabbedPane with modern rounded tabs
     */
    public static void styleTabbedPane(JTabbedPane tabbedPane) {
        tabbedPane.setFont(FONT_BODY);
        tabbedPane.setBackground(BACKGROUND);
        tabbedPane.setForeground(TEXT_PRIMARY);
        
        // Remove default borders
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Use a custom UI for modern rounded tabs
        tabbedPane.setUI(new BasicTabbedPaneUI() {
            private final int ARC_SIZE = 15;
            private final int TAB_HEIGHT = 46;  // Increased height for better text alignment
            private final int H_GAP = 8;
            
            @Override
            protected void installDefaults() {
                super.installDefaults();
                // Increased padding for better text alignment
                tabInsets = new Insets(10, 24, 10, 24);
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
                tabAreaInsets = new Insets(5, 10, 5, 10);
                contentBorderInsets = new Insets(10, 0, 0, 0);
            }
            
            @Override
            protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Paint tab area background
                g2.setColor(BACKGROUND);
                g2.fillRect(0, 0, tabPane.getWidth(), calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight));
                
                super.paintTabArea(g, tabPlacement, selectedIndex);
                g2.dispose();
            }
            
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                             int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Add horizontal gap between tabs
                int adjustedX = x + (tabIndex > 0 ? H_GAP / 2 : 0);
                int adjustedW = w - (tabIndex < tabPane.getTabCount() - 1 ? H_GAP : H_GAP / 2);
                
                if (isSelected) {
                    // Selected tab - modern primary color with gradient
                    GradientPaint gradient = new GradientPaint(
                        adjustedX, y, PRIMARY,
                        adjustedX, y + h, PRIMARY_DARK
                    );
                    g2.setPaint(gradient);
                    g2.fillRoundRect(adjustedX, y, adjustedW, h, ARC_SIZE, ARC_SIZE);
                    
                    // Add subtle highlight on top
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(adjustedX, y, adjustedW, h / 2, ARC_SIZE, ARC_SIZE);
                } else {
                    // Unselected tab - surface color with subtle shadow
                    g2.setColor(SURFACE);
                    g2.fillRoundRect(adjustedX, y + 2, adjustedW, h - 2, ARC_SIZE, ARC_SIZE);
                    
                    // Add border for unselected tabs
                    g2.setColor(BORDER);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(adjustedX, y + 2, adjustedW, h - 2, ARC_SIZE, ARC_SIZE);
                }
                
                g2.dispose();
            }
            
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                         int x, int y, int w, int h, boolean isSelected) {
                // Don't paint border - we handle it in paintTabBackground
            }
            
            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font,
                                    FontMetrics metrics, int tabIndex, String title,
                                    Rectangle textRect, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Use Poppins font with appropriate size
                Font poppinsFont;
                if (isSelected) {
                    poppinsFont = new Font("Poppins", Font.BOLD, 15);  // Larger, bold for selected
                } else {
                    poppinsFont = new Font("Poppins", Font.PLAIN, 14);  // Regular for unselected
                }
                g2.setFont(poppinsFont);
                
                // Get updated metrics for Poppins font
                FontMetrics fm = g2.getFontMetrics(poppinsFont);
                
                // Set text color based on selection
                if (isSelected) {
                    g2.setColor(TEXT_WHITE);
                } else {
                    g2.setColor(TEXT_PRIMARY);
                }
                
                // Center text both horizontally and vertically within the tab
                int textWidth = fm.stringWidth(title);
                int textX = textRect.x + (textRect.width - textWidth) / 2;  // Center horizontally
                int textY = textRect.y + ((textRect.height - fm.getHeight()) / 2) + fm.getAscent();  // Center vertically
                
                g2.drawString(title, textX, textY);
                g2.dispose();
            }
            
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = tabPane.getWidth();
                int height = tabPane.getHeight();
                int tabAreaHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                
                // Draw rounded border around content area
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(5, tabAreaHeight + 5, width - 10, height - tabAreaHeight - 10, 12, 12);
                
                g2.dispose();
            }
            
            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                return TAB_HEIGHT;
            }
            
            @Override
            protected MouseListener createMouseListener() {
                return new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        int tabIndex = tabForCoordinate(tabPane, e.getX(), e.getY());
                        if (tabIndex >= 0 && tabIndex != tabPane.getSelectedIndex()) {
                            tabPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        }
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        tabPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                    
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int tabIndex = tabForCoordinate(tabPane, e.getX(), e.getY());
                        if (tabIndex >= 0) {
                            tabPane.setSelectedIndex(tabIndex);
                        }
                    }
                };
            }
        });
    }
}
