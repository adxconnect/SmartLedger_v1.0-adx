package src.UI;
// We'll also add a placeholder for the dialog we will create
// import src.UI.AddEditTaxProfileDialog;
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
import src.UI.AddEditInvestmentDialog;
import src.BankAccount;
import src.Card;
import src.FinanceManager;
import src.Investment;
// Removed src.RecycleBinDialog import (using specific ones now)
import src.Transaction;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;

import src.UI.InvestmentRecycleBinDialog;
import src.Deposit; // Import for Deposit class
import src.UI.AddEditTaxProfileDialog;
// Added missing imports for dialogs
import src.UI.AddEditDepositDialog;
import src.UI.GullakDialog;
import src.UI.RecycleBinDialog; // Transaction Recycle Bin Dialog
import src.UI.ShowOtpDialog;
import src.UI.EnterOtpDialog;
import src.UI.SensitiveCardDetailsDialog;
import src.TaxProfile;
import src.Loan;
// We'll also add placeholders for the dialogs
import src.UI.AddEditLoanDialog;
import src.UI.LoanRecycleBinDialog;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

// Imports for Excel (Apache POI)
// Removed Excel (Apache POI) imports as XLSX export is no longer supported
// Imports for PDF (iText)
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;

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

    // --- Investment Tab Variables ---
    private JList<Investment> investmentList;
    private DefaultListModel<Investment> investmentListModel;
    private JPanel investmentDetailPanel;

    private JList<Card> cardList;
    private DefaultListModel<Card> cardListModel;
    private JPanel cardDetailPanel;
    // --- Taxation Tab Variables ---

    private JList<TaxProfile> taxProfileList;
    private DefaultListModel<TaxProfile> taxProfileListModel;
    private JPanel taxDetailPanel;
    // --- Loan Tab Variables ---
private JList<Loan> loanList;
private DefaultListModel<Loan> loanListModel;
private JPanel loanDetailPanel;


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
        JButton exportButton = new JButton("Export to File...");
        tTopPanel.add(exportButton);
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
        exportButton.addActionListener(e -> openExportDialog());
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
        bankAccountList.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
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
        depositList.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
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
        // ===         INVESTMENTS PANEL                         ===
        // =========================================================
        JPanel iPanel = new JPanel(new BorderLayout());
        JSplitPane investmentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        investmentSplitPane.setDividerLocation(220);
        investmentListModel = new DefaultListModel<>();
        investmentList = new JList<>(investmentListModel);
        investmentList.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        investmentList.setBorder(new EmptyBorder(5, 5, 5, 5));
        investmentSplitPane.setLeftComponent(new JScrollPane(investmentList));

        investmentDetailPanel = new JPanel(new BorderLayout());
        investmentDetailPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        investmentDetailPanel.add(new JLabel("Select an investment to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
        investmentSplitPane.setRightComponent(investmentDetailPanel);

        iPanel.add(investmentSplitPane, BorderLayout.CENTER);

        JPanel investmentButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addInvestmentBtn = new JButton("Add Investment");
        JButton deleteInvestmentBtn = new JButton("Delete Selected");
        JButton investmentRecycleBinBtn = new JButton("Investment Recycle Bin");
        investmentButtonPanel.add(addInvestmentBtn);
        investmentButtonPanel.add(deleteInvestmentBtn);
        investmentButtonPanel.add(investmentRecycleBinBtn);
        iPanel.add(investmentButtonPanel, BorderLayout.SOUTH);

        tabs.addTab("Investments", iPanel);

        // Actions
        investmentList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Investment selected = investmentList.getSelectedValue();
                showInvestmentDetails(selected);
            }
        });
        addInvestmentBtn.addActionListener(e -> openAddEditInvestmentDialog(null));
        deleteInvestmentBtn.addActionListener(e -> deleteSelectedInvestment());
        investmentRecycleBinBtn.addActionListener(e -> openInvestmentRecycleBin());

        // Initial load
        refreshInvestments();


        // =========================================================
// ===         NEW TAXATION PANEL (MASTER-DETAIL)        ===
// =========================================================
JPanel taxPanel = new JPanel(new BorderLayout());

// --- Split Pane ---
JSplitPane taxSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
taxSplitPane.setDividerLocation(220); // Adjust as needed

// --- Left Side: List of Tax Profiles ---
taxProfileListModel = new DefaultListModel<>();
taxProfileList = new JList<>(taxProfileListModel);
taxProfileList.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
taxProfileList.setBorder(new EmptyBorder(5, 5, 5, 5));
taxSplitPane.setLeftComponent(new JScrollPane(taxProfileList));

// --- Right Side: Detail Panel ---
taxDetailPanel = new JPanel(new BorderLayout());
taxDetailPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
taxDetailPanel.add(new JLabel("Select a tax profile to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
taxSplitPane.setRightComponent(taxDetailPanel);

taxPanel.add(taxSplitPane, BorderLayout.CENTER);

// --- Bottom Button Panel ---
JPanel taxButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
JButton addTaxProfileBtn = new JButton("Add New Tax Profile");
JButton deleteTaxProfileBtn = new JButton("Delete Selected Profile");

taxButtonPanel.add(addTaxProfileBtn);
taxButtonPanel.add(deleteTaxProfileBtn);
taxPanel.add(taxButtonPanel, BorderLayout.SOUTH);

tabs.addTab("Taxation", taxPanel); // Add the new tab

// --- Action Listeners ---
taxProfileList.addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        TaxProfile selected = taxProfileList.getSelectedValue();
        showTaxProfileDetails(selected); // New method we will add
    }
});

