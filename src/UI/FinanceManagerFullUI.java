package src.UI;
// We'll also add a placeholder for the dialog we will create
// import src.UI.AddEditTaxProfileDialog;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
import src.Lending;
import src.UI.LendingRecycleBinDialog;
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
import src.UI.EditProfileDialog;
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
    // --- NEW Main UI Components ---
    private SidebarPanel sidebarPanel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    // --- Main UI Components ---
  
    private LogoPanel headerLogo; // Logo panel for theme switching
    
    // Profile widget components for theme updates
    private JButton userProfileBtn;
    private JPanel avatarPanel;
    private JLabel nameLabel;
    private JLabel typeLabel;
    private JLabel dropdownIcon;
    private JPanel leftPanel;

    // --- Transaction Tab Variables ---
    private JComboBox<String> yearComboBox;
    private JComboBox<String> monthComboBox; // Month selector dropdown
    private JButton deleteMonthButton;
    private JButton calendarButton; // Month filter button
    private JPanel filterPanel; // Filter panel container
    private JPopupMenu monthMenu; // Month filter popup menu
    private JPopupMenu columnMenu; // Column filter popup menu
    private JLabel yearLabel; // "Select Year:" label
    private JLabel searchLabel; // "Search:" label
    private JTextField txnSearchField; // search text for transactions
    private JComboBox<String> txnSearchColumn; // column selector for transactions
    private JButton filterButton; // Filter button for column search
    private JButton exportButton; // Export button for transactions
    private JLabel incomeValueLabel; // Label to show total income
    private JLabel expenseValueLabel; // Label to show total expense
    private JLabel balanceValueLabel; // Label to show net balance
    private JTable transactionsTable; // Single table for all transactions
    private JScrollPane transactionsScrollPane; // ScrollPane for transactions table
    
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

    // --- Lending Tab Variables ---
    private JList<Lending> lendingList;
    private DefaultListModel<Lending> lendingListModel;
    private JPanel lendingDetailPanel;

    public FinanceManagerFullUI() {
        setTitle("SmartLedger - Personal Finance Manager");
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

    // Top header with modern user profile widget
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setBorder(new EmptyBorder(8, 16, 8, 16));
    topPanel.setBackground(ModernTheme.SURFACE);
    
        // Get current account details
        src.auth.Account currentAccount = SessionContext.getCurrentAccount();
        
        // LEFT SIDE - Modern User Profile Widget (aligned to left)
        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setBackground(ModernTheme.SURFACE);
        
        if (currentAccount != null) {
            // Create interactive user profile button with custom rounded painting
            userProfileBtn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    
                    // Draw rounded background
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    
                    // Draw border with proper insets to ensure it's fully visible
                    g2.setColor(ModernTheme.BORDER);
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
                    
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            
            userProfileBtn.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
            userProfileBtn.setBackground(ModernTheme.BACKGROUND);
            userProfileBtn.setContentAreaFilled(false);
            userProfileBtn.setBorderPainted(false);
            userProfileBtn.setFocusPainted(false);
            userProfileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            userProfileBtn.setOpaque(false);
            
            // Create circular avatar with custom painting
            avatarPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Draw circular background
                    g2.setColor(ModernTheme.PRIMARY);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    
                    // Draw text
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font(ModernTheme.FONT_HEADER.getFamily(), Font.BOLD, 16));
                    FontMetrics fm = g2.getFontMetrics();
                    String initial = currentAccount.getAccountName().substring(0, 1).toUpperCase();
                    int textWidth = fm.stringWidth(initial);
                    int textHeight = fm.getAscent();
                    g2.drawString(initial, (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 2);
                    
                    g2.dispose();
                }
            };
            avatarPanel.setPreferredSize(new Dimension(36, 36));
            avatarPanel.setOpaque(false);
            
            userProfileBtn.add(avatarPanel);
            
            // User info panel (name + type badge)
            JPanel userInfoPanel = new JPanel();
            userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
            userInfoPanel.setOpaque(false);
            
            // Name label with smaller font
            nameLabel = new JLabel(currentAccount.getAccountName());
            nameLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 13));
            nameLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Account type badge with smaller font and rounded corners
            String accountTypeText = currentAccount.getAccountType() == src.auth.Account.AccountType.BUSINESS 
                ? "Business" : "Personal";
            typeLabel = new JLabel(accountTypeText) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            typeLabel.setFont(new Font(ModernTheme.FONT_SMALL.getFamily(), Font.PLAIN, 10));
            typeLabel.setForeground(Color.WHITE);
            typeLabel.setBackground(currentAccount.getAccountType() == src.auth.Account.AccountType.BUSINESS 
                ? ModernTheme.PRIMARY : ModernTheme.SUCCESS);
            typeLabel.setOpaque(false);
            typeLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            userInfoPanel.add(nameLabel);
            userInfoPanel.add(Box.createVerticalStrut(2));
            userInfoPanel.add(typeLabel);
            
            userProfileBtn.add(userInfoPanel);
            
            // Dropdown arrow icon (smaller)
            dropdownIcon = new JLabel("▼");
            dropdownIcon.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 8));
            dropdownIcon.setForeground(ModernTheme.TEXT_SECONDARY);
            userProfileBtn.add(dropdownIcon);
            
            // Add interactive popup menu with rounded style
            JPopupMenu userMenu = new JPopupMenu() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            userMenu.setBackground(ModernTheme.SURFACE);
            userMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            
            // Profile info in popup (smaller fonts)
            JMenuItem profileHeader = new JMenuItem("Profile Information");
            profileHeader.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 12));
            profileHeader.setEnabled(false);
            profileHeader.setBackground(ModernTheme.SURFACE);
            userMenu.add(profileHeader);
            userMenu.addSeparator();
            
            // Email info with smaller font
            if (currentAccount.getEmail() != null && !currentAccount.getEmail().trim().isEmpty()) {
                JMenuItem emailItem = new JMenuItem("📧 " + currentAccount.getEmail());
                emailItem.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 11));
                emailItem.setEnabled(false);
                emailItem.setBackground(ModernTheme.SURFACE);
                userMenu.add(emailItem);
            }
            
            // Phone info with smaller font
            if (currentAccount.getPhone() != null && !currentAccount.getPhone().trim().isEmpty()) {
                JMenuItem phoneItem = new JMenuItem("📱 " + currentAccount.getPhone());
                phoneItem.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 11));
                phoneItem.setEnabled(false);
                phoneItem.setBackground(ModernTheme.SURFACE);
                userMenu.add(phoneItem);
            }
            
            userMenu.addSeparator();
            
            // Edit Profile option with smaller font
            JMenuItem editProfileItem = new JMenuItem("✏️ Edit Profile");
            editProfileItem.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 11));
            editProfileItem.setBackground(ModernTheme.SURFACE);
            editProfileItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
            editProfileItem.addActionListener(e -> {
                openEditProfileDialog();
            });
            userMenu.add(editProfileItem);
            
            // Show popup on click
            userProfileBtn.addActionListener(e -> {
                userMenu.show(userProfileBtn, 0, userProfileBtn.getHeight() + 5);
            });
            
            leftPanel.add(userProfileBtn);
        }
        
        topPanel.add(leftPanel, BorderLayout.WEST);
        
        // RIGHT SIDE - Dark Mode & Logout buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(ModernTheme.SURFACE);
        
        // Dark mode toggle button
        JButton darkModeBtn = ModernTheme.createDarkModeToggleButton();
        darkModeBtn.addActionListener(e -> {
            ModernTheme.toggleDarkMode();
            refreshUITheme();
        });
        rightPanel.add(darkModeBtn);
        
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
        
        rightPanel.add(logoutBtn);
        topPanel.add(rightPanel, BorderLayout.EAST);
        
        // Create a wrapper panel for header + separator line
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setBackground(ModernTheme.SURFACE);
        headerWrapper.add(topPanel, BorderLayout.CENTER);
        
        // Add horizontal separator line below the header
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(ModernTheme.BORDER);
        separator.setBackground(ModernTheme.BORDER);
        headerWrapper.add(separator, BorderLayout.SOUTH);
        
        mainPanel.add(headerWrapper, BorderLayout.NORTH);

        // --- NEW Sidebar Setup ---
        sidebarPanel = new SidebarPanel();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // --- NEW CardLayout Setup ---
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(ModernTheme.BACKGROUND); // Match frame background
        mainPanel.add(mainContentPanel, BorderLayout.CENTER);

        // =========================================================
        // ===         TRANSACTIONS PANEL                      ===
        // =========================================================
        JPanel tPanel = new JPanel(new BorderLayout(0, 0)); // Removed spacing to minimize gap
        tPanel.setBackground(ModernTheme.BACKGROUND);
        tPanel.setBorder(new EmptyBorder(0, 4, 4, 4)); // Minimal padding on all sides
    JPanel tTopPanel = new JPanel();
        tTopPanel.setLayout(new BoxLayout(tTopPanel, BoxLayout.X_AXIS));
        tTopPanel.setBorder(new EmptyBorder(8, 10, 8, 10)); // Padding for spacing
        tTopPanel.setBackground(ModernTheme.SURFACE);
        
        // Style the year label
        yearLabel = new JLabel("Select Year:");
        yearLabel.setFont(ModernTheme.FONT_BODY);
        yearLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        tTopPanel.add(yearLabel);
        tTopPanel.add(Box.createHorizontalStrut(10));
        
        // Create and style year combo box with fixed width
        yearComboBox = new JComboBox<>();
        yearComboBox.setPreferredSize(new Dimension(150, 42));
        yearComboBox.setMaximumSize(new Dimension(150, 42));
        ModernTheme.styleComboBox(yearComboBox);
        tTopPanel.add(yearComboBox);
        
        // Add calendar icon button for month filter
        tTopPanel.add(Box.createHorizontalStrut(15));
        
        // Initialize monthComboBox with default value (not displayed, just for internal use)
        monthComboBox = new JComboBox<>(new String[] {
            "All Months", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        });
        monthComboBox.setSelectedItem("All Months");
        
        JButton deleteYearButton = ModernTheme.createDangerButton("Delete All of Selected Year");
        tTopPanel.add(deleteYearButton);
        tTopPanel.add(Box.createHorizontalStrut(10));
        deleteMonthButton = ModernTheme.createDangerButton("Delete Selected Month");
        deleteMonthButton.setEnabled(false);
        tTopPanel.add(deleteMonthButton);
    // --- Search controls for Transactions (right-aligned) ---
    tTopPanel.add(Box.createHorizontalGlue()); // Push search to the right
    searchLabel = new JLabel("Search:");
    searchLabel.setFont(ModernTheme.FONT_BODY);
    searchLabel.setForeground(ModernTheme.TEXT_PRIMARY);
    tTopPanel.add(searchLabel);
    tTopPanel.add(Box.createHorizontalStrut(8));
    txnSearchField = new JTextField(20);
    txnSearchField.setMaximumSize(new Dimension(250, 42));
    ModernTheme.styleTextField(txnSearchField);
    tTopPanel.add(txnSearchField);
    
    // Modern filter button instead of dropdown
    String[] txnCols = new String[]{"All Columns","S.No.","Date","Timestamp","Day","Payment Method","Category","Type","Payee","Description","Amount"};
    txnSearchColumn = new JComboBox<>(txnCols);
    txnSearchColumn.setSelectedIndex(0);
    txnSearchColumn.setVisible(false); // Hide the combo box, we'll use popup instead
    
    filterButton = new JButton();
    filterButton.setIcon(ModernIcons.create(IconType.SEARCH, ModernTheme.TEXT_PRIMARY, 16));
    filterButton.setToolTipText("Filter by column");
    filterButton.setFont(ModernTheme.FONT_SMALL);
    filterButton.setFocusPainted(false);
    filterButton.setBorderPainted(false);
    filterButton.setContentAreaFilled(true);
    filterButton.setOpaque(true);
    filterButton.setBackground(ModernTheme.SURFACE);
    filterButton.setForeground(ModernTheme.TEXT_PRIMARY);
    filterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    filterButton.setBorder(BorderFactory.createCompoundBorder(
        new ModernTheme.RoundedBorder(8, ModernTheme.BORDER),
        BorderFactory.createEmptyBorder(6, 10, 6, 10)
    ));
    filterButton.setPreferredSize(new Dimension(40, 32));
    
    // Create popup menu for column selection
    columnMenu = new JPopupMenu();
    columnMenu.setBackground(ModernTheme.SURFACE);
    columnMenu.setBorder(null);
    
    for (String col : txnCols) {
        JMenuItem colItem = new JMenuItem(col);
        colItem.setFont(ModernTheme.FONT_BODY);
        colItem.setBackground(ModernTheme.SURFACE);
        colItem.setForeground(ModernTheme.TEXT_PRIMARY);
        colItem.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        // Hover effect
        colItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                colItem.setBackground(ModernTheme.PRIMARY_LIGHT);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                colItem.setBackground(ModernTheme.SURFACE);
            }
        });
        
        colItem.addActionListener(e -> {
            txnSearchColumn.setSelectedItem(col);
            applyTransactionSearchFilter(txnSearchField.getText(), col);
        });
        
        columnMenu.add(colItem);
    }
    
    filterButton.addActionListener(e -> {
        columnMenu.show(filterButton, 0, filterButton.getHeight());
    });
    
    // Hover effect for filter button
    filterButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            filterButton.setBackground(ModernTheme.PRIMARY_LIGHT);
        }
        public void mouseExited(java.awt.event.MouseEvent evt) {
            filterButton.setBackground(ModernTheme.SURFACE);
        }
    });
    
    tTopPanel.add(filterButton);
    
    // Remove calendar button from here - will add it above the table instead
        
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
        
        // Create container for filter panel and table
        JPanel tableContainer = new JPanel(new BorderLayout(0, 0));
        tableContainer.setBackground(ModernTheme.SURFACE);
        
        // Create filter panel with calendar button above the table
        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBackground(ModernTheme.SURFACE);
        filterPanel.setBorder(new EmptyBorder(8, 15, 8, 15)); // Just padding, no visible border
        
        // Create a container for the month filter button
        JPanel monthFilterContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        monthFilterContainer.setBackground(ModernTheme.SURFACE);
        
        // Create modern calendar icon button
        calendarButton = new JButton("📅 All Months");
        calendarButton.setFont(ModernTheme.FONT_BODY);
        calendarButton.setPreferredSize(new Dimension(160, 38));
        calendarButton.setFocusPainted(false);
        calendarButton.setOpaque(true);
        calendarButton.setBackground(ModernTheme.SURFACE);
        calendarButton.setForeground(ModernTheme.TEXT_PRIMARY);
        calendarButton.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(10, ModernTheme.BORDER),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        calendarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calendarButton.setToolTipText("Click to filter by month");
        calendarButton.setHorizontalAlignment(SwingConstants.CENTER);
        calendarButton.setIconTextGap(8);
        
        // Add hover effect with subtle background change
        calendarButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                calendarButton.setBackground(ModernTheme.PRIMARY_LIGHT);
                calendarButton.setBorder(BorderFactory.createCompoundBorder(
                    new ModernTheme.RoundedBorder(10, ModernTheme.PRIMARY),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                calendarButton.setBackground(ModernTheme.SURFACE);
                calendarButton.setBorder(BorderFactory.createCompoundBorder(
                    new ModernTheme.RoundedBorder(10, ModernTheme.BORDER),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
            }
        });
        
        // Create popup menu for months
        monthMenu = new JPopupMenu();
        monthMenu.setBackground(ModernTheme.SURFACE);
        monthMenu.setBorder(new ModernTheme.RoundedBorder(12, ModernTheme.BORDER));
        
        String[] months = {"All Months", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", 
                          "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        String[] fullMonths = {"All Months", "January", "February", "March", "April", "May", "June",
                              "July", "August", "September", "October", "November", "December"};
        
        for (int i = 0; i < months.length; i++) {
            final int index = i;
            JMenuItem monthItem = new JMenuItem(months[i]);
            monthItem.setFont(ModernTheme.FONT_BODY);
            monthItem.setBackground(ModernTheme.SURFACE);
            monthItem.setForeground(ModernTheme.TEXT_PRIMARY);
            monthItem.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            
            // Hover effect
            monthItem.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    monthItem.setBackground(ModernTheme.PRIMARY_LIGHT);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    monthItem.setBackground(ModernTheme.SURFACE);
                }
            });
            
            monthItem.addActionListener(e -> {
                monthComboBox.setSelectedItem(fullMonths[index]);
                if (months[index].equals("All Months")) {
                    calendarButton.setText("📅 All Months");
                } else {
                    calendarButton.setText("📅 " + fullMonths[index]);
                }
                calendarButton.setFont(ModernTheme.FONT_BODY);
                calendarButton.setForeground(ModernTheme.TEXT_PRIMARY);
                refreshTransactions();
            });
            
            monthMenu.add(monthItem);
        }
        
        calendarButton.addActionListener(e -> {
            monthMenu.show(calendarButton, 0, calendarButton.getHeight());
        });
        
        monthFilterContainer.add(calendarButton);
        filterPanel.add(monthFilterContainer);
        
        tableContainer.add(filterPanel, BorderLayout.NORTH);
        
        // Create single transaction table
        String[] transactionColumns = {"S.No", "Date", "Timestamp", "Day", "Payment Method", 
                                       "Category", "Type", "Payee", "Description", "Amount"};
        DefaultTableModel transactionModel = new DefaultTableModel(transactionColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionsTable = new JTable(transactionModel);
        transactionsTable.setRowHeight(48);
        transactionsTable.setShowGrid(false);
        transactionsTable.setIntercellSpacing(new Dimension(0, 0));
        transactionsTable.setBackground(ModernTheme.TABLE_BG);
        transactionsTable.setForeground(ModernTheme.TABLE_TEXT);
        transactionsTable.setSelectionBackground(ModernTheme.TABLE_SELECTION);
        transactionsTable.setSelectionForeground(ModernTheme.TABLE_TEXT);
        transactionsTable.setFont(ModernTheme.FONT_BODY);
        
        // Style table header
        transactionsTable.getTableHeader().setBackground(ModernTheme.TABLE_HEADER);
        transactionsTable.getTableHeader().setForeground(ModernTheme.TABLE_TEXT);
        transactionsTable.getTableHeader().setFont(ModernTheme.FONT_HEADER.deriveFont(13f));
        transactionsTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        transactionsTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        
        // Apply custom renderer for income/expense color coding with theme support
        transactionsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Get the Type column value (index 6)
                String type = (String) table.getValueAt(row, 6);
                
                setFont(ModernTheme.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
                
                // Alternating row colors - theme aware
                if (isSelected) {
                    setBackground(ModernTheme.TABLE_SELECTION);
                    setForeground(ModernTheme.TABLE_TEXT);
                } else {
                    if (row % 2 == 0) {
                        setBackground(ModernTheme.TABLE_ROW_EVEN);
                    } else {
                        setBackground(ModernTheme.TABLE_ROW_ODD);
                    }
                    setForeground(ModernTheme.TABLE_TEXT);
                }
                
                // Color code based on type
                if (column == 6) { // Type column
                    if ("Income".equals(type)) {
                        setForeground(new Color(34, 197, 94)); // Bright Green
                        setText("↑ INCOME");
                        setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 13f));
                    } else if ("Expense".equals(type)) {
                        setForeground(new Color(239, 68, 68)); // Bright Red
                        setText("↓ EXPENSE");
                        setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 13f));
                    }
                } else if (column == 9) { // Amount column
                    if ("Income".equals(type)) {
                        setForeground(new Color(74, 222, 128)); // Lighter green for amounts
                        setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 14f));
                    } else if ("Expense".equals(type)) {
                        setForeground(new Color(248, 113, 113)); // Lighter red for amounts
                        setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 14f));
                    }
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                
                return c;
            }
        });
        
        // Create rounded border scroll pane with theme support
        transactionsScrollPane = new JScrollPane(transactionsTable) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ModernTheme.TABLE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        transactionsScrollPane.setBorder(null);
        transactionsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        transactionsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        transactionsScrollPane.setBackground(ModernTheme.TABLE_BG);
        transactionsScrollPane.getViewport().setBackground(ModernTheme.TABLE_BG);
        transactionsScrollPane.setOpaque(false);
        styleModernScrollBar(transactionsScrollPane);
        tableContainer.add(transactionsScrollPane, BorderLayout.CENTER);
        
        tPanel.add(tableContainer, BorderLayout.CENTER);
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
    
    // Create modern rounded export button
    exportButton = new JButton("Export");
    exportButton.setFont(ModernTheme.FONT_BODY);
    exportButton.setIcon(ModernIcons.create(IconType.EXPORT, ModernTheme.TEXT_PRIMARY, 14));
    exportButton.setIconTextGap(6);
    exportButton.setFocusPainted(false);
    exportButton.setBorderPainted(false);
    exportButton.setContentAreaFilled(true);
    exportButton.setOpaque(true);
    exportButton.setBackground(ModernTheme.SURFACE);
    exportButton.setForeground(ModernTheme.TEXT_PRIMARY);
    exportButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    exportButton.setBorder(BorderFactory.createCompoundBorder(
        new ModernTheme.RoundedBorder(8, ModernTheme.BORDER),
        BorderFactory.createEmptyBorder(6, 12, 6, 12)
    ));
    
    // Add hover effect
    exportButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            exportButton.setBackground(ModernTheme.PRIMARY_LIGHT);
        }
        public void mouseExited(java.awt.event.MouseEvent evt) {
            exportButton.setBackground(ModernTheme.SURFACE);
        }
    });
    
        tBottomPanel.add(addTxnBtn);
        tBottomPanel.add(deleteTxnBtn);
        tBottomPanel.add(selectAllTxnBtn);
        tBottomPanel.add(recycleBinBtn);
        tBottomPanel.add(Box.createHorizontalStrut(20)); // Add spacing
        tBottomPanel.add(exportButton);
        tPanel.add(tBottomPanel, BorderLayout.SOUTH);
       mainContentPanel.add(tPanel, "Transactions");
        // Action Listeners
        recycleBinBtn.addActionListener(e -> openRecycleBin()); // Connect button
        yearComboBox.addActionListener(e -> refreshTransactions());
        // Month filtering is handled by calendar button's popup menu
        addTxnBtn.addActionListener(e -> openTransactionDialog());
        deleteTxnBtn.addActionListener(e -> deleteSelectedTransaction());
        selectAllTxnBtn.addActionListener(e -> {
            int rowCount = transactionsTable.getRowCount();
            if (rowCount > 0) {
                transactionsTable.setRowSelectionInterval(0, rowCount - 1);
            }
        });
        deleteMonthButton.addActionListener(e -> deleteSelectedMonth());
        deleteYearButton.addActionListener(e -> deleteSelectedYear());
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
        mainContentPanel.add(bPanel, "Bank Accounts");
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
        JLabel initialLabel = ModernTheme.createPlaceholderLabel("Select a deposit to view details.");
        depositDetailPanel.add(initialLabel, BorderLayout.CENTER);
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
        mainContentPanel.add(dPanel, "Deposits");
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
        JLabel investmentInitialLabel = ModernTheme.createPlaceholderLabel("Select an investment to view details.");
        investmentDetailPanel.add(investmentInitialLabel, BorderLayout.CENTER);
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

        mainContentPanel.add(iPanel, "Investments");

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
taxDetailPanel.setBorder(new EmptyBorder(8, 8, 8, 8)); // Reduced padding for more space
taxDetailPanel.add(ModernTheme.createPlaceholderLabel("Select a profile to view details."), BorderLayout.CENTER);

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

