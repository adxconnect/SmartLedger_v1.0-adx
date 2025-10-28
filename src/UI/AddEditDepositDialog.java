package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import src.Deposit;
import src.FinanceManager;

public class AddEditDepositDialog extends JDialog {

    private FinanceManager manager;
    private Deposit depositToEdit; // Null if adding, non-null if editing
    private FinanceManagerFullUI parentUI;

    // --- Common Fields ---
    private JTextField holderNameField;
    private JTextField descriptionField;
    private JTextField goalField;
    private JComboBox<String> typeComboBox;

    // --- FD/RD Fields ---
    private JPanel fdRdPanel; // Panel to hold these fields
    private JTextField accountNumberField;
    private JTextField principalField; // FD
    private JTextField monthlyAmountField; // RD
    private JTextField rateField;
    private JTextField tenureField;
    private JComboBox<String> tenureUnitComboBox;
    private JTextField startDateField; // DD-MM-YYYY

    // --- Gullak Fields (Initial Counts Only) ---
    private JPanel gullakPanel; // Panel to hold these fields
    private Map<Integer, JTextField> countFields; // Map denomination to its text field

    // --- Labels (to show/hide) ---
    private JLabel principalLabel;
    private JLabel monthlyAmountLabel;
    private JLabel rateLabel;
    private JLabel tenureLabel;
    private JLabel startDateLabel;
    private JLabel accountNumberLabel;


    public AddEditDepositDialog(Frame owner, FinanceManager manager, Deposit depositToEdit, FinanceManagerFullUI parentUI) {
        super(owner, (depositToEdit == null ? "Add New Deposit" : "Edit Deposit"), true); // Modal
        this.manager = manager;
        this.depositToEdit = depositToEdit;
        this.parentUI = parentUI;
        this.countFields = new HashMap<>();

        initComponents();
        setupDynamicFields();
        populateFieldsIfEditing(); // Fill fields if editing

        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout()); // Use GridBagLayout for better control
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // --- Row 0: Deposit Type ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Deposit Type:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2; // Span across 2 columns
        String[] types = {"FD", "RD", "Gullak"};
        typeComboBox = new JComboBox<>(types);
        formPanel.add(typeComboBox, gbc);
        gbc.gridwidth = 1; // Reset grid width

        // --- Row 1: Holder Name ---
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Holder Name:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        holderNameField = new JTextField(20);
        formPanel.add(holderNameField, gbc);
        gbc.gridwidth = 1;

        // --- Row 2: Description ---
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        descriptionField = new JTextField(20);
        formPanel.add(descriptionField, gbc);
        gbc.gridwidth = 1;

        // --- Row 3: Goal ---
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Goal:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        goalField = new JTextField(20);
        formPanel.add(goalField, gbc);
        gbc.gridwidth = 1;

        // --- Placeholder for Dynamic Panels ---
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3; // Span all columns
        fdRdPanel = createFdRdPanel(); // Create the panel for FD/RD fields
        formPanel.add(fdRdPanel, gbc);

