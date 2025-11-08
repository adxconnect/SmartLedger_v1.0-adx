package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import src.FinanceManager;
import src.Lending; // Use the Lending class

public class AddEditLendingDialog extends JDialog {

    private FinanceManager manager;
    private Lending lendingToEdit; // Null if adding
    private FinanceManagerFullUI parentUI;

    // --- UI Components ---
    private JTextField borrowerNameField;
    private JComboBox<String> loanTypeComboBox;
    private JTextField principalField; // Use JTextField for simplicity
    private JTextField rateField;      // Use JTextField
    private JTextField tenureField;    // Use JTextField (In Months)
    private JTextField dateLentField;
    private JTextArea notesArea;
    private JComboBox<String> statusComboBox; // For editing status

    public AddEditLendingDialog(Frame owner, FinanceManager manager, Lending lendingToEdit, FinanceManagerFullUI parentUI) {
        super(owner, (lendingToEdit == null ? "Add New Lending Record" : "Edit Lending Record"), true);
        this.manager = manager;
        this.lendingToEdit = lendingToEdit;
        this.parentUI = parentUI;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        initComponents();
        if (lendingToEdit != null) {
            populateFieldsForEdit();
        }

        pack();
        if (getWidth() < 700) {
            setSize(700, Math.max(getHeight(), 550));
        }
        if (getHeight() > 700) {
            setSize(getWidth(), 700);
        }
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

        // Modern Header - Green theme for Lending
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(new Color(34, 139, 34)); // Green theme
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 16));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel iconLabel = new JLabel(ModernIcons.create(ModernIcons.IconType.LOAN, ModernTheme.TEXT_WHITE, 22));
        JLabel titleLabel = new JLabel(lendingToEdit == null ? "Add New Lending Record" : "Edit Lending Record");
        titleLabel.setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(ModernTheme.TEXT_WHITE);
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(createHeaderCloseButton(), BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content wrapper
        JPanel contentWrapper = new JPanel(new BorderLayout(10, 10));
        contentWrapper.setBackground(Color.WHITE);
        contentWrapper.setBorder(new EmptyBorder(16, 16, 18, 16));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // --- Borrower Name ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Borrower Name:"), gbc);
        gbc.gridx = 1;
        borrowerNameField = new JTextField(20);
        ModernTheme.styleTextField(borrowerNameField);
        formPanel.add(borrowerNameField, gbc);
        row++;

        // --- Loan Type ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Loan Type:"), gbc);
        gbc.gridx = 1;
        String[] types = {"Friend", "Family", "Business", "Other"};
        loanTypeComboBox = new JComboBox<>(types);
        ModernTheme.styleComboBox(loanTypeComboBox);
        formPanel.add(loanTypeComboBox, gbc);
        row++;

        // --- Principal Amount ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Principal Amount (₹):"), gbc);
        gbc.gridx = 1;
        principalField = new JTextField("0.0", 15);
        ModernTheme.styleTextField(principalField);
        formPanel.add(principalField, gbc);
        row++;

        // --- Interest Rate ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Annual Interest Rate (%):"), gbc);
        gbc.gridx = 1;
        rateField = new JTextField("0.0", 15);
        ModernTheme.styleTextField(rateField);
        formPanel.add(rateField, gbc);
        row++;

        // --- Tenure ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Tenure (in Months):"), gbc);
        gbc.gridx = 1;
        tenureField = new JTextField("0", 15);
        ModernTheme.styleTextField(tenureField);
        formPanel.add(tenureField, gbc);
        row++;

        // --- Date Lent ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Date Lent (dd-MM-yyyy):"), gbc);
        gbc.gridx = 1;
        dateLentField = new JTextField(10);
        dateLentField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        ModernTheme.styleTextField(dateLentField);
        formPanel.add(dateLentField, gbc);
        row++;
        
        // --- Status (Only visible when editing) ---
        JLabel statusLabel = new JLabel("Status:");
        String[] statuses = {"Active", "Repaid"};
        statusComboBox = new JComboBox<>(statuses);
        ModernTheme.styleComboBox(statusComboBox);
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(statusLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(statusComboBox, gbc);
        statusLabel.setVisible(lendingToEdit != null);
        statusComboBox.setVisible(lendingToEdit != null);
        row++;

        // --- Notes ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(ModernTheme.FONT_BODY);
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        notesScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernTheme.TEXT_SECONDARY, 1, true),
            new EmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(notesScrollPane, gbc);
        row++;

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = ModernTheme.createSuccessButton(lendingToEdit == null ? "Add Lending" : "Update Lending");
        JButton cancelButton = ModernTheme.createSecondaryButton("Cancel");
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // --- Action Listeners ---
        saveButton.addActionListener(e -> saveLending());
        cancelButton.addActionListener(e -> dispose());
        
        // Disable fields if editing (only allow status/notes update)
        if (lendingToEdit != null) {
            borrowerNameField.setEditable(false);
            loanTypeComboBox.setEnabled(false);
            principalField.setEditable(false);
            rateField.setEditable(false);
            tenureField.setEditable(false);
            dateLentField.setEditable(false);
        }

        contentWrapper.add(formPanel, BorderLayout.CENTER);
        contentWrapper.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(contentWrapper, BorderLayout.CENTER);
        mainWrapper.add(mainPanel, BorderLayout.CENTER);
        add(mainWrapper);
    }
    
    /**
     * Creates the close button for the header (× symbol).
     */
    private JButton createHeaderCloseButton() {
        JButton closeBtn = new JButton("×");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 20));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(new Color(34, 139, 34));
        closeBtn.setBorder(new EmptyBorder(0, 10, 0, 10));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeBtn.setBackground(new Color(24, 119, 24));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeBtn.setBackground(new Color(34, 139, 34));
            }
        });
        return closeBtn;
    }

    /**
     * Loads existing lending data into the form when editing.
     */
    private void populateFieldsForEdit() {
        if (lendingToEdit == null) return;
        
        borrowerNameField.setText(lendingToEdit.getBorrowerName());
        loanTypeComboBox.setSelectedItem(lendingToEdit.getLoanType());
        principalField.setText(String.valueOf(lendingToEdit.getPrincipalAmount()));
        rateField.setText(String.valueOf(lendingToEdit.getInterestRate()));
        tenureField.setText(String.valueOf(lendingToEdit.getTenureMonths()));
        dateLentField.setText(lendingToEdit.getDateLent());
        statusComboBox.setSelectedItem(lendingToEdit.getStatus());
        notesArea.setText(lendingToEdit.getNotes());
    }

    /**
     * Gathers data, validates, and saves to the database.
     */
    private void saveLending() {
        try {
            if (lendingToEdit == null) { // Adding a new lending record
                String borrowerName = borrowerNameField.getText();
                String loanType = (String) loanTypeComboBox.getSelectedItem();
                double principal = Double.parseDouble(principalField.getText().trim());
                double rate = Double.parseDouble(rateField.getText().trim());
                int tenure = Integer.parseInt(tenureField.getText().trim());
                String dateLent = dateLentField.getText();
                String notes = notesArea.getText();

                if (borrowerName.isEmpty() || principal <= 0 || tenure <= 0) {
                    JOptionPane.showMessageDialog(this, "Borrower Name, Principal, and Tenure are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Create new Lending object (this auto-calculates EMI, etc.)
                Lending newLending = new Lending(borrowerName, loanType, principal, rate, tenure, dateLent, notes);
                
                manager.saveLending(newLending);
                JOptionPane.showMessageDialog(this, "Lending record saved and calculations complete!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } else { // Editing an existing record
                String newStatus = (String) statusComboBox.getSelectedItem();
                
                // We only allow updating the status
                // We could add notes update later if needed
                manager.updateLendingStatus(lendingToEdit.getId(), newStatus);
                JOptionPane.showMessageDialog(this, "Lending status updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            parentUI.refreshLendings(); // Refresh the main list
            dispose(); // Close dialog

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid number. Please check Principal, Rate, and Tenure.", "Input Error", JOptionPane.ERROR_MESSAGE);
            nfe.printStackTrace();
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(this, "Database error saving lending record: " + sqle.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            sqle.printStackTrace();
        }
    }
}