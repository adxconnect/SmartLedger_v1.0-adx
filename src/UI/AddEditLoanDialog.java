package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import javax.swing.text.NumberFormatter;

import src.FinanceManager;
import src.Loan;

public class AddEditLoanDialog extends JDialog {

    private FinanceManager manager;
    private Loan loanToEdit; // Null if adding
    private FinanceManagerFullUI parentUI;

    // --- UI Components ---
    private JTextField lenderNameField;
    private JComboBox<String> loanTypeComboBox;
    private JFormattedTextField principalField;
    private JFormattedTextField rateField;
    private JFormattedTextField tenureField; // In Months
    private JTextField startDateField;
    private JTextArea notesArea;
    private JComboBox<String> statusComboBox; // For editing status

    public AddEditLoanDialog(Frame owner, FinanceManager manager, Loan loanToEdit, FinanceManagerFullUI parentUI) {
        super(owner, (loanToEdit == null ? "Add New Loan" : "Edit Loan"), true);
        this.manager = manager;
        this.loanToEdit = loanToEdit;
        this.parentUI = parentUI;

        initComponents();
        if (loanToEdit != null) {
            populateFieldsForEdit();
        }
    }

    // In src/UI/AddEditLoanDialog.java
    
    private NumberFormatter createNumberFormatter(boolean allowDecimals) {
        NumberFormat format = NumberFormat.getNumberInstance();
        if (allowDecimals) {
            format.setGroupingUsed(false); // No commas
            format.setMaximumFractionDigits(2);
        } else {
            format.setParseIntegerOnly(true);
            format.setGroupingUsed(false);
        }
        
    NumberFormatter formatter = new NumberFormatter(format);
    // Accept any Number so parsed Long/Integer/Double values don't get rejected
    formatter.setValueClass(Number.class);
    formatter.setMinimum(0); // Minimum value
    formatter.setMaximum(allowDecimals ? Double.MAX_VALUE : Integer.MAX_VALUE); // Max value

    // Allow partial inputs like "1." while typing
    formatter.setAllowsInvalid(true);
    // Commit as soon as the edit is valid; we'll also persist text on focus loss
    formatter.setCommitsOnValidEdit(true);
        
        return formatter;
    }

    // --- Helpers to parse numbers robustly from text ---
    private double parseDoubleField(JFormattedTextField field) {
        String text = field.getText();
        if (text == null) text = "";
        text = text.trim().replace(",", ""); // drop grouping commas
        if (text.isEmpty()) throw new NumberFormatException("empty");
        return Double.parseDouble(text);
    }

