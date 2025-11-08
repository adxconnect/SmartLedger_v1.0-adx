package src.UI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Arrays;

import javax.swing.*;

import src.auth.Account;
import src.auth.AuthManager;
import src.auth.SessionContext;
import src.auth.UserPreferencesCache;

/**
 * Simple dialog that provides sign-in and registration flows for Finance Manager users.
 * This version uses a CardLayout instead of JTabbedPane.
 */
public class LoginDialog extends JDialog {

    private final AuthManager authManager;
    private boolean succeeded;
    
    // --- NEW: CardLayout for panel switching ---
    private CardLayout cardLayout;
    private JPanel mainCardPanel;
    private LogoPanel loginLogo; // Logo panel for theme switching
    private JButton darkModeBtn; // Dark mode toggle button reference
    
    // Welcome text labels with fixed colors
    private JLabel welcomeTitleLabel;
    private JLabel welcomeSubtitleLabel;

    private JPanel rootWrapper;
    private JPanel titleBar;
    private JLabel titleLabel;
    private JButton titleCloseButton;
    private Point dragOffset;

    private AutoCompleteTextField loginEmailField;
    private JPasswordField loginPasswordField;

    private AutoCompleteTextField registerNameField;
    private JComboBox<Account.AccountType> registerAccountTypeField;
    private AutoCompleteTextField registerEmailField;
    private AutoCompleteTextField registerPhoneField;
    private JPasswordField registerPasswordField;
    private JPasswordField registerConfirmPasswordField;

    public LoginDialog(Frame owner, AuthManager authManager) {
        super(owner, "SmartLedger - Welcome", true);
        this.authManager = authManager;
        setUndecorated(true);
        buildModernUi();
        setResizable(false);
        // NEW: Adjusted window size for the two-column layout
        setSize(960, 640);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(owner);
    getContentPane().setBackground(ModernTheme.SURFACE);
    }

