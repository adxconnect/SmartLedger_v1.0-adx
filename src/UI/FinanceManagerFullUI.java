package src.UI;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// Removed duplicate RecycleBinDialog import
import src.UI.DepositRecycleBinDialog; // Import for Deposit Recycle Bin

import src.BankAccount;
import src.Card;
import src.FinanceManager;
import src.GoldSilverInvestment;
import src.MutualFund;
// Removed src.RecycleBinDialog import (using specific ones now)
import src.Transaction;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import src.Deposit; // Import for Deposit class

// Added missing imports for dialogs
import src.UI.AddEditDepositDialog;
import src.UI.GullakDialog;
import src.UI.RecycleBinDialog; // Transaction Recycle Bin Dialog


public class FinanceManagerFullUI extends JFrame {
    private FinanceManager manager;

    // --- Transaction Tab Variables ---
    private JTabbedPane monthTabs;
    private JComboBox<String> yearComboBox;
    private JButton deleteMonthButton;

    // --- Bank Account Tab Variables ---
    private JList<BankAccount> bankAccountList;
    private DefaultListModel<BankAccount> bankListModel;
    private JPanel bankDetailPanel;

    // --- Deposits Tab Variables ---
    private JList<Deposit> depositList;
    private DefaultListModel<Deposit> depositListModel;
    private JPanel depositDetailPanel;

    // --- Other Tab Variables (Models) ---
    private DefaultTableModel ccModel, gssModel, mfModel;
    // Removed fdModel, rdModel


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

        // =========================================================
        // ===         TRANSACTIONS PANEL                      ===
        // =========================================================
        JPanel tPanel = new JPanel(new BorderLayout(5, 5));
        tPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel tTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        yearComboBox = new JComboBox<>();
        tTopPanel.add(new JLabel("Select Year:"));
        tTopPanel.add(yearComboBox);
        JButton deleteYearButton = new JButton("Delete All of Selected Year");
        tTopPanel.add(deleteYearButton);
        deleteMonthButton = new JButton("Delete Selected Month");
        deleteMonthButton.setEnabled(false);
        tTopPanel.add(deleteMonthButton);
        tPanel.add(tTopPanel, BorderLayout.NORTH);
        monthTabs = new JTabbedPane();
        tPanel.add(monthTabs, BorderLayout.CENTER);
        JPanel tBottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addTxnBtn = new JButton("Add Transaction");
        JButton deleteTxnBtn = new JButton("Delete Selected Transaction");
        JButton recycleBinBtn = new JButton("Recycle Bin");
        tBottomPanel.add(addTxnBtn);
        tBottomPanel.add(deleteTxnBtn);
        tBottomPanel.add(recycleBinBtn);
        tPanel.add(tBottomPanel, BorderLayout.SOUTH);
        tabs.addTab("Transactions", tPanel);
        // Action Listeners
        recycleBinBtn.addActionListener(e -> openRecycleBin()); // Connect button
        yearComboBox.addActionListener(e -> refreshTransactions());
        addTxnBtn.addActionListener(e -> openTransactionDialog());
        deleteTxnBtn.addActionListener(e -> deleteSelectedTransaction());
        deleteMonthButton.addActionListener(e -> deleteSelectedMonth());
        deleteYearButton.addActionListener(e -> deleteSelectedYear());
        monthTabs.addChangeListener(e -> deleteMonthButton.setEnabled(monthTabs.getTabCount() > 0));
        // Initial Load
        loadYearFilter();
        refreshTransactions();


