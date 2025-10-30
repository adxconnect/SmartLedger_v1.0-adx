package src;
import src.Investment;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import src.db.DBHelper;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import src.TaxProfile;
import src.Loan;
// Same-package types don't require imports

public class FinanceManager {
    private List<Transaction> transactions = new ArrayList<>();
    private DBHelper dbHelper;
    private Connection connection;
    

    public FinanceManager() throws SQLException { 
        dbHelper = new DBHelper();
        connection = dbHelper.getConnection();
        this.cards = new ArrayList<>();
        createTables();
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
    
    public boolean editTransaction(int index, Transaction newTransaction) {
        if (index >= 0 && index < transactions.size()) {
            transactions.set(index, newTransaction);
            return true;
        }
        return false;
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

    private List<Card> cards = new ArrayList<>();
    
    // --- Bank Account (Database) Methods ---
    public void saveBankAccount(BankAccount ba) throws SQLException {
        String sql = "INSERT INTO bank_accounts (account_number, holder_name, bank_name, ifsc_code, balance, " +
                     "account_type, interest_rate, annual_expense, " +
                     "account_subtype, company_name, business_name) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
            ps.setString(1, ba.getAccountNumber());
            ps.setString(2, ba.getHolderName());
            ps.setString(3, ba.getBankName());
            ps.setString(4, ba.getIfscCode());
            ps.setDouble(5, ba.getBalance());
            ps.setString(6, ba.getAccountType());
            ps.setDouble(7, ba.getInterestRate());
            ps.setDouble(8, ba.getAnnualExpense());
            ps.setString(9, ba.getAccountSubtype());
            ps.setString(10, ba.getCompanyName());
            ps.setString(11, ba.getBusinessName());
            
            ps.executeUpdate();
        }
    }

    public List<BankAccount> getAllBankAccounts() throws SQLException {
        List<BankAccount> accounts = new ArrayList<>();
        String sql = "SELECT * FROM bank_accounts";
        
        try (Statement stmt = dbHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
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
        return accounts;
    }

    public void deleteBankAccount(int accountId) throws SQLException {
        String sql = "DELETE FROM bank_accounts WHERE id = ?";
        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.executeUpdate();
        }
    }

    // --- Transaction (Database) Methods ---
    public void saveTransaction(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (date, category, type, amount, description, day, payment_method, payee) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
            
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
            java.time.LocalDate localDate = java.time.LocalDate.parse(t.getDate(), formatter);
            java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
            ps.setDate(1, sqlDate); 

            ps.setString(2, t.getCategory());
            ps.setString(3, t.getType());
            ps.setDouble(4, t.getAmount());
            ps.setString(5, t.getDescription());
            ps.setString(6, t.getDay());
            ps.setString(7, t.getPaymentMethod());
            ps.setString(8, t.getPayee());
            
            ps.executeUpdate();
        } catch (java.time.format.DateTimeParseException e) {
            throw new SQLException("Invalid date format. Please use DD-MM-YYYY.", e);
        }
    }

    // --- Fixed Deposit (Database) Methods ---
   

    // --- Card (Database) Methods ---
    public void saveCard(Card card) throws SQLException {
        System.out.println("DEBUG: Saving card: " + card.getCardName());
        String sql = "INSERT INTO cards (unique_id, card_name, card_type, card_number, valid_from, valid_through, cvv, " +
                     "front_image_path, back_image_path, credit_limit, current_expenses, amount_to_pay, days_left_to_pay, creation_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, java.util.UUID.randomUUID().toString());
            ps.setString(2, card.getCardName());
            ps.setString(3, card.getCardType());
            ps.setString(4, card.getCardNumber());
            ps.setString(5, card.getValidFrom());
            ps.setString(6, card.getValidThrough());
            ps.setString(7, card.getCvv());
            ps.setString(8, card.getFrontImagePath());
            ps.setString(9, card.getBackImagePath());

            // Credit fields: only set for Credit Card, else NULL
            boolean isCredit = "Credit Card".equals(card.getCardType());
            if (isCredit) {
                ps.setDouble(10, card.getCreditLimit());
                ps.setDouble(11, card.getCurrentExpenses());
                ps.setDouble(12, card.getAmountToPay());
                ps.setInt(13, card.getDaysLeftToPay());
            } else {
                ps.setNull(10, Types.DOUBLE);
                ps.setNull(11, Types.DOUBLE);
                ps.setNull(12, Types.DOUBLE);
                ps.setNull(13, Types.INTEGER);
            }

            // creation_date as DATE (YYYY-MM-DD)
            java.sql.Date created = null;
            try {
                if (card.getCreationDate() != null) {
                    java.time.LocalDate ld = java.time.LocalDate.parse(card.getCreationDate());
                    created = java.sql.Date.valueOf(ld);
                }
            } catch (Exception ignore) { /* leave as null */ }
            if (created != null) ps.setDate(14, created); else ps.setNull(14, Types.DATE);

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
        List<String> years = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(date) as year FROM transactions ORDER BY year DESC";
        
        try (Statement stmt = dbHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                years.add(String.valueOf(rs.getInt("year")));
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
        Map<String, List<Transaction>> groupedTransactions = new LinkedHashMap<>();
        String sql = "SELECT id, date, DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i:%s') as timestamp_str, " +
                     "day, category, type, payment_method, payee, amount, description, " +
                     "DATE_FORMAT(date, '%m-%Y') as month_year " +
                     "FROM transactions ";

        if (year != null && !year.equals("All Years")) {
            sql += "WHERE YEAR(date) = ? ";
        }
        
        sql += "ORDER BY date ASC, id ASC";

        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
            
            if (year != null && !year.equals("All Years")) {
                ps.setInt(1, Integer.parseInt(year));
            }
            
            ResultSet rs = ps.executeQuery();
            
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
            rs.close();
        }
        return groupedTransactions;
    }
    
