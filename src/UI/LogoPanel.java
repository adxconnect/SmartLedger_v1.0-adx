package src.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Custom logo component for Finance Manager.
 * Displays a modern, professional logo with icon and text.
 */
public class LogoPanel extends JPanel {
    
    private boolean showText;
    private int size;
    
    public LogoPanel(int size, boolean showText) {
        this.size = size;
        this.showText = showText;
        setOpaque(false);
        setPreferredSize(new Dimension(showText ? size * 4 : size, size));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw logo icon (a stylized wallet/money symbol)
        drawLogoIcon(g2, 0, 0, size);
        
        // Draw text if enabled
        if (showText) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
            g2.setColor(ModernTheme.TEXT_PRIMARY);
            
            String text = "FinanceHub";
            FontMetrics fm = g2.getFontMetrics();
            int textX = size + 10;
            int textY = (size + fm.getAscent()) / 2 - 2;
            g2.drawString(text, textX, textY);
        }
        
        g2.dispose();
    }
    
    /**
     * Draws the logo icon - a modern wallet with dollar sign
     */
    private void drawLogoIcon(Graphics2D g2, int x, int y, int size) {
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
     * Creates a large logo with text for header
     */
    public static LogoPanel createHeaderLogo() {
        return new LogoPanel(50, true);
    }
    
    /**
     * Creates a login screen logo
     */
    public static LogoPanel createLoginLogo() {
        return new LogoPanel(80, true);
    }
}
