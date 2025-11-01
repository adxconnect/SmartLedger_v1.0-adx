package src.UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import src.UI.FinanceManagerFullUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Map; // Import Map

import src.Card; // Still useful for reference, though we use Map
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

        setSize(700, 450); // Adjusted size
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Table Setup ---
        // Define columns relevant for identifying deleted cards
        String[] columns = {"Original ID", "Type", "Name", "Masked Number", "Valid Thru", "Deleted On"};
        cardRecycleBinModel = new DefaultTableModel(columns, 0);
        cardRecycleBinTable = new JTable(cardRecycleBinModel);

        JScrollPane scrollPane = new JScrollPane(cardRecycleBinTable);
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
        restoreButton.addActionListener(e -> restoreSelectedCard());
        deletePermButton.addActionListener(e -> deletePermanentlySelectedCard());
        closeButton.addActionListener(e -> dispose());

        // --- Load Initial Data ---
        loadRecycledCards();
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