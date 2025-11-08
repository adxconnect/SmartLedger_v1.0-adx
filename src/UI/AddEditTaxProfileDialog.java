package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.NumberFormatter;

import src.FinanceManager;
import src.TaxProfile;

public class AddEditTaxProfileDialog extends JDialog {

    private FinanceManager manager;
    private TaxProfile profileToEdit; // Null if adding
    private FinanceManagerFullUI parentUI;

    // --- Common Fields ---
    private JTextField profileNameField;
    private JComboBox<String> profileTypeComboBox;
    private JComboBox<String> yearComboBox;
    private JTextArea notesArea;

    // --- Dynamic Panels ---
    private CardLayout cardLayout;
    private JPanel dynamicPanelContainer;
    private JPanel employeePanel;
    private JPanel companyPanel;
    private JPanel otherPanel;

    // --- Field Storage (for calculation) ---
    private Map<String, JFormattedTextField> employeeIncomeFields;
    private Map<String, JFormattedTextField> employeeDeductionFields;
    private Map<String, JFormattedTextField> companyIncomeFields;
    private Map<String, JFormattedTextField> companyDeductionFields;
    private Map<String, JFormattedTextField> otherIncomeFields;
    private Map<String, JFormattedTextField> otherDeductionFields;

    // Separate Tax Paid fields per profile type to avoid ambiguity
    private JFormattedTextField employeeTaxPaidField;
    private JFormattedTextField companyTaxPaidField;
    private JFormattedTextField otherTaxPaidField;

    // --- Display Labels ---
    private JLabel grossIncomeLabel;
    private JLabel totalDeductionsLabel;
    private JLabel taxableIncomeLabel;

    public AddEditTaxProfileDialog(Frame owner, FinanceManager manager, TaxProfile profileToEdit, FinanceManagerFullUI parentUI) {
        super(owner, (profileToEdit == null ? "Add New Tax Profile" : "Edit Tax Profile"), true);
        this.manager = manager;
        this.profileToEdit = profileToEdit;
        this.parentUI = parentUI;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        // Initialize maps
        employeeIncomeFields = new HashMap<>();
        employeeDeductionFields = new HashMap<>();
        companyIncomeFields = new HashMap<>();
        companyDeductionFields = new HashMap<>();
        otherIncomeFields = new HashMap<>();
        otherDeductionFields = new HashMap<>();

        initComponents();
        if (profileToEdit != null) {
            populateFieldsForEdit();
        }

        pack();
        if (getWidth() < 900) {
            setSize(900, Math.max(getHeight(), 680));
        }
        if (getHeight() > 800) {
            setSize(getWidth(), 800);
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

        // Modern Header
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(new Color(34, 139, 34)); // Green theme for tax
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 16));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel iconLabel = new JLabel(ModernIcons.create(ModernIcons.IconType.TAX, ModernTheme.TEXT_WHITE, 22));
        JLabel titleLabel = new JLabel(profileToEdit == null ? "Add New Tax Profile" : "Edit Tax Profile");
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
        
        // --- Main Form Panel ---
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.setBackground(ModernTheme.BACKGROUND);

