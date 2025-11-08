package src.UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map; // Import Map

import src.FinanceManager;

public class CardRecycleBinDialog extends JDialog {

    private FinanceManager manager;
    private DefaultTableModel cardRecycleBinModel;
    private JTable cardRecycleBinTable;
    private FinanceManagerFullUI parentUI; // Reference to refresh the main list

    public CardRecycleBinDialog(Frame owner, FinanceManager manager, FinanceManagerFullUI parentUI) {
        super(owner, "Card Recycle Bin", true); // Modal
        this.manager = manager;
        this.parentUI = parentUI;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        initComponents();

        pack();
        if (getWidth() < 850) {
            setSize(850, Math.max(getHeight(), 500));
        }
        setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
        setLocationRelativeTo(owner);
    }
    
    private void initComponents() {
        // Main wrapper with rounded border
        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBackground(new Color(0, 0, 0, 0));
        
        // Main panel with dark background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER, 2));

        // Header panel with green background
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(34, 139, 34)); // Green for Cards
        headerPanel.setPreferredSize(new Dimension(0, 56));
        headerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));

        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(new Color(34, 139, 34));
        
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(ModernIcons.create(ModernIcons.IconType.RECYCLE, Color.WHITE, 24));
        JLabel titleLabel = new JLabel("Card Recycle Bin");
        titleLabel.setFont(ModernTheme.FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);
        
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(createHeaderCloseButton(), BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content wrapper with dark background
        JPanel contentWrapper = new JPanel(new BorderLayout(10, 10));
        contentWrapper.setBackground(ModernTheme.BACKGROUND);
        contentWrapper.setBorder(new EmptyBorder(16, 16, 18, 16));

        // --- Table Setup ---
        // Define columns relevant for identifying deleted cards
        String[] columns = {"Original ID", "Type", "Name", "Masked Number", "Valid Thru", "Deleted On"};
        cardRecycleBinModel = new DefaultTableModel(columns, 0);
        cardRecycleBinTable = new JTable(cardRecycleBinModel);
        
        // Style the table for dark mode
        cardRecycleBinTable.setFont(ModernTheme.FONT_BODY);
        cardRecycleBinTable.setRowHeight(32);
        cardRecycleBinTable.setBackground(ModernTheme.BACKGROUND);
        cardRecycleBinTable.setForeground(ModernTheme.TEXT_PRIMARY);
        cardRecycleBinTable.setSelectionBackground(new Color(34, 139, 34, 40));
        cardRecycleBinTable.setSelectionForeground(ModernTheme.TEXT_WHITE);
        cardRecycleBinTable.setGridColor(ModernTheme.BORDER);
        cardRecycleBinTable.getTableHeader().setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD));
        cardRecycleBinTable.getTableHeader().setBackground(ModernTheme.SURFACE);
        cardRecycleBinTable.getTableHeader().setForeground(ModernTheme.TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(cardRecycleBinTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER, 1));
        scrollPane.setBackground(ModernTheme.BACKGROUND);
        scrollPane.getViewport().setBackground(ModernTheme.BACKGROUND);
        contentWrapper.add(scrollPane, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(ModernTheme.BACKGROUND);
        
        JButton restoreButton = ModernTheme.createSuccessButton("Restore Selected");
        JButton deletePermButton = ModernTheme.createDangerButton("Delete Permanently");
        JButton closeButton = ModernTheme.createSecondaryButton("Close");

        buttonPanel.add(restoreButton);
        buttonPanel.add(deletePermButton);
        buttonPanel.add(closeButton);
        
        contentWrapper.add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        restoreButton.addActionListener(e -> restoreSelectedCard());
        deletePermButton.addActionListener(e -> deletePermanentlySelectedCard());
        closeButton.addActionListener(e -> dispose());

        mainPanel.add(contentWrapper, BorderLayout.CENTER);
        mainWrapper.add(mainPanel, BorderLayout.CENTER);
        add(mainWrapper);

        // --- Load Initial Data ---
        loadRecycledCards();
    }
    
    /**
     * Creates the close button for the header (× symbol).
     */
    private JButton createHeaderCloseButton() {
        JButton closeBtn = new JButton("×");
        closeBtn.setFont(new Font("Arial", Font.PLAIN, 22));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(new Color(0, 0, 0, 0));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeBtn.setBackground(new Color(255, 255, 255, 30));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeBtn.setBackground(new Color(0, 0, 0, 0));
            }
        });
        return closeBtn;
    }

    private void loadRecycledCards() {
        cardRecycleBinModel.setRowCount(0); // Clear table
        try {
            // Use the method that returns Map for UI display
            List<Map<String, Object>> recycledData = manager.getRecycledCardsForUI();

            for (Map<String, Object> data : recycledData) {
                // Add row using data from the map
                cardRecycleBinModel.addRow(new Object[]{
                    data.get("original_id"),
                    data.get("card_type"),
                    data.get("card_name"),
                    data.get("masked_card_number"), // Show masked number
                    data.get("valid_through"),
                    data.get("deleted_on_str") // Display deletion timestamp
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading card recycle bin: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void restoreSelectedCard() {
        int selectedRow = cardRecycleBinTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a card to restore.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int originalCardId = (int) cardRecycleBinModel.getValueAt(selectedRow, 0); // Get original ID

        try {
            manager.restoreCard(originalCardId);
            loadRecycledCards(); // Refresh this dialog's table
            parentUI.refreshAfterCardRestore(); // Refresh the main cards list
            JOptionPane.showMessageDialog(this, "Card restored successfully.", "Restored", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error restoring card: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePermanentlySelectedCard() {
        int selectedRow = cardRecycleBinTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a card to delete permanently.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int originalCardId = (int) cardRecycleBinModel.getValueAt(selectedRow, 0);

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete card (Original ID: " + originalCardId + ")?\nThis action CANNOT be undone.",
            "Confirm Permanent Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.permanentlyDeleteCard(originalCardId);
                loadRecycledCards(); // Refresh this dialog's table
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error permanently deleting card: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}