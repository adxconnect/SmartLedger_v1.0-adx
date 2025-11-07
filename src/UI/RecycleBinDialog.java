package src.UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import src.UI.FinanceManagerFullUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import src.FinanceManager;
import src.Transaction;

public class RecycleBinDialog extends JDialog {

    private FinanceManager manager;
    private DefaultTableModel recycleBinModel;
    private JTable recycleBinTable;
    private FinanceManagerFullUI parentUI;

    public RecycleBinDialog(Frame owner, FinanceManager manager, FinanceManagerFullUI parentUI) {
        super(owner, "Recycle Bin - Deleted Transactions", true);
        this.manager = manager;
        this.parentUI = parentUI;

        setSize(900, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(16, 16));
        setBackground(ModernTheme.BACKGROUND);

        // Main panel with modern styling
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(ModernTheme.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(ModernTheme.BACKGROUND);
        JLabel titleLabel = new JLabel("Recycle Bin - Deleted Transactions");
        titleLabel.setFont(ModernTheme.FONT_HEADER);
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Table Setup with modern styling
        String[] columns = {"ID", "Date", "Timestamp", "Category", "Type", "Amount", "Description"};
        recycleBinModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recycleBinTable = new JTable(recycleBinModel);
        
        // Modern table styling
        recycleBinTable.setFont(ModernTheme.FONT_BODY);
        recycleBinTable.setBackground(ModernTheme.SURFACE);
        recycleBinTable.setForeground(ModernTheme.TEXT_PRIMARY);
        recycleBinTable.setSelectionBackground(new Color(67, 97, 238, 40));
        recycleBinTable.setSelectionForeground(ModernTheme.TEXT_PRIMARY);
        recycleBinTable.setRowHeight(40);
        recycleBinTable.setShowVerticalLines(false);
        recycleBinTable.setShowHorizontalLines(true);
        recycleBinTable.setGridColor(new Color(200, 200, 200, 50));
        recycleBinTable.setIntercellSpacing(new Dimension(0, 1));
        recycleBinTable.setBorder(null);

        // Modern header styling
        JTableHeader header = recycleBinTable.getTableHeader();
        header.setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD));
        header.setBackground(ModernTheme.SURFACE);
        header.setForeground(ModernTheme.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ModernTheme.PRIMARY));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));

        // Cell renderer for better text alignment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < recycleBinTable.getColumnCount(); i++) {
            recycleBinTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(recycleBinTable);
        scrollPane.setBackground(ModernTheme.SURFACE);
        scrollPane.getViewport().setBackground(ModernTheme.SURFACE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200, 100), 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Modern Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setBackground(ModernTheme.BACKGROUND);

        JButton restoreButton = ModernTheme.createSuccessButton("Restore Selected");
        restoreButton.setIcon(ModernIcons.create(ModernIcons.IconType.ADD, ModernTheme.TEXT_WHITE, 16));
        restoreButton.setPreferredSize(new Dimension(180, 38));

        JButton deletePermButton = ModernTheme.createDangerButton("Permanently Delete");
        deletePermButton.setIcon(ModernIcons.create(ModernIcons.IconType.DELETE, ModernTheme.TEXT_WHITE, 16));
        deletePermButton.setPreferredSize(new Dimension(180, 38));

        JButton selectAllButton = ModernTheme.createSecondaryButton("Select All");
        selectAllButton.setPreferredSize(new Dimension(120, 38));

        JButton closeButton = ModernTheme.createSecondaryButton("Close");
        closeButton.setPreferredSize(new Dimension(100, 38));

        buttonPanel.add(restoreButton);
        buttonPanel.add(deletePermButton);
        buttonPanel.add(selectAllButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        restoreButton.addActionListener(e -> restoreSelected());
        deletePermButton.addActionListener(e -> deletePermanentlySelected());
        selectAllButton.addActionListener(e -> {
            int rowCount = recycleBinTable.getRowCount();
            if (rowCount > 0) {
                recycleBinTable.setRowSelectionInterval(0, rowCount - 1);
            }
        });
        closeButton.addActionListener(e -> dispose());

        add(mainPanel);
        loadRecycledTransactions();
    }

    private void loadRecycledTransactions() {
        recycleBinModel.setRowCount(0);
        try {
            List<Transaction> recycled = manager.getRecycledTransactions();
            for (Transaction t : recycled) {
                recycleBinModel.addRow(new Object[]{
                    t.getId(),
                    t.getDate(),
                    t.getTimestamp(),
                    t.getCategory(),
                    t.getType(),
                    String.format("₹%.2f", t.getAmount()),
                    t.getDescription()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading recycle bin: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void restoreSelected() {
        int[] selectedRows = recycleBinTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one transaction to restore.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int restored = 0;
            for (int viewRow : selectedRows) {
                int modelRow = recycleBinTable.convertRowIndexToModel(viewRow);
                int transactionId = (int) recycleBinModel.getValueAt(modelRow, 0);
                manager.restoreTransaction(transactionId);
                restored++;
            }
            loadRecycledTransactions();
            parentUI.refreshAfterRestore();
            
            // Modern success dialog
            parentUI.showModernSuccessDialog("Restored", "Restored " + restored + " transaction(s).");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error restoring transaction(s): " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePermanentlySelected() {
        int[] selectedRows = recycleBinTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one transaction to delete.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Modern confirmation using the parent UI's modern dialog
        int choice = parentUI.showModernConfirmDialog(
            "Confirm Permanent Delete",
            "Are you sure you want to permanently delete " + selectedRows.length + " transaction(s)?",
            "This action cannot be undone.",
            true
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                int deleted = 0;
                for (int viewRow : selectedRows) {
                    int modelRow = recycleBinTable.convertRowIndexToModel(viewRow);
                    int transactionId = (int) recycleBinModel.getValueAt(modelRow, 0);
                    manager.permanentlyDeleteTransaction(transactionId);
                    deleted++;
                }
                loadRecycledTransactions();
                parentUI.showModernSuccessDialog("Deleted", "Permanently deleted " + deleted + " transaction(s).");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting transaction(s): " + e.getMessage(), 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}