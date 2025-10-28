package src.UI;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import src.CreditCard;
import src.FinanceManager;
import src.FixedDeposit;
import src.GoldSilverInvestment;
import src.MutualFund;
import src.RecurringDeposit;
import src.BankAccount;
import src.Transaction;
import java.sql.SQLException;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.border.EmptyBorder;

public class FinanceManagerFullUI extends JFrame {
    private FinanceManager manager;
    private DefaultTableModel transactionModel, bankModel, fdModel, rdModel, ccModel, gssModel, mfModel;
    private JTable transactionTable, bankTable, fdTable;

    private JList<BankAccount> bankAccountList;
    private DefaultListModel<BankAccount> bankListModel;
    private JPanel bankDetailPanel;

    public FinanceManagerFullUI() {
        setTitle("Finance Manager - MySQL Edition");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        try {
            manager = new FinanceManager(); // Connect DB
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "MySQL connection failed: " + e.getMessage());
            System.exit(1);
        }

        // JTabbedPane setup
        JTabbedPane tabs = new JTabbedPane();
        add(tabs);

        // ----- TRANSACTIONS PANEL -----
        String[] tcols = {"S.No.", "Date", "Timestamp", "Day", "Payment Method", "Category", "Type", "Payee", "Description", "Amount"};
        transactionModel = new DefaultTableModel(tcols, 0);
        transactionTable = new JTable(transactionModel);
        JPanel tPanel = new JPanel(new BorderLayout());
        tPanel.add(new JScrollPane(transactionTable), BorderLayout.CENTER);
        JButton addTxnBtn = new JButton("Add Transaction");
        addTxnBtn.addActionListener(e -> openTransactionDialog());
        tPanel.add(addTxnBtn, BorderLayout.SOUTH);
        tabs.addTab("Transactions", tPanel);
        refreshTransactions();

        // In src/UI/FinanceManagerFullUI.java constructor

// ----- BANK ACCOUNTS PANEL (NEW MASTER-DETAIL VIEW) -----

// 1. Create the main panel
JPanel bPanel = new JPanel(new BorderLayout());

// 2. Create the Split Pane (this splits the UI left and right)
JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
splitPane.setDividerLocation(200); // 200 pixels for the left-side list

// 3. Create the Left-Side List (Master List)
bankListModel = new DefaultListModel<>();
bankAccountList = new JList<>(bankListModel);
bankAccountList.setFont(new Font("Arial", Font.PLAIN, 14));
bankAccountList.setBorder(new EmptyBorder(5, 5, 5, 5));

// 4. Create the Right-Side Panel (Detail View)
bankDetailPanel = new JPanel(); // This panel will hold the details
bankDetailPanel.setLayout(new BorderLayout());
bankDetailPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

// 5. Add list and detail panel to the split pane
splitPane.setLeftComponent(new JScrollPane(bankAccountList));
splitPane.setRightComponent(bankDetailPanel);

bPanel.add(splitPane, BorderLayout.CENTER);

// 6. Create the bottom button panel
JPanel bankButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

JButton addBankBtn = new JButton("Add New Account");
addBankBtn.addActionListener(e -> openBankAccountDialog());

JButton deleteBankBtn = new JButton("Delete Selected Account");
deleteBankBtn.addActionListener(e -> deleteSelectedAccount()); // New action

bankButtonPanel.add(addBankBtn);
bankButtonPanel.add(deleteBankBtn);

bPanel.add(bankButtonPanel, BorderLayout.SOUTH);

// 7. Add the main panel to the tabs
tabs.addTab("Bank Accounts", bPanel);

// 8. Add a listener to update details when we click an item
bankAccountList.addListSelectionListener(new ListSelectionListener() {
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            BankAccount selected = bankAccountList.getSelectedValue();
            if (selected != null) {
                showAccountDetails(selected);
            }
        }
    }
});

// 9. Load the initial data
refreshBankAccounts();

