package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import src.Deposit;
import src.FinanceManager;

public class AddEditDepositDialog extends JDialog {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final FinanceManager manager;
    private final Deposit depositToEdit;
    private final FinanceManagerFullUI parentUI;

    private JTextField holderNameField;
    private JTextField descriptionField;
    private JTextField goalField;
    private JComboBox<String> typeComboBox;

    private CardLayout typeCardLayout;
    private JPanel typeCardPanel;

    private JTextField accountNumberField;
    private JTextField principalField;
    private JTextField monthlyAmountField;
    private JTextField rateField;
    private JTextField tenureField;
    private JComboBox<String> tenureUnitComboBox;
    private JTextField startDateField;

    private JPanel principalRow;
    private JPanel monthlyRow;

    private final Map<Integer, JTextField> gullakCountFields = new LinkedHashMap<>();
    private JTextField gullakDueField;

    private JButton saveButton;

    public AddEditDepositDialog(Frame owner, FinanceManager manager, Deposit depositToEdit, FinanceManagerFullUI parentUI) {
        super(owner, depositToEdit == null ? "Add Deposit" : "Edit Deposit", true);
        this.manager = manager;
        this.depositToEdit = depositToEdit;
        this.parentUI = parentUI;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel mainPanel = buildMainPanel();
        add(mainPanel);

        if (depositToEdit != null) {
            populateFieldsFromModel();
            setTypeSpecificEditable(false);
        } else {
            setTypeSpecificEditable(true);
            updateTypeSpecificFields();
        }

        pack();
        if (getWidth() < 680) {
            setSize(680, Math.max(getHeight(), 560));
        }
        if (getHeight() > 700) {
            setSize(getWidth(), 700);
        }
        updateDialogShape();
        setLocationRelativeTo(owner);
        getRootPane().setDefaultButton(saveButton);
    }

