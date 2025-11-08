package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import src.FinanceManager;

public class InvestmentRecycleBinDialog extends JDialog {

    private FinanceManager manager;
    private DefaultTableModel recycleBinModel;
    private JTable recycleBinTable;
    private FinanceManagerFullUI parentUI;

    public InvestmentRecycleBinDialog(Frame owner, FinanceManager manager, FinanceManagerFullUI parentUI) {
        super(owner, "Investment Recycle Bin", true);
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

        mainPanel.add(createHeader(), BorderLayout.NORTH);

        mainPanel.add(createHeader(), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 14));
        contentPanel.setBackground(ModernTheme.SURFACE);
        contentPanel.setBorder(new EmptyBorder(18, 20, 20, 20));

        JLabel helperLabel = new JLabel("Restore investments you might need again or delete them forever.");
        helperLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(11.5f));
        helperLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        helperLabel.setBorder(new EmptyBorder(0, 2, 0, 0));

        recycleBinModel = new DefaultTableModel(new Object[]{
            "ID", "Type", "Holder Name", "Description", "Deleted On"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recycleBinTable = new JTable(recycleBinModel);
        styleTable(recycleBinTable);
        JScrollPane scrollPane = new JScrollPane(recycleBinTable);
        ModernTheme.styleScrollPane(scrollPane);
        scrollPane.setPreferredSize(new Dimension(700, 320));

        contentPanel.add(helperLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buildButtonPanel(), BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
        pack();
        if (getWidth() < 740) {
            setSize(740, Math.max(getHeight(), 440));
        }
        if (getHeight() > 600) {
            setSize(getWidth(), 600);
        }
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
        setLocationRelativeTo(owner);

        loadRecycledInvestments();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(new Color(34, 139, 34)); // Green theme for investments
        header.setBorder(new EmptyBorder(14, 18, 14, 12));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel(ModernIcons.create(ModernIcons.IconType.RECYCLE, ModernTheme.TEXT_WHITE, 20));
        JLabel titleLabel = new JLabel("Investment Recycle Bin");
        titleLabel.setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(ModernTheme.TEXT_WHITE);

        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(createCloseButton(), BorderLayout.EAST);

        return header;
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

    private void styleTable(JTable table) {
        table.setFont(ModernTheme.FONT_BODY);
        table.setForeground(ModernTheme.TEXT_PRIMARY);
        table.setBackground(ModernTheme.SURFACE);
        table.setRowHeight(40);
        table.setSelectionBackground(new Color(34, 139, 34, 40));
        table.setSelectionForeground(ModernTheme.TEXT_PRIMARY);
        table.setGridColor(new Color(0, 0, 0, 30));
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setBackground(ModernTheme.SURFACE);
        header.setForeground(ModernTheme.TEXT_SECONDARY);
        header.setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD, 12f));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(34, 139, 34)));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));
    }

    private JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton restoreButton = ModernTheme.createSuccessButton("Restore Selected");
        restoreButton.setIcon(ModernIcons.create(ModernIcons.IconType.MAGIC, ModernTheme.TEXT_WHITE, 16));
        restoreButton.setIconTextGap(8);

        JButton deletePermButton = ModernTheme.createDangerButton("Permanently Delete Selected");
        deletePermButton.setIcon(ModernIcons.create(ModernIcons.IconType.DELETE, ModernTheme.TEXT_WHITE, 16));
        deletePermButton.setIconTextGap(8);

        JButton closeButton = ModernTheme.createSecondaryButton("Close");

        restoreButton.addActionListener(e -> restoreSelectedInvestment());
        deletePermButton.addActionListener(e -> deletePermanentlySelectedInvestment());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(closeButton);
        buttonPanel.add(restoreButton);
        buttonPanel.add(deletePermButton);

        return buttonPanel;
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

        int investmentId = (int) recycleBinModel.getValueAt(selectedRow, 0);

        try {
            manager.restoreInvestment(investmentId);
            loadRecycledInvestments();
            parentUI.refreshAfterInvestmentRestore();
            parentUI.showModernSuccessDialog("Restored", "Investment restored successfully.");
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

        int choice = parentUI.showModernConfirmDialog(
            "Confirm Permanent Delete",
            "Are you sure you want to permanently delete this investment?",
            "This action cannot be undone.",
            true
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.permanentlyDeleteInvestment(investmentId);
                loadRecycledInvestments();
                parentUI.showModernSuccessDialog("Deleted", "Investment permanently deleted.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error permanently deleting investment: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}