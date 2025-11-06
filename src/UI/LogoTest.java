package src.UI;

import javax.swing.*;
import java.awt.*;

/**
 * Simple test to view the logo in different sizes
 */
public class LogoTest extends JFrame {
    
    public LogoTest() {
        setTitle("Logo Size Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main panel
        JPanel mainPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Original size (120px)
        LogoPanel logo120 = new LogoPanel(120, true);
        JPanel panel120 = new JPanel(new FlowLayout());
        panel120.add(new JLabel("120px Logo: "));
        panel120.add(logo120);
        
        // New login size (180px)
        LogoPanel logoLogin = LogoPanel.createLoginLogo();
        JPanel panelLogin = new JPanel(new FlowLayout());
        panelLogin.add(new JLabel("Login Logo (180px): "));
        panelLogin.add(logoLogin);
        
        // Larger size (240px)  
        LogoPanel logo240 = new LogoPanel(240, true);
        JPanel panel240 = new JPanel(new FlowLayout());
        panel240.add(new JLabel("240px Logo: "));
        panel240.add(logo240);
        
        mainPanel.add(panel120);
        mainPanel.add(panelLogin);
        mainPanel.add(panel240);
        
        add(mainPanel, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LogoTest());
    }
}