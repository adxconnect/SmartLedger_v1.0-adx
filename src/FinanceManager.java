package src;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;
import src.db.DBHelper;
import java.sql.*;
import java.util.*;

public class FinanceManager {
    private List<Transaction> transactions = new ArrayList<>();

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
    // Edit transaction at index (if valid)
public boolean editTransaction(int index, Transaction newTransaction) {
    if (index >= 0 && index < transactions.size()) {
        transactions.set(index, newTransaction);
        return true; // success
    }
    return false; // invalid index
}

// Delete transaction at index (if valid)
public boolean deleteTransaction(int index) {
    if (index >= 0 && index < transactions.size()) {
        transactions.remove(index);
        return true;
    }
    return false;
}

// Optional: View transactions with index numbers
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

    // Monthly summary
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
public void saveSavingsAccount(SavingsAccount sa) throws SQLException {
    String sql = "INSERT INTO savings_accounts (account_number, account_type, balance, holder_name, bank_name, ifsc_code) VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
        ps.setString(1, sa.getAccountNumber());
        ps.setString(2, sa.getAccountType());
        ps.setDouble(3, sa.getBalance());
        ps.setString(4, sa.getHolderName());
        ps.setString(5, sa.getBankName());
        ps.setString(6, sa.getIfscCode());
        ps.executeUpdate();
    }
}
public List<SavingsAccount> getAllSavingsAccounts() throws SQLException {
    List<SavingsAccount> accounts = new ArrayList<>();
    String sql = "SELECT * FROM savings_accounts";
    try (Statement stmt = dbHelper.getConnection().createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            accounts.add(new SavingsAccount(
                rs.getInt("id"),
                rs.getString("account_number"),
                rs.getString("account_type"),
                rs.getDouble("balance"),
                rs.getString("holder_name"),
                rs.getString("bank_name"),
                rs.getString("ifsc_code")
            ));
        }
    }
    return accounts;
}



