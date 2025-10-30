package src.UI;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

import javax.swing.*;

import src.auth.AuthManager;
import src.auth.EmailService;
import src.auth.OtpService;
import src.auth.UserPreferencesCache;

/**
 * Dialog for password reset using email OTP verification.
 */
public class ForgotPasswordDialog extends JDialog {
    
    private final AuthManager authManager;
    private AutoCompleteTextField emailField;
    private JTextField otpField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    
    private JButton sendOtpButton;
    private JButton verifyButton;
    private JButton cancelButton;
    
    private String currentEmail;
    private boolean otpVerified = false;
    
    public ForgotPasswordDialog(Frame owner, AuthManager authManager) {
        super(owner, "Reset Password", true);
        this.authManager = authManager;
        buildModernUi();
        setResizable(false);
        setSize(520, 580);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(ModernTheme.BACKGROUND);
    }
    
    private void buildModernUi() {
        setLayout(new BorderLayout());
        
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(ModernTheme.BACKGROUND);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // Header with icon
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(ModernTheme.BACKGROUND);
        
        // Title
        JLabel titleLabel = new JLabel("Reset Your Password");
        titleLabel.setFont(ModernTheme.FONT_TITLE);
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Enter your email to receive an OTP");
        subtitleLabel.setFont(ModernTheme.FONT_BODY);
        subtitleLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalStrut(25));
        
        mainContainer.add(headerPanel);
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(ModernTheme.SURFACE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(ModernTheme.CARD_RADIUS, ModernTheme.BORDER),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        
        // Email field
        addModernLabel(formPanel, "Email Address");
        emailField = new AutoCompleteTextField(30, UserPreferencesCache::getCachedEmails);
        String lastEmail = UserPreferencesCache.getLastEmail();
        if (lastEmail != null && !lastEmail.isEmpty()) {
            emailField.setTextQuietly(lastEmail);
        }
        ModernTheme.styleTextField(emailField);
        emailField.setMaximumSize(new Dimension(420, 42));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(emailField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // OTP field
        addModernLabel(formPanel, "OTP Code");
        otpField = new JTextField(30);
        ModernTheme.styleTextField(otpField);
        otpField.setMaximumSize(new Dimension(420, 42));
        otpField.setAlignmentX(Component.LEFT_ALIGNMENT);
        otpField.setEnabled(false);
        formPanel.add(otpField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // New password field
        addModernLabel(formPanel, "New Password");
        newPasswordField = new JPasswordField(30);
        styleModernPasswordField(newPasswordField);
        newPasswordField.setEnabled(false);
        formPanel.add(newPasswordField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Confirm password field
        addModernLabel(formPanel, "Confirm Password");
        confirmPasswordField = new JPasswordField(30);
        styleModernPasswordField(confirmPasswordField);
        confirmPasswordField.setEnabled(false);
        formPanel.add(confirmPasswordField);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Button panel inside form
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ModernTheme.SURFACE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(420, 50));
        
        sendOtpButton = ModernTheme.createPrimaryButton("Send OTP");
        verifyButton = ModernTheme.createSuccessButton("Reset Password");
        cancelButton = ModernTheme.createSecondaryButton("Cancel");
        
        sendOtpButton.setPreferredSize(new Dimension(120, 42));
        verifyButton.setPreferredSize(new Dimension(150, 42));
        cancelButton.setPreferredSize(new Dimension(100, 42));
        
        verifyButton.setEnabled(false);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(sendOtpButton);
        buttonPanel.add(verifyButton);
        
        formPanel.add(buttonPanel);
        
        mainContainer.add(formPanel);
        
        // Action listeners
        sendOtpButton.addActionListener(e -> sendOtp());
        verifyButton.addActionListener(e -> verifyAndResetPassword());
        cancelButton.addActionListener(e -> dispose());
        
        add(mainContainer);
    }
    
    private void addModernLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(ModernTheme.FONT_BODY);
        label.setForeground(ModernTheme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
    }
    
    private void styleModernPasswordField(JPasswordField field) {
        field.setFont(ModernTheme.FONT_BODY);
        field.setForeground(ModernTheme.TEXT_PRIMARY);
        field.setBackground(ModernTheme.SURFACE);
        field.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, ModernTheme.BORDER),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setMaximumSize(new Dimension(420, 42));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    
    private void sendOtp() {
        String email = emailField.getText().trim();
        
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter your email address.", 
                "Email Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Check if account exists
            if (!authManager.findByEmail(email).isPresent()) {
                JOptionPane.showMessageDialog(this, 
                    "No account found with this email address.", 
                    "Account Not Found", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Generate OTP
            String otp = OtpService.generateOtp(email);
            currentEmail = email;
            
            // Send OTP via email (simulated in console for free solution)
            String accountName = authManager.findByEmail(email).get().getAccountName();
            EmailService.sendPasswordResetOtp(email, accountName, otp);
            
            // Update UI
            emailField.setEnabled(false);
            sendOtpButton.setEnabled(false);
            otpField.setEnabled(true);
            newPasswordField.setEnabled(true);
            confirmPasswordField.setEnabled(true);
            verifyButton.setEnabled(true);
            
            JOptionPane.showMessageDialog(this, 
                "OTP has been sent to your email.\nCheck your email (or console for development) and enter the OTP.", 
                "OTP Sent", 
                JOptionPane.INFORMATION_MESSAGE);
            
            otpField.requestFocus();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Database error: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void verifyAndResetPassword() {
        String otp = otpField.getText().trim();
        char[] newPassword = newPasswordField.getPassword();
        char[] confirmPassword = confirmPasswordField.getPassword();
        
        try {
            // Validate inputs
            if (otp.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter the OTP code.", 
                    "OTP Required", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (newPassword.length == 0) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a new password.", 
                    "Password Required", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!Arrays.equals(newPassword, confirmPassword)) {
                JOptionPane.showMessageDialog(this, 
                    "Passwords do not match.", 
                    "Password Mismatch", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Verify OTP
            if (!OtpService.verifyOtp(currentEmail, otp)) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid or expired OTP. Please try again.", 
                    "Verification Failed", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Reset password
            authManager.resetPassword(currentEmail, newPassword);
            
            JOptionPane.showMessageDialog(this, 
                "Password has been reset successfully!\nYou can now login with your new password.", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error resetting password: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(newPassword, '\0');
            Arrays.fill(confirmPassword, '\0');
        }
    }
}
