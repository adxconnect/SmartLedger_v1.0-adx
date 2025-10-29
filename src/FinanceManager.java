package src;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import src.db.DBHelper;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
// Same-package types don't require imports

public class FinanceManager {
    private List<Transaction> transactions = new ArrayList<>();
    private DBHelper dbHelper;
    

    public FinanceManager() throws SQLException { 
        dbHelper = new DBHelper();
        this.cards = new ArrayList<>(); 
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
    private List<MutualFund> mutualFunds = new ArrayList<>();
    private List<Card> cards = new ArrayList<>();
    private List<GoldSilverInvestment> goldSilverInvestments = new ArrayList<>();
    
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


    // --- Gold/Silver (Database) Methods ---
    public void saveGoldSilverInvestment(GoldSilverInvestment gs) throws SQLException {
        String sql = "INSERT INTO gold_silver_investments (metal_type, weight, price_per_gram) VALUES (?, ?, ?)";
        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
            ps.setString(1, gs.getMetalType());
            ps.setDouble(2, gs.getWeight());
            ps.setDouble(3, gs.getPricePerGram());
            ps.executeUpdate();
        }
    }

    public List<GoldSilverInvestment> getAllGoldSilverInvestments() throws SQLException {
        List<GoldSilverInvestment> list = new ArrayList<>();
        String sql = "SELECT * FROM gold_silver_investments";
        try (Statement stmt = dbHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new GoldSilverInvestment(
                    rs.getString("metal_type"),
                    rs.getDouble("weight"),
                    rs.getDouble("price_per_gram")
                ));
            }
        }
        return list;
    }
    public void addGoldSilverInvestment(GoldSilverInvestment gs) { goldSilverInvestments.add(gs); }
    public void viewGoldSilverInvestments() { goldSilverInvestments.forEach(System.out::println); }


    // --- Mutual Fund (Database) Methods ---
    public void saveMutualFund(MutualFund mf) throws SQLException {
        String sql = "INSERT INTO mutual_funds (amount, annual_rate, years) VALUES (?, ?, ?)";
        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, mf.getAmount());
            ps.setDouble(2, mf.getAnnualRate());
            ps.setInt(3, mf.getYears());
            ps.executeUpdate();
        }
    }

    public List<MutualFund> getAllMutualFunds() throws SQLException {
        List<MutualFund> list = new ArrayList<>();
        String sql = "SELECT * FROM mutual_funds";
        try (Statement stmt = dbHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new MutualFund(
                    rs.getDouble("amount"),
                    rs.getDouble("annual_rate"),
                    rs.getInt("years")
                ));
            }
        }
        return list;
    }
    public void addMutualFund(MutualFund mf) { mutualFunds.add(mf); }
    public void viewMutualFunds() { mutualFunds.forEach(System.out::println); }


    // --- File-Saving Methods (for console app) ---
    public void saveRecurringDeposits(String filename) { /* ... */ }
    public void loadRecurringDeposits(String filename) { /* ... */ }
    public void saveFixedDeposits(String filename) { /* ... */ }
    public void loadFixedDeposits(String filename) { /* ... */ }

    public void saveMutualFunds(String filename) { /* ... */ }
    public void loadMutualFunds(String filename) { /* ... */ }
    public void saveGoldSilverInvestments(String filename) { /* ... */ }
    public void loadGoldSilverInvestments(String filename) { /* ... */ }

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
        
        sql += "ORDER BY date DESC, id DESC";

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


}