    // --- NEW RECYCLE BIN METHODS ---

    private void moveTransactionToRecycleBin(int transactionId) throws SQLException {
        String copySql = "INSERT INTO recycle_bin_transactions " +
                         "(id, timestamp, date, day, payment_method, category, type, payee, description, amount) " +
                         "SELECT " +
                         "id, timestamp, date, day, payment_method, category, type, payee, description, amount " +
                         "FROM transactions WHERE id = ?";
        
        String deleteSql = "DELETE FROM transactions WHERE id = ?";
        
        Connection conn = dbHelper.getConnection();
        conn.setAutoCommit(false); 
        
        try (PreparedStatement copyPs = conn.prepareStatement(copySql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
            
            copyPs.setInt(1, transactionId);
            deletePs.setInt(1, transactionId);
            
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

    public void deleteTransactionsByMonth(String monthYear) throws SQLException {
        List<Integer> idsToDelete = new ArrayList<>();
        String sql = "SELECT id FROM transactions WHERE DATE_FORMAT(date, '%m-%Y') = ?";
        
        try(PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
            ps.setString(1, monthYear);
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

    public void deleteTransactionsByYear(String year) throws SQLException {
        List<Integer> idsToDelete = new ArrayList<>();
        String sql = "SELECT id FROM transactions WHERE YEAR(date) = ?";
        
        try(PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(year));
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
        List<Transaction> recycledTxs = new ArrayList<>();
        // Select most columns, but get the ORIGINAL id
        String sql = "SELECT id, date, DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i:%s') as timestamp_str, " +
                     "day, category, type, payment_method, payee, amount, description " +
                     "FROM recycle_bin_transactions ORDER BY deleted_on DESC"; // Show newest deleted first
        
        try (Statement stmt = dbHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // Use the Transaction constructor (we don't need deleted_on in the object)
                recycledTxs.add(new Transaction(
                    rs.getInt("id"), // Use the original ID
                    // Format date back to DD-MM-YYYY
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
        return recycledTxs;
    }

    /**
     * Restores a transaction from the recycle bin back to the main transactions table.
     */
    public void restoreTransaction(int transactionId) throws SQLException {
        // 1. Copy the transaction back to the main table
        String copySql = "INSERT INTO transactions " +
                         "(id, timestamp, date, day, payment_method, category, type, payee, description, amount) " +
                         "SELECT " +
                         "id, timestamp, date, day, payment_method, category, type, payee, description, amount " +
                         "FROM recycle_bin_transactions WHERE id = ?";
        
        // 2. Delete the transaction from the recycle bin
        String deleteSql = "DELETE FROM recycle_bin_transactions WHERE id = ?";
        
        Connection conn = dbHelper.getConnection();
        conn.setAutoCommit(false); // Use a transaction
        
        try (PreparedStatement copyPs = conn.prepareStatement(copySql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
            
            copyPs.setInt(1, transactionId);
            deletePs.setInt(1, transactionId);
            
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
        String sql = "DELETE FROM recycle_bin_transactions WHERE id = ?";
        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            ps.executeUpdate();
        }
    }
    // Replace this method in src/FinanceManager.java
    public void saveDeposit(Deposit d) throws SQLException {
        // Added count columns and gullak_due_amount
        String sql = "INSERT INTO deposits (deposit_type, holder_name, description, goal, " +
                     "account_number, principal_amount, monthly_amount, interest_rate, " +
                     "tenure, tenure_unit, start_date, current_total, last_updated, " +
                     "count_500, count_200, count_100, count_50, count_20, count_10, count_5, count_2, count_1, gullak_due_amount) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getDepositType());
            ps.setString(2, d.getHolderName());
            ps.setString(3, d.getDescription());
            ps.setString(4, d.getGoal());
            ps.setString(5, d.getAccountNumber());
            ps.setDouble(6, d.getPrincipalAmount());
            ps.setDouble(7, d.getMonthlyAmount());
            ps.setDouble(8, d.getInterestRate());
            ps.setInt(9, d.getTenure());
            ps.setString(10, d.getTenureUnit());

            // Handle date conversion for start_date
            if (d.getStartDate() != null && !d.getStartDate().isEmpty()) {
                try {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    java.time.LocalDate localDate = java.time.LocalDate.parse(d.getStartDate(), formatter);
                    ps.setDate(11, java.sql.Date.valueOf(localDate));
                } catch (Exception e) {
                    ps.setNull(11, java.sql.Types.DATE);
                    System.err.println("Invalid start date format for deposit: " + d.getStartDate());
                }
            } else {
                ps.setNull(11, java.sql.Types.DATE);
            }

            // Get calculated total for Gullak, otherwise 0
            double calculatedTotal = ("Gullak".equals(d.getDepositType())) ? d.calculateTotalFromDenominations() : 0.0;
            ps.setDouble(12, calculatedTotal);

            // Set last_updated only if it's a Gullak
            if ("Gullak".equals(d.getDepositType())) {
                ps.setTimestamp(13, new java.sql.Timestamp(System.currentTimeMillis()));
            } else {
                ps.setNull(13, java.sql.Types.TIMESTAMP);
            }

            // Set denomination counts (default to 0 if null or not Gullak)
            Map<Integer, Integer> counts = d.getDenominationCounts();
            ps.setInt(14, counts != null ? counts.getOrDefault(500, 0) : 0);
            ps.setInt(15, counts != null ? counts.getOrDefault(200, 0) : 0);
            ps.setInt(16, counts != null ? counts.getOrDefault(100, 0) : 0);
            ps.setInt(17, counts != null ? counts.getOrDefault(50, 0) : 0);
            ps.setInt(18, counts != null ? counts.getOrDefault(20, 0) : 0);
            ps.setInt(19, counts != null ? counts.getOrDefault(10, 0) : 0);
            ps.setInt(20, counts != null ? counts.getOrDefault(5, 0) : 0);
            ps.setInt(21, counts != null ? counts.getOrDefault(2, 0) : 0);
            ps.setInt(22, counts != null ? counts.getOrDefault(1, 0) : 0);

            // Set Gullak due amount
            ps.setDouble(23, d.getGullakDueAmount());


            ps.executeUpdate();

        } catch (Exception e) {
            throw new SQLException("Error saving deposit: " + e.getMessage(), e);
        }
    }
    // Replace this method in src/FinanceManager.java
    public List<Deposit> getAllDeposits() throws SQLException {
        List<Deposit> deposits = new ArrayList<>();
        // Select all columns including new count and due columns
        String sql = "SELECT id, deposit_type, holder_name, description, goal, " +
                     "DATE_FORMAT(creation_date, '%Y-%m-%d %H:%i:%s') as creation_date_str, " +
                     "account_number, principal_amount, monthly_amount, interest_rate, " +
                     "tenure, tenure_unit, start_date, current_total, " + // Note: current_total from DB might be stale
                     "DATE_FORMAT(last_updated, '%Y-%m-%d %H:%i:%s') as last_updated_str, " +
                     "count_500, count_200, count_100, count_50, count_20, count_10, count_5, count_2, count_1, gullak_due_amount " +
                     "FROM deposits ORDER BY creation_date DESC";

        try (Statement stmt = dbHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Format start_date back to dd-MM-yyyy
                java.sql.Date sqlStartDate = rs.getDate("start_date");
                String formattedStartDate = (sqlStartDate == null) ? null :
                                            new java.text.SimpleDateFormat("dd-MM-yyyy").format(sqlStartDate);

                // Read denomination counts from the result set
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

                // Read Gullak due amount
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
                    formattedStartDate, // Use formatted date
                    rs.getDouble("current_total"), // Pass the DB value, constructor will recalculate
                    rs.getString("last_updated_str"),
                    gullakDueAmount, // Pass due amount
                    counts          // Pass counts map
                ));
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
         String sql = "UPDATE deposits SET " +
                      "current_total = ?, last_updated = CURRENT_TIMESTAMP, " +
                      "count_500 = ?, count_200 = ?, count_100 = ?, count_50 = ?, count_20 = ?, " +
                      "count_10 = ?, count_5 = ?, count_2 = ?, count_1 = ?, " +
                      "gullak_due_amount = ? " +
                      "WHERE id = ? AND deposit_type = 'Gullak'";

         try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
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

             ps.executeUpdate();
         }
     }
     // Replace these methods in src/FinanceManager.java

    // --- DEPOSIT RECYCLE BIN METHODS (UPDATED) ---

    private void moveDepositToRecycleBin(int depositId) throws SQLException {
        // Select ALL columns now
        String copySql = "INSERT INTO recycle_bin_deposits SELECT *, NOW() FROM deposits WHERE id = ?";
        String deleteSql = "DELETE FROM deposits WHERE id = ?";

        Connection conn = dbHelper.getConnection();
        conn.setAutoCommit(false);

        try (PreparedStatement copyPs = conn.prepareStatement(copySql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {

            copyPs.setInt(1, depositId);
            deletePs.setInt(1, depositId);

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
        List<Deposit> recycled = new ArrayList<>();
        // Select ALL columns
        String sql = "SELECT id, deposit_type, holder_name, description, goal, " +
                     "DATE_FORMAT(creation_date, '%Y-%m-%d %H:%i:%s') as creation_date_str, " +
                     "account_number, principal_amount, monthly_amount, interest_rate, " +
                     "tenure, tenure_unit, start_date, current_total, " +
                     "DATE_FORMAT(last_updated, '%Y-%m-%d %H:%i:%s') as last_updated_str, " +
                     "count_500, count_200, count_100, count_50, count_20, count_10, count_5, count_2, count_1, gullak_due_amount, " +
                     "DATE_FORMAT(deleted_on, '%Y-%m-%d %H:%i:%s') as deleted_on_str " +
                     "FROM recycle_bin_deposits ORDER BY deleted_on DESC";

        try (Statement stmt = dbHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

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
                    formattedStartDate, rs.getDouble("current_total"), // Pass DB total
                    rs.getString("last_updated_str"),
                    gullakDueAmount, counts // Pass new fields
                ));
                // We can add deleted_on_str if needed later
            }
        }
        return recycled;
    }

    public void restoreDeposit(int depositId) throws SQLException {
        // Select ALL columns except deleted_on
        String copySql = "INSERT INTO deposits SELECT id, deposit_type, holder_name, description, goal, creation_date, account_number, principal_amount, monthly_amount, interest_rate, tenure, tenure_unit, start_date, current_total, last_updated, count_500, count_200, count_100, count_50, count_20, count_10, count_5, count_2, count_1, gullak_due_amount FROM recycle_bin_deposits WHERE id = ?";
        String deleteSql = "DELETE FROM recycle_bin_deposits WHERE id = ?";

        Connection conn = dbHelper.getConnection();
        conn.setAutoCommit(false);

        try (PreparedStatement copyPs = conn.prepareStatement(copySql);
             PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {

            copyPs.setInt(1, depositId);
            deletePs.setInt(1, depositId);

            copyPs.executeUpdate();
            deletePs.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            conn.rollback();
            if (e.getErrorCode() == 1062) {
                System.out.println("Deposit " + depositId + " already exists. Removing from recycle bin.");
                try (PreparedStatement deletePsOnly = conn.prepareStatement(deleteSql)) {
                    deletePsOnly.setInt(1, depositId);
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
        String sql = "DELETE FROM recycle_bin_deposits WHERE id = ?";
        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
            ps.setInt(1, depositId);
            ps.executeUpdate();
        }
    }
    // Add this placeholder method to src/FinanceManager.java
    public void updateDeposit(Deposit d) throws SQLException {
        // Basic update for common fields - More specific updates might be needed
        String sql = "UPDATE deposits SET holder_name = ?, description = ?, goal = ? " +
                     // Potentially add updates for FD/RD fields if needed here
                     "WHERE id = ?";
        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getHolderName());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getGoal());
            ps.setInt(4, d.getId());
            ps.executeUpdate();
        }
        System.out.println("Basic deposit details updated for ID: " + d.getId());
        // Note: This does NOT update amounts, rates, counts, etc. yet.
        // We'll handle Gullak updates specifically. FD/RD updates can be added if needed.
    }
    // Replace this method in src/FinanceManager.java
    public List<Map<String, Object>> getRecycledDepositsForUI() throws SQLException {
        // Returns a List of Maps instead of Deposit objects to easily include deleted_on
        List<Map<String, Object>> recycled = new ArrayList<>();
        String sql = "SELECT id, deposit_type, holder_name, description, goal, " +
                     "DATE_FORMAT(creation_date, '%Y-%m-%d %H:%i:%s') as creation_date_str, " +
                     "account_number, principal_amount, monthly_amount, interest_rate, " +
                     "tenure, tenure_unit, start_date, current_total, " +
                     "DATE_FORMAT(last_updated, '%Y-%m-%d %H:%i:%s') as last_updated_str, " +
                     "count_500, count_200, count_100, count_50, count_20, count_10, count_5, count_2, count_1, gullak_due_amount, " +
                     "DATE_FORMAT(deleted_on, '%Y-%m-%d %H:%i:%s') as deleted_on_str " + // Get deletion date too
                     "FROM recycle_bin_deposits ORDER BY deleted_on DESC";

        try (Statement stmt = dbHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                 Map<String, Object> depositData = new HashMap<>();
                 depositData.put("id", rs.getInt("id"));
                 depositData.put("deposit_type", rs.getString("deposit_type"));
                 depositData.put("holder_name", rs.getString("holder_name"));
                 depositData.put("description", rs.getString("description"));
                 // Add other necessary fields for display...
                 depositData.put("principal_amount", rs.getDouble("principal_amount"));
                 depositData.put("monthly_amount", rs.getDouble("monthly_amount"));
                 depositData.put("current_total", rs.getDouble("current_total")); // Gullak total
                 depositData.put("deleted_on_str", rs.getString("deleted_on_str")); // Get deleted time

                 recycled.add(depositData);
            }
        }
        return recycled;
    }
     public List<Card> getAllCards() throws SQLException {
        System.out.println("DEBUG: Entering getAllCards...");
        List<Card> currentCards = new ArrayList<>();
        String sql = "SELECT id, unique_id, card_name, card_type, card_number, valid_from, valid_through, cvv, front_image_path, back_image_path, credit_limit, current_expenses, amount_to_pay, days_left_to_pay, creation_date FROM cards ORDER BY card_name";
        try (Statement stmt = dbHelper.getConnection().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
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
                 } catch (Exception e) { System.err.println("!!! ERROR creating Card object from ResultSet !!!"); e.printStackTrace(); }
            }
             System.out.println("DEBUG: Fetched " + currentCards.size() + " cards.");
        } catch (SQLException e) { System.err.println("!!! ERROR getting all cards (SQL) !!!"); e.printStackTrace(); throw e; }
        this.cards = currentCards; // Update the class member list
        return currentCards;
    }
    public void updateCard(Card card) throws SQLException {
        System.out.println("DEBUG: Updating card ID " + card.getId());
        String sql = "UPDATE cards SET card_name = ?, card_number = ?, valid_from = ?, valid_through = ?, cvv = ?, " +
                     "front_image_path = ?, back_image_path = ?, credit_limit = ?, current_expenses = ?, amount_to_pay = ?, days_left_to_pay = ? " +
                     "WHERE id = ?";
        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
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
            ps.executeUpdate();
        }
    }

    public void moveCardToRecycleBin(int cardId) throws SQLException {
        System.out.println("DEBUG: Moving card ID " + cardId + " to recycle bin");
        String copySql = "INSERT INTO recycle_bin_cards (original_id, unique_id, card_name, card_type, card_number, valid_from, valid_through, cvv, " +
                         "front_image_path, back_image_path, credit_limit, current_expenses, amount_to_pay, days_left_to_pay, creation_date, deleted_on) " +
                         "SELECT id, unique_id, card_name, card_type, card_number, valid_from, valid_through, cvv, front_image_path, back_image_path, " +
                         "credit_limit, current_expenses, amount_to_pay, days_left_to_pay, creation_date, NOW() FROM cards WHERE id = ?";
        String deleteSql = "DELETE FROM cards WHERE id = ?";

        Connection conn = dbHelper.getConnection();
        boolean oldAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try (PreparedStatement copyPs = conn.prepareStatement(copySql);
             PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
            copyPs.setInt(1, cardId);
            delPs.setInt(1, cardId);
            copyPs.executeUpdate();
            delPs.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAuto);
        }
    }

    public List<Map<String, Object>> getRecycledCardsForUI() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT original_id, card_type, card_name, card_number, valid_through, " +
                     "DATE_FORMAT(deleted_on, '%Y-%m-%d %H:%i:%s') AS deleted_on_str FROM recycle_bin_cards ORDER BY deleted_on DESC";
        try (Statement stmt = dbHelper.getConnection().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
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
        return list;
    }

    public void restoreCard(int originalCardId) throws SQLException {
        System.out.println("DEBUG: Restoring card original ID " + originalCardId);
        String copySql = "INSERT INTO cards (id, unique_id, card_name, card_type, card_number, valid_from, valid_through, cvv, " +
                         "front_image_path, back_image_path, credit_limit, current_expenses, amount_to_pay, days_left_to_pay, creation_date) " +
                         "SELECT original_id, unique_id, card_name, card_type, card_number, valid_from, valid_through, cvv, front_image_path, back_image_path, " +
                         "credit_limit, current_expenses, amount_to_pay, days_left_to_pay, creation_date FROM recycle_bin_cards WHERE original_id = ?";
        String deleteSql = "DELETE FROM recycle_bin_cards WHERE original_id = ?";

        Connection conn = dbHelper.getConnection();
        boolean oldAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try (PreparedStatement copyPs = conn.prepareStatement(copySql);
             PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
            copyPs.setInt(1, originalCardId);
            delPs.setInt(1, originalCardId);
            copyPs.executeUpdate();
            delPs.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            if (e.getErrorCode() == 1062) { // Duplicate key
                // If already exists in main table, just delete from recycle bin
                try (PreparedStatement delPsOnly = conn.prepareStatement(deleteSql)) {
                    delPsOnly.setInt(1, originalCardId);
                    delPsOnly.executeUpdate();
                    conn.commit();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            } else {
                throw e;
            }
        } finally {
            conn.setAutoCommit(oldAuto);
        }
    }

    public void permanentlyDeleteCard(int originalCardId) throws SQLException {
        String sql = "DELETE FROM recycle_bin_cards WHERE original_id = ?";
        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
            ps.setInt(1, originalCardId);
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
        String sql = "INSERT INTO investments (asset_type, holder_name, description, goal, start_date, " +
                     "account_details, ticker_symbol, exchange, quantity, initial_unit_cost, " +
                     "current_unit_price, property_address, tenure_years, interest_rate) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, inv.getAssetType());
            ps.setString(2, inv.getHolderName());
            ps.setString(3, inv.getDescription());
            ps.setString(4, inv.getGoal());
            
            // Handle Date
            if (inv.getStartDate() != null && !inv.getStartDate().isEmpty()) {
                // Assuming start date is passed as dd-MM-yyyy
                java.time.LocalDate localDate = java.time.LocalDate.parse(inv.getStartDate(), java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                ps.setDate(5, java.sql.Date.valueOf(localDate));
            } else {
                ps.setNull(5, Types.DATE);
            }
            
            ps.setString(6, inv.getAccountDetails());
            ps.setString(7, inv.getTickerSymbol());
            ps.setString(8, inv.getExchange());
            ps.setDouble(9, inv.getQuantity());
            ps.setDouble(10, inv.getInitialUnitCost());
            ps.setDouble(11, inv.getCurrentUnitPrice()); // Initial save might have current price = initial
            ps.setString(12, inv.getPropertyAddress());
            ps.setInt(13, inv.getTenureYears());
            ps.setDouble(14, inv.getInterestRate());
            
            ps.executeUpdate();
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    inv.setId(generatedKeys.getInt(1)); // Set the new ID back on the object
                }
            }
        } catch (java.time.format.DateTimeParseException e) {
            System.err.println("!!! ERROR parsing date in saveInvestment !!!");
            e.printStackTrace();
            throw new SQLException("Invalid date format. Expected dd-MM-yyyy.", e);
        } catch (SQLException e) { 
            System.err.println("!!! ERROR saving investment (SQL) !!!"); 
            e.printStackTrace(); throw e; 
        } catch (Exception e) { 
            System.err.println("!!! UNEXPECTED ERROR saving investment !!!"); 
            e.printStackTrace(); throw new SQLException("Unexpected error saving investment.", e); 
        }
    }
    
