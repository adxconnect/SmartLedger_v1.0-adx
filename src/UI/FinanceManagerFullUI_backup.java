package src.UI;
// We'll also add a placeholder for the dialog we will create
// import src.UI.AddEditTaxProfileDialog;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
// Removed duplicate RecycleBinDialog import
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

import src.Deposit; // Import for Deposit class
import src.TaxProfile;
import src.Loan;
import src.SummaryData;
import src.auth.Account;
import src.auth.SessionContext;
import java.util.Objects;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Imports for Excel (Apache POI)
// Removed Excel (Apache POI) imports as XLSX export is no longer supported
// Imports for PDF (iText)
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;

public class FinanceManagerFullUI extends JFrame {
    private final FinanceManager manager;
    private final Account currentAccount;
    private JLabel sessionStatusLabel;

    // --- Transaction Tab Variables ---
    private JTabbedPane monthTabs;
    private JComboBox<String> yearComboBox;
    private JButton deleteMonthButton;
    private JTextField txnSearchField; // search text for transactions
    private JComboBox<String> txnSearchColumn; // column selector for transactions

    private static final String ALL_COLUMNS_OPTION = "All Columns";
    private static final String[] TRANSACTION_TABLE_COLUMNS = {
        "S.No.", "Date", "Timestamp", "Day", "Payment Method",
        "Category", "Type", "Payee", "Description", "Amount",
        "Month Income Total", "Month Expense Total"
    };
    private static final int TRANSACTION_COLUMN_AMOUNT_INDEX = 9;
    private static final int TRANSACTION_COLUMN_MONTHLY_INCOME_TOTAL_INDEX = 10;
    private static final int TRANSACTION_COLUMN_MONTHLY_EXPENSE_TOTAL_INDEX = 11;

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

    // --- Summary Tab Variables ---
    private JTextField summaryCompanyField;
    private JTextField summaryDesignationField;
    private JTextField summaryHolderField;
    private JComboBox<String> summaryYearCombo;
    private JButton summaryRefreshButton;
    private JButton summaryExportCsvButton;
    private JButton summaryExportPdfButton;
    private JTextArea summaryOverviewArea;
    private SummaryData currentSummarySnapshot;
    private boolean summaryTabInitialized = false;
    private static final DateTimeFormatter SUMMARY_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private String buildWindowTitle() {
        if (currentAccount == null || currentAccount.getAccountName() == null || currentAccount.getAccountName().isBlank()) {
            return "Finance Manager";
        }
        return "Finance Manager - " + currentAccount.getAccountName();
    }

    private String buildSessionBanner() {
        if (currentAccount == null) {
            return "Signed in session unavailable";
        }

        List<String> segments = new ArrayList<>();
        if (currentAccount.getAccountName() != null && !currentAccount.getAccountName().isBlank()) {
            segments.add(currentAccount.getAccountName());
        }
        if (currentAccount.getAccountType() != null) {
            segments.add(currentAccount.getAccountType().name());
        }
        if (currentAccount.getEmail() != null && !currentAccount.getEmail().isBlank()) {
            segments.add(currentAccount.getEmail());
        }

        if (segments.isEmpty()) {
            return "Signed in";
        }
    return "Signed in as " + String.join(" | ", segments);
    }

    private static ActionListener onClick(Runnable action) {
        Objects.requireNonNull(action, "action");
        return event -> {
            Objects.requireNonNull(event, "event");
            action.run();
        };
    }

    private static ChangeListener onChange(Runnable action) {
        Objects.requireNonNull(action, "action");
        return event -> {
            Objects.requireNonNull(event, "event");
            action.run();
        };
    }