String[] fdcols = {"Account Number", "Principal", "Rate", "Tenure (months)", "Start Date", "Holder"};
fdModel = new DefaultTableModel(fdcols, 0);
fdTable = new JTable(fdModel);
JPanel fdPanel = new JPanel(new BorderLayout());
fdPanel.add(new JScrollPane(fdTable), BorderLayout.CENTER);
JButton addFDBtn = new JButton("Add FD");
fdPanel.add(addFDBtn, BorderLayout.SOUTH);
tabs.addTab("Fixed Deposits", fdPanel);
addFDBtn.addActionListener(e -> openFixedDepositDialog());
refreshFixedDeposits();

String[] rdcols = {"Account Number","Monthly Amount","Rate","Tenure (months)","Start Date","Holder"};
rdModel = new DefaultTableModel(rdcols, 0);
JTable rdTable = new JTable(rdModel);
JPanel rdPanel = new JPanel(new BorderLayout());
rdPanel.add(new JScrollPane(rdTable), BorderLayout.CENTER);
JButton addRDBtn = new JButton("Add Recurring Deposit");
rdPanel.add(addRDBtn, BorderLayout.SOUTH);
tabs.addTab("Recurring Deposits", rdPanel);
addRDBtn.addActionListener(e -> openRecurringDepositDialog());
refreshRecurringDeposits();

String[] cccols = {"Card Name", "Credit Limit", "Expenses", "Amount To Pay", "Days Left"};
ccModel = new DefaultTableModel(cccols, 0);
JTable ccTable = new JTable(ccModel);
JPanel ccPanel = new JPanel(new BorderLayout());
ccPanel.add(new JScrollPane(ccTable), BorderLayout.CENTER);
JButton addCCBtn = new JButton("Add Credit Card");
ccPanel.add(addCCBtn, BorderLayout.SOUTH);
tabs.addTab("Credit Cards", ccPanel);

addCCBtn.addActionListener(e -> openCreditCardDialog());
refreshCreditCards();

// Gold & Silver Panel
String[] gssCols = {"Metal Type", "Weight (g)", "Price/g", "Total Value"};
gssModel = new DefaultTableModel(gssCols, 0);
JTable gssTable = new JTable(gssModel);
JPanel gssPanel = new JPanel(new BorderLayout());
gssPanel.add(new JScrollPane(gssTable), BorderLayout.CENTER);
JButton addGSSBtn = new JButton("Add Gold/Silver");
gssPanel.add(addGSSBtn, BorderLayout.SOUTH);
tabs.addTab("Gold/Silver Investments", gssPanel);
addGSSBtn.addActionListener(e -> openGoldSilverDialog());
refreshGoldSilver();

String[] mfCols = {"Amount Invested", "Annual Rate (%)", "Years", "Maturity Value"};
mfModel = new DefaultTableModel(mfCols, 0);
JTable mfTable = new JTable(mfModel);
JPanel mfPanel = new JPanel(new BorderLayout());
mfPanel.add(new JScrollPane(mfTable), BorderLayout.CENTER);
JButton addMFBtn = new JButton("Add Mutual Fund");
mfPanel.add(addMFBtn, BorderLayout.SOUTH);
tabs.addTab("Mutual Funds", mfPanel);
addMFBtn.addActionListener(e -> openMutualFundDialog());
refreshMutualFunds();

    }

    // In FinanceManagerFullUI.java