        gbc.gridy = 5;
        gullakPanel = createGullakPanel(); // Create the panel for Gullak fields
        formPanel.add(gullakPanel, gbc);
        gbc.gridwidth = 1; // Reset

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // --- Action Listeners ---
        typeComboBox.addActionListener(e -> setupDynamicFields());
        saveButton.addActionListener(e -> saveDeposit());
        cancelButton.addActionListener(e -> dispose());

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Disable type change if editing
        if (depositToEdit != null) {
            typeComboBox.setEnabled(false);
        }
    }

    private JPanel createFdRdPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        // Account Number
        gbc.gridx = 0; gbc.gridy = row;
        accountNumberLabel = new JLabel("Account Number:");
        panel.add(accountNumberLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        accountNumberField = new JTextField(15);
        panel.add(accountNumberField, gbc);
        gbc.gridwidth = 1; row++;

        // Principal (FD)
        gbc.gridx = 0; gbc.gridy = row;
        principalLabel = new JLabel("Principal Amount (FD):");
        panel.add(principalLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        principalField = new JTextField("0.0", 15);
        panel.add(principalField, gbc);
        gbc.gridwidth = 1; row++;

        // Monthly Amount (RD)
        gbc.gridx = 0; gbc.gridy = row;
        monthlyAmountLabel = new JLabel("Monthly Amount (RD):");
        panel.add(monthlyAmountLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        monthlyAmountField = new JTextField("0.0", 15);
        panel.add(monthlyAmountField, gbc);
        gbc.gridwidth = 1; row++;

        // Rate
        gbc.gridx = 0; gbc.gridy = row;
        rateLabel = new JLabel("Interest Rate (%):");
        panel.add(rateLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        rateField = new JTextField("0.0", 15);
        panel.add(rateField, gbc);
        gbc.gridwidth = 1; row++;

        // Tenure
        gbc.gridx = 0; gbc.gridy = row;
        tenureLabel = new JLabel("Tenure:");
        panel.add(tenureLabel, gbc);
        gbc.gridx = 1;
        tenureField = new JTextField("0", 5);
        panel.add(tenureField, gbc);
        gbc.gridx = 2;
        String[] units = {"Days", "Months", "Years"};
        tenureUnitComboBox = new JComboBox<>(units);
        panel.add(tenureUnitComboBox, gbc);
        row++;

        // Start Date
        gbc.gridx = 0; gbc.gridy = row;
        startDateLabel = new JLabel("Start Date (DD-MM-YYYY):");
        panel.add(startDateLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        startDateField = new JTextField(15);
        startDateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))); // Default to today
        panel.add(startDateField, gbc);
        gbc.gridwidth = 1; row++;

        return panel;
    }

    private JPanel createGullakPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Initial Denomination Counts (Optional)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int[] denominations = {500, 200, 100, 50, 20, 10, 5, 2, 1};
        int row = 0;
        int col = 0;
        for (int denom : denominations) {
            gbc.gridx = col * 2; // Label column
            gbc.gridy = row;
            panel.add(new JLabel("₹" + denom + " x "), gbc);

            gbc.gridx = col * 2 + 1; // Text field column
            JTextField countField = new JTextField("0", 4);
            panel.add(countField, gbc);
            countFields.put(denom, countField); // Store reference

            col++;
            if (col >= 3) { // 3 denominations per row
                col = 0;
                row++;
            }
        }
        return panel;
    }


    private void setupDynamicFields() {
        String selectedType = (String) typeComboBox.getSelectedItem();

        boolean isFd = "FD".equals(selectedType);
        boolean isRd = "RD".equals(selectedType);
        boolean isGullak = "Gullak".equals(selectedType);

        // Show/Hide Panels
        fdRdPanel.setVisible(isFd || isRd);
        gullakPanel.setVisible(isGullak);

        // Show/Hide individual fields within FD/RD panel
        if (isFd || isRd) {
            accountNumberLabel.setVisible(true);
            accountNumberField.setVisible(true);
            rateLabel.setVisible(true);
            rateField.setVisible(true);
            tenureLabel.setVisible(true);
            tenureField.setVisible(true);
            tenureUnitComboBox.setVisible(true);
            startDateLabel.setVisible(true);
            startDateField.setVisible(true);

            principalLabel.setVisible(isFd);
            principalField.setVisible(isFd);
            monthlyAmountLabel.setVisible(isRd);
            monthlyAmountField.setVisible(isRd);

            // Adjust tenure units based on type
             DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) tenureUnitComboBox.getModel();
             model.removeAllElements();
             if (isFd) {
                 model.addElement("Days");
                 model.addElement("Months");
                 model.addElement("Years");
             } else { // RD
                 model.addElement("Months");
                 model.addElement("Years");
             }

        }

        this.pack(); // Adjust dialog size
    }

    private void populateFieldsIfEditing() {
        if (depositToEdit == null) {
            return; // Nothing to populate
        }

        typeComboBox.setSelectedItem(depositToEdit.getDepositType());
        holderNameField.setText(depositToEdit.getHolderName());
        descriptionField.setText(depositToEdit.getDescription());
        goalField.setText(depositToEdit.getGoal());

        if ("FD".equals(depositToEdit.getDepositType())) {
            accountNumberField.setText(depositToEdit.getAccountNumber());
            principalField.setText(String.valueOf(depositToEdit.getPrincipalAmount()));
            rateField.setText(String.valueOf(depositToEdit.getInterestRate()));
            tenureField.setText(String.valueOf(depositToEdit.getTenure()));
            tenureUnitComboBox.setSelectedItem(depositToEdit.getTenureUnit());
            startDateField.setText(depositToEdit.getStartDate());
        } else if ("RD".equals(depositToEdit.getDepositType())) {
            accountNumberField.setText(depositToEdit.getAccountNumber());
            monthlyAmountField.setText(String.valueOf(depositToEdit.getMonthlyAmount()));
            rateField.setText(String.valueOf(depositToEdit.getInterestRate()));
            tenureField.setText(String.valueOf(depositToEdit.getTenure()));
            tenureUnitComboBox.setSelectedItem(depositToEdit.getTenureUnit());
            startDateField.setText(depositToEdit.getStartDate());
        } else if ("Gullak".equals(depositToEdit.getDepositType())) {
            // Populate counts - Note: Editing counts is done in GullakDialog
            // Here we just show the counts that were loaded.
             Map<Integer, Integer> counts = depositToEdit.getDenominationCounts();
             if (counts != null) {
                 for (Map.Entry<Integer, JTextField> entry : countFields.entrySet()) {
                     entry.getValue().setText(String.valueOf(counts.getOrDefault(entry.getKey(), 0)));
                 }
                 // Disable editing counts in this dialog
                 for(JTextField field : countFields.values()){
                     field.setEditable(false);
                 }
             }
        }
        // Re-run dynamic setup to ensure visibility is correct
        setupDynamicFields();
    }

    private void saveDeposit() {
        try {
            String depositType = (String) typeComboBox.getSelectedItem();
            String holderName = holderNameField.getText();
            String description = descriptionField.getText();
            String goal = goalField.getText();

            // Variables for different types
            String accountNumber = null;
            double principalAmount = 0;
            double monthlyAmount = 0;
            double interestRate = 0;
            int tenure = 0;
            String tenureUnit = null;
            String startDate = null;
            Map<Integer, Integer> counts = null;
            double gullakDueAmount = 0; // Keep existing due amount if editing


            if ("FD".equals(depositType) || "RD".equals(depositType)) {
                accountNumber = accountNumberField.getText();
                interestRate = Double.parseDouble(rateField.getText());
                tenure = Integer.parseInt(tenureField.getText());
                tenureUnit = (String) tenureUnitComboBox.getSelectedItem();
                startDate = startDateField.getText();
                if ("FD".equals(depositType)) {
                    principalAmount = Double.parseDouble(principalField.getText());
                } else { // RD
                    monthlyAmount = Double.parseDouble(monthlyAmountField.getText());
                }
            } else if ("Gullak".equals(depositType)) {
                 if (depositToEdit != null) {
                      // If editing, keep existing counts and due amount
                      counts = depositToEdit.getDenominationCounts();
                      gullakDueAmount = depositToEdit.getGullakDueAmount();
                 } else {
                     // If adding, read initial counts from fields
                     counts = new HashMap<>();
                     for (Map.Entry<Integer, JTextField> entry : countFields.entrySet()) {
                         counts.put(entry.getKey(), Integer.parseInt(entry.getValue().getText()));
                     }
                 }
            }

            // Create or Update Deposit Object
             Deposit deposit;
             if (depositToEdit == null) { // Adding new
                 deposit = new Deposit(
                     0, // ID will be generated by DB
                     depositType, holderName, description, goal, null, // creation date is set by DB
                     accountNumber, principalAmount, monthlyAmount, interestRate,
                     tenure, tenureUnit, startDate, 0, // current total calculated in object/DAO
                     null, // last updated set by DB
                     gullakDueAmount, // 0 for new Gullak
                     counts
                 );
                 manager.saveDeposit(deposit);
             } else { // Editing existing (Limited editing here)
                  // NOTE: We're primarily allowing edits of common fields here.
                  // More complex edits (like Gullak counts/due) happen elsewhere.
                  // We need an update method in FinanceManager. Let's assume one exists for now.

                  // Rebuild the object with potentially updated common fields
                   deposit = new Deposit(
                       depositToEdit.getId(), // Keep original ID
                       depositToEdit.getDepositType(), // Type cannot change
                       holderName, description, goal, // Updated fields
                       depositToEdit.getCreationDate(), // Keep original
                       // Keep original type-specific fields for simplicity in this dialog
                       depositToEdit.getAccountNumber(), depositToEdit.getPrincipalAmount(),
                       depositToEdit.getMonthlyAmount(), depositToEdit.getInterestRate(),
                       depositToEdit.getTenure(), depositToEdit.getTenureUnit(),
                       depositToEdit.getStartDate(), depositToEdit.getCurrentTotal(),
                       depositToEdit.getLastUpdated(), depositToEdit.getGullakDueAmount(),
                       depositToEdit.getDenominationCounts()
                   );
                   // !!! We need an updateDeposit method in FinanceManager !!!
                   // manager.updateDeposit(deposit); // Placeholder for update logic
                    JOptionPane.showMessageDialog(this, "Update logic not fully implemented yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
             }


            dispose(); // Close dialog on success
            parentUI.refreshDeposits();
            if (parentUI != null) {
                parentUI.repaint(); // Refresh the UI display
            }

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid number entered. Please check amounts, rates, tenure, and counts.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(this, "Database error saving deposit: " + sqle.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}