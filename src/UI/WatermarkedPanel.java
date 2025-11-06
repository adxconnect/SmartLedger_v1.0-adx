package src.UI;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Font;
import javax.swing.JPanel;

/**
 * A custom JPanel that paints a large, faint watermark in its center.
 * The watermark (Rupee symbol) and its color are pulled from the ModernTheme
 * to automatically support dark and light modes.
 */
public class WatermarkedPanel extends JPanel {

    private String watermarkSymbol = "\u20B9"; // Unicode for Indian Rupee Symbol (₹)

    public WatermarkedPanel() {
        super();
        // Set the background color from the theme.
        // This is crucial for dark mode to work correctly.
        setBackground(ModernTheme.BACKGROUND);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Set background first
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D g2d = (Graphics2D) g.create();

        try {
            // 1. Set Watermark Properties
            // Use a very large, bold font. Adjust "450" as needed.
            Font watermarkFont = new Font("Roboto", Font.BOLD, 450); // Using Roboto as we discussed
            g2d.setFont(watermarkFont);

            // 2. Set Color & Opacity (Transparency)
            // Pulls the secondary text color from your theme
            g2d.setColor(ModernTheme.TEXT_SECONDARY);

            // Set the opacity. 0.05f is 5% visible. This is what makes it faint.
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));

            // 3. Enable Anti-Aliasing (for smooth text)
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 4. Calculate Center Position
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            int stringWidth = g2d.getFontMetrics().stringWidth(watermarkSymbol);
            int stringAscent = g2d.getFontMetrics().getAscent();
            int stringDescent = g2d.getFontMetrics().getDescent();
            int stringHeight = stringAscent + stringDescent;

            // Center the symbol
            int x = (panelWidth - stringWidth) / 2;
            int y = ((panelHeight - stringHeight) / 2) + stringAscent;

            // 5. Draw the Watermark
            g2d.drawString(watermarkSymbol, x, y);

        } finally {
            // Always dispose of the graphics context when done
            g2d.dispose();
        }
        
        // IMPORTANT: Call super.paintComponent(g) *after* painting the background
        // so that child components (like labels) are drawn on top.
        super.paintComponent(g); 
    }

    // Call this when you toggle dark mode to force a repaint
    public void updateTheme() {
        setBackground(ModernTheme.BACKGROUND);
        revalidate();
        repaint();
    }
}