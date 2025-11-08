package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
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

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        initComponents();
        if (loanToEdit != null) {
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
        headerPanel.setBackground(new Color(34, 139, 34)); // Green theme for loans
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 16));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel iconLabel = new JLabel(ModernIcons.create(ModernIcons.IconType.LOAN, ModernTheme.TEXT_WHITE, 22));
        JLabel titleLabel = new JLabel(loanToEdit == null ? "Add New Loan" : "Edit Loan");
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
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ModernTheme.BACKGROUND);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // --- Lender Name ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lenderNameLabel = new JLabel("Lender Name:");
        lenderNameLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        lenderNameLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(lenderNameLabel, gbc);
        gbc.gridx = 1;
        lenderNameField = new JTextField(20);
        ModernTheme.styleTextField(lenderNameField);
        formPanel.add(lenderNameField, gbc);
        row++;

        // --- Loan Type ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel loanTypeLabel = new JLabel("Loan Type:");
        loanTypeLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        loanTypeLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(loanTypeLabel, gbc);
        gbc.gridx = 1;
        String[] types = {"Personal", "Home", "Car", "Education", "Other"};
        loanTypeComboBox = new JComboBox<>(types);
        ModernTheme.styleComboBox(loanTypeComboBox);
        formPanel.add(loanTypeComboBox, gbc);
        row++;

        // --- Principal Amount ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel principalLabel = new JLabel("Principal Amount (₹):");
        principalLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        principalLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(principalLabel, gbc);
        gbc.gridx = 1;
    principalField = new JFormattedTextField(createNumberFormatter(true));
    principalField.setValue(0.0);
    principalField.setFocusLostBehavior(JFormattedTextField.PERSIST);
        ModernTheme.styleTextField(principalField);
        formPanel.add(principalField, gbc);
        row++;

        // --- Interest Rate ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel rateLabel = new JLabel("Annual Interest Rate (%):");
        rateLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        rateLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(rateLabel, gbc);
        gbc.gridx = 1;
    rateField = new JFormattedTextField(createNumberFormatter(true));
    rateField.setValue(0.0);
    rateField.setFocusLostBehavior(JFormattedTextField.PERSIST);
        ModernTheme.styleTextField(rateField);
        formPanel.add(rateField, gbc);
        row++;

        // --- Tenure ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel tenureLabel = new JLabel("Tenure (in Months):");
        tenureLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        tenureLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(tenureLabel, gbc);
        gbc.gridx = 1;
    tenureField = new JFormattedTextField(createNumberFormatter(false));
    tenureField.setValue(0);
    tenureField.setFocusLostBehavior(JFormattedTextField.PERSIST);
        ModernTheme.styleTextField(tenureField);
        formPanel.add(tenureField, gbc);
        row++;

        // --- Start Date ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel startDateLabel = new JLabel("Start Date (dd-MM-yyyy):");
        startDateLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        startDateLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(startDateLabel, gbc);
        gbc.gridx = 1;
        startDateField = new JTextField(10);
        startDateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        ModernTheme.styleTextField(startDateField);
        formPanel.add(startDateField, gbc);
        row++;
        
        // --- Status (Only visible when editing) ---
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        statusLabel.setFont(ModernTheme.FONT_BODY);
        String[] statuses = {"Active", "Paid Off"};
        statusComboBox = new JComboBox<>(statuses);
        ModernTheme.styleComboBox(statusComboBox);
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(statusLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(statusComboBox, gbc);
        statusLabel.setVisible(loanToEdit != null);
        statusComboBox.setVisible(loanToEdit != null);
        row++;

        // --- Notes ---
        gbc.gridx = 0; gbc.gridy = row;
        JLabel notesLabel = new JLabel("Notes:");
        notesLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        notesLabel.setFont(ModernTheme.FONT_BODY);
        formPanel.add(notesLabel, gbc);
        gbc.gridx = 1;
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(ModernTheme.FONT_BODY);
        notesArea.setBackground(ModernTheme.SURFACE);
        notesArea.setForeground(ModernTheme.TEXT_PRIMARY);
        notesArea.setCaretColor(ModernTheme.TEXT_PRIMARY);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, ModernTheme.BORDER),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        notesScrollPane.setBorder(null);
        notesScrollPane.setOpaque(false);
        notesScrollPane.getViewport().setOpaque(false);
        formPanel.add(notesScrollPane, gbc);
        row++;

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(ModernTheme.BACKGROUND);
        
        JButton saveButton = ModernTheme.createSuccessButton(loanToEdit == null ? "Add Loan" : "Update Loan");
        JButton cancelButton = ModernTheme.createSecondaryButton("Cancel");
        
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

        contentWrapper.add(formPanel, BorderLayout.CENTER);
        contentWrapper.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(contentWrapper, BorderLayout.CENTER);
        mainWrapper.add(mainPanel, BorderLayout.CENTER);
        add(mainWrapper);
        
        // Populate if editing
        if (loanToEdit != null) {
            populateFieldsForEdit();
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