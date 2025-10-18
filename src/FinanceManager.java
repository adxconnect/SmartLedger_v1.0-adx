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
        for (RecurringDeposit rd : recurringDeposits) pw.println(rd.toCSV());
    } catch (IOException e) { e.printStackTrace(); }
}

public void loadRecurringDeposits(String filename) {
    recurringDeposits.clear();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null)
            recurringDeposits.add(RecurringDeposit.fromCSV(line));
    } catch (IOException e) { System.out.println("No Recurring Deposit file found."); }
}

public void saveFixedDeposits(String filename) {
    try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
        for (FixedDeposit fd : fixedDeposits)
            pw.println(fd.toCSV());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

public void loadFixedDeposits(String filename) {
    fixedDeposits.clear();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null)
            fixedDeposits.add(FixedDeposit.fromCSV(line));
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



}
   
