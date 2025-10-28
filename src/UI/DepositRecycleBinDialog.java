package src.UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import src.Deposit; // Use the Deposit class
import src.FinanceManager;

public class DepositRecycleBinDialog extends JDialog {

    private FinanceManager manager;
    private DefaultTableModel depositRecycleBinModel;
    private JTable depositRecycleBinTable;
    private FinanceManagerFullUI parentUI; // Reference to refresh the main list

    public DepositRecycleBinDialog(Frame owner, FinanceManager manager, FinanceManagerFullUI parentUI) {
        super(owner, "Deposit Recycle Bin", true); // Modal
        this.manager = manager;
        this.parentUI = parentUI;

        setSize(800, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Table Setup ---
        // Define columns relevant for identifying deleted deposits
        String[] columns = {"Original ID", "Type", "Holder Name", "Description", "Amount", "Deleted On"}; // Example columns
        depositRecycleBinModel = new DefaultTableModel(columns, 0);
        depositRecycleBinTable = new JTable(depositRecycleBinModel);

        JScrollPane scrollPane = new JScrollPane(depositRecycleBinTable);
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
        restoreButton.addActionListener(e -> restoreSelectedDeposit());
        deletePermButton.addActionListener(e -> deletePermanentlySelectedDeposit());
        closeButton.addActionListener(e -> dispose());

        // --- Load Initial Data ---
        loadRecycledDeposits();
    }

    // Replace this method in src/UI/DepositRecycleBinDialog.java
    private void loadRecycledDeposits() {
        depositRecycleBinModel.setRowCount(0); // Clear table
        try {
            // Use the new method returning a List of Maps
            List<Map<String, Object>> recycledData = manager.getRecycledDepositsForUI();

            for (Map<String, Object> data : recycledData) {
                // Determine the relevant "Amount" to display based on type
                double amount = 0;
                String type = (String) data.get("deposit_type");
                if ("FD".equals(type)) {
                    amount = (Double) data.getOrDefault("principal_amount", 0.0);
                } else if ("RD".equals(type)) {
                    amount = (Double) data.getOrDefault("monthly_amount", 0.0);
                } else if ("Gullak".equals(type)) {
                    amount = (Double) data.getOrDefault("current_total", 0.0);
                }

                // Add row using data from the map
                depositRecycleBinModel.addRow(new Object[]{
                    data.get("id"),
                    type,
                    data.get("holder_name"),
                    data.get("description"),
                    amount,
                    data.get("deleted_on_str") // Display the deletion timestamp
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading deposit recycle bin: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restoreSelectedDeposit() {
        int selectedRow = depositRecycleBinTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a deposit to restore.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int depositId = (int) depositRecycleBinModel.getValueAt(selectedRow, 0); // Get ID

        try {
            manager.restoreDeposit(depositId);
            loadRecycledDeposits(); // Refresh this dialog's table
            parentUI.refreshAfterDepositRestore(); // Refresh the main deposits list
            JOptionPane.showMessageDialog(this, "Deposit restored successfully.", "Restored", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error restoring deposit: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePermanentlySelectedDeposit() {
        int selectedRow = depositRecycleBinTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a deposit to delete permanently.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int depositId = (int) depositRecycleBinModel.getValueAt(selectedRow, 0);

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete deposit ID: " + depositId + "?\nThis action CANNOT be undone.",
            "Confirm Permanent Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.permanentlyDeleteDeposit(depositId);
                loadRecycledDeposits(); // Refresh this dialog's table
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error permanently deleting deposit: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}