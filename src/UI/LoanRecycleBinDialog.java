package src.UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import src.FinanceManager;

public class LoanRecycleBinDialog extends JDialog {

    private FinanceManager manager;
    private DefaultTableModel recycleBinModel;
    private JTable recycleBinTable;
    private FinanceManagerFullUI parentUI; // Reference to refresh the main list

    public LoanRecycleBinDialog(Frame owner, FinanceManager manager, FinanceManagerFullUI parentUI) {
        super(owner, "Loan Recycle Bin", true); // Modal
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
        
        // Main panel with dark background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER, 2));

        // Header panel with green background
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(34, 139, 34)); // Green for Loans
        headerPanel.setPreferredSize(new Dimension(0, 56));
        headerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));

        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(new Color(34, 139, 34));
        
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(ModernIcons.create(ModernIcons.IconType.RECYCLE, Color.WHITE, 24));
        JLabel titleLabel = new JLabel("Loan Recycle Bin");
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
        String[] columns = {"Original ID", "Type", "Lender", "Principal", "Deleted On"};
        recycleBinModel = new DefaultTableModel(columns, 0);
        recycleBinTable = new JTable(recycleBinModel);
        
        // Style the table for dark mode
        recycleBinTable.setFont(ModernTheme.FONT_BODY);
        recycleBinTable.setRowHeight(32);
        recycleBinTable.setBackground(ModernTheme.BACKGROUND);
        recycleBinTable.setForeground(ModernTheme.TEXT_PRIMARY);
        recycleBinTable.setSelectionBackground(new Color(34, 139, 34, 40));
        recycleBinTable.setSelectionForeground(ModernTheme.TEXT_WHITE);
        recycleBinTable.setGridColor(ModernTheme.BORDER);
        recycleBinTable.getTableHeader().setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD));
        recycleBinTable.getTableHeader().setBackground(ModernTheme.SURFACE);
        recycleBinTable.getTableHeader().setForeground(ModernTheme.TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(recycleBinTable);
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
        restoreButton.addActionListener(e -> restoreSelectedLoan());
        deletePermButton.addActionListener(e -> deletePermanentlySelectedLoan());
        closeButton.addActionListener(e -> dispose());

        mainPanel.add(contentWrapper, BorderLayout.CENTER);
        mainWrapper.add(mainPanel, BorderLayout.CENTER);
        add(mainWrapper);

        // --- Load Initial Data ---
        loadRecycledLoans();
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

    /**
     * Fetches data from FinanceManager and populates the table.
     */
    private void loadRecycledLoans() {
        recycleBinModel.setRowCount(0); // Clear table
        try {
            // Use the UI-specific method from FinanceManager
            List<Map<String, Object>> recycledData = manager.getRecycledLoansForUI();

            for (Map<String, Object> data : recycledData) {
                // Add row using data from the map
                recycleBinModel.addRow(new Object[]{
                    data.get("id"),
                    data.get("loan_type"),
                    data.get("lender_name"),
                    data.get("principal_amount"),
                    data.get("deleted_on_str")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading loan recycle bin: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Restores the selected loan from the recycle bin.
     */
    private void restoreSelectedLoan() {
        int selectedRow = recycleBinTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a loan to restore.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int loanId = (int) recycleBinModel.getValueAt(selectedRow, 0); // Get ID from first column

        try {
            manager.restoreLoan(loanId);
            loadRecycledLoans(); // Refresh this dialog's table
            parentUI.refreshAfterLoanRestore(); // Refresh the main UI list
            JOptionPane.showMessageDialog(this, "Loan restored successfully.", "Restored", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error restoring loan: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Permanently deletes the selected loan from the recycle bin.
     */
    private void deletePermanentlySelectedLoan() {
        int selectedRow = recycleBinTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a loan to delete permanently.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int loanId = (int) recycleBinModel.getValueAt(selectedRow, 0);

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete this loan (ID: " + loanId + ")?\nThis action CANNOT be undone.",
            "Confirm Permanent Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.permanentlyDeleteLoan(loanId);
                loadRecycledLoans(); // Refresh this dialog's table
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error permanently deleting loan: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}