    private JPanel buildMainPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(ModernTheme.SURFACE);
        shell.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(22, ModernTheme.BORDER),
            new EmptyBorder(0, 0, 0, 0)
        ));

        shell.add(buildHeader(), BorderLayout.NORTH);
        shell.add(buildContent(), BorderLayout.CENTER);

        wrapper.add(shell, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(ModernTheme.PRIMARY_DARK);
        header.setBorder(new EmptyBorder(16, 20, 16, 16));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));

        JLabel iconLabel = new JLabel(ModernIcons.create(ModernIcons.IconType.DEPOSIT, ModernTheme.TEXT_WHITE, 22));
        titlePanel.add(iconLabel);
        titlePanel.add(Box.createHorizontalStrut(12));

        String titleText = depositToEdit == null ? "Add Deposit" : "Edit Deposit";
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(ModernTheme.TEXT_WHITE);

        String subtitleText = depositToEdit == null
            ? "Create a new FD, RD, or Gullak entry with modern styling."
            : "Update general details. Financial figures remain account scoped.";
        JLabel subtitleLabel = new JLabel(subtitleText);
        subtitleLabel.setFont(ModernTheme.FONT_SMALL);
        subtitleLabel.setForeground(new Color(255, 255, 255, 180));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitleLabel);

        titlePanel.add(textPanel);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(createCloseButton(), BorderLayout.EAST);
        return header;
    }

    private JPanel buildContent() {
        JPanel contentWrapper = new JPanel(new BorderLayout(0, 0));
        contentWrapper.setOpaque(false);
        contentWrapper.setBorder(new EmptyBorder(16, 16, 18, 16));

        String helperText = depositToEdit == null
            ? "All entries automatically use your signed-in account. Required fields are marked by context."
            : "Only summary details can be edited here. Use module actions for Gullak counts or FD/RD calculations.";
        JLabel helperLabel = new JLabel(helperText);
        helperLabel.setFont(ModernTheme.FONT_SMALL);
        helperLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        helperLabel.setBorder(new EmptyBorder(0, 2, 10, 2));

        JPanel stack = new JPanel(new GridBagLayout());
        stack.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 16, 0);

        JPanel baseCard = createBaseDetailsCard();
        stack.add(baseCard, gbc);

        typeCardLayout = new CardLayout();
        typeCardPanel = new JPanel(typeCardLayout);
        typeCardPanel.setOpaque(false);
        
        JPanel fdRdCard = createFdRdCard();
        JPanel gullakCard = createGullakCard();
        
        typeCardPanel.add(fdRdCard, "FD_RD");
        typeCardPanel.add(gullakCard, "GULLAK");

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        stack.add(typeCardPanel, gbc);

        JPanel scrollableContent = new JPanel(new BorderLayout(0, 0));
        scrollableContent.setOpaque(false);
        scrollableContent.add(helperLabel, BorderLayout.NORTH);
        scrollableContent.add(stack, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(scrollableContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ModernTheme.styleScrollPane(scrollPane);

        contentWrapper.add(scrollPane, BorderLayout.CENTER);
        contentWrapper.add(createButtonPanel(), BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::updateTypeSpecificFields);
        return contentWrapper;
    }

    private JPanel createBaseDetailsCard() {
        JPanel card = ModernTheme.createCardPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel cardTitle = new JLabel("Deposit Overview");
        cardTitle.setFont(ModernTheme.FONT_SUBTITLE.deriveFont(Font.BOLD, 14f));
        cardTitle.setForeground(ModernTheme.TEXT_PRIMARY);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(cardTitle);
        card.add(Box.createVerticalStrut(12));

        typeComboBox = new JComboBox<>(new String[]{"FD", "RD", "Gullak"});
        ModernTheme.styleComboBox(typeComboBox);
        typeComboBox.setPreferredSize(new Dimension(200, 35));
        typeComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        typeComboBox.addActionListener(e -> updateTypeSpecificFields());

        holderNameField = createTextField(25);
        descriptionField = createTextField(25);
        goalField = createTextField(25);

        card.add(createFormRow("Deposit Type", typeComboBox));
        card.add(Box.createVerticalStrut(10));
        card.add(createFormRow("Holder Name", holderNameField));
        card.add(Box.createVerticalStrut(10));
        card.add(createFormRow("Description (optional)", descriptionField));
        card.add(Box.createVerticalStrut(10));
        card.add(createFormRow("Savings Goal (optional)", goalField));

        return card;
    }

    private JPanel createFdRdCard() {
        JPanel card = ModernTheme.createCardPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel cardTitle = new JLabel("FD / RD Settings");
        cardTitle.setFont(ModernTheme.FONT_SUBTITLE.deriveFont(Font.BOLD, 14f));
        cardTitle.setForeground(ModernTheme.TEXT_PRIMARY);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(cardTitle);
        card.add(Box.createVerticalStrut(12));

        accountNumberField = createTextField(20);
        principalField = createNumericField(12);
        monthlyAmountField = createNumericField(12);
        rateField = createNumericField(8);
        tenureField = createNumericField(6);

        tenureUnitComboBox = new JComboBox<>();
        ModernTheme.styleComboBox(tenureUnitComboBox);
        tenureUnitComboBox.setMaximumSize(new Dimension(140, 40));

        startDateField = createTextField(12);
        startDateField.setText(LocalDate.now().format(DATE_FORMATTER));

        card.add(createFormRow("Linked Account Number", accountNumberField));
        card.add(Box.createVerticalStrut(10));

        principalRow = createFormRow("Principal Amount (₹)", principalField);
        card.add(principalRow);
        card.add(Box.createVerticalStrut(10));

        monthlyRow = createFormRow("Monthly Deposit (₹)", monthlyAmountField);
        card.add(monthlyRow);
        card.add(Box.createVerticalStrut(10));

        JPanel rateRow = createFormRow("Interest Rate (% p.a.)", rateField);
        card.add(rateRow);
        card.add(Box.createVerticalStrut(10));

        JPanel tenureInline = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tenureInline.setOpaque(false);
        tenureInline.setAlignmentX(Component.LEFT_ALIGNMENT);
        tenureField.setPreferredSize(new Dimension(120, 40));
        tenureField.setMaximumSize(new Dimension(120, 40));
        tenureUnitComboBox.setPreferredSize(new Dimension(120, 40));
        tenureInline.add(tenureField);
        tenureInline.add(tenureUnitComboBox);
        JPanel tenureRow = createFormRow("Tenure", tenureInline);
        card.add(tenureRow);
        card.add(Box.createVerticalStrut(10));

        JPanel startDateRow = createFormRow("Start Date (dd-MM-yyyy)", startDateField);
        card.add(startDateRow);

        updateTenureUnits("FD");
        return card;
    }

    private JPanel createGullakCard() {
        JPanel card = ModernTheme.createCardPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel cardTitle = new JLabel("Gullak Setup");
        cardTitle.setFont(ModernTheme.FONT_SUBTITLE.deriveFont(Font.BOLD, 14f));
        cardTitle.setForeground(ModernTheme.TEXT_PRIMARY);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel noteLabel = new JLabel("Enter starting coin counts. Totals auto-calculate in the dashboard.");
        noteLabel.setFont(ModernTheme.FONT_SMALL);
        noteLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        gullakDueField = createNumericField(10);
        gullakDueField.setText("0");

        JPanel grid = new JPanel(new GridLayout(3, 3, 10, 10));
        grid.setOpaque(false);
        gullakCountFields.clear();
        int[] denominations = {500, 200, 100, 50, 20, 10, 5, 2, 1};
        for (int denom : denominations) {
            JPanel cell = new JPanel();
            cell.setOpaque(false);
            cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));

            JLabel label = new JLabel("₹" + denom);
            label.setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD, 11f));
            label.setForeground(ModernTheme.TEXT_PRIMARY);

            JTextField field = createNumericField(6);
            field.setText("0");
            gullakCountFields.put(denom, field);

            cell.add(label);
            cell.add(Box.createVerticalStrut(3));
            cell.add(field);
            grid.add(cell);
        }

        card.add(cardTitle);
        card.add(Box.createVerticalStrut(5));
        card.add(noteLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(grid);
        card.add(Box.createVerticalStrut(14));
        card.add(createFormRow("Amount Due for Next Deposit (₹)", gullakDueField));

        return card;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton cancelButton = ModernTheme.createSecondaryButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        saveButton = ModernTheme.createPrimaryButton(depositToEdit == null ? "Add Deposit" : "Save Changes");
        ModernIcons.IconType iconType = depositToEdit == null ? ModernIcons.IconType.ADD : ModernIcons.IconType.EDIT;
        saveButton.setIcon(ModernIcons.create(iconType, ModernTheme.TEXT_WHITE, 16));
        saveButton.setIconTextGap(8);
        saveButton.addActionListener(e -> onSave());

        panel.add(cancelButton);
        panel.add(saveButton);
        return panel;
    }

    private JPanel createFormRow(String labelText, JComponent component) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD, 13f));
        label.setForeground(ModernTheme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (component instanceof JTextField) {
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        } else if (component instanceof JPanel) {
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
        } else if (component instanceof JComboBox) {
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        }

        row.add(label);
        row.add(Box.createVerticalStrut(6));
        row.add(component);

        return row;
    }

    private JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        ModernTheme.styleTextField(field);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return field;
    }

    private JTextField createNumericField(int columns) {
        JTextField field = createTextField(columns);
        field.setHorizontalAlignment(JTextField.RIGHT);
        return field;
    }

    private void populateFieldsFromModel() {
        if (depositToEdit == null) {
            return;
        }

        typeComboBox.setSelectedItem(depositToEdit.getDepositType());
        holderNameField.setText(valueOrEmpty(depositToEdit.getHolderName()));
        descriptionField.setText(valueOrEmpty(depositToEdit.getDescription()));
        goalField.setText(valueOrEmpty(depositToEdit.getGoal()));

        String type = depositToEdit.getDepositType();
        if ("Gullak".equals(type)) {
            Map<Integer, Integer> counts = depositToEdit.getDenominationCounts();
            for (Map.Entry<Integer, JTextField> entry : gullakCountFields.entrySet()) {
                int denom = entry.getKey();
                JTextField field = entry.getValue();
                int value = counts != null ? counts.getOrDefault(denom, 0) : 0;
                field.setText(String.valueOf(value));
            }
            gullakDueField.setText(String.format("%.2f", depositToEdit.getGullakDueAmount()));
        } else {
            accountNumberField.setText(valueOrEmpty(depositToEdit.getAccountNumber()));
            rateField.setText(formatNumber(depositToEdit.getInterestRate()));
            tenureField.setText(String.valueOf(depositToEdit.getTenure()));
            tenureUnitComboBox.setSelectedItem(valueOrEmpty(depositToEdit.getTenureUnit()));
            startDateField.setText(valueOrEmpty(depositToEdit.getStartDate()));
            if ("FD".equals(type)) {
                principalField.setText(formatNumber(depositToEdit.getPrincipalAmount()));
                monthlyAmountField.setText("0");
            } else {
                monthlyAmountField.setText(formatNumber(depositToEdit.getMonthlyAmount()));
                principalField.setText("0");
            }
        }

        updateTypeSpecificFields();
    }

    private void setTypeSpecificEditable(boolean editable) {
        if (accountNumberField != null) {
            accountNumberField.setEditable(editable);
        }
        if (principalField != null) {
            principalField.setEditable(editable);
        }
        if (monthlyAmountField != null) {
            monthlyAmountField.setEditable(editable);
        }
        if (rateField != null) {
            rateField.setEditable(editable);
        }
        if (tenureField != null) {
            tenureField.setEditable(editable);
        }
        if (tenureUnitComboBox != null) {
            tenureUnitComboBox.setEnabled(editable);
        }
        if (startDateField != null) {
            startDateField.setEditable(editable);
        }
        if (gullakDueField != null) {
            gullakDueField.setEditable(editable);
        }
        for (JTextField field : gullakCountFields.values()) {
            field.setEditable(editable);
        }
    }

    private void updateTypeSpecificFields() {
        if (typeCardPanel == null) {
            return;
        }

        String type = (String) typeComboBox.getSelectedItem();
        if (type == null) {
            type = "FD";
        }

        if ("Gullak".equals(type)) {
            typeCardLayout.show(typeCardPanel, "GULLAK");
        } else {
            typeCardLayout.show(typeCardPanel, "FD_RD");
            boolean isFd = "FD".equals(type);
            if (principalRow != null) {
                principalRow.setVisible(isFd);
            }
            if (monthlyRow != null) {
                monthlyRow.setVisible(!isFd);
            }
            updateTenureUnits(type);
        }

        typeCardPanel.revalidate();
        typeCardPanel.repaint();
        SwingUtilities.invokeLater(this::updateDialogShape);
    }

    private void updateTenureUnits(String type) {
        if (tenureUnitComboBox == null) {
            return;
        }
        String current = (String) tenureUnitComboBox.getSelectedItem();
        tenureUnitComboBox.removeAllItems();
        if ("FD".equals(type)) {
            tenureUnitComboBox.addItem("Days");
            tenureUnitComboBox.addItem("Months");
            tenureUnitComboBox.addItem("Years");
        } else {
            tenureUnitComboBox.addItem("Months");
            tenureUnitComboBox.addItem("Years");
        }
        if (current != null) {
            tenureUnitComboBox.setSelectedItem(current);
        }
        if (tenureUnitComboBox.getSelectedItem() == null && tenureUnitComboBox.getItemCount() > 0) {
            tenureUnitComboBox.setSelectedIndex(0);
        }
    }

    private void onSave() {
        try {
            FormData data = gatherFormData();
            if (depositToEdit == null) {
                Deposit deposit = new Deposit(
                    0,
                    data.type,
                    data.holderName,
                    data.description,
                    data.goal,
                    null,
                    data.accountNumber,
                    data.principalAmount,
                    data.monthlyAmount,
                    data.interestRate,
                    data.tenure,
                    data.tenureUnit,
                    data.startDate,
                    0,
                    null,
                    data.gullakDueAmount,
                    data.counts
                );
                manager.saveDeposit(deposit);
                JOptionPane.showMessageDialog(this, "Deposit added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Deposit updated = new Deposit(
                    depositToEdit.getId(),
                    depositToEdit.getDepositType(),
                    data.holderName,
                    data.description,
                    data.goal,
                    depositToEdit.getCreationDate(),
                    depositToEdit.getAccountNumber(),
                    depositToEdit.getPrincipalAmount(),
                    depositToEdit.getMonthlyAmount(),
                    depositToEdit.getInterestRate(),
                    depositToEdit.getTenure(),
                    depositToEdit.getTenureUnit(),
                    depositToEdit.getStartDate(),
                    depositToEdit.getCurrentTotal(),
                    depositToEdit.getLastUpdated(),
                    depositToEdit.getGullakDueAmount(),
                    depositToEdit.getDenominationCounts()
                );
                manager.updateDeposit(updated);
                JOptionPane.showMessageDialog(this, "Deposit updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
            }

            if (parentUI != null) {
                parentUI.refreshDeposits();
            }
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check Input", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Unable to persist deposit: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private FormData gatherFormData() throws ValidationException {
        FormData data = new FormData();
        data.type = depositToEdit == null ? (String) typeComboBox.getSelectedItem() : depositToEdit.getDepositType();
        if (data.type == null) {
            throw new ValidationException("Please choose a deposit type.");
        }

        data.holderName = holderNameField.getText().trim();
        if (data.holderName.isEmpty()) {
            throw new ValidationException("Holder name is required.");
        }
        data.description = descriptionField.getText().trim();
        data.goal = goalField.getText().trim();

        if (depositToEdit != null) {
            if ("Gullak".equals(data.type)) {
                data.counts = depositToEdit.getDenominationCounts();
                data.gullakDueAmount = depositToEdit.getGullakDueAmount();
                data.accountNumber = null;
            } else {
                data.accountNumber = depositToEdit.getAccountNumber();
                data.principalAmount = depositToEdit.getPrincipalAmount();
                data.monthlyAmount = depositToEdit.getMonthlyAmount();
                data.interestRate = depositToEdit.getInterestRate();
                data.tenure = depositToEdit.getTenure();
                data.tenureUnit = depositToEdit.getTenureUnit();
                data.startDate = depositToEdit.getStartDate();
                data.gullakDueAmount = 0;
            }
            return data;
        }

        if ("Gullak".equals(data.type)) {
            data.counts = new LinkedHashMap<>();
            for (Map.Entry<Integer, JTextField> entry : gullakCountFields.entrySet()) {
                String text = entry.getValue().getText().trim();
                int value = text.isEmpty() ? 0 : parseNonNegativeInt(text, "₹" + entry.getKey() + " count");
                data.counts.put(entry.getKey(), value);
            }
            String dueText = gullakDueField.getText().trim();
            data.gullakDueAmount = dueText.isEmpty() ? 0 : parseNonNegativeDouble(dueText, "Gullak due amount");
            data.accountNumber = null;
            data.principalAmount = 0;
            data.monthlyAmount = 0;
            data.interestRate = 0;
            data.tenure = 0;
            data.tenureUnit = null;
            data.startDate = null;
        } else {
            data.accountNumber = accountNumberField.getText().trim();
            if (data.accountNumber.isEmpty()) {
                throw new ValidationException("Account number is required for FD/RD deposits.");
            }
            data.interestRate = parseNonNegativeDouble(rateField.getText().trim(), "Interest rate");
            data.tenure = parsePositiveInt(tenureField.getText().trim(), "Tenure");
            data.tenureUnit = (String) tenureUnitComboBox.getSelectedItem();
            if (data.tenureUnit == null) {
                throw new ValidationException("Select a tenure unit.");
            }
            String startDateText = startDateField.getText().trim();
            if (startDateText.isEmpty()) {
                throw new ValidationException("Start date is required.");
            }
            validateDate(startDateText);
            data.startDate = startDateText;

            if ("FD".equals(data.type)) {
                data.principalAmount = parsePositiveDouble(principalField.getText().trim(), "Principal amount");
                data.monthlyAmount = 0;
            } else {
                data.principalAmount = 0;
                data.monthlyAmount = parsePositiveDouble(monthlyAmountField.getText().trim(), "Monthly amount");
            }
            data.counts = null;
            data.gullakDueAmount = 0;
        }

        return data;
    }

    private double parsePositiveDouble(String text, String label) throws ValidationException {
        double value = parseNonNegativeDouble(text, label);
        if (value <= 0) {
            throw new ValidationException(label + " must be greater than zero.");
        }
        return value;
    }

    private double parseNonNegativeDouble(String text, String label) throws ValidationException {
        if (text == null || text.isEmpty()) {
            throw new ValidationException(label + " is required.");
        }
        try {
            double value = Double.parseDouble(text);
            if (value < 0) {
                throw new ValidationException(label + " cannot be negative.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new ValidationException("Enter a valid number for " + label + ".");
        }
    }

    private int parsePositiveInt(String text, String label) throws ValidationException {
        int value = parseNonNegativeInt(text, label);
        if (value <= 0) {
            throw new ValidationException(label + " must be greater than zero.");
        }
        return value;
    }

    private int parseNonNegativeInt(String text, String label) throws ValidationException {
        if (text == null || text.isEmpty()) {
            throw new ValidationException(label + " is required.");
        }
        try {
            int value = Integer.parseInt(text);
            if (value < 0) {
                throw new ValidationException(label + " cannot be negative.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new ValidationException("Enter a valid number for " + label + ".");
        }
    }

    private void validateDate(String text) throws ValidationException {
        try {
            LocalDate.parse(text, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("Start date must follow dd-MM-yyyy format.");
        }
    }

    private JButton createCloseButton() {
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

    private void updateDialogShape() {
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatNumber(double value) {
        return String.format("%.2f", value);
    }

    private static class FormData {
        String type;
        String holderName;
        String description;
        String goal;
        String accountNumber;
        double principalAmount;
        double monthlyAmount;
        double interestRate;
        int tenure;
        String tenureUnit;
        String startDate;
        Map<Integer, Integer> counts;
        double gullakDueAmount;
    }

    private static class ValidationException extends Exception {
        ValidationException(String message) {
            super(message);
        }
    }
}