        // =========================================================
        // ===         BANK ACCOUNTS PANEL                     ===
        // =========================================================
        JPanel bPanel = new JPanel(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        bankListModel = new DefaultListModel<>();
        bankAccountList = new JList<>(bankListModel);
        bankAccountList.setFont(new Font("Arial", Font.PLAIN, 14));
        bankAccountList.setBorder(new EmptyBorder(5, 5, 5, 5));
        bankDetailPanel = new JPanel(new BorderLayout());
        bankDetailPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        splitPane.setLeftComponent(new JScrollPane(bankAccountList));
        splitPane.setRightComponent(bankDetailPanel);
        bPanel.add(splitPane, BorderLayout.CENTER);
        JPanel bankButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addBankBtn = new JButton("Add New Account");
        addBankBtn.addActionListener(e -> openBankAccountDialog());
        JButton deleteBankBtn = new JButton("Delete Selected Account");
        deleteBankBtn.addActionListener(e -> deleteSelectedAccount());
        bankButtonPanel.add(addBankBtn);
        bankButtonPanel.add(deleteBankBtn);
        bPanel.add(bankButtonPanel, BorderLayout.SOUTH);
        tabs.addTab("Bank Accounts", bPanel);
        bankAccountList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                BankAccount selected = bankAccountList.getSelectedValue();
                if (selected != null) showAccountDetails(selected);
            }
        });
        refreshBankAccounts();


        // =========================================================
        // ===         NEW DEPOSITS PANEL                      ===
        // =========================================================
        JPanel dPanel = new JPanel(new BorderLayout());
        JSplitPane depositSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        depositSplitPane.setDividerLocation(200);
        depositListModel = new DefaultListModel<>();
        depositList = new JList<>(depositListModel);
        depositList.setFont(new Font("Arial", Font.PLAIN, 14));
        depositList.setBorder(new EmptyBorder(5, 5, 5, 5));
        depositSplitPane.setLeftComponent(new JScrollPane(depositList));
        depositDetailPanel = new JPanel(new BorderLayout());
        depositDetailPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        depositDetailPanel.add(new JLabel("Select a deposit to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
        depositSplitPane.setRightComponent(depositDetailPanel);
        dPanel.add(depositSplitPane, BorderLayout.CENTER);
        JPanel depositButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addDepositBtn = new JButton("Add New Deposit");
        JButton deleteDepositBtn = new JButton("Delete Selected Deposit");
        JButton depositRecycleBinBtn = new JButton("Deposit Recycle Bin");
        depositButtonPanel.add(addDepositBtn);
        depositButtonPanel.add(deleteDepositBtn);
        depositButtonPanel.add(depositRecycleBinBtn);
        dPanel.add(depositButtonPanel, BorderLayout.SOUTH);
        tabs.addTab("Deposits", dPanel);
        // Action Listeners
        depositList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Deposit selected = depositList.getSelectedValue();
                showDepositDetails(selected);
            }
        });
        addDepositBtn.addActionListener(e -> openAddEditDepositDialog(null));
        deleteDepositBtn.addActionListener(e -> deleteSelectedDeposit());
        depositRecycleBinBtn.addActionListener(e -> openDepositRecycleBin());
        // Initial Load
        refreshDeposits();


        // =========================================================
        // ===         OTHER PANELS (CREDIT CARDS etc.)          ===
        // =========================================================
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
    } // End of Constructor

    // --- TRANSACTION METHODS ---

    private void loadYearFilter() {
        try {
            List<String> years = manager.getAvailableYears();
            String previouslySelected = (String) yearComboBox.getSelectedItem();
            yearComboBox.removeAllItems();
            for (String year : years) yearComboBox.addItem(year);
            if (previouslySelected != null && years.contains(previouslySelected)) {
                yearComboBox.setSelectedItem(previouslySelected);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading years: " + e.getMessage());
        }
    }

    private void refreshTransactions() {
        monthTabs.removeAll();
        String selectedYear = (String) yearComboBox.getSelectedItem();
        if (selectedYear == null) return;
        try {
            Map<String, List<Transaction>> groupedData = manager.getTransactionsGroupedByMonth(selectedYear);
            String[] tcols = {"S.No.", "Date", "Timestamp", "Day", "Payment Method", "Category", "Type", "Payee", "Description", "Amount"};
            for (String monthYear : groupedData.keySet()) {
                DefaultTableModel monthModel = new DefaultTableModel(tcols, 0);
                List<Transaction> txs = groupedData.get(monthYear);
                for (Transaction t : txs) {
                    monthModel.addRow(new Object[]{t.getId(), t.getDate(), t.getTimestamp(), t.getDay(), t.getPaymentMethod(), t.getCategory(), t.getType(), t.getPayee(), t.getDescription(), t.getAmount()});
                }
                JTable monthTable = new JTable(monthModel);
                monthTable.putClientProperty("monthYear", monthYear);
                JScrollPane scrollPane = new JScrollPane(monthTable);
                String tabTitle = getMonthName(monthYear);
                monthTabs.addTab(tabTitle, scrollPane);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
        }
        deleteMonthButton.setEnabled(monthTabs.getTabCount() > 0);
    }

    private void deleteSelectedTransaction() {
        JScrollPane currentScrollPane = (JScrollPane) monthTabs.getSelectedComponent();
        if (currentScrollPane == null) {
            JOptionPane.showMessageDialog(this, "Please select a transaction tab.", "No Tab", JOptionPane.WARNING_MESSAGE); return;
        }
        JTable currentTable = (JTable) currentScrollPane.getViewport().getView();
        int selectedRow = currentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete.", "No Transaction", JOptionPane.WARNING_MESSAGE); return;
        }
        int transactionId = (int) currentTable.getValueAt(selectedRow, 0);
        String desc = String.valueOf(currentTable.getValueAt(selectedRow, 8)); // Use String.valueOf for safety
        int choice = JOptionPane.showConfirmDialog(this, "Move this transaction to the recycle bin?\nID: " + transactionId + " (" + desc + ")", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.deleteTransactionById(transactionId);
                refreshTransactions();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting: " + e.getMessage());
            }
        }
    }

    private void deleteSelectedMonth() {
        int selectedIndex = monthTabs.getSelectedIndex();
        if (selectedIndex == -1) {
             JOptionPane.showMessageDialog(this, "No month is selected.", "Error", JOptionPane.ERROR_MESSAGE); return;
        }
        JScrollPane scrollPane = (JScrollPane) monthTabs.getComponentAt(selectedIndex);
        JTable table = (JTable) scrollPane.getViewport().getView();
        String monthYear = (String) table.getClientProperty("monthYear");
        String tabTitle = monthTabs.getTitleAt(selectedIndex);
        int choice = JOptionPane.showConfirmDialog(this, "Move ALL transactions for " + tabTitle + " to the recycle bin?", "Confirm Month Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.deleteTransactionsByMonth(monthYear);
                refreshTransactions();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting month: " + e.getMessage());
            }
        }
    }

    private void deleteSelectedYear() {
        String selectedYear = (String) yearComboBox.getSelectedItem();
        if (selectedYear == null || selectedYear.equals("All Years")) {
             JOptionPane.showMessageDialog(this, "Please select a specific year to delete.", "Invalid Year", JOptionPane.WARNING_MESSAGE); return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Move ALL transactions for the entire year " + selectedYear + " to the recycle bin?\nThis action cannot be undone from this screen.", "Confirm Year Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.deleteTransactionsByYear(selectedYear);
                loadYearFilter();
                refreshTransactions();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting year: " + e.getMessage());
            }
        }
    }

    private String getMonthName(String monthYear) {
        try {
            String[] parts = monthYear.split("-");
            int month = Integer.parseInt(parts[0]);
            String year = parts[1];
            java.time.LocalDate date = java.time.LocalDate.of(2000, month, 1);
            String monthName = date.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
            return monthName + " " + year;
        } catch (Exception e) {
            return monthYear;
        }
    }

    private void openTransactionDialog() {
        JDialog dlg = new JDialog(this, "New Transaction", true);
        dlg.setLayout(new GridLayout(8, 2, 5, 5));
        JTextField dateF = new JTextField(new java.text.SimpleDateFormat("dd-MM-yyyy").format(new java.util.Date()));
        JTextField catF = new JTextField();
        String[] types = {"Expense", "Income"};
        JComboBox<String> typeF = new JComboBox<>(types);
        String[] paymentMethods = {"UPI", "CASH", "CARD"};
        JComboBox<String> paymentMethodF = new JComboBox<>(paymentMethods);
        JTextField payeeF = new JTextField();
        JTextField amtF = new JTextField();
        JTextField descF = new JTextField();
        dlg.add(new JLabel("Date (DD-MM-YYYY)")); dlg.add(dateF);
        dlg.add(new JLabel("Category")); dlg.add(catF);
        dlg.add(new JLabel("Type (Income/Expense)")); dlg.add(typeF);
        dlg.add(new JLabel("Payment Method")); dlg.add(paymentMethodF);
        dlg.add(new JLabel("Payee")); dlg.add(payeeF);
        dlg.add(new JLabel("Amount")); dlg.add(amtF);
        dlg.add(new JLabel("Description")); dlg.add(descF);
        JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
        dlg.add(ok); dlg.add(cancel);
        ok.addActionListener(ev -> {
            try {
                String paymentMethod = (String) paymentMethodF.getSelectedItem();
                String type = (String) typeF.getSelectedItem();
                String payee = payeeF.getText();
                Transaction t = new Transaction(
                    dateF.getText(), catF.getText(), type,
                    Double.parseDouble(amtF.getText()), descF.getText(),
                    paymentMethod, payee
                );
                manager.saveTransaction(t);
                dlg.dispose();
                loadYearFilter();
                refreshTransactions();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
            }
        });
        cancel.addActionListener(_ -> dlg.dispose());
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // --- Transaction Recycle Bin Methods ---
    private void openRecycleBin() {
        RecycleBinDialog dialog = new RecycleBinDialog(this, manager, this);
        dialog.setVisible(true);
    }

    public void refreshAfterRestore() { // Callback from Transaction Recycle Bin
        System.out.println("Refreshing transactions list after restore...");
        loadYearFilter();
        refreshTransactions();
    }


    // --- BANK ACCOUNT METHODS ---
    private void showAccountDetails(BankAccount acc) {
        bankDetailPanel.removeAll();
        if(acc == null) {
            bankDetailPanel.add(new JLabel("Select an account.", SwingConstants.CENTER));
        } else {
            JPanel detailGrid = new JPanel(new GridLayout(0, 1, 5, 10));
            JLabel title = new JLabel(acc.getBankName());
            title.setFont(new Font("Arial", Font.BOLD, 24));
            detailGrid.add(title);
            String accType = acc.getAccountType();
            if ("Current".equals(accType)) accType += " (" + acc.getAccountSubtype() + ")";
            JLabel subTitle = new JLabel(accType + " Account");
            subTitle.setFont(new Font("Arial", Font.ITALIC, 18));
            detailGrid.add(subTitle);
            JLabel balanceLabel = new JLabel(String.format("Balance: ₹%.2f", acc.getBalance()));
            balanceLabel.setFont(new Font("Arial", Font.BOLD, 20));
            detailGrid.add(balanceLabel);
            detailGrid.add(new JSeparator());
            detailGrid.add(new JLabel("Holder: " + acc.getHolderName()));
            detailGrid.add(new JLabel("Account #: " + acc.getAccountNumber()));
            detailGrid.add(new JLabel("IFSC: " + acc.getIfscCode()));
            if ("Savings".equals(acc.getAccountType())) {
                detailGrid.add(new JSeparator());
                detailGrid.add(new JLabel("Interest Rate: " + acc.getInterestRate() + "%"));
                detailGrid.add(new JLabel("Annual Expenses: ₹" + acc.getAnnualExpense()));
                double interestAmount = acc.getEstimatedAnnualInterest();
                JLabel calcLabel = new JLabel(String.format("Estimated Annual Interest: ₹%.2f", interestAmount));
                calcLabel.setFont(new Font("Arial", Font.BOLD, 14));
                detailGrid.add(calcLabel);
            } else if ("Current".equals(acc.getAccountType())) {
                 detailGrid.add(new JSeparator());
                 if ("Salary".equals(acc.getAccountSubtype())) detailGrid.add(new JLabel("Company: " + acc.getCompanyName()));
                 else if ("Business".equals(acc.getAccountSubtype())) detailGrid.add(new JLabel("Business: " + acc.getBusinessName()));
            }
            bankDetailPanel.add(detailGrid, BorderLayout.NORTH);
        }
        bankDetailPanel.revalidate();
        bankDetailPanel.repaint();
    }

    private void refreshBankAccounts() {
        bankListModel.clear();
        try {
            List<BankAccount> accounts = manager.getAllBankAccounts();
            for (BankAccount acc : accounts) bankListModel.addElement(acc);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "ErrorLoading Bank Accounts: " + e.getMessage());
        }
    }

    private void openBankAccountDialog() {
        // Assume AddEditBankAccountDialog exists and handles logic
        // For brevity, keeping placeholder logic here, replace if needed
        JOptionPane.showMessageDialog(this, "Bank Account Dialog Placeholder");
         // Example call if you have the dialog:
         // AddEditBankAccountDialog dialog = new AddEditBankAccountDialog(this, manager, null); // null for adding
         // dialog.setVisible(true);
         // refreshBankAccounts();
    }

    private void deleteSelectedAccount() {
        BankAccount selected = bankAccountList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an account to delete.", "No Account Selected", JOptionPane.WARNING_MESSAGE); return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the account:\n" + selected.toString() + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.deleteBankAccount(selected.getId());
                refreshBankAccounts();
                showAccountDetails(null); // Clear details panel
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting account: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // --- DEPOSIT METHODS --- (These were incorrectly nested before)

    public void refreshDeposits() {
        depositListModel.clear();
        depositDetailPanel.removeAll();
        depositDetailPanel.add(new JLabel("Select a deposit to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
        depositDetailPanel.revalidate();
        depositDetailPanel.repaint();
        try {
            List<Deposit> deposits = manager.getAllDeposits();
            for (Deposit d : deposits) depositListModel.addElement(d);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading deposits: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDepositDetails(Deposit deposit) {
        depositDetailPanel.removeAll();
        if (deposit == null) {
            depositDetailPanel.add(new JLabel("Select a deposit to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
            depositDetailPanel.revalidate(); depositDetailPanel.repaint(); return;
        }
        JPanel detailGrid = new JPanel(new GridLayout(0, 1, 5, 10));
        String titleText = deposit.getDepositType() + ": " + (deposit.getHolderName() != null && !deposit.getHolderName().isEmpty() ? deposit.getHolderName() : (deposit.getDescription() != null && !deposit.getDescription().isEmpty() ? deposit.getDescription() : "Deposit #" + deposit.getId()));
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        detailGrid.add(title);
        detailGrid.add(new JSeparator());
        if (deposit.getGoal() != null && !deposit.getGoal().isEmpty()) detailGrid.add(new JLabel("Goal: " + deposit.getGoal()));
        if (deposit.getDescription() != null && !deposit.getDescription().isEmpty()) detailGrid.add(new JLabel("Description: " + deposit.getDescription()));
        detailGrid.add(new JLabel("Added On: " + (deposit.getCreationDate() != null ? deposit.getCreationDate().substring(0, 10) : "N/A")));
        switch(deposit.getDepositType()) {
            case "FD":
                detailGrid.add(new JSeparator());
                detailGrid.add(new JLabel(String.format("Principal: ₹%.2f", deposit.getPrincipalAmount())));
                detailGrid.add(new JLabel(String.format("Rate: %.2f%%", deposit.getInterestRate())));
                detailGrid.add(new JLabel("Tenure: " + deposit.getTenure() + " " + deposit.getTenureUnit()));
                detailGrid.add(new JLabel("Maturity Date: " + deposit.calculateMaturityDate()));
                detailGrid.add(new JLabel(String.format("Est. Maturity Value: ₹%.2f", deposit.calculateFDMaturityAmount())));
                break;
            case "RD":
                detailGrid.add(new JSeparator());
                detailGrid.add(new JLabel(String.format("Monthly Deposit: ₹%.2f", deposit.getMonthlyAmount())));
                detailGrid.add(new JLabel(String.format("Rate: %.2f%%", deposit.getInterestRate())));
                detailGrid.add(new JLabel("Tenure: " + deposit.getTenure() + " " + deposit.getTenureUnit()));
                detailGrid.add(new JLabel("Maturity Date: " + deposit.calculateMaturityDate()));
                detailGrid.add(new JLabel(String.format("Total Principal: ₹%.2f", deposit.getMonthlyAmount() * ("Months".equals(deposit.getTenureUnit()) ? deposit.getTenure() : 0))));
                break;
            case "Gullak":
                detailGrid.add(new JSeparator());
                detailGrid.add(new JLabel(String.format("Current Total: ₹%.2f", deposit.getCurrentTotal())));
                detailGrid.add(new JLabel(String.format("Amount Due (Withdrawn): ₹%.2f", deposit.getGullakDueAmount())));
                detailGrid.add(new JLabel("Last Updated: " + (deposit.getLastUpdated() != null ? deposit.getLastUpdated() : "Never")));
                break;
        }
        JButton editButton = new JButton("View / Edit Details");
        editButton.addActionListener(e -> {
            if ("Gullak".equals(deposit.getDepositType())) openGullakDialog(deposit);
            else openAddEditDepositDialog(deposit);
        });
        depositDetailPanel.add(detailGrid, BorderLayout.NORTH);
        depositDetailPanel.add(editButton, BorderLayout.SOUTH);
        depositDetailPanel.revalidate(); depositDetailPanel.repaint();
    }

    private void openAddEditDepositDialog(Deposit depositToEdit) {
        AddEditDepositDialog dialog = new AddEditDepositDialog(this, manager, depositToEdit, this);
        dialog.setVisible(true);
        // Refresh handled by dialog on save
    }

     private void openGullakDialog(Deposit gullakDeposit) {
         GullakDialog dialog = new GullakDialog(this, manager, gullakDeposit, this);
         dialog.setVisible(true);
         refreshDeposits(); // Refresh after Gullak changes
     }

    private void deleteSelectedDeposit() {
        Deposit selected = depositList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a deposit to delete.", "No Selection", JOptionPane.WARNING_MESSAGE); return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Move this deposit to the recycle bin?\n" + selected.toString(), "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.deleteDepositById(selected.getId());
                refreshDeposits();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting deposit: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openDepositRecycleBin() {
        DepositRecycleBinDialog dialog = new DepositRecycleBinDialog(this, manager, this);
        dialog.setVisible(true);
    }

    public void refreshAfterDepositRestore() { // Callback from Deposit Recycle Bin
        System.out.println("Refreshing deposits list after restore...");
        refreshDeposits();
    }


    // --- OTHER REFRESH/DIALOG METHODS ---

    private void openCreditCardDialog() { /* ... Keep your existing implementation ... */ }
    private void refreshCreditCards() { /* ... Keep your existing implementation ... */ }
    private void openGoldSilverDialog() { /* ... Keep your existing implementation ... */ }
    private void refreshGoldSilver() { /* ... Keep your existing implementation ... */ }
    private void openMutualFundDialog() { /* ... Keep your existing implementation ... */ }

    // --- refreshMutualFunds --- (Ensure methods below are OUTSIDE this one)
    private void refreshMutualFunds() {
        mfModel.setRowCount(0);
        try {
            for (MutualFund mf : manager.getAllMutualFunds()) {
                mfModel.addRow(new Object[]{
                    mf.getAmount(), mf.getAnnualRate(), mf.getYears(),
                    mf.getMaturityAmount()
                });
            }
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading: " + e.getMessage());
        }
    } // <<<--- MAKE SURE THIS BRACE '}' IS HERE, CLOSING refreshMutualFunds


    // --- Main Method ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FinanceManagerFullUI().setVisible(true));
    }

} // End of FinanceManagerFullUI class