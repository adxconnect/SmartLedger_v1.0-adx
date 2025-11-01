package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

        initComponents();
        if (lendingToEdit != null) {
            populateFieldsForEdit();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
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
        formPanel.add(borrowerNameField, gbc);
        row++;

        // --- Loan Type ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Loan Type:"), gbc);
        gbc.gridx = 1;
        String[] types = {"Friend", "Family", "Business", "Other"};
        loanTypeComboBox = new JComboBox<>(types);
        formPanel.add(loanTypeComboBox, gbc);
        row++;

        // --- Principal Amount ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Principal Amount (₹):"), gbc);
        gbc.gridx = 1;
        principalField = new JTextField("0.0", 15);
        formPanel.add(principalField, gbc);
        row++;

        // --- Interest Rate ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Annual Interest Rate (%):"), gbc);
        gbc.gridx = 1;
        rateField = new JTextField("0.0", 15);
        formPanel.add(rateField, gbc);
        row++;

        // --- Tenure ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Tenure (in Months):"), gbc);
        gbc.gridx = 1;
        tenureField = new JTextField("0", 15);
        formPanel.add(tenureField, gbc);
        row++;

        // --- Date Lent ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Date Lent (dd-MM-yyyy):"), gbc);
        gbc.gridx = 1;
        dateLentField = new JTextField(10);
        dateLentField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        formPanel.add(dateLentField, gbc);
        row++;
        
        // --- Status (Only visible when editing) ---
        JLabel statusLabel = new JLabel("Status:");
        String[] statuses = {"Active", "Repaid"};
        statusComboBox = new JComboBox<>(statuses);
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
        formPanel.add(new JScrollPane(notesArea), gbc);
        row++;

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
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

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());
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