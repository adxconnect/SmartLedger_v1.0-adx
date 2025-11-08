package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
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

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        initComponents();
        setupDynamicFields();
        populateFieldsIfEditing();

        pack();
        if (getWidth() < 700) {
            setSize(700, Math.max(getHeight(), 600));
        }
        if (getHeight() > 750) {
            setSize(getWidth(), 750);
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

        // Modern Header - Green theme for Cards
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(new Color(34, 139, 34)); // Green theme
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 16));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel iconLabel = new JLabel(ModernIcons.create(ModernIcons.IconType.CREDIT_CARD, ModernTheme.TEXT_WHITE, 22));
        JLabel titleLabel = new JLabel(cardToEdit == null ? "Add New Card" : "Edit Card");
        titleLabel.setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(ModernTheme.TEXT_WHITE);
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(createHeaderCloseButton(), BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content wrapper
        JPanel contentWrapper = new JPanel(new BorderLayout(10, 10));
        contentWrapper.setBackground(ModernTheme.BACKGROUND);
        contentWrapper.setBorder(new EmptyBorder(16, 16, 18, 16));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ModernTheme.BACKGROUND);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // --- Card Type ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel cardTypeLabel = new JLabel("Card Type:");
        cardTypeLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        cardTypeLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(cardTypeLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        String[] types = {"Credit Card", "Debit Card"};
        cardTypeComboBox = new JComboBox<>(types);
        ModernTheme.styleComboBox(cardTypeComboBox);
        formPanel.add(cardTypeComboBox, gbc);
        gbc.gridwidth = 1; row++;

        // --- Card Name ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel cardNameLabel = new JLabel("Card Name:");
        cardNameLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        cardNameLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(cardNameLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cardNameField = new JTextField(25);
        ModernTheme.styleTextField(cardNameField);
        formPanel.add(cardNameField, gbc);
        gbc.gridwidth = 1; row++;

        // --- Card Number ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel cardNumberLabel = new JLabel("Card Number (16 digits):");
        cardNumberLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        cardNumberLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(cardNumberLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cardNumberField = new JTextField(16);
        ModernTheme.styleTextField(cardNumberField);
        formPanel.add(cardNumberField, gbc);
        gbc.gridwidth = 1; row++;

        // --- Valid From / Through ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel validFromLabel = new JLabel("Valid From (MM/YY, Opt):");
        validFromLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        validFromLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(validFromLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; // Share space
        validFromField = new JTextField(5);
        ModernTheme.styleTextField(validFromField);
        formPanel.add(validFromField, gbc);

        gbc.gridx = 2; gbc.weightx = 0; // Label doesn't need extra space
        JLabel validThruLabel = new JLabel(" Valid Thru (MM/YY):");
        validThruLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        validThruLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(validThruLabel, gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        validThroughField = new JTextField(5);
        ModernTheme.styleTextField(validThroughField);
        formPanel.add(validThroughField, gbc);
        gbc.weightx = 0; row++; // Reset weight

        // --- CVV ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel cvvLabel = new JLabel("CVV/CVC:");
        cvvLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        cvvLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(cvvLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; // Only span 1 column
        cvvField = new JPasswordField(4); // Use JPasswordField
        ModernTheme.styleTextField(cvvField);
        formPanel.add(cvvField, gbc);
        gbc.gridwidth = 1; row++;

        // --- Front Image ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel frontImageLabel = new JLabel("Front Image Path:");
        frontImageLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        frontImageLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(frontImageLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        frontImagePathField = new JTextField(25);
        frontImagePathField.setEditable(false); // Path set by button
        ModernTheme.styleTextField(frontImagePathField);
        formPanel.add(frontImagePathField, gbc);
        gbc.gridx = 3; gbc.gridwidth = 1;
        chooseFrontButton = ModernTheme.createSecondaryButton("Choose...");
        formPanel.add(chooseFrontButton, gbc);
        row++;

        // --- Back Image ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel backImageLabel = new JLabel("Back Image Path:");
        backImageLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        backImageLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(backImageLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        backImagePathField = new JTextField(25);
        backImagePathField.setEditable(false);
        ModernTheme.styleTextField(backImagePathField);
        formPanel.add(backImagePathField, gbc);
        gbc.gridx = 3; gbc.gridwidth = 1;
        chooseBackButton = ModernTheme.createSecondaryButton("Choose...");
        formPanel.add(chooseBackButton, gbc);
        row++;

        // --- Credit Card Specific Panel ---
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 4; // Span all columns
        creditCardPanel = createCreditCardPanel();
        formPanel.add(creditCardPanel, gbc);
        gbc.gridwidth = 1; row++;

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(ModernTheme.BACKGROUND);
        
        JButton saveButton = ModernTheme.createSuccessButton("Save");
        JButton cancelButton = ModernTheme.createSecondaryButton("Cancel");
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // --- Action Listeners ---
        cardTypeComboBox.addActionListener(e -> setupDynamicFields());
        chooseFrontButton.addActionListener(e -> chooseImage(frontImagePathField));
        chooseBackButton.addActionListener(e -> chooseImage(backImagePathField));
        saveButton.addActionListener(e -> saveCard());
        cancelButton.addActionListener(e -> dispose());

        contentWrapper.add(formPanel, BorderLayout.CENTER);
        contentWrapper.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(contentWrapper, BorderLayout.CENTER);
        mainWrapper.add(mainPanel, BorderLayout.CENTER);
        add(mainWrapper);

        // Disable type change if editing
        if (cardToEdit != null) {
            cardTypeComboBox.setEnabled(false);
        }
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

    private JPanel createCreditCardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ModernTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        // Credit Limit
        gbc.gridx = 0; gbc.gridy = row;
        creditLimitLabel = new JLabel("Credit Limit:");
        creditLimitLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        creditLimitLabel.setFont(ModernTheme.FONT_BODY);
        panel.add(creditLimitLabel, gbc);
        gbc.gridx = 1;
        creditLimitField = new JTextField("0.0", 10);
        ModernTheme.styleTextField(creditLimitField);
        panel.add(creditLimitField, gbc);
        row++;

        // Current Expenses
        gbc.gridx = 0; gbc.gridy = row;
        currentExpensesLabel = new JLabel("Current Expenses:");
        currentExpensesLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        currentExpensesLabel.setFont(ModernTheme.FONT_BODY);
        panel.add(currentExpensesLabel, gbc);
        gbc.gridx = 1;
        currentExpensesField = new JTextField("0.0", 10);
        ModernTheme.styleTextField(currentExpensesField);
        panel.add(currentExpensesField, gbc);
        row++;

        // Amount To Pay
        gbc.gridx = 0; gbc.gridy = row;
        amountToPayLabel = new JLabel("Amount To Pay:");
        amountToPayLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        amountToPayLabel.setFont(ModernTheme.FONT_BODY);
        panel.add(amountToPayLabel, gbc);
        gbc.gridx = 1;
        amountToPayField = new JTextField("0.0", 10);
        ModernTheme.styleTextField(amountToPayField);
        panel.add(amountToPayField, gbc);
        row++;

        // Days Left
        gbc.gridx = 0; gbc.gridy = row;
        daysLeftLabel = new JLabel("Days Left To Pay:");
        daysLeftLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        daysLeftLabel.setFont(ModernTheme.FONT_BODY);
        panel.add(daysLeftLabel, gbc);
        gbc.gridx = 1;
        daysLeftField = new JTextField("0", 5);
        ModernTheme.styleTextField(daysLeftField);
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