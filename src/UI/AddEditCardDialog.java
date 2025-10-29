package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import src.Card; // Import the Card class
import src.FinanceManager;

public class AddEditCardDialog extends JDialog {

    private FinanceManager manager;
    private Card cardToEdit; // Null if adding, non-null if editing
    private FinanceManagerFullUI parentUI;

    // UI Components
    private JTextField cardNameField;
    private JComboBox<String> cardTypeComboBox;
    private JTextField cardNumberField;
    private JTextField validFromField; // Format MM/YY
    private JTextField validThroughField; // Format MM/YY
    private JPasswordField cvvField; // Use JPasswordField for basic masking
    private JTextField frontImagePathField;
    private JTextField backImagePathField;
    private JButton chooseFrontButton;
    private JButton chooseBackButton;

    // Credit Card Specific Fields
    private JPanel creditCardPanel; // Panel to hold these
    private JTextField creditLimitField;
    private JTextField currentExpensesField;
    private JTextField amountToPayField;
    private JTextField daysLeftField;
    private JLabel creditLimitLabel;
    private JLabel currentExpensesLabel;
    private JLabel amountToPayLabel;
    private JLabel daysLeftLabel;


    public AddEditCardDialog(Frame owner, FinanceManager manager, Card cardToEdit, FinanceManagerFullUI parentUI) {
        super(owner, (cardToEdit == null ? "Add New Card" : "Edit Card"), true); // Modal
        this.manager = manager;
        this.cardToEdit = cardToEdit;
        this.parentUI = parentUI;

        initComponents();
        setupDynamicFields();
        populateFieldsIfEditing();

        pack();
        setMinimumSize(new Dimension(500, 0)); // Ensure minimum width
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // --- Card Type ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Card Type:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        String[] types = {"Credit Card", "Debit Card"};
        cardTypeComboBox = new JComboBox<>(types);
        formPanel.add(cardTypeComboBox, gbc);
        gbc.gridwidth = 1; row++;

        // --- Card Name ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Card Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cardNameField = new JTextField(25);
        formPanel.add(cardNameField, gbc);
        gbc.gridwidth = 1; row++;

        // --- Card Number ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Card Number (16 digits):"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cardNumberField = new JTextField(16);
        formPanel.add(cardNumberField, gbc);
        gbc.gridwidth = 1; row++;

        // --- Valid From / Through ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Valid From (MM/YY, Opt):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; // Share space
        validFromField = new JTextField(5);
        formPanel.add(validFromField, gbc);

        gbc.gridx = 2; gbc.weightx = 0; // Label doesn't need extra space
        formPanel.add(new JLabel(" Valid Thru (MM/YY):"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        validThroughField = new JTextField(5);
        formPanel.add(validThroughField, gbc);
        gbc.weightx = 0; row++; // Reset weight

        // --- CVV ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("CVV/CVC:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; // Only span 1 column
        cvvField = new JPasswordField(4); // Use JPasswordField
        formPanel.add(cvvField, gbc);
        gbc.gridwidth = 1; row++;

        // --- Front Image ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Front Image Path:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        frontImagePathField = new JTextField(25);
        frontImagePathField.setEditable(false); // Path set by button
        formPanel.add(frontImagePathField, gbc);
        gbc.gridx = 3; gbc.gridwidth = 1;
        chooseFrontButton = new JButton("Choose...");
        formPanel.add(chooseFrontButton, gbc);
        row++;

        // --- Back Image ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Back Image Path:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        backImagePathField = new JTextField(25);
        backImagePathField.setEditable(false);
        formPanel.add(backImagePathField, gbc);
        gbc.gridx = 3; gbc.gridwidth = 1;
        chooseBackButton = new JButton("Choose...");
        formPanel.add(chooseBackButton, gbc);
        row++;

        // --- Credit Card Specific Panel ---
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 4; // Span all columns
        creditCardPanel = createCreditCardPanel();
        formPanel.add(creditCardPanel, gbc);
        gbc.gridwidth = 1; row++;

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // --- Action Listeners ---
        cardTypeComboBox.addActionListener(e -> setupDynamicFields());
        chooseFrontButton.addActionListener(e -> chooseImage(frontImagePathField));
        chooseBackButton.addActionListener(e -> chooseImage(backImagePathField));
        saveButton.addActionListener(e -> saveCard());
        cancelButton.addActionListener(e -> dispose());

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Disable type change if editing
        if (cardToEdit != null) {
            cardTypeComboBox.setEnabled(false);
        }
    }

    private JPanel createCreditCardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Credit Card Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        // Credit Limit
        gbc.gridx = 0; gbc.gridy = row;
        creditLimitLabel = new JLabel("Credit Limit:");
        panel.add(creditLimitLabel, gbc);
        gbc.gridx = 1;
        creditLimitField = new JTextField("0.0", 10);
        panel.add(creditLimitField, gbc);
        row++;

        // Current Expenses
        gbc.gridx = 0; gbc.gridy = row;
        currentExpensesLabel = new JLabel("Current Expenses:");
        panel.add(currentExpensesLabel, gbc);
        gbc.gridx = 1;
        currentExpensesField = new JTextField("0.0", 10);
        panel.add(currentExpensesField, gbc);
        row++;

        // Amount To Pay
        gbc.gridx = 0; gbc.gridy = row;
        amountToPayLabel = new JLabel("Amount To Pay:");
        panel.add(amountToPayLabel, gbc);
        gbc.gridx = 1;
        amountToPayField = new JTextField("0.0", 10);
        panel.add(amountToPayField, gbc);
        row++;

        // Days Left
        gbc.gridx = 0; gbc.gridy = row;
        daysLeftLabel = new JLabel("Days Left To Pay:");
        panel.add(daysLeftLabel, gbc);
        gbc.gridx = 1;
        daysLeftField = new JTextField("0", 5);
        panel.add(daysLeftField, gbc);
        row++;

        return panel;
    }

    private void setupDynamicFields() {
        boolean isCredit = "Credit Card".equals(cardTypeComboBox.getSelectedItem());
        creditCardPanel.setVisible(isCredit);
        this.pack(); // Adjust size
    }

    private void populateFieldsIfEditing() {
        if (cardToEdit == null) return;

        cardTypeComboBox.setSelectedItem(cardToEdit.getCardType());
        cardNameField.setText(cardToEdit.getCardName());
        cardNumberField.setText(cardToEdit.getCardNumber()); // Show full number when editing
        validFromField.setText(cardToEdit.getValidFrom());
        validThroughField.setText(cardToEdit.getValidThrough());
        cvvField.setText(cardToEdit.getCvv()); // Show CVV when editing
        frontImagePathField.setText(cardToEdit.getFrontImagePath());
        backImagePathField.setText(cardToEdit.getBackImagePath());

        if ("Credit Card".equals(cardToEdit.getCardType())) {
            creditLimitField.setText(String.valueOf(cardToEdit.getCreditLimit()));
            currentExpensesField.setText(String.valueOf(cardToEdit.getCurrentExpenses()));
            amountToPayField.setText(String.valueOf(cardToEdit.getAmountToPay()));
            daysLeftField.setText(String.valueOf(cardToEdit.getDaysLeftToPay()));
        }
        setupDynamicFields(); // Ensure correct visibility
    }

    private void chooseImage(JTextField pathField) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (jpg, png, gif, bmp)", "jpg", "jpeg", "png", "gif", "bmp");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            pathField.setText(file.getAbsolutePath());
        }
    }

    private boolean validateInput() {
        String cardNumber = cardNumberField.getText().trim();
        if (cardNumber.length() != 16 || !cardNumber.matches("\\d{16}")) {
            JOptionPane.showMessageDialog(this, "Card number must be exactly 16 digits.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // Basic MM/YY validation
        String validThru = validThroughField.getText().trim();
        if (!validThru.matches("\\d{2}/\\d{2}")) {
             JOptionPane.showMessageDialog(this, "Valid Through date must be in MM/YY format.", "Input Error", JOptionPane.ERROR_MESSAGE);
             return false;
        }
        String cvv = new String(cvvField.getPassword()).trim();
        if (!cvv.matches("\\d{3,4}")) {
             JOptionPane.showMessageDialog(this, "CVV must be 3 or 4 digits.", "Input Error", JOptionPane.ERROR_MESSAGE);
             return false;
        }
        // Add more validation as needed (e.g., check if numbers are valid numbers)
        return true;
    }

    private void saveCard() {
        if (!validateInput()) {
            return; // Stop if validation fails
        }

        try {
            String cardType = (String) cardTypeComboBox.getSelectedItem();
            String cardName = cardNameField.getText().trim();
            String cardNumber = cardNumberField.getText().trim();
            String validFrom = validFromField.getText().trim();
            String validThrough = validThroughField.getText().trim();
            String cvv = new String(cvvField.getPassword()).trim();
            String frontPath = frontImagePathField.getText().trim();
            String backPath = backImagePathField.getText().trim();

            double creditLimit = 0;
            double currentExpenses = 0;
            double amountToPay = 0;
            int daysLeft = 0;

            if ("Credit Card".equals(cardType)) {
                creditLimit = Double.parseDouble(creditLimitField.getText().trim());
                currentExpenses = Double.parseDouble(currentExpensesField.getText().trim());
                amountToPay = Double.parseDouble(amountToPayField.getText().trim());
                daysLeft = Integer.parseInt(daysLeftField.getText().trim());
            }

            String creationDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE); // YYYY-MM-DD

            if (cardToEdit == null) { // Adding new card
                Card newCard = new Card(
                    cardName, cardType, cardNumber, validFrom, validThrough, cvv,
                    frontPath, backPath, creditLimit, currentExpenses, amountToPay, daysLeft,
                    creationDate
                );
                manager.saveCard(newCard);
                JOptionPane.showMessageDialog(this, "Card added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            } else { // Editing existing card
                cardToEdit.setCardName(cardName);
                // cardType cannot be changed
                cardToEdit.setCardNumber(cardNumber);
                cardToEdit.setValidFrom(validFrom);
                cardToEdit.setValidThrough(validThrough);
                cardToEdit.setCvv(cvv);
                cardToEdit.setFrontImagePath(frontPath);
                cardToEdit.setBackImagePath(backPath);
                if ("Credit Card".equals(cardType)) {
                    cardToEdit.setCreditLimit(creditLimit);
                    cardToEdit.setCurrentExpenses(currentExpenses);
                    cardToEdit.setAmountToPay(amountToPay);
                    cardToEdit.setDaysLeftToPay(daysLeft);
                }
                // Update creation date? Probably not needed when editing.
                // cardToEdit.setCreationDate(creationDate);

                manager.updateCard(cardToEdit);
                JOptionPane.showMessageDialog(this, "Card updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            parentUI.refreshCards(); // Refresh the main list
            dispose(); // Close the dialog

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid number format in one of the fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(this, "Database error saving card: " + sqle.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Log detailed error
        }
    }
}