    /**
     * Updates an existing investment record in the database.
     * Used for editing details or updating the current price.
     */
    public void updateInvestment(Investment inv) throws SQLException {
        String sql = "UPDATE investments SET " +
                     "asset_type = ?, holder_name = ?, description = ?, goal = ?, start_date = ?, " +
                     "account_details = ?, ticker_symbol = ?, exchange = ?, quantity = ?, " +
                     "initial_unit_cost = ?, current_unit_price = ?, property_address = ?, " +
                     "tenure_years = ?, interest_rate = ? " +
                     "WHERE id = ?";
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
            ps.setInt(15, inv.getId()); // WHERE clause
            
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
        List<Investment> investments = new ArrayList<>();
        String sql = "SELECT * FROM investments ORDER BY holder_name, description";
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd-MM-yyyy");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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
                } catch (Exception e) { System.err.println("!!! ERROR creating Investment object from ResultSet !!!"); e.printStackTrace(); }
            }
             System.out.println("DEBUG: Fetched " + investments.size() + " investments.");
        } catch (SQLException e) { System.err.println("!!! ERROR executing SQL in getAllInvestments !!!"); e.printStackTrace(); throw e; }
        return investments;
    }

    /**
     * Moves an investment to the recycle bin table.
     */
    public void moveInvestmentToRecycleBin(int investmentId) throws SQLException {
        String copySql = "INSERT INTO recycle_bin_investments SELECT *, NOW() FROM investments WHERE id = ?";
        String deleteSql = "DELETE FROM investments WHERE id = ?";
        
        connection.setAutoCommit(false);
        try (PreparedStatement copyPs = connection.prepareStatement(copySql);
             PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {
            
            copyPs.setInt(1, investmentId);
            deletePs.setInt(1, investmentId);
            
            int copied = copyPs.executeUpdate();
            if (copied == 0) throw new SQLException("Failed to copy investment to recycle bin, ID not found: " + investmentId);
            
            deletePs.executeUpdate();
            
            connection.commit();
            System.out.println("DEBUG: Successfully moved investment ID " + investmentId + " to recycle bin.");
        } catch (SQLException e) {
            connection.rollback();
            System.err.println("!!! ERROR moving investment to recycle bin !!!"); e.printStackTrace();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Fetches recycled investments for the UI dialog.
     */
    public List<Map<String, Object>> getRecycledInvestmentsForUI() throws SQLException {
        List<Map<String, Object>> recycled = new ArrayList<>();
        String sql = "SELECT id, asset_type, holder_name, description, " +
                     "DATE_FORMAT(deleted_on, '%Y-%m-%d %H:%i:%s') as deleted_on_str " +
                     "FROM recycle_bin_investments ORDER BY deleted_on DESC";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                 Map<String, Object> data = new HashMap<>();
                 data.put("id", rs.getInt("id"));
                 data.put("asset_type", rs.getString("asset_type"));
                 data.put("holder_name", rs.getString("holder_name"));
                 data.put("description", rs.getString("description"));
                 data.put("deleted_on_str", rs.getString("deleted_on_str"));
                 recycled.add(data);
            }
        } catch (SQLException e) { System.err.println("!!! ERROR getting recycled investments !!!"); e.printStackTrace(); throw e; }
        return recycled;
    }

    /**
     * Restores an investment from the recycle bin.
     */
    public void restoreInvestment(int investmentId) throws SQLException {
        String copySql = "INSERT INTO investments (id, asset_type, holder_name, description, goal, start_date, account_details, ticker_symbol, exchange, quantity, initial_unit_cost, current_unit_price, property_address, tenure_years, interest_rate, last_updated) " +
                       "SELECT id, asset_type, holder_name, description, goal, start_date, account_details, ticker_symbol, exchange, quantity, initial_unit_cost, current_unit_price, property_address, tenure_years, interest_rate, last_updated " +
                       "FROM recycle_bin_investments WHERE id = ?";
        String deleteSql = "DELETE FROM recycle_bin_investments WHERE id = ?";
        
        connection.setAutoCommit(false);
        try (PreparedStatement copyPs = connection.prepareStatement(copySql);
             PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {
            
            copyPs.setInt(1, investmentId);
            deletePs.setInt(1, investmentId);
            
            copyPs.executeUpdate();
            deletePs.executeUpdate();
            
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            if (e.getErrorCode() == 1062) { // Handle duplicate key (already restored)
                 System.out.println("Investment " + investmentId + " already exists. Removing from recycle bin.");
                 try (PreparedStatement deletePsOnly = connection.prepareStatement(deleteSql)) {
                     deletePsOnly.setInt(1, investmentId); deletePsOnly.executeUpdate(); connection.commit();
                 } catch (SQLException ex) { connection.rollback(); throw ex; }
            } else { throw e; }
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Permanently deletes an investment from the recycle bin.
     */
    public void permanentlyDeleteInvestment(int investmentId) throws SQLException {
        String sql = "DELETE FROM recycle_bin_investments WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, investmentId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Updates only the current unit price of the given investment ID.
     * Useful for quick price refreshes from the UI.
     */
    public void updateInvestmentCurrentPrice(int investmentId, double newPrice) throws SQLException {
        String sql = "UPDATE investments SET current_unit_price = ?, last_updated = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, newPrice);
            ps.setInt(2, investmentId);
            ps.executeUpdate();
        }
    }
    // ==================================================================
    // ===         TABLE CREATION (If needed on startup)              ===
    // ==================================================================
     private void createTables() throws SQLException {
         try (Statement stmt = connection.createStatement()) {
             System.out.println("Checking/Creating database tables...");
             
             // Inside private void createTables()
stmt.execute("CREATE TABLE IF NOT EXISTS tax_profiles (" +
    "id INT AUTO_INCREMENT PRIMARY KEY," +
    "profile_name VARCHAR(100) NOT NULL," +
    "profile_type VARCHAR(50) NOT NULL," +
    "financial_year VARCHAR(10) NOT NULL," +
    "gross_income DECIMAL(15, 2) DEFAULT 0.00," +
    "total_deductions DECIMAL(15, 2) DEFAULT 0.00," +
    "taxable_income DECIMAL(15, 2) DEFAULT 0.00," +
    "tax_paid DECIMAL(15, 2) DEFAULT 0.00," +
    "notes TEXT," + 
    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
")");
             
             
             // --- KEEP EXISTING TABLES ---
             stmt.execute("CREATE TABLE IF NOT EXISTS transactions (id INT AUTO_INCREMENT PRIMARY KEY, date DATE NOT NULL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, day VARCHAR(20), payment_method VARCHAR(20), category VARCHAR(100), type VARCHAR(20) NOT NULL, payee VARCHAR(100), description TEXT, amount DECIMAL(10, 2) NOT NULL)");
             stmt.execute("CREATE TABLE IF NOT EXISTS recycle_bin_transactions (id INT PRIMARY KEY, timestamp TIMESTAMP, date DATE, day VARCHAR(20), payment_method VARCHAR(20), category VARCHAR(100), type VARCHAR(20), payee VARCHAR(100), description TEXT, amount DECIMAL(10, 2), deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
             stmt.execute("CREATE TABLE IF NOT EXISTS bank_accounts (id INT AUTO_INCREMENT PRIMARY KEY, account_number VARCHAR(50) NOT NULL, holder_name VARCHAR(100), bank_name VARCHAR(100), ifsc_code VARCHAR(20), balance DECIMAL(15, 2) DEFAULT 0.00, account_type VARCHAR(20) NOT NULL, interest_rate DECIMAL(5, 2), annual_expense DECIMAL(15, 2), account_subtype VARCHAR(20), company_name VARCHAR(100), business_name VARCHAR(100))");
             stmt.execute("CREATE TABLE IF NOT EXISTS deposits (id INT AUTO_INCREMENT PRIMARY KEY, deposit_type VARCHAR(20) NOT NULL, holder_name VARCHAR(100), description TEXT, goal VARCHAR(255), creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, account_number VARCHAR(50), principal_amount DECIMAL(15, 2), monthly_amount DECIMAL(15, 2), interest_rate DECIMAL(5, 2), tenure INT, tenure_unit VARCHAR(10), start_date DATE, current_total DECIMAL(15, 2), last_updated TIMESTAMP NULL, count_500 INT DEFAULT 0, count_200 INT DEFAULT 0, count_100 INT DEFAULT 0, count_50 INT DEFAULT 0, count_20 INT DEFAULT 0, count_10 INT DEFAULT 0, count_5 INT DEFAULT 0, count_2 INT DEFAULT 0, count_1 INT DEFAULT 0, gullak_due_amount DECIMAL(15, 2) DEFAULT 0.00)");
             stmt.execute("CREATE TABLE IF NOT EXISTS recycle_bin_deposits (id INT NOT NULL PRIMARY KEY, deposit_type VARCHAR(20) NOT NULL, holder_name VARCHAR(100), description TEXT, goal VARCHAR(255), creation_date TIMESTAMP, account_number VARCHAR(50), principal_amount DECIMAL(15, 2), monthly_amount DECIMAL(15, 2), interest_rate DECIMAL(5, 2), tenure INT, tenure_unit VARCHAR(10), start_date DATE, current_total DECIMAL(15, 2), last_updated TIMESTAMP NULL, count_500 INT DEFAULT 0, count_200 INT DEFAULT 0, count_100 INT DEFAULT 0, count_50 INT DEFAULT 0, count_20 INT DEFAULT 0, count_10 INT DEFAULT 0, count_5 INT DEFAULT 0, count_2 INT DEFAULT 0, count_1 INT DEFAULT 0, gullak_due_amount DECIMAL(15, 2) DEFAULT 0.00, deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
             stmt.execute("CREATE TABLE IF NOT EXISTS cards (id INT AUTO_INCREMENT PRIMARY KEY, unique_id VARCHAR(36) UNIQUE NOT NULL, card_name VARCHAR(100) NOT NULL, card_type VARCHAR(20) NOT NULL, card_number VARCHAR(16) NOT NULL, valid_from VARCHAR(5), valid_through VARCHAR(5) NOT NULL, cvv VARCHAR(4) NOT NULL, front_image_path VARCHAR(255), back_image_path VARCHAR(255), credit_limit DECIMAL(15, 2), current_expenses DECIMAL(15, 2), amount_to_pay DECIMAL(15, 2), days_left_to_pay INT, creation_date DATE)");
             stmt.execute("CREATE TABLE IF NOT EXISTS recycle_bin_cards (recycle_id INT AUTO_INCREMENT PRIMARY KEY, original_id INT NOT NULL, unique_id VARCHAR(36) NOT NULL, card_name VARCHAR(100) NOT NULL, card_type VARCHAR(20) NOT NULL, card_number VARCHAR(16) NOT NULL, valid_from VARCHAR(5), valid_through VARCHAR(5) NOT NULL, cvv VARCHAR(4) NOT NULL, front_image_path VARCHAR(255), back_image_path VARCHAR(255), credit_limit DECIMAL(15, 2), current_expenses DECIMAL(15, 2), amount_to_pay DECIMAL(15, 2), days_left_to_pay INT, creation_date DATE, deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

             // --- REMOVE OLD INVESTMENT TABLES ---
             stmt.execute("DROP TABLE IF EXISTS gold_silver_investments;");
             stmt.execute("DROP TABLE IF EXISTS mutual_funds;");
             
             // --- ADD NEW INVESTMENT TABLES ---
             stmt.execute("CREATE TABLE IF NOT EXISTS investments (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
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
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
             ")");
             
             stmt.execute("CREATE TABLE IF NOT EXISTS recycle_bin_investments (" +
                "id INT NOT NULL PRIMARY KEY," +
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
                "deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
             ")");

             System.out.println("Tables checked/created/updated.");
         }
     }
     // ==================================================================
    // ===         NEW TAX PROFILE METHODS                          ===
    // ==================================================================

    /**
     * Saves a new tax profile to the database.
     */
    public void saveTaxProfile(TaxProfile tp) throws SQLException {
        String sql = "INSERT INTO tax_profiles (profile_name, profile_type, financial_year, " +
                     "gross_income, total_deductions, taxable_income, tax_paid, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tp.getProfileName());
            ps.setString(2, tp.getProfileType());
            ps.setString(3, tp.getFinancialYear());
            ps.setDouble(4, tp.getGrossIncome());
            ps.setDouble(5, tp.getTotalDeductions());
            ps.setDouble(6, tp.getTaxableIncome()); // Store the calculated value
            ps.setDouble(7, tp.getTaxPaid());
            ps.setString(8, tp.getNotes());
            
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
        List<TaxProfile> profiles = new ArrayList<>();
        String sql = "SELECT * FROM tax_profiles ORDER BY financial_year DESC, profile_name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
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
        
        String sql = "UPDATE tax_profiles SET " +
                     "profile_name = ?, profile_type = ?, financial_year = ?, " +
                     "gross_income = ?, total_deductions = ?, taxable_income = ?, " +
                     "tax_paid = ?, notes = ? " +
                     "WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tp.getProfileName());
            ps.setString(2, tp.getProfileType());
            ps.setString(3, tp.getFinancialYear());
            ps.setDouble(4, tp.getGrossIncome());
            ps.setDouble(5, tp.getTotalDeductions());
            ps.setDouble(6, tp.getTaxableIncome()); // Store updated calculation
            ps.setDouble(7, tp.getTaxPaid());
            ps.setString(8, tp.getNotes());
            ps.setInt(9, tp.getId()); // WHERE clause
            
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
    public void deleteTaxProfile(int profileId) throws SQLException {
        String sql = "DELETE FROM tax_profiles WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.executeUpdate();
        } catch (SQLException e) { 
            System.err.println("!!! ERROR deleting tax profile (SQL) !!!"); 
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
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT id, date, DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i:%s') as timestamp_str, " +
                     "day, category, type, payment_method, payee, amount, description " +
                     "FROM transactions ";

        if (year != null && !year.equals("All Years")) {
            sql += "WHERE YEAR(date) = ? ";
        }
        sql += "ORDER BY date ASC, id ASC"; // Keep ascending order

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (year != null && !year.equals("All Years")) {
                ps.setInt(1, Integer.parseInt(year));
            }
            ResultSet rs = ps.executeQuery();
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
            rs.close();
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
        String sql = "INSERT INTO loans (lender_name, loan_type, principal_amount, interest_rate, " +
                     "tenure_months, start_date, status, notes, " +
                     "emi_amount, total_interest, total_payment) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, loan.getLenderName());
            ps.setString(2, loan.getLoanType());
            ps.setDouble(3, loan.getPrincipalAmount());
            ps.setDouble(4, loan.getInterestRate());
            ps.setInt(5, loan.getTenureMonths());

            // Handle Date
            if (loan.getStartDate() != null && !loan.getStartDate().isEmpty()) {
                 try {
                    LocalDate localDate = LocalDate.parse(loan.getStartDate(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    ps.setDate(6, java.sql.Date.valueOf(localDate));
                 } catch (Exception e) {
                     System.err.println("Invalid loan start date format, setting to null: " + loan.getStartDate());
                     ps.setNull(6, Types.DATE);
                 }
            } else {
                ps.setNull(6, Types.DATE);
            }
            
            ps.setString(7, "Active"); // Default status
            ps.setString(8, loan.getNotes());
            
            // Add the calculated fields
            ps.setDouble(9, loan.getEmiAmount());
            ps.setDouble(10, loan.getTotalInterest());
            ps.setDouble(11, loan.getTotalPayment());
            
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
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans ORDER BY status, lender_name";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
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
        String sql = "UPDATE loans SET status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, loanId);
            ps.executeUpdate();
        } catch (SQLException e) { 
            System.err.println("!!! ERROR updating loan status (SQL) !!!"); 
            e.printStackTrace(); 
            throw e; 
        }
    }

    /**
     * Moves a loan to the recycle bin table.
     */
    public void moveLoanToRecycleBin(int loanId) throws SQLException {
        String copySql = "INSERT INTO recycle_bin_loans SELECT *, NOW() FROM loans WHERE id = ?";
        String deleteSql = "DELETE FROM loans WHERE id = ?";
        
        connection.setAutoCommit(false);
        try (PreparedStatement copyPs = connection.prepareStatement(copySql);
             PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {
            
            copyPs.setInt(1, loanId);
            deletePs.setInt(1, loanId);
            
            int copied = copyPs.executeUpdate();
            if (copied == 0) throw new SQLException("Failed to copy loan to recycle bin, ID not found: " + loanId);
            
            deletePs.executeUpdate();
            connection.commit();
            
        } catch (SQLException e) {
            connection.rollback();
            System.err.println("!!! ERROR moving loan to recycle bin !!!"); 
            e.printStackTrace();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    /**
     * Fetches recycled loans for the UI dialog.
     */
    public List<Map<String, Object>> getRecycledLoansForUI() throws SQLException {
        List<Map<String, Object>> recycled = new ArrayList<>();
        String sql = "SELECT id, loan_type, lender_name, principal_amount, " +
                     "DATE_FORMAT(deleted_on, '%Y-%m-%d %H:%i:%s') as deleted_on_str " +
                     "FROM recycle_bin_loans ORDER BY deleted_on DESC";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                 Map<String, Object> data = new HashMap<>();
                 data.put("id", rs.getInt("id"));
                 data.put("loan_type", rs.getString("loan_type"));
                 data.put("lender_name", rs.getString("lender_name"));
                 data.put("principal_amount", rs.getDouble("principal_amount"));
                 data.put("deleted_on_str", rs.getString("deleted_on_str"));
                 recycled.add(data);
            }
        } catch (SQLException e) { System.err.println("!!! ERROR getting recycled loans !!!"); e.printStackTrace(); throw e; }
        return recycled;
    }

    /**
     * Restores a loan from the recycle bin.
     */
    public void restoreLoan(int loanId) throws SQLException {
        String copySql = "INSERT INTO loans (id, lender_name, loan_type, principal_amount, interest_rate, tenure_months, start_date, emi_amount, total_interest, total_payment, status, notes) " +
                       "SELECT id, lender_name, loan_type, principal_amount, interest_rate, tenure_months, start_date, emi_amount, total_interest, total_payment, status, notes " +
                       "FROM recycle_bin_loans WHERE id = ?";
        String deleteSql = "DELETE FROM recycle_bin_loans WHERE id = ?";
        
        connection.setAutoCommit(false);
        try (PreparedStatement copyPs = connection.prepareStatement(copySql);
             PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {
            
            copyPs.setInt(1, loanId);
            deletePs.setInt(1, loanId);
            
            copyPs.executeUpdate();
            deletePs.executeUpdate();
            
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            if (e.getErrorCode() == 1062) { // Handle duplicate key (already restored)
                 System.out.println("Loan " + loanId + " already exists. Removing from recycle bin.");
                 try (PreparedStatement deletePsOnly = connection.prepareStatement(deleteSql)) {
                     deletePsOnly.setInt(1, loanId);
                     deletePsOnly.executeUpdate();
                     connection.commit();
                 } catch (SQLException ex) {
                     connection.rollback();
                     throw ex;
                 }
            } else { throw e; }
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Permanently deletes a loan from the recycle bin.
     */
    public void permanentlyDeleteLoan(int loanId) throws SQLException {
        String sql = "DELETE FROM recycle_bin_loans WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, loanId);
            ps.executeUpdate();
        }
    }
}