    private void buildModernUi() {
        // --- NEW: Main container with a two-column layout ---
    rootWrapper = new JPanel(new BorderLayout());
    rootWrapper.setBackground(ModernTheme.SURFACE);
    rootWrapper.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER, 1));

    titleBar = createTitleBar();
    rootWrapper.add(titleBar, BorderLayout.NORTH);

    JPanel mainPanel = new JPanel(new GridLayout(1, 2));
    mainPanel.setOpaque(false);
        
        // --- Column 1: Branding and Welcome Message ---
        JPanel leftPanel = createBrandingPanel();
        
        // --- Column 2: Login Form ---
        JPanel rightPanel = createFormPanel();

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        rootWrapper.add(mainPanel, BorderLayout.CENTER);
        setContentPane(rootWrapper);
        updateTitleBarTheme();
    }

    /**
     * Creates the left-side branding panel with a gradient, logo, and welcome text.
     */
    private JPanel createBrandingPanel() {
        // Create a custom gradient panel that adapts to light/dark mode
        JPanel brandingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Define colors based on dark mode state
                Color gradientStart, gradientEnd;
                if (ModernTheme.isDarkMode()) {
                    // Dark mode: Much lighter blue gradient for logo text visibility
                    gradientStart = new Color(65, 95, 145);  // Much lighter blue
                    gradientEnd = new Color(75, 135, 145);    // Much lighter teal
                } else {
                    // Light mode: Lighter, more transparent blue to teal gradient
                    gradientStart = new Color(120, 145, 245);  // Lighter blue
                    gradientEnd = new Color(100, 170, 178);   // Lighter teal
                }
                
                GradientPaint gp = new GradientPaint(0, 0, gradientStart, getWidth(), getHeight(), gradientEnd);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        
        brandingPanel.setLayout(new BorderLayout(20, 20));
        brandingPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Welcome message at the top-left
        JPanel welcomeHeader = new JPanel();
        welcomeHeader.setLayout(new BoxLayout(welcomeHeader, BoxLayout.Y_AXIS));
        welcomeHeader.setOpaque(false);

        welcomeTitleLabel = new JLabel("Welcome Back!");
        welcomeTitleLabel.setFont(ModernTheme.FONT_TITLE.deriveFont(36f)); // Larger font
        // Very light blue gradient color - works for both dark and light modes
        welcomeTitleLabel.setForeground(new Color(220, 240, 255)); // Very light blue
        welcomeHeader.add(welcomeTitleLabel);

        welcomeSubtitleLabel = new JLabel("Sign in to continue to SmartLedger");
        welcomeSubtitleLabel.setFont(ModernTheme.FONT_BODY.deriveFont(16f));
        // Very light blue gradient color - consistent with title
        welcomeSubtitleLabel.setForeground(new Color(200, 230, 255)); // Very light blue with slightly more saturation
        welcomeHeader.add(welcomeSubtitleLabel);
        
        brandingPanel.add(welcomeHeader, BorderLayout.NORTH);

        // Logo in the center - Logo1.png for light mode, Logo2.png for dark mode
        loginLogo = new LogoPanel(350, true); // Increased to 350px for maximum visibility
        JPanel logoContainer = new JPanel(new GridBagLayout()); // Use GridBagLayout to center
        logoContainer.setOpaque(false);
        logoContainer.add(loginLogo);
        brandingPanel.add(logoContainer, BorderLayout.CENTER);
        
        // Dark mode toggle at the bottom-left
        darkModeBtn = ModernTheme.createDarkModeToggleButton();
        darkModeBtn.addActionListener(e -> {
            ModernTheme.toggleDarkMode();
            refreshDialogTheme();
        });
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setOpaque(false);
        footer.add(darkModeBtn);
        brandingPanel.add(footer, BorderLayout.SOUTH);

        return brandingPanel;
    }

    /**
     * Creates the right-side panel containing the login form.
     */
    private JPanel createFormPanel() {
        // This panel will hold the CardLayout for switching between sign-in and register
        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(ModernTheme.BACKGROUND);
        mainCardPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Build the sign-in and register panels
        JPanel signInPanel = buildModernSignInPanel();
        JPanel registerPanel = buildModernRegisterPanel();

        // The cards no longer need their own border, the form panel has it
        mainCardPanel.add(signInPanel, "Sign In");
        mainCardPanel.add(registerPanel, "Register");

        // Show the "Sign In" card first
        cardLayout.show(mainCardPanel, "Sign In");
        
        return mainCardPanel;
    }

    private JPanel createTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(ModernTheme.SURFACE);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER));

        JPanel leftSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        leftSection.setOpaque(false);
        titleLabel = new JLabel("SmartLedger");
        titleLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 14));
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        leftSection.add(titleLabel);

        JPanel controlSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controlSection.setOpaque(false);
        titleCloseButton = createTitleBarButton("X", true);
        titleCloseButton.addActionListener(e -> {
            succeeded = false;
            dispose();
        });
        controlSection.add(titleCloseButton);

        bar.add(leftSection, BorderLayout.WEST);
        bar.add(controlSection, BorderLayout.EAST);

        enableWindowDrag(bar);
        enableWindowDrag(leftSection);
        enableWindowDrag(titleLabel);

        return bar;
    }

    private JButton createTitleBarButton(String text, boolean closeButton) {
        JButton btn = new JButton(text);
        btn.putClientProperty("titleBarClose", closeButton);
        btn.putClientProperty("titleBarButton", Boolean.TRUE);
        btn.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 12));
        btn.setFocusable(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
    btn.setPreferredSize(new Dimension(44, 26));
        btn.setBackground(ModernTheme.SURFACE);
        btn.setForeground(ModernTheme.TEXT_PRIMARY);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (closeButton) {
                    btn.setBackground(ModernTheme.DANGER);
                    btn.setForeground(ModernTheme.TEXT_WHITE);
                } else {
                    btn.setBackground(ModernTheme.BACKGROUND);
                    btn.setForeground(ModernTheme.TEXT_PRIMARY);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateTitleButtonTheme(btn);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (closeButton) {
                    btn.setBackground(ModernTheme.DANGER.darker());
                    btn.setForeground(ModernTheme.TEXT_WHITE);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (closeButton && btn.contains(e.getPoint())) {
                    btn.setBackground(ModernTheme.DANGER);
                    btn.setForeground(ModernTheme.TEXT_WHITE);
                }
            }
        });

        updateTitleButtonTheme(btn);
        return btn;
    }

    private void updateTitleButtonTheme(JButton btn) {
        if (btn == null) {
            return;
        }
        boolean closeButton = Boolean.TRUE.equals(btn.getClientProperty("titleBarClose"));
        btn.setBackground(ModernTheme.SURFACE);
        btn.setForeground(closeButton ? ModernTheme.TEXT_PRIMARY : ModernTheme.TEXT_PRIMARY);
    }

    private void enableWindowDrag(Component component) {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point click = e.getLocationOnScreen();
                Point window = getLocation();
                dragOffset = new Point(click.x - window.x, click.y - window.y);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset == null) {
                    return;
                }
                Point drag = e.getLocationOnScreen();
                setLocation(drag.x - dragOffset.x, drag.y - dragOffset.y);
            }
        };
        component.addMouseListener(adapter);
        component.addMouseMotionListener(adapter);
    }

    private void updateTitleBarTheme() {
        if (rootWrapper != null) {
            rootWrapper.setBackground(ModernTheme.SURFACE);
            rootWrapper.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER, 1));
        }
        if (titleBar != null) {
            titleBar.setBackground(ModernTheme.SURFACE);
            titleBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER));
        }
        if (titleLabel != null) {
            titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        }
        updateTitleButtonTheme(titleCloseButton);
    }

    private JPanel buildModernSignInPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ModernTheme.BACKGROUND); // Match the form panel background
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        // --- All form elements will be centered ---
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Email Field with left-aligned label
        JPanel emailSection = new JPanel();
        emailSection.setLayout(new BoxLayout(emailSection, BoxLayout.Y_AXIS));
        emailSection.setBackground(ModernTheme.BACKGROUND);
        emailSection.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailSection.setMaximumSize(new Dimension(490, 80));
        
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(ModernTheme.FONT_BUTTON);
        emailLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        loginEmailField = new AutoCompleteTextField(30, UserPreferencesCache::getCachedEmails);
        String lastEmail = UserPreferencesCache.getLastEmail();
        if (lastEmail != null && !lastEmail.isEmpty()) {
            loginEmailField.setTextQuietly(lastEmail);
        }
        ModernTheme.styleTextField(loginEmailField);
        loginEmailField.setMaximumSize(new Dimension(490, 42));
        loginEmailField.setPreferredSize(new Dimension(490, 42));
        loginEmailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        emailSection.add(emailLabel);
        emailSection.add(Box.createVerticalStrut(8));
        emailSection.add(loginEmailField);
        
        panel.add(emailSection);
        panel.add(Box.createVerticalStrut(20));

        // Password Field with left-aligned label
        JPanel passwordSection = new JPanel();
        passwordSection.setLayout(new BoxLayout(passwordSection, BoxLayout.Y_AXIS));
        passwordSection.setBackground(ModernTheme.BACKGROUND);
        passwordSection.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordSection.setMaximumSize(new Dimension(490, 80));
        
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(ModernTheme.FONT_BUTTON);
        passwordLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        loginPasswordField = new JPasswordField(30);
        loginPasswordField.setFont(ModernTheme.FONT_BODY);
        loginPasswordField.setForeground(ModernTheme.TEXT_PRIMARY);
        loginPasswordField.setBackground(ModernTheme.SURFACE);
        loginPasswordField.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, ModernTheme.BORDER),
            BorderFactory.createEmptyBorder(8, 12, 8, 45)
        ));
        loginPasswordField.setMaximumSize(new Dimension(490, 42));
        loginPasswordField.setPreferredSize(new Dimension(490, 42));
        loginPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create password field wrapper with eye toggle button
        JPanel passwordFieldWrapper = createPasswordFieldWithToggle(loginPasswordField);
        passwordFieldWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordSection.add(passwordLabel);
        passwordSection.add(Box.createVerticalStrut(8));
        passwordSection.add(passwordFieldWrapper);
        
        panel.add(passwordSection);
        panel.add(Box.createVerticalStrut(20)); // Increased space

        // Links Panel for Forgot Password and Register
        JPanel linksPanel = new JPanel();
        linksPanel.setLayout(new BoxLayout(linksPanel, BoxLayout.Y_AXIS));
        linksPanel.setBackground(ModernTheme.BACKGROUND);
        linksPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Forgot Password Link
        JLabel forgotPasswordLabel = new JLabel("<html><a href=''>Forgot Password?</a></html>");
        forgotPasswordLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(Font.BOLD));
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        forgotPasswordLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openForgotPasswordDialog();
                // --- MODIFICATION ---
                setTitle("SmartLedger - Reset Password");
            }
        });
        linksPanel.add(forgotPasswordLabel);
        
        // Add vertical gap between the two links
        linksPanel.add(Box.createVerticalStrut(8));
        
        // Register link
        JLabel registerLink = new JLabel("<html>Don't have an account? <a href=''>Register</a></html>");
        registerLink.setFont(ModernTheme.FONT_SMALL.deriveFont(Font.BOLD));
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Open dedicated Register popup with scrollable body and fixed footer
                RegisterDialog dialog = new RegisterDialog((Frame) getOwner(), authManager);
                dialog.setVisible(true);
            }
        });
        linksPanel.add(registerLink);
        panel.add(linksPanel);
        
        panel.add(Box.createVerticalStrut(30)); // Space before buttons

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(ModernTheme.BACKGROUND);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(490, 50));
        
        JButton cancelButton = ModernTheme.createSecondaryButton("Cancel");
        JButton signInButton = ModernTheme.createPrimaryButton("Sign In");
        signInButton.setPreferredSize(new Dimension(120, 42));
        cancelButton.setPreferredSize(new Dimension(100, 42));
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(signInButton);
        panel.add(buttonPanel);

        signInButton.addActionListener(e -> attemptLogin());
        cancelButton.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        getRootPane().setDefaultButton(signInButton);
        return panel;
    }

    private JPanel buildModernRegisterPanel() {
        // --- MODIFICATION: Use BorderLayout ---
        JPanel panel = new JPanel(new BorderLayout()); 
        panel.setBackground(ModernTheme.SURFACE);
        // Remove the old border, padding will be handled by sub-panels
        // panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 28, 20));

        // Header band (no change, just add to NORTH)
        JPanel header = createCardHeader("Create Account", "Set up your profile to get started");
        panel.add(header, BorderLayout.NORTH);
        // --- END MODIFICATION ---

        // Build a compact two-column form
        JPanel formGrid = new JPanel(new GridBagLayout());
        // --- MODIFICATION: Add padding around the form itself ---
        formGrid.setOpaque(false); // Make it transparent
        formGrid.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        // --- END MODIFICATION ---
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        Dimension fieldSize = new Dimension(270, 34);

        // Row 0: Account Name | Account Type
        int row = 0;
        // Removed section dividers for a cleaner, tighter layout
        // Account Name
        JLabel nameLabel = new JLabel("Account Name");
        nameLabel.setFont(new Font("Poppins", Font.PLAIN, 13));
        nameLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; formGrid.add(nameLabel, gc);
        registerNameField = new AutoCompleteTextField(20, UserPreferencesCache::getCachedAccountNames);
        ModernTheme.styleTextField(registerNameField);
        registerNameField.setPreferredSize(fieldSize);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 0.5; formGrid.add(registerNameField, gc);
        // Account Type
        JLabel typeLabel = new JLabel("Account Type");
        typeLabel.setFont(new Font("Poppins", Font.PLAIN, 13));
        typeLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        gc.gridx = 2; gc.gridy = row; gc.weightx = 0; formGrid.add(typeLabel, gc);
        registerAccountTypeField = new JComboBox<>(Account.AccountType.values());
        registerAccountTypeField.setSelectedItem(Account.AccountType.PERSONAL);
        ModernTheme.styleComboBox(registerAccountTypeField);
        registerAccountTypeField.setPreferredSize(fieldSize);
        gc.gridx = 3; gc.gridy = row; gc.weightx = 0.5; formGrid.add(registerAccountTypeField, gc);

        // Row 1: Email | Phone
        row++;
        // Removed extra vertical spacing and dividers
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Poppins", Font.PLAIN, 13));
        emailLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; formGrid.add(emailLabel, gc);
        registerEmailField = new AutoCompleteTextField(20, UserPreferencesCache::getCachedEmails);
        ModernTheme.styleTextField(registerEmailField);
        registerEmailField.setPreferredSize(fieldSize);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 0.5; formGrid.add(registerEmailField, gc);
        JLabel phoneLabel = new JLabel("Phone Number (Optional)");
        phoneLabel.setFont(new Font("Poppins", Font.PLAIN, 13));
        phoneLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        gc.gridx = 2; gc.gridy = row; gc.weightx = 0; formGrid.add(phoneLabel, gc);
        registerPhoneField = new AutoCompleteTextField(20, UserPreferencesCache::getCachedPhoneNumbers);
        ModernTheme.styleTextField(registerPhoneField);
        registerPhoneField.setPreferredSize(fieldSize);
        gc.gridx = 3; gc.gridy = row; gc.weightx = 0.5; formGrid.add(registerPhoneField, gc);

        // Row 2: Password | Confirm Password
        row++;
        // Removed extra vertical spacing and dividers
        JLabel pwdLabel = new JLabel("Password");
        pwdLabel.setFont(new Font("Poppins", Font.PLAIN, 13));
        pwdLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; formGrid.add(pwdLabel, gc);
        registerPasswordField = new JPasswordField(20);
        styleModernPasswordField(registerPasswordField);
        registerPasswordField.setPreferredSize(fieldSize);
        JPanel regPwdWrapper = createPasswordFieldWithToggle(registerPasswordField);
        regPwdWrapper.setPreferredSize(fieldSize);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 0.5; formGrid.add(regPwdWrapper, gc);
        JLabel cpwdLabel = new JLabel("Confirm Password");
        cpwdLabel.setFont(new Font("Poppins", Font.PLAIN, 13));
        cpwdLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        gc.gridx = 2; gc.gridy = row; gc.weightx = 0; formGrid.add(cpwdLabel, gc);
        registerConfirmPasswordField = new JPasswordField(20);
        styleModernPasswordField(registerConfirmPasswordField);
        registerConfirmPasswordField.setPreferredSize(fieldSize);
        JPanel regConfirmPwdWrapper = createPasswordFieldWithToggle(registerConfirmPasswordField);
        regConfirmPwdWrapper.setPreferredSize(fieldSize);
        gc.gridx = 3; gc.gridy = row; gc.weightx = 0.5; formGrid.add(regConfirmPwdWrapper, gc);
        
        // --- MODIFICATION: Add formGrid to CENTER ---
        // (We wrap it to prevent it from stretching vertically)
        JPanel formGridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        formGridWrapper.setOpaque(false);
        formGridWrapper.add(formGrid);
        panel.add(formGridWrapper, BorderLayout.CENTER);
        // --- END MODIFICATION ---


        // --- MODIFICATION: Create a fixed footer panel ---
        JPanel footerPanel = new JPanel(new BorderLayout(10, 0)); // Use BorderLayout with 10px gap
        footerPanel.setOpaque(false); // Make transparent
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20)); // Padding

        // Sign-in link
        JLabel signInLink = new JLabel("<html>Already have an account? <a href=''>Sign In</a></html>");
        signInLink.setFont(ModernTheme.FONT_SMALL);
        signInLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signInLink.setHorizontalAlignment(SwingConstants.LEFT); // Align link to the left
        signInLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                cardLayout.show(mainCardPanel, "Sign In");
                setTitle("SmartLedger - Welcome");
            }
        });
        // Add link to the WEST (left)
        footerPanel.add(signInLink, BorderLayout.WEST);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Align buttons to the right
        buttonPanel.setOpaque(false); // Make transparent
        JButton cancelButton = ModernTheme.createSecondaryButton("Cancel");
        JButton createButton = ModernTheme.createSuccessButton("Create Account");
        createButton.setPreferredSize(new Dimension(140, 34));
        cancelButton.setPreferredSize(new Dimension(100, 34));
        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        
        // Add buttons to the EAST (right)
        footerPanel.add(buttonPanel, BorderLayout.EAST);

        // Add the entire footer panel to the SOUTH of the main panel
        panel.add(footerPanel, BorderLayout.SOUTH);
        // --- END MODIFICATION ---

        createButton.addActionListener(e -> attemptRegistration());
        cancelButton.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        return panel;
    }

    // --- UI helpers for unique card styling ---
    private JPanel createCardHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(ModernTheme.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setForeground(ModernTheme.TEXT_WHITE);
        titleLbl.setFont(ModernTheme.FONT_HEADER);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setForeground(new Color(255, 255, 255, 200));
        subLbl.setFont(ModernTheme.FONT_SMALL);

        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setOpaque(false);
        texts.add(titleLbl);
        texts.add(subLbl);
        header.add(texts, BorderLayout.WEST);
        return header;
    }

    private JPanel sectionDivider(String text) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        JLabel lbl = new JLabel(text);
        lbl.setFont(ModernTheme.FONT_SMALL);
        lbl.setForeground(ModernTheme.TEXT_SECONDARY);
        JSeparator left = new JSeparator();
        JSeparator right = new JSeparator();
        left.setForeground(ModernTheme.BORDER);
        right.setForeground(ModernTheme.BORDER);
        wrap.add(left, BorderLayout.WEST);
        wrap.add(lbl, BorderLayout.CENTER);
        wrap.add(right, BorderLayout.EAST);
        wrap.setBorder(BorderFactory.createEmptyBorder(6, 0, 2, 0));
        return wrap;
    }
    
    private void addModernField(JPanel panel, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Poppins", Font.PLAIN, 13));
        label.setForeground(ModernTheme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
    }
    
    /**
     * Creates a panel with password field and eye toggle button
     */
    private JPanel createPasswordFieldWithToggle(JPasswordField passwordField) {
        // Create wrapper panel with BorderLayout
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(passwordField.getMaximumSize());
        wrapper.setPreferredSize(passwordField.getPreferredSize());
        
        // Add password field
        wrapper.add(passwordField, BorderLayout.CENTER);
        
        // Create eye toggle button
        JButton eyeButton = new JButton();
        eyeButton.setIcon(ModernIcons.create(ModernIcons.IconType.EYE_OFF, ModernTheme.TEXT_SECONDARY, 18));
        eyeButton.setBorderPainted(false);
        eyeButton.setContentAreaFilled(false);
        eyeButton.setFocusPainted(false);
        eyeButton.setOpaque(false);
        eyeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeButton.setPreferredSize(new Dimension(40, passwordField.getPreferredSize().height));
        eyeButton.setFocusable(false); // Prevent focus stealing from password field
        
        // Toggle password visibility
        eyeButton.addActionListener(e -> {
            if (passwordField.getEchoChar() == (char) 0) {
                // Hide password
                passwordField.setEchoChar('•');
                eyeButton.setIcon(ModernIcons.create(ModernIcons.IconType.EYE_OFF, ModernTheme.TEXT_SECONDARY, 18));
            } else {
                // Show password
                passwordField.setEchoChar((char) 0);
                eyeButton.setIcon(ModernIcons.create(ModernIcons.IconType.EYE, ModernTheme.TEXT_SECONDARY, 18));
            }
        });
        
        // Add eye button to the right
        wrapper.add(eyeButton, BorderLayout.EAST);
        
        return wrapper;
    }
    
    private void styleModernPasswordField(JPasswordField field) {
        field.setFont(new Font("Poppins", Font.PLAIN, 13));
        field.setForeground(ModernTheme.TEXT_PRIMARY);
        field.setBackground(ModernTheme.SURFACE);
        field.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, ModernTheme.BORDER),
            BorderFactory.createEmptyBorder(6, 12, 6, 45)
        ));
        field.setMaximumSize(new Dimension(520, 38));
        field.setPreferredSize(new Dimension(520, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    
    private void styleModernScrollBar(JScrollPane scrollPane) {
        // Style vertical scrollbar
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = ModernTheme.PRIMARY_LIGHT;
                this.trackColor = ModernTheme.BACKGROUND;
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
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y, thumbBounds.width - 4, 
                    thumbBounds.height, 8, 8);
                g2.dispose();
            }
            
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }
        });
        verticalScrollBar.setPreferredSize(new Dimension(10, 0));
    }

    private void attemptLogin() {
        String email = loginEmailField.getText().trim();
        char[] password = loginPasswordField.getPassword();
        if (email.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this, "Email and password are required.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            Arrays.fill(password, '\0');
            return;
        }

        try {
            Account account = authManager.verifyCredentials(email, password);
            authManager.recordLoginEvent(account.getId(), "LOGIN_SUCCESS", null, null);
            // Cache the email for autocomplete
            UserPreferencesCache.cacheEmail(email);
            succeeded = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Authentication Failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Unable to sign in: " + ex.getMessage(), "Authentication Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void attemptRegistration() {
        String accountName = registerNameField.getText().trim();
        String email = registerEmailField.getText().trim();
        String phone = registerPhoneField.getText().trim();
        char[] password = registerPasswordField.getPassword();
        char[] confirm = registerConfirmPasswordField.getPassword();

        if (accountName.isEmpty() || email.isEmpty() || password.length == 0 || confirm.length == 0) {
            JOptionPane.showMessageDialog(this, "All required fields must be filled.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
            return;
        }

        if (!Arrays.equals(password, confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
            return;
        }

        Account.AccountType accountType = (Account.AccountType) registerAccountTypeField.getSelectedItem();
        try {
            Account newAccount = authManager.createAccount(
                accountName,
                accountType,
                email,
                phone.isBlank() ? null : phone,
                password,
                null,
                null
            );
            authManager.recordLoginEvent(newAccount.getId(), "ACCOUNT_CREATED", null, null);
            // Cache all the registration data for autocomplete
            UserPreferencesCache.cacheAccountName(accountName);
            UserPreferencesCache.cacheEmail(email);
            if (!phone.isBlank()) {
                UserPreferencesCache.cachePhoneNumber(phone);
            }
            JOptionPane.showMessageDialog(this, "Account created successfully. You are now signed in.", "Welcome", JOptionPane.INFORMATION_MESSAGE);
            SessionContext.setCurrentAccount(newAccount);
            succeeded = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Unable to create account: " + ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
        }
    }

    private void openForgotPasswordDialog() {
        ForgotPasswordDialog dialog = new ForgotPasswordDialog((Frame) getOwner(), authManager);
        dialog.setVisible(true);
    }
    
    /**
     * Refreshes the dialog theme when dark mode is toggled.
     * This updates all components without recreating the dialog.
     */
    private void refreshDialogTheme() {
        // Update dialog background
        getContentPane().setBackground(ModernTheme.BACKGROUND);
        
        // Refresh logo to match current theme
        if (loginLogo != null) {
            loginLogo.refreshLogo();
        }
        
        // Explicitly repaint the dark mode toggle button (custom painted icon)
        if (darkModeBtn != null) {
            darkModeBtn.repaint();
        }
        
        // Recursively update all components
        updateComponentTheme(getContentPane());

        updateTitleBarTheme();
        
        // Repaint and revalidate to apply changes
        revalidate();
        repaint();
    }
    
    /**
     * Recursively updates the theme for all components in the container.
     */
    private void updateComponentTheme(Container container) {
        container.setBackground(ModernTheme.BACKGROUND);
        
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                panel.setBackground(ModernTheme.SURFACE);
            } else if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                // Skip welcome text labels - they have fixed light blue colors
                if (label != welcomeTitleLabel && label != welcomeSubtitleLabel) {
                    label.setForeground(ModernTheme.TEXT_PRIMARY);
                }
            } else if (comp instanceof JTextField) {
                JTextField field = (JTextField) comp;
                field.setBackground(ModernTheme.SURFACE);
                field.setForeground(ModernTheme.TEXT_PRIMARY);
                field.setCaretColor(ModernTheme.TEXT_PRIMARY);
            } else if (comp instanceof JPasswordField) {
                JPasswordField field = (JPasswordField) comp;
                field.setBackground(ModernTheme.SURFACE);
                field.setForeground(ModernTheme.TEXT_PRIMARY);
                field.setCaretColor(ModernTheme.TEXT_PRIMARY);
            } else if (comp instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>) comp;
                combo.setBackground(ModernTheme.SURFACE);
                combo.setForeground(ModernTheme.TEXT_PRIMARY);
            } else if (comp instanceof JTabbedPane) {
                JTabbedPane tabs = (JTabbedPane) comp;
                tabs.setBackground(ModernTheme.SURFACE);
                tabs.setForeground(ModernTheme.TEXT_PRIMARY);
            } else if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                // Use the centralized button refresh utility
                ModernTheme.refreshButtonColors(btn);
            }
            
            // Recursively update child containers
            if (comp instanceof Container) {
                updateComponentTheme((Container) comp);
            }
        }
    }
    
    public boolean isSucceeded() {
        return succeeded;
    }
    
}