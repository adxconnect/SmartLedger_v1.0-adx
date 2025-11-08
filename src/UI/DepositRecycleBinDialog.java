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

public class DepositRecycleBinDialog extends JDialog {

    private final FinanceManager manager;
    private final FinanceManagerFullUI parentUI;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public DepositRecycleBinDialog(Frame owner, FinanceManager manager, FinanceManagerFullUI parentUI) {
        super(owner, "Deposit Recycle Bin", true);
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

        JPanel contentPanel = new JPanel(new BorderLayout(0, 14));
        contentPanel.setBackground(ModernTheme.SURFACE);
        contentPanel.setBorder(new EmptyBorder(18, 20, 20, 20));

        JLabel helperLabel = new JLabel("Restore deposits you might need again or delete them forever.");
        helperLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(11.5f));
        helperLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        helperLabel.setBorder(new EmptyBorder(0, 2, 0, 0));

        tableModel = new DefaultTableModel(new Object[]{
            "ID", "Type", "Holder", "Description", "Amount", "Deleted On"
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

        loadRecycledDeposits();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(ModernTheme.PRIMARY_DARK);
        header.setBorder(new EmptyBorder(14, 18, 14, 12));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel(ModernIcons.create(ModernIcons.IconType.RECYCLE, ModernTheme.TEXT_WHITE, 20));
        JLabel titleLabel = new JLabel("Deposit Recycle Bin");
        titleLabel.setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(ModernTheme.TEXT_WHITE);

        JLabel subtitleLabel = new JLabel("Soft-deleted deposits stay here until you restore or purge them.");
        subtitleLabel.setFont(ModernTheme.FONT_SMALL);
        subtitleLabel.setForeground(new Color(255, 255, 255, 180));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitleLabel);

        titlePanel.add(iconLabel);
        titlePanel.add(textPanel);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(createCloseButton(), BorderLayout.EAST);
        return header;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton closeButton = ModernTheme.createSecondaryButton("Close");
        closeButton.addActionListener(e -> dispose());

        JButton restoreButton = ModernTheme.createSuccessButton("Restore Selected");
        restoreButton.setIcon(ModernIcons.create(ModernIcons.IconType.MAGIC, ModernTheme.TEXT_WHITE, 16));
        restoreButton.setIconTextGap(8);
        restoreButton.addActionListener(e -> restoreSelectedDeposit());

        JButton deleteButton = ModernTheme.createDangerButton("Delete Permanently");
        deleteButton.setIcon(ModernIcons.create(ModernIcons.IconType.DELETE, ModernTheme.TEXT_WHITE, 16));
        deleteButton.setIconTextGap(8);
        deleteButton.addActionListener(e -> deleteSelectedDeposit());

        panel.add(closeButton);
        panel.add(restoreButton);
        panel.add(deleteButton);
        getRootPane().setDefaultButton(restoreButton);
        return panel;
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

    private void loadRecycledDeposits() {
        tableModel.setRowCount(0);
        try {
            List<Map<String, Object>> recycled = manager.getRecycledDepositsForUI();
            for (Map<String, Object> data : recycled) {
                String type = valueOrEmpty(data.get("deposit_type"));
                double amount = 0;
                if ("FD".equalsIgnoreCase(type)) {
                    amount = asDouble(data.get("principal_amount"));
                } else if ("RD".equalsIgnoreCase(type)) {
                    amount = asDouble(data.get("monthly_amount"));
                } else if ("Gullak".equalsIgnoreCase(type)) {
                    amount = asDouble(data.get("current_total"));
                }

                tableModel.addRow(new Object[] {
                    data.get("id"),
                    type,
                    valueOrEmpty(data.get("holder_name")),
                    valueOrEmpty(data.get("description")),
                    formatAmount(amount),
                    valueOrEmpty(data.get("deleted_on_str"))
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading deposit recycle bin: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restoreSelectedDeposit() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a deposit to restore.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int depositId = (int) tableModel.getValueAt(modelRow, 0);

        try {
            manager.restoreDeposit(depositId);
            loadRecycledDeposits();
            if (parentUI != null) {
                parentUI.refreshAfterDepositRestore();
            }
            JOptionPane.showMessageDialog(this, "Deposit restored successfully.", "Restored", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error restoring deposit: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedDeposit() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a deposit to delete permanently.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int depositId = (int) tableModel.getValueAt(modelRow, 0);

        int choice = JOptionPane.showConfirmDialog(
            this,
            "This will permanently delete the deposit. Continue?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            manager.permanentlyDeleteDeposit(depositId);
            loadRecycledDeposits();
            if (parentUI != null) {
                parentUI.refreshAfterDepositRestore();
            }
            JOptionPane.showMessageDialog(this, "Deposit deleted permanently.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting deposit: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String valueOrEmpty(Object value) {
        return value == null ? "" : value.toString();
    }

    private double asDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0;
    }

    private String formatAmount(double value) {
        return String.format("\u20B9%,.2f", value);
    }
}