package src.UI;

import javax.swing.*;
import java.awt.*;

/**
 * A lightweight card container that paints a soft drop shadow with rounded
 * corners and no visible border line – ideal for modern UI cards.
 */
public class ShadowPanel extends JPanel {
    private final int cornerRadius;
    private final int shadowSize;
    private final float shadowAlpha; // 0..1

    /**
     * @param bg           Background color of the card surface
     * @param cornerRadius Corner radius for the card
     * @param shadowSize   Thickness of the shadow halo in pixels
     * @param shadowAlpha  Max alpha (0..1) of the shadow at the inner edge
     */
    public ShadowPanel(Color bg, int cornerRadius, int shadowSize, float shadowAlpha) {
        super(new BorderLayout());
        this.cornerRadius = cornerRadius;
        this.shadowSize = shadowSize;
        this.shadowAlpha = shadowAlpha;
        setOpaque(false); // We paint our own background + shadow
        setBackground(bg);
        // Add room for the shadow around the content
        setBorder(BorderFactory.createEmptyBorder(shadowSize, shadowSize, shadowSize, shadowSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Paint soft shadow with a quick falloff (squared curve) so it remains subtle
        for (int i = shadowSize; i > 0; i--) {
            float ratio = (float) i / (float) shadowSize; // 1 -> 0
            float falloff = ratio * ratio;                // faster fade to keep it very light
            int alpha = Math.min(255, Math.max(0, (int) (shadowAlpha * falloff * 255)));
            g2.setColor(new Color(0, 0, 0, alpha));
            g2.fillRoundRect(
                shadowSize - i,
                shadowSize - i,
                w - (shadowSize - i) * 2,
                h - (shadowSize - i) * 2,
                cornerRadius + i,
                cornerRadius + i
            );
        }

        // Card surface (no stroke to avoid border line)
        g2.setColor(getBackground());
        g2.fillRoundRect(
            shadowSize,
            shadowSize,
            w - shadowSize * 2,
            h - shadowSize * 2,
            cornerRadius,
            cornerRadius
        );

        g2.dispose();
        super.paintComponent(g);
    }
}
