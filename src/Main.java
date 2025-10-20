package src;
import java.util.Scanner;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        FinanceManager manager = new FinanceManager();
        manager.loadRecurringDeposits("data/recurringdeposits.txt");
        manager.loadFixedDeposits("data/fixeddeposits.txt");
        manager.loadCreditCards("data/creditcards.txt");
        manager.loadFromFile("data/transactions.txt");
        manager.loadMutualFunds("data/mutualfunds.txt");
        manager.loadGoldSilverInvestments("data/goldsilverinvestments.txt");

        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n--- Personal Finance Manager ---");
            System.out.println("1. Add Transaction");
            System.out.println("2. View Transactions");
            System.out.println("3. Show Balance");
            System.out.println("4. View Transactions with IDs");
            System.out.println("5. Edit Transaction");
            System.out.println("6. Delete Transaction");
            System.out.println("7. Show Monthly Report");
            System.out.println("8. Show Category-wise Report");
            System.out.println("9. Set Monthly Budget");
            System.out.println("10. Check Budget Status");
            System.out.println("11. Save and Exit");
            System.out.println("12. Add Savings Account");
            System.out.println("13. View Savings Accounts");
            System.out.println("14. Add Fixed Deposit");
            System.out.println("15. View Fixed Deposits");
            System.out.println("16. Add Recurring Deposit");
            System.out.println("17. View Recurring Deposits");
            System.out.println("18. Add Mutual Fund");
            System.out.println("19. View Mutual Funds");
            System.out.println("20. Add Gold/Silver Investment");
            System.out.println("21. View Gold/Silver Investments");
            System.out.println("22. Add Credit Card");
            System.out.println("23. View Credit Cards");
            System.out.println("24. Add Credit Card Expense");
            System.out.println("25. Make Credit Card Payment");
            System.out.println("26. Save and Exit");


            System.out.print("Enter choice: ");

            choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter date (DD-MM-YYYY): ");
                    String date = sc.nextLine();
                    System.out.print("Enter category: ");
                    String category = sc.nextLine();
                    System.out.print("Enter type (Income/Expense): ");
                    String type = sc.nextLine();
                    System.out.print("Enter amount: ");
                    double amount = sc.nextDouble();
                    sc.nextLine();
                    System.out.print("Enter description: ");
                    String desc = sc.nextLine();

                    manager.addTransaction(new Transaction(date, category, type, amount, desc));
                    System.out.println("Transaction added successfully!");
                }
                case 2 -> manager.viewAllTransactions();
                case 3 -> System.out.println("Current balance: ₹" + manager.calculateBalance());
                case 4 -> {System.out.println("All transactions with IDs:");
                manager.viewTransactionsWithIndex();
                }
                case 5 -> {
        System.out.print("Enter transaction ID to edit: ");
    int editId = sc.nextInt();
    sc.nextLine(); // consume newline

    System.out.println("Enter new transaction details:");
    System.out.print("Date (DD-MM-YYYY): ");
    String date = sc.nextLine();
    System.out.print("Category: ");
    String category = sc.nextLine();
    System.out.print("Type (Income/Expense): ");
    String type = sc.nextLine();
    System.out.print("Amount: ");
    double amount = sc.nextDouble();
    sc.nextLine();
    System.out.print("Description: ");
    String desc = sc.nextLine();

    Transaction newTrans = new Transaction(date, category, type, amount, desc);
    boolean edited = manager.editTransaction(editId, newTrans);
    if (edited) {
        System.out.println("Transaction edited successfully.");
    } else {
        System.out.println("Invalid transaction ID.");
    }
}
                case 6 -> {
    System.out.print("Enter transaction ID to delete: ");
    int deleteId = sc.nextInt();
    sc.nextLine();

    boolean deleted = manager.deleteTransaction(deleteId);
    if (deleted) {
        System.out.println("Transaction deleted successfully.");
    } else {
        System.out.println("Invalid transaction ID.");
    }
}
case 7 -> {
    System.out.print("Enter month (MM-YYYY): ");
    String month = sc.nextLine();
    manager.showMonthlySummary(month);
}
case 8 -> manager.showCategorySummary();

case 9 -> {
    System.out.print("Enter your monthly budget: ");
    double budget = sc.nextDouble();
    sc.nextLine();
    manager.setMonthlyBudget(budget);
}

case 10 -> manager.checkBudgetStatus();

case 11 -> {
    manager.saveToFile("data/transactions.txt");
    System.out.println("Exiting program. Goodbye!");
}
case 12 -> {  
    System.out.print("Enter initial balance: ");  
    double balance = sc.nextDouble();  
    System.out.print("Enter annual interest rate (%): ");  
    double rate = sc.nextDouble();  
    System.out.print("Enter number of years: ");  
    int years = sc.nextInt();  
    sc.nextLine();  
    manager.addSavingsAccount(new SavingsAccount(balance, rate, years));  
    System.out.println("Savings Account added successfully!");  
}

