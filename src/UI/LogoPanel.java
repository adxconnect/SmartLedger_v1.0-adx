package src.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;

/**
 * Custom logo component for SmartLedger.
 * Displays a modern, professional logo with icon and text.
 * Supports light mode (Logo1.png) and dark mode (Logo2.png).
 */
public class LogoPanel extends JPanel {
    
    private boolean showText;
    private int size;
    private Image lightModeLogo;
    private Image darkModeLogo;
    private String customLogoFile; // For custom logo files like Logo3.png
    
    public LogoPanel(int size, boolean showText) {
        this.size = size;
        this.showText = showText;
        this.customLogoFile = null;
        setOpaque(false);
        
        // Load both PNG logo images for light and dark modes
        loadLogoImages();
        
        // Update size after loading - if we have custom logo, don't need extra space for text
        updatePreferredSize();
    }
    
    /**
     * Constructor for using a specific custom logo file
     */
    public LogoPanel(int size, boolean showText, String customLogoFile) {
        this.size = size;
        this.showText = showText;
        this.customLogoFile = customLogoFile;
        setOpaque(false);
        
        // Load custom logo
        loadLogoImages();
        
        // Update size after loading
        updatePreferredSize();
    }
    
    private void updatePreferredSize() {
        // If we have a custom PNG logo, only use the logo size (no text)
        // If we don't have custom logo and showText is true, allocate space for text
        boolean needsTextSpace = showText && lightModeLogo == null && darkModeLogo == null;
        setPreferredSize(new Dimension(needsTextSpace ? size * 4 : size, size));
        
        // Ensure proper alignment for centering
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setAlignmentY(Component.CENTER_ALIGNMENT);
        
        revalidate();
    }
    
    private void loadLogoImages() {
        // If a custom logo file is specified, use it for both modes
        if (customLogoFile != null && !customLogoFile.isEmpty()) {
            Image customLogo = loadLogoFromPath(customLogoFile);
            lightModeLogo = customLogo;
            darkModeLogo = customLogo;
            if (customLogo != null) {
                System.out.println("Custom logo (" + customLogoFile + ") loaded successfully");
            } else {
                System.out.println("Custom logo (" + customLogoFile + ") not found, falling back to programmatic logo");
            }
        } else {
            // Load light mode logo (Logo1.png)
            lightModeLogo = loadLogoFromPath("Logo1.png");
            
            // Load dark mode logo (Logo2.png)
            darkModeLogo = loadLogoFromPath("Logo2.png");
            
            if (lightModeLogo != null || darkModeLogo != null) {
                System.out.println("SmartLedger logos loaded successfully");
            } else {
                System.out.println("Logo images not found, falling back to programmatic logo");
            }
        }
    }
    
    private Image loadLogoFromPath(String filename) {
        try {
            // Try multiple paths for logo files
            
            // Path 1: Try from logo directory (relative to project root)
            java.io.File logoFile = new java.io.File("logo/" + filename);
            if (logoFile.exists()) {
                return ImageIO.read(logoFile);
            }
            
            // Path 2: Try from resources folder in classpath
            InputStream imageStream = getClass().getResourceAsStream("/resources/" + filename);
            if (imageStream != null) {
                Image img = ImageIO.read(imageStream);
                imageStream.close();
                return img;
            }
            
            // Path 3: Try from src/resources
            logoFile = new java.io.File("src/resources/" + filename);
            if (logoFile.exists()) {
                return ImageIO.read(logoFile);
            }
            
        } catch (IOException e) {
            System.err.println("Error loading " + filename + ": " + e.getMessage());
        }
        
        return null; // Return null if not found
    }
    
    /**
     * Refresh the logo display (useful after dark mode toggle)
     */
    public void refreshLogo() {
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw logo icon (PNG image based on theme or fallback to programmatic logo)
        drawLogoIcon(g2, 0, 0, size);
        
        // Draw text if enabled (but only if we're using programmatic logo)
        if (showText && lightModeLogo == null && darkModeLogo == null) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
            g2.setColor(ModernTheme.TEXT_PRIMARY);
            
            String text = "SmartLedger";
            FontMetrics fm = g2.getFontMetrics();
            int textX = size + 10;
            int textY = (size + fm.getAscent()) / 2 - 2;
            g2.drawString(text, textX, textY);
        }
        
        g2.dispose();
    }
    
    /**
     * Draws the logo icon - PNG image based on theme mode if available, 
     * otherwise fallback to programmatic logo
     */
    private void drawLogoIcon(Graphics2D g2, int x, int y, int size) {
        // Select the appropriate logo based on dark mode state
        Image currentLogo = ModernTheme.isDarkMode() ? darkModeLogo : lightModeLogo;
        
        // If the appropriate logo is not available, try the other one
        if (currentLogo == null) {
            currentLogo = ModernTheme.isDarkMode() ? lightModeLogo : darkModeLogo;
        }
        
        // If PNG image is loaded, use it
        if (currentLogo != null) {
            // Draw the PNG image scaled to fit the size
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(currentLogo, x, y, size, size, null);
            return;
        }
        
        // Fallback to programmatic logo if PNG not available
        // Create gradient for background circle
        GradientPaint gradient = new GradientPaint(
            x, y, ModernTheme.PRIMARY,
            x + size, y + size, ModernTheme.PRIMARY_DARK
        );
        
        // Draw circular background
        g2.setPaint(gradient);
        g2.fillOval(x, y, size, size);
        
        // Draw wallet outline
        g2.setColor(ModernTheme.TEXT_WHITE);
        g2.setStroke(new BasicStroke(size / 20f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        int padding = size / 5;
        int walletWidth = size - (padding * 2);
        int walletHeight = (int) (walletWidth * 0.7);
        int walletX = x + padding;
        int walletY = y + (size - walletHeight) / 2;
        
        // Wallet body (rounded rectangle)
        RoundRectangle2D wallet = new RoundRectangle2D.Float(
            walletX, walletY, walletWidth, walletHeight,
            size / 10f, size / 10f
        );
        g2.draw(wallet);
        
        // Wallet flap (curved line at top)
        int flapY = walletY + walletHeight / 4;
        Path2D flap = new Path2D.Float();
        flap.moveTo(walletX, flapY);
        flap.quadTo(walletX + walletWidth / 2, flapY - size / 15, walletX + walletWidth, flapY);
        g2.draw(flap);
        
        // Dollar sign in the center
        g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
        String dollarSign = "$";
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (size - fm.stringWidth(dollarSign)) / 2;
        int textY = y + (size + fm.getAscent()) / 2 - 2;
        g2.drawString(dollarSign, textX, textY);
    }
    
    /**
     * Creates a small logo for sidebar
     */
    public static LogoPanel createSmallLogo() {
        return new LogoPanel(40, false);
    }
    
    /**
     * Creates an icon-only logo (no text) for collapsed sidebar
     */
    public static LogoPanel createIconOnlyLogo(int size) {
        return new LogoPanel(size, false);
    }
    
    /**
     * Creates a large logo with text for header
     */
    public static LogoPanel createHeaderLogo() {
        return new LogoPanel(155, true); // Increased to 155px for much better visibility
    }
    
    /**
     * Creates a login screen logo
     */
    public static LogoPanel createLoginLogo() {
        return new LogoPanel(280, true); // Increased size for a more prominent centered login logo
    }
    
    /**
     * Creates a login screen logo with custom logo file
     */
    public static LogoPanel createLoginLogoWithCustomFile(int size, String logoFile) {
        return new LogoPanel(size, true, logoFile);
    }
}
