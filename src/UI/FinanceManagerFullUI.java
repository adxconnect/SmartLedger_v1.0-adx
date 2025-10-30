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
import src.SummaryData;
// Modern UI imports
import src.UI.ModernTheme;
import src.UI.LogoPanel;
import src.UI.ModernIcons;
import src.UI.ModernIcons.IconType;
// We'll also add placeholders for the dialogs
import src.UI.AddEditLoanDialog;
import src.UI.LoanRecycleBinDialog;
import src.auth.SessionContext;
import src.auth.UserPreferencesCache;

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
    private FinanceManager manager;

    // --- Transaction Tab Variables ---
    private JTabbedPane monthTabs;
    private JComboBox<String> yearComboBox;
    private JButton deleteMonthButton;
    private JTextField txnSearchField; // search text for transactions
    private JComboBox<String> txnSearchColumn; // column selector for transactions
    private JLabel incomeValueLabel; // Label to show total income
    private JLabel expenseValueLabel; // Label to show total expense
    private JLabel balanceValueLabel; // Label to show net balance

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
    private AutoCompleteTextField summaryCompanyField;
    private AutoCompleteTextField summaryDesignationField;
    private AutoCompleteTextField summaryHolderField;
    private JComboBox<String> summaryYearCombo;
    private JButton summaryRefreshButton;
    private JButton summaryExportCsvButton;
    private JButton summaryExportPdfButton;
    private JTextArea summaryOverviewArea;
    private SummaryData currentSummarySnapshot;
    private boolean summaryTabInitialized = false;
    private static final DateTimeFormatter SUMMARY_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");


    public FinanceManagerFullUI() {
        setTitle("Finance Manager - MySQL Edition");
        setSize(900, 600);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Prevent auto-logout on close
    // Apply modern background to the frame
    getContentPane().setBackground(ModernTheme.BACKGROUND);

        // Handle window closing - just hide, don't logout
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                    FinanceManagerFullUI.this,
                    "Do you want to logout or just minimize?\n\nYes = Logout\nNo = Minimize\nCancel = Stay",
                    "Exit Options",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (choice == JOptionPane.YES_OPTION) {
                    performLogout();
                } else if (choice == JOptionPane.NO_OPTION) {
                    setState(java.awt.Frame.ICONIFIED); // Minimize
                }
                // Cancel or close dialog = do nothing, stay in app
            }
        });

        try {
            manager = new FinanceManager(); // Connect DB
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "MySQL connection failed: " + e.getMessage());
            System.exit(1);
        }

    // Main container with BorderLayout to add logout button at top
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBackground(ModernTheme.BACKGROUND);
    add(mainPanel);

    // Top header with logo, user info and logout
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setBorder(new EmptyBorder(12, 16, 12, 16));
    topPanel.setBackground(ModernTheme.SURFACE);
        
        // Left side - User Info
    JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
    userInfoPanel.setBackground(ModernTheme.SURFACE);
    // App logo on the very left
    JPanel logoWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    logoWrap.setOpaque(false);
    logoWrap.add(LogoPanel.createHeaderLogo());
    userInfoPanel.add(logoWrap);
    userInfoPanel.add(Box.createHorizontalStrut(12));
        
        // Get current account details
        src.auth.Account currentAccount = SessionContext.getCurrentAccount();
        if (currentAccount != null) {
            // Account name with icon
            JLabel nameLabel = new JLabel(currentAccount.getAccountName());
            nameLabel.setFont(ModernTheme.FONT_HEADER);
            nameLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            userInfoPanel.add(nameLabel);
            
            // Account type badge
            String accountTypeText = currentAccount.getAccountType() == src.auth.Account.AccountType.BUSINESS 
                ? "Business" : "Personal";
            JLabel typeLabel = new JLabel(accountTypeText);
            typeLabel.setFont(ModernTheme.FONT_SMALL);
            typeLabel.setForeground(Color.WHITE);
            typeLabel.setBackground(currentAccount.getAccountType() == src.auth.Account.AccountType.BUSINESS 
                ? ModernTheme.PRIMARY : ModernTheme.SUCCESS);
            typeLabel.setOpaque(true);
            typeLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            userInfoPanel.add(typeLabel);
            
            // Email if available
            if (currentAccount.getEmail() != null && !currentAccount.getEmail().trim().isEmpty()) {
                JLabel emailLabel = new JLabel(currentAccount.getEmail());
                emailLabel.setFont(ModernTheme.FONT_SMALL);
                emailLabel.setForeground(ModernTheme.TEXT_SECONDARY);
                userInfoPanel.add(emailLabel);
            }
            
            // Phone if available
            if (currentAccount.getPhone() != null && !currentAccount.getPhone().trim().isEmpty()) {
                JLabel phoneLabel = new JLabel(currentAccount.getPhone());
                phoneLabel.setFont(ModernTheme.FONT_SMALL);
                phoneLabel.setForeground(ModernTheme.TEXT_SECONDARY);
                userInfoPanel.add(phoneLabel);
            }
        }
        
        topPanel.add(userInfoPanel, BorderLayout.CENTER);
        
        // Right side - Dark Mode & Logout buttons
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        logoutPanel.setBackground(ModernTheme.SURFACE);
        
        // Dark mode toggle button
        JButton darkModeBtn = ModernTheme.createDarkModeToggleButton();
        darkModeBtn.addActionListener(e -> {
            ModernTheme.toggleDarkMode();
            refreshUITheme();
        });
        logoutPanel.add(darkModeBtn);
        
        // Logout button
        JButton logoutBtn = ModernTheme.createDangerButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(110, 36));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                performLogout();
            }
        });
        
        logoutPanel.add(logoutBtn);
        topPanel.add(logoutPanel, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // JTabbedPane setup
    JTabbedPane tabs = new JTabbedPane();
    styleTabbedPane(tabs);
        mainPanel.add(tabs, BorderLayout.CENTER);

        // =========================================================
        // ===         TRANSACTIONS PANEL                      ===
        // =========================================================
        JPanel tPanel = new JPanel(new BorderLayout(5, 5));
        tPanel.setBackground(ModernTheme.BACKGROUND);
        tPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
    JPanel tTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tTopPanel.setBackground(ModernTheme.SURFACE);
        yearComboBox = new JComboBox<>();
        ModernTheme.styleComboBox(yearComboBox);
        tTopPanel.add(new JLabel("Select Year:"));
        tTopPanel.add(yearComboBox);
        JButton deleteYearButton = ModernTheme.createDangerButton("Delete All of Selected Year");
        tTopPanel.add(deleteYearButton);
        deleteMonthButton = ModernTheme.createDangerButton("Delete Selected Month");
        deleteMonthButton.setEnabled(false);
        tTopPanel.add(deleteMonthButton);
    // --- Search controls for Transactions ---
    tTopPanel.add(Box.createHorizontalStrut(10));
    tTopPanel.add(new JLabel("Search:"));
    txnSearchField = new JTextField(18);
    ModernTheme.styleTextField(txnSearchField);
    tTopPanel.add(txnSearchField);
    String[] txnCols = new String[]{"All Columns","S.No.","Date","Timestamp","Day","Payment Method","Category","Type","Payee","Description","Amount"};
    txnSearchColumn = new JComboBox<>(txnCols);
    ModernTheme.styleComboBox(txnSearchColumn);
    txnSearchColumn.setSelectedIndex(0);
    tTopPanel.add(txnSearchColumn);
        
        // Create a container for top panel and summary cards
    JPanel topContainer = new JPanel(new BorderLayout());
    topContainer.setOpaque(false);
        topContainer.add(tTopPanel, BorderLayout.NORTH);
        
        // Add Income/Expense Summary Cards Panel
    JPanel summaryCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
    summaryCardsPanel.setBackground(ModernTheme.BACKGROUND);
        summaryCardsPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        // Create Income Card
        incomeValueLabel = new JLabel("₹0.00");
        JPanel incomeCard = createSummaryCard("Total Income", incomeValueLabel, new Color(46, 204, 113), new Color(39, 174, 96));
        summaryCardsPanel.add(incomeCard);
        
        // Create Expense Card
        expenseValueLabel = new JLabel("₹0.00");
        JPanel expenseCard = createSummaryCard("Total Expense", expenseValueLabel, new Color(231, 76, 60), new Color(192, 57, 43));
        summaryCardsPanel.add(expenseCard);
        
        // Create Balance Card
        balanceValueLabel = new JLabel("₹0.00");
        JPanel balanceCard = createSummaryCard("Net Balance", balanceValueLabel, new Color(52, 152, 219), new Color(41, 128, 185));
        summaryCardsPanel.add(balanceCard);
        
        topContainer.add(summaryCardsPanel, BorderLayout.CENTER);
        tPanel.add(topContainer, BorderLayout.NORTH);
        
        monthTabs = new JTabbedPane();
        tPanel.add(monthTabs, BorderLayout.CENTER);
    JPanel tBottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    tBottomPanel.setBackground(ModernTheme.SURFACE);
    JButton addTxnBtn = ModernTheme.createPrimaryButton("Add Transaction");
    addTxnBtn.setIcon(ModernIcons.create(IconType.ADD, ModernTheme.TEXT_WHITE, 16));
    addTxnBtn.setIconTextGap(8);
    JButton deleteTxnBtn = ModernTheme.createDangerButton("Delete Selected Transaction");
    deleteTxnBtn.setIcon(ModernIcons.create(IconType.DELETE, ModernTheme.TEXT_WHITE, 16));
    deleteTxnBtn.setIconTextGap(8);
    JButton selectAllTxnBtn = ModernTheme.createSecondaryButton("Select All");
    JButton recycleBinBtn = ModernTheme.createSecondaryButton("Recycle Bin");
    recycleBinBtn.setIcon(ModernIcons.create(IconType.RECYCLE, ModernTheme.TEXT_PRIMARY, 16));
    recycleBinBtn.setIconTextGap(8);
    JButton exportButton = ModernTheme.createSecondaryButton("Export to File...");
    exportButton.setIcon(ModernIcons.create(IconType.EXPORT, ModernTheme.TEXT_PRIMARY, 16));
    exportButton.setIconTextGap(8);
        tTopPanel.add(exportButton);
        tBottomPanel.add(addTxnBtn);
        tBottomPanel.add(deleteTxnBtn);
        tBottomPanel.add(selectAllTxnBtn);
        tBottomPanel.add(recycleBinBtn);
        tPanel.add(tBottomPanel, BorderLayout.SOUTH);
        tabs.addTab("Transactions", tPanel);
        // Action Listeners
        recycleBinBtn.addActionListener(e -> openRecycleBin()); // Connect button
        yearComboBox.addActionListener(e -> refreshTransactions());
        addTxnBtn.addActionListener(e -> openTransactionDialog());
        deleteTxnBtn.addActionListener(e -> deleteSelectedTransaction());
        selectAllTxnBtn.addActionListener(e -> {
            JScrollPane currentScrollPane = (JScrollPane) monthTabs.getSelectedComponent();
            if (currentScrollPane == null) return;
            JTable currentTable = (JTable) currentScrollPane.getViewport().getView();
            int rowCount = currentTable.getRowCount();
            if (rowCount > 0) {
                currentTable.setRowSelectionInterval(0, rowCount - 1);
            }
        });
        deleteMonthButton.addActionListener(e -> deleteSelectedMonth());
        deleteYearButton.addActionListener(e -> deleteSelectedYear());
        monthTabs.addChangeListener(e -> {
            deleteMonthButton.setEnabled(monthTabs.getTabCount() > 0);
            applyTransactionSearchFilter(txnSearchField.getText(), (String) txnSearchColumn.getSelectedItem());
            updateSummaryCards(); // Update cards when month tab changes
        });
        exportButton.addActionListener(e -> openExportDialog());
        // Live search listeners
        txnSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() { applyTransactionSearchFilter(txnSearchField.getText(), (String) txnSearchColumn.getSelectedItem()); }
            public void insertUpdate(javax.swing.event.DocumentEvent e){ update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ update(); }
        });
        txnSearchColumn.addActionListener(e -> applyTransactionSearchFilter(txnSearchField.getText(), (String) txnSearchColumn.getSelectedItem()));
        // Initial Load
        loadYearFilter();
        refreshTransactions();


        // =========================================================
        // ===         BANK ACCOUNTS PANEL                     ===
        // =========================================================
    JPanel bPanel = new JPanel(new BorderLayout());
    bPanel.setBackground(ModernTheme.BACKGROUND);
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setDividerLocation(200);
    splitPane.setContinuousLayout(true);
    splitPane.setDividerSize(4);
    splitPane.setOpaque(false);
    bankListModel = new DefaultListModel<>();
    bankAccountList = new JList<>(bankListModel);
    styleList(bankAccountList);
    bankAccountList.setBorder(new EmptyBorder(5, 5, 5, 5));
    bankDetailPanel = new JPanel(new BorderLayout());
    bankDetailPanel.setBackground(ModernTheme.SURFACE);
    bankDetailPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
    JScrollPane bankListScroll = new JScrollPane(bankAccountList);
    styleModernScrollBar(bankListScroll);
    splitPane.setLeftComponent(bankListScroll);
    splitPane.setRightComponent(bankDetailPanel);
    bPanel.add(splitPane, BorderLayout.CENTER);
    JPanel bankButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    bankButtonPanel.setBackground(ModernTheme.SURFACE);
    JButton addBankBtn = ModernTheme.createPrimaryButton("Add New Account");
    addBankBtn.setIcon(ModernIcons.create(IconType.ADD, ModernTheme.TEXT_WHITE, 16));
    addBankBtn.setIconTextGap(8);
    addBankBtn.addActionListener(e -> openBankAccountDialog());
    JButton deleteBankBtn = ModernTheme.createDangerButton("Delete Selected Account");
    deleteBankBtn.setIcon(ModernIcons.create(IconType.DELETE, ModernTheme.TEXT_WHITE, 16));
    deleteBankBtn.setIconTextGap(8);
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
        dPanel.setBackground(ModernTheme.BACKGROUND);
        JSplitPane depositSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        depositSplitPane.setDividerLocation(200);
        depositSplitPane.setContinuousLayout(true);
        depositSplitPane.setDividerSize(4);
        depositSplitPane.setOpaque(false);
        depositListModel = new DefaultListModel<>();
        depositList = new JList<>(depositListModel);
        styleList(depositList);
        depositList.setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane depositListScroll = new JScrollPane(depositList);
        styleModernScrollBar(depositListScroll);
        depositSplitPane.setLeftComponent(depositListScroll);
        depositDetailPanel = new JPanel(new BorderLayout());
        depositDetailPanel.setBackground(ModernTheme.SURFACE);
        depositDetailPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        depositDetailPanel.add(new JLabel("Select a deposit to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
        depositSplitPane.setRightComponent(depositDetailPanel);
        dPanel.add(depositSplitPane, BorderLayout.CENTER);
        JPanel depositButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        depositButtonPanel.setBackground(ModernTheme.SURFACE);
        JButton addDepositBtn = ModernTheme.createPrimaryButton("Add New Deposit");
        addDepositBtn.setIcon(ModernIcons.create(IconType.ADD, ModernTheme.TEXT_WHITE, 16));
        addDepositBtn.setIconTextGap(8);
        JButton deleteDepositBtn = ModernTheme.createDangerButton("Delete Selected Deposit");
        deleteDepositBtn.setIcon(ModernIcons.create(IconType.DELETE, ModernTheme.TEXT_WHITE, 16));
        deleteDepositBtn.setIconTextGap(8);
        JButton depositRecycleBinBtn = ModernTheme.createSecondaryButton("Deposit Recycle Bin");
        depositRecycleBinBtn.setIcon(ModernIcons.create(IconType.RECYCLE, ModernTheme.TEXT_PRIMARY, 16));
        depositRecycleBinBtn.setIconTextGap(8);
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
        iPanel.setBackground(ModernTheme.BACKGROUND);
        JSplitPane investmentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        investmentSplitPane.setDividerLocation(220);
        investmentSplitPane.setContinuousLayout(true);
        investmentSplitPane.setDividerSize(4);
        investmentSplitPane.setOpaque(false);
        investmentListModel = new DefaultListModel<>();
        investmentList = new JList<>(investmentListModel);
        styleList(investmentList);
        investmentList.setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane investmentListScroll = new JScrollPane(investmentList);
        styleModernScrollBar(investmentListScroll);
        investmentSplitPane.setLeftComponent(investmentListScroll);

        investmentDetailPanel = new JPanel(new BorderLayout());
        investmentDetailPanel.setBackground(ModernTheme.SURFACE);
        investmentDetailPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        investmentDetailPanel.add(new JLabel("Select an investment to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
        investmentSplitPane.setRightComponent(investmentDetailPanel);

        iPanel.add(investmentSplitPane, BorderLayout.CENTER);

        JPanel investmentButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        investmentButtonPanel.setBackground(ModernTheme.SURFACE);
        JButton addInvestmentBtn = ModernTheme.createPrimaryButton("Add Investment");
        addInvestmentBtn.setIcon(ModernIcons.create(IconType.ADD, ModernTheme.TEXT_WHITE, 16));
        addInvestmentBtn.setIconTextGap(8);
        JButton deleteInvestmentBtn = ModernTheme.createDangerButton("Delete Selected");
        deleteInvestmentBtn.setIcon(ModernIcons.create(IconType.DELETE, ModernTheme.TEXT_WHITE, 16));
        deleteInvestmentBtn.setIconTextGap(8);
        JButton investmentRecycleBinBtn = ModernTheme.createSecondaryButton("Investment Recycle Bin");
        investmentRecycleBinBtn.setIcon(ModernIcons.create(IconType.RECYCLE, ModernTheme.TEXT_PRIMARY, 16));
        investmentRecycleBinBtn.setIconTextGap(8);
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
taxPanel.setBackground(ModernTheme.BACKGROUND);

// --- Split Pane ---
JSplitPane taxSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
taxSplitPane.setDividerLocation(220); // Adjust as needed
taxSplitPane.setContinuousLayout(true);
taxSplitPane.setDividerSize(4);
taxSplitPane.setOpaque(false);

// --- Left Side: List of Tax Profiles ---
taxProfileListModel = new DefaultListModel<>();
taxProfileList = new JList<>(taxProfileListModel);
styleList(taxProfileList);
taxProfileList.setBorder(new EmptyBorder(5, 5, 5, 5));
JScrollPane taxListScroll = new JScrollPane(taxProfileList);
styleModernScrollBar(taxListScroll);
taxSplitPane.setLeftComponent(taxListScroll);

// --- Right Side: Detail Panel ---
taxDetailPanel = new JPanel(new BorderLayout());
taxDetailPanel.setBackground(ModernTheme.SURFACE);
taxDetailPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
taxDetailPanel.add(new JLabel("Select a tax profile to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
taxSplitPane.setRightComponent(taxDetailPanel);

taxPanel.add(taxSplitPane, BorderLayout.CENTER);

// --- Bottom Button Panel ---
JPanel taxButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
taxButtonPanel.setBackground(ModernTheme.SURFACE);
JButton addTaxProfileBtn = ModernTheme.createPrimaryButton("Add New Tax Profile");
addTaxProfileBtn.setIcon(ModernIcons.create(IconType.ADD, ModernTheme.TEXT_WHITE, 16));
addTaxProfileBtn.setIconTextGap(8);
JButton deleteTaxProfileBtn = ModernTheme.createDangerButton("Delete Selected Profile");
deleteTaxProfileBtn.setIcon(ModernIcons.create(IconType.DELETE, ModernTheme.TEXT_WHITE, 16));
deleteTaxProfileBtn.setIconTextGap(8);

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
lPanel.setBackground(ModernTheme.BACKGROUND);

// --- Split Pane ---
JSplitPane loanSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
loanSplitPane.setDividerLocation(220); // Adjust as needed
loanSplitPane.setContinuousLayout(true);
loanSplitPane.setDividerSize(4);
loanSplitPane.setOpaque(false);

// --- Left Side: List of Loans ---
loanListModel = new DefaultListModel<>();
loanList = new JList<>(loanListModel);
styleList(loanList);
loanList.setBorder(new EmptyBorder(5, 5, 5, 5));
JScrollPane loanListScroll = new JScrollPane(loanList);
styleModernScrollBar(loanListScroll);
loanSplitPane.setLeftComponent(loanListScroll);

// --- Right Side: Detail Panel ---
loanDetailPanel = new JPanel(new BorderLayout());
loanDetailPanel.setBackground(ModernTheme.SURFACE);
loanDetailPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
loanDetailPanel.add(new JLabel("Select a loan to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
loanSplitPane.setRightComponent(loanDetailPanel);

lPanel.add(loanSplitPane, BorderLayout.CENTER);

// --- Bottom Button Panel ---
JPanel loanButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
loanButtonPanel.setBackground(ModernTheme.SURFACE);
JButton addLoanBtn = ModernTheme.createPrimaryButton("Add New Loan");
addLoanBtn.setIcon(ModernIcons.create(IconType.ADD, ModernTheme.TEXT_WHITE, 16));
addLoanBtn.setIconTextGap(8);
JButton deleteLoanBtn = ModernTheme.createDangerButton("Delete Selected Loan");
deleteLoanBtn.setIcon(ModernIcons.create(IconType.DELETE, ModernTheme.TEXT_WHITE, 16));
deleteLoanBtn.setIconTextGap(8);
JButton loanRecycleBinBtn = ModernTheme.createSecondaryButton("Loan Recycle Bin");
loanRecycleBinBtn.setIcon(ModernIcons.create(IconType.RECYCLE, ModernTheme.TEXT_PRIMARY, 16));
loanRecycleBinBtn.setIconTextGap(8);

loanButtonPanel.add(addLoanBtn);
loanButtonPanel.add(deleteLoanBtn);
loanButtonPanel.add(loanRecycleBinBtn);
lPanel.add(loanButtonPanel, BorderLayout.SOUTH);

tabs.addTab("Loans / EMI", lPanel); // Add the new tab// --- Action Listeners ---
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
cPanel.setBackground(ModernTheme.BACKGROUND);

// --- Split Pane ---
JSplitPane cardSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
cardSplitPane.setDividerLocation(220); // Adjust width for card names
cardSplitPane.setContinuousLayout(true);
cardSplitPane.setDividerSize(4);
cardSplitPane.setOpaque(false);

// --- Left Side: List of Cards ---
cardListModel = new DefaultListModel<>();
cardList = new JList<>(cardListModel);
styleList(cardList);
cardList.setBorder(new EmptyBorder(5, 5, 5, 5));
JScrollPane cardListScroll = new JScrollPane(cardList);
styleModernScrollBar(cardListScroll);
cardSplitPane.setLeftComponent(cardListScroll);

// --- Right Side: Detail Panel ---
cardDetailPanel = new JPanel(new BorderLayout());
cardDetailPanel.setBackground(ModernTheme.SURFACE);
cardDetailPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
cardDetailPanel.add(new JLabel("Select a card to view details.", SwingConstants.CENTER), BorderLayout.CENTER);
cardSplitPane.setRightComponent(cardDetailPanel);

cPanel.add(cardSplitPane, BorderLayout.CENTER);

// --- Bottom Button Panel ---
JPanel cardButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
cardButtonPanel.setBackground(ModernTheme.SURFACE);
JButton addCardBtn = ModernTheme.createPrimaryButton("Add New Card");
addCardBtn.setIcon(ModernIcons.create(IconType.ADD, ModernTheme.TEXT_WHITE, 16));
addCardBtn.setIconTextGap(8);
JButton deleteCardBtn = ModernTheme.createDangerButton("Delete Selected Card");
deleteCardBtn.setIcon(ModernIcons.create(IconType.DELETE, ModernTheme.TEXT_WHITE, 16));
deleteCardBtn.setIconTextGap(8);
JButton cardRecycleBinBtn = ModernTheme.createSecondaryButton("Card Recycle Bin");
cardRecycleBinBtn.setIcon(ModernIcons.create(IconType.RECYCLE, ModernTheme.TEXT_PRIMARY, 16));
cardRecycleBinBtn.setIconTextGap(8);

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
                String[] tcols = {"S.No.", "Date", "Timestamp", "Day", "Payment Method", "Category", "Type", "Payee", "Description", "Amount", "ID"};
            int serialNumber = 1; // Start serial number from 1 for each user
            for (String monthYear : groupedData.keySet()) {
                    DefaultTableModel monthModel = new DefaultTableModel(tcols, 0) {
                        @Override
                        public Class<?> getColumnClass(int column) {
                            if (column == 0 || column == 10) return Integer.class;
                            if (column == 9) return Double.class;
                            return String.class;
                        }
                    };
                List<Transaction> txs = groupedData.get(monthYear);
                
                // Calculate month totals
                double monthIncome = 0.0;
                double monthExpense = 0.0;
                
                for (Transaction t : txs) {
                        monthModel.addRow(new Object[]{serialNumber++, t.getDate(), t.getTimestamp(), t.getDay(), t.getPaymentMethod(), t.getCategory(), t.getType(), t.getPayee(), t.getDescription(), t.getAmount(), t.getId()});
                        
                        // Calculate totals for this month
                        if ("Income".equalsIgnoreCase(t.getType())) {
                            monthIncome += t.getAmount();
                        } else if ("Expense".equalsIgnoreCase(t.getType())) {
                            monthExpense += t.getAmount();
                        }
                }
                JTable monthTable = new JTable(monthModel);
                // Apply modern table styling
                ModernTheme.styleTable(monthTable);
                
                // Add grid lines between columns for modern look
                monthTable.setShowGrid(true);
                monthTable.setGridColor(new Color(220, 220, 220)); // Light gray grid lines
                monthTable.setIntercellSpacing(new Dimension(1, 1)); // 1px spacing for grid lines
                
                // Hide the ID column (last column)
                monthTable.getColumnModel().getColumn(10).setMinWidth(0);
                monthTable.getColumnModel().getColumn(10).setMaxWidth(0);
                monthTable.getColumnModel().getColumn(10).setWidth(0);
                
                // Enable sorting and filtering per-month table
                javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(monthModel);
                monthTable.setRowSorter(sorter);
                monthTable.putClientProperty("sorter", sorter);
                monthTable.putClientProperty("monthYear", monthYear);
                
                // Store month totals in the scroll pane's client properties
                JScrollPane scrollPane = new JScrollPane(monthTable);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                styleModernScrollBar(scrollPane);
                scrollPane.putClientProperty("monthIncome", monthIncome);
                scrollPane.putClientProperty("monthExpense", monthExpense);
                
                String tabTitle = getMonthName(monthYear);
                monthTabs.addTab(tabTitle, scrollPane);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
        }
        
        deleteMonthButton.setEnabled(monthTabs.getTabCount() > 0);
        
        // Update summary cards for the first month (or selected tab)
        updateSummaryCards();
        // Re-apply any active search filter after refresh
        if (txnSearchField != null && txnSearchColumn != null) {
            applyTransactionSearchFilter(txnSearchField.getText(), (String) txnSearchColumn.getSelectedItem());
        }
    }

    // Applies a RowFilter to the currently selected month's table based on the text and target column
    private void applyTransactionSearchFilter(String query, String columnLabel) {
        JScrollPane currentScrollPane = (JScrollPane) monthTabs.getSelectedComponent();
        if (currentScrollPane == null) return;
        JTable table = (JTable) currentScrollPane.getViewport().getView();
        @SuppressWarnings("unchecked")
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = (javax.swing.table.TableRowSorter<DefaultTableModel>) table.getClientProperty("sorter");
        if (sorter == null) return;

        if (query == null) query = "";
        String q = query.trim();
        if (q.isEmpty()) { sorter.setRowFilter(null); return; }

    // Case-insensitive contains match
    javax.swing.RowFilter<DefaultTableModel, Object> filter;

        // Column mapping to model indices
        String[] labels = {"S.No.", "Date", "Timestamp", "Day", "Payment Method", "Category", "Type", "Payee", "Description", "Amount"};
        if ("All Columns".equals(columnLabel)) {
            java.util.List<javax.swing.RowFilter<DefaultTableModel, Object>> filters = new java.util.ArrayList<>();
            for (int i = 0; i < labels.length; i++) {
                final int col = i;
                filters.add(new javax.swing.RowFilter<DefaultTableModel, Object>() {
                    public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                        Object v = entry.getValue(col);
                        return v != null && v.toString().toLowerCase().contains(q.toLowerCase());
                    }
                });
            }
            filter = javax.swing.RowFilter.orFilter(filters);
        } else {
            int targetCol = -1;
            for (int i = 0; i < labels.length; i++) if (labels[i].equals(columnLabel)) { targetCol = i; break; }
            if (targetCol < 0) { sorter.setRowFilter(null); return; }
            final int colIndex = targetCol;
            filter = new javax.swing.RowFilter<DefaultTableModel, Object>() {
                public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    Object v = entry.getValue(colIndex);
                    return v != null && v.toString().toLowerCase().contains(q.toLowerCase());
                }
            };
        }
        sorter.setRowFilter(filter);
    }

    // Apply rounded, modern tabs consistent with Login/Forgot screens
    private void styleTabbedPane(JTabbedPane tabs) {
        tabs.setFont(ModernTheme.FONT_BODY);
        tabs.setBackground(ModernTheme.SURFACE);
        tabs.setOpaque(true);
        tabs.setBorder(new EmptyBorder(8, 8, 0, 8));
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                              int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 12;
                Color bg = isSelected ? ModernTheme.PRIMARY : ModernTheme.SURFACE;
                g2.setColor(bg);
                g2.fillRoundRect(x, y + 5, w, h - 5, arc, arc);
                if (!isSelected) {
                    g2.setColor(ModernTheme.BORDER);
                    g2.drawRoundRect(x, y + 5, w, h - 5, arc, arc);
                }
                g2.dispose();
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                                     int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(ModernTheme.FONT_BODY);
                g2.setColor(isSelected ? ModernTheme.TEXT_WHITE : ModernTheme.TEXT_PRIMARY);
                int x = textRect.x;
                int y = textRect.y + metrics.getAscent();
                g2.drawString(title, x, y);
                g2.dispose();
            }
        });
    }

    // Consistent modern thin scrollbars across panes
    private void styleModernScrollBar(JScrollPane scrollPane) {
        JScrollBar vbar = scrollPane.getVerticalScrollBar();
        if (vbar != null) {
            vbar.setUnitIncrement(16);
            vbar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    thumbColor = ModernTheme.PRIMARY;
                    trackColor = ModernTheme.SURFACE;
                }
                @Override
                protected Dimension getMinimumThumbSize() {
                    return new Dimension(10, 30);
                }
                @Override
                protected Dimension getMaximumThumbSize() {
                    return new Dimension(10, Integer.MAX_VALUE);
                }
                @Override
                protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
                @Override
                protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
                private JButton createZeroButton() {
                    JButton b = new JButton();
                    b.setPreferredSize(new Dimension(0, 0));
                    b.setMinimumSize(new Dimension(0, 0));
                    b.setMaximumSize(new Dimension(0, 0));
                    return b;
                }
                @Override
                protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ModernTheme.PRIMARY);
                    g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
                    g2.dispose();
                }
                @Override
                protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(ModernTheme.SURFACE);
                    g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                    g2.dispose();
                }
            });
            vbar.setPreferredSize(new Dimension(10, vbar.getPreferredSize().height));
        }
    }

    // Simple modern renderer for JList with theme colors
    private void styleList(JList<?> list) {
        list.setFont(ModernTheme.FONT_BODY);
        list.setBackground(ModernTheme.SURFACE);
        list.setForeground(ModernTheme.TEXT_PRIMARY);
        list.setSelectionBackground(new Color(67, 97, 238, 40));
        list.setSelectionForeground(ModernTheme.TEXT_PRIMARY);
        list.setFixedCellHeight(28);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> jList, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(jList, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(6, 8, 6, 8));
                setFont(ModernTheme.FONT_BODY);
                if (isSelected) {
                    setBackground(new Color(67, 97, 238, 30));
                    setForeground(ModernTheme.TEXT_PRIMARY);
                } else {
                    setBackground(ModernTheme.SURFACE);
                    setForeground(ModernTheme.TEXT_PRIMARY);
                }
                return c;
            }
        });
    }

    private void initSummaryTab(JTabbedPane tabs) {
        JPanel summaryPanel = new JPanel(new BorderLayout(10, 10));
        summaryPanel.setBackground(ModernTheme.BACKGROUND);
        summaryPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(ModernTheme.SURFACE);
        inputPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        summaryCompanyField = new AutoCompleteTextField(20, UserPreferencesCache::getCachedCompanyNames);
        ModernTheme.styleTextField(summaryCompanyField);
        summaryDesignationField = new AutoCompleteTextField(18, UserPreferencesCache::getCachedDesignations);
        ModernTheme.styleTextField(summaryDesignationField);
        summaryHolderField = new AutoCompleteTextField(20, UserPreferencesCache::getCachedHolders);
        ModernTheme.styleTextField(summaryHolderField);
        summaryYearCombo = new JComboBox<>();
        ModernTheme.styleComboBox(summaryYearCombo);
        
        summaryRefreshButton = ModernTheme.createPrimaryButton("Generate Summary");
        summaryRefreshButton.setIcon(ModernIcons.create(IconType.SEARCH, ModernTheme.TEXT_WHITE, 16));
        summaryRefreshButton.setIconTextGap(8);
        
        summaryExportCsvButton = ModernTheme.createSecondaryButton("Export CSV");
        summaryExportCsvButton.setIcon(ModernIcons.create(IconType.EXPORT, ModernTheme.TEXT_PRIMARY, 16));
        summaryExportCsvButton.setIconTextGap(8);
        summaryExportCsvButton.setEnabled(false);
        
        summaryExportPdfButton = ModernTheme.createSecondaryButton("Export PDF");
        summaryExportPdfButton.setIcon(ModernIcons.create(IconType.EXPORT, ModernTheme.TEXT_PRIMARY, 16));
        summaryExportPdfButton.setIconTextGap(8);
        summaryExportPdfButton.setEnabled(false);

        int row = 0;
        JLabel companyLabel = new JLabel("Company:");
        companyLabel.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = row; inputPanel.add(companyLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(summaryCompanyField, gbc);

        JLabel designationLabel = new JLabel("Designation:");
        designationLabel.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = row; inputPanel.add(designationLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(summaryDesignationField, gbc);

        JLabel holderLabel = new JLabel("Report Holder:");
        holderLabel.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = row; inputPanel.add(holderLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(summaryHolderField, gbc);

        JLabel yearLabel = new JLabel("Transaction Year:");
        yearLabel.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = row; inputPanel.add(yearLabel, gbc);
        gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(summaryYearCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(summaryRefreshButton);
        buttonPanel.add(summaryExportCsvButton);
        buttonPanel.add(summaryExportPdfButton);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        inputPanel.add(buttonPanel, gbc);

        summaryOverviewArea = new JTextArea(24, 80);
        summaryOverviewArea.setEditable(false);
        summaryOverviewArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        summaryOverviewArea.setBackground(ModernTheme.SURFACE);
        summaryOverviewArea.setForeground(ModernTheme.TEXT_PRIMARY);
        summaryOverviewArea.setLineWrap(true);
        summaryOverviewArea.setWrapStyleWord(true);
        summaryOverviewArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane summaryScroll = new JScrollPane(summaryOverviewArea);
        summaryScroll.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER, 1));
        styleModernScrollBar(summaryScroll);

        summaryPanel.add(inputPanel, BorderLayout.NORTH);
        summaryPanel.add(summaryScroll, BorderLayout.CENTER);

        loadSummaryYearChoices();

        summaryRefreshButton.addActionListener(e -> regenerateSummary());
        summaryExportCsvButton.addActionListener(e -> exportSummaryAsCsv());
        summaryExportPdfButton.addActionListener(e -> exportSummaryAsPdf());
        summaryYearCombo.addActionListener(e -> { if (summaryTabInitialized) regenerateSummary(); });

        tabs.addTab("Summary & Reports", summaryPanel);

        summaryTabInitialized = true;
        regenerateSummary();
    }

    private void loadSummaryYearChoices() {
        if (summaryYearCombo == null) return;
        try {
            List<String> years = manager.getAvailableYears();
            summaryYearCombo.removeAllItems();
            for (String year : years) {
                summaryYearCombo.addItem(year);
            }
            if (summaryYearCombo.getItemCount() > 0) {
                summaryYearCombo.setSelectedIndex(0);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading summary years: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void regenerateSummary() {
        if (!summaryTabInitialized) {
            return;
        }
        try {
            String company = summaryCompanyField != null ? summaryCompanyField.getText() : "";
            String designation = summaryDesignationField != null ? summaryDesignationField.getText() : "";
            String holder = summaryHolderField != null ? summaryHolderField.getText() : "";
            String year = summaryYearCombo != null ? (String) summaryYearCombo.getSelectedItem() : "All Years";

            // Cache the entered values for autocomplete
            if (!company.trim().isEmpty()) {
                UserPreferencesCache.cacheCompanyName(company.trim());
            }
            if (!designation.trim().isEmpty()) {
                UserPreferencesCache.cacheDesignation(designation.trim());
            }
            if (!holder.trim().isEmpty()) {
                UserPreferencesCache.cacheHolderName(holder.trim());
            }

            SummaryData summary = manager.buildSummaryData(company, designation, holder, year);
            currentSummarySnapshot = summary;
            renderSummary(summary);
            summaryExportCsvButton.setEnabled(true);
            summaryExportPdfButton.setEnabled(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Unable to prepare summary: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            renderSummary(null);
        }
    }

    private void renderSummary(SummaryData data) {
        if (summaryOverviewArea == null) {
            return;
        }
        if (data == null) {
            summaryOverviewArea.setText("Summary unavailable. Please try generating again.");
            summaryExportCsvButton.setEnabled(false);
            summaryExportPdfButton.setEnabled(false);
            return;
        }

        StringBuilder sb = new StringBuilder();

        String holder = defaultIfEmpty(data.getHolderName(), "N/A");
        String designation = defaultIfEmpty(data.getDesignation(), "N/A");
        String company = defaultIfEmpty(data.getCompanyName(), "N/A");

        sb.append("Prepared For : ").append(holder);
        if (!isBlank(designation) && !"N/A".equals(designation)) {
            sb.append(" (" + designation + ")");
        }
        sb.append(System.lineSeparator());
        if (!isBlank(company) && !"N/A".equals(company)) {
            sb.append("Company      : ").append(company).append(System.lineSeparator());
        }
        LocalDateTime generated = data.getGeneratedAt();
        if (generated != null) {
            sb.append("Generated On : ").append(generated.format(SUMMARY_TIMESTAMP_FORMAT)).append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());

        appendSection(sb, "Transactions Overview");
        SummaryData.TransactionSummary txn = data.getTransactions();
        sb.append(String.format("  Total Entries     : %d%n", txn.getTotalCount()));
        sb.append(String.format("  Total Income      : %s%n", formatCurrency(txn.getTotalIncome())));
        sb.append(String.format("  Total Expense     : %s%n", formatCurrency(txn.getTotalExpense())));
        sb.append(String.format("  Net Balance       : %s%n", formatCurrency(txn.getNetBalance())));

        appendSection(sb, "Bank Accounts");
        SummaryData.BankSummary bank = data.getBank();
        sb.append(String.format("  Accounts          : %d%n", bank.getAccountCount()));
        sb.append(String.format("  Unique Holders    : %d%n", bank.getUniqueHolderCount()));
        sb.append(String.format("  Total Balance     : %s%n", formatCurrency(bank.getTotalBalance())));
        if (!isBlank(bank.getTopAccountLabel())) {
            sb.append(String.format("  Top Account       : %s (%s)%n", bank.getTopAccountLabel(), formatCurrency(bank.getTopAccountBalance())));
        }

        appendSection(sb, "Deposits");
        SummaryData.DepositSummary deposits = data.getDeposits();
        sb.append(String.format("  Total Deposits    : %d%n", deposits.getTotalCount()));
        sb.append(String.format("  FD Principal      : %s%n", formatCurrency(deposits.getTotalFdPrincipal())));
        sb.append(String.format("  FD Maturity Est.  : %s%n", formatCurrency(deposits.getTotalFdMaturityEstimate())));
        sb.append(String.format("  RD Contributions  : %s%n", formatCurrency(deposits.getTotalRdContribution())));
        sb.append(String.format("  RD Maturity Est.  : %s%n", formatCurrency(deposits.getTotalRdMaturityEstimate())));
        sb.append(String.format("  Gullak Balance    : %s%n", formatCurrency(deposits.getTotalGullakBalance())));
        sb.append(String.format("  Gullak Due        : %s%n", formatCurrency(deposits.getTotalGullakDue())));
        if (!deposits.getMaturityHighlights().isEmpty()) {
            sb.append("  Upcoming Maturities:" + System.lineSeparator());
            for (SummaryData.DepositSummary.MaturityInfo info : deposits.getMaturityHighlights()) {
                sb.append(String.format("    - %s | Due %s | Principal %s | Value %s%n",
                        info.getLabel(),
                        info.getMaturityDateLabel(),
                        formatCurrency(info.getPrincipalValue()),
                        formatCurrency(info.getMaturityValue())));
            }
        }

        appendSection(sb, "Investments");
        SummaryData.InvestmentSummary investments = data.getInvestments();
        sb.append(String.format("  Total Assets      : %d%n", investments.getTotalCount()));
        sb.append(String.format("  Initial Value     : %s%n", formatCurrency(investments.getTotalInitialValue())));
        sb.append(String.format("  Current Value     : %s%n", formatCurrency(investments.getTotalCurrentValue())));
        sb.append(String.format("  Net P/L           : %s%n", formatCurrency(investments.getTotalProfitOrLoss())));
        if (!investments.getTopPerformers().isEmpty()) {
            sb.append("  Top Performers:" + System.lineSeparator());
            for (SummaryData.InvestmentSummary.InvestmentHighlight hi : investments.getTopPerformers()) {
                sb.append(String.format("    - %s [%s] : %s (%+.2f%%)%n",
                        defaultIfEmpty(hi.getLabel(), "Asset"),
                        defaultIfEmpty(hi.getAssetType(), "N/A"),
                        formatCurrency(hi.getCurrentValue()),
                        hi.getProfitOrLossPercentage()));
            }
        }

        appendSection(sb, "Loans");
        SummaryData.LoanSummary loans = data.getLoans();
        sb.append(String.format("  Total Loans       : %d%n", loans.getTotalCount()));
        sb.append(String.format("  Active Loans      : %d%n", loans.getActiveCount()));
        sb.append(String.format("  Closed Loans      : %d%n", loans.getPaidOffCount()));
        sb.append(String.format("  Principal Issued  : %s%n", formatCurrency(loans.getTotalPrincipal())));
        sb.append(String.format("  Principal O/S     : %s%n", formatCurrency(loans.getTotalPrincipalOutstanding())));
        sb.append(String.format("  Principal Repaid  : %s%n", formatCurrency(loans.getTotalPrincipalPaidOff())));
        sb.append(String.format("  Total Monthly EMI : %s%n", formatCurrency(loans.getTotalMonthlyEmi())));
        sb.append(String.format("  Outstanding Pay   : %s%n", formatCurrency(loans.getTotalRepayableOutstanding())));
        if (!loans.getKeyLoans().isEmpty()) {
            sb.append("  Key Loans:" + System.lineSeparator());
            for (SummaryData.LoanSummary.LoanHighlight hl : loans.getKeyLoans()) {
                sb.append(String.format("    - %s [%s] : EMI %s | Principal %s | Total %s%n",
                        defaultIfEmpty(hl.getLabel(), "Loan"),
                        defaultIfEmpty(hl.getStatus(), "Status"),
                        formatCurrency(hl.getEmiAmount()),
                        formatCurrency(hl.getPrincipalAmount()),
                        formatCurrency(hl.getTotalRepayable())));
            }
        }

        appendSection(sb, "Cards");
        SummaryData.CardSummary cards = data.getCards();
        sb.append(String.format("  Total Cards       : %d%n", cards.getTotalCount()));
        sb.append(String.format("  Credit Cards      : %d%n", cards.getCreditCardCount()));
        sb.append(String.format("  Debit Cards       : %d%n", cards.getDebitCardCount()));
        sb.append(String.format("  Credit Limit      : %s%n", formatCurrency(cards.getTotalCreditLimit())));
        sb.append(String.format("  Credit Used       : %s%n", formatCurrency(cards.getTotalCreditUsed())));
        sb.append(String.format("  Credit Available  : %s%n", formatCurrency(cards.getTotalCreditAvailable())));
        sb.append(String.format("  Total Amount Due  : %s%n", formatCurrency(cards.getTotalCreditDue())));
        if (!cards.getKeyCards().isEmpty()) {
            sb.append("  High Due Cards:" + System.lineSeparator());
            for (SummaryData.CardSummary.CardHighlight ch : cards.getKeyCards()) {
                sb.append(String.format("    - %s [%s] : Limit %s | Available %s | Due %s%n",
                        defaultIfEmpty(ch.getCardName(), "Card"),
                        defaultIfEmpty(ch.getCardType(), "Type"),
                        formatCurrency(ch.getCreditLimit()),
                        formatCurrency(ch.getAvailableCredit()),
                        formatCurrency(ch.getAmountDue())));
            }
        }

        appendSection(sb, "Tax Profiles");
        SummaryData.TaxSummary tax = data.getTax();
        sb.append(String.format("  Profiles          : %d%n", tax.getProfileCount()));
        sb.append(String.format("  Gross Income      : %s%n", formatCurrency(tax.getTotalGrossIncome())));
        sb.append(String.format("  Deductions        : %s%n", formatCurrency(tax.getTotalDeductions())));
        sb.append(String.format("  Taxable Income    : %s%n", formatCurrency(tax.getTotalTaxableIncome())));
        sb.append(String.format("  Tax Paid          : %s%n", formatCurrency(tax.getTotalTaxPaid())));
        if (!isBlank(tax.getLatestFinancialYear())) {
            sb.append(String.format("  Latest Year       : %s | Taxable %s | Tax Paid %s%n",
                    tax.getLatestFinancialYear(),
                    formatCurrency(tax.getLatestYearTaxable()),
                    formatCurrency(tax.getLatestYearTaxPaid())));
        }
        if (!tax.getKeyProfiles().isEmpty()) {
            sb.append("  Key Profiles:" + System.lineSeparator());
            for (SummaryData.TaxSummary.TaxProfileHighlight th : tax.getKeyProfiles()) {
                sb.append(String.format("    - %s (%s) : Taxable %s | Paid %s%n",
                        defaultIfEmpty(th.getProfileName(), "Profile"),
                        defaultIfEmpty(th.getFinancialYear(), "Year"),
                        formatCurrency(th.getTaxableIncome()),
                        formatCurrency(th.getTaxPaid())));
            }
        }

        summaryOverviewArea.setText(sb.toString());
        summaryOverviewArea.setCaretPosition(0);
    }

    private void exportSummaryAsCsv() {
        if (currentSummarySnapshot == null) {
            JOptionPane.showMessageDialog(this, "Please generate the summary before exporting.", "Summary Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Summary CSV");
        chooser.setSelectedFile(new File(buildDefaultSummaryFilename("csv")));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = ensureExtension(chooser.getSelectedFile(), ".csv");
            try {
                writeSummaryToCsv(currentSummarySnapshot, target);
                JOptionPane.showMessageDialog(this, "Summary exported to \"" + target.getAbsolutePath() + "\"", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Unable to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportSummaryAsPdf() {
        if (currentSummarySnapshot == null) {
            JOptionPane.showMessageDialog(this, "Please generate the summary before exporting.", "Summary Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Summary PDF");
        chooser.setSelectedFile(new File(buildDefaultSummaryFilename("pdf")));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = ensureExtension(chooser.getSelectedFile(), ".pdf");
            try {
                writeSummaryToPdf(currentSummarySnapshot, target);
                JOptionPane.showMessageDialog(this, "Summary exported to \"" + target.getAbsolutePath() + "\"", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to export PDF: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void writeSummaryToCsv(SummaryData data, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Header,Value");
            writer.printf("Company,%s%n", escapeCsv(defaultIfEmpty(data.getCompanyName(), "")));
            writer.printf("Designation,%s%n", escapeCsv(defaultIfEmpty(data.getDesignation(), "")));
            writer.printf("Holder,%s%n", escapeCsv(defaultIfEmpty(data.getHolderName(), "")));
            writer.printf("Generated At,%s%n", data.getGeneratedAt() != null ? data.getGeneratedAt().format(SUMMARY_TIMESTAMP_FORMAT) : "");

            writer.println();
            writer.println("Transactions");
            SummaryData.TransactionSummary txn = data.getTransactions();
            writer.printf("Total Entries,%d%n", txn.getTotalCount());
            writer.printf("Total Income,%s%n", formatCurrency(txn.getTotalIncome()));
            writer.printf("Total Expense,%s%n", formatCurrency(txn.getTotalExpense()));
            writer.printf("Net Balance,%s%n", formatCurrency(txn.getNetBalance()));

            writer.println();
            writer.println("Bank Accounts");
            SummaryData.BankSummary bank = data.getBank();
            writer.printf("Accounts,%d%n", bank.getAccountCount());
            writer.printf("Unique Holders,%d%n", bank.getUniqueHolderCount());
            writer.printf("Total Balance,%s%n", formatCurrency(bank.getTotalBalance()));
            writer.printf("Top Account,%s%n", escapeCsv(defaultIfEmpty(bank.getTopAccountLabel(), "")));
            writer.printf("Top Account Balance,%s%n", formatCurrency(bank.getTopAccountBalance()));

            writer.println();
            writer.println("Deposits");
            SummaryData.DepositSummary deposits = data.getDeposits();
            writer.printf("Total Deposits,%d%n", deposits.getTotalCount());
            writer.printf("FD Principal,%s%n", formatCurrency(deposits.getTotalFdPrincipal()));
            writer.printf("FD Maturity Estimate,%s%n", formatCurrency(deposits.getTotalFdMaturityEstimate()));
            writer.printf("RD Contribution,%s%n", formatCurrency(deposits.getTotalRdContribution()));
            writer.printf("RD Maturity Estimate,%s%n", formatCurrency(deposits.getTotalRdMaturityEstimate()));
            writer.printf("Gullak Balance,%s%n", formatCurrency(deposits.getTotalGullakBalance()));
            writer.printf("Gullak Due,%s%n", formatCurrency(deposits.getTotalGullakDue()));
            if (!deposits.getMaturityHighlights().isEmpty()) {
                writer.println("Upcoming Maturities");
                writer.println("Label,Due Date,Principal,Maturity Value");
                for (SummaryData.DepositSummary.MaturityInfo info : deposits.getMaturityHighlights()) {
                    writer.printf("%s,%s,%s,%s%n",
                            escapeCsv(info.getLabel()),
                            escapeCsv(info.getMaturityDateLabel()),
                            formatCurrency(info.getPrincipalValue()),
                            formatCurrency(info.getMaturityValue()));
                }
            }

            writer.println();
            writer.println("Investments");
            SummaryData.InvestmentSummary investments = data.getInvestments();
            writer.printf("Total Assets,%d%n", investments.getTotalCount());
            writer.printf("Initial Value,%s%n", formatCurrency(investments.getTotalInitialValue()));
            writer.printf("Current Value,%s%n", formatCurrency(investments.getTotalCurrentValue()));
            writer.printf("Net Profit/Loss,%s%n", formatCurrency(investments.getTotalProfitOrLoss()));
            if (!investments.getTopPerformers().isEmpty()) {
                writer.println("Top Performers");
                writer.println("Label,Type,Current Value,Profit/Loss %,Profit/Loss Absolute");
                for (SummaryData.InvestmentSummary.InvestmentHighlight hi : investments.getTopPerformers()) {
                    writer.printf("%s,%s,%s,%.2f,%s%n",
                            escapeCsv(defaultIfEmpty(hi.getLabel(), "")),
                            escapeCsv(defaultIfEmpty(hi.getAssetType(), "")),
                            formatCurrency(hi.getCurrentValue()),
                            hi.getProfitOrLossPercentage(),
                            formatCurrency(hi.getProfitOrLoss()));
                }
            }

            writer.println();
            writer.println("Loans");
            SummaryData.LoanSummary loans = data.getLoans();
            writer.printf("Total Loans,%d%n", loans.getTotalCount());
            writer.printf("Active Loans,%d%n", loans.getActiveCount());
            writer.printf("Closed Loans,%d%n", loans.getPaidOffCount());
            writer.printf("Principal Issued,%s%n", formatCurrency(loans.getTotalPrincipal()));
            writer.printf("Principal Outstanding,%s%n", formatCurrency(loans.getTotalPrincipalOutstanding()));
            writer.printf("Principal Paid,%s%n", formatCurrency(loans.getTotalPrincipalPaidOff()));
            writer.printf("Monthly EMI,%s%n", formatCurrency(loans.getTotalMonthlyEmi()));
            writer.printf("Outstanding Repayable,%s%n", formatCurrency(loans.getTotalRepayableOutstanding()));
            if (!loans.getKeyLoans().isEmpty()) {
                writer.println("Key Loans");
                writer.println("Label,Status,EMI,Principal,Total Repayable");
                for (SummaryData.LoanSummary.LoanHighlight hl : loans.getKeyLoans()) {
                    writer.printf("%s,%s,%s,%s,%s%n",
                            escapeCsv(defaultIfEmpty(hl.getLabel(), "")),
                            escapeCsv(defaultIfEmpty(hl.getStatus(), "")),
                            formatCurrency(hl.getEmiAmount()),
                            formatCurrency(hl.getPrincipalAmount()),
                            formatCurrency(hl.getTotalRepayable()));
                }
            }

            writer.println();
            writer.println("Cards");
            SummaryData.CardSummary cards = data.getCards();
            writer.printf("Total Cards,%d%n", cards.getTotalCount());
            writer.printf("Credit Cards,%d%n", cards.getCreditCardCount());
            writer.printf("Debit Cards,%d%n", cards.getDebitCardCount());
            writer.printf("Total Credit Limit,%s%n", formatCurrency(cards.getTotalCreditLimit()));
            writer.printf("Credit Used,%s%n", formatCurrency(cards.getTotalCreditUsed()));
            writer.printf("Credit Available,%s%n", formatCurrency(cards.getTotalCreditAvailable()));
            writer.printf("Amount Due,%s%n", formatCurrency(cards.getTotalCreditDue()));
            if (!cards.getKeyCards().isEmpty()) {
                writer.println("Key Cards");
                writer.println("Card,Type,Credit Limit,Available Credit,Amount Due");
                for (SummaryData.CardSummary.CardHighlight ch : cards.getKeyCards()) {
                    writer.printf("%s,%s,%s,%s,%s%n",
                            escapeCsv(defaultIfEmpty(ch.getCardName(), "")),
                            escapeCsv(defaultIfEmpty(ch.getCardType(), "")),
                            formatCurrency(ch.getCreditLimit()),
                            formatCurrency(ch.getAvailableCredit()),
                            formatCurrency(ch.getAmountDue()));
                }
            }

            writer.println();
            writer.println("Tax Profiles");
            SummaryData.TaxSummary tax = data.getTax();
            writer.printf("Total Profiles,%d%n", tax.getProfileCount());
            writer.printf("Gross Income,%s%n", formatCurrency(tax.getTotalGrossIncome()));
            writer.printf("Deductions,%s%n", formatCurrency(tax.getTotalDeductions()));
            writer.printf("Taxable Income,%s%n", formatCurrency(tax.getTotalTaxableIncome()));
            writer.printf("Tax Paid,%s%n", formatCurrency(tax.getTotalTaxPaid()));
            writer.printf("Latest Financial Year,%s%n", escapeCsv(defaultIfEmpty(tax.getLatestFinancialYear(), "")));
            writer.printf("Latest Taxable,%s%n", formatCurrency(tax.getLatestYearTaxable()));
            writer.printf("Latest Tax Paid,%s%n", formatCurrency(tax.getLatestYearTaxPaid()));
            if (!tax.getKeyProfiles().isEmpty()) {
                writer.println("Key Profiles");
                writer.println("Profile,Financial Year,Taxable Income,Tax Paid");
                for (SummaryData.TaxSummary.TaxProfileHighlight th : tax.getKeyProfiles()) {
                    writer.printf("%s,%s,%s,%s%n",
                            escapeCsv(defaultIfEmpty(th.getProfileName(), "")),
                            escapeCsv(defaultIfEmpty(th.getFinancialYear(), "")),
                            formatCurrency(th.getTaxableIncome()),
                            formatCurrency(th.getTaxPaid()));
                }
            }
        }
    }

    private void writeSummaryToPdf(SummaryData data, File file) throws DocumentException, IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Document document = new Document();
            PdfWriter.getInstance(document, fos);
            document.open();

            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            com.itextpdf.text.Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BaseColor.BLACK);
            com.itextpdf.text.Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);

            document.add(new Paragraph("Finance Summary Report", titleFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Prepared For: " + defaultIfEmpty(data.getHolderName(), "N/A"), textFont));
            if (!isBlank(data.getDesignation())) {
                document.add(new Paragraph("Designation: " + defaultIfEmpty(data.getDesignation(), ""), textFont));
            }
            if (!isBlank(data.getCompanyName())) {
                document.add(new Paragraph("Company: " + defaultIfEmpty(data.getCompanyName(), ""), textFont));
            }
            if (data.getGeneratedAt() != null) {
                document.add(new Paragraph("Generated On: " + data.getGeneratedAt().format(SUMMARY_TIMESTAMP_FORMAT), textFont));
            }
            document.add(new Paragraph(" "));

            addSummarySection(document, "Transactions Overview", new String[][]{
                    {"Total Entries", String.valueOf(data.getTransactions().getTotalCount())},
                    {"Total Income", formatCurrency(data.getTransactions().getTotalIncome())},
                    {"Total Expense", formatCurrency(data.getTransactions().getTotalExpense())},
                    {"Net Balance", formatCurrency(data.getTransactions().getNetBalance())}
            }, sectionFont, textFont);

            SummaryData.BankSummary bank = data.getBank();
            addSummarySection(document, "Bank Accounts", new String[][]{
                    {"Accounts", String.valueOf(bank.getAccountCount())},
                    {"Unique Holders", String.valueOf(bank.getUniqueHolderCount())},
                    {"Total Balance", formatCurrency(bank.getTotalBalance())},
                    {"Top Account", defaultIfEmpty(bank.getTopAccountLabel(), "")},
                    {"Top Balance", formatCurrency(bank.getTopAccountBalance())}
            }, sectionFont, textFont);

            SummaryData.DepositSummary deposits = data.getDeposits();
            addSummarySection(document, "Deposits", new String[][]{
                    {"Total Deposits", String.valueOf(deposits.getTotalCount())},
                    {"FD Principal", formatCurrency(deposits.getTotalFdPrincipal())},
                    {"FD Maturity", formatCurrency(deposits.getTotalFdMaturityEstimate())},
                    {"RD Contribution", formatCurrency(deposits.getTotalRdContribution())},
                    {"RD Maturity", formatCurrency(deposits.getTotalRdMaturityEstimate())},
                    {"Gullak Balance", formatCurrency(deposits.getTotalGullakBalance())},
                    {"Gullak Due", formatCurrency(deposits.getTotalGullakDue())}
            }, sectionFont, textFont);
            if (!deposits.getMaturityHighlights().isEmpty()) {
                document.add(new Paragraph("Upcoming Maturities", sectionFont));
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{3f, 2f, 2f, 2f});
                addTableHeader(table, new String[]{"Label", "Due Date", "Principal", "Maturity"});
                for (SummaryData.DepositSummary.MaturityInfo info : deposits.getMaturityHighlights()) {
                    table.addCell(new Phrase(defaultIfEmpty(info.getLabel(), ""), textFont));
                    table.addCell(new Phrase(defaultIfEmpty(info.getMaturityDateLabel(), ""), textFont));
                    table.addCell(new Phrase(formatCurrency(info.getPrincipalValue()), textFont));
                    table.addCell(new Phrase(formatCurrency(info.getMaturityValue()), textFont));
                }
                document.add(table);
            }

            SummaryData.InvestmentSummary investments = data.getInvestments();
            addSummarySection(document, "Investments", new String[][]{
                    {"Total Assets", String.valueOf(investments.getTotalCount())},
                    {"Initial Value", formatCurrency(investments.getTotalInitialValue())},
                    {"Current Value", formatCurrency(investments.getTotalCurrentValue())},
                    {"Net Profit/Loss", formatCurrency(investments.getTotalProfitOrLoss())}
            }, sectionFont, textFont);
            if (!investments.getTopPerformers().isEmpty()) {
                document.add(new Paragraph("Top Performers", sectionFont));
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{3f, 2f, 2f, 2f});
                addTableHeader(table, new String[]{"Asset", "Type", "Current Value", "Profit %"});
                for (SummaryData.InvestmentSummary.InvestmentHighlight hi : investments.getTopPerformers()) {
                    table.addCell(new Phrase(defaultIfEmpty(hi.getLabel(), ""), textFont));
                    table.addCell(new Phrase(defaultIfEmpty(hi.getAssetType(), ""), textFont));
                    table.addCell(new Phrase(formatCurrency(hi.getCurrentValue()), textFont));
                    table.addCell(new Phrase(String.format("%.2f%%", hi.getProfitOrLossPercentage()), textFont));
                }
                document.add(table);
            }

            SummaryData.LoanSummary loans = data.getLoans();
            addSummarySection(document, "Loans", new String[][]{
                    {"Total Loans", String.valueOf(loans.getTotalCount())},
                    {"Active Loans", String.valueOf(loans.getActiveCount())},
                    {"Closed Loans", String.valueOf(loans.getPaidOffCount())},
                    {"Principal Issued", formatCurrency(loans.getTotalPrincipal())},
                    {"Principal Outstanding", formatCurrency(loans.getTotalPrincipalOutstanding())},
                    {"Principal Paid", formatCurrency(loans.getTotalPrincipalPaidOff())},
                    {"Monthly EMI", formatCurrency(loans.getTotalMonthlyEmi())},
                    {"Outstanding Repayable", formatCurrency(loans.getTotalRepayableOutstanding())}
            }, sectionFont, textFont);
            if (!loans.getKeyLoans().isEmpty()) {
                document.add(new Paragraph("Key Loans", sectionFont));
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{3f, 1.5f, 1.5f, 1.5f, 1.5f});
                addTableHeader(table, new String[]{"Loan", "Status", "EMI", "Principal", "Total"});
                for (SummaryData.LoanSummary.LoanHighlight hl : loans.getKeyLoans()) {
                    table.addCell(new Phrase(defaultIfEmpty(hl.getLabel(), ""), textFont));
                    table.addCell(new Phrase(defaultIfEmpty(hl.getStatus(), ""), textFont));
                    table.addCell(new Phrase(formatCurrency(hl.getEmiAmount()), textFont));
                    table.addCell(new Phrase(formatCurrency(hl.getPrincipalAmount()), textFont));
                    table.addCell(new Phrase(formatCurrency(hl.getTotalRepayable()), textFont));
                }
                document.add(table);
            }

            SummaryData.CardSummary cards = data.getCards();
            addSummarySection(document, "Cards", new String[][]{
                    {"Total Cards", String.valueOf(cards.getTotalCount())},
                    {"Credit Cards", String.valueOf(cards.getCreditCardCount())},
                    {"Debit Cards", String.valueOf(cards.getDebitCardCount())},
                    {"Total Credit Limit", formatCurrency(cards.getTotalCreditLimit())},
                    {"Credit Used", formatCurrency(cards.getTotalCreditUsed())},
                    {"Available Credit", formatCurrency(cards.getTotalCreditAvailable())},
                    {"Amount Due", formatCurrency(cards.getTotalCreditDue())}
            }, sectionFont, textFont);
            if (!cards.getKeyCards().isEmpty()) {
                document.add(new Paragraph("High Due Cards", sectionFont));
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{3f, 1.5f, 1.5f, 1.5f});
                addTableHeader(table, new String[]{"Card", "Type", "Available", "Amount Due"});
                for (SummaryData.CardSummary.CardHighlight ch : cards.getKeyCards()) {
                    table.addCell(new Phrase(defaultIfEmpty(ch.getCardName(), ""), textFont));
                    table.addCell(new Phrase(defaultIfEmpty(ch.getCardType(), ""), textFont));
                    table.addCell(new Phrase(formatCurrency(ch.getAvailableCredit()), textFont));
                    table.addCell(new Phrase(formatCurrency(ch.getAmountDue()), textFont));
                }
                document.add(table);
            }

            SummaryData.TaxSummary tax = data.getTax();
            addSummarySection(document, "Tax Profiles", new String[][]{
                    {"Total Profiles", String.valueOf(tax.getProfileCount())},
                    {"Gross Income", formatCurrency(tax.getTotalGrossIncome())},
                    {"Deductions", formatCurrency(tax.getTotalDeductions())},
                    {"Taxable Income", formatCurrency(tax.getTotalTaxableIncome())},
                    {"Tax Paid", formatCurrency(tax.getTotalTaxPaid())},
                    {"Latest Year", defaultIfEmpty(tax.getLatestFinancialYear(), "")},
                    {"Latest Taxable", formatCurrency(tax.getLatestYearTaxable())},
                    {"Latest Tax Paid", formatCurrency(tax.getLatestYearTaxPaid())}
            }, sectionFont, textFont);
            if (!tax.getKeyProfiles().isEmpty()) {
                document.add(new Paragraph("Key Profiles", sectionFont));
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{3f, 2f, 2f, 2f});
                addTableHeader(table, new String[]{"Profile", "Financial Year", "Taxable", "Tax Paid"});
                for (SummaryData.TaxSummary.TaxProfileHighlight th : tax.getKeyProfiles()) {
                    table.addCell(new Phrase(defaultIfEmpty(th.getProfileName(), ""), textFont));
                    table.addCell(new Phrase(defaultIfEmpty(th.getFinancialYear(), ""), textFont));
                    table.addCell(new Phrase(formatCurrency(th.getTaxableIncome()), textFont));
                    table.addCell(new Phrase(formatCurrency(th.getTaxPaid()), textFont));
                }
                document.add(table);
            }

            document.close();
        }
    }

    private void addSummarySection(Document document, String title, String[][] rows,
                                    com.itextpdf.text.Font headerFont,
                                    com.itextpdf.text.Font cellFont) throws DocumentException {
        document.add(new Paragraph(title, headerFont));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4f);
        table.setSpacingAfter(10f);
        table.setWidths(new float[]{2f, 3f});
        for (String[] row : rows) {
            if (row == null || row.length < 2) continue;
            table.addCell(new Phrase(defaultIfEmpty(row[0], ""), cellFont));
            table.addCell(new Phrase(defaultIfEmpty(row[1], ""), cellFont));
        }
        document.add(table);
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        com.itextpdf.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);
        }
    }

    private String buildDefaultSummaryFilename(String extension) {
        String holder = currentSummarySnapshot != null ? defaultIfEmpty(currentSummarySnapshot.getHolderName(), "Summary") : "Summary";
        String safeHolder = holder.replaceAll("[^a-zA-Z0-9-_]", "_");
    String timestamp = LocalDate.now().toString();
        return safeHolder + "_Report_" + timestamp + "." + extension;
    }

    private File ensureExtension(File file, String extension) {
        String name = file.getName();
        if (!name.toLowerCase().endsWith(extension)) {
            File parent = file.getParentFile();
            if (parent != null) {
                return new File(parent, name + extension);
            }
            return new File(name + extension);
        }
        return file;
    }

    private void appendSection(StringBuilder sb, String title) {
        sb.append(title).append(System.lineSeparator());
        sb.append(repeat('-', Math.max(8, title.length()))).append(System.lineSeparator());
    }

    private String repeat(char ch, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }

    private String formatCurrency(double value) {
        return String.format("₹%,.2f", value);
    }

    private String escapeCsv(String input) {
        if (input == null) {
            return "";
        }
        String escaped = input.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String defaultIfEmpty(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
            // Get the actual database ID from the hidden column (index 10)
            int modelRow = currentTable.convertRowIndexToModel(selectedRow);
            DefaultTableModel model = (DefaultTableModel) currentTable.getModel();
            int transactionId = (int) model.getValueAt(modelRow, 10);
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

    
    // --- Method to Update Summary Cards Based on Selected Month ---
    private void updateSummaryCards() {
        JScrollPane currentScrollPane = (JScrollPane) monthTabs.getSelectedComponent();
        if (currentScrollPane == null) {
            // No month selected, reset to 0
            if (incomeValueLabel != null) incomeValueLabel.setText("₹0.00");
            if (expenseValueLabel != null) expenseValueLabel.setText("₹0.00");
            if (balanceValueLabel != null) balanceValueLabel.setText("₹0.00");
            return;
        }
        
        // Get the stored totals for the selected month
        Double monthIncome = (Double) currentScrollPane.getClientProperty("monthIncome");
        Double monthExpense = (Double) currentScrollPane.getClientProperty("monthExpense");
        
        if (monthIncome == null) monthIncome = 0.0;
        if (monthExpense == null) monthExpense = 0.0;
        
        double netBalance = monthIncome - monthExpense;
        
        // Update the labels
        if (incomeValueLabel != null) {
            incomeValueLabel.setText(String.format("₹%,.2f", monthIncome));
        }
        if (expenseValueLabel != null) {
            expenseValueLabel.setText(String.format("₹%,.2f", monthExpense));
        }
        if (balanceValueLabel != null) {
            balanceValueLabel.setText(String.format("₹%,.2f", netBalance));
        }
    }
    
    // --- Helper Method to Create Summary Cards ---
    private JPanel createSummaryCard(String title, JLabel valueLabel, Color bgColor, Color darkColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, bgColor, 0, getHeight(), darkColor);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(10, 10));
        card.setPreferredSize(new Dimension(200, 100));
        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        card.setOpaque(false);
        
        // Title Label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Value Label (passed as parameter)
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    // --- Logout Method ---
        // --- Refresh UI Theme Method ---
        private void refreshUITheme() {
            // Update all components to use new colors
            SwingUtilities.updateComponentTreeUI(this);
            getContentPane().setBackground(ModernTheme.BACKGROUND);
        
            // Refresh all tabs
            refreshTransactions();
            refreshBankAccounts();
            refreshDeposits();
            refreshInvestments();
            refreshTaxProfiles();
            refreshLoans();
            refreshCards();
            regenerateSummary();
        
            repaint();
            revalidate();
        }
    
    private void performLogout() {
        try {
            // Close database connection
            if (manager != null) {
                manager.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Clear session
        SessionContext.clear();
        
        // Dispose this window
        dispose();
        
        // Restart the application (show login again)
        SwingUtilities.invokeLater(() -> {
            FinanceManagerApp.main(new String[0]);
        });
    }
   
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
            int serialNumber = 1; // Start serial number from 1
            for (Transaction t : data) {
                StringJoiner sj = new StringJoiner(",");
                sj.add(String.valueOf(serialNumber++));
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
            int serialNumber = 1; // Start serial number from 1
            for (Transaction t : data) {
                table.addCell(new Phrase(String.valueOf(serialNumber++), cellFont));
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