addTaxProfileBtn.addActionListener(e -> openAddEditTaxProfileDialog(null)); // New method
deleteTaxProfileBtn.addActionListener(e -> deleteSelectedTaxProfile()); // New method

// Load initial data
refreshTaxProfiles(); // New method
// =========================================================
// ===         NEW LOANS / EMI PANEL (MASTER-DETAIL)     ===
// =========================================================
JPanel lPanel = new JPanel(new BorderLayout());

// --- Split Pane ---
JSplitPane loanSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
loanSplitPane.setDividerLocation(220); // Adjust as needed

// --- Left Side: List of Loans ---
loanListModel = new DefaultListModel<>();
loanList = new JList<>(loanListModel);
loanList.setFont(new Font("Arial", Font.PLAIN, 14));
loanList.setBorder(new EmptyBorder(5, 5, 5, 5));
loanSplitPane.setLeftComponent(new JScrollPane(loanList));

// --- Right Side: Detail Panel ---
loanDetailPanel = new JPanel(new BorderLayout());
loanDetailPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
loanDetailPanel.add(new JLabel("Select a loan to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
loanSplitPane.setRightComponent(loanDetailPanel);

lPanel.add(loanSplitPane, BorderLayout.CENTER);

// --- Bottom Button Panel ---
JPanel loanButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
JButton addLoanBtn = new JButton("Add New Loan");
JButton deleteLoanBtn = new JButton("Delete Selected Loan");
JButton loanRecycleBinBtn = new JButton("Loan Recycle Bin");

loanButtonPanel.add(addLoanBtn);
loanButtonPanel.add(deleteLoanBtn);
loanButtonPanel.add(loanRecycleBinBtn);
lPanel.add(loanButtonPanel, BorderLayout.SOUTH);

tabs.addTab("Loans / EMI", lPanel); // Add the new tab

// --- Action Listeners ---
loanList.addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        Loan selected = loanList.getSelectedValue();
        showLoanDetails(selected); // New method we will add
    }
});

addLoanBtn.addActionListener(e -> openAddEditLoanDialog(null)); // New method
deleteLoanBtn.addActionListener(e -> deleteSelectedLoan()); // New method
loanRecycleBinBtn.addActionListener(e -> openLoanRecycleBin()); // New method

// Load initial data
refreshLoans(); // New method
// =========================================================
// ===         NEW CARDS PANEL (MASTER-DETAIL)           ===
// =========================================================
JPanel cPanel = new JPanel(new BorderLayout());

// --- Split Pane ---
JSplitPane cardSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
cardSplitPane.setDividerLocation(220); // Adjust width for card names

// --- Left Side: List of Cards ---
cardListModel = new DefaultListModel<>();
cardList = new JList<>(cardListModel);
cardList.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
cardList.setBorder(new EmptyBorder(5, 5, 5, 5));
cardSplitPane.setLeftComponent(new JScrollPane(cardList));

// --- Right Side: Detail Panel ---
cardDetailPanel = new JPanel(new BorderLayout());
cardDetailPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
cardDetailPanel.add(new JLabel("Select a card to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
cardSplitPane.setRightComponent(cardDetailPanel);

cPanel.add(cardSplitPane, BorderLayout.CENTER);

// --- Bottom Button Panel ---
JPanel cardButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
JButton addCardBtn = new JButton("Add New Card");
JButton deleteCardBtn = new JButton("Delete Selected Card");
JButton cardRecycleBinBtn = new JButton("Card Recycle Bin");

cardButtonPanel.add(addCardBtn);
cardButtonPanel.add(deleteCardBtn);
cardButtonPanel.add(cardRecycleBinBtn);
cPanel.add(cardButtonPanel, BorderLayout.SOUTH);

tabs.addTab("Cards", cPanel); // Renamed tab

// --- Action Listeners ---
cardList.addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        Card selected = cardList.getSelectedValue();
        showCardDetails(selected); // Method to be added below
    }
});

addCardBtn.addActionListener(e -> openAddEditCardDialog(null)); // Method stub below
deleteCardBtn.addActionListener(e -> deleteSelectedCard()); // Method to be added below
cardRecycleBinBtn.addActionListener(e -> openCardRecycleBin()); // Method stub below