    private int parseIntField(JFormattedTextField field) {
        String text = field.getText();
        if (text == null) text = "";
        text = text.trim().replace(",", "");
        if (text.isEmpty()) throw new NumberFormatException("empty");
        // allow users to type 12.0 etc.; round to nearest int
        return (int) Math.round(Double.parseDouble(text));
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

        // --- Lender Name ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Lender Name:"), gbc);
        gbc.gridx = 1;
        lenderNameField = new JTextField(20);
        formPanel.add(lenderNameField, gbc);
        row++;

        // --- Loan Type ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Loan Type:"), gbc);
        gbc.gridx = 1;
        String[] types = {"Personal", "Home", "Car", "Education", "Other"};
        loanTypeComboBox = new JComboBox<>(types);
        formPanel.add(loanTypeComboBox, gbc);
        row++;

        // --- Principal Amount ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Principal Amount (₹):"), gbc);
        gbc.gridx = 1;
    principalField = new JFormattedTextField(createNumberFormatter(true));
    principalField.setValue(0.0);
    principalField.setFocusLostBehavior(JFormattedTextField.PERSIST);
        formPanel.add(principalField, gbc);
        row++;

        // --- Interest Rate ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Annual Interest Rate (%):"), gbc);
        gbc.gridx = 1;
    rateField = new JFormattedTextField(createNumberFormatter(true));
    rateField.setValue(0.0);
    rateField.setFocusLostBehavior(JFormattedTextField.PERSIST);
        formPanel.add(rateField, gbc);
        row++;

        // --- Tenure ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Tenure (in Months):"), gbc);
        gbc.gridx = 1;
    tenureField = new JFormattedTextField(createNumberFormatter(false));
    tenureField.setValue(0);
    tenureField.setFocusLostBehavior(JFormattedTextField.PERSIST);
        formPanel.add(tenureField, gbc);
        row++;

        // --- Start Date ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Start Date (dd-MM-yyyy):"), gbc);
        gbc.gridx = 1;
        startDateField = new JTextField(10);
        startDateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        formPanel.add(startDateField, gbc);
        row++;
        
        // --- Status (Only visible when editing) ---
        JLabel statusLabel = new JLabel("Status:");
        String[] statuses = {"Active", "Paid Off"};
        statusComboBox = new JComboBox<>(statuses);
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(statusLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(statusComboBox, gbc);
        statusLabel.setVisible(loanToEdit != null);
        statusComboBox.setVisible(loanToEdit != null);
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
        saveButton.addActionListener(e -> saveLoan());
        cancelButton.addActionListener(e -> dispose());
        
        // Disable fields if editing
        if (loanToEdit != null) {
            lenderNameField.setEditable(false);
            loanTypeComboBox.setEnabled(false);
            principalField.setEditable(false);
            rateField.setEditable(false);
            tenureField.setEditable(false);
            startDateField.setEditable(false);
        }

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    pack();
    // Center relative to the dialog's owner window
    setLocationRelativeTo(getOwner());
    }

    /**
     * Loads existing loan data into the form when editing.
     */
    private void populateFieldsForEdit() {
        if (loanToEdit == null) return;
        
        lenderNameField.setText(loanToEdit.getLenderName());
        loanTypeComboBox.setSelectedItem(loanToEdit.getLoanType());
        principalField.setValue(loanToEdit.getPrincipalAmount());
        rateField.setValue(loanToEdit.getInterestRate());
        tenureField.setValue(loanToEdit.getTenureMonths());
        startDateField.setText(loanToEdit.getStartDate());
        statusComboBox.setSelectedItem(loanToEdit.getStatus());
        notesArea.setText(loanToEdit.getNotes());
    }

    /**
     * Gathers data, validates, and saves to the database.
     */
    private void saveLoan() {
        try {
            if (loanToEdit == null) { // Adding a new loan
                String lenderName = lenderNameField.getText();
                String loanType = (String) loanTypeComboBox.getSelectedItem();
                // Parse directly from text to avoid formatter edge cases
                double principal = parseDoubleField(principalField);
                double rate = parseDoubleField(rateField);
                int tenure = parseIntField(tenureField);
                String startDate = startDateField.getText();
                String notes = notesArea.getText();

                if (lenderName.isEmpty() || principal <= 0 || rate <= 0 || tenure <= 0) {
                    JOptionPane.showMessageDialog(this, "Lender, Principal, Rate, and Tenure are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Create new Loan object (this auto-calculates EMI, etc.)
                Loan newLoan = new Loan(lenderName, loanType, principal, rate, tenure, startDate, notes);
                
                manager.saveLoan(newLoan);
                JOptionPane.showMessageDialog(this, "Loan saved and calculations complete!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } else { // Editing an existing loan
                String newStatus = (String) statusComboBox.getSelectedItem();
                
                // We only allow updating the status and notes
                // Updating core terms would require a new loan entry
                manager.updateLoanStatus(loanToEdit.getId(), newStatus);
                // We should also add an updateNotes method if needed
                JOptionPane.showMessageDialog(this, "Loan status updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            parentUI.refreshLoans(); // Refresh the main list
            dispose(); // Close dialog

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid number. Please check Principal, Rate, and Tenure.", "Input Error", JOptionPane.ERROR_MESSAGE);
            nfe.printStackTrace();
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(this, "Database error saving loan: " + sqle.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            sqle.printStackTrace();
        }
    }
}