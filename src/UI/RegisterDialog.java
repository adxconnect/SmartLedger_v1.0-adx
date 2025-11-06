package src.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import src.auth.Account;
import src.auth.AuthManager;
import src.auth.SessionContext;
import src.auth.UserPreferencesCache;

/**
 * Dedicated popup for modern account registration with a scrollable body
 * and sticky footer actions.
 */
public class RegisterDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        Pattern.CASE_INSENSITIVE);

    private final AuthManager authManager;

    private AutoCompleteTextField nameField;
    private JComboBox<Account.AccountType> accountTypeField;
    private AutoCompleteTextField emailField;
    private JLabel emailHintLabel;
    private AutoCompleteTextField phoneField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JProgressBar passwordStrengthBar;
    private JLabel passwordStrengthText;
    private JLabel passwordMatchLabel;
    private JButton createAccountButton;
    private JScrollPane formScrollPane;

    private JPanel profileCard;
    private JPanel contactCard;
    private JPanel securityCard;
    
    private JPanel headerPanel;
    private JButton darkModeButton;

    private JLabel profileStepChip;
    private JLabel contactStepChip;
    private JLabel securityStepChip;
    private boolean profileStepActive = true;
    private boolean contactStepActive;
    private boolean securityStepActive;

    private boolean succeeded;

    public RegisterDialog(Frame owner, AuthManager authManager) {
        super(owner, "SmartLedger - Create Account", true);
        this.authManager = authManager;
        buildUi();
        setResizable(false);
        setSize(760, 760);
        setMinimumSize(new Dimension(700, 700));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(ModernTheme.BACKGROUND);
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        add(createHeader(), BorderLayout.NORTH);

        JPanel formPanel = buildFormPanel();
        formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(null);
        formScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScrollPane.getViewport().setBackground(ModernTheme.BACKGROUND);
        formScrollPane.setBackground(ModernTheme.BACKGROUND);
        styleScrollBar(formScrollPane);
        add(formScrollPane, BorderLayout.CENTER);

        add(createFooter(), BorderLayout.SOUTH);

        installValidationListeners();
        updateEmailHint();
        updatePasswordStrength();
        updatePasswordMatch();
        updateCreateButtonState();
    }

    private JComponent createHeader() {
        headerPanel = ModernTheme.createGradientPanel(ModernTheme.PRIMARY_DARK, ModernTheme.PRIMARY);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Create your account");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.TEXT_WHITE);
        textPanel.add(title);

        textPanel.add(Box.createVerticalStrut(4));

    JLabel subtitle = new JLabel("Modern onboarding in just a minute.");
    subtitle.setFont(ModernTheme.FONT_BODY);
    subtitle.setForeground(new Color(255, 255, 255, 220));

    JPanel subtitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    subtitleRow.setOpaque(false);
    subtitleRow.add(subtitle);

    JLabel magicIcon = new JLabel(ModernIcons.create(ModernIcons.IconType.MAGIC,
        new Color(255, 255, 255, 230), 32));
    subtitleRow.add(magicIcon);

    subtitleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
    textPanel.add(subtitleRow);

        headerPanel.add(textPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        rightPanel.add(createStepProgressRow());
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel buildFormPanel() {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(18, 22, 22, 22));

        profileCard = createProfileCard();
        contactCard = createContactCard();
        securityCard = createSecurityCard();

        container.add(profileCard);
        container.add(Box.createVerticalStrut(18));
        container.add(contactCard);
        container.add(Box.createVerticalStrut(18));
        container.add(securityCard);

        return container;
    }

    private JPanel createProfileCard() {
        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = baseConstraints();

        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        gc.insets = new Insets(0, 0, 16, 0);
        card.add(sectionTitle("Profile"), gc);

        gc.gridwidth = 1;
        gc.gridy++;
        gc.insets = new Insets(0, 0, 12, 16);
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        card.add(makeFormLabel("Account Name"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 12, 0);
        nameField = new AutoCompleteTextField(24, UserPreferencesCache::getCachedAccountNames);
        ModernTheme.styleTextField(nameField);
        card.add(nameField, gc);

        gc.gridx = 0;
        gc.gridy++;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(0, 0, 0, 16);
        card.add(makeFormLabel("Account Type"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 0, 0);
        accountTypeField = new JComboBox<>(Account.AccountType.values());
        accountTypeField.setSelectedItem(Account.AccountType.PERSONAL);
        ModernTheme.styleComboBox(accountTypeField);
        card.add(accountTypeField, gc);

        return card;
    }

    private JPanel createContactCard() {
        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = baseConstraints();

        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        gc.insets = new Insets(0, 0, 16, 0);
        card.add(sectionTitle("Contact"), gc);

        gc.gridwidth = 1;
        gc.gridy++;
        gc.insets = new Insets(0, 0, 12, 16);
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        card.add(makeFormLabel("Email Address"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 12, 0);
        emailField = new AutoCompleteTextField(24, UserPreferencesCache::getCachedEmails);
        ModernTheme.styleTextField(emailField);
        card.add(emailField, gc);

        gc.gridx = 1;
        gc.gridy++;
        gc.gridwidth = 1;
        gc.insets = new Insets(-6, 0, 12, 0);
        emailHintLabel = helperHint("We'll send verification to this address.");
        card.add(emailHintLabel, gc);

        gc.gridx = 0;
        gc.gridy++;
        gc.gridwidth = 1;
        gc.insets = new Insets(0, 0, 0, 16);
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        card.add(makeFormLabel("Phone Number (Optional)"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 0, 0);
        phoneField = new AutoCompleteTextField(24, UserPreferencesCache::getCachedPhoneNumbers);
        ModernTheme.styleTextField(phoneField);
        card.add(phoneField, gc);

        return card;
    }

    private JPanel createSecurityCard() {
        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = baseConstraints();

        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        gc.insets = new Insets(0, 0, 16, 0);
        card.add(sectionTitle("Security"), gc);

        gc.gridwidth = 1;
        gc.gridy++;
        gc.insets = new Insets(0, 0, 12, 16);
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        card.add(makeFormLabel("Password"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 12, 0);
        passwordField = new JPasswordField(24);
        stylePasswordField(passwordField);
        JPanel passwordWrapper = createPasswordFieldWithToggle(passwordField);
        card.add(passwordWrapper, gc);

        gc.gridx = 0;
        gc.gridy++;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(0, 0, 12, 16);
        card.add(makeFormLabel("Confirm Password"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 12, 0);
        confirmField = new JPasswordField(24);
        stylePasswordField(confirmField);
        JPanel confirmWrapper = createPasswordFieldWithToggle(confirmField);
        card.add(confirmWrapper, gc);

        gc.gridx = 1;
        gc.gridy++;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 12, 0);
        passwordStrengthBar = new JProgressBar(0, 100);
        passwordStrengthBar.setBorder(BorderFactory.createEmptyBorder());
        passwordStrengthBar.setStringPainted(false);
        passwordStrengthBar.setPreferredSize(new Dimension(220, 8));
        card.add(passwordStrengthBar, gc);

        gc.gridy++;
        gc.insets = new Insets(0, 0, 6, 0);
        passwordStrengthText = helperHint("Password strength");
        card.add(passwordStrengthText, gc);

        gc.gridy++;
        gc.insets = new Insets(0, 0, 0, 0);
        passwordMatchLabel = helperHint("Enter password to verify match.");
        card.add(passwordMatchLabel, gc);

        return card;
    }

    private JComponent createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(16, 22, 18, 22));
        footer.setBackground(ModernTheme.SURFACE);

        JLabel signInLink = new JLabel("<html>Already have an account? <a href=''>Sign in</a></html>");
        signInLink.setFont(ModernTheme.FONT_SMALL);
        signInLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signInLink.setForeground(ModernTheme.TEXT_SECONDARY);
        signInLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                succeeded = false;
                dispose();
            }
        });
        footer.add(signInLink, BorderLayout.WEST);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonRow.setOpaque(false);

        JButton cancelButton = ModernTheme.createSecondaryButton("Cancel");
        cancelButton.addActionListener(e -> {
            succeeded = false;
            dispose();
        });
        buttonRow.add(cancelButton);

        createAccountButton = ModernTheme.createSuccessButton("Create Account");
        createAccountButton.setEnabled(false);
        createAccountButton.addActionListener(e -> attemptRegistration());
        buttonRow.add(createAccountButton);

        footer.add(buttonRow, BorderLayout.EAST);

        getRootPane().setDefaultButton(createAccountButton);
        return footer;
    }

    private JPanel createStepProgressRow() {
        JPanel progressRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        progressRow.setOpaque(false);
        profileStepActive = true;
        contactStepActive = false;
        securityStepActive = false;
        profileStepChip = createStepChip("1 Profile", profileStepActive);
        contactStepChip = createStepChip("2 Contact", contactStepActive);
        securityStepChip = createStepChip("3 Security", securityStepActive);
        progressRow.add(profileStepChip);
        progressRow.add(contactStepChip);
        progressRow.add(securityStepChip);
        return progressRow;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ModernTheme.FONT_SUBTITLE);
        label.setForeground(ModernTheme.TEXT_PRIMARY);
        return label;
    }

    private JLabel helperHint(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ModernTheme.FONT_SMALL);
        label.setForeground(ModernTheme.TEXT_SECONDARY);
        return label;
    }

    private JLabel makeFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ModernTheme.FONT_BODY);
        label.setForeground(ModernTheme.TEXT_PRIMARY);
        return label;
    }

    private JLabel createStepChip(String text, boolean active) {
        JLabel chip = new JLabel(text);
        chip.setFont(ModernTheme.FONT_SMALL);
        chip.setOpaque(true);
        styleStepChip(chip, active);
        return chip;
    }

    private void styleStepChip(JLabel chip, boolean active) {
    // All chips get the same PRIMARY blue background and white text
    Color borderColor = ModernTheme.PRIMARY;
        chip.setBorder(BorderFactory.createCompoundBorder(
                new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, borderColor),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
    chip.setBackground(ModernTheme.PRIMARY);
    chip.setForeground(ModernTheme.TEXT_WHITE);
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        return gc;
    }

    private void installValidationListeners() {
        DocumentListener updateAll = onDocumentChange(this::updateCreateButtonState);
        nameField.getDocument().addDocumentListener(updateAll);
        phoneField.getDocument().addDocumentListener(updateAll);

        emailField.getDocument().addDocumentListener(onDocumentChange(() -> {
            updateEmailHint();
            updateCreateButtonState();
        }));

        DocumentListener passwordListener = onDocumentChange(() -> {
            updatePasswordStrength();
            updatePasswordMatch();
            updateCreateButtonState();
        });
        passwordField.getDocument().addDocumentListener(passwordListener);
        confirmField.getDocument().addDocumentListener(passwordListener);
    }

    private void updateEmailHint() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailHintLabel.setText("Use your primary email address.");
            emailHintLabel.setForeground(ModernTheme.TEXT_SECONDARY);
            return;
        }
        if (!isEmailValid(email)) {
            emailHintLabel.setText("Please enter a valid email address.");
            emailHintLabel.setForeground(ModernTheme.DANGER);
            return;
        }
        emailHintLabel.setText("Looks good. Verification goes here.");
        emailHintLabel.setForeground(ModernTheme.SUCCESS);
    }

    private void updatePasswordStrength() {
        char[] password = passwordField.getPassword();
        int score = calculatePasswordScore(password);
        passwordStrengthBar.setValue(score);

        Color barColor;
        String status;
        if (score < 35) {
            barColor = ModernTheme.DANGER;
            status = "Weak password — add more variety.";
        } else if (score < 65) {
            barColor = ModernTheme.WARNING;
            status = "Fair password — consider adding symbols.";
        } else {
            barColor = ModernTheme.SUCCESS;
            status = "Strong password.";
        }
        passwordStrengthBar.setForeground(barColor);
        passwordStrengthText.setText(status);
        passwordStrengthText.setForeground(barColor);
    }

    private void updatePasswordMatch() {
        char[] password = passwordField.getPassword();
        char[] confirm = confirmField.getPassword();

        if (password.length == 0 && confirm.length == 0) {
            passwordMatchLabel.setText("Enter password to verify match.");
            passwordMatchLabel.setForeground(ModernTheme.TEXT_SECONDARY);
            return;
        }

        if (Arrays.equals(password, confirm)) {
            passwordMatchLabel.setText("Passwords match.");
            passwordMatchLabel.setForeground(ModernTheme.SUCCESS);
        } else {
            passwordMatchLabel.setText("Passwords do not match yet.");
            passwordMatchLabel.setForeground(ModernTheme.DANGER);
        }
    }

    private void updateCreateButtonState() {
        boolean requiredFilled = !nameField.getText().trim().isEmpty()
                && isEmailValid(emailField.getText().trim())
                && passwordField.getPassword().length > 0
                && confirmField.getPassword().length > 0
                && Arrays.equals(passwordField.getPassword(), confirmField.getPassword());

        boolean strongEnough = passwordStrengthBar.getValue() >= 50;
        createAccountButton.setEnabled(requiredFilled && strongEnough);
    }

    private int calculatePasswordScore(char[] password) {
        if (password == null || password.length == 0) {
            return 0;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;

        for (char ch : password) {
            if (Character.isUpperCase(ch)) {
                hasUpper = true;
            } else if (Character.isLowerCase(ch)) {
                hasLower = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else {
                hasSymbol = true;
            }
        }

        int lengthScore = Math.min(40, password.length * 4);
        int varietyScore = (hasUpper ? 15 : 0) + (hasLower ? 15 : 0) + (hasDigit ? 15 : 0) + (hasSymbol ? 15 : 0);
        int bonus = password.length >= 12 ? 15 : password.length >= 8 ? 5 : 0;

        return Math.min(100, lengthScore + varietyScore + bonus);
    }

    private void attemptRegistration() {
        String accountName = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        char[] password = passwordField.getPassword();
        char[] confirm = confirmField.getPassword();

        if (accountName.isEmpty() || email.isEmpty() || password.length == 0 || confirm.length == 0) {
            JOptionPane.showMessageDialog(this, "Please complete all required fields.", "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
            return;
        }

        if (!isEmailValid(email)) {
            JOptionPane.showMessageDialog(this, "Please provide a valid email address.", "Invalid Email",
                    JOptionPane.WARNING_MESSAGE);
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
            return;
        }

        if (!Arrays.equals(password, confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
            return;
        }

        Account.AccountType accountType = (Account.AccountType) accountTypeField.getSelectedItem();
        try {
            Account newAccount = authManager.createAccount(
                    accountName,
                    accountType,
                    email,
                    phone.isBlank() ? null : phone,
                    password,
                    null,
                    null);
            authManager.recordLoginEvent(newAccount.getId(), "ACCOUNT_CREATED", null, null);

            UserPreferencesCache.cacheAccountName(accountName);
            UserPreferencesCache.cacheEmail(email);
            if (!phone.isBlank()) {
                UserPreferencesCache.cachePhoneNumber(phone);
            }

            JOptionPane.showMessageDialog(this, "Account created successfully. You are now signed in.", "Welcome",
                    JOptionPane.INFORMATION_MESSAGE);
            SessionContext.setCurrentAccount(newAccount);
            succeeded = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Unable to create account: " + ex.getMessage(), "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
        }
    }

    private void stylePasswordField(JPasswordField field) {
        field.setFont(ModernTheme.FONT_BODY);
        field.setForeground(ModernTheme.TEXT_PRIMARY);
        field.setBackground(ModernTheme.SURFACE);
        field.setBorder(BorderFactory.createCompoundBorder(
                new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, ModernTheme.BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 40)));
        field.setCaretColor(ModernTheme.PRIMARY);
        field.setSelectionColor(ModernTheme.PRIMARY_LIGHT);
        field.setSelectedTextColor(ModernTheme.TEXT_PRIMARY);
    }

    /**
     * Creates a panel with password field and eye toggle button
     */
    private JPanel createPasswordFieldWithToggle(JPasswordField passwordField) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        
        // Add the password field
        wrapper.add(passwordField, BorderLayout.CENTER);
        
        // Create eye toggle button
        JButton eyeButton = new JButton();
        eyeButton.setIcon(ModernIcons.create(ModernIcons.IconType.EYE_OFF, ModernTheme.TEXT_SECONDARY, 18));
        eyeButton.setBorderPainted(false);
        eyeButton.setContentAreaFilled(false);
        eyeButton.setFocusPainted(false);
        eyeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeButton.setPreferredSize(new Dimension(40, passwordField.getPreferredSize().height));
        
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
        
        // Add button to the right (EAST)
        wrapper.add(eyeButton, BorderLayout.EAST);
        
        return wrapper;
    }

    private void styleScrollBar(JScrollPane scrollPane) {
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                // Modern scrollbar colors - more visible
                this.thumbColor = ModernTheme.isDarkMode() ? 
                    new Color(100, 100, 100) : new Color(180, 180, 180);
                this.trackColor = ModernTheme.BACKGROUND;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return zeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return zeroButton();
            }

            private JButton zeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                // Rounded modern scrollbar thumb
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, 
                    thumbBounds.width - 4, thumbBounds.height - 4, 6, 6);
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
        vertical.setPreferredSize(new Dimension(12, 0)); // Slightly wider for better visibility
    }

    private void refreshTheme() {
        // Don't call updateComponentTreeUI - it destroys custom components
        // Instead, manually update all components
        
        // Schedule the refresh on the EDT after theme colors have updated
        java.awt.EventQueue.invokeLater(() -> {
            getContentPane().setBackground(ModernTheme.BACKGROUND);

            // Update card panels
            if (profileCard != null) {
                profileCard.setBackground(ModernTheme.CARD_BG);
                profileCard.setBorder(ModernTheme.createCardBorder());
            }
            if (contactCard != null) {
                contactCard.setBackground(ModernTheme.CARD_BG);
                contactCard.setBorder(ModernTheme.createCardBorder());
            }
            if (securityCard != null) {
                securityCard.setBackground(ModernTheme.CARD_BG);
                securityCard.setBorder(ModernTheme.createCardBorder());
            }

            // Update text fields and combobox with new theme
            ModernTheme.styleTextField(nameField);
            ModernTheme.styleComboBox(accountTypeField);
            ModernTheme.styleTextField(emailField);
            ModernTheme.styleTextField(phoneField);
            stylePasswordField(passwordField);
            stylePasswordField(confirmField);

            // Update scrollbar
            if (formScrollPane != null) {
                formScrollPane.getViewport().setBackground(ModernTheme.BACKGROUND);
                formScrollPane.setBackground(ModernTheme.BACKGROUND);
                styleScrollBar(formScrollPane);
            }
            
            // Refresh dark mode toggle button icon
            if (darkModeButton != null) {
                darkModeButton.repaint();
            }
            
            // Update dynamic labels
            updateEmailHint();
            updatePasswordStrength();
            updatePasswordMatch();
            refreshStepChips();
            
            // Refresh ALL labels in the entire dialog recursively
            refreshLabelsInContainer(getContentPane());
            
            // Force complete repaint
            revalidate();
            repaint();
        });
    }
    
    private void refreshLabelsInContainer(java.awt.Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String text = label.getText();
                
                // Skip empty labels
                if (text == null || text.isEmpty()) {
                    if (comp instanceof java.awt.Container) {
                        refreshLabelsInContainer((java.awt.Container) comp);
                    }
                    continue;
                }
                
                // Determine label type by font size and update with current theme colors
                int fontSize = label.getFont().getSize();
                
                if (fontSize >= 16) { 
                    // Section titles: Profile, Contact, Security
                    label.setForeground(ModernTheme.TEXT_PRIMARY);
                } else if (fontSize <= 12) { 
                    // Hint text: "Use your primary email address"
                    label.setForeground(ModernTheme.TEXT_SECONDARY);
                } else { 
                    // Form labels: Account Name, Email Address, Password, etc.
                    label.setForeground(ModernTheme.TEXT_PRIMARY);
                }
            }
            
            // Recursively refresh all nested containers
            if (comp instanceof java.awt.Container) {
                refreshLabelsInContainer((java.awt.Container) comp);
            }
        }
    }

    private void refreshStepChips() {
        if (profileStepChip != null) {
            styleStepChip(profileStepChip, profileStepActive);
        }
        if (contactStepChip != null) {
            styleStepChip(contactStepChip, contactStepActive);
        }
        if (securityStepChip != null) {
            styleStepChip(securityStepChip, securityStepActive);
        }
    }

    private boolean isEmailValid(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private DocumentListener onDocumentChange(Runnable task) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                task.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                task.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                task.run();
            }
        };
    }
}
