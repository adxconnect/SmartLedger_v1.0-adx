import java.util.ArrayList;
import java.util.List;

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
}
