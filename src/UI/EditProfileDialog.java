package src.UI;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import src.auth.Account;
import src.auth.SessionContext;
import src.db.DBHelper;

/**
 * Modern dialog for editing user profile information
 */
public class EditProfileDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        Pattern.CASE_INSENSITIVE);

    private JTextField nameField;
    private JComboBox<Account.AccountType> accountTypeField;
    private JTextField emailField;
    private JTextField phoneField;
    private JLabel emailValidLabel;
    private JButton saveButton;
    
    private boolean succeeded = false;
    private Account currentAccount;

    public EditProfileDialog(Frame owner) {
        super(owner, "Edit Profile", true);
        this.currentAccount = SessionContext.getCurrentAccount();
        buildUI();
        setResizable(false);
        setSize(550, 600);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(ModernTheme.BACKGROUND);
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        
        // Header Panel
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ModernTheme.SURFACE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        
        JLabel titleLabel = new JLabel("Edit Profile");
        titleLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 24));
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Update your account information");
        subtitleLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 13));
        subtitleLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitleLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(ModernTheme.BACKGROUND);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 32, 24, 32));
        
        // Profile Information Card
        JPanel profileCard = createModernCard();
        profileCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Account Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel nameLabel = new JLabel("Account Name");
        nameLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        nameLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        profileCard.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new JTextField(currentAccount.getAccountName());
        nameField.setPreferredSize(new Dimension(300, 38));
        ModernTheme.styleTextField(nameField);
        profileCard.add(nameField, gbc);
        
        // Account Type
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel typeLabel = new JLabel("Account Type");
        typeLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        typeLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        profileCard.add(typeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        accountTypeField = new JComboBox<>(Account.AccountType.values());
        accountTypeField.setSelectedItem(currentAccount.getAccountType());
        accountTypeField.setPreferredSize(new Dimension(300, 38));
        ModernTheme.styleComboBox(accountTypeField);
        profileCard.add(accountTypeField, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        emailLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        profileCard.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel emailPanel = new JPanel();
        emailPanel.setLayout(new BoxLayout(emailPanel, BoxLayout.Y_AXIS));
        emailPanel.setOpaque(false);
        
        emailField = new JTextField(currentAccount.getEmail());
        emailField.setPreferredSize(new Dimension(300, 38));
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        ModernTheme.styleTextField(emailField);
        
        emailValidLabel = new JLabel();
        emailValidLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 11));
        
        emailField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateEmail(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateEmail(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateEmail(); }
        });
        
        emailPanel.add(emailField);
        emailPanel.add(Box.createVerticalStrut(4));
        emailPanel.add(emailValidLabel);
        
        profileCard.add(emailPanel, gbc);
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        JLabel phoneLabel = new JLabel("Phone");
        phoneLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        phoneLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        profileCard.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        phoneField = new JTextField(currentAccount.getPhone());
        phoneField.setPreferredSize(new Dimension(300, 38));
        ModernTheme.styleTextField(phoneField);
        profileCard.add(phoneField, gbc);
        
        contentPanel.add(profileCard);
        contentPanel.add(Box.createVerticalStrut(16));
        
        // Information Note
        JPanel notePanel = createInfoNote();
        contentPanel.add(notePanel);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Footer Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        footerPanel.setBackground(ModernTheme.SURFACE);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER));
        
        JButton cancelButton = ModernTheme.createSecondaryButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 38));
        cancelButton.addActionListener(e -> dispose());
        
        saveButton = ModernTheme.createPrimaryButton("Save Changes");
        saveButton.setPreferredSize(new Dimension(140, 38));
        saveButton.addActionListener(e -> saveProfile());
        
        footerPanel.add(cancelButton);
        footerPanel.add(saveButton);
        
        add(footerPanel, BorderLayout.SOUTH);
        
        // Initial validation
        validateEmail();
    }
    
    private JPanel createModernCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBackground(ModernTheme.SURFACE);
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        return card;
    }
    
    private JPanel createInfoNote() {
        JPanel notePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ModernTheme.isDarkMode() ? new java.awt.Color(59, 130, 246, 30) : new java.awt.Color(59, 130, 246, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        notePanel.setOpaque(false);
        notePanel.setLayout(new BoxLayout(notePanel, BoxLayout.X_AXIS));
        notePanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        
        JLabel iconLabel = new JLabel("ℹ️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        
        JLabel noteText = new JLabel("<html><b>Note:</b> Password changes require separate authentication. Contact support to change your password.</html>");
        noteText.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 12));
        noteText.setForeground(ModernTheme.TEXT_PRIMARY);
        
        notePanel.add(iconLabel);
        notePanel.add(Box.createHorizontalStrut(12));
        notePanel.add(noteText);
        
        return notePanel;
    }
    
    private void validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailValidLabel.setText("");
            return;
        }
        
        if (EMAIL_PATTERN.matcher(email).matches()) {
            emailValidLabel.setText("✓ Valid email");
            emailValidLabel.setForeground(ModernTheme.SUCCESS);
        } else {
            emailValidLabel.setText("✗ Invalid email format");
            emailValidLabel.setForeground(ModernTheme.DANGER);
        }
    }
    
    private void saveProfile() {
        // Validate inputs
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Account name is required", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return;
        }
        
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid email address", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            emailField.requestFocus();
            return;
        }
        
        // Update account in database
        try {
            DBHelper dbHelper = new DBHelper();
            Connection conn = dbHelper.getConnection();
            String sql = "UPDATE accounts SET account_name = ?, account_type = ?, email = ?, phone = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, ((Account.AccountType) accountTypeField.getSelectedItem()).name());
                pstmt.setString(3, email.isEmpty() ? null : email);
                pstmt.setString(4, phone.isEmpty() ? null : phone);
                pstmt.setInt(5, currentAccount.getId());
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // Update session context
                    currentAccount.setAccountName(name);
                    currentAccount.setAccountType((Account.AccountType) accountTypeField.getSelectedItem());
                    currentAccount.setEmail(email.isEmpty() ? null : email);
                    currentAccount.setPhone(phone.isEmpty() ? null : phone);
                    
                    SessionContext.setCurrentAccount(currentAccount);
                    
                    succeeded = true;
                    
                    JOptionPane.showMessageDialog(this, 
                        "Profile updated successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to update profile. Please try again.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error updating profile: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