mainContentPanel.add(taxPanel, "Taxation");// Add the new tab

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
// --- NEW EMPTY STATE ---
// 1. Create the WatermarkedPanel
WatermarkedPanel loanEmptyStatePanel = new WatermarkedPanel();
loanEmptyStatePanel.setLayout(new GridBagLayout()); // To center content

// 2. Create the labels
JLabel emptyTitle = ModernTheme.createPlaceholderLabel("Select a loan to view details.");
JLabel emptyHint = new JLabel("Or click 'Add New Loan' to start.");
emptyHint.setFont(ModernTheme.FONT_BODY);
emptyHint.setForeground(ModernTheme.TEXT_SECONDARY);

// 3. Add labels to a sub-panel
JPanel emptyContent = new JPanel(new BorderLayout(0, 10));
emptyContent.setOpaque(false); // Make it transparent
emptyContent.add(emptyTitle, BorderLayout.NORTH);
emptyContent.add(emptyHint, BorderLayout.CENTER);

// 4. Add the content to the watermarked panel
loanEmptyStatePanel.add(emptyContent); 

// 5. Add the watermarked panel to the detail panel
loanDetailPanel.add(loanEmptyStatePanel, BorderLayout.CENTER);
// --- END NEW EMPTY STATE ---
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

mainContentPanel.add(lPanel, "Loans / EMI"); // Add the new tab
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
// ===         NEW LENDING PANEL (MASTER-DETAIL)         ===
// =========================================================
JPanel lePanel = new JPanel(new BorderLayout()); // 'le' for Lending