        // --- 1. Top Panel (Common Info) ---
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(ModernTheme.BACKGROUND);
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel profileNameLabel = new JLabel("Profile Name:");
        profileNameLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        profileNameLabel.setFont(ModernTheme.FONT_BODY);
        topPanel.add(profileNameLabel, gbc);
        profileNameField = new JTextField(20);
        ModernTheme.styleTextField(profileNameField);
        gbc.gridx = 1; gbc.gridwidth = 3; topPanel.add(profileNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel profileTypeLabel = new JLabel("Profile Type:");
        profileTypeLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        profileTypeLabel.setFont(ModernTheme.FONT_BODY);
        topPanel.add(profileTypeLabel, gbc);
        String[] types = {"Employee", "Company", "Other"};
        profileTypeComboBox = new JComboBox<>(types);
        ModernTheme.styleComboBox(profileTypeComboBox);
        gbc.gridx = 1; gbc.gridwidth = 1; topPanel.add(profileTypeComboBox, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        JLabel financialYearLabel = new JLabel("Financial Year:");
        financialYearLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        financialYearLabel.setFont(ModernTheme.FONT_BODY);
        topPanel.add(financialYearLabel, gbc);
        String[] years = {"2025-26", "2024-25", "2023-24", "2022-23"}; // Example years
        yearComboBox = new JComboBox<>(years);
        ModernTheme.styleComboBox(yearComboBox);
        gbc.gridx = 3; topPanel.add(yearComboBox, gbc);

        // --- 2. Center Panel (Dynamic Forms) ---
        cardLayout = new CardLayout();
        dynamicPanelContainer = new JPanel(cardLayout);
        dynamicPanelContainer.setBackground(ModernTheme.BACKGROUND);

        // Create the 3 dynamic panels
        employeePanel = createEmployeePanel();
        companyPanel = createCompanyPanel();
        otherPanel = createOtherPanel();

        dynamicPanelContainer.add(employeePanel, "Employee");
        dynamicPanelContainer.add(companyPanel, "Company");
        dynamicPanelContainer.add(otherPanel, "Other");

        // --- 3. Bottom Panel (Calculation & Save) ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(ModernTheme.BACKGROUND);
        
        JPanel summaryPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        summaryPanel.setBackground(ModernTheme.BACKGROUND);
        summaryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        grossIncomeLabel = new JLabel("Gross Income: ₹0.00");
        grossIncomeLabel.setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD, 14f));
        grossIncomeLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        totalDeductionsLabel = new JLabel("Total Deductions: ₹0.00");
        totalDeductionsLabel.setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD, 14f));
        totalDeductionsLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        taxableIncomeLabel = new JLabel("Total Taxable Income: ₹0.00");
        taxableIncomeLabel.setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD, 16f));
        taxableIncomeLabel.setForeground(new Color(34, 139, 34)); // Green

        summaryPanel.add(grossIncomeLabel);
        summaryPanel.add(totalDeductionsLabel);
        summaryPanel.add(taxableIncomeLabel);
        
        JPanel notesAndSavePanel = new JPanel(new BorderLayout(5, 10));
        notesAndSavePanel.setBackground(ModernTheme.BACKGROUND);
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
        notesAndSavePanel.add(notesScrollPane, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ModernTheme.BACKGROUND);
        
        JButton calculateButton = ModernTheme.createSecondaryButton("Calculate");
        calculateButton.setIcon(ModernIcons.create(ModernIcons.IconType.MONEY, ModernTheme.TEXT_PRIMARY, 16));
        
        JButton saveButton = ModernTheme.createPrimaryButton("Save");
        saveButton.setIcon(ModernIcons.create(ModernIcons.IconType.ADD, ModernTheme.TEXT_WHITE, 16));
        
        JButton cancelButton = ModernTheme.createSecondaryButton("Cancel");
        
        buttonPanel.add(calculateButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        notesAndSavePanel.add(buttonPanel, BorderLayout.SOUTH);

        bottomPanel.add(summaryPanel, BorderLayout.NORTH);
        bottomPanel.add(notesAndSavePanel, BorderLayout.CENTER);

        // --- Add Listeners ---
        profileTypeComboBox.addActionListener(e -> cardLayout.show(dynamicPanelContainer, (String)profileTypeComboBox.getSelectedItem()));
        calculateButton.addActionListener(e -> calculateTotals());
        saveButton.addActionListener(e -> saveProfile());
        cancelButton.addActionListener(e -> dispose());
        
        // Disable type change if editing
        if (profileToEdit != null) {
            profileTypeComboBox.setEnabled(false);
        }

        // --- Assemble Main Dialog ---
        formPanel.add(topPanel, BorderLayout.NORTH);
        formPanel.add(dynamicPanelContainer, BorderLayout.CENTER);
        formPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        ModernTheme.styleScrollPane(scrollPane);
        
        contentWrapper.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(contentWrapper, BorderLayout.CENTER);
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
    
    // --- Panel Creation Helper Methods ---
    
    private NumberFormatter createNumberFormatter() {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(false); // No commas
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Double.class);
        formatter.setMinimum(0.0);
        formatter.setMaximum(Double.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true); // Value changes on valid key press
        return formatter;
    }

    // Helper to add fields to a panel and map them
    private void addCalcField(JPanel panel, GridBagConstraints gbc, String label, Map<String, JFormattedTextField> map, int y) {
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        fieldLabel.setFont(ModernTheme.FONT_BODY);
        panel.add(fieldLabel, gbc);
        
        JFormattedTextField field = new JFormattedTextField(createNumberFormatter());
        field.setValue(0.0);
        field.setColumns(12);
        field.setBackground(ModernTheme.SURFACE);
        field.setForeground(ModernTheme.TEXT_PRIMARY);
        field.setCaretColor(ModernTheme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, ModernTheme.BORDER),
            new EmptyBorder(6, 10, 6, 10)
        ));
        field.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
        map.put(label, field); // Store the field for calculation
    }

    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ModernTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(2, 5, 2, 5); gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        // Income
        addCalcField(panel, gbc, "Basic Salary:", employeeIncomeFields, row++);
        addCalcField(panel, gbc, "HRA Received:", employeeIncomeFields, row++);
        addCalcField(panel, gbc, "Special Allowances:", employeeIncomeFields, row++);
        addCalcField(panel, gbc, "Bonus/Commission:", employeeIncomeFields, row++);
        addCalcField(panel, gbc, "Other Income:", employeeIncomeFields, row++);

        // Spacer
        JSeparator separator1 = new JSeparator(SwingConstants.HORIZONTAL);
        separator1.setForeground(ModernTheme.BORDER);
        separator1.setBackground(ModernTheme.BORDER);
        gbc.gridy = row++; gbc.gridwidth = 2; panel.add(separator1, gbc);
        gbc.gridwidth = 1; // Reset

        // Deductions
        addCalcField(panel, gbc, "HRA Exemption:", employeeDeductionFields, row++);
        addCalcField(panel, gbc, "Standard Deduction:", employeeDeductionFields, row++);
        addCalcField(panel, gbc, "Professional Tax:", employeeDeductionFields, row++);
        addCalcField(panel, gbc, "Section 80C (PF, LIC, etc):", employeeDeductionFields, row++);
        addCalcField(panel, gbc, "Section 80D (Medical):", employeeDeductionFields, row++);
        addCalcField(panel, gbc, "Section 80E (Education Loan):", employeeDeductionFields, row++);
        addCalcField(panel, gbc, "Other Deductions:", employeeDeductionFields, row++);

        // Tax Paid
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        JLabel taxPaidLabel = new JLabel("Tax Paid (TDS):");
        taxPaidLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        taxPaidLabel.setFont(ModernTheme.FONT_BODY);
        panel.add(taxPaidLabel, gbc);
        employeeTaxPaidField = new JFormattedTextField(createNumberFormatter());
        employeeTaxPaidField.setValue(0.0);
        employeeTaxPaidField.setColumns(12);
        employeeTaxPaidField.setBackground(ModernTheme.SURFACE);
        employeeTaxPaidField.setForeground(ModernTheme.TEXT_PRIMARY);
        employeeTaxPaidField.setCaretColor(ModernTheme.TEXT_PRIMARY);
        employeeTaxPaidField.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, ModernTheme.BORDER),
            new EmptyBorder(6, 10, 6, 10)
        ));
        employeeTaxPaidField.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(employeeTaxPaidField, gbc);

        return panel;
    }

    private JPanel createCompanyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ModernTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(2, 5, 2, 5); gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        addCalcField(panel, gbc, "Gross Revenue/Turnover:", companyIncomeFields, row++);
        addCalcField(panel, gbc, "Other Income:", companyIncomeFields, row++);
        JSeparator separator2 = new JSeparator(SwingConstants.HORIZONTAL);
        separator2.setForeground(ModernTheme.BORDER);
        separator2.setBackground(ModernTheme.BORDER);
        gbc.gridy = row++; gbc.gridwidth = 2; panel.add(separator2, gbc);
        gbc.gridwidth = 1; // Reset
        addCalcField(panel, gbc, "Cost of Goods Sold (COGS):", companyDeductionFields, row++);
        addCalcField(panel, gbc, "Employee Salaries:", companyDeductionFields, row++);
        addCalcField(panel, gbc, "Operating Expenses (Rent, Utilities):", companyDeductionFields, row++);
        addCalcField(panel, gbc, "Depreciation:", companyDeductionFields, row++);
        addCalcField(panel, gbc, "Interest Payments:", companyDeductionFields, row++);
        addCalcField(panel, gbc, "Other Expenses/Deductions:", companyDeductionFields, row++);
        
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        JLabel companyTaxPaidLabel = new JLabel("Tax Paid (Advance, etc.):");
        companyTaxPaidLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        companyTaxPaidLabel.setFont(ModernTheme.FONT_BODY);
        panel.add(companyTaxPaidLabel, gbc);
        companyTaxPaidField = new JFormattedTextField(createNumberFormatter());
        companyTaxPaidField.setValue(0.0);
        companyTaxPaidField.setColumns(12);
        companyTaxPaidField.setBackground(ModernTheme.SURFACE);
        companyTaxPaidField.setForeground(ModernTheme.TEXT_PRIMARY);
        companyTaxPaidField.setCaretColor(ModernTheme.TEXT_PRIMARY);
        companyTaxPaidField.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, ModernTheme.BORDER),
            new EmptyBorder(6, 10, 6, 10)
        ));
        companyTaxPaidField.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(companyTaxPaidField, gbc);

        return panel;
    }
    
    private JPanel createOtherPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ModernTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(2, 5, 2, 5); gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addCalcField(panel, gbc, "Capital Gains (Stocks, etc.):", otherIncomeFields, row++);
        addCalcField(panel, gbc, "Income from Property:", otherIncomeFields, row++);
        addCalcField(panel, gbc, "Interest/Dividends:", otherIncomeFields, row++);
        addCalcField(panel, gbc, "Other:", otherIncomeFields, row++);
        JSeparator separator3 = new JSeparator(SwingConstants.HORIZONTAL);
        separator3.setForeground(ModernTheme.BORDER);
        separator3.setBackground(ModernTheme.BORDER);
        gbc.gridy = row++; gbc.gridwidth = 2; panel.add(separator3, gbc);
        gbc.gridwidth = 1; // Reset
        addCalcField(panel, gbc, "Capital Loss (if any):", otherDeductionFields, row++);
        addCalcField(panel, gbc, "Property Tax / Maintenance:", otherDeductionFields, row++);
        addCalcField(panel, gbc, "Other Deductions:", otherDeductionFields, row++);
        
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        JLabel otherTaxPaidLabel = new JLabel("Tax Paid (Advance, etc.):");
        otherTaxPaidLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        otherTaxPaidLabel.setFont(ModernTheme.FONT_BODY);
        panel.add(otherTaxPaidLabel, gbc);
        otherTaxPaidField = new JFormattedTextField(createNumberFormatter());
        otherTaxPaidField.setValue(0.0);
        otherTaxPaidField.setColumns(12);
        otherTaxPaidField.setBackground(ModernTheme.SURFACE);
        otherTaxPaidField.setForeground(ModernTheme.TEXT_PRIMARY);
        otherTaxPaidField.setCaretColor(ModernTheme.TEXT_PRIMARY);
        otherTaxPaidField.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, ModernTheme.BORDER),
            new EmptyBorder(6, 10, 6, 10)
        ));
        otherTaxPaidField.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(otherTaxPaidField, gbc);

        return panel;
    }


    // --- Logic Methods ---

    private double getSum(Map<String, JFormattedTextField> map) {
        double total = 0;
        for (JFormattedTextField field : map.values()) {
            if (field.isDisplayable()) { // Only sum visible fields
                total += (Double) field.getValue();
            }
        }
        return total;
    }

    private void calculateTotals() {
        String selectedType = (String) profileTypeComboBox.getSelectedItem();
        double gross = 0;
        double deductions = 0;
        
        if ("Employee".equals(selectedType)) {
            gross = getSum(employeeIncomeFields);
            deductions = getSum(employeeDeductionFields);
        } else if ("Company".equals(selectedType)) {
            gross = getSum(companyIncomeFields);
            deductions = getSum(companyDeductionFields);
        } else if ("Other".equals(selectedType)) {
            gross = getSum(otherIncomeFields);
            deductions = getSum(otherDeductionFields);
        }

        double taxable = Math.max(0, gross - deductions);

        // Update labels
        grossIncomeLabel.setText(String.format("Gross Income: ₹%.2f", gross));
        totalDeductionsLabel.setText(String.format("Total Deductions: - ₹%.2f", deductions));
        taxableIncomeLabel.setText(String.format("Total Taxable Income: ₹%.2f", taxable));
    }

    private void saveProfile() {
        // First, run calculation to ensure totals are up to date
        calculateTotals();

        try {
            String profileName = profileNameField.getText();
            String profileType = (String) profileTypeComboBox.getSelectedItem();
            String financialYear = (String) yearComboBox.getSelectedItem();
            String notes = notesArea.getText();
            
            // Get calculated totals from labels (or re-calculate)
            double gross = Double.parseDouble(grossIncomeLabel.getText().replaceAll("[^\\d.]", ""));
            double deductions = Double.parseDouble(totalDeductionsLabel.getText().replaceAll("[^\\d.]", ""));
            
            // Read Tax Paid from the correct panel's dedicated field
            double taxPaid = 0.0;
            if ("Employee".equals(profileType)) {
                taxPaid = ((Number) employeeTaxPaidField.getValue()).doubleValue();
            } else if ("Company".equals(profileType)) {
                taxPaid = ((Number) companyTaxPaidField.getValue()).doubleValue();
            } else {
                taxPaid = ((Number) otherTaxPaidField.getValue()).doubleValue();
            }


            if (profileToEdit == null) { // Adding New
                TaxProfile newProfile = new TaxProfile(profileName, profileType, financialYear,
                                                       gross, deductions, taxPaid, notes);
                newProfile.calculateTaxableIncome(); // Ensure calculation
                manager.saveTaxProfile(newProfile);
                JOptionPane.showMessageDialog(this, "Tax Profile saved successfully!");
            } else { // Editing Existing
                profileToEdit.setProfileName(profileName);
                profileToEdit.setFinancialYear(financialYear);
                profileToEdit.setGrossIncome(gross);
                profileToEdit.setTotalDeductions(deductions);
                profileToEdit.setTaxPaid(taxPaid);
                profileToEdit.setNotes(notes);
                // Type is disabled, no need to set
                
                manager.updateTaxProfile(profileToEdit); // This will re-calculate taxable income
                JOptionPane.showMessageDialog(this, "Tax Profile updated successfully!");
            }
            
            parentUI.refreshTaxProfiles(); // Refresh main list
            dispose();

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid number in one of the fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            nfe.printStackTrace();
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(this, "Database error saving profile: " + sqle.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            sqle.printStackTrace();
        }
    }

    /**
     * Loads existing data into the form if editing
     */
    private void populateFieldsForEdit() {
        if (profileToEdit == null) return;

        profileNameField.setText(profileToEdit.getProfileName());
        profileTypeComboBox.setSelectedItem(profileToEdit.getProfileType());
        yearComboBox.setSelectedItem(profileToEdit.getFinancialYear());
        notesArea.setText(profileToEdit.getNotes());
        
        // This is complex because we don't store individual fields in the DB
        // We only store totals. We'll set the totals labels, but fields will remain 0.
        // A better design would store the individual fields as JSON in the DB.
        
        grossIncomeLabel.setText(String.format("Gross Income: ₹%.2f", profileToEdit.getGrossIncome()));
        totalDeductionsLabel.setText(String.format("Total Deductions: - ₹%.2f", profileToEdit.getTotalDeductions()));
        taxableIncomeLabel.setText(String.format("Total Taxable Income: ₹%.2f", profileToEdit.getTaxableIncome()));
        
        // Set the tax paid field based on profile type
        if ("Employee".equals(profileToEdit.getProfileType())) {
            if (employeeTaxPaidField != null) employeeTaxPaidField.setValue(profileToEdit.getTaxPaid());
        } else if ("Company".equals(profileToEdit.getProfileType())) {
            if (companyTaxPaidField != null) companyTaxPaidField.setValue(profileToEdit.getTaxPaid());
        } else if ("Other".equals(profileToEdit.getProfileType())) {
            if (otherTaxPaidField != null) otherTaxPaidField.setValue(profileToEdit.getTaxPaid());
        }
        
        // Show the correct panel
        cardLayout.show(dynamicPanelContainer, profileToEdit.getProfileType());
    }
}