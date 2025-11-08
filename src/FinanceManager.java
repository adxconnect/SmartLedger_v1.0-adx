package src;
import src.Investment;
import src.SummaryData;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import src.auth.AuthManager;
import src.auth.SessionContext;
import src.db.DBHelper;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import src.Lending;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
//Same-package types don't require imports.

public class FinanceManager {
    private List<Transaction> transactions = new ArrayList<>();
    private DBHelper dbHelper;
    private Connection connection;
    private AuthManager authManager;

    private static final List<String> ACCOUNT_SCOPED_TABLES = Arrays.asList(
        "transactions",
        "recycle_bin_transactions",
        "bank_accounts",
    "recycle_bin_bank_accounts",
        "deposits",
        "recycle_bin_deposits",
        "cards",
        "recycle_bin_cards",
        "investments",
        "recycle_bin_investments",
        "loans",
        "recycle_bin_loans",
        "lendings",
        "recycle_bin_lendings",
        "tax_profiles",
        "recycle_bin_tax_profiles"
    );
    private static void launch() {
    // --- ADD THIS LINE ---
    registerGoogleFonts(); // Load fonts BEFORE creating any UI
    // --- END OF ADDITION ---

    SessionContext.clear();
    DBHelper helper = null;
    // ... rest of your launch method ...
}

