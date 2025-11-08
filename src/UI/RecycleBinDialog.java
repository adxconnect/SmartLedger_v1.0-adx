package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import java.awt.*;
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
        super(owner, "", true);
        this.manager = manager;
        this.parentUI = parentUI;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.SURFACE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(20, ModernTheme.BORDER),
            new EmptyBorder(0, 0, 0, 0)
        ));

        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(ModernTheme.PRIMARY_DARK);
        headerPanel.setBorder(new EmptyBorder(14, 18, 14, 12));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel iconLabel = new JLabel(ModernIcons.create(ModernIcons.IconType.RECYCLE, ModernTheme.TEXT_WHITE, 20));
        JLabel titleLabel = new JLabel("Transaction Recycle Bin");
        titleLabel.setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(ModernTheme.TEXT_WHITE);
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(createCloseButton(), BorderLayout.EAST);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setBackground(ModernTheme.SURFACE);
        contentPanel.setBorder(new EmptyBorder(20, 22, 22, 22));

        JLabel helperLabel = new JLabel("Restore a transaction or permanently remove it.");
        helperLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(11.5f));
        helperLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        helperLabel.setBorder(new EmptyBorder(0, 2, 0, 0));
        contentPanel.add(helperLabel, BorderLayout.NORTH);

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
        ModernTheme.styleScrollPane(scrollPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton restoreButton = ModernTheme.createSuccessButton("Restore Selected");
        restoreButton.setIcon(ModernIcons.create(ModernIcons.IconType.ADD, ModernTheme.TEXT_WHITE, 16));

        JButton deletePermButton = ModernTheme.createDangerButton("Permanently Delete");
        deletePermButton.setIcon(ModernIcons.create(ModernIcons.IconType.DELETE, ModernTheme.TEXT_WHITE, 16));

        JButton selectAllButton = ModernTheme.createSecondaryButton("Select All");

        JButton closeButton = ModernTheme.createSecondaryButton("Close");
        buttonPanel.add(closeButton);
        buttonPanel.add(selectAllButton);
        buttonPanel.add(restoreButton);
        buttonPanel.add(deletePermButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

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

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
        pack();
        if (getWidth() < 760) {
            setSize(760, Math.max(getHeight(), 480));
        }
        setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
        setLocationRelativeTo(owner);
        loadRecycledTransactions();
    }

    private JButton createCloseButton() {
        JButton closeBtn = new JButton("×");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        closeBtn.setForeground(ModernTheme.TEXT_WHITE);
        closeBtn.setOpaque(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(32, 32));
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeBtn.setForeground(new Color(255, 255, 255, 200));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeBtn.setForeground(ModernTheme.TEXT_WHITE);
            }
        });
        closeBtn.addActionListener(e -> dispose());
        return closeBtn;
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