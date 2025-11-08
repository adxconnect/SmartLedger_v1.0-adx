package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import src.FinanceManager;

public class BankAccountRecycleBinDialog extends JDialog {

    private final FinanceManager manager;
    private final FinanceManagerFullUI parentUI;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public BankAccountRecycleBinDialog(Frame owner, FinanceManager manager, FinanceManagerFullUI parentUI) {
        super(owner, "Bank Account Recycle Bin", true);
        this.manager = manager;
        this.parentUI = parentUI;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.SURFACE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(20, ModernTheme.BORDER),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(ModernTheme.PRIMARY_DARK);
        headerPanel.setBorder(new EmptyBorder(14, 18, 14, 12));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel iconLabel = new JLabel(ModernIcons.create(ModernIcons.IconType.RECYCLE, ModernTheme.TEXT_WHITE, 20));
        JLabel titleLabel = new JLabel("Bank Account Recycle Bin");
        titleLabel.setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(ModernTheme.TEXT_WHITE);
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(createCloseButton(), BorderLayout.EAST);

        tableModel = new DefaultTableModel(new Object[]{
            "ID", "Bank", "Account #", "Holder", "Type", "Subtype", "Balance", "Deleted On"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        ModernTheme.styleScrollPane(scrollPane);

        JLabel helperLabel = new JLabel("Restore a bank account or permanently remove it.");
        helperLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(11.5f));
        helperLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        helperLabel.setBorder(new EmptyBorder(0, 2, 0, 0));

        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setBackground(ModernTheme.SURFACE);
        contentPanel.setBorder(new EmptyBorder(20, 22, 22, 22));
        contentPanel.add(helperLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buildButtonPanel(), BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
        pack();
        if (getWidth() < 760) {
            setSize(760, Math.max(getHeight(), 460));
        }
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
        setLocationRelativeTo(owner);

        loadRecycledAccounts();
    }

    private JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton restoreButton = ModernTheme.createSuccessButton("Restore Selected");
        restoreButton.setIcon(ModernIcons.create(ModernIcons.IconType.MAGIC, ModernTheme.TEXT_WHITE, 16));
        restoreButton.setIconTextGap(8);
        restoreButton.addActionListener(e -> restoreSelectedAccount());

        JButton deleteButton = ModernTheme.createDangerButton("Delete Permanently");
        deleteButton.setIcon(ModernIcons.create(ModernIcons.IconType.DELETE, ModernTheme.TEXT_WHITE, 16));
        deleteButton.setIconTextGap(8);
        deleteButton.addActionListener(e -> deleteSelectedAccount());

    JButton closeButton = ModernTheme.createSecondaryButton("Close");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(closeButton);
        buttonPanel.add(restoreButton);
        buttonPanel.add(deleteButton);
        getRootPane().setDefaultButton(restoreButton);
        return buttonPanel;
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
        table.setSelectionBackground(ModernTheme.PRIMARY_LIGHT);
        table.setSelectionForeground(ModernTheme.TEXT_PRIMARY);
        table.setGridColor(new Color(0, 0, 0, 30));
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setBackground(ModernTheme.SURFACE);
        header.setForeground(ModernTheme.TEXT_SECONDARY);
        header.setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD, 12f));
    }

    private void loadRecycledAccounts() {
        tableModel.setRowCount(0);
        try {
            List<Map<String, Object>> recycled = manager.getRecycledBankAccountsForUI();
            for (Map<String, Object> row : recycled) {
                tableModel.addRow(new Object[] {
                    row.get("id"),
                    valueOrEmpty(row.get("bank_name")),
                    valueOrEmpty(row.get("account_number")),
                    valueOrEmpty(row.get("holder_name")),
                    valueOrEmpty(row.get("account_type")),
                    valueOrEmpty(row.get("account_subtype")),
                    formatAmount(row.get("balance")),
                    valueOrEmpty(row.get("deleted_on_str"))
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading recycle bin: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restoreSelectedAccount() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account to restore.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int accountId = (int) tableModel.getValueAt(modelRow, 0);
        try {
            manager.restoreBankAccount(accountId);
            loadRecycledAccounts();
            parentUI.refreshAfterBankAccountRestore();
            JOptionPane.showMessageDialog(this, "Bank account restored successfully.", "Restored", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error restoring account: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedAccount() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an account to delete permanently.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int accountId = (int) tableModel.getValueAt(modelRow, 0);

        int choice = JOptionPane.showConfirmDialog(
            this,
            "This will permanently remove the account. Continue?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            manager.permanentlyDeleteBankAccount(accountId);
            loadRecycledAccounts();
            parentUI.refreshAfterBankAccountRestore();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting account: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String valueOrEmpty(Object value) {
        return value == null ? "" : value.toString();
    }

    private String formatAmount(Object value) {
        if (value instanceof Number) {
            return String.format("\u20B9%,.2f", ((Number) value).doubleValue());
        }
        return "\u20B9 0.00";
    }
}
