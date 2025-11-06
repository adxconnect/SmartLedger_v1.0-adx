package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * A modern, theme-aware collapsible sidebar for navigation.
 * Replaces the JTabbedPane.
 */
public class SidebarPanel extends JPanel {

    private JPanel navigationPanel;
    private Map<String, JToggleButton> navigationButtons;
    private ButtonGroup buttonGroup;
    private boolean isExpanded = true;
    private static final int EXPANDED_WIDTH = 220;
    private static final int COLLAPSED_WIDTH = 60;
    private JButton toggleButton;
    private LogoPanel fullLogo;
    private LogoPanel iconOnlyLogo;
    private JPanel logoContainer;

    public SidebarPanel() {
        this.navigationButtons = new HashMap<>();
        this.buttonGroup = new ButtonGroup();
        
        setLayout(new BorderLayout());
        setBackground(ModernTheme.SURFACE);
        setBorder(null); // No border
        setPreferredSize(new Dimension(EXPANDED_WIDTH, 0)); // Set preferred width

        // 1. Top panel with logo and toggle button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ModernTheme.SURFACE);
        
        // Logo Panel at the Top (with no gaps or padding)
        logoContainer = new JPanel(new BorderLayout());
        logoContainer.setBackground(ModernTheme.SURFACE);
        logoContainer.setBorder(new EmptyBorder(0, 10, 10, 10)); // No top padding, only left/bottom/right
        
        // Create both logo versions
        fullLogo = LogoPanel.createHeaderLogo();
        iconOnlyLogo = LogoPanel.createIconOnlyLogo(40);
        
        logoContainer.add(fullLogo, BorderLayout.WEST);
        topPanel.add(logoContainer, BorderLayout.CENTER);
        
        // Toggle button (hamburger menu)
        toggleButton = new JButton();
        toggleButton.setIcon(ModernIcons.create(ModernIcons.IconType.MENU, ModernTheme.TEXT_PRIMARY, 20));
        toggleButton.setBackground(ModernTheme.SURFACE);
        toggleButton.setBorderPainted(false);
        toggleButton.setFocusPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.setPreferredSize(new Dimension(40, 40));
        toggleButton.addActionListener(e -> toggleSidebar());
        
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        togglePanel.setBackground(ModernTheme.SURFACE);
        togglePanel.add(toggleButton);
        topPanel.add(togglePanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        // 2. Navigation Buttons in the Center
        navigationPanel = new JPanel();
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
        navigationPanel.setBackground(ModernTheme.SURFACE);
        navigationPanel.setBorder(new EmptyBorder(15, 10, 15, 10)); // More padding for modern look

        // Add buttons with spacing
        addNavButton("Transactions", ModernIcons.IconType.TRANSACTIONS, true);
        addNavButton("Bank Accounts", ModernIcons.IconType.BANK, false);
        addNavButton("Deposits", ModernIcons.IconType.DEPOSIT, false);
        addNavButton("Investments", ModernIcons.IconType.INVESTMENT, false);
        addNavButton("Taxation", ModernIcons.IconType.TAX, false);
        addNavButton("Loans / EMI", ModernIcons.IconType.LOAN, false);
        addNavButton("Lending", ModernIcons.IconType.MONEY, false); // Assuming MONEY icon for Lending
        addNavButton("Cards", ModernIcons.IconType.CREDIT_CARD, false);
        addNavButton("Summary & Reports", ModernIcons.IconType.SUMMARY, false);

        // Create modern scrollpane with no borders
        JScrollPane scrollPane = new JScrollPane(navigationPanel);
        scrollPane.setBorder(null); // Remove border
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Style the scrollbar to be modern and invisible when not needed
        styleModernScrollBar(scrollPane);
        
        add(scrollPane, BorderLayout.CENTER); 
    }
    
    /**
     * Styles scrollbar to match modern theme
     */
    private void styleModernScrollBar(JScrollPane scrollPane) {
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setOpaque(false);
        verticalBar.setPreferredSize(new Dimension(8, 0));
        verticalBar.setUnitIncrement(16);
        verticalBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = ModernTheme.isDarkMode() ? new Color(80, 80, 80, 150) : new Color(180, 180, 180, 150);
                this.trackColor = new Color(0, 0, 0, 0); // Transparent track
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
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, 
                                thumbBounds.width - 4, thumbBounds.height - 4, 6, 6);
                g2.dispose();
            }
            
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                // Don't paint track - transparent
            }
        });
    }

    /**
     * Helper to create and add a styled navigation button.
     */
    private void addNavButton(String name, ModernIcons.IconType iconType, boolean selected) {
        JToggleButton button = new JToggleButton(name);
        button.setActionCommand(name); // Set action command to name so it persists when text is removed
        button.putClientProperty("iconType", iconType); // Store icon type for theme updates
        button.putClientProperty("buttonName", name); // Store original name for later use
        ModernTheme.styleSidebarButton(button, iconType); // We will add this method to ModernTheme
        button.setSelected(selected);
        
        // Add to ButtonGroup so only one can be selected
        buttonGroup.add(button);
        
        navigationPanel.add(button);
        navigationPanel.add(Box.createRigidArea(new Dimension(0, 6))); // 6px vertical spacing for tighter modern look
        navigationButtons.put(name, button);
    }

    /**
     * Allows the main UI to listen to button clicks.
     */
    public void addNavigationListener(ActionListener listener) {
        for (JToggleButton button : navigationButtons.values()) {
            button.addActionListener(listener);
        }
    }

    /**
     * Toggles the sidebar between expanded and collapsed states.
     */
    private void toggleSidebar() {
        isExpanded = !isExpanded;
        
        if (isExpanded) {
            // Expand sidebar
            setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));
            logoContainer.removeAll();
            logoContainer.add(fullLogo, BorderLayout.WEST);
            
            // Show text on buttons and restore normal size
            for (Map.Entry<String, JToggleButton> entry : navigationButtons.entrySet()) {
                JToggleButton btn = entry.getValue();
                String buttonName = entry.getKey();
                
                btn.setText(buttonName);
                btn.setActionCommand(buttonName); // Ensure action command is set
                btn.setToolTipText(null); // Remove tooltip in expanded mode
                btn.setHorizontalAlignment(SwingConstants.LEFT);
                btn.setIconTextGap(12);
                btn.setMinimumSize(new Dimension(200, 44));
                btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
                btn.setPreferredSize(new Dimension(200, 44));
                btn.setBorder(new EmptyBorder(10, 15, 10, 15));
                
                // Refresh the button to update icon size
                ModernIcons.IconType iconType = (ModernIcons.IconType) btn.getClientProperty("iconType");
                if (iconType != null) {
                    ModernTheme.styleSidebarButton(btn, iconType);
                }
            }
        } else {
            // Collapse sidebar - icon only mode
            setPreferredSize(new Dimension(COLLAPSED_WIDTH, 0));
            logoContainer.removeAll();
            logoContainer.add(iconOnlyLogo, BorderLayout.CENTER);
            
            // Hide text, show only centered icons
            for (JToggleButton button : navigationButtons.values()) {
                String buttonName = (String) button.getClientProperty("buttonName");
                
                button.setText(""); // Remove text but keep action command
                button.setActionCommand(buttonName); // Ensure action command is preserved
                button.setToolTipText(buttonName); // Add tooltip for collapsed mode
                button.setHorizontalAlignment(SwingConstants.CENTER);
                button.setIconTextGap(0);
                button.setMinimumSize(new Dimension(50, 44));
                button.setMaximumSize(new Dimension(50, 44));
                button.setPreferredSize(new Dimension(50, 44));
                button.setBorder(new EmptyBorder(10, 5, 10, 5));
                
                // Ensure icon is visible and properly sized
                ModernIcons.IconType iconType = (ModernIcons.IconType) button.getClientProperty("iconType");
                if (iconType != null) {
                    Color iconColor = button.isSelected() 
                        ? new Color(255, 255, 255) 
                        : (ModernTheme.isDarkMode() ? new Color(220, 220, 220) : new Color(60, 60, 60));
                    button.setIcon(ModernIcons.create(iconType, iconColor, 24));
                }
                
                // Ensure button is visible and clickable
                button.setVisible(true);
                button.setEnabled(true);
                button.setFocusable(true);
                button.revalidate();
                button.repaint();
            }
        }
        
        logoContainer.revalidate();
        logoContainer.repaint();
        navigationPanel.revalidate();
        navigationPanel.repaint();
        revalidate();
        repaint();
        
        // Notify parent to revalidate layout
        Container parent = getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }
    
    /**
     * Updates the theme for the sidebar itself and all its buttons.
     */
    public void updateTheme() {
        setBackground(ModernTheme.SURFACE);
        setBorder(null); // No border
        navigationPanel.setBackground(ModernTheme.SURFACE);
        toggleButton.setIcon(ModernIcons.create(ModernIcons.IconType.MENU, ModernTheme.TEXT_PRIMARY, 20));
        
        // Update all buttons
        for (JToggleButton button : navigationButtons.values()) {
            // Retrieve the stored icon type
            ModernIcons.IconType type = (ModernIcons.IconType) button.getClientProperty("iconType");
            ModernTheme.styleSidebarButton(button, type); 
        }
    }
}
