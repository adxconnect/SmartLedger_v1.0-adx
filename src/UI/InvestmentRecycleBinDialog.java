package src.UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import src.FinanceManager;

public class InvestmentRecycleBinDialog extends JDialog {

    private FinanceManager manager;
    private DefaultTableModel recycleBinModel;
    private JTable recycleBinTable;
    private FinanceManagerFullUI parentUI; // Reference to the main UI

    public InvestmentRecycleBinDialog(Frame owner, FinanceManager manager, FinanceManagerFullUI parentUI) {
        super(owner, "Investment Recycle Bin", true); // Modal
        this.manager = manager;
        this.parentUI = parentUI;

        setSize(700, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Table Setup ---
        String[] columns = {"ID", "Type", "Holder Name", "Description", "Deleted On"};
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
        restoreButton.addActionListener(e -> restoreSelectedInvestment());
        deletePermButton.addActionListener(e -> deletePermanentlySelectedInvestment());
        closeButton.addActionListener(e -> dispose());

        // --- Load Initial Data ---
        loadRecycledInvestments();
    }

    private void loadRecycledInvestments() {
        recycleBinModel.setRowCount(0); // Clear table
        try {
            // Use the UI-specific method from FinanceManager
            List<Map<String, Object>> recycledData = manager.getRecycledInvestmentsForUI();

            for (Map<String, Object> data : recycledData) {
                // Add row using data from the map
                recycleBinModel.addRow(new Object[]{
                    data.get("id"),
                    data.get("asset_type"),
                    data.get("holder_name"),
                    data.get("description"),
                    data.get("deleted_on_str")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading investment recycle bin: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restoreSelectedInvestment() {
        int selectedRow = recycleBinTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an investment to restore.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int investmentId = (int) recycleBinModel.getValueAt(selectedRow, 0); // Get ID from first column

        try {
            manager.restoreInvestment(investmentId);
            loadRecycledInvestments(); // Refresh this dialog's table
            parentUI.refreshAfterInvestmentRestore(); // Refresh the main UI list
            JOptionPane.showMessageDialog(this, "Investment restored successfully.", "Restored", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error restoring investment: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePermanentlySelectedInvestment() {
        int selectedRow = recycleBinTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an investment to delete permanently.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int investmentId = (int) recycleBinModel.getValueAt(selectedRow, 0);

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete this investment (ID: " + investmentId + ")?\nThis action CANNOT be undone.",
            "Confirm Permanent Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.permanentlyDeleteInvestment(investmentId);
                loadRecycledInvestments(); // Refresh this dialog's table
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error permanently deleting investment: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}