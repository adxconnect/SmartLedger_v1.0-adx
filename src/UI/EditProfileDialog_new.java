package src.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import src.auth.Account;
import src.auth.PasswordHasher;
import src.auth.SessionContext;
import src.db.DBHelper;

/**
 * Modern dialog for editing user profile information with PAN card, password change, and profile picture
 */
public class EditProfileDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern PAN_PATTERN = Pattern.compile(
        "^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    private JTextField nameField;
    private JComboBox<Account.AccountType> accountTypeField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField panCardField;
    private JLabel emailValidLabel;
    private JLabel panValidLabel;
    private JLabel profilePictureLabel;
    private JButton saveButton;
    private JButton changePasswordButton;
    private JButton uploadPictureButton;
    
    private String newProfilePicturePath = null;
    private boolean succeeded = false;
    private Account currentAccount;

    public EditProfileDialog(Frame owner) {
        super(owner, "Edit Profile", true);
        this.currentAccount = SessionContext.getCurrentAccount();
        buildUI();
        setResizable(false);
        setSize(600, 750);
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
                g2.setColor(new Color(34, 139, 34)); // Green header
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
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Update your account information");
        subtitleLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(230, 255, 230));
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitleLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Content Panel with Scroll
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(ModernTheme.BACKGROUND);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 32, 24, 32));
        
        // Profile Picture Section
        JPanel pictureCard = createModernCard();
        pictureCard.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        
        profilePictureLabel = new JLabel();
        profilePictureLabel.setPreferredSize(new Dimension(120, 120));
        profilePictureLabel.setHorizontalAlignment(SwingConstants.CENTER);
        profilePictureLabel.setVerticalAlignment(SwingConstants.CENTER);
        loadProfilePicture();
        
        JPanel picturePanel = new JPanel(new BorderLayout(10, 10));
        picturePanel.setOpaque(false);
        picturePanel.add(profilePictureLabel, BorderLayout.CENTER);
        
        uploadPictureButton = ModernTheme.createSecondaryButton("Upload Picture");
        uploadPictureButton.setPreferredSize(new Dimension(140, 36));
        uploadPictureButton.addActionListener(e -> uploadProfilePicture());
        picturePanel.add(uploadPictureButton, BorderLayout.SOUTH);
        
        pictureCard.add(picturePanel);
        contentPanel.add(pictureCard);
        contentPanel.add(Box.createVerticalStrut(16));
        
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
        
        emailField = new JTextField(currentAccount.getEmail() != null ? currentAccount.getEmail() : "");
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
        phoneField = new JTextField(currentAccount.getPhone() != null ? currentAccount.getPhone() : "");
        phoneField.setPreferredSize(new Dimension(300, 38));
        ModernTheme.styleTextField(phoneField);
        profileCard.add(phoneField, gbc);
        
        // PAN Card
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        JLabel panLabel = new JLabel("PAN Card");
        panLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        panLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        profileCard.add(panLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel panPanel = new JPanel();
        panPanel.setLayout(new BoxLayout(panPanel, BoxLayout.Y_AXIS));
        panPanel.setOpaque(false);
        
        panCardField = new JTextField(currentAccount.getPanCard() != null ? currentAccount.getPanCard() : "");
        panCardField.setPreferredSize(new Dimension(300, 38));
        panCardField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        ModernTheme.styleTextField(panCardField);
        
        panValidLabel = new JLabel();
        panValidLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 11));
        
        panCardField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validatePAN(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validatePAN(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validatePAN(); }
        });
        
        panPanel.add(panCardField);
        panPanel.add(Box.createVerticalStrut(4));
        panPanel.add(panValidLabel);
        
        profileCard.add(panPanel, gbc);
        
        contentPanel.add(profileCard);
        contentPanel.add(Box.createVerticalStrut(16));
        
        // Password Change Section
        JPanel passwordCard = createModernCard();
        passwordCard.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        passwordLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        
        changePasswordButton = ModernTheme.createSecondaryButton("Change Password");
        changePasswordButton.setPreferredSize(new Dimension(160, 38));
        changePasswordButton.addActionListener(e -> changePassword());
        
        passwordCard.add(passwordLabel);
        passwordCard.add(changePasswordButton);
        
        contentPanel.add(passwordCard);
        
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
        validatePAN();
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
    
    private void loadProfilePicture() {
        String picturePath = currentAccount.getProfilePicturePath();
        
        if (picturePath != null && !picturePath.isEmpty() && new File(picturePath).exists()) {
            try {
                Image img = ImageIO.read(new File(picturePath));
                Image scaled = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                
                // Create circular image
                ImageIcon icon = new ImageIcon(scaled);
                profilePictureLabel.setIcon(icon);
            } catch (IOException e) {
                setDefaultProfilePicture();
            }
        } else {
            setDefaultProfilePicture();
        }
    }
    
    private void setDefaultProfilePicture() {
        profilePictureLabel.setIcon(ModernIcons.create(ModernIcons.IconType.USER, ModernTheme.TEXT_SECONDARY, 80));
        profilePictureLabel.setText("No Photo");
        profilePictureLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 11));
        profilePictureLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        profilePictureLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        profilePictureLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    }
    
    private void uploadProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            try {
                // Create profile_pictures directory if it doesn't exist
                File profilePicDir = new File("profile_pictures");
                if (!profilePicDir.exists()) {
                    profilePicDir.mkdirs();
                }
                
                // Copy file to profile_pictures directory
                String fileName = "user_" + currentAccount.getId() + "_" + System.currentTimeMillis() + getFileExtension(selectedFile.getName());
                File destFile = new File(profilePicDir, fileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                newProfilePicturePath = destFile.getAbsolutePath();
                
                // Load and display the new picture
                Image img = ImageIO.read(destFile);
                Image scaled = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaled);
                profilePictureLabel.setIcon(icon);
                profilePictureLabel.setText("");
                
                JOptionPane.showMessageDialog(this, 
                    "Profile picture uploaded! Click 'Save Changes' to apply.", 
                    "Picture Uploaded", 
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to upload picture: " + e.getMessage(), 
                    "Upload Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : ".jpg";
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
    
    private void validatePAN() {
        String pan = panCardField.getText().trim().toUpperCase();
        if (pan.isEmpty()) {
            panValidLabel.setText("");
            return;
        }
        
        if (PAN_PATTERN.matcher(pan).matches()) {
            panValidLabel.setText("✓ Valid PAN format");
            panValidLabel.setForeground(ModernTheme.SUCCESS);
        } else {
            panValidLabel.setText("✗ Invalid PAN (Format: ABCDE1234F)");
            panValidLabel.setForeground(ModernTheme.DANGER);
        }
    }
    
    private void changePassword() {
        // Create password change dialog
        JDialog passwordDialog = new JDialog(this, "Change Password", true);
        passwordDialog.setLayout(new BorderLayout(10, 10));
        passwordDialog.setSize(450, 350);
        passwordDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Current Password
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField currentPasswordField = new JPasswordField(20);
        ModernTheme.styleTextField(currentPasswordField);
        panel.add(currentPasswordField, gbc);
        
        // New Password
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField newPasswordField = new JPasswordField(20);
        ModernTheme.styleTextField(newPasswordField);
        panel.add(newPasswordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmPasswordField = new JPasswordField(20);
        ModernTheme.styleTextField(confirmPasswordField);
        panel.add(confirmPasswordField, gbc);
        
        passwordDialog.add(panel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelBtn = ModernTheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> passwordDialog.dispose());
        
        JButton changeBtn = ModernTheme.createPrimaryButton("Change Password");
        changeBtn.addActionListener(e -> {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(passwordDialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(passwordDialog, "New passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(passwordDialog, "Password must be at least 6 characters!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // Verify current password
                String hashedCurrent = PasswordHasher.hashPassword(currentPassword.toCharArray(), currentAccount.getPasswordSalt());
                if (!hashedCurrent.equals(currentAccount.getPasswordHash())) {
                    JOptionPane.showMessageDialog(passwordDialog, "Current password is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Generate new hash
                String newSalt = PasswordHasher.generateSalt();
                String newHash = PasswordHasher.hashPassword(newPassword.toCharArray(), newSalt);
                
                // Update in database
                DBHelper dbHelper = new DBHelper();
                Connection conn = dbHelper.getConnection();
                String sql = "UPDATE accounts SET password_hash = ?, password_salt = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, newHash);
                    pstmt.setString(2, newSalt);
                    pstmt.setInt(3, currentAccount.getId());
                    
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        currentAccount.setPasswordHash(newHash);
                        currentAccount.setPasswordSalt(newSalt);
                        
                        JOptionPane.showMessageDialog(passwordDialog, 
                            "Password changed successfully!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        passwordDialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(passwordDialog, 
                            "Failed to change password.", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(passwordDialog, 
                    "Error changing password: " + ex.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(changeBtn);
        passwordDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        passwordDialog.setVisible(true);
    }
    
    private void saveProfile() {
        // Validate inputs
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pan = panCardField.getText().trim().toUpperCase();
        
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
        
        if (!pan.isEmpty() && !PAN_PATTERN.matcher(pan).matches()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid PAN card number (Format: ABCDE1234F)", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            panCardField.requestFocus();
            return;
        }
        
        // Update account in database
        try {
            DBHelper dbHelper = new DBHelper();
            Connection conn = dbHelper.getConnection();
            
            String profilePicPath = newProfilePicturePath != null ? newProfilePicturePath : currentAccount.getProfilePicturePath();
            
            String sql = "UPDATE accounts SET account_name = ?, account_type = ?, email = ?, phone = ?, pan_card = ?, profile_picture_path = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, ((Account.AccountType) accountTypeField.getSelectedItem()).name());
                pstmt.setString(3, email.isEmpty() ? null : email);
                pstmt.setString(4, phone.isEmpty() ? null : phone);
                pstmt.setString(5, pan.isEmpty() ? null : pan);
                pstmt.setString(6, profilePicPath);
                pstmt.setInt(7, currentAccount.getId());
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // Update session context
                    currentAccount.setAccountName(name);
                    currentAccount.setAccountType((Account.AccountType) accountTypeField.getSelectedItem());
                    currentAccount.setEmail(email.isEmpty() ? null : email);
                    currentAccount.setPhone(phone.isEmpty() ? null : phone);
                    currentAccount.setPanCard(pan.isEmpty() ? null : pan);
                    currentAccount.setProfilePicturePath(profilePicPath);
                    
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
