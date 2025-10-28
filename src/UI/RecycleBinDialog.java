package src.UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import src.FinanceManager;
import src.Transaction; // Use the Transaction class

public class RecycleBinDialog extends JDialog { // Make sure class name matches file name

    private FinanceManager manager;
    private DefaultTableModel recycleBinModel;
    private JTable recycleBinTable;
    private FinanceManagerFullUI parentUI; // Reference to the main UI to refresh it

    public RecycleBinDialog(Frame owner, FinanceManager manager, FinanceManagerFullUI parentUI) {
        super(owner, "Recycle Bin - Deleted Transactions", true); // Modal dialog
        this.manager = manager;
        this.parentUI = parentUI;

        setSize(800, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Table Setup ---
        // Columns relevant for deleted transactions
        String[] columns = {"Original ID", "Date", "Timestamp", "Category", "Type", "Amount", "Description"};
        recycleBinModel = new DefaultTableModel(columns, 0);
        recycleBinTable = new JTable(recycleBinModel);

        JScrollPane scrollPane = new JScrollPane(recycleBinTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton restoreButton = new JButton("Restore Selected");
        JButton deletePermButton = new JButton("Permanently Delete Selected");
        JButton closeButton = new JButton("Close");

        buttonPanel.add(restoreButton);
        buttonPanel.add(deletePermButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        restoreButton.addActionListener(e -> restoreSelected());
        deletePermButton.addActionListener(e -> deletePermanentlySelected());
        closeButton.addActionListener(e -> dispose()); // Just close the dialog

        // --- Load Initial Data ---
        loadRecycledTransactions();
    }

    /**
     * Fetches data from FinanceManager and populates the table.
     */
    private void loadRecycledTransactions() {
        recycleBinModel.setRowCount(0); // Clear existing rows
        try {
            // Assuming getRecycledTransactions returns List<Transaction>
            List<Transaction> recycled = manager.getRecycledTransactions();
            for (Transaction t : recycled) {
                recycleBinModel.addRow(new Object[]{
                    t.getId(),         // Original ID
                    t.getDate(),
                    t.getTimestamp(),
                    t.getCategory(),
                    t.getType(),
                    t.getAmount(),
                    t.getDescription()
                    // Add deleted_on if you modify getRecycledTransactions to return it
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transaction recycle bin: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
             // Catch potential ClassCastException or other issues if getRecycledTransactions changes
             JOptionPane.showMessageDialog(this, "An unexpected error occurred loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             e.printStackTrace(); // Log the full error for debugging
        }
    }

    /**
     * Handles restoring the selected transaction.
     */
    private void restoreSelected() {
        int selectedRow = recycleBinTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to restore.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int transactionId = (int) recycleBinModel.getValueAt(selectedRow, 0); // Get ID from first column

        try {
            manager.restoreTransaction(transactionId);
            loadRecycledTransactions(); // Refresh this dialog's table
            parentUI.refreshAfterRestore(); // Tell the main UI to refresh its transactions
            JOptionPane.showMessageDialog(this, "Transaction restored successfully.", "Restored", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error restoring transaction: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles permanently deleting the selected transaction.
     */
    private void deletePermanentlySelected() {
        int selectedRow = recycleBinTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete permanently.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int transactionId = (int) recycleBinModel.getValueAt(selectedRow, 0);

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete transaction ID: " + transactionId + "?\nThis action cannot be undone.",
            "Confirm Permanent Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.permanentlyDeleteTransaction(transactionId);
                loadRecycledTransactions(); // Refresh this dialog's table
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error permanently deleting transaction: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}