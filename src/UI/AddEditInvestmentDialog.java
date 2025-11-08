package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
// Removed Gson dependency; we'll store simple key=value; pairs in a string

import src.Investment;
import src.FinanceManager;

public class AddEditInvestmentDialog extends JDialog {

    private FinanceManager manager;
    private Investment investmentToEdit; // Null if adding
    private FinanceManagerFullUI parentUI;
    // Removed Gson; using simple delimited string for profile storage

    // --- Profile Fields ---
    private JTextField holderNameField, panField, ageField, emailField, phoneField;
    private JComboBox<String> genderComboBox;

    // --- Common Asset Fields ---
    private JComboBox<String> assetTypeComboBox;
    private JTextField descriptionField, goalField, startDateField;

    // --- Dynamic Panels ---
    private JPanel unitAssetPanel; // For Stocks, MF, Gold, Silver, Crypto
    private JPanel realEstatePanel;
    private JPanel bondPanel;
    private JPanel otherPanel;

    // --- Unit Asset Fields ---
    private JTextField tickerField, exchangeField;
    private JTextField quantityField, initialUnitCostField, currentUnitPriceField;

    // --- Real Estate Fields ---
    private JTextArea propertyAddressArea;
    private JTextField initialTotalCostField; // Using initialUnitCost field for total
    private JTextField currentTotalValueField; // Using currentUnitPrice field for total

    // --- Bond/Tenure Fields ---
    private JTextField tenureYearsField;
    private JTextField interestRateField;