case 13 -> {  
    System.out.println("\n--- Savings Accounts ---");  
    manager.viewSavingsAccounts();  
}

case 14 -> {  
    System.out.print("Enter principal amount: ");  
    double principal = sc.nextDouble();  
    System.out.print("Enter annual interest rate (%): ");  
    double rate = sc.nextDouble();  
    System.out.print("Enter duration in years: ");  
    int years = sc.nextInt();  
    sc.nextLine();  
    manager.addFixedDeposit(new FixedDeposit(principal, rate, years));  
    System.out.println("Fixed Deposit added successfully!");  
}

case 15 -> {  
    System.out.println("\n--- Fixed Deposits ---");  
    manager.viewFixedDeposits();  
}

case 16 -> {  
    System.out.print("Enter monthly deposit amount: ");  
    double monthlyDeposit = sc.nextDouble();  
    System.out.print("Enter annual interest rate (%): ");  
    double rate = sc.nextDouble();  
    System.out.print("Enter duration in months: ");  
    int months = sc.nextInt();  
    sc.nextLine();  
    manager.addRecurringDeposit(new RecurringDeposit(monthlyDeposit, rate, months));  
    System.out.println("Recurring Deposit added successfully!");  
}

case 17 -> {  
    System.out.println("\n--- Recurring Deposits ---");  
    manager.viewRecurringDeposits();  
}

case 18 -> {  
    System.out.print("Enter investment amount: ");  
    double amount = sc.nextDouble();  
    System.out.print("Enter expected return rate (%): ");  
    double expectedRate = sc.nextDouble();  
    System.out.print("Enter duration in years: ");  
    int years = sc.nextInt();  
    sc.nextLine();  
    manager.addMutualFund(new MutualFund(amount, expectedRate, years));  
    System.out.println("Mutual Fund investment added successfully!");  
}

case 19 -> {  
    System.out.println("\n--- Mutual Fund Investments ---");  
    manager.viewMutualFunds();  
}

case 20 -> {  
    System.out.print("Enter metal type (Gold/Silver): ");  
    String type = sc.nextLine();  
    System.out.print("Enter weight (in grams): ");  
    double weight = sc.nextDouble();  
    System.out.print("Enter current price per gram: ");  
    double pricePerGram = sc.nextDouble();  
    sc.nextLine();  
    manager.addGoldSilverInvestment(new GoldSilverInvestment(type, weight, pricePerGram));  
    System.out.println(type + " investment added successfully!");  
}

case 21 -> {  
    System.out.println("\n--- Gold & Silver Investments ---");  
    manager.viewGoldSilverInvestments();  
}
case 22 -> {
    System.out.print("Enter card name: ");
    String name = sc.nextLine();
    System.out.print("Enter credit limit: ");
    double limit = sc.nextDouble();
    System.out.print("Enter current expenses: ");
    double expenses = sc.nextDouble();
    System.out.print("Enter amount to pay: ");
    double amountToPay = sc.nextDouble();
    System.out.print("Enter days left to pay: ");
    int daysLeft = sc.nextInt();
    sc.nextLine();

    manager.addCreditCard(new CreditCard(name, limit, expenses, amountToPay, daysLeft));
    System.out.println("Credit card added successfully!");
}

case 23 -> {
    System.out.println("\n--- Credit Cards ---");
    manager.viewCreditCards();
}

case 24 -> {
    System.out.println("Select card index to add expense:");
    manager.viewCreditCards();
    System.out.print("Enter card number: ");
    int cardIndex = sc.nextInt();
    System.out.print("Enter expense amount: ");
    double amount = sc.nextDouble();
    sc.nextLine();
    manager.addCreditCardExpense(cardIndex, amount);
}

case 25 -> {
    System.out.println("Select card index to make payment:");
    manager.viewCreditCards();
    System.out.print("Enter card number: ");
    int cardIndex = sc.nextInt();
    System.out.print("Enter payment amount: ");
    double amount = sc.nextDouble();
    sc.nextLine();
    manager.makeCreditCardPayment(cardIndex, amount);
}

case 26 -> {
    manager.saveToFile("data/transactions.txt");
    manager.saveRecurringDeposits("data/recurringdeposits.txt");
    manager.saveFixedDeposits("data/fixeddeposits.txt");
    manager.saveCreditCards("data/creditcards.txt");
    manager.saveMutualFunds("data/mutualfunds.txt");
    manager.saveGoldSilverInvestments("data/goldsilverinvestments.txt");
    System.out.println("Exiting program. Goodbye!");
    sc.close();
}
            default -> System.out.println("Invalid choice!");
            }
        } while (choice != 11 && choice != 26);
    }
     

}