// Category-wise summary
public void showCategorySummary() {
    Map<String, Double> categorySummary = new HashMap<>();
    for (Transaction t : transactions) {
        if (t.getType().equalsIgnoreCase("Expense")) {
            String category = "Uncategorized";
            try {
                java.lang.reflect.Method m = t.getClass().getMethod("getCategory");
                Object val = m.invoke(t);
                if (val != null) category = val.toString();
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // If Transaction doesn't have getCategory(), fall back to Uncategorized
            }
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
private double monthlyBudget = 0;

// Set budget for current month
public void setMonthlyBudget(double budget) {
    this.monthlyBudget = budget;
    System.out.println("Monthly budget set to ₹" + budget);
}

// Get total expenses
public double getTotalExpenses() {
    double total = 0;
    for (Transaction t : transactions) {
        if (t.getType().equalsIgnoreCase("Expense"))
            total += t.getAmount();
    }
    return total;
}

// Check budget status
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
private List<SavingsAccount> savingsAccounts = new ArrayList<>();
private List<FixedDeposit> fixedDeposits = new ArrayList<>();
private List<RecurringDeposit> recurringDeposits = new ArrayList<>();
private List<MutualFund> mutualFunds = new ArrayList<>();
private List<CreditCard> creditCards = new ArrayList<>();
private List<GoldSilverInvestment> goldSilverInvestments = new ArrayList<>();

public void addSavingsAccount(SavingsAccount sa) { savingsAccounts.add(sa); }
public void viewSavingsAccounts() { savingsAccounts.forEach(System.out::println); }

public void addFixedDeposit(FixedDeposit fd) { fixedDeposits.add(fd); }
public void viewFixedDeposits() { fixedDeposits.forEach(System.out::println); }

public void addRecurringDeposit(RecurringDeposit rd) { recurringDeposits.add(rd); }
public void viewRecurringDeposits() { recurringDeposits.forEach(System.out::println); }

public void addMutualFund(MutualFund mf) { mutualFunds.add(mf); }
public void viewMutualFunds() { mutualFunds.forEach(System.out::println); }

public void addGoldSilverInvestment(GoldSilverInvestment gs) { goldSilverInvestments.add(gs); }
public void viewGoldSilverInvestments() { goldSilverInvestments.forEach(System.out::println); }

public void addCreditCard(CreditCard cc) {
    creditCards.add(cc);
}

public void viewCreditCards() {
    if (creditCards.isEmpty()) {
        System.out.println("No credit cards added yet.");
        return;
    }
    creditCards.forEach(System.out::println);
}

public void makeCreditCardPayment(int index, double amount) {
    if (index >= 0 && index < creditCards.size()) {
        Object cc = creditCards.get(index);
        String[] candidateNames = { "makePayment", "pay", "addPayment", "makeCreditPayment", "makePaymentInINR" };
        boolean invoked = false;
        for (String name : candidateNames) {
            try {
                java.lang.reflect.Method m = cc.getClass().getMethod(name, double.class);
                m.invoke(cc, amount);
                invoked = true;
                break;
            } catch (NoSuchMethodException e) {
                // try with boxed Double parameter
                try {
                    java.lang.reflect.Method m = cc.getClass().getMethod(name, Double.class);
                    m.invoke(cc, amount);
                    invoked = true;
                    break;
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                    // continue to next candidate
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                System.out.println("Error invoking payment method: " + e.getMessage());
                invoked = true;
                break;
            }
        }
        if (!invoked) {
            System.out.println("Payment method not found on selected CreditCard object.");
        }
    } else {
        System.out.println("Invalid credit card selection.");
    }
}

public void addCreditCardExpense(int index, double amount) {
    if (index >= 0 && index < creditCards.size()) {
        Object cc = creditCards.get(index);
        String[] candidateNames = { "addExpense", "charge", "addCharge", "recordExpense", "addTransaction", "addPurchase" };
        boolean invoked = false;
        for (String name : candidateNames) {
            try {
                java.lang.reflect.Method m = cc.getClass().getMethod(name, double.class);
                m.invoke(cc, amount);
                invoked = true;
                break;
            } catch (NoSuchMethodException e) {
                // try with boxed Double parameter
                try {
                    java.lang.reflect.Method m = cc.getClass().getMethod(name, Double.class);
                    m.invoke(cc, amount);
                    invoked = true;
                    break;
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                    // continue to next candidate
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                System.out.println("Error invoking method to add expense: " + e.getMessage());
                invoked = true;
                break;
            }
        }
        if (!invoked) {
            System.out.println("Method to add expense not found on selected CreditCard object.");
        }
    } else {
        System.out.println("Invalid credit card selection.");
    }
}

public void saveRecurringDeposits(String filename) {
    try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
        for (RecurringDeposit rd : recurringDeposits) pw.println(rd.toString());
    } catch (IOException e) { e.printStackTrace(); }
}

public void loadRecurringDeposits(String filename) {
    recurringDeposits.clear();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null) {
            // Parse CSV line manually since fromCSV method doesn't exist
            String[] parts = line.split(",");
            if (parts.length >= 6) {
                recurringDeposits.add(new RecurringDeposit(
                    0, // id - use 0 as default
                    parts[0], // account number
                    Double.parseDouble(parts[1]), // monthly amount
                    Double.parseDouble(parts[2]), // rate
                    Integer.parseInt(parts[3]), // tenure months
                    parts[4], // start date
                    parts[5] // holder name
                ));
            }
        }
    } catch (IOException e) { System.out.println("No Recurring Deposit file found."); }
}

public void saveFixedDeposits(String filename) {
    try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
        for (FixedDeposit fd : fixedDeposits)
            pw.println(fd.toString());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

public void loadFixedDeposits(String filename) {
    fixedDeposits.clear();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null) {
            // Parse CSV line manually since fromCSV method doesn't exist
            String[] parts = line.split(",");
            if (parts.length >= 6) {
                fixedDeposits.add(new FixedDeposit(
                    0, // id - use 0 as default
                    parts[0], // account number
                    Double.parseDouble(parts[1]), // principal
                    Double.parseDouble(parts[2]), // rate
                    Integer.parseInt(parts[3]), // tenure months
                    parts[4], // start date
                    parts[5] // holder name
                ));
            }
        }
    } catch (IOException e) {
        System.out.println("No Fixed Deposit file found.");
    }
}
public void saveCreditCards(String filename) {
    try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
        for (CreditCard cc : creditCards)
            pw.println(cc.toCSV());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

public void loadCreditCards(String filename) {
    creditCards.clear();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null)
            creditCards.add(CreditCard.fromCSV(line));
    } catch (IOException e) {
        System.out.println("No Credit Card file found.");
    }
}public void saveMutualFunds(String filename) {
    try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
        for (MutualFund mf : mutualFunds)
            pw.println(mf.toCSV());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

public void loadMutualFunds(String filename) {
    mutualFunds.clear();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null)
            mutualFunds.add(MutualFund.fromCSV(line));
    } catch (IOException e) {
        System.out.println("No Mutual Fund file found.");
    }
}