// --- Split Pane ---
JSplitPane lendingSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
lendingSplitPane.setDividerLocation(220);

// --- Left Side: List of Lendings ---
lendingListModel = new DefaultListModel<>();
lendingList = new JList<>(lendingListModel);
lendingList.setFont(new Font("Arial", Font.PLAIN, 14));
lendingList.setBorder(new EmptyBorder(5, 5, 5, 5));
lendingSplitPane.setLeftComponent(new JScrollPane(lendingList));

// --- Right Side: Detail Panel ---
lendingDetailPanel = new JPanel(new BorderLayout());
lendingDetailPanel.setBackground(ModernTheme.SURFACE);
lendingDetailPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
JLabel lendingInitialLabel = ModernTheme.createPlaceholderLabel("Select a lending record to view details.");
lendingDetailPanel.add(lendingInitialLabel, BorderLayout.CENTER);
lendingSplitPane.setRightComponent(lendingDetailPanel);

lePanel.add(lendingSplitPane, BorderLayout.CENTER);

// --- Bottom Button Panel ---
JPanel lendingButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
JButton addLendingBtn = new JButton("Add New Lending");
JButton deleteLendingBtn = new JButton("Delete Selected Lending");
JButton lendingRecycleBinBtn = new JButton("Lending Recycle Bin");

lendingButtonPanel.add(addLendingBtn);
lendingButtonPanel.add(deleteLendingBtn);
lendingButtonPanel.add(lendingRecycleBinBtn);
lePanel.add(lendingButtonPanel, BorderLayout.SOUTH);

mainContentPanel.add(lePanel, "Lending"); // Add the new "Lending" tab

// --- Action Listeners ---
lendingList.addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        Lending selected = lendingList.getSelectedValue();
        showLendingDetails(selected); // New method we will add
    }
});

addLendingBtn.addActionListener(e -> openAddEditLendingDialog(null)); // New method
deleteLendingBtn.addActionListener(e -> deleteSelectedLending()); // New method
lendingRecycleBinBtn.addActionListener(e -> openLendingRecycleBin()); // New method

// Load initial data
refreshLendings(); // New method
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
cardDetailPanel.add(ModernTheme.createPlaceholderLabel("Select a card to view details."), BorderLayout.CENTER);
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

mainContentPanel.add(cPanel, "Cards");// Renamed tab

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
   initSummaryTab();
sidebarPanel.addNavigationListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        cardLayout.show(mainContentPanel, command);
    }
});