// Load initial data
refreshCards(); // Method to be added below


    
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
            title.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
            detailGrid.add(title);
            String accType = acc.getAccountType();
            if ("Current".equals(accType)) accType += " (" + acc.getAccountSubtype() + ")";
            JLabel subTitle = new JLabel(accType + " Account");
            subTitle.setFont(new java.awt.Font("Arial", java.awt.Font.ITALIC, 18));
            detailGrid.add(subTitle);
            JLabel balanceLabel = new JLabel(String.format("Balance: ₹%.2f", acc.getBalance()));
            balanceLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
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
                calcLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
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
    JDialog dlg = new JDialog(this, "Add New Bank Account", true);
    dlg.setLayout(new BorderLayout(10, 10));

    // --- Panel for all fields ---
    JPanel fieldsPanel = new JPanel();
    fieldsPanel.setLayout(new GridLayout(0, 2, 5, 5)); // Use GridLayout
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
    fieldsPanel.add(new JLabel("Account Type:")); fieldsPanel.add(typeCombo);
    fieldsPanel.add(new JLabel("Bank Name:")); fieldsPanel.add(bankF);
    fieldsPanel.add(new JLabel("Account Number:")); fieldsPanel.add(accNumF);
    fieldsPanel.add(new JLabel("Holder Name:")); fieldsPanel.add(holderF);
    fieldsPanel.add(new JLabel("IFSC Code:")); fieldsPanel.add(ifscF);
    fieldsPanel.add(new JLabel("Current Balance:")); fieldsPanel.add(balanceF);

    // --- Add Specific Fields ---
    fieldsPanel.add(rateLabel); fieldsPanel.add(rateF);
    fieldsPanel.add(expenseLabel); fieldsPanel.add(expenseF);
    fieldsPanel.add(subtypeLabel); fieldsPanel.add(subtypeCombo);
    fieldsPanel.add(companyLabel); fieldsPanel.add(companyF);
    fieldsPanel.add(businessLabel); fieldsPanel.add(businessF);

    // --- Logic to Show/Hide Fields ---
    ActionListener typeListener = e -> { // Create listener separately
        String selected = (String) typeCombo.getSelectedItem();
        boolean isSavings = "Savings".equals(selected);
        rateLabel.setVisible(isSavings); rateF.setVisible(isSavings);
        expenseLabel.setVisible(isSavings); expenseF.setVisible(isSavings);
        subtypeLabel.setVisible(!isSavings); subtypeCombo.setVisible(!isSavings);
        // Trigger sub-type logic only if type changed and listener exists
         if (subtypeCombo.getActionListeners().length > 0) {
             subtypeCombo.getActionListeners()[0].actionPerformed(null); // Trigger dependent visibility
         }
         dlg.pack(); // Repack dialog when visibility changes
    };
    typeCombo.addActionListener(typeListener); // Add listener

    ActionListener subtypeListener = e -> { // Create listener separately
        boolean isSavings = "Savings".equals(typeCombo.getSelectedItem());
        boolean makeVisible = !isSavings; // Only make these visible if it's Current
        String subSelected = (String) subtypeCombo.getSelectedItem();
        boolean isSalary = "Salary".equals(subSelected);

        companyLabel.setVisible(makeVisible && isSalary);
        companyF.setVisible(makeVisible && isSalary);
        businessLabel.setVisible(makeVisible && !isSalary);
        businessF.setVisible(makeVisible && !isSalary);
         dlg.pack(); // Repack dialog when visibility changes
    };
     subtypeCombo.addActionListener(subtypeListener); // Add listener

    // --- Buttons Panel ---
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    buttonPanel.add(ok); buttonPanel.add(cancel);

    dlg.add(fieldsPanel, BorderLayout.CENTER);
    dlg.add(buttonPanel, BorderLayout.SOUTH);

    // --- Save Logic ---
    ok.addActionListener(_ -> {
        try {
            String accountType = (String) typeCombo.getSelectedItem();
            double balance = Double.parseDouble(balanceF.getText());
            double interestRate = 0.0, annualExpense = 0.0;
            String accountSubtype = "", companyName = "", businessName = "";

            if ("Savings".equals(accountType)) {
                interestRate = Double.parseDouble(rateF.getText());
                annualExpense = Double.parseDouble(expenseF.getText());
            } else { // Current
                accountSubtype = (String) subtypeCombo.getSelectedItem();
                if ("Salary".equals(accountSubtype)) companyName = companyF.getText();
                else businessName = businessF.getText();
            }

            BankAccount ba = new BankAccount(
                accNumF.getText(), holderF.getText(), bankF.getText(), ifscF.getText(), balance,
                accountType, interestRate, annualExpense,
                accountSubtype, companyName, businessName
            );
            manager.saveBankAccount(ba);
            dlg.dispose();
            refreshBankAccounts(); // Refresh the main list
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
            ex.printStackTrace(); // Print detailed error to console
        }
    });
    cancel.addActionListener(ev -> dlg.dispose());

    // --- Initialize View ---
     typeListener.actionPerformed(null); // Call listener once to set initial visibility

    dlg.pack(); // Pack initially
    dlg.setLocationRelativeTo(this);
    dlg.setVisible(true);
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
        title.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
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

    


   
    // --- Main Method ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FinanceManagerFullUI().setVisible(true));
    }
    // =========================================================
// ===         NEW CARD UI METHODS                       ===
// =========================================================

/**
 * Refreshes the list of cards on the left side.
 * Make sure this is PUBLIC so dialogs can call it.
 */