public void saveGoldSilverInvestments(String filename) {
    try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
        for (GoldSilverInvestment gs : goldSilverInvestments)
            pw.println(gs.toCSV());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

public void loadGoldSilverInvestments(String filename) {
    goldSilverInvestments.clear();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null)
            goldSilverInvestments.add(GoldSilverInvestment.fromCSV(line));
    } catch (IOException e) {
        System.out.println("No Gold/Silver investment file found.");
    }
}
 private DBHelper dbHelper;
    public FinanceManager() throws SQLException { dbHelper = new DBHelper(); }

    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> txs = new ArrayList<>();
        String sql = "SELECT * FROM transactions";
        try (Statement stmt = dbHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                txs.add(new Transaction(
                    rs.getString("date"),
                    rs.getString("category"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("description")
                ));
            }
        }
        return txs;
    }

    public void saveTransaction(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (date, category, type, amount, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
            ps.setString(1, t.getDate());
            ps.setString(2, t.getCategory());
            ps.setString(3, t.getType());
            ps.setDouble(4, t.getAmount());
            ps.setString(5, t.getDescription());
            ps.executeUpdate();
        }
    }
    // In FinanceManager.java
public void saveFixedDeposit(FixedDeposit fd) throws SQLException {
    String sql = "INSERT INTO fixed_deposits (account_number, principal, rate, tenure_months, start_date, holder_name) VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
        ps.setString(1, fd.getAccountNumber());
        ps.setDouble(2, fd.getPrincipal());
        ps.setDouble(3, fd.getRate());
        ps.setInt(4, fd.getTenureMonths());
        ps.setString(5, fd.getStartDate());
        ps.setString(6, fd.getHolderName());
        ps.executeUpdate();
    }
}

public List<FixedDeposit> getAllFixedDeposits() throws SQLException {
    List<FixedDeposit> deposits = new ArrayList<>();
    String sql = "SELECT * FROM fixed_deposits";
    try (Statement stmt = dbHelper.getConnection().createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            deposits.add(new FixedDeposit(
                rs.getInt("id"),
                rs.getString("account_number"),
                rs.getDouble("principal"),
                rs.getDouble("rate"),
                rs.getInt("tenure_months"),
                rs.getString("start_date"),
                rs.getString("holder_name")
            ));
        }
    }
    return deposits;
}
public void saveRecurringDeposit(RecurringDeposit rd) throws SQLException {
    String sql = "INSERT INTO recurring_deposits (account_number, monthly_amount, rate, tenure_months, start_date, holder_name) VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
        ps.setString(1, rd.getAccountNumber());
        ps.setDouble(2, rd.getMonthlyAmount());
        ps.setDouble(3, rd.getRate());
        ps.setInt(4, rd.getTenureMonths());
        ps.setString(5, rd.getStartDate());
        ps.setString(6, rd.getHolderName());
        ps.executeUpdate();
    }
}

public List<RecurringDeposit> getAllRecurringDeposits() throws SQLException {
    List<RecurringDeposit> deposits = new ArrayList<>();
    String sql = "SELECT * FROM recurring_deposits";
    try (Statement stmt = dbHelper.getConnection().createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            deposits.add(new RecurringDeposit(
                rs.getInt("id"),
                rs.getString("account_number"),
                rs.getDouble("monthly_amount"),
                rs.getDouble("rate"),
                rs.getInt("tenure_months"),
                rs.getString("start_date"),
                rs.getString("holder_name")
            ));
        }
    }
    return deposits;
}
// Save a credit card in MySQL
public void saveCreditCard(CreditCard cc) throws SQLException {
    String sql = "INSERT INTO credit_cards (card_name, credit_limit, expenses, amount_to_pay, days_left) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = dbHelper.getConnection().prepareStatement(sql)) {
        ps.setString(1, cc.getCardName());
        ps.setDouble(2, cc.getLimit());
        ps.setDouble(3, cc.getExpenses());
        ps.setDouble(4, cc.getAmountToPay());
        ps.setInt(5, cc.getDaysLeftToPay());
        ps.executeUpdate();
    }
}

// Get all credit cards from MySQL
public List<CreditCard> getAllCreditCards() throws SQLException {
    List<CreditCard> cards = new ArrayList<>();
    String sql = "SELECT * FROM credit_cards";
    try (Statement stmt = dbHelper.getConnection().createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            cards.add(new CreditCard(
                rs.getString("card_name"),
                rs.getDouble("credit_limit"),
                rs.getDouble("expenses"),
                rs.getDouble("amount_to_pay"),
                rs.getInt("days_left")
            ));
        }
    }
    return cards;
}
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


}
   
