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
}