// Show the first card by default
cardLayout.show(mainContentPanel, "Transactions");

    
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
        String selectedYear = (String) yearComboBox.getSelectedItem();
        String selectedMonth = (String) monthComboBox.getSelectedItem();
        if (selectedYear == null) return;
        
        // Clear current table
        DefaultTableModel model = (DefaultTableModel) transactionsTable.getModel();
        model.setRowCount(0);
        
        try {
            Map<String, List<Transaction>> groupedData = manager.getTransactionsGroupedByMonth(selectedYear);
            int serialNumber = 1;
            double totalIncome = 0.0;
            double totalExpense = 0.0;
            boolean hasTransactions = false;
            
            // Filter by selected month
            for (String monthYear : groupedData.keySet()) {
                String monthName = getMonthName(monthYear);
                
                // Skip if not "All Months" and doesn't match selected month
                // monthName is like "October 2025", selectedMonth is like "October"
                if (!"All Months".equals(selectedMonth) && !monthName.startsWith(selectedMonth + " ")) {
                    continue;
                }
                
                List<Transaction> txs = groupedData.get(monthYear);
                for (Transaction t : txs) {
                    model.addRow(new Object[]{
                        serialNumber++, 
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
                    
                    // Calculate totals
                    if ("Income".equalsIgnoreCase(t.getType())) {
                        totalIncome += t.getAmount();
                    } else if ("Expense".equalsIgnoreCase(t.getType())) {
                        totalExpense += t.getAmount();
                    }
                    hasTransactions = true;
                }
            }
            
            // Update summary cards
            incomeValueLabel.setText(String.format("₹%.2f", totalIncome));
            expenseValueLabel.setText(String.format("₹%.2f", totalExpense));
            balanceValueLabel.setText(String.format("₹%.2f", totalIncome - totalExpense));
            
            deleteMonthButton.setEnabled(hasTransactions && !"All Months".equals(selectedMonth));
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
        }
        
        // Re-apply any active search filter after refresh
        if (txnSearchField != null && txnSearchColumn != null) {
            applyTransactionSearchFilter(txnSearchField.getText(), (String) txnSearchColumn.getSelectedItem());
        }
    }

    // Applies a RowFilter to the transactions table based on the text and target column
    private void applyTransactionSearchFilter(String query, String columnLabel) {
        if (transactionsTable == null) return;
        
        @SuppressWarnings("unchecked")
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = 
            (javax.swing.table.TableRowSorter<DefaultTableModel>) transactionsTable.getRowSorter();
        if (sorter == null) {
            sorter = new javax.swing.table.TableRowSorter<>((DefaultTableModel) transactionsTable.getModel());
            transactionsTable.setRowSorter(sorter);
        }

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
 
    /**
     * Applies a modern cell renderer to the transaction table with:
     * - Alternating row colors
     * - Custom formatting for Amount column (₹ symbol, color-coded Income/Expense)
     * - Badge-style rendering for Type column
     * - Enhanced typography and spacing
     */
    private void applyModernTableRenderer(JTable table) {
        DefaultTableCellRenderer modernRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Set font
                setFont(ModernTheme.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                
                // Alternating row colors for better readability
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(ModernTheme.SURFACE);
                    } else {
                        c.setBackground(ModernTheme.isDarkMode() ? 
                            new Color(45, 45, 45) : new Color(248, 249, 250));
                    }
                    c.setForeground(ModernTheme.TEXT_PRIMARY);
                } else {
                    c.setBackground(ModernTheme.PRIMARY_LIGHT);
                    c.setForeground(ModernTheme.TEXT_PRIMARY);
                }
                
                // Center align S.No. column
                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setFont(ModernTheme.FONT_SMALL.deriveFont(Font.BOLD));
                }
                // Right align Amount column
                else if (column == 9) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD));
                    
                    // Format amount with ₹ symbol and color code based on type
                    if (value != null) {
                        double amount = (Double) value;
                        int modelRow = table.convertRowIndexToModel(row);
                        String type = (String) table.getModel().getValueAt(modelRow, 6);
                        
                        setText(String.format("₹%.2f", amount));
                        
                        if (!isSelected) {
                            if ("Income".equalsIgnoreCase(type)) {
                                c.setForeground(new Color(34, 139, 34)); // Green for income
                            } else if ("Expense".equalsIgnoreCase(type)) {
                                c.setForeground(new Color(220, 53, 69)); // Red for expense
                            }
                        }
                    }
                }
                // Left align other text columns
                else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                
                return c;
            }
        };
        
        // Apply renderer to all columns except Type column (which gets special treatment)
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 6) { // Not the Type column
                table.getColumnModel().getColumn(i).setCellRenderer(modernRenderer);
            }
        }
        
        // Special badge-style renderer for Type column (Income/Expense)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                label.setFont(ModernTheme.FONT_SMALL.deriveFont(Font.BOLD));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
                
                // Alternating row background for non-selected rows
                if (!isSelected) {
                    if (row % 2 == 0) {
                        label.setBackground(ModernTheme.SURFACE);
                    } else {
                        label.setBackground(ModernTheme.isDarkMode() ? 
                            new Color(45, 45, 45) : new Color(248, 249, 250));
                    }
                }
                
                // Badge style for Income/Expense
                if (value != null && !isSelected) {
                    String type = value.toString();
                    if ("Income".equalsIgnoreCase(type)) {
                        label.setForeground(new Color(34, 139, 34));
                        label.setText("⬆ " + type.toUpperCase());
                    } else if ("Expense".equalsIgnoreCase(type)) {
                        label.setForeground(new Color(220, 53, 69));
                        label.setText("⬇ " + type.toUpperCase());
                    } else {
                        label.setForeground(ModernTheme.TEXT_PRIMARY);
                    }
                } else if (isSelected) {
                    label.setBackground(ModernTheme.PRIMARY_LIGHT);
                    label.setForeground(ModernTheme.TEXT_PRIMARY);
                }
                
                label.setOpaque(true);
                return label;
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

    private void initSummaryTab() {
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

        mainContentPanel.add(summaryPanel, "Summary & Reports");

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
        if (transactionsTable == null) {
            JOptionPane.showMessageDialog(this, "No transactions available.", "No Table", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTable currentTable = transactionsTable;
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
        String selectedMonth = (String) monthComboBox.getSelectedItem();
        if (selectedMonth == null || "All Months".equals(selectedMonth)) {
            JOptionPane.showMessageDialog(this, "Please select a specific month to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String selectedYear = (String) yearComboBox.getSelectedItem();
        if (selectedYear == null) return;
        
        int choice = JOptionPane.showConfirmDialog(this, 
            "Move ALL transactions for " + selectedMonth + " " + selectedYear + " to the recycle bin?", 
            "Confirm Month Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Convert month name to month number
                String[] months = {"January", "February", "March", "April", "May", "June",
                                  "July", "August", "September", "October", "November", "December"};
                int monthNum = 1;
                for (int i = 0; i < months.length; i++) {
                    if (months[i].equals(selectedMonth)) {
                        monthNum = i + 1;
                        break;
                    }
                }
                String monthYear = selectedYear + "-" + String.format("%02d", monthNum);
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
        bankDetailPanel.setBackground(ModernTheme.SURFACE); // Ensure proper background
        
        if(acc == null) {
            JLabel selectLabel = ModernTheme.createPlaceholderLabel("Select an account.");
            bankDetailPanel.add(selectLabel);
        } else {
            JPanel detailGrid = new JPanel(new GridLayout(0, 1, 5, 10));
            detailGrid.setBackground(ModernTheme.SURFACE);
            
            // Title with modern theme
            JLabel title = new JLabel(acc.getBankName());
            title.setFont(ModernTheme.FONT_TITLE);
            title.setForeground(ModernTheme.TEXT_PRIMARY);
            detailGrid.add(title);
            
            // Subtitle with modern theme
            String accType = acc.getAccountType();
            if ("Current".equals(accType)) accType += " (" + acc.getAccountSubtype() + ")";
            JLabel subTitle = new JLabel(accType + " Account");
            subTitle.setFont(ModernTheme.FONT_SUBTITLE);
            subTitle.setForeground(ModernTheme.TEXT_SECONDARY);
            detailGrid.add(subTitle);
            
            // Balance with modern theme
            JLabel balanceLabel = new JLabel(String.format("Balance: ₹%.2f", acc.getBalance()));
            balanceLabel.setFont(ModernTheme.FONT_HEADER);
            balanceLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            detailGrid.add(balanceLabel);
            
            // Separator
            JSeparator sep1 = new JSeparator();
            sep1.setForeground(ModernTheme.BORDER);
            detailGrid.add(sep1);
            
            // Detail labels with modern theme
            JLabel holderLabel = new JLabel("Holder: " + acc.getHolderName());
            holderLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            holderLabel.setFont(ModernTheme.FONT_BODY);
            detailGrid.add(holderLabel);
            
            JLabel accountLabel = new JLabel("Account #: " + acc.getAccountNumber());
            accountLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            accountLabel.setFont(ModernTheme.FONT_BODY);
            detailGrid.add(accountLabel);
            
            JLabel ifscLabel = new JLabel("IFSC: " + acc.getIfscCode());
            ifscLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            ifscLabel.setFont(ModernTheme.FONT_BODY);
            detailGrid.add(ifscLabel);
            
            if ("Savings".equals(acc.getAccountType())) {
                JSeparator sep2 = new JSeparator();
                sep2.setForeground(ModernTheme.BORDER);
                detailGrid.add(sep2);
                
                JLabel interestLabel = new JLabel("Interest Rate: " + acc.getInterestRate() + "%");
                interestLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                interestLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(interestLabel);
                
                JLabel expenseLabel = new JLabel("Annual Expenses: ₹" + acc.getAnnualExpense());
                expenseLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                expenseLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(expenseLabel);
                
                double interestAmount = acc.getEstimatedAnnualInterest();
                JLabel calcLabel = new JLabel(String.format("Estimated Annual Interest: ₹%.2f", interestAmount));
                calcLabel.setFont(ModernTheme.FONT_HEADER);
                calcLabel.setForeground(ModernTheme.SUCCESS); // Green color for positive amount
                detailGrid.add(calcLabel);
            } else if ("Current".equals(acc.getAccountType())) {
                JSeparator sep2 = new JSeparator();
                sep2.setForeground(ModernTheme.BORDER);
                detailGrid.add(sep2);
                
                if ("Salary".equals(acc.getAccountSubtype())) {
                    JLabel companyLabel = new JLabel("Company: " + acc.getCompanyName());
                    companyLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                    companyLabel.setFont(ModernTheme.FONT_BODY);
                    detailGrid.add(companyLabel);
                } else if ("Business".equals(acc.getAccountSubtype())) {
                    JLabel businessLabel = new JLabel("Business: " + acc.getBusinessName());
                    businessLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                    businessLabel.setFont(ModernTheme.FONT_BODY);
                    detailGrid.add(businessLabel);
                }
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
        depositDetailPanel.add(ModernTheme.createPlaceholderLabel("Select a deposit to view details."), BorderLayout.CENTER);
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
        depositDetailPanel.setBackground(ModernTheme.SURFACE);
        
        if (deposit == null) {
            JLabel selectLabel = ModernTheme.createPlaceholderLabel("Select a deposit to view details.");
            depositDetailPanel.add(selectLabel, BorderLayout.CENTER);
            depositDetailPanel.revalidate(); depositDetailPanel.repaint(); return;
        }
        
        JPanel detailGrid = new JPanel(new GridLayout(0, 1, 5, 10));
        detailGrid.setBackground(ModernTheme.SURFACE);
        
        String titleText = deposit.getDepositType() + ": " + (deposit.getHolderName() != null && !deposit.getHolderName().isEmpty() ? deposit.getHolderName() : (deposit.getDescription() != null && !deposit.getDescription().isEmpty() ? deposit.getDescription() : "Deposit #" + deposit.getId()));
        JLabel title = new JLabel(titleText);
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_PRIMARY);
        detailGrid.add(title);
        
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(ModernTheme.BORDER);
        detailGrid.add(sep1);
        
        if (deposit.getGoal() != null && !deposit.getGoal().isEmpty()) {
            JLabel goalLabel = new JLabel("Goal: " + deposit.getGoal());
            goalLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            goalLabel.setFont(ModernTheme.FONT_BODY);
            detailGrid.add(goalLabel);
        }
        
        if (deposit.getDescription() != null && !deposit.getDescription().isEmpty()) {
            JLabel descLabel = new JLabel("Description: " + deposit.getDescription());
            descLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            descLabel.setFont(ModernTheme.FONT_BODY);
            detailGrid.add(descLabel);
        }
        
        JLabel dateLabel = new JLabel("Added On: " + (deposit.getCreationDate() != null ? deposit.getCreationDate().substring(0, 10) : "N/A"));
        dateLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        dateLabel.setFont(ModernTheme.FONT_BODY);
        detailGrid.add(dateLabel);
        switch(deposit.getDepositType()) {
            case "FD":
                JSeparator sep2 = new JSeparator();
                sep2.setForeground(ModernTheme.BORDER);
                detailGrid.add(sep2);
                
                JLabel principalLabel = new JLabel(String.format("Principal: ₹%.2f", deposit.getPrincipalAmount()));
                principalLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                principalLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(principalLabel);
                
                JLabel rateLabel = new JLabel(String.format("Rate: %.2f%%", deposit.getInterestRate()));
                rateLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                rateLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(rateLabel);
                
                JLabel tenureLabel = new JLabel("Tenure: " + deposit.getTenure() + " " + deposit.getTenureUnit());
                tenureLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                tenureLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(tenureLabel);
                
                JLabel maturityDateLabel = new JLabel("Maturity Date: " + deposit.calculateMaturityDate());
                maturityDateLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                maturityDateLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(maturityDateLabel);
                
                JLabel maturityValueLabel = new JLabel(String.format("Est. Maturity Value: ₹%.2f", deposit.calculateFDMaturityAmount()));
                maturityValueLabel.setForeground(ModernTheme.SUCCESS);
                maturityValueLabel.setFont(ModernTheme.FONT_HEADER);
                detailGrid.add(maturityValueLabel);
                break;
                
            case "RD":
                JSeparator sep3 = new JSeparator();
                sep3.setForeground(ModernTheme.BORDER);
                detailGrid.add(sep3);
                
                JLabel monthlyLabel = new JLabel(String.format("Monthly Deposit: ₹%.2f", deposit.getMonthlyAmount()));
                monthlyLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                monthlyLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(monthlyLabel);
                
                JLabel rdRateLabel = new JLabel(String.format("Rate: %.2f%%", deposit.getInterestRate()));
                rdRateLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                rdRateLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(rdRateLabel);
                
                JLabel rdTenureLabel = new JLabel("Tenure: " + deposit.getTenure() + " " + deposit.getTenureUnit());
                rdTenureLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                rdTenureLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(rdTenureLabel);
                
                JLabel rdMaturityLabel = new JLabel("Maturity Date: " + deposit.calculateMaturityDate());
                rdMaturityLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                rdMaturityLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(rdMaturityLabel);
                
                JLabel totalPrincipalLabel = new JLabel(String.format("Total Principal: ₹%.2f", deposit.getMonthlyAmount() * ("Months".equals(deposit.getTenureUnit()) ? deposit.getTenure() : 0)));
                totalPrincipalLabel.setForeground(ModernTheme.SUCCESS);
                totalPrincipalLabel.setFont(ModernTheme.FONT_HEADER);
                detailGrid.add(totalPrincipalLabel);
                break;
                
            case "Gullak":
                JSeparator sep4 = new JSeparator();
                sep4.setForeground(ModernTheme.BORDER);
                detailGrid.add(sep4);
                
                JLabel currentTotalLabel = new JLabel(String.format("Current Total: ₹%.2f", deposit.getCurrentTotal()));
                currentTotalLabel.setForeground(ModernTheme.SUCCESS);
                currentTotalLabel.setFont(ModernTheme.FONT_HEADER);
                detailGrid.add(currentTotalLabel);
                
                JLabel dueAmountLabel = new JLabel(String.format("Amount Due (Withdrawn): ₹%.2f", deposit.getGullakDueAmount()));
                dueAmountLabel.setForeground(ModernTheme.DANGER);
                dueAmountLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(dueAmountLabel);
                
                JLabel lastUpdatedLabel = new JLabel("Last Updated: " + (deposit.getLastUpdated() != null ? deposit.getLastUpdated() : "Never"));
                lastUpdatedLabel.setForeground(ModernTheme.TEXT_SECONDARY);
                lastUpdatedLabel.setFont(ModernTheme.FONT_SMALL);
                detailGrid.add(lastUpdatedLabel);
                break;
        }
        JButton editButton = ModernTheme.createPrimaryButton("View / Edit Details");
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

    
    // --- Method to Update Summary Cards Based on Current View ---
    private void updateSummaryCards() {
        // Summary cards are now updated in refreshTransactions()
        // This method can be kept for compatibility but doesn't need to do anything
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
            // Update main container backgrounds
            getContentPane().setBackground(ModernTheme.BACKGROUND);
            
            // Update all panels recursively
            updateComponentTheme(this);
            
            // Refresh logo to match current theme
            if (headerLogo != null) {
                headerLogo.refreshLogo();
            }
            
                // Update sidebar styling
                if (sidebarPanel != null) {
                    sidebarPanel.updateTheme();
                }
            
            // Update profile widget theme
            updateProfileWidgetTheme();
            
            // Refresh all data to update table styling
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
        
        /**
         * Updates profile widget colors when theme changes
         */
        private void updateProfileWidgetTheme() {
            if (leftPanel != null) {
                leftPanel.setBackground(ModernTheme.SURFACE);
            }
            
            if (userProfileBtn != null) {
                userProfileBtn.setBackground(ModernTheme.BACKGROUND);
                userProfileBtn.repaint();
            }
            
            if (avatarPanel != null) {
                avatarPanel.repaint(); // Repaint to update PRIMARY color
            }
            
            if (nameLabel != null) {
                nameLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            }
            
            if (typeLabel != null) {
                // Update badge background based on account type
                src.auth.Account currentAccount = SessionContext.getCurrentAccount();
                if (currentAccount != null) {
                    typeLabel.setBackground(currentAccount.getAccountType() == src.auth.Account.AccountType.BUSINESS 
                        ? ModernTheme.PRIMARY : ModernTheme.SUCCESS);
                }
                typeLabel.repaint();
            }
            
            if (dropdownIcon != null) {
                dropdownIcon.setForeground(ModernTheme.TEXT_SECONDARY);
            }
        }
        
        /**
         * Recursively updates theme for all components
         */
        private void updateComponentTheme(Container container) {
            for (Component component : container.getComponents()) {
                // Update background colors for panels
                if (component instanceof JPanel) {
                    component.setBackground(ModernTheme.SURFACE);
                }
                
                // Update table styling
                if (component instanceof JTable) {
                    JTable table = (JTable) component;
                    ModernTheme.styleTable(table);
                }
                
                // Update list styling
                if (component instanceof JList) {
                    JList<?> list = (JList<?>) component;
                    styleList(list);
                }
                
                // Update scroll pane styling
                if (component instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) component;
                    scrollPane.setBackground(ModernTheme.BACKGROUND);
                    scrollPane.getViewport().setBackground(ModernTheme.SURFACE);
                    if (scrollPane.getViewport().getView() instanceof JTable) {
                        ModernTheme.styleTable((JTable) scrollPane.getViewport().getView());
                    } else if (scrollPane.getViewport().getView() instanceof JList) {
                        styleList((JList<?>) scrollPane.getViewport().getView());
                    }
                }
                
                // Update label colors - but preserve specific colors for important elements
                if (component instanceof JLabel) {
                    JLabel label = (JLabel) component;
                    // Don't override success/danger/warning colors, only update default text
                    Color currentColor = label.getForeground();
                    if (currentColor.equals(Color.BLACK) || currentColor.equals(new Color(51, 51, 51)) || 
                        currentColor.equals(new Color(33, 37, 41))) {
                        label.setForeground(ModernTheme.TEXT_PRIMARY);
                    }
                }
                
                // Update text field styling
                if (component instanceof JTextField) {
                    JTextField textField = (JTextField) component;
                    ModernTheme.styleTextField(textField);
                }
                
                // Update combo box styling
                if (component instanceof JComboBox) {
                    JComboBox<?> comboBox = (JComboBox<?>) component;
                    ModernTheme.styleComboBox(comboBox);
                }
                
                // Update separators
                if (component instanceof JSeparator) {
                    component.setForeground(ModernTheme.BORDER);
                }
                
                // Recursively update child containers
                if (component instanceof Container) {
                    updateComponentTheme((Container) component);
                }
            }
            
            // Force update of detail panels
            if (bankDetailPanel != null) {
                bankDetailPanel.setBackground(ModernTheme.SURFACE);
            }
            if (depositDetailPanel != null) {
                depositDetailPanel.setBackground(ModernTheme.SURFACE);
            }
            if (investmentDetailPanel != null) {
                investmentDetailPanel.setBackground(ModernTheme.SURFACE);
            }
            
            // Force update of all lists with proper styling
            if (bankAccountList != null) {
                styleList(bankAccountList);
            }
            if (depositList != null) {
                styleList(depositList);
            }
            if (investmentList != null) {
                styleList(investmentList);
            }
            if (taxProfileList != null) {
                styleList(taxProfileList);
            }
            if (loanList != null) {
                styleList(loanList);
            }
            if (cardList != null) {
                styleList(cardList);
            }
            
            // Update calendar button and filter panel
            if (calendarButton != null) {
                calendarButton.setBackground(ModernTheme.SURFACE);
                calendarButton.setForeground(ModernTheme.TEXT_PRIMARY);
                calendarButton.setBorder(BorderFactory.createCompoundBorder(
                    new ModernTheme.RoundedBorder(10, ModernTheme.BORDER),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
            }
            
            if (filterPanel != null) {
                filterPanel.setBackground(ModernTheme.SURFACE);
                filterPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
            
            // Update transaction tab labels
            if (yearLabel != null) {
                yearLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            }
            
            if (searchLabel != null) {
                searchLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            }
            
            // Update filter button icon
            if (filterButton != null) {
                filterButton.setIcon(ModernIcons.create(IconType.SEARCH, ModernTheme.TEXT_PRIMARY, 16));
                filterButton.setBackground(ModernTheme.SURFACE);
                filterButton.setForeground(ModernTheme.TEXT_PRIMARY);
            }
            
            // Update transaction export button icon
            if (exportButton != null) {
                exportButton.setIcon(ModernIcons.create(IconType.EXPORT, ModernTheme.TEXT_PRIMARY, 14));
                exportButton.setBackground(ModernTheme.SURFACE);
                exportButton.setForeground(ModernTheme.TEXT_PRIMARY);
            }
            
            // Update summary export button icons
            if (summaryExportCsvButton != null) {
                summaryExportCsvButton.setIcon(ModernIcons.create(IconType.EXPORT, ModernTheme.TEXT_PRIMARY, 16));
            }
            if (summaryExportPdfButton != null) {
                summaryExportPdfButton.setIcon(ModernIcons.create(IconType.EXPORT, ModernTheme.TEXT_PRIMARY, 16));
            }
            
            // Update month filter popup menu
            if (monthMenu != null) {
                monthMenu.setBackground(ModernTheme.SURFACE);
                monthMenu.setBorder(new ModernTheme.RoundedBorder(12, ModernTheme.BORDER));
                
                // Update all menu items
                for (Component comp : monthMenu.getComponents()) {
                    if (comp instanceof JMenuItem) {
                        JMenuItem item = (JMenuItem) comp;
                        item.setBackground(ModernTheme.SURFACE);
                        item.setForeground(ModernTheme.TEXT_PRIMARY);
                    }
                }
            }
            
            // Update column filter popup menu
            if (columnMenu != null) {
                columnMenu.setBackground(ModernTheme.SURFACE);
                
                // Update all menu items
                for (Component comp : columnMenu.getComponents()) {
                    if (comp instanceof JMenuItem) {
                        JMenuItem item = (JMenuItem) comp;
                        item.setBackground(ModernTheme.SURFACE);
                        item.setForeground(ModernTheme.TEXT_PRIMARY);
                    }
                }
            }
        }
    
    /**
     * Opens the Edit Profile dialog
     */
    private void openEditProfileDialog() {
        EditProfileDialog dialog = new EditProfileDialog(this);
        dialog.setVisible(true);
        
        if (dialog.isSucceeded()) {
            // Refresh UI to show updated information
            updateProfileWidget();
        }
    }
    
    /**
     * Updates the profile widget with current account information
     */
    private void updateProfileWidget() {
        src.auth.Account currentAccount = SessionContext.getCurrentAccount();
        if (currentAccount != null && nameLabel != null) {
            nameLabel.setText(currentAccount.getAccountName());
            
            if (typeLabel != null) {
                typeLabel.setText(currentAccount.getAccountType().toString());
                typeLabel.setBackground(currentAccount.getAccountType() == src.auth.Account.AccountType.BUSINESS 
                    ? ModernTheme.PRIMARY : ModernTheme.SUCCESS);
            }
        }
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
    cardDetailPanel.add(ModernTheme.createPlaceholderLabel("Select a card to view details."), BorderLayout.CENTER);
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
        cardDetailPanel.add(ModernTheme.createPlaceholderLabel("Select a card to view details."), BorderLayout.CENTER);
        cardDetailPanel.revalidate(); cardDetailPanel.repaint();
        return;
    }

    JPanel detailGrid = new JPanel(new GridLayout(0, 1, 5, 10)); // Single column
    detailGrid.setBackground(ModernTheme.SURFACE);

    JLabel title = new JLabel(card.getCardName());
    title.setFont(ModernTheme.FONT_SUBTITLE);
    title.setForeground(ModernTheme.TEXT_PRIMARY);
    detailGrid.add(title);
    
    JLabel cardTypeLabel = new JLabel(card.getCardType());
    cardTypeLabel.setFont(ModernTheme.FONT_BODY);
    cardTypeLabel.setForeground(ModernTheme.TEXT_PRIMARY);
    detailGrid.add(cardTypeLabel);
    
    JLabel numberLabel = new JLabel("Number: " + card.getMaskedCardNumber()); // Show masked number
    numberLabel.setFont(ModernTheme.FONT_BODY);
    numberLabel.setForeground(ModernTheme.TEXT_PRIMARY);
    detailGrid.add(numberLabel);
    
    JLabel validThruLabel = new JLabel("Valid Thru: " + (card.getValidThrough() != null ? card.getValidThrough() : "N/A"));
    validThruLabel.setFont(ModernTheme.FONT_BODY);
    validThruLabel.setForeground(ModernTheme.TEXT_PRIMARY);
    detailGrid.add(validThruLabel);

    detailGrid.add(new JSeparator());

    if ("Credit Card".equals(card.getCardType())) {
        JLabel limitLabel = new JLabel(String.format("Limit: ₹%.2f", card.getCreditLimit()));
        limitLabel.setFont(ModernTheme.FONT_BODY);
        limitLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        detailGrid.add(limitLabel);
        
        JLabel spendLabel = new JLabel(String.format("Current Spend: ₹%.2f", card.getCurrentExpenses()));
        spendLabel.setFont(ModernTheme.FONT_BODY);
        spendLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        detailGrid.add(spendLabel);
        
        JLabel dueLabel = new JLabel(String.format("Amount Due: ₹%.2f", card.getAmountToPay()));
        dueLabel.setFont(ModernTheme.FONT_BODY);
        dueLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        detailGrid.add(dueLabel);
        
        JLabel daysLabel = new JLabel("Days Until Due: " + card.getDaysLeftToPay());
        daysLabel.setFont(ModernTheme.FONT_BODY);
        daysLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        detailGrid.add(daysLabel);
        
        detailGrid.add(new JSeparator());
    }

    // --- Buttons for Actions ---
    JPanel buttonSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttonSubPanel.setBackground(ModernTheme.SURFACE);
    
    JButton viewSensitiveButton = ModernTheme.createPrimaryButton("View Full Details (Requires OTP)");
    JButton editButton = ModernTheme.createSecondaryButton("Edit Basic Info");

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
        investmentDetailPanel.add(ModernTheme.createPlaceholderLabel("Select an investment to view details."), BorderLayout.CENTER);
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
        investmentDetailPanel.setBackground(ModernTheme.SURFACE);
        
        if (inv == null) {
            JLabel selectLabel = ModernTheme.createPlaceholderLabel("Select an investment to view details.");
            investmentDetailPanel.add(selectLabel, BorderLayout.CENTER);
        } else {
            JPanel detailGrid = new JPanel(new GridLayout(0, 1, 5, 10)); // Single column layout
            detailGrid.setBackground(ModernTheme.SURFACE);
            
            String titleName = (inv.getDescription() != null && !inv.getDescription().isEmpty()) ? inv.getDescription() : inv.getTickerSymbol();
            JLabel title = new JLabel(titleName + " (" + inv.getAssetType() + ")");
            title.setFont(ModernTheme.FONT_SUBTITLE);
            title.setForeground(ModernTheme.TEXT_PRIMARY);
            detailGrid.add(title);
            
            JLabel holderLabel = new JLabel("Holder: " + inv.getHolderName());
            holderLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            holderLabel.setFont(ModernTheme.FONT_BODY);
            detailGrid.add(holderLabel);

            JSeparator sep1 = new JSeparator();
            sep1.setForeground(ModernTheme.BORDER);
            detailGrid.add(sep1);
            
            // Calculated Values
            double initialCost = inv.getTotalInitialCost();
            double currentValue = inv.getTotalCurrentValue();
            double pnl = inv.getProfitOrLoss();
            double pnlPercent = inv.getProfitOrLossPercentage();

            JLabel initialCostLabel = new JLabel(String.format("Total Initial Cost: ₹%.2f", initialCost));
            initialCostLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            initialCostLabel.setFont(ModernTheme.FONT_BODY);
            detailGrid.add(initialCostLabel);
            
            JLabel currentValueLabel = new JLabel(String.format("Total Current Value: ₹%.2f", currentValue));
            currentValueLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            currentValueLabel.setFont(ModernTheme.FONT_BODY);
            detailGrid.add(currentValueLabel);
            
            // Profit/Loss Label
            String pnlText = String.format("Profit/Loss: ₹%.2f (%.2f%%)", pnl, pnlPercent);
            JLabel pnlLabel = new JLabel(pnlText);
            // Set color based on profit or loss using theme colors
            if (pnl > 0) {
                pnlLabel.setForeground(ModernTheme.SUCCESS); // Green for profit
            } else if (pnl < 0) {
                pnlLabel.setForeground(ModernTheme.DANGER); // Red for loss
            } else {
                pnlLabel.setForeground(ModernTheme.TEXT_PRIMARY); // Default for no change
            }
            pnlLabel.setFont(ModernTheme.FONT_HEADER);
            detailGrid.add(pnlLabel);

            JSeparator sep2 = new JSeparator();
            sep2.setForeground(ModernTheme.BORDER);
            detailGrid.add(sep2);
            
            // Unit Details (if applicable)
            if (inv.getQuantity() > 0) {
                JLabel quantityLabel = new JLabel(String.format("Quantity: %.4f units", inv.getQuantity()));
                quantityLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                quantityLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(quantityLabel);
                
                JLabel avgCostLabel = new JLabel(String.format("Avg. Cost Price: ₹%.2f /unit", inv.getInitialUnitCost()));
                avgCostLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                avgCostLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(avgCostLabel);
                
                JLabel currentPriceLabel = new JLabel(String.format("Current Price: ₹%.2f /unit", inv.getCurrentUnitPrice()));
                currentPriceLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                currentPriceLabel.setFont(ModernTheme.FONT_BODY);
                detailGrid.add(currentPriceLabel);
            }

            // --- Buttons for Actions ---
            JPanel buttonSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttonSubPanel.setBackground(ModernTheme.SURFACE);
            
            JButton updatePriceButton = ModernTheme.createSecondaryButton("Update Current Price");
            JButton editButton = ModernTheme.createPrimaryButton("Edit Details");
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
    taxDetailPanel.add(ModernTheme.createPlaceholderLabel("Select a profile to view details."), BorderLayout.CENTER);
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
    taxDetailPanel.setBackground(ModernTheme.SURFACE);
    
    if (tp == null) {
        JLabel selectLabel = ModernTheme.createPlaceholderLabel("Select a profile to view details.");
        taxDetailPanel.add(selectLabel, BorderLayout.CENTER);
    } else {
        // Create main content panel with efficient layout
        JPanel mainContent = new JPanel(new BorderLayout(8, 8));
        mainContent.setBackground(ModernTheme.SURFACE);

        // === TOP SECTION - Header Info ===
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        headerPanel.setBackground(ModernTheme.SURFACE);
        
        JLabel title = new JLabel(tp.getProfileName() + " (" + tp.getFinancialYear() + ")");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_PRIMARY);
        headerPanel.add(title);
        
        JLabel profileTypeLabel = new JLabel("Profile Type: " + tp.getProfileType());
        profileTypeLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        profileTypeLabel.setFont(ModernTheme.FONT_SMALL);
        headerPanel.add(profileTypeLabel);

        // === CENTER SECTION - Financial Data in Compact Grid ===
        JPanel financialPanel = new JPanel(new GridLayout(2, 2, 15, 8));
        financialPanel.setBackground(ModernTheme.SURFACE);
        financialPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ModernTheme.BORDER, 1), 
            "Financial Summary",
            0, 0, ModernTheme.FONT_SMALL, ModernTheme.TEXT_SECONDARY));

        // Income section
        JPanel incomePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        incomePanel.setBackground(ModernTheme.SURFACE);
        JLabel grossLabel = new JLabel(String.format("Gross Income: ₹%.2f", tp.getGrossIncome()));
        grossLabel.setFont(ModernTheme.FONT_BODY);
        grossLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        JLabel deducLabel = new JLabel(String.format("Deductions: -₹%.2f", tp.getTotalDeductions()));
        deducLabel.setForeground(ModernTheme.DANGER);
        deducLabel.setFont(ModernTheme.FONT_SMALL);
        incomePanel.add(grossLabel);
        incomePanel.add(deducLabel);

        // Taxable income - highlighted
        JPanel taxablePanel = new JPanel(new BorderLayout());
        taxablePanel.setBackground(ModernTheme.SURFACE);
        taxablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernTheme.PRIMARY, 2),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        JLabel taxableLabel = new JLabel(String.format("₹%.2f", tp.getTaxableIncome()));
        taxableLabel.setFont(ModernTheme.FONT_HEADER);
        taxableLabel.setForeground(ModernTheme.PRIMARY);
        taxableLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel taxableTitle = new JLabel("Taxable Income");
        taxableTitle.setFont(ModernTheme.FONT_SMALL);
        taxableTitle.setForeground(ModernTheme.TEXT_SECONDARY);
        taxableTitle.setHorizontalAlignment(SwingConstants.CENTER);
        taxablePanel.add(taxableTitle, BorderLayout.NORTH);
        taxablePanel.add(taxableLabel, BorderLayout.CENTER);

        // Tax paid
        JPanel taxPaidPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        taxPaidPanel.setBackground(ModernTheme.SURFACE);
        JLabel taxPaidTitle = new JLabel("Tax Paid (TDS):");
        taxPaidTitle.setFont(ModernTheme.FONT_SMALL);
        taxPaidTitle.setForeground(ModernTheme.TEXT_SECONDARY);
        JLabel taxPaidLabel = new JLabel(String.format("₹%.2f", tp.getTaxPaid()));
        taxPaidLabel.setForeground(ModernTheme.SUCCESS);
        taxPaidLabel.setFont(ModernTheme.FONT_BODY);
        taxPaidPanel.add(taxPaidTitle);
        taxPaidPanel.add(taxPaidLabel);

        financialPanel.add(incomePanel);
        financialPanel.add(taxablePanel);
        financialPanel.add(taxPaidPanel);
        financialPanel.add(new JLabel("")); // Empty space for balance

        // === BOTTOM SECTION - Notes and Actions ===
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setBackground(ModernTheme.SURFACE);

        // Compact notes section
        if (tp.getNotes() != null && !tp.getNotes().isEmpty()) {
            JPanel notesPanel = new JPanel(new BorderLayout());
            notesPanel.setBackground(ModernTheme.SURFACE);
            notesPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1), 
                "Notes",
                0, 0, ModernTheme.FONT_SMALL, ModernTheme.TEXT_SECONDARY));
            
            JLabel notesLabel = new JLabel("<html>" + tp.getNotes().replace("\n", "<br>") + "</html>");
            notesLabel.setFont(ModernTheme.FONT_SMALL);
            notesLabel.setForeground(ModernTheme.TEXT_SECONDARY);
            notesLabel.setVerticalAlignment(SwingConstants.TOP);
            notesPanel.add(notesLabel, BorderLayout.CENTER);
            bottomPanel.add(notesPanel, BorderLayout.CENTER);
        }

        // Action button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(ModernTheme.SURFACE);
        JButton editButton = ModernTheme.createPrimaryButton("Edit Profile");
        editButton.addActionListener(e -> openAddEditTaxProfileDialog(tp));
        buttonPanel.add(editButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Assemble everything
        mainContent.add(headerPanel, BorderLayout.NORTH);
        mainContent.add(financialPanel, BorderLayout.CENTER);
        mainContent.add(bottomPanel, BorderLayout.SOUTH);
        
        taxDetailPanel.add(mainContent, BorderLayout.CENTER);
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
                String selectedMonth = (String) monthComboBox.getSelectedItem();
                if (selectedMonth == null || "All Months".equals(selectedMonth)) {
                    JOptionPane.showMessageDialog(this, "Please select a specific month to export.", "Export Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Export from the current filtered table
                JTable currentTable = transactionsTable;
                String selectedYear = (String) yearComboBox.getSelectedItem();
                
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
                defaultFilename = selectedMonth + "_" + selectedYear + "_Transactions";
                
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
    loanDetailPanel.add(ModernTheme.createPlaceholderLabel("Select a loan to view details."), BorderLayout.CENTER);
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
    loanDetailPanel.setBackground(ModernTheme.SURFACE);
    
    if (loan == null) {
        JLabel selectLabel = ModernTheme.createPlaceholderLabel("Select a loan to view details.");
        loanDetailPanel.add(selectLabel, BorderLayout.CENTER);
    } else {
        JPanel detailGrid = new JPanel(new GridBagLayout());
        detailGrid.setBackground(ModernTheme.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // --- Title ---
        JLabel title = new JLabel(loan.getLenderName() + " - " + loan.getLoanType());
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        detailGrid.add(title, gbc);

        // --- EMI ---
        JLabel emiLabel = new JLabel(String.format("Monthly EMI: ₹%.2f", loan.getEmiAmount()));
        emiLabel.setFont(ModernTheme.FONT_HEADER);
        emiLabel.setForeground(ModernTheme.DANGER); // Red for EMI (expense)
        gbc.gridy = row++;
        detailGrid.add(emiLabel, gbc);

        gbc.gridy = row++; gbc.insets = new Insets(5, 0, 5, 0);
        detailGrid.add(new JSeparator(), gbc);
        gbc.insets = new Insets(4, 5, 4, 5);

        // --- Loan Details ---
        gbc.gridwidth = 1; // Reset to 1 column
        
        JLabel principalLabelKey = new JLabel("Principal Amount:");
        principalLabelKey.setForeground(ModernTheme.TEXT_SECONDARY);
        principalLabelKey.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(principalLabelKey, gbc);
        
        JLabel principalLabelValue = new JLabel(String.format("₹%.2f", loan.getPrincipalAmount()));
        principalLabelValue.setForeground(ModernTheme.TEXT_PRIMARY);
        principalLabelValue.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 1; detailGrid.add(principalLabelValue, gbc);
        row++;

        JLabel rateLabelKey = new JLabel("Annual Interest Rate:");
        rateLabelKey.setForeground(ModernTheme.TEXT_SECONDARY);
        rateLabelKey.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(rateLabelKey, gbc);
        
        JLabel rateLabelValue = new JLabel(String.format("%.2f %%", loan.getInterestRate()));
        rateLabelValue.setForeground(ModernTheme.TEXT_PRIMARY);
        rateLabelValue.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 1; detailGrid.add(rateLabelValue, gbc);
        row++;

        JLabel tenureLabelKey = new JLabel("Tenure:");
        tenureLabelKey.setForeground(ModernTheme.TEXT_SECONDARY);
        tenureLabelKey.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(tenureLabelKey, gbc);
        
        JLabel tenureLabelValue = new JLabel(loan.getTenureMonths() + " months");
        tenureLabelValue.setForeground(ModernTheme.TEXT_PRIMARY);
        tenureLabelValue.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 1; detailGrid.add(tenureLabelValue, gbc);
        row++;

        JLabel dateLabelKey = new JLabel("Start Date:");
        dateLabelKey.setForeground(ModernTheme.TEXT_SECONDARY);
        dateLabelKey.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(dateLabelKey, gbc);
        
        JLabel dateLabelValue = new JLabel(loan.getStartDate() != null ? loan.getStartDate() : "N/A");
        dateLabelValue.setForeground(ModernTheme.TEXT_PRIMARY);
        dateLabelValue.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 1; detailGrid.add(dateLabelValue, gbc);
        row++;

        JLabel statusLabelKey = new JLabel("Status:");
        statusLabelKey.setForeground(ModernTheme.TEXT_SECONDARY);
        statusLabelKey.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(statusLabelKey, gbc);
        
        JLabel statusLabelValue = new JLabel(loan.getStatus());
        statusLabelValue.setForeground("Active".equals(loan.getStatus()) ? ModernTheme.SUCCESS : ModernTheme.TEXT_PRIMARY);
        statusLabelValue.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 1; detailGrid.add(statusLabelValue, gbc);
        row++;

        // --- Total Payment Details ---
        gbc.gridy = row++; gbc.gridwidth = 2; gbc.insets = new Insets(5, 0, 5, 0);
        JSeparator sep = new JSeparator();
        sep.setForeground(ModernTheme.BORDER);
        detailGrid.add(sep, gbc);
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.gridwidth = 1;

        JLabel totalPrincipalKey = new JLabel("Total Principal Paid:");
        totalPrincipalKey.setForeground(ModernTheme.TEXT_SECONDARY);
        totalPrincipalKey.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(totalPrincipalKey, gbc);
        
        JLabel totalPrincipalValue = new JLabel(String.format("₹%.2f", loan.getPrincipalAmount()));
        totalPrincipalValue.setForeground(ModernTheme.TEXT_PRIMARY);
        totalPrincipalValue.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 1; detailGrid.add(totalPrincipalValue, gbc);
        row++;

        JLabel totalInterestKey = new JLabel("Total Interest Paid:");
        totalInterestKey.setForeground(ModernTheme.TEXT_SECONDARY);
        totalInterestKey.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(totalInterestKey, gbc);
        
        JLabel totalInterestValue = new JLabel(String.format("₹%.2f", loan.getTotalInterest()));
        totalInterestValue.setForeground(ModernTheme.DANGER);
        totalInterestValue.setFont(ModernTheme.FONT_BODY);
        gbc.gridx = 1; detailGrid.add(totalInterestValue, gbc);
        row++;

        JLabel totalPayKey = new JLabel("Total Payment:");
        totalPayKey.setForeground(ModernTheme.TEXT_SECONDARY);
        totalPayKey.setFont(ModernTheme.FONT_HEADER);
        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(totalPayKey, gbc);
        
        JLabel totalPayLabel = new JLabel(String.format("₹%.2f", loan.getTotalPayment()));
        totalPayLabel.setFont(ModernTheme.FONT_HEADER);
        totalPayLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        gbc.gridx = 1; detailGrid.add(totalPayLabel, gbc);
        row++;

        // --- Buttons ---
        JPanel buttonSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonSubPanel.setBackground(ModernTheme.SURFACE);
        JButton editButton = ModernTheme.createPrimaryButton("Edit / Mark as Paid");
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
// ==================================================================
// ===               LENDING UI METHODS                         ===
// ==================================================================

/**
 * Refreshes the list of lending records on the left side.
 * Make this PUBLIC so dialogs can call it.
 */
public void refreshLendings() {
    System.out.println("--- Refreshing Lendings ---");
    lendingListModel.clear();
    lendingDetailPanel.removeAll();
    lendingDetailPanel.add(ModernTheme.createPlaceholderLabel("Select a lending record to view details."), BorderLayout.CENTER);
    lendingDetailPanel.revalidate();
    lendingDetailPanel.repaint();
    try {
        List<Lending> lendings = manager.getAllLendings();
        System.out.println("Fetched " + lendings.size() + " lending records.");
        for (Lending l : lendings) {
            lendingListModel.addElement(l);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading lending records: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

/**
 * Shows calculated details for the selected lending record.
 */
private void showLendingDetails(Lending lending) {
    lendingDetailPanel.removeAll();
    lendingDetailPanel.setBackground(ModernTheme.SURFACE);
    
    if (lending == null) {
        JLabel selectLabel = ModernTheme.createPlaceholderLabel("Select a lending record to view details.");
        lendingDetailPanel.add(selectLabel, BorderLayout.CENTER);
    } else {
        JPanel detailGrid = new JPanel(new GridBagLayout());
        detailGrid.setBackground(ModernTheme.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // --- Title ---
        JLabel title = new JLabel(lending.getBorrowerName() + " (" + lending.getLoanType() + ")");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        detailGrid.add(title, gbc);

        // --- EMI (What they owe you) ---
        JLabel emiLabel = new JLabel(String.format("Expected Monthly Payment: ₹%.2f", lending.getMonthlyPayment()));
        emiLabel.setFont(ModernTheme.FONT_HEADER);
        emiLabel.setForeground(ModernTheme.SUCCESS); // Green for income
        gbc.gridy = row++;
        detailGrid.add(emiLabel, gbc);

        gbc.gridy = row++; gbc.insets = new Insets(5, 0, 5, 0);
        detailGrid.add(new JSeparator(), gbc);
        gbc.insets = new Insets(4, 5, 4, 5);

        // --- Loan Details ---
        gbc.gridwidth = 1; // Reset to 1 column
        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Principal Lent:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(String.format("₹%.2f", lending.getPrincipalAmount())), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Annual Interest Rate:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(String.format("%.2f %%", lending.getInterestRate())), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Tenure:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(lending.getTenureMonths() + " months"), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Date Lent:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(lending.getDateLent() != null ? lending.getDateLent() : "N/A"), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(lending.getStatus()), gbc);
        row++;

        // --- Total Payment Details ---
        gbc.gridy = row++; gbc.gridwidth = 2; gbc.insets = new Insets(5, 0, 5, 0);
        detailGrid.add(new JSeparator(), gbc);
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Total Principal:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(String.format("₹%.2f", lending.getPrincipalAmount())), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Total Interest to Receive:"), gbc);
        gbc.gridx = 1; detailGrid.add(new JLabel(String.format("₹%.2f", lending.getTotalInterestToReceive())), gbc);
        row++;

        JLabel totalPayLabel = new JLabel(String.format("₹%.2f", lending.getTotalToReceive()));
        totalPayLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = row; detailGrid.add(new JLabel("Total to Receive:"), gbc);
        gbc.gridx = 1; detailGrid.add(totalPayLabel, gbc);
        row++;

        // --- Buttons ---
        JPanel buttonSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editButton = new JButton("Edit / Mark as Repaid");
        editButton.addActionListener(e -> openAddEditLendingDialog(lending));
        buttonSubPanel.add(editButton);

        lendingDetailPanel.add(detailGrid, BorderLayout.NORTH);
        lendingDetailPanel.add(buttonSubPanel, BorderLayout.CENTER);
    }
    lendingDetailPanel.revalidate();
    lendingDetailPanel.repaint();
}

/**
 * Opens the dialog to add/edit a lending record (Placeholder).
 */
private void openAddEditLendingDialog(Lending lendingToEdit) {
    AddEditLendingDialog dialog = new AddEditLendingDialog(this, manager, lendingToEdit, this);
    dialog.setVisible(true);
    // Refresh is handled by the dialog on success
}

/**
 * Deletes the selected lending record (moves to recycle bin).
 */
private void deleteSelectedLending() {
    Lending selected = lendingList.getSelectedValue();
    if (selected == null) {
        JOptionPane.showMessageDialog(this, "Please select a record to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
        return;
    }

    int choice = JOptionPane.showConfirmDialog(this,
        "Move this lending record to the recycle bin?\n" + selected.toString(),
        "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

    if (choice == JOptionPane.YES_OPTION) {
        try {
            manager.moveLendingToRecycleBin(selected.getId());
            refreshLendings(); // Refresh the list
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting lending record: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}

/**
 * Opens the Lending Recycle Bin dialog (Placeholder).
 */
private void openLendingRecycleBin() {
     LendingRecycleBinDialog dialog = new LendingRecycleBinDialog(this, manager, this);
     dialog.setVisible(true);
     // The main list will refresh via the callback 'refreshAfterLendingRestore' if needed
}

/**
 * Callback method for the Lending Recycle Bin dialog (Placeholder).
 * Make sure this is PUBLIC.
 */
public void refreshAfterLendingRestore() {
    System.out.println("Refreshing lending list after restore...");
    refreshLendings();
}

}