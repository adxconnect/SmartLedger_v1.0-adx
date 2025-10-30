package src.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Utility class for creating modern SVG-style icons for the Finance Manager app.
 * All icons are drawn programmatically for crisp rendering at any size.
 */
public class ModernIcons {
    
    /**
     * Creates an icon with specified color and size
     */
    public static Icon create(IconType type, Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.setColor(color);
                g2.translate(x, y);
                
                float strokeWidth = size / 12f;
                g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                drawIcon(g2, type, size);
                
                g2.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return size;
            }
            
            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }
    
    /**
     * Draws the specific icon based on type
     */
    private static void drawIcon(Graphics2D g2, IconType type, int size) {
        int pad = size / 6;
        
        switch (type) {
            case DASHBOARD:
                drawDashboard(g2, size, pad);
                break;
            case TRANSACTIONS:
                drawTransactions(g2, size, pad);
                break;
            case BANK:
                drawBank(g2, size, pad);
                break;
            case CREDIT_CARD:
                drawCreditCard(g2, size, pad);
                break;
            case INVESTMENT:
                drawInvestment(g2, size, pad);
                break;
            case LOAN:
                drawLoan(g2, size, pad);
                break;
            case TAX:
                drawTax(g2, size, pad);
                break;
            case DEPOSIT:
                drawDeposit(g2, size, pad);
                break;
            case SUMMARY:
                drawSummary(g2, size, pad);
                break;
            case ADD:
                drawAdd(g2, size, pad);
                break;
            case DELETE:
                drawDelete(g2, size, pad);
                break;
            case EDIT:
                drawEdit(g2, size, pad);
                break;
            case RECYCLE:
                drawRecycle(g2, size, pad);
                break;
            case EXPORT:
                drawExport(g2, size, pad);
                break;
            case SEARCH:
                drawSearch(g2, size, pad);
                break;
            case SETTINGS:
                drawSettings(g2, size, pad);
                break;
            case LOGOUT:
                drawLogout(g2, size, pad);
                break;
            case USER:
                drawUser(g2, size, pad);
                break;
            case MONEY:
                drawMoney(g2, size, pad);
                break;
        }
    }
    
    private static void drawDashboard(Graphics2D g2, int size, int pad) {
        // Grid of squares
        int gap = size / 16;
        int w = (size - pad * 2 - gap) / 2;
        g2.fillRoundRect(pad, pad, w, w, size / 8, size / 8);
        g2.fillRoundRect(pad + w + gap, pad, w, w, size / 8, size / 8);
        g2.fillRoundRect(pad, pad + w + gap, w, w, size / 8, size / 8);
        g2.fillRoundRect(pad + w + gap, pad + w + gap, w, w, size / 8, size / 8);
    }
    
    private static void drawTransactions(Graphics2D g2, int size, int pad) {
        // List with dollar signs
        int y = pad + size / 6;
        int lineHeight = size / 5;
        for (int i = 0; i < 3; i++) {
            g2.drawLine(pad, y, size - pad, y);
            g2.drawLine(pad, y, pad, y + lineHeight / 3);
            y += lineHeight;
        }
    }
    
    private static void drawBank(Graphics2D g2, int size, int pad) {
        // Bank building with columns
        int roofY = pad + size / 4;
        Path2D roof = new Path2D.Float();
        roof.moveTo(pad, roofY);
        roof.lineTo(size / 2, pad);
        roof.lineTo(size - pad, roofY);
        g2.draw(roof);
        
        int colWidth = size / 8;
        int colGap = size / 12;
        int startX = pad + colGap;
        for (int i = 0; i < 3; i++) {
            g2.drawLine(startX + i * (colWidth + colGap), roofY, startX + i * (colWidth + colGap), size - pad);
        }
        g2.drawLine(pad, size - pad, size - pad, size - pad);
    }
    
    private static void drawCreditCard(Graphics2D g2, int size, int pad) {
        // Credit card rectangle
        RoundRectangle2D card = new RoundRectangle2D.Float(pad, pad + size / 5, size - pad * 2, size - pad * 2 - size / 5, size / 8, size / 8);
        g2.draw(card);
        // Magnetic stripe
        g2.fillRect(pad, pad + size / 3, size - pad * 2, size / 8);
        // Chip
        g2.fillRoundRect(pad + size / 6, size / 2, size / 5, size / 6, size / 16, size / 16);
    }
    
    private static void drawInvestment(Graphics2D g2, int size, int pad) {
        // Upward trending chart
        Path2D chart = new Path2D.Float();
        chart.moveTo(pad, size - pad);
        chart.lineTo(pad + size / 4, size - pad - size / 4);
        chart.lineTo(pad + size / 2, size - pad - size / 6);
        chart.lineTo(pad + size * 3 / 4, size - pad - size / 2);
        chart.lineTo(size - pad, size - pad - size * 2 / 3);
        g2.draw(chart);
        
        // Arrow at end
        g2.drawLine(size - pad, size - pad - size * 2 / 3, size - pad - size / 8, size - pad - size / 2);
        g2.drawLine(size - pad, size - pad - size * 2 / 3, size - pad - size / 6, size - pad - size * 2 / 3);
    }
    
    private static void drawLoan(Graphics2D g2, int size, int pad) {
        // Dollar sign with circular arrows
        g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
        FontMetrics fm = g2.getFontMetrics();
        String dollar = "$";
        int textX = (size - fm.stringWidth(dollar)) / 2;
        int textY = (size + fm.getAscent()) / 2;
        g2.drawString(dollar, textX, textY);
        
        // Circular arrow
        Arc2D arc = new Arc2D.Float(pad, pad, size - pad * 2, size - pad * 2, 45, 270, Arc2D.OPEN);
        g2.draw(arc);
    }
    
    private static void drawTax(Graphics2D g2, int size, int pad) {
        // Document with percentage
        Path2D doc = new Path2D.Float();
        doc.moveTo(pad, pad);
        doc.lineTo(size - pad - size / 4, pad);
        doc.lineTo(size - pad, pad + size / 4);
        doc.lineTo(size - pad, size - pad);
        doc.lineTo(pad, size - pad);
        doc.closePath();
        g2.draw(doc);
        
        // Percentage symbol
        g2.setFont(new Font("Segoe UI", Font.BOLD, size / 3));
        FontMetrics fm = g2.getFontMetrics();
        String pct = "%";
        int textX = (size - fm.stringWidth(pct)) / 2;
        int textY = (size + fm.getAscent()) / 2;
        g2.drawString(pct, textX, textY);
    }
    
    private static void drawDeposit(Graphics2D g2, int size, int pad) {
        // Piggy bank or safe
        g2.drawRoundRect(pad, pad + size / 4, size - pad * 2, size / 2, size / 8, size / 8);
        g2.fillRect(size / 2 - size / 16, pad + size / 4 - size / 8, size / 8, size / 8);
        g2.drawOval(size / 2 - size / 8, size / 2, size / 4, size / 4);
    }
    
    private static void drawSummary(Graphics2D g2, int size, int pad) {
        // Document with lines
        g2.drawRoundRect(pad, pad, size - pad * 2, size - pad * 2, size / 8, size / 8);
        int lineY = pad + size / 4;
        for (int i = 0; i < 3; i++) {
            g2.drawLine(pad + size / 6, lineY, size - pad - size / 6, lineY);
            lineY += size / 6;
        }
    }
    
    private static void drawAdd(Graphics2D g2, int size, int pad) {
        // Plus sign
        int center = size / 2;
        int length = size - pad * 2;
        g2.drawLine(center, pad, center, pad + length);
        g2.drawLine(pad, center, pad + length, center);
    }
    
    private static void drawDelete(Graphics2D g2, int size, int pad) {
        // Trash can
        g2.drawLine(pad, pad + size / 5, size - pad, pad + size / 5);
        Path2D can = new Path2D.Float();
        can.moveTo(pad + size / 6, pad + size / 5);
        can.lineTo(pad + size / 8, size - pad);
        can.lineTo(size - pad - size / 8, size - pad);
        can.lineTo(size - pad - size / 6, pad + size / 5);
        g2.draw(can);
        g2.drawLine(pad + size / 3, pad, size - pad - size / 3, pad);
    }
    
    private static void drawEdit(Graphics2D g2, int size, int pad) {
        // Pencil
        Path2D pencil = new Path2D.Float();
        pencil.moveTo(size - pad, pad);
        pencil.lineTo(pad, size - pad);
        pencil.lineTo(pad + size / 6, size - pad);
        pencil.lineTo(size - pad, pad + size / 6);
        pencil.closePath();
        g2.draw(pencil);
        g2.drawLine(pad + size / 8, size - pad - size / 8, size - pad - size / 8, pad + size / 8);
    }
    
    private static void drawRecycle(Graphics2D g2, int size, int pad) {
        // Recycle symbol (curved arrows)
        Arc2D arc1 = new Arc2D.Float(pad, pad, size / 2, size / 2, 0, 180, Arc2D.OPEN);
        Arc2D arc2 = new Arc2D.Float(size / 2, pad, size / 2, size / 2, 180, 180, Arc2D.OPEN);
        Arc2D arc3 = new Arc2D.Float(pad + size / 4, size / 2, size / 2, size / 2, 270, 180, Arc2D.OPEN);
        g2.draw(arc1);
        g2.draw(arc2);
        g2.draw(arc3);
    }
    
    private static void drawExport(Graphics2D g2, int size, int pad) {
        // Arrow pointing out of box
        g2.drawRoundRect(pad, size / 2, size - pad * 2, size / 2 - pad, size / 12, size / 12);
        // Arrow
        g2.drawLine(size / 2, size / 2, size / 2, pad);
        g2.drawLine(size / 2, pad, size / 2 - size / 6, pad + size / 6);
        g2.drawLine(size / 2, pad, size / 2 + size / 6, pad + size / 6);
    }
    
    private static void drawSearch(Graphics2D g2, int size, int pad) {
        // Magnifying glass
        int circleSize = size / 2;
        g2.drawOval(pad, pad, circleSize, circleSize);
        g2.drawLine(pad + circleSize - size / 8, pad + circleSize - size / 8, size - pad, size - pad);
    }
    
    private static void drawSettings(Graphics2D g2, int size, int pad) {
        // Gear
        int center = size / 2;
        int outerRadius = size / 2 - pad;
        int innerRadius = size / 4;
        int teeth = 8;
        
        for (int i = 0; i < teeth; i++) {
            double angle = Math.PI * 2 * i / teeth;
            int x1 = center + (int) (innerRadius * Math.cos(angle));
            int y1 = center + (int) (innerRadius * Math.sin(angle));
            int x2 = center + (int) (outerRadius * Math.cos(angle));
            int y2 = center + (int) (outerRadius * Math.sin(angle));
            g2.drawLine(x1, y1, x2, y2);
        }
        g2.drawOval(center - innerRadius / 2, center - innerRadius / 2, innerRadius, innerRadius);
    }
    
    private static void drawLogout(Graphics2D g2, int size, int pad) {
        // Door with arrow
        g2.drawRoundRect(pad, pad, size / 2, size - pad * 2, size / 12, size / 12);
        // Arrow
        g2.drawLine(size / 2, size / 2, size - pad, size / 2);
        g2.drawLine(size - pad, size / 2, size - pad - size / 6, size / 2 - size / 8);
        g2.drawLine(size - pad, size / 2, size - pad - size / 6, size / 2 + size / 8);
    }
    
    private static void drawUser(Graphics2D g2, int size, int pad) {
        // User silhouette
        int headSize = size / 3;
        g2.fillOval(size / 2 - headSize / 2, pad, headSize, headSize);
        // Body
        Arc2D body = new Arc2D.Float(pad, pad + headSize, size - pad * 2, size - pad - headSize, 0, 180, Arc2D.OPEN);
        g2.draw(body);
    }
    
    private static void drawMoney(Graphics2D g2, int size, int pad) {
        // Stack of coins
        for (int i = 0; i < 3; i++) {
            int y = size - pad - size / 6 - i * size / 8;
            g2.drawOval(pad + size / 6, y, size - pad * 2 - size / 3, size / 4);
        }
    }
    
    /**
     * Icon types available
     */
    public enum IconType {
        DASHBOARD, TRANSACTIONS, BANK, CREDIT_CARD, INVESTMENT, LOAN,
        TAX, DEPOSIT, SUMMARY, ADD, DELETE, EDIT, RECYCLE, EXPORT,
        SEARCH, SETTINGS, LOGOUT, USER, MONEY
    }
}
