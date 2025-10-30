package src.UI;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

import javax.swing.*;

import src.auth.Account;
import src.auth.AuthManager;
import src.auth.SessionContext;
import src.auth.UserPreferencesCache;

/**
 * Simple dialog that provides sign-in and registration flows for Finance Manager users.
 */
public class LoginDialog extends JDialog {

    private final AuthManager authManager;
    private boolean succeeded;

    private AutoCompleteTextField loginEmailField;
    private JPasswordField loginPasswordField;

    private AutoCompleteTextField registerNameField;
    private JComboBox<Account.AccountType> registerAccountTypeField;
    private AutoCompleteTextField registerEmailField;
    private AutoCompleteTextField registerPhoneField;
    private JPasswordField registerPasswordField;
    private JPasswordField registerConfirmPasswordField;

    public LoginDialog(Frame owner, AuthManager authManager) {
        super(owner, "FinanceHub - Welcome", true);
        this.authManager = authManager;
        buildModernUi();
        setResizable(false);
        setSize(620, 750);  // Increased size to fit content properly
        setMinimumSize(new Dimension(620, 750));
        setPreferredSize(new Dimension(620, 750));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(ModernTheme.BACKGROUND);
    }

    private void buildModernUi() {
        setLayout(new BorderLayout());
        
        // Create main container with modern background
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(ModernTheme.BACKGROUND);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // Logo and Title Section
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(ModernTheme.BACKGROUND);
        
        LogoPanel logo = LogoPanel.createLoginLogo();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(logo);
        headerPanel.add(Box.createVerticalStrut(15));
        
        JLabel titleLabel = new JLabel("Welcome Back!");
        titleLabel.setFont(ModernTheme.FONT_TITLE);
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("Sign in to manage your finances");
        subtitleLabel.setFont(ModernTheme.FONT_BODY);
        subtitleLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(subtitleLabel);
        
        headerPanel.add(Box.createVerticalStrut(15));
        
        // Add dark mode toggle button
        JButton darkModeBtn = ModernTheme.createDarkModeToggleButton();
        darkModeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        darkModeBtn.addActionListener(e -> {
            ModernTheme.toggleDarkMode();
            dispose();
            new LoginDialog((Frame) getOwner(), authManager).setVisible(true);
        });
        headerPanel.add(darkModeBtn);
        
        headerPanel.add(Box.createVerticalStrut(15));
        
        // Create modern tabbed pane with custom styling
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(ModernTheme.FONT_HEADER);
        tabs.setBackground(ModernTheme.SURFACE);
        tabs.setForeground(ModernTheme.TEXT_PRIMARY);
        tabs.setOpaque(true);
        
        // Style the tab pane
        tabs.setBorder(BorderFactory.createEmptyBorder());
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected) {
                    g2.setColor(ModernTheme.PRIMARY);
                } else {
                    g2.setColor(ModernTheme.BACKGROUND);
                }
                g2.fillRoundRect(x, y, w, h, 8, 8);
            }
            
            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                    int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setFont(font);
                g.setColor(isSelected ? ModernTheme.TEXT_WHITE : ModernTheme.TEXT_SECONDARY);
                g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            }
        });
        
        tabs.addTab("Sign In", buildModernSignInPanel());
        
        // Register panel with modern scroll
        JPanel registerPanel = buildModernRegisterPanel();
        JScrollPane registerScroll = new JScrollPane(registerPanel);
        registerScroll.setBorder(null);
        registerScroll.getVerticalScrollBar().setUnitIncrement(16);
        registerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        styleModernScrollBar(registerScroll);
        tabs.addTab("Register", registerScroll);
        
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(tabs, BorderLayout.CENTER);
        
        add(mainContainer);
    }

    private JPanel buildModernSignInPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ModernTheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        // Email Field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(ModernTheme.FONT_BODY);
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

        panel.add(emailLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(loginEmailField);
        panel.add(Box.createVerticalStrut(15));

        // Password Field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(ModernTheme.FONT_BODY);
        passwordLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        loginPasswordField = new JPasswordField(30);
        loginPasswordField.setFont(ModernTheme.FONT_BODY);
        loginPasswordField.setForeground(ModernTheme.TEXT_PRIMARY);
        loginPasswordField.setBackground(ModernTheme.SURFACE);
        loginPasswordField.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, ModernTheme.BORDER),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        loginPasswordField.setMaximumSize(new Dimension(490, 42));
        loginPasswordField.setPreferredSize(new Dimension(490, 42));
        loginPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(passwordLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(loginPasswordField);
        panel.add(Box.createVerticalStrut(8));

        // Forgot Password Link
        JLabel forgotPasswordLabel = new JLabel("<html><a href=''>Forgot Password?</a></html>");
        forgotPasswordLabel.setFont(ModernTheme.FONT_SMALL);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        forgotPasswordLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openForgotPasswordDialog();
            }
        });
        panel.add(forgotPasswordLabel);
        panel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ModernTheme.SURFACE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ModernTheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Account Name
        addModernField(panel, "Account Name");
        registerNameField = new AutoCompleteTextField(20, UserPreferencesCache::getCachedAccountNames);
        ModernTheme.styleTextField(registerNameField);
        registerNameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerNameField.setMaximumSize(new Dimension(520, 38));
        registerNameField.setPreferredSize(new Dimension(520, 38));
        registerNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(registerNameField);
        panel.add(Box.createVerticalStrut(12));

        // Account Type
        JLabel typeLabel = new JLabel("Account Type");
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(typeLabel);
        panel.add(Box.createVerticalStrut(6));
        
        registerAccountTypeField = new JComboBox<>(Account.AccountType.values());
        registerAccountTypeField.setSelectedItem(Account.AccountType.PERSONAL);
        ModernTheme.styleComboBox(registerAccountTypeField);
        registerAccountTypeField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerAccountTypeField.setMaximumSize(new Dimension(520, 38));
        registerAccountTypeField.setPreferredSize(new Dimension(520, 38));
        registerAccountTypeField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(registerAccountTypeField);
        panel.add(Box.createVerticalStrut(12));

        // Email
        addModernField(panel, "Email Address");
        registerEmailField = new AutoCompleteTextField(20, UserPreferencesCache::getCachedEmails);
        ModernTheme.styleTextField(registerEmailField);
        registerEmailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerEmailField.setMaximumSize(new Dimension(520, 38));
        registerEmailField.setPreferredSize(new Dimension(520, 38));
        registerEmailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(registerEmailField);
        panel.add(Box.createVerticalStrut(12));

        // Phone
        addModernField(panel, "Phone Number (Optional)");
        registerPhoneField = new AutoCompleteTextField(20, UserPreferencesCache::getCachedPhoneNumbers);
        ModernTheme.styleTextField(registerPhoneField);
        registerPhoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerPhoneField.setMaximumSize(new Dimension(520, 38));
        registerPhoneField.setPreferredSize(new Dimension(520, 38));
        registerPhoneField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(registerPhoneField);
        panel.add(Box.createVerticalStrut(12));

        // Password
        addModernField(panel, "Password");
        registerPasswordField = new JPasswordField(20);
        styleModernPasswordField(registerPasswordField);
        panel.add(registerPasswordField);
        panel.add(Box.createVerticalStrut(10));

        // Confirm Password
        addModernField(panel, "Confirm Password");
        registerConfirmPasswordField = new JPasswordField(20);
        styleModernPasswordField(registerConfirmPasswordField);
        panel.add(registerConfirmPasswordField);
        panel.add(Box.createVerticalStrut(15));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(ModernTheme.SURFACE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(520, 42));
        
        JButton cancelButton = ModernTheme.createSecondaryButton("Cancel");
        JButton createButton = ModernTheme.createSuccessButton("Create Account");
        createButton.setPreferredSize(new Dimension(130, 34));
        createButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelButton.setPreferredSize(new Dimension(90, 34));
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        panel.add(buttonPanel);

        createButton.addActionListener(e -> attemptRegistration());
        cancelButton.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        return panel;
    }
    
    private void addModernField(JPanel panel, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(ModernTheme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
    }
    
    private void styleModernPasswordField(JPasswordField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(ModernTheme.TEXT_PRIMARY);
        field.setBackground(ModernTheme.SURFACE);
        field.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, ModernTheme.BORDER),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
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
    
    public boolean isSucceeded() {
        return succeeded;
    }
}