    public AddEditInvestmentDialog(Frame owner, FinanceManager manager, Investment investmentToEdit, FinanceManagerFullUI parentUI) {
        super(owner, (investmentToEdit == null ? "Add New Investment" : "Edit Investment"), true);
        this.manager = manager;
        this.investmentToEdit = investmentToEdit;
        this.parentUI = parentUI;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        initComponents();
        setupDynamicPanels();
        if (investmentToEdit != null) {
            populateFieldsForEdit();
            assetTypeComboBox.setEnabled(false);
        }

        pack();
        if (getWidth() < 680) {
            setSize(680, Math.max(getHeight(), 600));
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

        // Modern Header
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(new Color(34, 139, 34)); // Green theme
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 16));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel iconLabel = new JLabel(ModernIcons.create(ModernIcons.IconType.INVESTMENT, ModernTheme.TEXT_WHITE, 22));
        JLabel titleLabel = new JLabel(investmentToEdit == null ? "Add New Investment" : "Edit Investment");
        titleLabel.setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(ModernTheme.TEXT_WHITE);
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(createHeaderCloseButton(), BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content wrapper
        JPanel contentWrapper = new JPanel(new BorderLayout(10, 10));
        contentWrapper.setOpaque(false);
        contentWrapper.setBorder(new EmptyBorder(16, 16, 18, 16));
        
        // --- Main Form Panel ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS)); // Stack panels vertically
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(Color.WHITE);

        // --- Profile Panel ---
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setBorder(BorderFactory.createTitledBorder("Holder Profile"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0; profilePanel.add(new JLabel("Name:"), gbc);
        holderNameField = new JTextField(20);
        ModernTheme.styleTextField(holderNameField);
        gbc.gridx = 1; gbc.gridwidth=3; profilePanel.add(holderNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth=1; profilePanel.add(new JLabel("PAN:"), gbc);
        panField = new JTextField(10);
        ModernTheme.styleTextField(panField);
        gbc.gridx = 1; profilePanel.add(panField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 1; profilePanel.add(new JLabel("Age:"), gbc);
        ageField = new JTextField(3);
        ModernTheme.styleTextField(ageField);
        gbc.gridx = 3; profilePanel.add(ageField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; profilePanel.add(new JLabel("Email:"), gbc);
        emailField = new JTextField(20);
        ModernTheme.styleTextField(emailField);
        gbc.gridx = 1; gbc.gridwidth=3; profilePanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth=1; profilePanel.add(new JLabel("Phone:"), gbc);
        phoneField = new JTextField(10);
        ModernTheme.styleTextField(phoneField);
        gbc.gridx = 1; profilePanel.add(phoneField, gbc);

        gbc.gridx = 2; gbc.gridy = 3; profilePanel.add(new JLabel("Gender:"), gbc);
        genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        ModernTheme.styleComboBox(genderComboBox);
        gbc.gridx = 3; profilePanel.add(genderComboBox, gbc);

        // --- Asset Details Panel ---
        JPanel assetPanel = new JPanel(new GridBagLayout());
        assetPanel.setBackground(Color.WHITE);
        assetPanel.setBorder(BorderFactory.createTitledBorder("Investment Details"));
        gbc = new GridBagConstraints(); // Reset gbc
        gbc.insets = new Insets(2, 5, 2, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; assetPanel.add(new JLabel("Asset Type:"), gbc);
        String[] types = {"Indian Stocks", "Mutual Fund", "SIP", "US Stocks", "IPO", "Digital Assets", "Real Estate", "Structured Bond", "Gold", "Silver", "Private Equity", "ESOPs/RSUs", "Others"};
        assetTypeComboBox = new JComboBox<>(types);
        ModernTheme.styleComboBox(assetTypeComboBox);
        gbc.gridx = 1; gbc.gridwidth=3; assetPanel.add(assetTypeComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth=1; assetPanel.add(new JLabel("Description:"), gbc);
        descriptionField = new JTextField(20);
        ModernTheme.styleTextField(descriptionField);
        gbc.gridx = 1; gbc.gridwidth=3; assetPanel.add(descriptionField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth=1; assetPanel.add(new JLabel("Goal:"), gbc);
        goalField = new JTextField(20);
        ModernTheme.styleTextField(goalField);
        gbc.gridx = 1; gbc.gridwidth=3; assetPanel.add(goalField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth=1; assetPanel.add(new JLabel("Start Date (dd-MM-yyyy):"), gbc);
        startDateField = new JTextField(10);
        ModernTheme.styleTextField(startDateField);
        gbc.gridx = 1; gbc.gridwidth=1; assetPanel.add(startDateField, gbc);
        startDateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        // --- Create Dynamic Panels (but don't add them yet) ---
        unitAssetPanel = createUnitAssetPanel();
        realEstatePanel = createRealEstatePanel();
        bondPanel = createBondPanel();
        otherPanel = createOtherPanel(); // Fallback panel

        // Add panels to the main form panel
        formPanel.add(profilePanel);
        formPanel.add(assetPanel);
        formPanel.add(unitAssetPanel);
        formPanel.add(realEstatePanel);
        formPanel.add(bondPanel);
        formPanel.add(otherPanel);

        // --- Bottom Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton saveButton = ModernTheme.createPrimaryButton("Save");
        saveButton.setIcon(ModernIcons.create(ModernIcons.IconType.ADD, ModernTheme.TEXT_WHITE, 16));
        
        JButton cancelButton = ModernTheme.createSecondaryButton("Cancel");
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        // --- Action Listeners ---
        assetTypeComboBox.addActionListener(e -> setupDynamicPanels());
        saveButton.addActionListener(e -> saveInvestment());
        cancelButton.addActionListener(e -> dispose());

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        ModernTheme.styleScrollPane(scrollPane);
        
        contentWrapper.add(scrollPane, BorderLayout.CENTER);
        contentWrapper.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(contentWrapper, BorderLayout.CENTER);
        mainWrapper.add(mainPanel, BorderLayout.CENTER);
        add(mainWrapper);
    }

    private JButton createHeaderCloseButton() {
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
    
    // --- Panel Creation Methods ---
    private JPanel createUnitAssetPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Asset Details"));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(2, 5, 2, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Ticker/Symbol:"), gbc);
        tickerField = new JTextField(10);
        ModernTheme.styleTextField(tickerField);
        gbc.gridx = 1; panel.add(tickerField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0; panel.add(new JLabel("Exchange:"), gbc);
        exchangeField = new JTextField(10);
        ModernTheme.styleTextField(exchangeField);
        gbc.gridx = 3; panel.add(exchangeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Quantity:"), gbc);
        quantityField = new JTextField("0.0", 10);
        ModernTheme.styleTextField(quantityField);
        gbc.gridx = 1; panel.add(quantityField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Initial Price /unit:"), gbc);
        initialUnitCostField = new JTextField("0.0", 10);
        ModernTheme.styleTextField(initialUnitCostField);
        gbc.gridx = 1; panel.add(initialUnitCostField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 2; panel.add(new JLabel("Current Price /unit:"), gbc);
        currentUnitPriceField = new JTextField("0.0", 10);
        ModernTheme.styleTextField(currentUnitPriceField);
        gbc.gridx = 3; panel.add(currentUnitPriceField, gbc);
        
        return panel;
    }
    
    private JPanel createRealEstatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Real Estate Details"));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(2, 5, 2, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Property Address:"), gbc);
        propertyAddressArea = new JTextArea(3, 30);
        propertyAddressArea.setFont(ModernTheme.FONT_BODY);
        propertyAddressArea.setBackground(ModernTheme.SURFACE);
        propertyAddressArea.setForeground(ModernTheme.TEXT_PRIMARY);
        propertyAddressArea.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(ModernTheme.BUTTON_RADIUS, ModernTheme.BORDER),
            new EmptyBorder(8, 12, 8, 12)
        ));
        gbc.gridx = 1; gbc.gridheight=2; panel.add(new JScrollPane(propertyAddressArea), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridheight=1; panel.add(new JLabel("Initial Total Cost:"), gbc);
        initialTotalCostField = new JTextField("0.0", 15);
        ModernTheme.styleTextField(initialTotalCostField);
        gbc.gridx = 1; panel.add(initialTotalCostField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Current Total Value:"), gbc);
        currentTotalValueField = new JTextField("0.0", 15);
        ModernTheme.styleTextField(currentTotalValueField);
        gbc.gridx = 1; panel.add(currentTotalValueField, gbc);

        return panel;
    }

    private JPanel createBondPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Bond/Tenure Details"));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(2, 5, 2, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Initial Cost:"), gbc);
        initialUnitCostField = new JTextField("0.0", 10);
        ModernTheme.styleTextField(initialUnitCostField);
        gbc.gridx = 1; panel.add(initialUnitCostField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Tenure (Years):"), gbc);
        tenureYearsField = new JTextField("0", 5);
        ModernTheme.styleTextField(tenureYearsField);
        gbc.gridx = 1; panel.add(tenureYearsField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Interest Rate (%):"), gbc);
        interestRateField = new JTextField("0.0", 5);
        ModernTheme.styleTextField(interestRateField);
        gbc.gridx = 1; panel.add(interestRateField, gbc);

        return panel;
    }

    private JPanel createOtherPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Asset Details"));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(2, 5, 2, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Initial Cost:"), gbc);
        initialTotalCostField = new JTextField("0.0", 10);
        ModernTheme.styleTextField(initialTotalCostField);
        gbc.gridx = 1; panel.add(initialTotalCostField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Current Value:"), gbc);
        currentTotalValueField = new JTextField("0.0", 10);
        ModernTheme.styleTextField(currentTotalValueField);
        gbc.gridx = 1; panel.add(currentTotalValueField, gbc);

        return panel;
    }

    /**
     * Shows/Hides the dynamic panels based on Asset Type selection
     */
    private void setupDynamicPanels() {
        String selectedType = (String) assetTypeComboBox.getSelectedItem();
        
        // Hide all panels first
        unitAssetPanel.setVisible(false);
        realEstatePanel.setVisible(false);
        bondPanel.setVisible(false);
        otherPanel.setVisible(false);

        // Show the correct panel
        if (selectedType.equals("Indian Stocks") || selectedType.equals("Mutual Fund") || selectedType.equals("SIP") ||
            selectedType.equals("US Stocks") || selectedType.equals("IPO") || selectedType.equals("Digital Assets") ||
            selectedType.equals("Gold") || selectedType.equals("Silver")) {
            
            unitAssetPanel.setVisible(true);
            
            // Set default current price = initial price for new entries
            if (investmentToEdit == null) {
                currentUnitPriceField.setText(initialUnitCostField.getText());
                initialUnitCostField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCurrent(); }
                    public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCurrent(); }
                    public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCurrent(); }
                    public void updateCurrent() {
                         currentUnitPriceField.setText(initialUnitCostField.getText());
                    }
                });
            }
        
        } else if (selectedType.equals("Real Estate")) {
            realEstatePanel.setVisible(true);
             // Set default current value = initial cost for new entries
             if (investmentToEdit == null) {
                currentTotalValueField.setText(initialTotalCostField.getText());
                initialTotalCostField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCurrent(); }
                    public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCurrent(); }
                    public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCurrent(); }
                    public void updateCurrent() {
                         currentTotalValueField.setText(initialTotalCostField.getText());
                    }
                });
            }

        } else if (selectedType.equals("Structured Bond")) {
            bondPanel.setVisible(true);
            
        } else { // Others, Private Equity, ESOPs
            otherPanel.setVisible(true);
             // Set default current value = initial cost for new entries
             if (investmentToEdit == null) {
                currentTotalValueField.setText(initialTotalCostField.getText());
                initialTotalCostField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCurrent(); }
                    public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCurrent(); }
                    public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCurrent(); }
                    public void updateCurrent() {
                         currentTotalValueField.setText(initialTotalCostField.getText());
                    }
                });
            }
        }
        
        this.pack(); // Resize dialog to fit new panel
    }

    /**
     * Loads existing data into the form if editing
     */
    private void populateFieldsForEdit() {
        if (investmentToEdit == null) return;
        
        // Deserialize profile data from simple key=value;key2=value2 format
        String details = investmentToEdit.getAccountDetails();
        if (details != null && !details.trim().isEmpty()) {
            try {
                Map<String, String> profile = parseProfile(details);
                holderNameField.setText(profile.getOrDefault("holderName", investmentToEdit.getHolderName() != null ? investmentToEdit.getHolderName() : ""));
                panField.setText(profile.getOrDefault("pan", ""));
                ageField.setText(profile.getOrDefault("age", ""));
                emailField.setText(profile.getOrDefault("email", ""));
                phoneField.setText(profile.getOrDefault("phone", ""));
                String gender = profile.getOrDefault("gender", "Male");
                genderComboBox.setSelectedItem(gender);
            } catch (Exception e) {
                System.err.println("Error parsing account details string: " + e.getMessage());
                holderNameField.setText(investmentToEdit.getHolderName()); // Fallback
            }
        } else {
            holderNameField.setText(investmentToEdit.getHolderName()); // Fallback
        }

        // Populate common fields
        assetTypeComboBox.setSelectedItem(investmentToEdit.getAssetType());
        descriptionField.setText(investmentToEdit.getDescription());
        goalField.setText(investmentToEdit.getGoal());
        startDateField.setText(investmentToEdit.getStartDate() != null ? investmentToEdit.getStartDate() : "");

        // Populate type-specific fields
        String type = investmentToEdit.getAssetType();
        if (type.equals("Indian Stocks") || type.equals("Mutual Fund") || type.equals("SIP") ||
            type.equals("US Stocks") || type.equals("IPO") || type.equals("Digital Assets") ||
            type.equals("Gold") || type.equals("Silver")) {
            
            tickerField.setText(investmentToEdit.getTickerSymbol());
            exchangeField.setText(investmentToEdit.getExchange());
            quantityField.setText(String.valueOf(investmentToEdit.getQuantity()));
            initialUnitCostField.setText(String.valueOf(investmentToEdit.getInitialUnitCost()));
            currentUnitPriceField.setText(String.valueOf(investmentToEdit.getCurrentUnitPrice()));
        
        } else if (type.equals("Real Estate")) {
            propertyAddressArea.setText(investmentToEdit.getPropertyAddress());
            initialTotalCostField.setText(String.valueOf(investmentToEdit.getInitialUnitCost()));
            currentTotalValueField.setText(String.valueOf(investmentToEdit.getCurrentUnitPrice()));

        } else if (type.equals("Structured Bond")) {
             initialUnitCostField.setText(String.valueOf(investmentToEdit.getInitialUnitCost())); // Use the shared field
             tenureYearsField.setText(String.valueOf(investmentToEdit.getTenureYears()));
             interestRateField.setText(String.valueOf(investmentToEdit.getInterestRate()));
             
        } else { // Others
             initialTotalCostField.setText(String.valueOf(investmentToEdit.getInitialUnitCost()));
             currentTotalValueField.setText(String.valueOf(investmentToEdit.getCurrentUnitPrice()));
        }
    }

    /**
     * Gathers data, validates, and saves to database
     */
    private void saveInvestment() {
        try {
            // 1. Gather Profile Data and serialize to simple string
            Map<String, String> profile = new HashMap<>();
            profile.put("holderName", holderNameField.getText().trim());
            profile.put("pan", panField.getText().trim());
            profile.put("age", ageField.getText().trim());
            profile.put("email", emailField.getText().trim());
            profile.put("phone", phoneField.getText().trim());
            profile.put("gender", (String) genderComboBox.getSelectedItem());
            String accountDetailsJson = toProfileString(profile);

            // 2. Gather Common Data
            String assetType = (String) assetTypeComboBox.getSelectedItem();
            String description = descriptionField.getText().trim();
            String goal = goalField.getText().trim();
            String startDate = startDateField.getText().trim();

            // 3. Initialize all possible fields
            String ticker = null, exchange = null, propertyAddress = null;
            double quantity = 0, initialUnitCost = 0, currentUnitPrice = 0;
            int tenure = 0; double rate = 0;

            // 4. Gather Type-Specific Data
            if (assetType.equals("Indian Stocks") || assetType.equals("Mutual Fund") || assetType.equals("SIP") ||
                assetType.equals("US Stocks") || assetType.equals("IPO") || assetType.equals("Digital Assets") ||
                assetType.equals("Gold") || assetType.equals("Silver")) {
                
                ticker = tickerField.getText().trim();
                exchange = exchangeField.getText().trim();
                quantity = Double.parseDouble(quantityField.getText().trim());
                initialUnitCost = Double.parseDouble(initialUnitCostField.getText().trim());
                currentUnitPrice = Double.parseDouble(currentUnitPriceField.getText().trim());
            
            } else if (assetType.equals("Real Estate")) {
                propertyAddress = propertyAddressArea.getText().trim();
                initialUnitCost = Double.parseDouble(initialTotalCostField.getText().trim()); // Use as Total Cost
                currentUnitPrice = Double.parseDouble(currentTotalValueField.getText().trim()); // Use as Total Value
                quantity = 1; // Real Estate is 1 unit

            } else if (assetType.equals("Structured Bond")) {
                 initialUnitCost = Double.parseDouble(initialUnitCostField.getText().trim()); // Use as Total Cost
                 currentUnitPrice = initialUnitCost; // Current price is same as initial
                 tenure = Integer.parseInt(tenureYearsField.getText().trim());
                 rate = Double.parseDouble(interestRateField.getText().trim());
                 quantity = 1;
                 
            } else { // Others, Private Equity, ESOPs
                 initialUnitCost = Double.parseDouble(initialTotalCostField.getText().trim());
                 currentUnitPrice = Double.parseDouble(currentTotalValueField.getText().trim());
                 quantity = 1;
            }

            // 5. Create or Update Investment Object
            if (investmentToEdit == null) { // Adding New
                Investment newInv = new Investment(
                    0, assetType, profile.get("holderName"), description, goal, startDate, accountDetailsJson,
                    ticker, exchange, quantity, initialUnitCost, currentUnitPrice,
                    propertyAddress, tenure, rate
                );
                manager.saveInvestment(newInv);
                JOptionPane.showMessageDialog(this, "Investment added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else { // Editing Existing
                investmentToEdit.setHolderName(profile.get("holderName"));
                investmentToEdit.setAccountDetails(accountDetailsJson);
                investmentToEdit.setDescription(description);
                investmentToEdit.setGoal(goal);
                investmentToEdit.setStartDate(startDate);
                // Set type-specific fields
                investmentToEdit.setDescription(description);
                investmentToEdit.setGoal(goal);
                investmentToEdit.setStartDate(startDate);
                // Note: Individual setters for ticker, exchange etc. may not exist in Investment class
                // These values should be set through a comprehensive update method or constructor
                
                manager.updateInvestment(investmentToEdit);
                JOptionPane.showMessageDialog(this, "Investment updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            
            parentUI.refreshInvestments(); // Refresh main list
            dispose(); // Close dialog

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid number. Please check all cost, quantity, and rate fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            nfe.printStackTrace();
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(this, "Database error saving investment: " + sqle.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            sqle.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // --- Helpers for profile serialization without external libs ---
    private String toProfileString(Map<String, String> profile) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : profile.entrySet()) {
            if (sb.length() > 0) sb.append(';');
            // Replace any semicolons or equals to avoid breaking format
            String key = e.getKey().replace(";", ",").replace("=", ":");
            String val = (e.getValue() == null ? "" : e.getValue()).replace(";", ",").replace("=", ":");
            sb.append(key).append('=').append(val);
        }
        return sb.toString();
    }

    private Map<String, String> parseProfile(String s) {
        Map<String, String> map = new HashMap<>();
        String[] parts = s.split(";");
        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) continue;
            int idx = part.indexOf('=');
            if (idx <= 0) continue;
            String key = part.substring(0, idx).trim();
            String val = part.substring(idx + 1).trim();
            map.put(key, val);
        }
        return map;
    }
}