    public FinanceManagerFullUI() {
        if (!SessionContext.isLoggedIn() || SessionContext.getCurrentAccount() == null) {
            throw new IllegalStateException("No authenticated account in session. Please sign in first.");
        }

        this.currentAccount = SessionContext.getCurrentAccount();

        setTitle(buildWindowTitle());
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        FinanceManager connectedManager;
        try {
            connectedManager = new FinanceManager();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "MySQL connection failed: " + e.getMessage());
            throw new IllegalStateException("Failed to connect to database", e);
        }
        this.manager = connectedManager;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                Objects.requireNonNull(event, "event");
                manager.close();
                SessionContext.clear();
            }
        });

        getContentPane().setLayout(new BorderLayout());
        sessionStatusLabel = new JLabel(buildSessionBanner(), SwingConstants.LEFT);
        sessionStatusLabel.setBorder(new EmptyBorder(6, 10, 6, 10));
        add(sessionStatusLabel, BorderLayout.NORTH);

        // JTabbedPane setup
        JTabbedPane tabs = new JTabbedPane();
        add(tabs, BorderLayout.CENTER);

        // =========================================================
        // ===         TRANSACTIONS PANEL                      ===
        // =========================================================
        JPanel tPanel = new JPanel(new BorderLayout(5, 5));
        tPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // --- Top Panel for Year, Delete, and Search controls ---
        JPanel tTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        yearComboBox = new JComboBox<>();
        tTopPanel.add(new JLabel("Select Year:"));
        tTopPanel.add(yearComboBox);
        JButton deleteYearButton = new JButton("Delete All of Selected Year");
        tTopPanel.add(deleteYearButton);
        deleteMonthButton = new JButton("Delete Selected Month");
        deleteMonthButton.setEnabled(false);
        tTopPanel.add(deleteMonthButton);

        // Add some spacing
        tTopPanel.add(Box.createHorizontalStrut(15));

        // --- Search Controls ---
        tTopPanel.add(new JLabel("Search:"));
        txnSearchField = new JTextField(15);
        tTopPanel.add(txnSearchField);
        
        tTopPanel.add(new JLabel("In:"));
        txnSearchColumn = new JComboBox<>(buildTransactionSearchOptions());
        txnSearchColumn.setSelectedIndex(0);
        tTopPanel.add(txnSearchColumn);

        tPanel.add(tTopPanel, BorderLayout.NORTH);
        
        // --- Center Panel for Tabs ---
        monthTabs = new JTabbedPane();
        monthTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tPanel.add(monthTabs, BorderLayout.CENTER);
        
        // --- Bottom Panel for Action Buttons ---
        JPanel tBottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addTxnBtn = new JButton("Add Transaction");
        JButton deleteTxnBtn = new JButton("Delete Selected Transaction");
        JButton recycleBinBtn = new JButton("Recycle Bin");
        JButton exportButton = new JButton("Export to File...");
        
        tBottomPanel.add(addTxnBtn);
        tBottomPanel.add(deleteTxnBtn);
        tBottomPanel.add(recycleBinBtn);
        tBottomPanel.add(exportButton);
        
        tPanel.add(tBottomPanel, BorderLayout.SOUTH);
        tabs.addTab("Transactions", tPanel);

        // --- Action Listeners for Transactions Tab ---
        recycleBinBtn.addActionListener(onClick(this::openRecycleBin));
        yearComboBox.addActionListener(onClick(this::refreshTransactions));
        addTxnBtn.addActionListener(onClick(this::openTransactionDialog));
        deleteTxnBtn.addActionListener(onClick(this::deleteSelectedTransaction));
        deleteMonthButton.addActionListener(onClick(this::deleteSelectedMonth));
        deleteYearButton.addActionListener(onClick(this::deleteSelectedYear));
        monthTabs.addChangeListener(onChange(() -> {
            deleteMonthButton.setEnabled(monthTabs.getTabCount() > 0);
            applyAllTransactionFilters();
        }));
        exportButton.addActionListener(onClick(this::openExportDialog));
        
        // Live search listeners
        txnSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() { applyAllTransactionFilters(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e){ update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ update(); }
        });
        txnSearchColumn.addActionListener(onClick(this::applyAllTransactionFilters));
        
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
    addBankBtn.addActionListener(onClick(this::openBankAccountDialog));
    JButton deleteBankBtn = new JButton("Delete Selected Account");
    deleteBankBtn.addActionListener(onClick(this::deleteSelectedAccount));
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
    addDepositBtn.addActionListener(onClick(() -> openAddEditDepositDialog(null)));
    deleteDepositBtn.addActionListener(onClick(this::deleteSelectedDeposit));
    depositRecycleBinBtn.addActionListener(onClick(this::openDepositRecycleBin));
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
    addInvestmentBtn.addActionListener(onClick(() -> openAddEditInvestmentDialog(null)));
    deleteInvestmentBtn.addActionListener(onClick(this::deleteSelectedInvestment));
    investmentRecycleBinBtn.addActionListener(onClick(this::openInvestmentRecycleBin));

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

