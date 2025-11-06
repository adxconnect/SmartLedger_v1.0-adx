package src.UI;

import javax.swing.*;
import java.awt.*;

/**
 * Simple mockup to test login screen layout with the larger logo
 */
public class LoginMockup extends JFrame {
    
    public LoginMockup() {
        setTitle("Login Screen Mockup - Logo Size Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Set background color like the real login dialog
        getContentPane().setBackground(new Color(248, 249, 250));
        
        // Main container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(new Color(248, 249, 250));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        // Header panel with logo
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(248, 249, 250));
        
        // Add some top spacing
        headerPanel.add(Box.createVerticalStrut(20));
        
        // Logo
        LogoPanel logo = LogoPanel.createLoginLogo();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Wrap logo in a centered panel to ensure proper centering
        JPanel logoContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoContainer.setBackground(new Color(248, 249, 250));
        logoContainer.add(logo);
        
        headerPanel.add(logoContainer);
        
        // Add spacing after logo
        headerPanel.add(Box.createVerticalStrut(30));
        
        // Welcome text
        JLabel titleLabel = new JLabel("Welcome Back!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(51, 51, 51));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        
        headerPanel.add(Box.createVerticalStrut(10));
        
        JLabel subtitleLabel = new JLabel("Sign in to continue to Finance Manager");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(108, 117, 125));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(subtitleLabel);
        
        // Mock login form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(248, 249, 250));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Email field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(emailField, gbc);
        
        // Password field
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(passwordField, gbc);
        
        // Sign in button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton signInButton = new JButton("Sign In");
        signInButton.setPreferredSize(new Dimension(120, 40));
        signInButton.setBackground(new Color(0, 123, 255));
        signInButton.setForeground(Color.WHITE);
        signInButton.setFocusPainted(false);
        formPanel.add(signInButton, gbc);
        
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(formPanel, BorderLayout.CENTER);
        
        add(mainContainer, BorderLayout.CENTER);
        
        setSize(600, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginMockup());
    }
}