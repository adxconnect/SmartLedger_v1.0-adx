package src.UI;

import javax.swing.*;
import java.awt.*;

/**
 * Test to show header logo sizes
 */
public class HeaderLogoTest extends JFrame {
    
    public HeaderLogoTest() {
        setTitle("Header Logo Size Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main panel
        JPanel mainPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Original header size (50px)
        LogoPanel logo50 = new LogoPanel(50, true);
        JPanel panel50 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel50.add(new JLabel("Original Header (50px): "));
        panel50.add(logo50);
        
        // New header size (80px)
        LogoPanel logoHeader = LogoPanel.createHeaderLogo();
        JPanel panelHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelHeader.add(new JLabel("New Header (80px): "));
        panelHeader.add(logoHeader);
        
        // Even larger option (100px)
        LogoPanel logo100 = new LogoPanel(100, true);
        JPanel panel100 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel100.add(new JLabel("Larger Header (100px): "));
        panel100.add(logo100);
        
        // Login size for comparison (180px)
        LogoPanel logoLogin = LogoPanel.createLoginLogo();
        JPanel panelLogin = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelLogin.add(new JLabel("Login Size (180px): "));
        panelLogin.add(logoLogin);
        
        mainPanel.add(panel50);
        mainPanel.add(panelHeader);
        mainPanel.add(panel100);
        mainPanel.add(panelLogin);
        
        add(mainPanel, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HeaderLogoTest());
    }
}