    public FinanceManager() throws SQLException { 
        dbHelper = new DBHelper();
        connection = dbHelper.getConnection();
        authManager = new AuthManager(connection);
        this.cards = new ArrayList<>();
        createTables();
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    private int requireAccountId() {
        Integer accountId = SessionContext.getCurrentAccountId();
        if (accountId == null) {
            throw new IllegalStateException("No account in session. Please log in first.");
        }
        return accountId;
    }

    // --- Transaction (File) Methods (for console app) ---
    public void addTransaction(Transaction t) {
        transactions.add(t);
    }
    
    public void viewAllTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }
        for (Transaction t : transactions)
            System.out.println(t);
    }

    public double calculateBalance() {
        double balance = 0;
        for (Transaction t : transactions) {
            if (t.getType().equalsIgnoreCase("Income"))
                balance += t.getAmount();
            else
                balance -= t.getAmount();
        }
        return balance;
    }
    
    public boolean deleteTransaction(int index) {
        if (index >= 0 && index < transactions.size()) {
            transactions.remove(index);
            return true;
        }
        return false;
    }
    
    public void viewTransactionsWithIndex() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }
        for (int i = 0; i < transactions.size(); i++) {
            System.out.println(i + ": " + transactions.get(i));
        }
    }
    
    public void saveToFile(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (Transaction t : transactions) {
                pw.println(t.toCSV());
            }
            System.out.println("Transactions saved to " + filename);
        } catch (IOException e) {
            System.out.println("Error saving transactions: " + e.getMessage());
        }
    }
    
    public void loadFromFile(String filename) {
        transactions.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                transactions.add(Transaction.fromCSV(line));
            }
            System.out.println("Transactions loaded from " + filename);
        } catch (FileNotFoundException e) {
            System.out.println("No previous data found. Starting fresh.");
        } catch (IOException e) {
            System.out.println("Error loading transactions: " + e.getMessage());
        }
    }
    
    public void showMonthlySummary(String month) {
        double totalIncome = 0, totalExpense = 0;
        for (Transaction t : transactions) {
            if (t.getMonth().equalsIgnoreCase(month)) {
                if (t.getType().equalsIgnoreCase("Income"))
                    totalIncome += t.getAmount();
                else if (t.getType().equalsIgnoreCase("Expense"))
                    totalExpense += t.getAmount();
            }
        }
        System.out.println("\n--- Monthly Summary for " + month + " ---");
        System.out.println("Total Income: ₹" + totalIncome);
        System.out.println("Total Expense: ₹" + totalExpense);
        System.out.println("Net Balance: ₹" + (totalIncome - totalExpense));
    }
    
    public void showCategorySummary() {
        Map<String, Double> categorySummary = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType().equalsIgnoreCase("Expense")) {
                String category = t.getCategory(); // Simpler now
                categorySummary.put(
                    category,
                    categorySummary.getOrDefault(category, 0.0) + t.getAmount()
                );
            }
        }
        System.out.println("\n--- Category-wise Expense Summary ---");
        for (String category : categorySummary.keySet()) {
            System.out.println(category + ": ₹" + categorySummary.get(category));
        }
    }

    // --- Budget (File) Methods (for console app) ---
    private double monthlyBudget = 0;
    public void setMonthlyBudget(double budget) {
        this.monthlyBudget = budget;
        System.out.println("Monthly budget set to ₹" + budget);
    }
    
    public double getTotalExpenses() {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.getType().equalsIgnoreCase("Expense"))
                total += t.getAmount();
        }
        return total;
    }
    
    public void checkBudgetStatus() {
        double expenses = getTotalExpenses();
        System.out.println("\n--- Budget Summary ---");
        System.out.println("Budget Limit: ₹" + monthlyBudget);
        System.out.println("Expenses So Far: ₹" + expenses);
        if (monthlyBudget == 0) {
            System.out.println("Budget not set yet!");
        } else if (expenses > monthlyBudget) {
            System.out.println("Alert: You have exceeded your monthly budget!");
        } else if (expenses >= monthlyBudget * 0.9) {
            System.out.println("Warning: You have reached 90% of your budget.");
        } else {
            System.out.println("Good job! You are within budget.");
        }
    }

    // --- Old In-Memory Lists (for console app) ---

    @SuppressWarnings("unused")
    private List<Card> cards = new ArrayList<>();
    
    // --- Bank Account (Database) Methods ---
    public void saveBankAccount(BankAccount ba) throws SQLException {
        int accountId = requireAccountId();
        String sql = "INSERT INTO bank_accounts (account_id, account_number, holder_name, bank_name, ifsc_code, balance, "
                + "account_type, interest_rate, annual_expense, account_subtype, company_name, business_name) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, ba.getAccountNumber());
            ps.setString(3, ba.getHolderName());
            ps.setString(4, ba.getBankName());
            ps.setString(5, ba.getIfscCode());
            ps.setDouble(6, ba.getBalance());
            ps.setString(7, ba.getAccountType());
            ps.setDouble(8, ba.getInterestRate());
            ps.setDouble(9, ba.getAnnualExpense());
            ps.setString(10, ba.getAccountSubtype());
            ps.setString(11, ba.getCompanyName());
            ps.setString(12, ba.getBusinessName());

            ps.executeUpdate();
        }
    }

    public List<BankAccount> getAllBankAccounts() throws SQLException {
        int accountId = requireAccountId();
        List<BankAccount> accounts = new ArrayList<>();
        String sql = "SELECT * FROM bank_accounts WHERE account_id = ? ORDER BY bank_name, account_number";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(new BankAccount(
                            rs.getInt("id"),
                            rs.getString("account_number"),
                            rs.getString("holder_name"),
                            rs.getString("bank_name"),
                            rs.getString("ifsc_code"),
                            rs.getDouble("balance"),
                            rs.getString("account_type"),
                            rs.getDouble("interest_rate"),
                            rs.getDouble("annual_expense"),
                            rs.getString("account_subtype"),
                            rs.getString("company_name"),
                            rs.getString("business_name")
                    ));
                }
            }
        }
        return accounts;
    }

    private void moveBankAccountToRecycleBin(int bankAccountId) throws SQLException {
        int accountId = requireAccountId();
        String copySql = "INSERT INTO recycle_bin_bank_accounts "
                + "(id, account_id, account_number, holder_name, bank_name, ifsc_code, balance, account_type, interest_rate, annual_expense, account_subtype, company_name, business_name) "
                + "SELECT id, account_id, account_number, holder_name, bank_name, ifsc_code, balance, account_type, interest_rate, annual_expense, account_subtype, company_name, business_name "
                + "FROM bank_accounts WHERE id = ? AND account_id = ?";
        String deleteSql = "DELETE FROM bank_accounts WHERE id = ? AND account_id = ?";

        Connection conn = connection;
        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try (PreparedStatement copyPs = conn.prepareStatement(copySql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {

            copyPs.setInt(1, bankAccountId);
            copyPs.setInt(2, accountId);
            deletePs.setInt(1, bankAccountId);
            deletePs.setInt(2, accountId);

            copyPs.executeUpdate();
            deletePs.executeUpdate();

            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    public void deleteBankAccount(int bankAccountId) throws SQLException {
        moveBankAccountToRecycleBin(bankAccountId);
    }

    public List<Map<String, Object>> getRecycledBankAccountsForUI() throws SQLException {
        int accountId = requireAccountId();
        List<Map<String, Object>> recycled = new ArrayList<>();
        String sql = "SELECT id, bank_name, account_number, holder_name, account_type, account_subtype, balance, interest_rate, annual_expense, company_name, business_name, "
                + "DATE_FORMAT(deleted_on, '%Y-%m-%d %H:%i:%s') AS deleted_on_str "
                + "FROM recycle_bin_bank_accounts WHERE account_id = ? ORDER BY deleted_on DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("bank_name", rs.getString("bank_name"));
                    row.put("account_number", rs.getString("account_number"));
                    row.put("holder_name", rs.getString("holder_name"));
                    row.put("account_type", rs.getString("account_type"));
                    row.put("account_subtype", rs.getString("account_subtype"));
                    row.put("balance", rs.getDouble("balance"));
                    row.put("interest_rate", rs.getDouble("interest_rate"));
                    row.put("annual_expense", rs.getDouble("annual_expense"));
                    row.put("company_name", rs.getString("company_name"));
                    row.put("business_name", rs.getString("business_name"));
                    row.put("deleted_on_str", rs.getString("deleted_on_str"));
                    recycled.add(row);
                }
            }
        }
        return recycled;
    }

    public void restoreBankAccount(int bankAccountId) throws SQLException {
        int accountId = requireAccountId();
        String copySql = "INSERT INTO bank_accounts (id, account_id, account_number, holder_name, bank_name, ifsc_code, balance, account_type, interest_rate, annual_expense, account_subtype, company_name, business_name) "
                + "SELECT id, account_id, account_number, holder_name, bank_name, ifsc_code, balance, account_type, interest_rate, annual_expense, account_subtype, company_name, business_name "
                + "FROM recycle_bin_bank_accounts WHERE id = ? AND account_id = ?";
        String deleteSql = "DELETE FROM recycle_bin_bank_accounts WHERE id = ? AND account_id = ?";

        Connection conn = connection;
        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try (PreparedStatement copyPs = conn.prepareStatement(copySql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {

            copyPs.setInt(1, bankAccountId);
            copyPs.setInt(2, accountId);
            deletePs.setInt(1, bankAccountId);
            deletePs.setInt(2, accountId);

            copyPs.executeUpdate();
            deletePs.executeUpdate();

            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            if (ex.getErrorCode() == 1062) {
                try (PreparedStatement deleteOnly = conn.prepareStatement(deleteSql)) {
                    deleteOnly.setInt(1, bankAccountId);
                    deleteOnly.setInt(2, accountId);
                    deleteOnly.executeUpdate();
                    conn.commit();
                }
            } else {
                throw ex;
            }
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    public void permanentlyDeleteBankAccount(int bankAccountId) throws SQLException {
        int accountId = requireAccountId();
        String sql = "DELETE FROM recycle_bin_bank_accounts WHERE id = ? AND account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bankAccountId);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }

    // --- Transaction (Database) Methods ---
    public void saveTransaction(Transaction t) throws SQLException {
        int accountId = requireAccountId();
        String sql = "INSERT INTO transactions (account_id, date, category, type, amount, description, day, payment_method, payee) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
            java.time.LocalDate localDate = java.time.LocalDate.parse(t.getDate(), formatter);
            java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);

            ps.setInt(1, accountId);
            ps.setDate(2, sqlDate);
            ps.setString(3, t.getCategory());
            ps.setString(4, t.getType());
            ps.setDouble(5, t.getAmount());
            ps.setString(6, t.getDescription());
            ps.setString(7, t.getDay());
            ps.setString(8, t.getPaymentMethod());
            ps.setString(9, t.getPayee());

            ps.executeUpdate();
        } catch (java.time.format.DateTimeParseException e) {
            throw new SQLException("Invalid date format. Please use DD-MM-YYYY.", e);
        }
    }

    // --- Fixed Deposit (Database) Methods ---
   

    // --- Card (Database) Methods ---
    public void saveCard(Card card) throws SQLException {
        System.out.println("DEBUG: Saving card: " + card.getCardName());
        int accountId = requireAccountId();
        String sql = "INSERT INTO cards (account_id, unique_id, card_name, card_type, card_number, valid_from, valid_through, cvv, " +
                     "front_image_path, back_image_path, credit_limit, current_expenses, amount_to_pay, days_left_to_pay, creation_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, accountId);
            ps.setString(2, java.util.UUID.randomUUID().toString());
            ps.setString(3, card.getCardName());
            ps.setString(4, card.getCardType());
            ps.setString(5, card.getCardNumber());
            ps.setString(6, card.getValidFrom());
            ps.setString(7, card.getValidThrough());
            ps.setString(8, card.getCvv());
            ps.setString(9, card.getFrontImagePath());
            ps.setString(10, card.getBackImagePath());

            boolean isCredit = "Credit Card".equals(card.getCardType());
            if (isCredit) {
                ps.setDouble(11, card.getCreditLimit());
                ps.setDouble(12, card.getCurrentExpenses());
                ps.setDouble(13, card.getAmountToPay());
                ps.setInt(14, card.getDaysLeftToPay());
            } else {
                ps.setNull(11, Types.DOUBLE);
                ps.setNull(12, Types.DOUBLE);
                ps.setNull(13, Types.DOUBLE);
                ps.setNull(14, Types.INTEGER);
            }

            java.sql.Date created = null;
            try {
                if (card.getCreationDate() != null) {
                    java.time.LocalDate ld = java.time.LocalDate.parse(card.getCreationDate());
                    created = java.sql.Date.valueOf(ld);
                }
            } catch (Exception ignore) { /* leave as null */ }
            if (created != null) {
                ps.setDate(15, created);
            } else {
                ps.setNull(15, Types.DATE);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    card.setId(keys.getInt(1));
                }
            }
        }
    }


   


    // --- File-Saving Methods (for console app) ---
    public void saveRecurringDeposits(String filename) { /* ... */ }
    public void loadRecurringDeposits(String filename) { /* ... */ }
    public void saveFixedDeposits(String filename) { /* ... */ }
    public void loadFixedDeposits(String filename) { /* ... */ }

    // --- CONSOLE APP'S DEPRECATED BANK METHODS ---
    public void addSavingsAccount(BankAccount sa) { /* ... */ }
    public void viewSavingsAccounts() { /* ... */ }


    // ==================================================================
    // ===         NEW/MODIFIED TRANSACTION & RECYCLE BIN METHODS     ===
    // ==================================================================

    /**
     * NEW: Gets a list of all distinct years from transactions to populate the dropdown.
     */
    public List<String> getAvailableYears() throws SQLException {
        int accountId = requireAccountId();
        List<String> years = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(date) as year FROM transactions WHERE account_id = ? ORDER BY year DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    years.add(String.valueOf(rs.getInt("year")));
                }
            }
        }
        if (years.isEmpty()) {
            years.add(String.valueOf(java.time.Year.now().getValue()));
        }
        years.add(0, "All Years"); 
        return years;
    }

    /**
     * MODIFIED: Now accepts a year to filter by.
     * Gets all transactions for a specific year, grouped by month.
     */
    public Map<String, List<Transaction>> getTransactionsGroupedByMonth(String year) throws SQLException {
        int accountId = requireAccountId();
        Map<String, List<Transaction>> groupedTransactions = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder("SELECT id, date, DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i:%s') as timestamp_str, "
                + "day, category, type, payment_method, payee, amount, description, "
                + "DATE_FORMAT(date, '%m-%Y') as month_year FROM transactions WHERE account_id = ? ");

        boolean filterByYear = year != null && !"All Years".equals(year);
        if (filterByYear) {
            sql.append("AND YEAR(date) = ? ");
        }
        sql.append("ORDER BY date ASC, id ASC");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setInt(1, accountId);
            if (filterByYear) {
                ps.setInt(2, Integer.parseInt(year));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String monthYear = rs.getString("month_year");
                    if (!groupedTransactions.containsKey(monthYear)) {
                        groupedTransactions.put(monthYear, new ArrayList<>());
                    }

                    Transaction t = new Transaction(
                            rs.getInt("id"),
                            new java.text.SimpleDateFormat("dd-MM-YYYY").format(rs.getDate("date")),
                            rs.getString("timestamp_str"),
                            rs.getString("day"),
                            rs.getString("category"),
                            rs.getString("type"),
                            rs.getDouble("amount"),
                            rs.getString("description"),
                            rs.getString("payment_method"),
                            rs.getString("payee")
                    );
                    groupedTransactions.get(monthYear).add(t);
                }
            }
        }
        return groupedTransactions;
    }
    
    // --- NEW RECYCLE BIN METHODS ---

    private void moveTransactionToRecycleBin(int transactionId) throws SQLException {
        int accountId = requireAccountId();
        String copySql = "INSERT INTO recycle_bin_transactions "
                + "(account_id, id, timestamp, date, day, payment_method, category, type, payee, description, amount) "
                + "SELECT account_id, id, timestamp, date, day, payment_method, category, type, payee, description, amount "
                + "FROM transactions WHERE id = ? AND account_id = ?";

        String deleteSql = "DELETE FROM transactions WHERE id = ? AND account_id = ?";

        Connection conn = connection;
        conn.setAutoCommit(false); 
        
        try (PreparedStatement copyPs = conn.prepareStatement(copySql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
            
            copyPs.setInt(1, transactionId);
            copyPs.setInt(2, accountId);
            deletePs.setInt(1, transactionId);
            deletePs.setInt(2, accountId);
            
            copyPs.executeUpdate();
            deletePs.executeUpdate();
            
            conn.commit();
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void deleteTransactionById(int transactionId) throws SQLException {
        moveTransactionToRecycleBin(transactionId);
    }

    public void deleteTransactionsByMonth(int year, int month) throws SQLException {
        int accountId = requireAccountId();
        List<Integer> idsToDelete = new ArrayList<>();
        String sql = "SELECT id FROM transactions WHERE account_id = ? AND YEAR(date) = ? AND MONTH(date) = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                idsToDelete.add(rs.getInt("id"));
            }
            rs.close();
        }

        for (int id : idsToDelete) {
            moveTransactionToRecycleBin(id);
        }
    }

    public void deleteTransactionsByYear(String year) throws SQLException {
        int accountId = requireAccountId();
        List<Integer> idsToDelete = new ArrayList<>();
        String sql = "SELECT id FROM transactions WHERE account_id = ? AND YEAR(date) = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, Integer.parseInt(year));
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                idsToDelete.add(rs.getInt("id"));
            }
            rs.close();
        }
        
        for (int id : idsToDelete) {
            moveTransactionToRecycleBin(id);
        }
    }
    // --- NEW RECYCLE BIN LOGIC METHODS ---

    /**
     * Fetches all transactions currently in the recycle bin.
     */
    public List<Transaction> getRecycledTransactions() throws SQLException {
        int accountId = requireAccountId();
        List<Transaction> recycledTxs = new ArrayList<>();
        String sql = "SELECT id, date, DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i:%s') as timestamp_str, "
                + "day, category, type, payment_method, payee, amount, description "
                + "FROM recycle_bin_transactions WHERE account_id = ? ORDER BY deleted_on DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    recycledTxs.add(new Transaction(
                            rs.getInt("id"),
                            new java.text.SimpleDateFormat("dd-MM-YYYY").format(rs.getDate("date")),
                            rs.getString("timestamp_str"),
                            rs.getString("day"),
                            rs.getString("category"),
                            rs.getString("type"),
                            rs.getDouble("amount"),
                            rs.getString("description"),
                            rs.getString("payment_method"),
                            rs.getString("payee")
                    ));
                }
            }
        }
        return recycledTxs;
    }

    /**
     * Restores a transaction from the recycle bin back to the main transactions table.
     */
    public void restoreTransaction(int transactionId) throws SQLException {
        // 1. Copy the transaction back to the main table
    int accountId = requireAccountId();
    String copySql = "INSERT INTO transactions "
        + "(id, account_id, timestamp, date, day, payment_method, category, type, payee, description, amount) "
        + "SELECT id, account_id, timestamp, date, day, payment_method, category, type, payee, description, amount "
        + "FROM recycle_bin_transactions WHERE id = ? AND account_id = ?";

    String deleteSql = "DELETE FROM recycle_bin_transactions WHERE id = ? AND account_id = ?";

    Connection conn = connection;
        conn.setAutoCommit(false); // Use a transaction
        
        try (PreparedStatement copyPs = conn.prepareStatement(copySql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
            
            copyPs.setInt(1, transactionId);
            copyPs.setInt(2, accountId);
            deletePs.setInt(1, transactionId);
            deletePs.setInt(2, accountId);
            
            copyPs.executeUpdate();
            deletePs.executeUpdate();
            
            conn.commit(); // Commit if both succeed
            
        } catch (SQLException e) {
            conn.rollback(); // Rollback if anything fails
            // Check if the error is a duplicate key error (meaning it was already restored or exists)
            if (e.getErrorCode() == 1062) { // MySQL error code for duplicate entry
                 // If it's a duplicate, just delete from recycle bin
                 System.out.println("Transaction " + transactionId + " already exists in main table. Removing from recycle bin.");
                 try (PreparedStatement deletePsOnly = conn.prepareStatement(deleteSql)) {
                     deletePsOnly.setInt(1, transactionId);
                     deletePsOnly.setInt(2, accountId);
                     deletePsOnly.executeUpdate();
                     conn.commit(); // Commit the delete
                 } catch (SQLException ex) {
                     conn.rollback();
                     throw ex; // Throw error from the delete attempt
                 }
            } else {
                 throw e; // Re-throw other errors
            }
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Permanently deletes a transaction from the recycle bin.
     */
    public void permanentlyDeleteTransaction(int transactionId) throws SQLException {
        int accountId = requireAccountId();
        String sql = "DELETE FROM recycle_bin_transactions WHERE id = ? AND account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }
    // Replace this method in src/FinanceManager.java
    public void saveDeposit(Deposit d) throws SQLException {
        int accountId = requireAccountId();
        String sql = "INSERT INTO deposits (account_id, deposit_type, holder_name, description, goal, "
                + "account_number, principal_amount, monthly_amount, interest_rate, "
                + "tenure, tenure_unit, start_date, current_total, last_updated, "
                + "count_500, count_200, count_100, count_50, count_20, count_10, count_5, count_2, count_1, gullak_due_amount) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, accountId);
            ps.setString(2, d.getDepositType());
            ps.setString(3, d.getHolderName());
            ps.setString(4, d.getDescription());
            ps.setString(5, d.getGoal());
            ps.setString(6, d.getAccountNumber());
            ps.setDouble(7, d.getPrincipalAmount());
            ps.setDouble(8, d.getMonthlyAmount());
            ps.setDouble(9, d.getInterestRate());
            ps.setInt(10, d.getTenure());
            ps.setString(11, d.getTenureUnit());

            // Handle date conversion for start_date
            if (d.getStartDate() != null && !d.getStartDate().isEmpty()) {
                try {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    java.time.LocalDate localDate = java.time.LocalDate.parse(d.getStartDate(), formatter);
                    ps.setDate(12, java.sql.Date.valueOf(localDate));
                } catch (Exception e) {
                    ps.setNull(12, java.sql.Types.DATE);
                    System.err.println("Invalid start date format for deposit: " + d.getStartDate());
                }
            } else {
                ps.setNull(12, java.sql.Types.DATE);
            }

            // Get calculated total for Gullak, otherwise 0
            double calculatedTotal = ("Gullak".equals(d.getDepositType())) ? d.calculateTotalFromDenominations() : 0.0;
            ps.setDouble(13, calculatedTotal);

            // Set last_updated only if it's a Gullak
            if ("Gullak".equals(d.getDepositType())) {
                ps.setTimestamp(14, new java.sql.Timestamp(System.currentTimeMillis()));
            } else {
                ps.setNull(14, java.sql.Types.TIMESTAMP);
            }

            // Set denomination counts (default to 0 if null or not Gullak)
            Map<Integer, Integer> counts = d.getDenominationCounts();
            ps.setInt(15, counts != null ? counts.getOrDefault(500, 0) : 0);
            ps.setInt(16, counts != null ? counts.getOrDefault(200, 0) : 0);
            ps.setInt(17, counts != null ? counts.getOrDefault(100, 0) : 0);
            ps.setInt(18, counts != null ? counts.getOrDefault(50, 0) : 0);
            ps.setInt(19, counts != null ? counts.getOrDefault(20, 0) : 0);
            ps.setInt(20, counts != null ? counts.getOrDefault(10, 0) : 0);
            ps.setInt(21, counts != null ? counts.getOrDefault(5, 0) : 0);
            ps.setInt(22, counts != null ? counts.getOrDefault(2, 0) : 0);
            ps.setInt(23, counts != null ? counts.getOrDefault(1, 0) : 0);

            // Set Gullak due amount
            ps.setDouble(24, d.getGullakDueAmount());


            ps.executeUpdate();

        } catch (Exception e) {
            throw new SQLException("Error saving deposit: " + e.getMessage(), e);
        }
    }
    // Replace this method in src/FinanceManager.java
    public List<Deposit> getAllDeposits() throws SQLException {
        int accountId = requireAccountId();
        List<Deposit> deposits = new ArrayList<>();
        String sql = "SELECT id, deposit_type, holder_name, description, goal, "
                + "DATE_FORMAT(creation_date, '%Y-%m-%d %H:%i:%s') as creation_date_str, "
                + "account_number, principal_amount, monthly_amount, interest_rate, "
                + "tenure, tenure_unit, start_date, current_total, "
                + "DATE_FORMAT(last_updated, '%Y-%m-%d %H:%i:%s') as last_updated_str, "
                + "count_500, count_200, count_100, count_50, count_20, count_10, count_5, count_2, count_1, gullak_due_amount "
                + "FROM deposits WHERE account_id = ? ORDER BY creation_date DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlStartDate = rs.getDate("start_date");
                    String formattedStartDate = (sqlStartDate == null)
                            ? null
                            : new java.text.SimpleDateFormat("dd-MM-yyyy").format(sqlStartDate);

                    Map<Integer, Integer> counts = new HashMap<>();
                    counts.put(500, rs.getInt("count_500"));
                    counts.put(200, rs.getInt("count_200"));
                    counts.put(100, rs.getInt("count_100"));
                    counts.put(50, rs.getInt("count_50"));
                    counts.put(20, rs.getInt("count_20"));
                    counts.put(10, rs.getInt("count_10"));
                    counts.put(5, rs.getInt("count_5"));
                    counts.put(2, rs.getInt("count_2"));
                    counts.put(1, rs.getInt("count_1"));

                    double gullakDueAmount = rs.getDouble("gullak_due_amount");

                    deposits.add(new Deposit(
                            rs.getInt("id"),
                            rs.getString("deposit_type"),
                            rs.getString("holder_name"),
                            rs.getString("description"),
                            rs.getString("goal"),
                            rs.getString("creation_date_str"),
                            rs.getString("account_number"),
                            rs.getDouble("principal_amount"),
                            rs.getDouble("monthly_amount"),
                            rs.getDouble("interest_rate"),
                            rs.getInt("tenure"),
                            rs.getString("tenure_unit"),
                            formattedStartDate,
                            rs.getDouble("current_total"),
                            rs.getString("last_updated_str"),
                            gullakDueAmount,
                            counts
                    ));
                }
            }
        }
        return deposits;
    }
    // Replace updateGullakTotal with this method in src/FinanceManager.java
    public void updateGullakDetails(int depositId, Map<Integer, Integer> counts, double newDueAmount) throws SQLException {
         // Calculate the new total based on the provided counts
         double newTotal = 0;
         if (counts != null) {
             for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                 newTotal += entry.getKey() * entry.getValue();
             }
         }

         // Update counts, total, due amount, and timestamp
         int accountId = requireAccountId();
         String sql = "UPDATE deposits SET " +
                      "current_total = ?, last_updated = CURRENT_TIMESTAMP, " +
                      "count_500 = ?, count_200 = ?, count_100 = ?, count_50 = ?, count_20 = ?, " +
                      "count_10 = ?, count_5 = ?, count_2 = ?, count_1 = ?, " +
                      "gullak_due_amount = ? " +
                      "WHERE id = ? AND deposit_type = 'Gullak' AND account_id = ?";

         try (PreparedStatement ps = connection.prepareStatement(sql)) {
             ps.setDouble(1, newTotal); // Calculated total
             // Set counts
             ps.setInt(2, counts != null ? counts.getOrDefault(500, 0) : 0);
             ps.setInt(3, counts != null ? counts.getOrDefault(200, 0) : 0);
             ps.setInt(4, counts != null ? counts.getOrDefault(100, 0) : 0);
             ps.setInt(5, counts != null ? counts.getOrDefault(50, 0) : 0);
             ps.setInt(6, counts != null ? counts.getOrDefault(20, 0) : 0);
             ps.setInt(7, counts != null ? counts.getOrDefault(10, 0) : 0);
             ps.setInt(8, counts != null ? counts.getOrDefault(5, 0) : 0);
             ps.setInt(9, counts != null ? counts.getOrDefault(2, 0) : 0);
             ps.setInt(10, counts != null ? counts.getOrDefault(1, 0) : 0);
             // Set due amount
             ps.setDouble(11, newDueAmount);
             // Set ID
             ps.setInt(12, depositId);
             ps.setInt(13, accountId);

             ps.executeUpdate();
         }
     }
     // Replace these methods in src/FinanceManager.java

    // --- DEPOSIT RECYCLE BIN METHODS (UPDATED) ---

    private void moveDepositToRecycleBin(int depositId) throws SQLException {
        int accountId = requireAccountId();
        String copySql = "INSERT INTO recycle_bin_deposits SELECT *, NOW() FROM deposits WHERE id = ? AND account_id = ?";
        String deleteSql = "DELETE FROM deposits WHERE id = ? AND account_id = ?";

        Connection conn = connection;
        conn.setAutoCommit(false);

        try (PreparedStatement copyPs = conn.prepareStatement(copySql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {

            copyPs.setInt(1, depositId);
            copyPs.setInt(2, accountId);
            deletePs.setInt(1, depositId);
            deletePs.setInt(2, accountId);

            copyPs.executeUpdate();
            deletePs.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void deleteDepositById(int depositId) throws SQLException {
        moveDepositToRecycleBin(depositId);
    }

    public List<Deposit> getRecycledDeposits() throws SQLException {
        int accountId = requireAccountId();
        List<Deposit> recycled = new ArrayList<>();
        String sql = "SELECT id, deposit_type, holder_name, description, goal, "
                + "DATE_FORMAT(creation_date, '%Y-%m-%d %H:%i:%s') as creation_date_str, "
                + "account_number, principal_amount, monthly_amount, interest_rate, "
                + "tenure, tenure_unit, start_date, current_total, "
                + "DATE_FORMAT(last_updated, '%Y-%m-%d %H:%i:%s') as last_updated_str, "
                + "count_500, count_200, count_100, count_50, count_20, count_10, count_5, count_2, count_1, gullak_due_amount, "
                + "DATE_FORMAT(deleted_on, '%Y-%m-%d %H:%i:%s') as deleted_on_str "
                + "FROM recycle_bin_deposits WHERE account_id = ? ORDER BY deleted_on DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                java.sql.Date sqlStartDate = rs.getDate("start_date");
                String formattedStartDate = (sqlStartDate == null) ? null :
                                            new java.text.SimpleDateFormat("dd-MM-yyyy").format(sqlStartDate);

                // Read counts
                Map<Integer, Integer> counts = new HashMap<>();
                counts.put(500, rs.getInt("count_500"));
                counts.put(200, rs.getInt("count_200"));
                counts.put(100, rs.getInt("count_100"));
                counts.put(50, rs.getInt("count_50"));
                counts.put(20, rs.getInt("count_20"));
                counts.put(10, rs.getInt("count_10"));
                counts.put(5, rs.getInt("count_5"));
                counts.put(2, rs.getInt("count_2"));
                counts.put(1, rs.getInt("count_1"));

                double gullakDueAmount = rs.getDouble("gullak_due_amount");

                    recycled.add(new Deposit(
                            rs.getInt("id"), rs.getString("deposit_type"), rs.getString("holder_name"),
                            rs.getString("description"), rs.getString("goal"), rs.getString("creation_date_str"),
                            rs.getString("account_number"), rs.getDouble("principal_amount"), rs.getDouble("monthly_amount"),
                            rs.getDouble("interest_rate"), rs.getInt("tenure"), rs.getString("tenure_unit"),
                            formattedStartDate, rs.getDouble("current_total"),
                            rs.getString("last_updated_str"),
                            gullakDueAmount, counts
                    ));
                }
            }
        }
        return recycled;
    }

    public void restoreDeposit(int depositId) throws SQLException {
    int accountId = requireAccountId();
    String copySql = "INSERT INTO deposits (id, account_id, deposit_type, holder_name, description, goal, creation_date, account_number, "
        + "principal_amount, monthly_amount, interest_rate, tenure, tenure_unit, start_date, current_total, last_updated, "
        + "count_500, count_200, count_100, count_50, count_20, count_10, count_5, count_2, count_1, gullak_due_amount) "
        + "SELECT id, account_id, deposit_type, holder_name, description, goal, creation_date, account_number, principal_amount, monthly_amount, "
        + "interest_rate, tenure, tenure_unit, start_date, current_total, last_updated, count_500, count_200, count_100, count_50, count_20, count_10, "
        + "count_5, count_2, count_1, gullak_due_amount FROM recycle_bin_deposits WHERE id = ? AND account_id = ?";
    String deleteSql = "DELETE FROM recycle_bin_deposits WHERE id = ? AND account_id = ?";

    Connection conn = connection;
        conn.setAutoCommit(false);

        try (PreparedStatement copyPs = conn.prepareStatement(copySql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {

        copyPs.setInt(1, depositId);
        copyPs.setInt(2, accountId);
        deletePs.setInt(1, depositId);
        deletePs.setInt(2, accountId);

            copyPs.executeUpdate();
            deletePs.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            conn.rollback();
            if (e.getErrorCode() == 1062) {
                System.out.println("Deposit " + depositId + " already exists. Removing from recycle bin.");
                try (PreparedStatement deletePsOnly = conn.prepareStatement(deleteSql)) {
                    deletePsOnly.setInt(1, depositId);
                    deletePsOnly.setInt(2, accountId);
                    deletePsOnly.executeUpdate();
                    conn.commit();
                } catch (SQLException ex) {
                    conn.rollback(); throw ex;
                }
            } else {
                throw e;
            }
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void permanentlyDeleteDeposit(int depositId) throws SQLException {
        int accountId = requireAccountId();
        String sql = "DELETE FROM recycle_bin_deposits WHERE id = ? AND account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, depositId);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }
    // Add this placeholder method to src/FinanceManager.java
    public void updateDeposit(Deposit d) throws SQLException {
        // Basic update for common fields - More specific updates might be needed
        int accountId = requireAccountId();
        String sql = "UPDATE deposits SET holder_name = ?, description = ?, goal = ? " +
                     "WHERE id = ? AND account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, d.getHolderName());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getGoal());
            ps.setInt(4, d.getId());
            ps.setInt(5, accountId);
            ps.executeUpdate();
        }
        System.out.println("Basic deposit details updated for ID: " + d.getId());
        // Note: This does NOT update amounts, rates, counts, etc. yet.
        // We'll handle Gullak updates specifically. FD/RD updates can be added if needed.
    }
    // Replace this method in src/FinanceManager.java
    public List<Map<String, Object>> getRecycledDepositsForUI() throws SQLException {
        int accountId = requireAccountId();
        List<Map<String, Object>> recycled = new ArrayList<>();
        String sql = "SELECT id, deposit_type, holder_name, description, goal, "
                + "DATE_FORMAT(creation_date, '%Y-%m-%d %H:%i:%s') as creation_date_str, "
                + "account_number, principal_amount, monthly_amount, interest_rate, "
                + "tenure, tenure_unit, start_date, current_total, "
                + "DATE_FORMAT(last_updated, '%Y-%m-%d %H:%i:%s') as last_updated_str, "
                + "count_500, count_200, count_100, count_50, count_20, count_10, count_5, count_2, count_1, gullak_due_amount, "
                + "DATE_FORMAT(deleted_on, '%Y-%m-%d %H:%i:%s') as deleted_on_str "
                + "FROM recycle_bin_deposits WHERE account_id = ? ORDER BY deleted_on DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> depositData = new HashMap<>();
                    depositData.put("id", rs.getInt("id"));
                    depositData.put("deposit_type", rs.getString("deposit_type"));
                    depositData.put("holder_name", rs.getString("holder_name"));
                    depositData.put("description", rs.getString("description"));
                    depositData.put("principal_amount", rs.getDouble("principal_amount"));
                    depositData.put("monthly_amount", rs.getDouble("monthly_amount"));
                    depositData.put("current_total", rs.getDouble("current_total"));
                    depositData.put("deleted_on_str", rs.getString("deleted_on_str"));

                    recycled.add(depositData);
                }
            }
        }
        return recycled;
    }
    public List<Card> getAllCards() throws SQLException {
        System.out.println("DEBUG: Entering getAllCards...");
        int accountId = requireAccountId();
        List<Card> currentCards = new ArrayList<>();
        String sql = "SELECT id, unique_id, card_name, card_type, card_number, valid_from, valid_through, cvv, front_image_path, back_image_path, credit_limit, current_expenses, amount_to_pay, days_left_to_pay, creation_date " +
                     "FROM cards WHERE account_id = ? ORDER BY card_name";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        java.sql.Date sqlCreationDate = rs.getDate("creation_date");
                        String formattedCreationDate = (sqlCreationDate == null) ? null : sqlCreationDate.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                        Card card = new Card(
                            rs.getInt("id"), rs.getString("unique_id"), rs.getString("card_name"), rs.getString("card_type"),
                            rs.getString("card_number"), rs.getString("valid_from"), rs.getString("valid_through"), rs.getString("cvv"),
                            rs.getString("front_image_path"), rs.getString("back_image_path"), rs.getDouble("credit_limit"),
                            rs.getDouble("current_expenses"), rs.getDouble("amount_to_pay"), rs.getInt("days_left_to_pay"),
                            formattedCreationDate
                        );
                        currentCards.add(card);
                    } catch (Exception e) {
                        System.err.println("!!! ERROR creating Card object from ResultSet !!!");
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("DEBUG: Fetched " + currentCards.size() + " cards.");
        } catch (SQLException e) {
            System.err.println("!!! ERROR getting all cards (SQL) !!!");
            e.printStackTrace();
            throw e;
        }
        this.cards = currentCards;
        return currentCards;
    }
    public void updateCard(Card card) throws SQLException {
        System.out.println("DEBUG: Updating card ID " + card.getId());
        int accountId = requireAccountId();
        String sql = "UPDATE cards SET card_name = ?, card_number = ?, valid_from = ?, valid_through = ?, cvv = ?, " +
                     "front_image_path = ?, back_image_path = ?, credit_limit = ?, current_expenses = ?, amount_to_pay = ?, days_left_to_pay = ? " +
                     "WHERE id = ? AND account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, card.getCardName());
            ps.setString(2, card.getCardNumber());
            ps.setString(3, card.getValidFrom());
            ps.setString(4, card.getValidThrough());
            ps.setString(5, card.getCvv());
            ps.setString(6, card.getFrontImagePath());
            ps.setString(7, card.getBackImagePath());

            boolean isCredit = "Credit Card".equals(card.getCardType());
            if (isCredit) {
                ps.setDouble(8, card.getCreditLimit());
                ps.setDouble(9, card.getCurrentExpenses());
                ps.setDouble(10, card.getAmountToPay());
                ps.setInt(11, card.getDaysLeftToPay());
            } else {
                ps.setNull(8, Types.DOUBLE);
                ps.setNull(9, Types.DOUBLE);
                ps.setNull(10, Types.DOUBLE);
                ps.setNull(11, Types.INTEGER);
            }

            ps.setInt(12, card.getId());
            ps.setInt(13, accountId);
            ps.executeUpdate();
        }
    }

    public void moveCardToRecycleBin(int cardId) throws SQLException {
        System.out.println("DEBUG: Moving card ID " + cardId + " to recycle bin");
        int accountId = requireAccountId();
        String copySql = "INSERT INTO recycle_bin_cards (account_id, original_id, unique_id, card_name, card_type, card_number, valid_from, valid_through, cvv, " +
                         "front_image_path, back_image_path, credit_limit, current_expenses, amount_to_pay, days_left_to_pay, creation_date, deleted_on) " +
                         "SELECT account_id, id, unique_id, card_name, card_type, card_number, valid_from, valid_through, cvv, front_image_path, back_image_path, " +
                         "credit_limit, current_expenses, amount_to_pay, days_left_to_pay, creation_date, NOW() FROM cards WHERE id = ? AND account_id = ?";
        String deleteSql = "DELETE FROM cards WHERE id = ? AND account_id = ?";

        boolean oldAuto = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement copyPs = connection.prepareStatement(copySql);
             PreparedStatement delPs = connection.prepareStatement(deleteSql)) {
            copyPs.setInt(1, cardId);
            copyPs.setInt(2, accountId);
            delPs.setInt(1, cardId);
            delPs.setInt(2, accountId);
            int rowsCopied = copyPs.executeUpdate();
            if (rowsCopied == 0) {
                throw new SQLException("Card not found for current account: " + cardId);
            }
            delPs.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(oldAuto);
        }
    }

    public List<Map<String, Object>> getRecycledCardsForUI() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        int accountId = requireAccountId();
        String sql = "SELECT original_id, card_type, card_name, card_number, valid_through, " +
                     "DATE_FORMAT(deleted_on, '%Y-%m-%d %H:%i:%s') AS deleted_on_str " +
                     "FROM recycle_bin_cards WHERE account_id = ? ORDER BY deleted_on DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("original_id", rs.getInt("original_id"));
                    row.put("card_type", rs.getString("card_type"));
                    row.put("card_name", rs.getString("card_name"));
                    String num = rs.getString("card_number");
                    String masked = (num == null || num.length() < 4) ? "**** **** ****" : ("**** **** **** " + num.substring(num.length() - 4));
                    row.put("masked_card_number", masked);
                    row.put("valid_through", rs.getString("valid_through"));
                    row.put("deleted_on_str", rs.getString("deleted_on_str"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    public void restoreCard(int originalCardId) throws SQLException {
        System.out.println("DEBUG: Restoring card original ID " + originalCardId);
        int accountId = requireAccountId();
        String copySql = "INSERT INTO cards (id, account_id, unique_id, card_name, card_type, card_number, valid_from, valid_through, cvv, " +
                         "front_image_path, back_image_path, credit_limit, current_expenses, amount_to_pay, days_left_to_pay, creation_date) " +
                         "SELECT original_id, account_id, unique_id, card_name, card_type, card_number, valid_from, valid_through, cvv, front_image_path, back_image_path, " +
                         "credit_limit, current_expenses, amount_to_pay, days_left_to_pay, creation_date FROM recycle_bin_cards WHERE original_id = ? AND account_id = ?";
        String deleteSql = "DELETE FROM recycle_bin_cards WHERE original_id = ? AND account_id = ?";

        boolean oldAuto = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement copyPs = connection.prepareStatement(copySql);
             PreparedStatement delPs = connection.prepareStatement(deleteSql)) {
            copyPs.setInt(1, originalCardId);
            copyPs.setInt(2, accountId);
            delPs.setInt(1, originalCardId);
            delPs.setInt(2, accountId);
            copyPs.executeUpdate();
            delPs.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            if (e.getErrorCode() == 1062) { // Duplicate key
                // If already exists in main table, just delete from recycle bin
                try (PreparedStatement delPsOnly = connection.prepareStatement(deleteSql)) {
                    delPsOnly.setInt(1, originalCardId);
                    delPsOnly.setInt(2, accountId);
                    delPsOnly.executeUpdate();
                    connection.commit();
                } catch (SQLException ex) {
                    connection.rollback();
                    throw ex;
                }
            } else {
                throw e;
            }
        } finally {
            connection.setAutoCommit(oldAuto);
        }
    }

    public void permanentlyDeleteCard(int originalCardId) throws SQLException {
        int accountId = requireAccountId();
        String sql = "DELETE FROM recycle_bin_cards WHERE original_id = ? AND account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, originalCardId);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }

    // ==================================================================
    // ===         NEW INVESTMENT METHODS                           ===
    // ==================================================================

    /**
     * Saves a new investment record to the database.
     */
    public void saveInvestment(Investment inv) throws SQLException {
        int accountId = requireAccountId();
        String sql = "INSERT INTO investments (account_id, asset_type, holder_name, description, goal, start_date, " +
                     "account_details, ticker_symbol, exchange, quantity, initial_unit_cost, current_unit_price, " +
                     "property_address, tenure_years, interest_rate) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, accountId);
            ps.setString(2, inv.getAssetType());
            ps.setString(3, inv.getHolderName());
            ps.setString(4, inv.getDescription());
            ps.setString(5, inv.getGoal());

            if (inv.getStartDate() != null && !inv.getStartDate().isEmpty()) {
                LocalDate localDate = LocalDate.parse(inv.getStartDate(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                ps.setDate(6, java.sql.Date.valueOf(localDate));
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.setString(7, inv.getAccountDetails());
            ps.setString(8, inv.getTickerSymbol());
            ps.setString(9, inv.getExchange());
            ps.setDouble(10, inv.getQuantity());
            ps.setDouble(11, inv.getInitialUnitCost());
            ps.setDouble(12, inv.getCurrentUnitPrice());
            ps.setString(13, inv.getPropertyAddress());
            ps.setInt(14, inv.getTenureYears());
            ps.setDouble(15, inv.getInterestRate());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    inv.setId(generatedKeys.getInt(1));
                }
            }
        } catch (java.time.format.DateTimeParseException e) {
            System.err.println("!!! ERROR parsing date in saveInvestment !!!");
            e.printStackTrace();
            throw new SQLException("Invalid date format. Expected dd-MM-yyyy.", e);
        } catch (SQLException e) {
            System.err.println("!!! ERROR saving investment (SQL) !!!");
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("!!! UNEXPECTED ERROR saving investment !!!");
            e.printStackTrace();
            throw new SQLException("Unexpected error saving investment.", e);
        }
    }
    
    /**
     * Updates an existing investment record in the database.
     * Used for editing details or updating the current price.
     */
    public void updateInvestment(Investment inv) throws SQLException {
        int accountId = requireAccountId();
        String sql = "UPDATE investments SET " +
                     "asset_type = ?, holder_name = ?, description = ?, goal = ?, start_date = ?, " +
                     "account_details = ?, ticker_symbol = ?, exchange = ?, quantity = ?, " +
                     "initial_unit_cost = ?, current_unit_price = ?, property_address = ?, " +
                     "tenure_years = ?, interest_rate = ? " +
                     "WHERE id = ? AND account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, inv.getAssetType());
            ps.setString(2, inv.getHolderName());
            ps.setString(3, inv.getDescription());
            ps.setString(4, inv.getGoal());
            if (inv.getStartDate() != null && !inv.getStartDate().isEmpty()) {
                java.time.LocalDate localDate = java.time.LocalDate.parse(inv.getStartDate(), java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                ps.setDate(5, java.sql.Date.valueOf(localDate));
            } else { ps.setNull(5, Types.DATE); }
            ps.setString(6, inv.getAccountDetails());
            ps.setString(7, inv.getTickerSymbol());
            ps.setString(8, inv.getExchange());
            ps.setDouble(9, inv.getQuantity());
            ps.setDouble(10, inv.getInitialUnitCost());
            ps.setDouble(11, inv.getCurrentUnitPrice());
            ps.setString(12, inv.getPropertyAddress());
            ps.setInt(13, inv.getTenureYears());
            ps.setDouble(14, inv.getInterestRate());
            ps.setInt(15, inv.getId());
            ps.setInt(16, accountId);
            
            ps.executeUpdate();
        } catch (java.time.format.DateTimeParseException e) {
            System.err.println("!!! ERROR parsing date in updateInvestment !!!");
            e.printStackTrace();
            throw new SQLException("Invalid date format. Expected dd-MM-yyyy.", e);
        } catch (SQLException e) { 
            System.err.println("!!! ERROR updating investment (SQL) !!!"); 
            e.printStackTrace(); throw e; 
        } catch (Exception e) { 
            System.err.println("!!! UNEXPECTED ERROR updating investment !!!"); 
            e.printStackTrace(); throw new SQLException("Unexpected error updating investment.", e); 
        }
    }

    /**
     * Fetches all investments from the database.
     */
    public List<Investment> getAllInvestments() throws SQLException {
        System.out.println("DEBUG: Entering getAllInvestments...");
        int accountId = requireAccountId();
        List<Investment> investments = new ArrayList<>();
        String sql = "SELECT * FROM investments WHERE account_id = ? ORDER BY holder_name, description";
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd-MM-yyyy");

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        java.sql.Date sqlStartDate = rs.getDate("start_date");
                        String formattedStartDate = (sqlStartDate != null) ? dateFormat.format(sqlStartDate) : null;

                        Investment inv = new Investment(
                            rs.getInt("id"),
                            rs.getString("asset_type"),
                            rs.getString("holder_name"),
                            rs.getString("description"),
                            rs.getString("goal"),
                            formattedStartDate,
                            rs.getString("account_details"),
                            rs.getString("ticker_symbol"),
                            rs.getString("exchange"),
                            rs.getDouble("quantity"),
                            rs.getDouble("initial_unit_cost"),
                            rs.getDouble("current_unit_price"),
                            rs.getString("property_address"),
                            rs.getInt("tenure_years"),
                            rs.getDouble("interest_rate")
                        );
                        investments.add(inv);
                    } catch (Exception e) {
                        System.err.println("!!! ERROR creating Investment object from ResultSet !!!");
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("DEBUG: Fetched " + investments.size() + " investments.");
        } catch (SQLException e) {
            System.err.println("!!! ERROR executing SQL in getAllInvestments !!!");
            e.printStackTrace();
            throw e;
        }
        return investments;
    }

    /**
     * Moves an investment to the recycle bin table.
     */
    public void moveInvestmentToRecycleBin(int investmentId) throws SQLException {
        int accountId = requireAccountId();
        String copySql = "INSERT INTO recycle_bin_investments (id, account_id, asset_type, holder_name, description, goal, start_date, account_details, ticker_symbol, exchange, quantity, initial_unit_cost, current_unit_price, property_address, tenure_years, interest_rate, last_updated, deleted_on) " +
                         "SELECT id, account_id, asset_type, holder_name, description, goal, start_date, account_details, ticker_symbol, exchange, quantity, initial_unit_cost, current_unit_price, property_address, tenure_years, interest_rate, last_updated, NOW() FROM investments WHERE id = ? AND account_id = ?";
        String deleteSql = "DELETE FROM investments WHERE id = ? AND account_id = ?";

        boolean oldAuto = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement copyPs = connection.prepareStatement(copySql);
             PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {

            copyPs.setInt(1, investmentId);
            copyPs.setInt(2, accountId);
            deletePs.setInt(1, investmentId);
            deletePs.setInt(2, accountId);

            int copied = copyPs.executeUpdate();
            if (copied == 0) {
                throw new SQLException("Failed to copy investment to recycle bin, ID not found for current account: " + investmentId);
            }

            deletePs.executeUpdate();

            connection.commit();
            System.out.println("DEBUG: Successfully moved investment ID " + investmentId + " to recycle bin.");
        } catch (SQLException e) {
            connection.rollback();
            System.err.println("!!! ERROR moving investment to recycle bin !!!");
            e.printStackTrace();
            throw e;
        } finally {
            connection.setAutoCommit(oldAuto);
        }
    }

    /**
     * Fetches recycled investments for the UI dialog.
     */
    public List<Map<String, Object>> getRecycledInvestmentsForUI() throws SQLException {
        List<Map<String, Object>> recycled = new ArrayList<>();
        int accountId = requireAccountId();
        String sql = "SELECT id, asset_type, holder_name, description, " +
                     "DATE_FORMAT(deleted_on, '%Y-%m-%d %H:%i:%s') as deleted_on_str " +
                     "FROM recycle_bin_investments WHERE account_id = ? ORDER BY deleted_on DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", rs.getInt("id"));
                    data.put("asset_type", rs.getString("asset_type"));
                    data.put("holder_name", rs.getString("holder_name"));
                    data.put("description", rs.getString("description"));
                    data.put("deleted_on_str", rs.getString("deleted_on_str"));
                    recycled.add(data);
                }
            }
        } catch (SQLException e) {
            System.err.println("!!! ERROR getting recycled investments !!!");
            e.printStackTrace();
            throw e;
        }
        return recycled;
    }

    /**
     * Restores an investment from the recycle bin.
     */
    public void restoreInvestment(int investmentId) throws SQLException {
        int accountId = requireAccountId();
        String copySql = "INSERT INTO investments (id, account_id, asset_type, holder_name, description, goal, start_date, account_details, ticker_symbol, exchange, quantity, initial_unit_cost, current_unit_price, property_address, tenure_years, interest_rate, last_updated) " +
                       "SELECT id, account_id, asset_type, holder_name, description, goal, start_date, account_details, ticker_symbol, exchange, quantity, initial_unit_cost, current_unit_price, property_address, tenure_years, interest_rate, last_updated " +
                       "FROM recycle_bin_investments WHERE id = ? AND account_id = ?";
        String deleteSql = "DELETE FROM recycle_bin_investments WHERE id = ? AND account_id = ?";

        boolean oldAuto = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement copyPs = connection.prepareStatement(copySql);
             PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {

            copyPs.setInt(1, investmentId);
            copyPs.setInt(2, accountId);
            deletePs.setInt(1, investmentId);
            deletePs.setInt(2, accountId);

            copyPs.executeUpdate();
            deletePs.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            if (e.getErrorCode() == 1062) {
                 System.out.println("Investment " + investmentId + " already exists. Removing from recycle bin.");
                 try (PreparedStatement deletePsOnly = connection.prepareStatement(deleteSql)) {
                     deletePsOnly.setInt(1, investmentId);
                     deletePsOnly.setInt(2, accountId);
                     deletePsOnly.executeUpdate();
                     connection.commit();
                 } catch (SQLException ex) {
                     connection.rollback();
                     throw ex;
                 }
            } else {
                throw e;
            }
        } finally {
            connection.setAutoCommit(oldAuto);
        }
    }

    /**
     * Permanently deletes an investment from the recycle bin.
     */
    public void permanentlyDeleteInvestment(int investmentId) throws SQLException {
        int accountId = requireAccountId();
        String sql = "DELETE FROM recycle_bin_investments WHERE id = ? AND account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, investmentId);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Updates only the current unit price of the given investment ID.
     * Useful for quick price refreshes from the UI.
     */
    public void updateInvestmentCurrentPrice(int investmentId, double newPrice) throws SQLException {
        int accountId = requireAccountId();
        String sql = "UPDATE investments SET current_unit_price = ?, last_updated = CURRENT_TIMESTAMP WHERE id = ? AND account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, newPrice);
            ps.setInt(2, investmentId);
            ps.setInt(3, accountId);
            ps.executeUpdate();
        }
    }
    // ==================================================================
    // ===         TABLE CREATION (If needed on startup)              ===
    // ==================================================================
     private void createTables() throws SQLException {
         try (Statement stmt = connection.createStatement()) {
             System.out.println("Checking/Creating database tables...");
             
             // Note: accounts and login_audit tables are created by AuthManager

             // Inside private void createTables()
            stmt.execute("CREATE TABLE IF NOT EXISTS tax_profiles (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "account_id INT NOT NULL," +
                "profile_name VARCHAR(100) NOT NULL," +
                "profile_type VARCHAR(50) NOT NULL," +
                "financial_year VARCHAR(10) NOT NULL," +
                "gross_income DECIMAL(15, 2) DEFAULT 0.00," +
                "total_deductions DECIMAL(15, 2) DEFAULT 0.00," +
                "taxable_income DECIMAL(15, 2) DEFAULT 0.00," +
                "tax_paid DECIMAL(15, 2) DEFAULT 0.00," +
                "notes TEXT," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE" +
            ")");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS recycle_bin_tax_profiles (" +
                "id INT NOT NULL PRIMARY KEY," +
                "account_id INT NOT NULL," +
                "profile_name VARCHAR(100) NOT NULL," +
                "profile_type VARCHAR(50) NOT NULL," +
                "financial_year VARCHAR(10) NOT NULL," +
                "gross_income DECIMAL(15, 2) DEFAULT 0.00," +
                "total_deductions DECIMAL(15, 2) DEFAULT 0.00," +
                "taxable_income DECIMAL(15, 2) DEFAULT 0.00," +
                "tax_paid DECIMAL(15, 2) DEFAULT 0.00," +
                "notes TEXT," +
                "deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE" +
            ")");
             
             
             // --- KEEP EXISTING TABLES ---
         stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
             "id INT AUTO_INCREMENT PRIMARY KEY, " +
             "account_id INT NOT NULL, " +
             "date DATE NOT NULL, " +
             "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
             "day VARCHAR(20), " +
             "payment_method VARCHAR(20), " +
             "category VARCHAR(100), " +
             "type VARCHAR(20) NOT NULL, " +
             "payee VARCHAR(100), " +
             "description TEXT, " +
             "amount DECIMAL(10, 2) NOT NULL, " +
             "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE"
             + ")");
         stmt.execute("CREATE TABLE IF NOT EXISTS recycle_bin_transactions (" +
             "id INT PRIMARY KEY, " +
             "account_id INT NOT NULL, " +
             "timestamp TIMESTAMP, " +
             "date DATE, " +
             "day VARCHAR(20), " +
             "payment_method VARCHAR(20), " +
             "category VARCHAR(100), " +
             "type VARCHAR(20), " +
             "payee VARCHAR(100), " +
             "description TEXT, " +
             "amount DECIMAL(10, 2), " +
             "deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
             "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE"
             + ")");
         stmt.execute("CREATE TABLE IF NOT EXISTS bank_accounts (" +
             "id INT AUTO_INCREMENT PRIMARY KEY, " +
             "account_id INT NOT NULL, " +
             "account_number VARCHAR(50) NOT NULL, holder_name VARCHAR(100), bank_name VARCHAR(100), ifsc_code VARCHAR(20), " +
             "balance DECIMAL(15, 2) DEFAULT 0.00, account_type VARCHAR(20) NOT NULL, interest_rate DECIMAL(5, 2), " +
             "annual_expense DECIMAL(15, 2), account_subtype VARCHAR(20), company_name VARCHAR(100), business_name VARCHAR(100), " +
             "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE"
             + ")");
        stmt.execute("CREATE TABLE IF NOT EXISTS recycle_bin_bank_accounts (" +
            "id INT NOT NULL PRIMARY KEY, " +
            "account_id INT NOT NULL, " +
            "account_number VARCHAR(50) NOT NULL, holder_name VARCHAR(100), bank_name VARCHAR(100), ifsc_code VARCHAR(20), " +
            "balance DECIMAL(15, 2) DEFAULT 0.00, account_type VARCHAR(20) NOT NULL, interest_rate DECIMAL(5, 2), " +
            "annual_expense DECIMAL(15, 2), account_subtype VARCHAR(20), company_name VARCHAR(100), business_name VARCHAR(100), " +
            "deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE"
            + ")");
         stmt.execute("CREATE TABLE IF NOT EXISTS deposits (" +
             "id INT AUTO_INCREMENT PRIMARY KEY, " +
             "account_id INT NOT NULL, " +
             "deposit_type VARCHAR(20) NOT NULL, holder_name VARCHAR(100), description TEXT, goal VARCHAR(255), " +
             "creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, account_number VARCHAR(50), principal_amount DECIMAL(15, 2), " +
             "monthly_amount DECIMAL(15, 2), interest_rate DECIMAL(5, 2), tenure INT, tenure_unit VARCHAR(10), start_date DATE, " +
             "current_total DECIMAL(15, 2), last_updated TIMESTAMP NULL, count_500 INT DEFAULT 0, count_200 INT DEFAULT 0, " +
             "count_100 INT DEFAULT 0, count_50 INT DEFAULT 0, count_20 INT DEFAULT 0, count_10 INT DEFAULT 0, count_5 INT DEFAULT 0, " +
             "count_2 INT DEFAULT 0, count_1 INT DEFAULT 0, gullak_due_amount DECIMAL(15, 2) DEFAULT 0.00, " +
             "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE"
             + ")");
         stmt.execute("CREATE TABLE IF NOT EXISTS recycle_bin_deposits (" +
             "id INT NOT NULL PRIMARY KEY, " +
             "account_id INT NOT NULL, " +
             "deposit_type VARCHAR(20) NOT NULL, holder_name VARCHAR(100), description TEXT, goal VARCHAR(255), creation_date TIMESTAMP, " +
             "account_number VARCHAR(50), principal_amount DECIMAL(15, 2), monthly_amount DECIMAL(15, 2), interest_rate DECIMAL(5, 2), " +
             "tenure INT, tenure_unit VARCHAR(10), start_date DATE, current_total DECIMAL(15, 2), last_updated TIMESTAMP NULL, " +
             "count_500 INT DEFAULT 0, count_200 INT DEFAULT 0, count_100 INT DEFAULT 0, count_50 INT DEFAULT 0, count_20 INT DEFAULT 0, " +
             "count_10 INT DEFAULT 0, count_5 INT DEFAULT 0, count_2 INT DEFAULT 0, count_1 INT DEFAULT 0, gullak_due_amount DECIMAL(15, 2) DEFAULT 0.00, " +
             "deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
             "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE"
             + ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS cards (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "account_id INT NOT NULL, " +
                "unique_id VARCHAR(36) UNIQUE NOT NULL, " +
                "card_name VARCHAR(100) NOT NULL, " +
                "card_type VARCHAR(20) NOT NULL, " +
                "card_number VARCHAR(16) NOT NULL, " +
                "valid_from VARCHAR(5), " +
                "valid_through VARCHAR(5) NOT NULL, " +
                "cvv VARCHAR(4) NOT NULL, " +
                "front_image_path VARCHAR(255), " +
                "back_image_path VARCHAR(255), " +
                "credit_limit DECIMAL(15, 2), " +
                "current_expenses DECIMAL(15, 2), " +
                "amount_to_pay DECIMAL(15, 2), " +
                "days_left_to_pay INT, " +
                "creation_date DATE, " +
                "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE" +
            ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS recycle_bin_cards (" +
                "recycle_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "account_id INT NOT NULL, " +
                "original_id INT NOT NULL, " +
                "unique_id VARCHAR(36) NOT NULL, " +
                "card_name VARCHAR(100) NOT NULL, " +
                "card_type VARCHAR(20) NOT NULL, " +
                "card_number VARCHAR(16) NOT NULL, " +
                "valid_from VARCHAR(5), " +
                "valid_through VARCHAR(5) NOT NULL, " +
                "cvv VARCHAR(4) NOT NULL, " +
                "front_image_path VARCHAR(255), " +
                "back_image_path VARCHAR(255), " +
                "credit_limit DECIMAL(15, 2), " +
                "current_expenses DECIMAL(15, 2), " +
                "amount_to_pay DECIMAL(15, 2), " +
                "days_left_to_pay INT, " +
                "creation_date DATE, " +
                "deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE" +
            ")");

             // --- REMOVE OLD INVESTMENT TABLES ---
             stmt.execute("DROP TABLE IF EXISTS gold_silver_investments;");
             stmt.execute("DROP TABLE IF EXISTS mutual_funds;");
             
             // --- ADD NEW INVESTMENT TABLES ---
            stmt.execute("CREATE TABLE IF NOT EXISTS investments (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "account_id INT NOT NULL," +
                "asset_type VARCHAR(50) NOT NULL," +
                "holder_name VARCHAR(100)," +
                "description VARCHAR(255)," +
                "goal VARCHAR(255)," +
                "start_date DATE," +
                "account_details TEXT," +
                "ticker_symbol VARCHAR(20)," +
                "exchange VARCHAR(20)," +
                "quantity DECIMAL(20, 8) DEFAULT 0.0," +
                "initial_unit_cost DECIMAL(15, 2) DEFAULT 0.0," +
                "current_unit_price DECIMAL(15, 2) DEFAULT 0.0," +
                "property_address TEXT," +
                "tenure_years INT," +
                "interest_rate DECIMAL(5, 2)," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS recycle_bin_investments (" +
                "id INT NOT NULL PRIMARY KEY," +
                "account_id INT NOT NULL," +
                "asset_type VARCHAR(50) NOT NULL," +
                "holder_name VARCHAR(100)," +
                "description VARCHAR(255)," +
                "goal VARCHAR(255)," +
                "start_date DATE," +
                "account_details TEXT," +
                "ticker_symbol VARCHAR(20)," +
                "exchange VARCHAR(20)," +
                "quantity DECIMAL(20, 8) DEFAULT 0.0," +
                "initial_unit_cost DECIMAL(15, 2) DEFAULT 0.0," +
                "current_unit_price DECIMAL(15, 2) DEFAULT 0.0," +
                "property_address TEXT," +
                "tenure_years INT," +
                "interest_rate DECIMAL(5, 2)," +
                "last_updated TIMESTAMP," +
                "deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS loans (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "account_id INT NOT NULL," +
                "lender_name VARCHAR(100)," +
                "loan_type VARCHAR(50)," +
                "principal_amount DECIMAL(15, 2) DEFAULT 0.00," +
                "interest_rate DECIMAL(5, 2) DEFAULT 0.00," +
                "tenure_months INT," +
                "start_date DATE," +
                "status VARCHAR(20)," +
                "notes TEXT," +
                "emi_amount DECIMAL(15, 2) DEFAULT 0.00," +
                "total_interest DECIMAL(15, 2) DEFAULT 0.00," +
                "total_payment DECIMAL(15, 2) DEFAULT 0.00," +
                "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE" +
            ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS recycle_bin_loans (" +
                "id INT NOT NULL PRIMARY KEY," +
                "account_id INT NOT NULL," +
                "lender_name VARCHAR(100)," +
                "loan_type VARCHAR(50)," +
                "principal_amount DECIMAL(15, 2) DEFAULT 0.00," +
                "interest_rate DECIMAL(5, 2) DEFAULT 0.00," +
                "tenure_months INT," +
                "start_date DATE," +
                "status VARCHAR(20)," +
                "notes TEXT," +
                "emi_amount DECIMAL(15, 2) DEFAULT 0.00," +
                "total_interest DECIMAL(15, 2) DEFAULT 0.00," +
                "total_payment DECIMAL(15, 2) DEFAULT 0.00," +
                "deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE" +
            ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS lendings (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "account_id INT NOT NULL," +
            "borrower_name VARCHAR(100) NOT NULL," +
            "loan_type VARCHAR(50) NOT NULL," +
            "principal_amount DECIMAL(15, 2) NOT NULL," +
            "interest_rate DECIMAL(5, 2) NOT NULL," +
            "tenure_months INT NOT NULL," +
            "date_lent DATE," +
            "interest_to_receive DECIMAL(15, 2) NOT NULL," +
            "total_to_receive DECIMAL(15, 2) NOT NULL," +
            "monthly_payment DECIMAL(10, 2) NOT NULL," +
            "status VARCHAR(20) DEFAULT 'Active'," +
            "notes TEXT," +
            "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE" +
        ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS recycle_bin_lendings (" +
            "id INT NOT NULL," +
            "account_id INT NOT NULL," +
            "borrower_name VARCHAR(100) NOT NULL," +
            "loan_type VARCHAR(50) NOT NULL," +
            "principal_amount DECIMAL(15, 2) NOT NULL," +
            "interest_rate DECIMAL(5, 2) NOT NULL," +
            "tenure_months INT NOT NULL," +
            "date_lent DATE," +
            "interest_to_receive DECIMAL(15, 2) NOT NULL," +
            "total_to_receive DECIMAL(15, 2) NOT NULL," +
            "monthly_payment DECIMAL(10, 2) NOT NULL," +
            "status VARCHAR(20) DEFAULT 'Active'," +
            "notes TEXT," +
            "deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "PRIMARY KEY(id)," +
            "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE" +
        ")");

             System.out.println("Tables checked/created/updated.");
            ensureAccountScopedSchema();
         }
     }

    private void ensureAccountScopedSchema() throws SQLException {
        ensureColumnExists("transactions", "account_id INT");
        ensureColumnExists("recycle_bin_transactions", "account_id INT");
        ensureColumnExists("bank_accounts", "account_id INT");
    ensureColumnExists("recycle_bin_bank_accounts", "account_id INT");
        ensureColumnExists("deposits", "account_id INT");
        ensureColumnExists("recycle_bin_deposits", "account_id INT");
        ensureColumnExists("cards", "account_id INT");
        ensureColumnExists("recycle_bin_cards", "account_id INT");
        ensureColumnExists("investments", "account_id INT");
        ensureColumnExists("recycle_bin_investments", "account_id INT");
        ensureColumnExists("loans", "account_id INT");
        ensureColumnExists("recycle_bin_loans", "account_id INT");
        ensureColumnExists("lendings", "account_id INT");
        ensureColumnExists("recycle_bin_lendings", "account_id INT");
        ensureColumnExists("tax_profiles", "account_id INT");
    }

    private void ensureColumnExists(String tableName, String columnDefinition) throws SQLException {
        String columnName = columnDefinition.split("\\s+")[0];
        if (columnExists(tableName, columnName)) {
            return;
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnDefinition);
            System.out.println("Schema migration: added column " + columnName + " to " + tableName);
        }
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        try (ResultSet rs = metaData.getColumns(catalog, null, tableName, columnName)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = metaData.getColumns(catalog, null, tableName.toUpperCase(), columnName.toUpperCase())) {
            return rs.next();
        }
    }

    public void backfillLegacyRecordsToCurrentAccount() throws SQLException {
        int accountId = requireAccountId();
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            for (String table : ACCOUNT_SCOPED_TABLES) {
                if (!columnExists(table, "account_id")) {
                    continue;
                }
                String sql = "UPDATE " + table + " SET account_id = ? WHERE account_id IS NULL OR account_id = 0";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, accountId);
                    int updated = ps.executeUpdate();
                    if (updated > 0) {
                        System.out.println("Backfill: updated " + updated + " row(s) in " + table + " for account " + accountId);
                    }
                }
            }
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        } else if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                System.err.println("Failed to close database connection: " + ex.getMessage());
            }
        }
    }
     // ==================================================================
    // ===         NEW TAX PROFILE METHODS                          ===
    // ==================================================================

    /**
     * Saves a new tax profile to the database.
     */
    public void saveTaxProfile(TaxProfile tp) throws SQLException {
        int accountId = requireAccountId();
        String sql = "INSERT INTO tax_profiles (account_id, profile_name, profile_type, financial_year, " +
                     "gross_income, total_deductions, taxable_income, tax_paid, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, accountId);
            ps.setString(2, tp.getProfileName());
            ps.setString(3, tp.getProfileType());
            ps.setString(4, tp.getFinancialYear());
            ps.setDouble(5, tp.getGrossIncome());
            ps.setDouble(6, tp.getTotalDeductions());
            ps.setDouble(7, tp.getTaxableIncome());
            ps.setDouble(8, tp.getTaxPaid());
            ps.setString(9, tp.getNotes());
            
            ps.executeUpdate();
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // We don't set the ID on the object here,
                    // but we could if the model had a setId() method.
                }
            }
        } catch (SQLException e) { 
            System.err.println("!!! ERROR saving tax profile (SQL) !!!"); 
            e.printStackTrace(); 
            throw e; 
        }
    }

    /**
     * Fetches all tax profiles from the database.
     */
    public List<TaxProfile> getAllTaxProfiles() throws SQLException {
        System.out.println("DEBUG: Entering getAllTaxProfiles...");
        int accountId = requireAccountId();
        List<TaxProfile> profiles = new ArrayList<>();
        String sql = "SELECT * FROM tax_profiles WHERE account_id = ? ORDER BY financial_year DESC, profile_name";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                try {
                    TaxProfile tp = new TaxProfile(
                        rs.getInt("id"),
                        rs.getString("profile_name"),
                        rs.getString("profile_type"),
                        rs.getString("financial_year"),
                        rs.getDouble("gross_income"),
                        rs.getDouble("total_deductions"),
                        rs.getDouble("taxable_income"),
                        rs.getDouble("tax_paid"),
                        rs.getString("notes")
                    );
                    profiles.add(tp);
                } catch (Exception e) {
                    System.err.println("!!! ERROR creating TaxProfile object from ResultSet !!!");
                    e.printStackTrace();
                }
            }
            System.out.println("DEBUG: Fetched " + profiles.size() + " tax profiles.");
            }
        } catch (SQLException e) {
            System.err.println("!!! ERROR executing SQL in getAllTaxProfiles !!!"); 
            e.printStackTrace(); 
            throw e; 
        }
        return profiles;
    }

    /**
     * Updates an existing tax profile in the database.
     */
    public void updateTaxProfile(TaxProfile tp) throws SQLException {
        // Re-calculate taxable income before saving
        tp.calculateTaxableIncome();
        int accountId = requireAccountId();
        String sql = "UPDATE tax_profiles SET " +
                     "profile_name = ?, profile_type = ?, financial_year = ?, " +
                     "gross_income = ?, total_deductions = ?, taxable_income = ?, " +
                     "tax_paid = ?, notes = ? " +
                     "WHERE id = ? AND account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tp.getProfileName());
            ps.setString(2, tp.getProfileType());
            ps.setString(3, tp.getFinancialYear());
            ps.setDouble(4, tp.getGrossIncome());
            ps.setDouble(5, tp.getTotalDeductions());
            ps.setDouble(6, tp.getTaxableIncome()); // Store updated calculation
            ps.setDouble(7, tp.getTaxPaid());
            ps.setString(8, tp.getNotes());
            ps.setInt(9, tp.getId());
            ps.setInt(10, accountId);
            
            ps.executeUpdate();
        } catch (SQLException e) { 
            System.err.println("!!! ERROR updating tax profile (SQL) !!!"); 
            e.printStackTrace(); 
            throw e; 
        }
    }

    /**
     * Deletes a tax profile from the database (Hard Delete).
     */
    /**
     * Moves a tax profile to recycle bin before deletion.
     */
    private void moveTaxProfileToRecycleBin(int profileId) throws SQLException {
        int accountId = requireAccountId();
        String copySql = "INSERT INTO recycle_bin_tax_profiles "
                + "(id, account_id, profile_name, profile_type, financial_year, "
                + "gross_income, total_deductions, taxable_income, tax_paid, notes, deleted_on) "
                + "SELECT id, account_id, profile_name, profile_type, financial_year, "
                + "gross_income, total_deductions, taxable_income, tax_paid, notes, NOW() "
                + "FROM tax_profiles WHERE id = ? AND account_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(copySql)) {
            ps.setInt(1, profileId);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("!!! ERROR moving tax profile to recycle bin !!!"); 
            e.printStackTrace();
            throw e;
        }
    }
    
    public void deleteTaxProfile(int profileId) throws SQLException {
        int accountId = requireAccountId();
        
        // First move to recycle bin
        moveTaxProfileToRecycleBin(profileId);
        
        // Then delete from main table
        String sql = "DELETE FROM tax_profiles WHERE id = ? AND account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        } catch (SQLException e) { 
            System.err.println("!!! ERROR deleting tax profile (SQL) !!!"); 
            e.printStackTrace(); 
            throw e; 
        }
    }
    
    /**
     * Retrieves all deleted tax profiles from the recycle bin.
     */
    public List<TaxProfile> getTaxProfilesFromRecycleBin() throws SQLException {
        int accountId = requireAccountId();
        List<TaxProfile> profiles = new ArrayList<>();
        
        String sql = "SELECT id, account_id, profile_name, profile_type, financial_year, "
                + "gross_income, total_deductions, taxable_income, tax_paid, notes, deleted_on "
                + "FROM recycle_bin_tax_profiles WHERE account_id = ? ORDER BY deleted_on DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                TaxProfile profile = new TaxProfile(
                    rs.getInt("id"),
                    rs.getString("profile_name"),
                    rs.getString("profile_type"),
                    rs.getString("financial_year"),
                    rs.getDouble("gross_income"),
                    rs.getDouble("total_deductions"),
                    rs.getDouble("taxable_income"),
                    rs.getDouble("tax_paid"),
                    rs.getString("notes")
                );
                profile.setDeletedOn(rs.getTimestamp("deleted_on"));
                profiles.add(profile);
            }
        } catch (SQLException e) {
            System.err.println("!!! ERROR fetching tax profiles from recycle bin !!!"); 
            e.printStackTrace();
            throw e;
        }
        
        return profiles;
    }
    
    /**
     * Restores a tax profile from the recycle bin.
     */
    public void restoreTaxProfileFromRecycleBin(int profileId) throws SQLException {
        int accountId = requireAccountId();
        
        // Copy back to main table
        String copySql = "INSERT INTO tax_profiles "
                + "(id, account_id, profile_name, profile_type, financial_year, "
                + "gross_income, total_deductions, taxable_income, tax_paid, notes) "
                + "SELECT id, account_id, profile_name, profile_type, financial_year, "
                + "gross_income, total_deductions, taxable_income, tax_paid, notes "
                + "FROM recycle_bin_tax_profiles WHERE id = ? AND account_id = ?";
        
        String deleteSql = "DELETE FROM recycle_bin_tax_profiles WHERE id = ? AND account_id = ?";
        
        try (PreparedStatement copyPs = connection.prepareStatement(copySql);
             PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {
            
            copyPs.setInt(1, profileId);
            copyPs.setInt(2, accountId);
            copyPs.executeUpdate();
            
            deletePs.setInt(1, profileId);
            deletePs.setInt(2, accountId);
            deletePs.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("!!! ERROR restoring tax profile from recycle bin !!!"); 
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Permanently deletes a tax profile from the recycle bin.
     */
    public void deleteTaxProfilePermanently(int profileId) throws SQLException {
        int accountId = requireAccountId();
        String sql = "DELETE FROM recycle_bin_tax_profiles WHERE id = ? AND account_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("!!! ERROR permanently deleting tax profile !!!"); 
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * Fetches all transactions for a specific year as a single list.
     * @param year The year to fetch (e.g., "2025")
     * @return A List of all transactions for that year.
     * @throws SQLException
     */
    public List<Transaction> getAllTransactionsForYear(String year) throws SQLException {
        int accountId = requireAccountId();
        List<Transaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, date, DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i:%s') as timestamp_str, "
                + "day, category, type, payment_method, payee, amount, description FROM transactions WHERE account_id = ? ");

        boolean filterByYear = year != null && !"All Years".equals(year);
        if (filterByYear) {
            sql.append("AND YEAR(date) = ? ");
        }
        sql.append("ORDER BY date ASC, id ASC");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setInt(1, accountId);
            if (filterByYear) {
                ps.setInt(2, Integer.parseInt(year));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        java.sql.Date sqlDate = rs.getDate("date");
                        String formattedDate = (sqlDate != null) ? dateFormat.format(sqlDate) : null;
                        Transaction t = new Transaction(
                                rs.getInt("id"), formattedDate, rs.getString("timestamp_str"), rs.getString("day"),
                                rs.getString("category"), rs.getString("type"), rs.getDouble("amount"),
                                rs.getString("description"), rs.getString("payment_method"), rs.getString("payee")
                        );
                        transactions.add(t);
                    } catch (Exception e) {
                        System.err.println("!!! ERROR creating Transaction object from ResultSet in getAllTransactionsForYear !!!");
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("!!! ERROR executing SQL in getAllTransactionsForYear !!!");
            e.printStackTrace();
            throw e;
        }
        return transactions;
    }
    // ==================================================================
    // ===         NEW LOAN / EMI METHODS                           ===
    // ==================================================================

    /**
     * Saves a new loan to the database, including all calculated fields.
     */
    public void saveLoan(Loan loan) throws SQLException {
        int accountId = requireAccountId();
        String sql = "INSERT INTO loans (account_id, lender_name, loan_type, principal_amount, interest_rate, " +
                     "tenure_months, start_date, status, notes, " +
                     "emi_amount, total_interest, total_payment) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, accountId);
            ps.setString(2, loan.getLenderName());
            ps.setString(3, loan.getLoanType());
            ps.setDouble(4, loan.getPrincipalAmount());
            ps.setDouble(5, loan.getInterestRate());
            ps.setInt(6, loan.getTenureMonths());

            // Handle Date
            if (loan.getStartDate() != null && !loan.getStartDate().isEmpty()) {
                 try {
                    LocalDate localDate = LocalDate.parse(loan.getStartDate(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    ps.setDate(7, java.sql.Date.valueOf(localDate));
                 } catch (Exception e) {
                     System.err.println("Invalid loan start date format, setting to null: " + loan.getStartDate());
                     ps.setNull(7, Types.DATE);
                 }
            } else {
                ps.setNull(7, Types.DATE);
            }
            
            ps.setString(8, "Active");
            ps.setString(9, loan.getNotes());
            
            // Add the calculated fields
            ps.setDouble(10, loan.getEmiAmount());
            ps.setDouble(11, loan.getTotalInterest());
            ps.setDouble(12, loan.getTotalPayment());
            
            ps.executeUpdate();
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // We can set the ID on the object if we add a setId method to Loan.java
                    // loan.setId(generatedKeys.getInt(1)); 
                }
            }
        } catch (SQLException e) { 
            System.err.println("!!! ERROR saving loan (SQL) !!!"); 
            e.printStackTrace(); throw e; 
        }
    }
    
    /**
     * Fetches all loans from the database.
     */
    public List<Loan> getAllLoans() throws SQLException {
        System.out.println("DEBUG: Entering getAllLoans...");
        int accountId = requireAccountId();
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE account_id = ? ORDER BY status, lender_name";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                try {
                    java.sql.Date sqlStartDate = rs.getDate("start_date");
                    String formattedStartDate = (sqlStartDate != null) ? dateFormat.format(sqlStartDate) : null;
                    
                    Loan loan = new Loan(
                        rs.getInt("id"),
                        rs.getString("lender_name"),
                        rs.getString("loan_type"),
                        rs.getDouble("principal_amount"),
                        rs.getDouble("interest_rate"),
                        rs.getInt("tenure_months"),
                        formattedStartDate,
                        rs.getString("status"),
                        rs.getString("notes"),
                        rs.getDouble("emi_amount"),
                        rs.getDouble("total_interest"),
                        rs.getDouble("total_payment")
                    );
                    loans.add(loan);
                } catch (Exception e) {
                    System.err.println("!!! ERROR creating Loan object from ResultSet !!!");
                    e.printStackTrace();
                }
            }
            System.out.println("DEBUG: Fetched " + loans.size() + " loans.");
            }
        } catch (SQLException e) {
            System.err.println("!!! ERROR executing SQL in getAllLoans !!!"); 
            e.printStackTrace(); 
            throw e; 
        }
        return loans;
    }
    
    /**
     * Updates the status of a loan (e.g., to "Paid Off").
     */
    public void updateLoanStatus(int loanId, String newStatus) throws SQLException {
        int accountId = requireAccountId();
        String sql = "UPDATE loans SET status = ? WHERE id = ? AND account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, loanId);
            ps.setInt(3, accountId);
            ps.executeUpdate();
        } catch (SQLException e) { 
            System.err.println("!!! ERROR updating loan status (SQL) !!!"); 
            e.printStackTrace(); 
            throw e; 
        }
    }

    /**
     * Builds a point-in-time snapshot of the overall finance portfolio for exports.
     */
    public SummaryData buildSummaryData(String companyName, String designation, String holderName, String transactionsYear) throws SQLException {
        SummaryData summary = new SummaryData();
        summary.setCompanyName(safeTrim(companyName));
        summary.setDesignation(safeTrim(designation));
        summary.setHolderName(safeTrim(holderName));
        summary.setGeneratedAt(LocalDateTime.now());

        String yearFilter = (transactionsYear == null || transactionsYear.isBlank()) ? "All Years" : transactionsYear;

        // Transactions
        List<Transaction> allTransactions = getAllTransactionsForYear(yearFilter);
        SummaryData.TransactionSummary txnSummary = summary.getTransactions();
        txnSummary.setTotalCount(allTransactions.size());
        double totalIncome = 0.0;
        double totalExpense = 0.0;
        for (Transaction transaction : allTransactions) {
            if ("Income".equalsIgnoreCase(transaction.getType())) {
                totalIncome += transaction.getAmount();
            } else if ("Expense".equalsIgnoreCase(transaction.getType())) {
                totalExpense += transaction.getAmount();
            }
        }
        txnSummary.setTotalIncome(totalIncome);
        txnSummary.setTotalExpense(totalExpense);
        txnSummary.setNetBalance(totalIncome - totalExpense);

        // Bank accounts
        List<BankAccount> accounts = getAllBankAccounts();
        SummaryData.BankSummary bankSummary = summary.getBank();
        bankSummary.setAccountCount(accounts.size());
        Set<String> uniqueHolders = new HashSet<>();
        double totalBankBalance = 0.0;
        BankAccount richestAccount = null;
        for (BankAccount account : accounts) {
            if (account.getHolderName() != null && !account.getHolderName().isBlank()) {
                uniqueHolders.add(account.getHolderName().trim());
            }
            totalBankBalance += account.getBalance();
            if (richestAccount == null || account.getBalance() > richestAccount.getBalance()) {
                richestAccount = account;
            }
        }
        bankSummary.setUniqueHolderCount(uniqueHolders.size());
        bankSummary.setTotalBalance(totalBankBalance);
        if (richestAccount != null) {
            bankSummary.setTopAccountLabel(buildBankAccountLabel(richestAccount));
            bankSummary.setTopAccountBalance(richestAccount.getBalance());
        }

        // Deposits
        List<Deposit> allDeposits = getAllDeposits();
        SummaryData.DepositSummary depositSummary = summary.getDeposits();
        depositSummary.setTotalCount(allDeposits.size());
        double totalFdPrincipal = 0.0;
        double totalFdMaturity = 0.0;
        double totalRdContribution = 0.0;
        double totalRdMaturity = 0.0;
        double totalGullakBalance = 0.0;
        double totalGullakDue = 0.0;
        List<SummaryData.DepositSummary.MaturityInfo> maturityInfos = new ArrayList<>();
        DateTimeFormatter depositDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        for (Deposit deposit : allDeposits) {
            String type = deposit.getDepositType();
            if ("FD".equalsIgnoreCase(type)) {
                totalFdPrincipal += deposit.getPrincipalAmount();
                totalFdMaturity += deposit.calculateFDMaturityAmount();
            } else if ("RD".equalsIgnoreCase(type)) {
                double contribution = deposit.getMonthlyAmount() * Math.max(deposit.getTenure(), 0);
                if ("Years".equalsIgnoreCase(deposit.getTenureUnit())) {
                    contribution = deposit.getMonthlyAmount() * deposit.getTenure() * 12.0;
                } else if ("Days".equalsIgnoreCase(deposit.getTenureUnit())) {
                    contribution = deposit.getMonthlyAmount() * (deposit.getTenure() / 30.0);
                }
                totalRdContribution += Math.max(contribution, 0.0);
                totalRdMaturity += deposit.calculateRDMaturityAmount();
            } else if ("Gullak".equalsIgnoreCase(type)) {
                totalGullakBalance += deposit.getCurrentTotal();
                totalGullakDue += deposit.getGullakDueAmount();
            }

            String maturityStr = deposit.calculateMaturityDate();
            if (maturityStr != null && !maturityStr.isBlank()) {
                try {
                    LocalDate maturityDate = LocalDate.parse(maturityStr, depositDateFormatter);
                    double principalValue = 0.0;
                    double maturityValue = 0.0;
                    if ("FD".equalsIgnoreCase(type)) {
                        principalValue = deposit.getPrincipalAmount();
                        maturityValue = deposit.calculateFDMaturityAmount();
                    } else if ("RD".equalsIgnoreCase(type)) {
                        principalValue = deposit.getMonthlyAmount() * Math.max(deposit.getTenure(), 0);
                        maturityValue = deposit.calculateRDMaturityAmount();
                    } else if ("Gullak".equalsIgnoreCase(type)) {
                        principalValue = deposit.getCurrentTotal();
                        maturityValue = deposit.getCurrentTotal();
                    }
                    maturityInfos.add(new SummaryData.DepositSummary.MaturityInfo(
                            buildDepositLabel(deposit),
                            maturityDate,
                            maturityStr,
                            principalValue,
                            maturityValue
                    ));
                } catch (Exception ignored) {
                    // Ignore invalid or placeholder maturity dates.
                }
            }
        }
        depositSummary.setTotalFdPrincipal(totalFdPrincipal);
        depositSummary.setTotalFdMaturityEstimate(totalFdMaturity);
        depositSummary.setTotalRdContribution(totalRdContribution);
        depositSummary.setTotalRdMaturityEstimate(totalRdMaturity);
        depositSummary.setTotalGullakBalance(totalGullakBalance);
        depositSummary.setTotalGullakDue(totalGullakDue);
        maturityInfos.sort(Comparator.comparing(SummaryData.DepositSummary.MaturityInfo::getDueDate));
        if (maturityInfos.size() > 5) {
            maturityInfos = new ArrayList<>(maturityInfos.subList(0, 5));
        }
        depositSummary.setMaturityHighlights(maturityInfos);

        // Investments
        List<Investment> investmentList = getAllInvestments();
        SummaryData.InvestmentSummary investmentSummary = summary.getInvestments();
        investmentSummary.setTotalCount(investmentList.size());
        double totalInitialValue = 0.0;
        double totalCurrentValue = 0.0;
        List<SummaryData.InvestmentSummary.InvestmentHighlight> investmentHighlights = new ArrayList<>();
        for (Investment investment : investmentList) {
            double initial = investment.getTotalInitialCost();
            double current = investment.getTotalCurrentValue();
            totalInitialValue += initial;
            totalCurrentValue += current;
            investmentHighlights.add(new SummaryData.InvestmentSummary.InvestmentHighlight(
                    buildInvestmentLabel(investment),
                    safeTrim(investment.getAssetType()),
                    current,
                    investment.getProfitOrLoss(),
                    investment.getProfitOrLossPercentage()
            ));
        }
        investmentSummary.setTotalInitialValue(totalInitialValue);
        investmentSummary.setTotalCurrentValue(totalCurrentValue);
        investmentSummary.setTotalProfitOrLoss(totalCurrentValue - totalInitialValue);
        investmentHighlights.sort((a, b) -> Double.compare(b.getProfitOrLoss(), a.getProfitOrLoss()));
        if (investmentHighlights.size() > 5) {
            investmentHighlights = new ArrayList<>(investmentHighlights.subList(0, 5));
        }
        investmentSummary.setTopPerformers(investmentHighlights);

        // Loans
        List<Loan> loanList = getAllLoans();
        SummaryData.LoanSummary loanSummary = summary.getLoans();
        loanSummary.setTotalCount(loanList.size());
        double totalPrincipal = 0.0;
        double totalPrincipalOutstanding = 0.0;
        double totalPrincipalPaid = 0.0;
        double totalMonthlyEmi = 0.0;
        double totalRepayableOutstanding = 0.0;
        int activeLoans = 0;
        int closedLoans = 0;
        List<SummaryData.LoanSummary.LoanHighlight> loanHighlights = new ArrayList<>();
        for (Loan loan : loanList) {
            String status = safeTrim(loan.getStatus());
            totalPrincipal += loan.getPrincipalAmount();
            boolean isClosed = status.equalsIgnoreCase("Closed") || status.equalsIgnoreCase("Paid Off") || status.equalsIgnoreCase("Completed");
            if (isClosed) {
                closedLoans++;
                totalPrincipalPaid += loan.getPrincipalAmount();
            } else {
                activeLoans++;
                totalPrincipalOutstanding += loan.getPrincipalAmount();
                totalMonthlyEmi += loan.getEmiAmount();
                totalRepayableOutstanding += loan.getTotalPayment();
            }
            loanHighlights.add(new SummaryData.LoanSummary.LoanHighlight(
                    buildLoanLabel(loan),
                    safeTrim(loan.getLoanType()),
                    status.isEmpty() ? "Active" : status,
                    loan.getEmiAmount(),
                    loan.getPrincipalAmount(),
                    loan.getTotalPayment()
            ));
        }
        loanSummary.setActiveCount(activeLoans);
        loanSummary.setPaidOffCount(closedLoans);
        loanSummary.setTotalPrincipal(totalPrincipal);
        loanSummary.setTotalPrincipalOutstanding(totalPrincipalOutstanding);
        loanSummary.setTotalPrincipalPaidOff(totalPrincipalPaid);
        loanSummary.setTotalMonthlyEmi(totalMonthlyEmi);
        loanSummary.setTotalRepayableOutstanding(totalRepayableOutstanding);
        loanHighlights.sort((a, b) -> Double.compare(b.getTotalRepayable(), a.getTotalRepayable()));
        if (loanHighlights.size() > 5) {
            loanHighlights = new ArrayList<>(loanHighlights.subList(0, 5));
        }
        loanSummary.setKeyLoans(loanHighlights);

        // Cards
        List<Card> cardList = getAllCards();
        SummaryData.CardSummary cardSummary = summary.getCards();
        cardSummary.setTotalCount(cardList.size());
        int creditCount = 0;
        int debitCount = 0;
        double totalCreditLimit = 0.0;
        double totalCreditUsed = 0.0;
        double totalCreditAvailable = 0.0;
        double totalCreditDue = 0.0;
        List<SummaryData.CardSummary.CardHighlight> cardHighlights = new ArrayList<>();
        for (Card card : cardList) {
            boolean isCredit = "Credit Card".equalsIgnoreCase(safeTrim(card.getCardType()));
            if (isCredit) {
                creditCount++;
                totalCreditLimit += card.getCreditLimit();
                totalCreditUsed += card.getCurrentExpenses();
                double available = card.getCreditLimit() - card.getCurrentExpenses();
                if (available < 0) {
                    available = 0;
                }
                totalCreditAvailable += available;
                totalCreditDue += card.getAmountToPay();
                cardHighlights.add(new SummaryData.CardSummary.CardHighlight(
                        card.getCardName(),
                        card.getCardType(),
                        card.getCreditLimit(),
                        available,
                        card.getAmountToPay()
                ));
            } else {
                debitCount++;
                cardHighlights.add(new SummaryData.CardSummary.CardHighlight(
                        card.getCardName(),
                        card.getCardType(),
                        0.0,
                        0.0,
                        card.getAmountToPay()
                ));
            }
        }
        cardSummary.setCreditCardCount(creditCount);
        cardSummary.setDebitCardCount(debitCount);
        cardSummary.setTotalCreditLimit(totalCreditLimit);
        cardSummary.setTotalCreditUsed(totalCreditUsed);
        cardSummary.setTotalCreditAvailable(totalCreditAvailable);
        cardSummary.setTotalCreditDue(totalCreditDue);
        cardHighlights.sort((a, b) -> Double.compare(b.getAmountDue(), a.getAmountDue()));
        if (cardHighlights.size() > 5) {
            cardHighlights = new ArrayList<>(cardHighlights.subList(0, 5));
        }
        cardSummary.setKeyCards(cardHighlights);

        // Tax profiles
        List<TaxProfile> taxProfiles = getAllTaxProfiles();
        SummaryData.TaxSummary taxSummary = summary.getTax();
        taxSummary.setProfileCount(taxProfiles.size());
        double totalGrossIncome = 0.0;
        double totalDeductions = 0.0;
        double totalTaxableIncome = 0.0;
        double totalTaxPaid = 0.0;
        TaxProfile latestProfile = null;
        int latestYearKey = Integer.MIN_VALUE;
        List<SummaryData.TaxSummary.TaxProfileHighlight> taxHighlights = new ArrayList<>();
        for (TaxProfile profile : taxProfiles) {
            totalGrossIncome += profile.getGrossIncome();
            totalDeductions += profile.getTotalDeductions();
            totalTaxableIncome += profile.getTaxableIncome();
            totalTaxPaid += profile.getTaxPaid();

            int key = parseFinancialYearKey(profile.getFinancialYear());
            if (key > latestYearKey) {
                latestYearKey = key;
                latestProfile = profile;
            }

            taxHighlights.add(new SummaryData.TaxSummary.TaxProfileHighlight(
                    profile.getProfileName(),
                    safeTrim(profile.getFinancialYear()),
                    profile.getTaxableIncome(),
                    profile.getTaxPaid()
            ));
        }
        taxSummary.setTotalGrossIncome(totalGrossIncome);
        taxSummary.setTotalDeductions(totalDeductions);
        taxSummary.setTotalTaxableIncome(totalTaxableIncome);
        taxSummary.setTotalTaxPaid(totalTaxPaid);
        if (latestProfile != null) {
            taxSummary.setLatestFinancialYear(safeTrim(latestProfile.getFinancialYear()));
            taxSummary.setLatestYearTaxable(latestProfile.getTaxableIncome());
            taxSummary.setLatestYearTaxPaid(latestProfile.getTaxPaid());
        }
        taxHighlights.sort((a, b) -> Double.compare(b.getTaxableIncome(), a.getTaxableIncome()));
        if (taxHighlights.size() > 5) {
            taxHighlights = new ArrayList<>(taxHighlights.subList(0, 5));
        }
        taxSummary.setKeyProfiles(taxHighlights);

        return summary;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String buildBankAccountLabel(BankAccount account) {
        StringBuilder label = new StringBuilder();
        if (account.getBankName() != null && !account.getBankName().isBlank()) {
            label.append(account.getBankName().trim());
        }
        if (account.getAccountNumber() != null && !account.getAccountNumber().isBlank()) {
            if (label.length() > 0) {
                label.append(" - ");
            }
            label.append(account.getAccountNumber().trim());
        }
        if (label.length() == 0 && account.getHolderName() != null && !account.getHolderName().isBlank()) {
            label.append(account.getHolderName().trim());
        }
        return label.length() == 0 ? "Account" : label.toString();
    }

    private String buildDepositLabel(Deposit deposit) {
        StringBuilder label = new StringBuilder();
        if (deposit.getDepositType() != null && !deposit.getDepositType().isBlank()) {
            label.append(deposit.getDepositType().trim());
        }
        if (deposit.getHolderName() != null && !deposit.getHolderName().isBlank()) {
            if (label.length() > 0) {
                label.append(" - ");
            }
            label.append(deposit.getHolderName().trim());
        } else if (deposit.getDescription() != null && !deposit.getDescription().isBlank()) {
            if (label.length() > 0) {
                label.append(" - ");
            }
            label.append(deposit.getDescription().trim());
        }
        return label.length() == 0 ? "Deposit" : label.toString();
    }

    private String buildInvestmentLabel(Investment investment) {
        StringBuilder label = new StringBuilder();
        if (investment.getAssetType() != null && !investment.getAssetType().isBlank()) {
            label.append(investment.getAssetType().trim());
        }
        if (investment.getDescription() != null && !investment.getDescription().isBlank()) {
            if (label.length() > 0) {
                label.append(" - ");
            }
            label.append(investment.getDescription().trim());
        }
        if (label.length() == 0 && investment.getHolderName() != null && !investment.getHolderName().isBlank()) {
            label.append(investment.getHolderName().trim());
        }
        return label.length() == 0 ? "Investment" : label.toString();
    }

    private String buildLoanLabel(Loan loan) {
        StringBuilder label = new StringBuilder();
        if (loan.getLoanType() != null && !loan.getLoanType().isBlank()) {
            label.append(loan.getLoanType().trim());
        }
        if (loan.getLenderName() != null && !loan.getLenderName().isBlank()) {
            if (label.length() > 0) {
                label.append(" - ");
            }
            label.append(loan.getLenderName().trim());
        }
        return label.length() == 0 ? "Loan" : label.toString();
    }

    private int parseFinancialYearKey(String financialYear) {
        if (financialYear == null || financialYear.isBlank()) {
            return Integer.MIN_VALUE;
        }
        String[] tokens = financialYear.split("[^0-9]");
        for (String token : tokens) {
            if (token.length() == 4) {
                try {
                    return Integer.parseInt(token);
                } catch (NumberFormatException ignored) {
                    // Ignore invalid token.
                }
            }
        }
        for (String token : tokens) {
            if (token.length() == 2) {
                try {
                    return 2000 + Integer.parseInt(token);
                } catch (NumberFormatException ignored) {
                    // Ignore invalid token.
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Moves a loan to the recycle bin table.
     */
    public void moveLoanToRecycleBin(int loanId) throws SQLException {
        int accountId = requireAccountId();
        String copySql = "INSERT INTO recycle_bin_loans (id, account_id, lender_name, loan_type, principal_amount, interest_rate, tenure_months, start_date, status, notes, emi_amount, total_interest, total_payment, deleted_on) " +
                         "SELECT id, account_id, lender_name, loan_type, principal_amount, interest_rate, tenure_months, start_date, status, notes, emi_amount, total_interest, total_payment, NOW() FROM loans WHERE id = ? AND account_id = ?";
        String deleteSql = "DELETE FROM loans WHERE id = ? AND account_id = ?";

        boolean oldAuto = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement copyPs = connection.prepareStatement(copySql);
             PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {

            copyPs.setInt(1, loanId);
            copyPs.setInt(2, accountId);
            deletePs.setInt(1, loanId);
            deletePs.setInt(2, accountId);

            int copied = copyPs.executeUpdate();
            if (copied == 0) {
                throw new SQLException("Failed to copy loan to recycle bin, ID not found for current account: " + loanId);
            }

            deletePs.executeUpdate();
            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            System.err.println("!!! ERROR moving loan to recycle bin !!!");
            e.printStackTrace();
            throw e;
        } finally {
            connection.setAutoCommit(oldAuto);
        }
    }
    
    /**
     * Fetches recycled loans for the UI dialog.
     */
    public List<Map<String, Object>> getRecycledLoansForUI() throws SQLException {
        List<Map<String, Object>> recycled = new ArrayList<>();
        int accountId = requireAccountId();
        String sql = "SELECT id, loan_type, lender_name, principal_amount, " +
                     "DATE_FORMAT(deleted_on, '%Y-%m-%d %H:%i:%s') as deleted_on_str " +
                     "FROM recycle_bin_loans WHERE account_id = ? ORDER BY deleted_on DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                 Map<String, Object> data = new HashMap<>();
                 data.put("id", rs.getInt("id"));
                 data.put("loan_type", rs.getString("loan_type"));
                 data.put("lender_name", rs.getString("lender_name"));
                 data.put("principal_amount", rs.getDouble("principal_amount"));
                 data.put("deleted_on_str", rs.getString("deleted_on_str"));
                 recycled.add(data);
            }
            }
        } catch (SQLException e) { System.err.println("!!! ERROR getting recycled loans !!!"); e.printStackTrace(); throw e; }
        return recycled;
    }

    /**
     * Restores a loan from the recycle bin.
     */
    public void restoreLoan(int loanId) throws SQLException {
        int accountId = requireAccountId();
        String copySql = "INSERT INTO loans (id, account_id, lender_name, loan_type, principal_amount, interest_rate, tenure_months, start_date, emi_amount, total_interest, total_payment, status, notes) " +
                       "SELECT id, account_id, lender_name, loan_type, principal_amount, interest_rate, tenure_months, start_date, emi_amount, total_interest, total_payment, status, notes " +
                       "FROM recycle_bin_loans WHERE id = ? AND account_id = ?";
        String deleteSql = "DELETE FROM recycle_bin_loans WHERE id = ? AND account_id = ?";
        
        boolean oldAuto = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement copyPs = connection.prepareStatement(copySql);
             PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {
            
            copyPs.setInt(1, loanId);
            copyPs.setInt(2, accountId);
            deletePs.setInt(1, loanId);
            deletePs.setInt(2, accountId);
            
            copyPs.executeUpdate();
            deletePs.executeUpdate();
            
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            if (e.getErrorCode() == 1062) { // Handle duplicate key (already restored)
                 System.out.println("Loan " + loanId + " already exists. Removing from recycle bin.");
                 try (PreparedStatement deletePsOnly = connection.prepareStatement(deleteSql)) {
                     deletePsOnly.setInt(1, loanId);
                     deletePsOnly.setInt(2, accountId);
                     deletePsOnly.executeUpdate();
                     connection.commit();
                 } catch (SQLException ex) {
                     connection.rollback();
                     throw ex;
                 }
            } else { throw e; }
        } finally {
            connection.setAutoCommit(oldAuto);
        }
    }

    /**
     * Permanently deletes a loan from the recycle bin.
     */
    public void permanentlyDeleteLoan(int loanId) throws SQLException {
        int accountId = requireAccountId();
        String sql = "DELETE FROM recycle_bin_loans WHERE id = ? AND account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, loanId);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }
    // ==================================================================
    // ===         NEW LENDING METHODS                              ===
    // ==================================================================

    /**
     * Saves a new lending record to the database.
     */
    public void saveLending(Lending lending) throws SQLException {
        String sql = "INSERT INTO lendings (account_id, borrower_name, loan_type, principal_amount, interest_rate, " +
                     "tenure_months, date_lent, status, notes, " +
                     "interest_to_receive, total_to_receive, monthly_payment) " + // Corrected column names
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, SessionContext.getCurrentAccountId()); // Add account_id
            ps.setString(2, lending.getBorrowerName()); // Use getBorrowerName()
            ps.setString(3, lending.getLoanType());
            ps.setDouble(4, lending.getPrincipalAmount());
            ps.setDouble(5, lending.getInterestRate());
            ps.setInt(6, lending.getTenureMonths());

            // Handle Date
            if (lending.getDateLent() != null && !lending.getDateLent().isEmpty()) {
                 try {
                    LocalDate localDate = LocalDate.parse(lending.getDateLent(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    ps.setDate(7, java.sql.Date.valueOf(localDate));
                 } catch (Exception e) {
                     System.err.println("Invalid lending start date format, setting to null: " + lending.getDateLent());
                     ps.setNull(7, Types.DATE);
                 }
            } else {
                ps.setNull(7, Types.DATE);
            }
            
            ps.setString(8, "Active"); // Default status
            ps.setString(9, lending.getNotes());
            
            // Add the calculated fields
            ps.setDouble(10, lending.getTotalInterestToReceive()); // Corrected method
            ps.setDouble(11, lending.getTotalToReceive());      // Corrected method
            ps.setDouble(12, lending.getMonthlyPayment());     // Corrected method
            
            ps.executeUpdate();
            
            // Optional: Get generated ID
            // try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
            //     if (generatedKeys.next()) {
            //         // We can set the ID on the object if we add a setId method to Loan.java
            //         // loan.setId(generatedKeys.getInt(1)); 
            //     }
            // }
        } catch (SQLException e) { 
            System.err.println("!!! ERROR saving lending record (SQL) !!!"); 
            e.printStackTrace(); throw e; 
        }
    }
    
    /**
     * Fetches all lending records from the database.
     */
    public List<Lending> getAllLendings() throws SQLException {
        System.out.println("DEBUG: Entering getAllLendings...");
        List<Lending> lendings = new ArrayList<>();
        
        // Check if account_id column exists, if not, return empty list or use fallback query
        if (!columnExists("lendings", "account_id")) {
            System.out.println("DEBUG: account_id column doesn't exist in lendings table yet, returning empty list");
            return lendings;
        }
        
        String sql = "SELECT * FROM lendings WHERE account_id = ? ORDER BY status, borrower_name";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, SessionContext.getCurrentAccountId());
            try (ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                try {
                    java.sql.Date sqlDateLent = rs.getDate("date_lent");
                    String formattedDateLent = (sqlDateLent != null) ? dateFormat.format(sqlDateLent) : null;
                    
                    Lending lending = new Lending(
                        rs.getInt("id"),
                        rs.getString("borrower_name"),
                        rs.getString("loan_type"),
                        rs.getDouble("principal_amount"),
                        rs.getDouble("interest_rate"),
                        rs.getInt("tenure_months"),
                        formattedDateLent,
                        rs.getString("status"),
                        rs.getString("notes"),
                        rs.getDouble("monthly_payment"),
                        rs.getDouble("interest_to_receive"),
                        rs.getDouble("total_to_receive")
                    );
                    lendings.add(lending);
                } catch (Exception e) {
                    System.err.println("!!! ERROR creating Lending object from ResultSet !!!");
                    e.printStackTrace();
                }
            }
            System.out.println("DEBUG: Fetched " + lendings.size() + " lending records.");
            }
        } catch (SQLException e) {
            System.err.println("!!! ERROR executing SQL in getAllLendings !!!"); 
            e.printStackTrace(); 
            throw e; 
        }
        return lendings;
    }
    
    /**
     * Updates the status of a lending record (e.g., to "Repaid").
     */
    public void updateLendingStatus(int lendingId, String newStatus) throws SQLException {
        String sql = "UPDATE lendings SET status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, lendingId);
            ps.executeUpdate();
        } catch (SQLException e) { 
            System.err.println("!!! ERROR updating lending status (SQL) !!!"); 
            e.printStackTrace(); 
            throw e; 
        }
    }

    /**
     * Moves a lending record to the recycle bin table.
     */
    public void moveLendingToRecycleBin(int lendingId) throws SQLException {
        String copySql = "INSERT INTO recycle_bin_lendings SELECT *, NOW() FROM lendings WHERE id = ?";
        String deleteSql = "DELETE FROM lendings WHERE id = ?";
        
        connection.setAutoCommit(false);
        try (PreparedStatement copyPs = connection.prepareStatement(copySql);
             PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {
            
            copyPs.setInt(1, lendingId);
            deletePs.setInt(1, lendingId);
            
            int copied = copyPs.executeUpdate();
            if (copied == 0) throw new SQLException("Failed to copy lending to recycle bin, ID not found: " + lendingId);
            
            deletePs.executeUpdate();
            connection.commit();
            
        } catch (SQLException e) {
            connection.rollback();
            System.err.println("!!! ERROR moving lending record to recycle bin !!!"); 
            e.printStackTrace();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    /**
     * Fetches recycled lending records for the UI dialog.
     */
    public List<Map<String, Object>> getRecycledLendingsForUI() throws SQLException {
        List<Map<String, Object>> recycled = new ArrayList<>();
        String sql = "SELECT id, loan_type, borrower_name, principal_amount, " +
                     "DATE_FORMAT(deleted_on, '%Y-%m-%d %H:%i:%s') as deleted_on_str " +
                     "FROM recycle_bin_lendings WHERE account_id = ? ORDER BY deleted_on DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, SessionContext.getCurrentAccountId());
            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                 Map<String, Object> data = new HashMap<>();
                 data.put("id", rs.getInt("id"));
                 data.put("loan_type", rs.getString("loan_type"));
                 data.put("borrower_name", rs.getString("borrower_name"));
                 data.put("principal_amount", rs.getDouble("principal_amount"));
                 data.put("deleted_on_str", rs.getString("deleted_on_str"));
                 recycled.add(data);
            }
            }
        } catch (SQLException e) { System.err.println("!!! ERROR getting recycled lendings !!!"); e.printStackTrace(); throw e; }
        return recycled;
    }

    /**
     * Restores a lending record from the recycle bin.
     */
    public void restoreLending(int lendingId) throws SQLException {
        String copySql = "INSERT INTO lendings (id, account_id, borrower_name, loan_type, principal_amount, interest_rate, tenure_months, date_lent, monthly_payment, interest_to_receive, total_to_receive, status, notes) " +
                       "SELECT id, account_id, borrower_name, loan_type, principal_amount, interest_rate, tenure_months, date_lent, monthly_payment, interest_to_receive, total_to_receive, status, notes " +
                       "FROM recycle_bin_lendings WHERE id = ?";
        String deleteSql = "DELETE FROM recycle_bin_lendings WHERE id = ?";
        
        connection.setAutoCommit(false);
        try (PreparedStatement copyPs = connection.prepareStatement(copySql);
             PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {
            
            copyPs.setInt(1, lendingId);
            deletePs.setInt(1, lendingId);
            
            copyPs.executeUpdate();
            deletePs.executeUpdate();
            
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            if (e.getErrorCode() == 1062) { // Handle duplicate key
                 System.out.println("Lending record " + lendingId + " already exists. Removing from recycle bin.");
                 try (PreparedStatement deletePsOnly = connection.prepareStatement(deleteSql)) {
                     deletePsOnly.setInt(1, lendingId); deletePsOnly.executeUpdate(); connection.commit();
                 } catch (SQLException ex) { connection.rollback(); throw ex; }
            } else { throw e; }
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Permanently deletes a lending record from the recycle bin.
     */
    public void permanentlyDeleteLending(int lendingId) throws SQLException {
        String sql = "DELETE FROM recycle_bin_lendings WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, lendingId);
            ps.executeUpdate();
        }
    }
    private static void registerGoogleFonts() {
    try {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        int fontsLoaded = 0;
        
        // List of Poppins font files to load
        String[] poppinsFonts = {
            "Poppins-Regular.ttf",
            "Poppins-Bold.ttf",
            "Poppins-SemiBold.ttf",
            "Poppins-Medium.ttf",
            "Poppins-Thin.ttf",
            "Poppins-ExtraBold.ttf",
            "Poppins-Italic.ttf",
            "Poppins-SemiBoldItalic.ttf"
        };
        
        for (String fontFile : poppinsFonts) {
            try {
                // Try loading from resources in classpath
                InputStream fontStream = FinanceManager.class.getResourceAsStream("/resources/" + fontFile);
                
                // If not found in classpath, try from file system
                if (fontStream == null) {
                    try {
                        java.io.File file = new java.io.File("src/resources/" + fontFile);
                        if (file.exists()) {
                            fontStream = new java.io.FileInputStream(file);
                        }
                    } catch (Exception ignored) {}
                }
                
                if (fontStream != null) {
                    Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                    ge.registerFont(font);
                    fontStream.close();
                    fontsLoaded++;
                } else {
                    System.err.println("Warning: " + fontFile + " not found in resources");
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to load " + fontFile + ": " + e.getMessage());
            }
        }
        
        if (fontsLoaded > 0) {
            System.out.println("Successfully registered " + fontsLoaded + " Poppins font(s) for SmartLedger.");
        } else {
            System.err.println("Warning: No Poppins fonts were loaded. Using system default fonts.");
        }

    } catch (Exception e) {
        System.err.println("Failed to load Poppins fonts. Using system default.");
        e.printStackTrace();
    }
}
}