private void refreshTransactions() {
    transactionModel.setRowCount(0); // Clear the table
    try {
        List<Transaction> txs = manager.getAllTransactions();
        for (Transaction t : txs) {
            // Add new data in the correct column order
            transactionModel.addRow(new Object[]{
                t.getId(),
                t.getDate(),
                t.getTimestamp(),
                t.getDay(),
                t.getPaymentMethod(),
                t.getCategory(),
                t.getType(),
                t.getPayee(),
                t.getDescription(),
                t.getAmount()
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "ErrorLoading: " + e.getMessage());
    }
}

    // In FinanceManagerFullUI.java
private void openTransactionDialog() {
    JDialog dlg = new JDialog(this, "New Transaction", true);
    // We have more fields, so we need more rows (8)
    dlg.setLayout(new GridLayout(8, 2, 5, 5)); // (rows, cols, hgap, vgap)

    // Create new UI components
    JTextField dateF = new JTextField("28-10-2025"); // (Example format)
    JTextField catF = new JTextField();

    // Dropdown for Income/Expense
    String[] types = {"Expense", "Income"};
    JComboBox<String> typeF = new JComboBox<>(types);

    // --- This is your requested dropdown ---
    String[] paymentMethods = {"UPI", "CASH", "CARD"};
    JComboBox<String> paymentMethodF = new JComboBox<>(paymentMethods);

    JTextField payeeF = new JTextField(); // New field for Payee
    JTextField amtF = new JTextField();
    JTextField descF = new JTextField();

    // Add components to the dialog in order
    dlg.add(new JLabel("Date (DD-MM-YYYY)"));  dlg.add(dateF);
    dlg.add(new JLabel("Category"));           dlg.add(catF);
    dlg.add(new JLabel("Type (Income/Expense)")); dlg.add(typeF);
    dlg.add(new JLabel("Payment Method"));     dlg.add(paymentMethodF); // New
    dlg.add(new JLabel("Payee"));               dlg.add(payeeF);        // New
    dlg.add(new JLabel("Amount"));              dlg.add(amtF);
    dlg.add(new JLabel("Description"));         dlg.add(descF);

    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    dlg.add(ok);
    dlg.add(cancel);

    // --- Update the Save button's logic ---
    ok.addActionListener(ev -> {
        try {
            // Get values from the new dropdowns and fields
            String paymentMethod = (String) paymentMethodF.getSelectedItem();
            String type = (String) typeF.getSelectedItem();
            String payee = payeeF.getText();

            // Use our new Transaction constructor
            Transaction t = new Transaction(
                    dateF.getText(),
                    catF.getText(),
                    type,
                    Double.parseDouble(amtF.getText()),
                    descF.getText(),
                    paymentMethod,
                    payee
            );

            manager.saveTransaction(t); // Save to DB
            dlg.dispose();
            refreshTransactions(); // Refresh the table
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
        }
    });

    cancel.addActionListener(_ -> dlg.dispose());

    dlg.pack(); // Sizes the dialog automatically
    dlg.setLocationRelativeTo(this);
    dlg.setVisible(true);
}
// In src/UI/FinanceManagerFullUI.java
private void refreshBankAccounts() {
    bankListModel.clear(); // Clear the list
    try {
        List<BankAccount> accounts = manager.getAllBankAccounts();
        for (BankAccount acc : accounts) {
            bankListModel.addElement(acc); // Add accounts to the list
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "ErrorLoading Bank Accounts: " + e.getMessage());
    }
}
// In src/UI/FinanceManagerFullUI.java
private void openBankAccountDialog() {
    JDialog dlg = new JDialog(this, "Add New Bank Account", true);
    dlg.setLayout(new BorderLayout(10, 10));

    // --- Panel for all fields ---
    JPanel fieldsPanel = new JPanel();
    fieldsPanel.setLayout(new GridLayout(0, 2, 5, 5));
    fieldsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    // --- Common Fields ---
    JTextField accNumF = new JTextField();
    JTextField holderF = new JTextField();
    JTextField bankF = new JTextField();
    JTextField ifscF = new JTextField();
    JTextField balanceF = new JTextField("0.0");

    // --- Type Selection ---
    String[] accTypes = {"Savings", "Current"};
    JComboBox<String> typeCombo = new JComboBox<>(accTypes);

    // --- Savings-Only Fields ---
    JLabel rateLabel = new JLabel("Interest Rate (%)");
    JTextField rateF = new JTextField("0.0");
    JLabel expenseLabel = new JLabel("Annual Expense (for interest calc)");
    JTextField expenseF = new JTextField("0.0");

    // --- Current-Only Fields ---
    JLabel subtypeLabel = new JLabel("Current Account Type");
    String[] subTypes = {"Salary", "Business"};
    JComboBox<String> subtypeCombo = new JComboBox<>(subTypes);

    JLabel companyLabel = new JLabel("Company Name");
    JTextField companyF = new JTextField();

    JLabel businessLabel = new JLabel("Business Name");
    JTextField businessF = new JTextField();

    // --- Add Common Fields First ---
    fieldsPanel.add(new JLabel("Account Type:"));
    fieldsPanel.add(typeCombo);
    fieldsPanel.add(new JLabel("Bank Name:"));
    fieldsPanel.add(bankF);
    fieldsPanel.add(new JLabel("Account Number:"));
    fieldsPanel.add(accNumF);
    fieldsPanel.add(new JLabel("Holder Name:"));
    fieldsPanel.add(holderF);
    fieldsPanel.add(new JLabel("IFSC Code:"));
    fieldsPanel.add(ifscF);
    fieldsPanel.add(new JLabel("Current Balance:"));
    fieldsPanel.add(balanceF);

    // --- Add Specific Fields ---
    fieldsPanel.add(rateLabel); fieldsPanel.add(rateF);
    fieldsPanel.add(expenseLabel); fieldsPanel.add(expenseF);

    fieldsPanel.add(subtypeLabel); fieldsPanel.add(subtypeCombo);
    fieldsPanel.add(companyLabel); fieldsPanel.add(companyF);
    fieldsPanel.add(businessLabel); fieldsPanel.add(businessF);

    // --- Logic to Show/Hide Fields ---
    typeCombo.addActionListener(e -> {
        String selected = (String) typeCombo.getSelectedItem();
        boolean isSavings = "Savings".equals(selected);

        rateLabel.setVisible(isSavings);
        rateF.setVisible(isSavings);
        expenseLabel.setVisible(isSavings);
        expenseF.setVisible(isSavings);

        subtypeLabel.setVisible(!isSavings);
        subtypeCombo.setVisible(!isSavings);

        // Trigger the sub-type combo's logic
        subtypeCombo.getActionListeners()[0].actionPerformed(null);
    });

    subtypeCombo.addActionListener(e -> {
        boolean isSavings = "Savings".equals(typeCombo.getSelectedItem());
        if (!isSavings) {
            String subSelected = (String) subtypeCombo.getSelectedItem();
            boolean isSalary = "Salary".equals(subSelected);
            companyLabel.setVisible(isSalary);
            companyF.setVisible(isSalary);
            businessLabel.setVisible(!isSalary);
            businessF.setVisible(!isSalary);
        } else {
            companyLabel.setVisible(false);
            companyF.setVisible(false);
            businessLabel.setVisible(false);
            businessF.setVisible(false);
        }
    });

    // --- Buttons Panel ---
    JPanel buttonPanel = new JPanel();
    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    buttonPanel.add(ok);
    buttonPanel.add(cancel);

    dlg.add(fieldsPanel, BorderLayout.CENTER);
    dlg.add(buttonPanel, BorderLayout.SOUTH);

    // --- Save Logic ---
    ok.addActionListener(_ -> {
        try {
            // Get all values, set 0 or "" for non-applicable ones
            String accountType = (String) typeCombo.getSelectedItem();
            double balance = Double.parseDouble(balanceF.getText());

            double interestRate = 0.0;
            double annualExpense = 0.0;
            if ("Savings".equals(accountType)) {
                interestRate = Double.parseDouble(rateF.getText());
                annualExpense = Double.parseDouble(expenseF.getText());
            }

            String accountSubtype = "";
            String companyName = "";
            String businessName = "";
            if ("Current".equals(accountType)) {
                accountSubtype = (String) subtypeCombo.getSelectedItem();
                if ("Salary".equals(accountSubtype)) {
                    companyName = companyF.getText();
                } else {
                    businessName = businessF.getText();
                }
            }

            // Use the new BankAccount constructor
            BankAccount ba = new BankAccount(
                accNumF.getText(), holderF.getText(), bankF.getText(), ifscF.getText(), balance,
                accountType, interestRate, annualExpense,
                accountSubtype, companyName, businessName
            );

            manager.saveBankAccount(ba);
            dlg.dispose();
            refreshBankAccounts(); // Refresh the new list

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
        }
    });

    cancel.addActionListener(ev -> dlg.dispose());

    // --- Initialize View ---
    typeCombo.setSelectedItem("Savings");

    dlg.pack();
    dlg.setLocationRelativeTo(this);
    dlg.setVisible(true);
}
// Also inside your FinanceManagerFullUI class:
private void openFixedDepositDialog() {
    JDialog dlg = new JDialog(this, "Add Fixed Deposit", true);
    dlg.setLayout(new GridLayout(7, 2));
    JTextField accNumF = new JTextField();
    JTextField principalF = new JTextField();
    JTextField rateF = new JTextField();
    JTextField tenureF = new JTextField();
    JTextField startDateF = new JTextField();
    JTextField holderF = new JTextField();
    dlg.add(new JLabel("Account Number")); dlg.add(accNumF);
    dlg.add(new JLabel("Principal")); dlg.add(principalF);
    dlg.add(new JLabel("Rate (%)")); dlg.add(rateF);
    dlg.add(new JLabel("Tenure (Months)")); dlg.add(tenureF);
    dlg.add(new JLabel("Start Date (DD-MM-YYYY)")); dlg.add(startDateF);
    dlg.add(new JLabel("Holder Name")); dlg.add(holderF);
    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    dlg.add(ok); dlg.add(cancel);
    ok.addActionListener(ev -> {
        try {
            FixedDeposit fd = new FixedDeposit(
                accNumF.getText(), Double.parseDouble(principalF.getText()),
                Double.parseDouble(rateF.getText()), Integer.parseInt(tenureF.getText()),
                startDateF.getText(), holderF.getText()
            );
            manager.saveFixedDeposit(fd);
            dlg.dispose();
            refreshFixedDeposits();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
        }
    });
    cancel.addActionListener(ev -> dlg.dispose());
    dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
}

private void refreshFixedDeposits() {
    fdModel.setRowCount(0);
    try {
        for (FixedDeposit fd : manager.getAllFixedDeposits())
            fdModel.addRow(fd.toObjectArray());
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading FDs: " + e.getMessage());
    }
}

private void openRecurringDepositDialog() {
    JDialog dlg = new JDialog(this, "Add Recurring Deposit", true);
    dlg.setLayout(new GridLayout(7, 2));
    JTextField accNumF = new JTextField();
    JTextField amountF = new JTextField();
    JTextField rateF = new JTextField();
    JTextField tenureF = new JTextField();
    JTextField startDateF = new JTextField();
    JTextField holderF = new JTextField();
    dlg.add(new JLabel("Account Number")); dlg.add(accNumF);
    dlg.add(new JLabel("Monthly Amount")); dlg.add(amountF);
    dlg.add(new JLabel("Rate (%)")); dlg.add(rateF);
    dlg.add(new JLabel("Tenure (Months)")); dlg.add(tenureF);
    dlg.add(new JLabel("Start Date (DD-MM-YYYY)")); dlg.add(startDateF);
    dlg.add(new JLabel("Holder Name")); dlg.add(holderF);
    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    dlg.add(ok); dlg.add(cancel);
    ok.addActionListener(ev -> {
        try {
            RecurringDeposit rd = new RecurringDeposit(
                accNumF.getText(), Double.parseDouble(amountF.getText()),
                Double.parseDouble(rateF.getText()), Integer.parseInt(tenureF.getText()),
                startDateF.getText(), holderF.getText()
            );
            manager.saveRecurringDeposit(rd);
            dlg.dispose();
            refreshRecurringDeposits();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
        }
    });
    cancel.addActionListener(ev -> dlg.dispose());
    dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
}

private void refreshRecurringDeposits() {
    rdModel.setRowCount(0);
    try {
        for (RecurringDeposit rd : manager.getAllRecurringDeposits())
            rdModel.addRow(rd.toObjectArray());
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading RDs: " + e.getMessage());
    }
}
private void openCreditCardDialog() {
    JDialog dlg = new JDialog(this, "Add Credit Card", true);
    dlg.setLayout(new GridLayout(6,2));
    JTextField nameF = new JTextField(), limitF = new JTextField(), expF = new JTextField(), payF = new JTextField(), daysF = new JTextField();
    dlg.add(new JLabel("Card Name")); dlg.add(nameF);
    dlg.add(new JLabel("Credit Limit")); dlg.add(limitF);
    dlg.add(new JLabel("Expenses")); dlg.add(expF);
    dlg.add(new JLabel("Amount To Pay")); dlg.add(payF);
    dlg.add(new JLabel("Days Left")); dlg.add(daysF);
    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    dlg.add(ok); dlg.add(cancel);
    ok.addActionListener(ev -> {
        try {
            CreditCard cc = new CreditCard(
                nameF.getText(),
                Double.parseDouble(limitF.getText()),
                Double.parseDouble(expF.getText()),
                Double.parseDouble(payF.getText()),
                Integer.parseInt(daysF.getText())
            );
            manager.saveCreditCard(cc);
            dlg.dispose();
            refreshCreditCards();
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
        }
    });
    cancel.addActionListener(ev -> dlg.dispose());
    dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
}

private void refreshCreditCards() {
    ccModel.setRowCount(0);
    try {
        for (CreditCard cc : manager.getAllCreditCards())
            ccModel.addRow(cc.toObjectArray());
    } catch(Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading Credit Cards: " + e.getMessage());
    }
}
private void refreshGoldSilver() {
    gssModel.setRowCount(0);
    try {
        for (GoldSilverInvestment gs : manager.getAllGoldSilverInvestments()) {
            double value = gs.getWeight() * gs.getPricePerGram();
            gssModel.addRow(new Object[]{gs.getMetalType(), gs.getWeight(), gs.getPricePerGram(), value});
        }
    } catch(Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading: " + e.getMessage());
    }
}

private void openGoldSilverDialog() {
    JDialog dlg = new JDialog(this, "Add Gold/Silver Investment", true);
    dlg.setLayout(new GridLayout(4,2));
    JTextField typeF = new JTextField();
    JTextField weightF = new JTextField();
    JTextField priceF = new JTextField();
    dlg.add(new JLabel("Metal Type (Gold/Silver):")); dlg.add(typeF);
    dlg.add(new JLabel("Weight (g):")); dlg.add(weightF);
    dlg.add(new JLabel("Price/g:")); dlg.add(priceF);
    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    dlg.add(ok); dlg.add(cancel);
    ok.addActionListener(ev -> {
        try {
            GoldSilverInvestment gs = new GoldSilverInvestment(
                typeF.getText(), Double.parseDouble(weightF.getText()), Double.parseDouble(priceF.getText())
            );
            manager.saveGoldSilverInvestment(gs);
            dlg.dispose();
            refreshGoldSilver();
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
        }
    });
    cancel.addActionListener(ev -> dlg.dispose());
    dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
}
private void refreshMutualFunds() {
    mfModel.setRowCount(0);
    try {
        for (MutualFund mf : manager.getAllMutualFunds()) {
            mfModel.addRow(new Object[]{
                mf.getAmount(), mf.getAnnualRate(), mf.getYears(),
                mf.getMaturityAmount() // Calculation shown in table
            });
        }
    } catch(Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading: " + e.getMessage());
    }
}

private void openMutualFundDialog() {
    JDialog dlg = new JDialog(this, "Add Mutual Fund Investment", true);
    dlg.setLayout(new GridLayout(4,2));
    JTextField amtF = new JTextField();
    JTextField rateF = new JTextField();
    JTextField yearsF = new JTextField();
    dlg.add(new JLabel("Amount:")); dlg.add(amtF);
    dlg.add(new JLabel("Annual Rate (%):")); dlg.add(rateF);
    dlg.add(new JLabel("Years:")); dlg.add(yearsF);
    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    dlg.add(ok); dlg.add(cancel);
    ok.addActionListener(ev -> {
        try {
            MutualFund mf = new MutualFund(
                Double.parseDouble(amtF.getText()), Double.parseDouble(rateF.getText()), Integer.parseInt(yearsF.getText())
            );
            manager.saveMutualFund(mf);
            dlg.dispose();
            refreshMutualFunds();
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
        }
    });
    cancel.addActionListener(ev -> dlg.dispose());
    dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
}

/**
 * This method builds the beautiful detail panel on the right side
 * based on which account is selected.
 */
private void showAccountDetails(BankAccount acc) {
    bankDetailPanel.removeAll(); // Clear old details

    // Use a 1-column grid to stack details nicely
    JPanel detailGrid = new JPanel(new GridLayout(0, 1, 5, 10));

    // --- Title (Bank Name) ---
    JLabel title = new JLabel(acc.getBankName());
    title.setFont(new Font("Arial", Font.BOLD, 24));
    detailGrid.add(title);

    // --- Sub-Title (Account Type) ---
    String accType = acc.getAccountType();
    if ("Current".equals(accType)) {
        accType += " (" + acc.getAccountSubtype() + ")";
    }
    JLabel subTitle = new JLabel(accType + " Account");
    subTitle.setFont(new Font("Arial", Font.ITALIC, 18));
    detailGrid.add(subTitle);

    // --- Balance ---
    JLabel balanceLabel = new JLabel(String.format("Balance: ₹%.2f", acc.getBalance()));
    balanceLabel.setFont(new Font("Arial", Font.BOLD, 20));
    detailGrid.add(balanceLabel);

    // --- Common Details ---
    detailGrid.add(new JSeparator());
    detailGrid.add(new JLabel("Holder: " + acc.getHolderName()));
    detailGrid.add(new JLabel("Account #: " + acc.getAccountNumber()));
    detailGrid.add(new JLabel("IFSC: " + acc.getIfscCode()));

    // --- Specific Details ---
    if ("Savings".equals(acc.getAccountType())) {
        detailGrid.add(new JSeparator());
        detailGrid.add(new JLabel("Interest Rate: " + acc.getInterestRate() + "%"));
        detailGrid.add(new JLabel("Annual Expenses: ₹" + acc.getAnnualExpense()));

        // Calculated Field
        double interestAmount = acc.getEstimatedAnnualInterest();
        JLabel calcLabel = new JLabel(String.format("Estimated Annual Interest: ₹%.2f", interestAmount));
        calcLabel.setFont(new Font("Arial", Font.BOLD, 14));
        detailGrid.add(calcLabel);

    } else if ("Current".equals(acc.getAccountType())) {
        detailGrid.add(new JSeparator());
        if ("Salary".equals(acc.getAccountSubtype())) {
            detailGrid.add(new JLabel("Company: " + acc.getCompanyName()));
        } else if ("Business".equals(acc.getAccountSubtype())) {
            detailGrid.add(new JLabel("Business: " + acc.getBusinessName()));
        }
    }

    bankDetailPanel.add(detailGrid, BorderLayout.NORTH);

    // Refresh the panel
    bankDetailPanel.revalidate();
    bankDetailPanel.repaint();
}
// Add this new method to src/UI/FinanceManagerFullUI.java

private void deleteSelectedAccount() {
    // 1. Get the selected account from the list
    BankAccount selected = bankAccountList.getSelectedValue();

    // 2. Check if anything is selected
    if (selected == null) {
        JOptionPane.showMessageDialog(this, "Please select an account to delete.", "No Account Selected", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // 3. Show a confirmation dialog
    int choice = JOptionPane.showConfirmDialog(
        this, 
        "Are you sure you want to delete the account:\n" + selected.toString() + "?", 
        "Confirm Delete", 
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );

    // 4. If they clicked "Yes", proceed with deletion
    if (choice == JOptionPane.YES_OPTION) {
        try {
            // Call the new method in FinanceManager
            manager.deleteBankAccount(selected.getId());

            // Refresh the list
            refreshBankAccounts();

            // Clear the detail panel
            bankDetailPanel.removeAll();
            bankDetailPanel.revalidate();
            bankDetailPanel.repaint();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting account: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FinanceManagerFullUI().setVisible(true));
    }

}
