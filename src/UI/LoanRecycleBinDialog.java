package src.UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

        setSize(700, 450); // Set a good default size
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Table Setup ---
        String[] columns = {"Original ID", "Type", "Lender", "Principal", "Deleted On"};
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
        restoreButton.addActionListener(e -> restoreSelectedLoan());
        deletePermButton.addActionListener(e -> deletePermanentlySelectedLoan());
        closeButton.addActionListener(e -> dispose());

        // --- Load Initial Data ---
        loadRecycledLoans();
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