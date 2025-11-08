package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

import src.FinanceManager;
import src.TaxProfile;

public class TaxProfileRecycleBinDialog extends JDialog {

    private FinanceManager manager;
    private FinanceManagerFullUI parentUI;
    private JTable table;
    private DefaultTableModel tableModel;

    public TaxProfileRecycleBinDialog(Frame owner, FinanceManager manager, FinanceManagerFullUI parentUI) {
        super(owner, "Tax Profile Recycle Bin", true);
        this.manager = manager;
        this.parentUI = parentUI;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        initComponents();
        loadRecycleBin();

        pack();
        if (getWidth() < 800) setSize(800, getHeight());
        if (getHeight() > 600) setSize(getWidth(), 600);
        setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setOpaque(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.SURFACE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(22, ModernTheme.BORDER),
            new EmptyBorder(0, 0, 0, 0)
        ));

        // Modern Header
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(new Color(34, 139, 34)); // Green theme
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 16));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel iconLabel = new JLabel(ModernIcons.create(ModernIcons.IconType.RECYCLE, ModernTheme.TEXT_WHITE, 22));
        JLabel titleLabel = new JLabel("Tax Profile Recycle Bin");
        titleLabel.setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(ModernTheme.TEXT_WHITE);
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(createHeaderCloseButton(), BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Table
        String[] columns = {"Profile Name", "Type", "Financial Year", "Gross Income", "Deductions", "Taxable Income", "Deleted On"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(ModernTheme.FONT_BODY);
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(34, 139, 34, 40)); // Light green
        table.setSelectionForeground(ModernTheme.TEXT_PRIMARY);
        table.setGridColor(ModernTheme.BORDER);
        table.getTableHeader().setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD));
        table.getTableHeader().setBackground(ModernTheme.SURFACE);
        table.getTableHeader().setForeground(ModernTheme.TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, ModernTheme.BORDER));
        ModernTheme.styleScrollPane(scrollPane);

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton restoreButton = ModernTheme.createSuccessButton("Restore");
        restoreButton.setIcon(ModernIcons.create(ModernIcons.IconType.EDIT, ModernTheme.TEXT_WHITE, 16));
        
        JButton deleteButton = ModernTheme.createDangerButton("Delete Permanently");
        deleteButton.setIcon(ModernIcons.create(ModernIcons.IconType.DELETE, ModernTheme.TEXT_WHITE, 16));

        buttonPanel.add(restoreButton);
        buttonPanel.add(deleteButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Action listeners
        restoreButton.addActionListener(e -> restoreSelected());
        deleteButton.addActionListener(e -> deleteSelected());

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainWrapper.add(mainPanel, BorderLayout.CENTER);
        add(mainWrapper);
    }

    private JButton createHeaderCloseButton() {
        JButton closeBtn = new JButton("×");
        closeBtn.setFont(new Font("Arial", Font.PLAIN, 22));
        closeBtn.setForeground(ModernTheme.TEXT_WHITE);
        closeBtn.setBackground(new Color(0, 0, 0, 0));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeBtn.setBackground(new Color(255, 255, 255, 30));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeBtn.setBackground(new Color(0, 0, 0, 0));
            }
        });
        closeBtn.addActionListener(e -> dispose());
        return closeBtn;
    }

    private void loadRecycleBin() {
        try {
            List<TaxProfile> deletedProfiles = manager.getTaxProfilesFromRecycleBin();
            tableModel.setRowCount(0);

            for (TaxProfile profile : deletedProfiles) {
                tableModel.addRow(new Object[]{
                    profile.getProfileName(),
                    profile.getProfileType(),
                    profile.getFinancialYear(),
                    String.format("₹%.2f", profile.getGrossIncome()),
                    String.format("₹%.2f", profile.getTotalDeductions()),
                    String.format("₹%.2f", profile.getTaxableIncome()),
                    profile.getDeletedOn() != null ? profile.getDeletedOn().toString() : "N/A"
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading recycle bin: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void restoreSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a tax profile to restore.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Get the profile ID from the deleted profiles list
            List<TaxProfile> deletedProfiles = manager.getTaxProfilesFromRecycleBin();
            if (selectedRow >= deletedProfiles.size()) {
                JOptionPane.showMessageDialog(this, "Invalid selection.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            TaxProfile profile = deletedProfiles.get(selectedRow);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Restore tax profile '" + profile.getProfileName() + "'?",
                "Confirm Restore",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                manager.restoreTaxProfileFromRecycleBin(profile.getId());
                JOptionPane.showMessageDialog(this, "Tax profile restored successfully!");
                loadRecycleBin();
                parentUI.refreshTaxProfiles();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error restoring tax profile: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a tax profile to delete permanently.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Get the profile ID from the deleted profiles list
            List<TaxProfile> deletedProfiles = manager.getTaxProfilesFromRecycleBin();
            if (selectedRow >= deletedProfiles.size()) {
                JOptionPane.showMessageDialog(this, "Invalid selection.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            TaxProfile profile = deletedProfiles.get(selectedRow);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete tax profile '" + profile.getProfileName() + "'?\nThis action cannot be undone!",
                "Confirm Permanent Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                manager.deleteTaxProfilePermanently(profile.getId());
                JOptionPane.showMessageDialog(this, "Tax profile permanently deleted!");
                loadRecycleBin();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting tax profile: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