addTaxProfileBtn.addActionListener(onClick(() -> openAddEditTaxProfileDialog(null))); // New method
deleteTaxProfileBtn.addActionListener(onClick(this::deleteSelectedTaxProfile)); // New method

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

addLoanBtn.addActionListener(onClick(() -> openAddEditLoanDialog(null))); // New method
deleteLoanBtn.addActionListener(onClick(this::deleteSelectedLoan)); // New method
loanRecycleBinBtn.addActionListener(onClick(this::openLoanRecycleBin)); // New method

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

addCardBtn.addActionListener(onClick(() -> openAddEditCardDialog(null))); // Method stub below
deleteCardBtn.addActionListener(onClick(this::deleteSelectedCard)); // Method to be added below
cardRecycleBinBtn.addActionListener(onClick(this::openCardRecycleBin)); // Method stub below

// Load initial data
refreshCards(); // Method to be added below

    // =========================================================
    // ===         SUMMARY & REPORTS PANEL                  ===
    // =========================================================
    initSummaryTab(tabs);


    
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
            loadSummaryYearChoices();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading years: " + e.getMessage());
        }
    }

    private void refreshTransactions() {
        monthTabs.removeAll();
        String selectedYear = (String) yearComboBox.getSelectedItem();
        if (selectedYear == null) {
            return;
        }
        try {
            Map<String, List<Transaction>> groupedData = manager.getTransactionsGroupedByMonth(selectedYear);
            for (String monthYear : groupedData.keySet()) {
                DefaultTableModel monthModel = createTransactionsTableModel();
                List<Transaction> txs = groupedData.get(monthYear);

                double monthIncome = 0d;
                double monthExpense = 0d;
                for (Transaction t : txs) {
                    if ("Income".equalsIgnoreCase(t.getType())) {
                        monthIncome += t.getAmount();
                    } else if ("Expense".equalsIgnoreCase(t.getType())) {
                        monthExpense += t.getAmount();
                    }
                }

                int serial = 1;
                for (Transaction t : txs) {
                    monthModel.addRow(new Object[]{
                        serial++,
                        t.getDate(),
                        t.getTimestamp(),
                        t.getDay(),
                        t.getPaymentMethod(),
                        t.getCategory(),
                        t.getType(),
                        t.getPayee(),
                        t.getDescription(),
                        t.getAmount(),
                        monthIncome,
                        monthExpense
                    });
                }

                JTable monthTable = new JTable(monthModel);
                javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(monthModel);
                monthTable.setRowSorter(sorter);
                monthTable.putClientProperty("sorter", sorter);
                monthTable.putClientProperty("monthYear", monthYear);
                monthTable.setFillsViewportHeight(true);
                monthTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                JScrollPane scrollPane = new JScrollPane(monthTable);
                scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                String tabTitle = getMonthName(monthYear);
                monthTabs.addTab(tabTitle, scrollPane);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
        }
        deleteMonthButton.setEnabled(monthTabs.getTabCount() > 0);
        applyAllTransactionFilters();
    }

    private void applyAllTransactionFilters() {
        if (monthTabs == null) {
            return;
        }
        JScrollPane currentScrollPane = (JScrollPane) monthTabs.getSelectedComponent();
        if (currentScrollPane == null) {
            return;
        }
        JTable table = (JTable) currentScrollPane.getViewport().getView();
        if (table == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = (javax.swing.table.TableRowSorter<DefaultTableModel>) table.getClientProperty("sorter");
        if (sorter == null) {
            return;
        }

        String generalQuery = txnSearchField != null ? txnSearchField.getText() : "";
        String selectedColumn = txnSearchColumn != null ? (String) txnSearchColumn.getSelectedItem() : ALL_COLUMNS_OPTION;
    javax.swing.RowFilter<DefaultTableModel, Object> generalFilter = buildGeneralRowFilter(generalQuery, selectedColumn);
        sorter.setRowFilter(generalFilter);
    }

    private static javax.swing.RowFilter<DefaultTableModel, Object> buildGeneralRowFilter(String query, String columnLabel) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (columnLabel == null || columnLabel.isBlank()) {
            columnLabel = ALL_COLUMNS_OPTION;
        }

        if (ALL_COLUMNS_OPTION.equals(columnLabel)) {
            java.util.List<javax.swing.RowFilter<DefaultTableModel, Object>> subFilters = new java.util.ArrayList<>();
            for (int i = 0; i < TRANSACTION_TABLE_COLUMNS.length; i++) {
                javax.swing.RowFilter<DefaultTableModel, Object> columnFilter = buildColumnRowFilter(i, trimmed);
                if (columnFilter != null) {
                    subFilters.add(columnFilter);
                }
            }
            return subFilters.isEmpty() ? null : javax.swing.RowFilter.orFilter(subFilters);
        }

        int targetCol = findTransactionColumnIndex(columnLabel);
        if (targetCol < 0) {
            return null;
        }
        return buildColumnRowFilter(targetCol, trimmed);
    }

    private static javax.swing.RowFilter<DefaultTableModel, Object> buildColumnRowFilter(int columnIndex, String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        final String queryLower = trimmed.toLowerCase();
        return new javax.swing.RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String cellText = stringValue(entry.getValue(columnIndex)).toLowerCase();
                return cellText.contains(queryLower);
            }
        };
    }

    private static DefaultTableModel createTransactionsTableModel() {
        return new DefaultTableModel(TRANSACTION_TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Integer.class;
                }
                if (columnIndex == TRANSACTION_COLUMN_AMOUNT_INDEX
                        || columnIndex == TRANSACTION_COLUMN_MONTHLY_INCOME_TOTAL_INDEX
                        || columnIndex == TRANSACTION_COLUMN_MONTHLY_EXPENSE_TOTAL_INDEX) {
                    return Double.class;
                }
                return String.class;
            }
        };
    }

    private static String[] buildTransactionSearchOptions() {
        String[] options = new String[TRANSACTION_TABLE_COLUMNS.length + 1];
        options[0] = ALL_COLUMNS_OPTION;
        System.arraycopy(TRANSACTION_TABLE_COLUMNS, 0, options, 1, TRANSACTION_TABLE_COLUMNS.length);
        return options;
    }

    private static int findTransactionColumnIndex(String columnLabel) {
        for (int i = 0; i < TRANSACTION_TABLE_COLUMNS.length; i++) {
            if (TRANSACTION_TABLE_COLUMNS[i].equals(columnLabel)) {
                return i;
            }
        }
        return -1;
    }

    private static String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private static int intValue(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String text = stringValue(value);
        if (text.isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static double doubleValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        String text = stringValue(value);
        if (text.isEmpty()) {
            return 0d;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            return 0d;
        }
    }

    private static Transaction buildTransactionFromModelRow(DefaultTableModel model, int rowIndex) {
        int serial = intValue(model.getValueAt(rowIndex, 0), rowIndex + 1);
        String date = stringValue(model.getValueAt(rowIndex, 1));
        String timestamp = stringValue(model.getValueAt(rowIndex, 2));
        String day = stringValue(model.getValueAt(rowIndex, 3));
        String paymentMethod = stringValue(model.getValueAt(rowIndex, 4));
        String category = stringValue(model.getValueAt(rowIndex, 5));
        String type = stringValue(model.getValueAt(rowIndex, 6));
        String payee = stringValue(model.getValueAt(rowIndex, 7));
        String description = stringValue(model.getValueAt(rowIndex, 8));
    double amount = doubleValue(model.getValueAt(rowIndex, TRANSACTION_COLUMN_AMOUNT_INDEX));
        return new Transaction(serial, date, timestamp, day, category, type, amount, description, paymentMethod, payee);
    }

    private static List<Transaction> copyWithSequentialIds(List<Transaction> source) {
        if (source == null) {
            return new ArrayList<>();
        }
        List<Transaction> copy = new ArrayList<>(source.size());
        int serial = 1;
        for (Transaction original : source) {
            copy.add(new Transaction(
                    serial++,
                    original.getDate(),
                    original.getTimestamp(),
                    original.getDay(),
                    original.getCategory(),
                    original.getType(),
                    original.getAmount(),
                    original.getDescription(),
                    original.getPaymentMethod(),
                    original.getPayee()
            ));
        }
        return copy;
    }

    private void initSummaryTab(JTabbedPane tabs) {
        JPanel summaryPanel = new JPanel(new BorderLayout(10, 10));
        summaryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        summaryCompanyField = new JTextField(20);
        summaryDesignationField = new JTextField(18);
        summaryHolderField = new JTextField(20);
        summaryYearCombo = new JComboBox<>();
        summaryRefreshButton = new JButton("Generate Summary");
        summaryExportCsvButton = new JButton("Export CSV");
        summaryExportPdfButton = new JButton("Export PDF");
        summaryExportCsvButton.setEnabled(false);
        summaryExportPdfButton.setEnabled(false);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; inputPanel.add(new JLabel("Company:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(summaryCompanyField, gbc);

        gbc.gridx = 0; gbc.gridy = row; inputPanel.add(new JLabel("Designation:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(summaryDesignationField, gbc);

        gbc.gridx = 0; gbc.gridy = row; inputPanel.add(new JLabel("Report Holder:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(summaryHolderField, gbc);

        gbc.gridx = 0; gbc.gridy = row; inputPanel.add(new JLabel("Transaction Year:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(summaryYearCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(summaryRefreshButton);
        buttonPanel.add(summaryExportCsvButton);
        buttonPanel.add(summaryExportPdfButton);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        inputPanel.add(buttonPanel, gbc);

        summaryOverviewArea = new JTextArea(24, 80);
        summaryOverviewArea.setEditable(false);
        summaryOverviewArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        summaryOverviewArea.setLineWrap(true);
        summaryOverviewArea.setWrapStyleWord(true);
        summaryOverviewArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        summaryPanel.add(inputPanel, BorderLayout.NORTH);
        summaryPanel.add(new JScrollPane(summaryOverviewArea), BorderLayout.CENTER);

        loadSummaryYearChoices();

    summaryRefreshButton.addActionListener(onClick(this::regenerateSummary));
    summaryExportCsvButton.addActionListener(onClick(this::exportSummaryAsCsv));
    summaryExportPdfButton.addActionListener(onClick(this::exportSummaryAsPdf));
    summaryYearCombo.addActionListener(onClick(() -> { if (summaryTabInitialized) regenerateSummary(); }));

        tabs.addTab("Summary & Reports", summaryPanel);

        summaryTabInitialized = true;
        regenerateSummary();
    }