public void refreshCards() {
    System.out.println("--- Refreshing Cards ---"); // Add log
    cardListModel.clear();
    cardDetailPanel.removeAll(); // Clear details
    cardDetailPanel.add(new JLabel("Select a card to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
    cardDetailPanel.revalidate();
    cardDetailPanel.repaint();
    try {
        List<Card> cards = manager.getAllCards(); // Use the new method from FinanceManager
        System.out.println("Fetched " + cards.size() + " cards."); // Add log
        for (Card c : cards) {
            cardListModel.addElement(c);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading cards: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace(); // Print detailed error
    } catch (Exception e) {
         JOptionPane.showMessageDialog(this, "Unexpected error refreshing cards: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         e.printStackTrace(); // Print detailed error
    }
     System.out.println("--- Finished Refreshing Cards ---"); // Add log
}

/**
 * Shows basic card details and buttons on the right side.
 * Sensitive details will require OTP verification later.
 */
private void showCardDetails(Card card) {
    cardDetailPanel.removeAll();
    if (card == null) {
        cardDetailPanel.add(new JLabel("Select a card to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
        cardDetailPanel.revalidate(); cardDetailPanel.repaint();
        return;
    }

    JPanel detailGrid = new JPanel(new GridLayout(0, 1, 5, 10)); // Single column

    JLabel title = new JLabel(card.getCardName());
    title.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
    detailGrid.add(title);
    detailGrid.add(new JLabel(card.getCardType()));
    detailGrid.add(new JLabel("Number: " + card.getMaskedCardNumber())); // Show masked number
    detailGrid.add(new JLabel("Valid Thru: " + (card.getValidThrough() != null ? card.getValidThrough() : "N/A")));

    detailGrid.add(new JSeparator());

    if ("Credit Card".equals(card.getCardType())) {
        detailGrid.add(new JLabel(String.format("Limit: ₹%.2f", card.getCreditLimit())));
        detailGrid.add(new JLabel(String.format("Current Spend: ₹%.2f", card.getCurrentExpenses())));
        detailGrid.add(new JLabel(String.format("Amount Due: ₹%.2f", card.getAmountToPay())));
        detailGrid.add(new JLabel("Days Until Due: " + card.getDaysLeftToPay()));
        detailGrid.add(new JSeparator());
    }

    // --- Buttons for Actions ---
    JPanel buttonSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton viewSensitiveButton = new JButton("View Full Details (Requires OTP)");
    JButton editButton = new JButton("Edit Basic Info");

    buttonSubPanel.add(viewSensitiveButton);
    buttonSubPanel.add(editButton);

    // --- Action Listeners for Buttons ---
    viewSensitiveButton.addActionListener(e -> showSensitiveCardDetailsWithOTP(card)); // Placeholder below
    editButton.addActionListener(e -> openAddEditCardDialog(card)); // Placeholder below

    cardDetailPanel.add(detailGrid, BorderLayout.NORTH);
    cardDetailPanel.add(buttonSubPanel, BorderLayout.CENTER); // Add buttons below details

    cardDetailPanel.revalidate();
    cardDetailPanel.repaint();
}

 /**
  * Securely shows sensitive card details after OTP verification.
  * Generates a 6-digit OTP (simulated delivery), enforces 2-minute TTL and 3 attempts.
  */
 private void showSensitiveCardDetailsWithOTP(Card card) {
     try {
         // Generate OTP and TTL window
         String otp = String.format("%06d", new Random().nextInt(1_000_000));
         long issuedAt = System.currentTimeMillis();
         final long ttlMs = 2L * 60L * 1000L; // 2 minutes

         // Show OTP (simulated delivery)
         ShowOtpDialog otpDialog = new ShowOtpDialog(this, otp);
         otpDialog.setVisible(true);

         int attempts = 0;
         final int maxAttempts = 3;
         while (attempts < maxAttempts) {
             if (System.currentTimeMillis() - issuedAt > ttlMs) {
                 JOptionPane.showMessageDialog(this, "OTP expired. Please try again.", "OTP Expired", JOptionPane.WARNING_MESSAGE);
                 return;
             }

             EnterOtpDialog enter = new EnterOtpDialog(this);
             enter.setVisible(true);
             String entered = enter.getEnteredOtp();
             if (entered == null) { // Cancelled
                 return;
             }

             if (otp.equals(entered)) {
                 SensitiveCardDetailsDialog details = new SensitiveCardDetailsDialog(this, card);
                 details.setVisible(true);
                 return;
             } else {
                 attempts++;
                 int left = maxAttempts - attempts;
                 JOptionPane.showMessageDialog(this,
                         left > 0 ? ("Incorrect OTP. Attempts left: " + left) : "Incorrect OTP. No attempts left.",
                         "OTP Incorrect",
                         JOptionPane.ERROR_MESSAGE);
             }
         }
     } catch (Exception ex) {
         JOptionPane.showMessageDialog(this, "Error during OTP verification: " + ex.getMessage(), "OTP Error", JOptionPane.ERROR_MESSAGE);
     }
 }

/**
 * Opens the dialog to add a new card or edit an existing one (Placeholder).
 */
private void openAddEditCardDialog(Card cardToEdit) {
    // We are now calling the real dialog
    AddEditCardDialog dialog = new AddEditCardDialog(this, manager, cardToEdit, this);
    dialog.setVisible(true);
    // Refresh is handled by the dialog itself upon successful save.
}

/**
 * Deletes the selected card and moves it to the recycle bin.
 */
private void deleteSelectedCard() {
    Card selected = cardList.getSelectedValue();
    if (selected == null) {
        JOptionPane.showMessageDialog(this, "Please select a card to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
        return;
    }

    int choice = JOptionPane.showConfirmDialog(this,
        "Move this card to the recycle bin?\n" + selected.toString(),
        "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

    if (choice == JOptionPane.YES_OPTION) {
        try {
            manager.moveCardToRecycleBin(selected.getId()); // Use the new method from FinanceManager
            refreshCards(); // Refresh the list in the UI
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting card: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Print detailed error
        }
    }
}

/**
 * Opens the Card Recycle Bin dialog (Placeholder).
 */
private void openCardRecycleBin() {
    // We are now calling the real dialog
    CardRecycleBinDialog dialog = new CardRecycleBinDialog(this, manager, this);
    dialog.setVisible(true);
    // Refresh (after restore) is handled by the dialog itself.
}
/**
 * Callback method for the Card Recycle Bin dialog (Placeholder).
 * Make sure this is PUBLIC.
 */
public void refreshAfterCardRestore() {
    System.out.println("Refreshing cards list after restore...");
    refreshCards();
}
// =========================================================
    // ===         NEW INVESTMENT UI METHODS                 ===
    // =========================================================

    /**
     * Refreshes the list of investments on the left side.
     * Make this PUBLIC so dialogs can call it.
     */
    public void refreshInvestments() {
        System.out.println("--- Refreshing Investments ---");
        investmentListModel.clear();
        investmentDetailPanel.removeAll(); // Clear details
        investmentDetailPanel.add(new JLabel("Select an investment to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
        investmentDetailPanel.revalidate();
        investmentDetailPanel.repaint();
        try {
            List<Investment> investments = manager.getAllInvestments();
            System.out.println("Fetched " + investments.size() + " investments.");
            for (Investment inv : investments) {
                investmentListModel.addElement(inv);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading investments: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
         System.out.println("--- Finished Refreshing Investments ---");
    }

    /**
     * Shows calculated details for the selected investment on the right panel.
     */
    private void showInvestmentDetails(Investment inv) {
        investmentDetailPanel.removeAll();
        if (inv == null) {
            investmentDetailPanel.add(new JLabel("Select an investment to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
            JPanel detailGrid = new JPanel(new GridLayout(0, 1, 5, 10)); // Single column layout
            String titleName = (inv.getDescription() != null && !inv.getDescription().isEmpty()) ? inv.getDescription() : inv.getTickerSymbol();
            JLabel title = new JLabel(titleName + " (" + inv.getAssetType() + ")");
            title.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
            detailGrid.add(title);
            detailGrid.add(new JLabel("Holder: " + inv.getHolderName()));

            detailGrid.add(new JSeparator());
            
            // Calculated Values
            double initialCost = inv.getTotalInitialCost();
            double currentValue = inv.getTotalCurrentValue();
            double pnl = inv.getProfitOrLoss();
            double pnlPercent = inv.getProfitOrLossPercentage();

            detailGrid.add(new JLabel(String.format("Total Initial Cost: ₹%.2f", initialCost)));
            detailGrid.add(new JLabel(String.format("Total Current Value: ₹%.2f", currentValue)));
            
            // Profit/Loss Label
            String pnlText = String.format("Profit/Loss: ₹%.2f (%.2f%%)", pnl, pnlPercent);
            JLabel pnlLabel = new JLabel(pnlText);
            // Set color based on profit or loss
            if (pnl > 0) {
                pnlLabel.setForeground(new java.awt.Color(0, 128, 0)); // Dark Green
            } else if (pnl < 0) {
                pnlLabel.setForeground(java.awt.Color.RED);
            }
            pnlLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
            detailGrid.add(pnlLabel);

            detailGrid.add(new JSeparator());
            
            // Unit Details (if applicable)
            if (inv.getQuantity() > 0) {
                detailGrid.add(new JLabel(String.format("Quantity: %.4f units", inv.getQuantity())));
                detailGrid.add(new JLabel(String.format("Avg. Cost Price: ₹%.2f /unit", inv.getInitialUnitCost())));
                detailGrid.add(new JLabel(String.format("Current Price: ₹%.2f /unit", inv.getCurrentUnitPrice())));
            }

            // --- Buttons for Actions ---
            JPanel buttonSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton updatePriceButton = new JButton("Update Current Price");
            JButton editButton = new JButton("Edit Details");
            buttonSubPanel.add(updatePriceButton);
            buttonSubPanel.add(editButton);

            // --- Action Listeners for Buttons ---
            editButton.addActionListener(e -> openAddEditInvestmentDialog(inv)); // Open edit dialog
            updatePriceButton.addActionListener(e -> openUpdatePriceDialog(inv)); // New method stub

            investmentDetailPanel.add(detailGrid, BorderLayout.NORTH);
            investmentDetailPanel.add(buttonSubPanel, BorderLayout.CENTER);
        }
        investmentDetailPanel.revalidate();
        investmentDetailPanel.repaint();
    }

    /**
     * Opens a dialog to manually update the current price of an asset.
     */
    private void openUpdatePriceDialog(Investment inv) {
        String currentPriceStr = JOptionPane.showInputDialog(
            this,
            "Enter new Current Unit Price for:\n" + inv.getDescription(),
            "Update Price",
            JOptionPane.QUESTION_MESSAGE
        );

        if (currentPriceStr != null && !currentPriceStr.isEmpty()) {
            try {
                double newPrice = Double.parseDouble(currentPriceStr);
                // Update the investment's current unit price through the manager
                manager.updateInvestmentCurrentPrice(inv.getId(), newPrice);
                refreshInvestments(); // Refresh the entire list
                // Re-select the item to show updated details
                investmentList.setSelectedValue(inv, true);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Failed to update price: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Opens the dialog to add a new investment or edit an existing one (Placeholder).
     */
    private void openAddEditInvestmentDialog(Investment investmentToEdit) {
    AddEditInvestmentDialog dialog = new AddEditInvestmentDialog(this, manager, investmentToEdit, this);
    dialog.setVisible(true);
    // Refresh is handled by the dialog on success
}

    /**
     * Deletes the selected investment and moves it to the recycle bin.
     */
    private void deleteSelectedInvestment() {
        Investment selected = investmentList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an investment to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
            "Move this investment to the recycle bin?\n" + selected.toString(),
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                manager.moveInvestmentToRecycleBin(selected.getId());
                refreshInvestments(); // Refresh the list
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting investment: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Opens the Investment Recycle Bin dialog (Placeholder).
     */
    private void openInvestmentRecycleBin() {
     InvestmentRecycleBinDialog dialog = new InvestmentRecycleBinDialog(this, manager, this);
     dialog.setVisible(true);
     // The main list will refresh via the callback 'refreshAfterInvestmentRestore' if needed
}

    /**
     * Callback method for the Investment Recycle Bin dialog (Placeholder).
     * Make sure this is PUBLIC.
     */
    public void refreshAfterInvestmentRestore() {
        System.out.println("Refreshing investments list after restore...");
        refreshInvestments();
    }
    
// ==================================================================
// ===               TAXATION UI METHODS                          ===
// ==================================================================

/**
 * Refreshes the list of tax profiles on the left side.
 * Make this PUBLIC so dialogs can call it.
 */
public void refreshTaxProfiles() {
    System.out.println("--- Refreshing Tax Profiles ---");
    taxProfileListModel.clear();
    taxDetailPanel.removeAll();
    taxDetailPanel.add(new JLabel("Select a profile to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
    taxDetailPanel.revalidate();
    taxDetailPanel.repaint();
    try {
        List<TaxProfile> profiles = manager.getAllTaxProfiles();
        System.out.println("Fetched " + profiles.size() + " tax profiles.");
        for (TaxProfile tp : profiles) {
            taxProfileListModel.addElement(tp);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading tax profiles: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

/**
 * Shows calculated details for the selected tax profile.
 */
private void showTaxProfileDetails(TaxProfile tp) {
    taxDetailPanel.removeAll();
    if (tp == null) {
        taxDetailPanel.add(new JLabel("Select a profile to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
    } else {
        JPanel detailGrid = new JPanel(new GridLayout(0, 1, 5, 10)); // Single column layout

        JLabel title = new JLabel(tp.getProfileName() + " (" + tp.getFinancialYear() + ")");
        title.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        detailGrid.add(title);
        detailGrid.add(new JLabel("Profile Type: " + tp.getProfileType()));

        detailGrid.add(new JSeparator());

        // --- Core Calculation Display ---
        JLabel grossLabel = new JLabel(String.format("Gross Income: ₹%.2f", tp.getGrossIncome()));
        grossLabel.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        detailGrid.add(grossLabel);

        JLabel deducLabel = new JLabel(String.format("Total Deductions: - ₹%.2f", tp.getTotalDeductions()));
        deducLabel.setForeground(java.awt.Color.RED);
        deducLabel.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        detailGrid.add(deducLabel);

        detailGrid.add(new JSeparator());

        JLabel taxableLabel = new JLabel(String.format("Total Taxable Income: ₹%.2f", tp.getTaxableIncome()));
        taxableLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        detailGrid.add(taxableLabel);

        detailGrid.add(new JSeparator());

        JLabel taxPaidLabel = new JLabel(String.format("Tax Already Paid (TDS, etc.): ₹%.2f", tp.getTaxPaid()));
        detailGrid.add(taxPaidLabel);

        if (tp.getNotes() != null && !tp.getNotes().isEmpty()) {
            detailGrid.add(new JSeparator());
            JTextArea notesArea = new JTextArea("Notes:\n" + tp.getNotes());
            notesArea.setEditable(false);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            detailGrid.add(notesArea);
        }

        // --- Buttons ---
        JPanel buttonSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editButton = new JButton("Edit Profile");
        editButton.addActionListener(e -> openAddEditTaxProfileDialog(tp));
        buttonSubPanel.add(editButton);

        taxDetailPanel.add(detailGrid, BorderLayout.NORTH);
        taxDetailPanel.add(buttonSubPanel, BorderLayout.CENTER);
    }
    taxDetailPanel.revalidate();
    taxDetailPanel.repaint();
}

/**
 * Opens the dialog to add/edit a tax profile (Placeholder).
 */
private void openAddEditTaxProfileDialog(TaxProfile profileToEdit) {
    AddEditTaxProfileDialog dialog = new AddEditTaxProfileDialog(this, manager, profileToEdit, this);
    dialog.setVisible(true);
    // Refresh is handled by the dialog on success
}

/**
 * Deletes the selected tax profile.
 */
private void deleteSelectedTaxProfile() {
    TaxProfile selected = taxProfileList.getSelectedValue();
    if (selected == null) {
        JOptionPane.showMessageDialog(this, "Please select a profile to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
        return;
    }

    int choice = JOptionPane.showConfirmDialog(this,
        "Are you sure you want to permanently delete this tax profile?\n" + selected.toString(),
        "Confirm Permanent Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

    if (choice == JOptionPane.YES_OPTION) {
        try {
            manager.deleteTaxProfile(selected.getId());
            refreshTaxProfiles(); // Refresh the list
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting profile: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
/**
     * 1. Shows a dialog to choose export options (Month/Year, Format).
     */
    private void openExportDialog() {
        // --- Create the options panel ---
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        
        // Option 1: Scope (Month vs. Year)
        JRadioButton monthRadio = new JRadioButton("Export Current Month Only");
        JRadioButton yearRadio = new JRadioButton("Export Entire Selected Year");
        ButtonGroup scopeGroup = new ButtonGroup();
        scopeGroup.add(monthRadio); scopeGroup.add(yearRadio);
        monthRadio.setSelected(true);
        
        JPanel scopePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scopePanel.setBorder(BorderFactory.createTitledBorder("Scope"));
        scopePanel.add(monthRadio); scopePanel.add(yearRadio);

    // Option 2: Format (CSV, PDF)
    JRadioButton csvRadio = new JRadioButton("CSV (Comma Separated)");
    JRadioButton pdfRadio = new JRadioButton("PDF Document");
    ButtonGroup formatGroup = new ButtonGroup();
    formatGroup.add(csvRadio); formatGroup.add(pdfRadio);
    csvRadio.setSelected(true);
        
        JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    formatPanel.setBorder(BorderFactory.createTitledBorder("Format"));
    formatPanel.add(csvRadio); formatPanel.add(pdfRadio);
        
        panel.add(scopePanel);
        panel.add(formatPanel);

        int result = JOptionPane.showConfirmDialog(this, panel, "Export Options", 
                                                 JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result != JOptionPane.OK_OPTION) {
            return; // User cancelled
        }

        // --- Get selected options ---
        String scope = monthRadio.isSelected() ? "Month" : "Year";
    String format = csvRadio.isSelected() ? "CSV" : "PDF";
        
        // --- Get the data to export ---
        List<Transaction> dataToExport = new ArrayList<>();
        String defaultFilename = "";
        
        try {
            if (scope.equals("Month")) {
                JScrollPane currentScrollPane = (JScrollPane) monthTabs.getSelectedComponent();
                if (currentScrollPane == null) {
                    JOptionPane.showMessageDialog(this, "No month tab selected.", "Export Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                JTable currentTable = (JTable) currentScrollPane.getViewport().getView();
                String monthYear = (String) currentTable.getClientProperty("monthYear");
                
                // Get filtered/sorted data directly from the JTable
                for (int i = 0; i < currentTable.getRowCount(); i++) {
                    int modelRow = currentTable.convertRowIndexToModel(i);
                    int transactionId = (int) currentTable.getModel().getValueAt(modelRow, 0); // Get ID from model
                    
                    // Find this transaction in the full list (this is inefficient but simple)
                    // A better way would be to get the data directly from the table row
                    // Let's just export what's visible in the table
                     dataToExport.add(new Transaction(
                         (int) currentTable.getValueAt(i, 0), // S.No (ID)
                         (String) currentTable.getValueAt(i, 1), // Date
                         (String) currentTable.getValueAt(i, 2), // Timestamp
                         (String) currentTable.getValueAt(i, 3), // Day
                         (String) currentTable.getValueAt(i, 5), // Category
                         (String) currentTable.getValueAt(i, 6), // Type
                         (Double) currentTable.getValueAt(i, 9), // Amount
                         (String) currentTable.getValueAt(i, 8), // Description
                         (String) currentTable.getValueAt(i, 4), // Payment Method
                         (String) currentTable.getValueAt(i, 7)  // Payee
                     ));
                }
                defaultFilename = getMonthName(monthYear) + "_Transactions";
                
            } else { // "Year"
                String selectedYear = (String) yearComboBox.getSelectedItem();
                if (selectedYear == null || selectedYear.equals("All Years")) {
                     dataToExport = manager.getAllTransactionsForYear("All Years");
                     defaultFilename = "All_Transactions";
                } else {
                     dataToExport = manager.getAllTransactionsForYear(selectedYear);
                     defaultFilename = selectedYear + "_Transactions";
                }
            }
            
            if (dataToExport.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No transactions found to export.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // --- Show Save Dialog ---
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Export File");
            fileChooser.setSelectedFile(new File(defaultFilename + "." + format.toLowerCase()));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                // --- Call the correct writer ---
                if (format.equals("CSV")) {
                    writeToCsv(dataToExport, file);
                } else if (format.equals("PDF")) {
                    writeToPdf(dataToExport, file);
                }
                JOptionPane.showMessageDialog(this, "Export successful!\nSaved to: " + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred during export: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Excel (.xlsx) export removed by request — only CSV and PDF remain.

    /**
     * 3. Writes a List of Transactions to a .csv file.
     */
    private void writeToCsv(List<Transaction> data, File file) throws IOException {
        String[] columns = {"S.No.", "Date", "Timestamp", "Day", "Payment Method", "Category", "Type", "Payee", "Description", "Amount"};
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // Write header
            pw.println(String.join(",", columns));
            
            // Write data
            for (Transaction t : data) {
                StringJoiner sj = new StringJoiner(",");
                sj.add(String.valueOf(t.getId()));
                sj.add("\"" + t.getDate() + "\"");
                sj.add("\"" + t.getTimestamp() + "\"");
                sj.add("\"" + t.getDay() + "\"");
                sj.add("\"" + t.getPaymentMethod() + "\"");
                sj.add("\"" + t.getCategory() + "\"");
                sj.add("\"" + t.getType() + "\"");
                sj.add("\"" + t.getPayee() + "\"");
                // Escape quotes in description
                String desc = t.getDescription().replace("\"", "\"\"");
                sj.add("\"" + desc + "\"");
                sj.add(String.valueOf(t.getAmount()));
                pw.println(sj.toString());
            }
        }
    }
    
    /**
     * 4. Writes a List of Transactions to a .pdf file.
     * Requires iText library.
     */
    private void writeToPdf(List<Transaction> data, File file) {
        // --- PDF STUB ---
        // This method requires the iText library (e.g., itextpdf-5.5.13.3.jar)
        // in your /lib folder.
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Document document = new Document();
            PdfWriter.getInstance(document, fos);
            document.open();
            
            // Title
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            document.add(new Paragraph("Transaction Report", titleFont));
            document.add(new Paragraph(" ")); // Empty line
            
            // Create Table
            String[] columns = {"S.No", "Date", "Category", "Type", "Payee", "Description", "Amount"};
            PdfPTable table = new PdfPTable(columns.length);
            table.setWidthPercentage(100);
            
            // Add Header
            com.itextpdf.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            for (String header : columns) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
            
            // Add Data Rows
            com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
            for (Transaction t : data) {
                table.addCell(new Phrase(String.valueOf(t.getId()), cellFont));
                table.addCell(new Phrase(t.getDate(), cellFont));
                table.addCell(new Phrase(t.getCategory(), cellFont));
                table.addCell(new Phrase(t.getType(), cellFont));
                table.addCell(new Phrase(t.getPayee(), cellFont));
                table.addCell(new Phrase(t.getDescription(), cellFont));
                
                // Align amount to the right
                PdfPCell amountCell = new PdfPCell(new Phrase(String.format("%.2f", t.getAmount()), cellFont));
                amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(amountCell);
            }
            
            document.add(table);
            document.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error writing PDF file: " + e.getMessage() +
                "\n\nMake sure the iText library (itextpdf.jar) is in your /lib folder.", 
                "PDF Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // ==================================================================
// ===               LOAN / EMI UI METHODS                      ===
// ==================================================================

/**
 * Refreshes the list of loans on the left side.
 * Make this PUBLIC so dialogs can call it.
 */
public void refreshLoans() {
    System.out.println("--- Refreshing Loans ---");
    loanListModel.clear();
    loanDetailPanel.removeAll();
    loanDetailPanel.add(new JLabel("Select a loan to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
    loanDetailPanel.revalidate();
    loanDetailPanel.repaint();
    try {
        List<Loan> loans = manager.getAllLoans();
        System.out.println("Fetched " + loans.size() + " loans.");
        for (Loan l : loans) {
            loanListModel.addElement(l);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading loans: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

/**
 * Shows calculated details for the selected loan.
 */
private void showLoanDetails(Loan loan) {
    loanDetailPanel.removeAll();
    if (loan == null) {
        loanDetailPanel.add(new JLabel("Select a loan to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
    } else {
        JPanel detailGrid = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // --- Title ---
        JLabel title = new JLabel(loan.getLenderName() + " - " + loan.getLoanType());
        title.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        detailGrid.add(title, gbc);

        // --- EMI ---
        JLabel emiLabel = new JLabel(String.format("Monthly EMI: ₹%.2f", loan.getEmiAmount()));
        emiLabel.setFont(new Font("Arial", Font.BOLD, 16));
        emiLabel.setForeground(new Color(0, 100, 0)); // Dark green
        gbc.gridy = row++;
        detailGrid.add(emiLabel, gbc);

        gbc.gridy = row++; gbc.insets = new Insets(5, 0, 5, 0);
        detailGrid.add(new JSeparator(), gbc);
        gbc.insets = new Insets(4, 5, 4, 5);

        // --- Loan Details ---
        gbc.gridwidth = 1; // Reset to 1 column
        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Principal Amount:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(String.format("₹%.2f", loan.getPrincipalAmount())), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Annual Interest Rate:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(String.format("%.2f %%", loan.getInterestRate())), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Tenure:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(loan.getTenureMonths() + " months"), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(loan.getStartDate() != null ? loan.getStartDate() : "N/A"), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(loan.getStatus()), gbc);
        row++;

        // --- Total Payment Details ---
        gbc.gridy = row++; gbc.gridwidth = 2; gbc.insets = new Insets(5, 0, 5, 0);
        detailGrid.add(new JSeparator(), gbc);
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Total Principal Paid:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(String.format("₹%.2f", loan.getPrincipalAmount())), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Total Interest Paid:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(String.format("₹%.2f", loan.getTotalInterest())), gbc);
        row++;

        JLabel totalPayLabel = new JLabel(String.format("₹%.2f", loan.getTotalPayment()));
        totalPayLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Total Payment:"), gbc);
        gbc.gridx = 1; detailGrid.add(totalPayLabel, gbc);
        row++;

        // --- Buttons ---
        JPanel buttonSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editButton = new JButton("Edit / Mark as Paid");
        editButton.addActionListener(e -> openAddEditLoanDialog(loan));
        buttonSubPanel.add(editButton);

        loanDetailPanel.add(detailGrid, BorderLayout.NORTH);
        loanDetailPanel.add(buttonSubPanel, BorderLayout.CENTER);
    }
    loanDetailPanel.revalidate();
    loanDetailPanel.repaint();
}

/**
 * Opens the dialog to add/edit a loan (Placeholder).
 */
/**
 * Opens the dialog to add/edit a loan.
 */
private void openAddEditLoanDialog(Loan loanToEdit) {
    AddEditLoanDialog dialog = new AddEditLoanDialog(this, manager, loanToEdit, this);
    dialog.setVisible(true);
    // Refresh is handled by the dialog on success
}

/**
 * Deletes the selected loan (moves to recycle bin).
 */
private void deleteSelectedLoan() {
    Loan selected = loanList.getSelectedValue();
    if (selected == null) {
        JOptionPane.showMessageDialog(this, "Please select a loan to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
        return;
    }

    int choice = JOptionPane.showConfirmDialog(this,
        "Move this loan to the recycle bin?\n" + selected.toString(),
        "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

    if (choice == JOptionPane.YES_OPTION) {
        try {
            manager.moveLoanToRecycleBin(selected.getId());
            refreshLoans(); // Refresh the list
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting loan: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}

/**
 * Opens the Loan Recycle Bin dialog (Placeholder).
 */
/**
 * Opens the Loan Recycle Bin dialog.
 */
private void openLoanRecycleBin() {
     LoanRecycleBinDialog dialog = new LoanRecycleBinDialog(this, manager, this);
     dialog.setVisible(true);
     // The main list will refresh via the callback 'refreshAfterLoanRestore' if needed
}

/**
 * Callback method for the Loan Recycle Bin dialog (Placeholder).
 * Make sure this is PUBLIC.
 */
public void refreshAfterLoanRestore() {
    System.out.println("Refreshing loans list after restore...");
    refreshLoans();
}

} 