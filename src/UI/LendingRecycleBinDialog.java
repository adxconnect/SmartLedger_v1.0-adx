package src.UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import src.FinanceManager;

public class LendingRecycleBinDialog extends JDialog {

    private FinanceManager manager;
    private DefaultTableModel recycleBinModel;
    private JTable recycleBinTable;
    private FinanceManagerFullUI parentUI; // Reference to refresh the main list

    public LendingRecycleBinDialog(Frame owner, FinanceManager manager, FinanceManagerFullUI parentUI) {
        super(owner, "Lending Recycle Bin", true); // Modal
        this.manager = manager;
        this.parentUI = parentUI;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        initComponents();

        pack();
        if (getWidth() < 800) {
            setSize(800, Math.max(getHeight(), 500));
        }
        setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
        setLocationRelativeTo(owner);
    }
    
    private void initComponents() {
        // Main wrapper with rounded border
        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBackground(new Color(0, 0, 0, 0));
        
        // Main panel with white background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER, 2));

        // Header panel with green background
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(34, 139, 34)); // Green for Lending
        headerPanel.setPreferredSize(new Dimension(0, 56));
        headerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));

        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(new Color(34, 139, 34));
        
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(ModernIcons.create(ModernIcons.IconType.RECYCLE, Color.WHITE, 24));
        JLabel titleLabel = new JLabel("Lending Recycle Bin");
        titleLabel.setFont(ModernTheme.FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);
        
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(createHeaderCloseButton(), BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content wrapper
        JPanel contentWrapper = new JPanel(new BorderLayout(10, 10));
        contentWrapper.setBackground(Color.WHITE);
        contentWrapper.setBorder(new EmptyBorder(16, 16, 18, 16));

        // --- Table Setup ---
        String[] columns = {"Original ID", "Type", "Borrower", "Principal", "Deleted On"};
        recycleBinModel = new DefaultTableModel(columns, 0);
        recycleBinTable = new JTable(recycleBinModel);
        
        // Style the table
        recycleBinTable.setFont(ModernTheme.FONT_BODY);
        recycleBinTable.setRowHeight(32);
        recycleBinTable.setSelectionBackground(new Color(34, 139, 34, 40));
        recycleBinTable.setSelectionForeground(ModernTheme.TEXT_PRIMARY);
        recycleBinTable.getTableHeader().setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD));
        recycleBinTable.getTableHeader().setBackground(ModernTheme.SURFACE);
        recycleBinTable.getTableHeader().setForeground(ModernTheme.TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(recycleBinTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER, 1));
        contentWrapper.add(scrollPane, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton restoreButton = ModernTheme.createSuccessButton("Restore Selected");
        JButton deletePermButton = ModernTheme.createDangerButton("Delete Permanently");
        JButton closeButton = ModernTheme.createSecondaryButton("Close");

        buttonPanel.add(restoreButton);
        buttonPanel.add(deletePermButton);
        buttonPanel.add(closeButton);
        
        contentWrapper.add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        restoreButton.addActionListener(e -> restoreSelectedLending());
        deletePermButton.addActionListener(e -> deletePermanentlySelectedLending());
        closeButton.addActionListener(e -> dispose());

        mainPanel.add(contentWrapper, BorderLayout.CENTER);
        mainWrapper.add(mainPanel, BorderLayout.CENTER);
        add(mainWrapper);

        // --- Load Initial Data ---
        loadRecycledLendings();
    }
    
    /**
     * Creates the close button for the header (× symbol).
     */
    private JButton createHeaderCloseButton() {
        JButton closeBtn = new JButton("×");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 20));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(new Color(34, 139, 34));
        closeBtn.setBorder(new EmptyBorder(0, 10, 0, 10));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeBtn.setBackground(new Color(24, 119, 24));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeBtn.setBackground(new Color(34, 139, 34));
            }
        });
        return closeBtn;
    }

    /**
     * Fetches data from FinanceManager and populates the table.
     */
    private void loadRecycledLendings() {
        recycleBinModel.setRowCount(0); // Clear table
        try {
            // Use the UI-specific method from FinanceManager
            List<Map<String, Object>> recycledData = manager.getRecycledLendingsForUI();

            for (Map<String, Object> data : recycledData) {
                // Add row using data from the map
                recycleBinModel.addRow(new Object[]{
                    data.get("id"),
                    data.get("loan_type"),
                    data.get("borrower_name"),
                    data.get("principal_amount"),
                    data.get("deleted_on_str")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading lending recycle bin: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Restores the selected lending record from the recycle bin.
     */
    private void restoreSelectedLending() {
        int selectedRow = recycleBinTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to restore.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int lendingId = (int) recycleBinModel.getValueAt(selectedRow, 0); // Get ID from first column

        try {
            manager.restoreLending(lendingId);
            loadRecycledLendings(); // Refresh this dialog's table
            parentUI.refreshAfterLendingRestore(); // Refresh the main UI list
            JOptionPane.showMessageDialog(this, "Lending record restored successfully.", "Restored", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error restoring lending record: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Permanently deletes the selected lending record from the recycle bin.
     */
    private void deletePermanentlySelectedLending() {
        int selectedRow = recycleBinTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete permanently.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int lendingId = (int) recycleBinModel.getValueAt(selectedRow, 0);

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete this record (ID: " + lendingId + ")?\nThis action CANNOT be undone.",
            "Confirm Permanent Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.permanentlyDeleteLending(lendingId);
                loadRecycledLendings(); // Refresh this dialog's table
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error permanently deleting lending record: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}