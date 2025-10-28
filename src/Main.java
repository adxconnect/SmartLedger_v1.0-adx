package src;
import java.util.Scanner;
import java.sql.SQLException;
// Import the BankAccount class
import src.BankAccount;
import java.util.List;
// Import other necessary classes if used directly (though most interaction is via FinanceManager)
import src.Transaction;
import src.Card;
import src.GoldSilverInvestment;
import src.MutualFund;
// Removed FixedDeposit and RecurringDeposit imports

public class Main {
    public static void main(String[] args) throws SQLException {
        FinanceManager manager = new FinanceManager();

        // Note: File-loading methods might need adjustment or removal
        // if they rely on the deleted FD/RD classes.
        // manager.loadRecurringDeposits("data/recurringdeposits.txt"); // Assuming this is updated or removed in FinanceManager
        // manager.loadFixedDeposits("data/fixeddeposits.txt");     // Assuming this is updated or removed in FinanceManager
        manager.loadCreditCards("data/creditcards.txt");
        manager.loadFromFile("data/transactions.txt"); // Loads Transactions
        manager.loadMutualFunds("data/mutualfunds.txt");
        manager.loadGoldSilverInvestments("data/goldsilverinvestments.txt");

        Scanner sc = new Scanner(System.in);
        int choice = 0;

        do {
            System.out.println("\n--- Personal Finance Manager (Console) ---");
            System.out.println("1. Add Transaction (File)");
            System.out.println("2. View Transactions (File)");
            System.out.println("3. Show Balance (File)");
            System.out.println("4. View Transactions with IDs (File)");
            System.out.println("5. Edit Transaction (File)");
            System.out.println("6. Delete Transaction (File)");
            System.out.println("7. Show Monthly Report (File)");
            System.out.println("8. Show Category-wise Report (File)");
            System.out.println("9. Set Monthly Budget (File)");
            System.out.println("10. Check Budget Status (File)");
            System.out.println("11. Save Transactions to File & Continue"); // Changed exit option slightly
            System.out.println("12. Add Bank Account (DB)");
            System.out.println("13. View Bank Accounts (DB)");
            // Options 14-17 removed
            System.out.println("18. Add Mutual Fund (File)");
            System.out.println("19. View Mutual Funds (File)");
            System.out.println("20. Add Gold/Silver Investment (File)");
            System.out.println("21. View Gold/Silver Investments (File)");
            System.out.println("22. Add Credit Card (File)");
            System.out.println("23. View Credit Cards (File)");
            System.out.println("24. Add Credit Card Expense (File)");
            System.out.println("25. Make Credit Card Payment (File)");
            System.out.println("26. Save All Files & Exit"); // Main exit option

            System.out.print("Enter choice: ");

            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                sc.next(); // Clear invalid input
                continue;
            }
            choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1 -> { // Add Transaction (File)
                    System.out.print("Enter date (DD-MM-YYYY): "); String date = sc.nextLine();
                    System.out.print("Enter category: "); String category = sc.nextLine();
                    System.out.print("Enter type (Income/Expense): "); String type = sc.nextLine();
                    System.out.print("Enter amount: "); double amount = sc.nextDouble(); sc.nextLine();
                    System.out.print("Enter description: "); String desc = sc.nextLine();
                    System.out.print("Enter Payment Method (UPI/CASH/CARD): "); String paymentMethod = sc.nextLine();
                    System.out.print("Enter Payee: "); String payee = sc.nextLine();
                    manager.addTransaction(new Transaction(date, category, type, amount, desc, paymentMethod, payee));
                    System.out.println("Transaction added to local list (remember to save!)");
                }
                case 2 -> manager.viewAllTransactions(); // View Transactions (File)
                case 3 -> System.out.println("Current balance (File): ₹" + manager.calculateBalance()); // Show Balance (File)
                case 4 -> manager.viewTransactionsWithIndex(); // View Transactions with IDs (File)
                case 5 -> { // Edit Transaction (File)
                     System.out.print("Enter transaction ID to edit: "); int editId = sc.nextInt(); sc.nextLine();
                     System.out.println("Enter new transaction details:");
                     System.out.print("Date (DD-MM-YYYY): "); String date = sc.nextLine();
                     System.out.print("Category: "); String category = sc.nextLine();
                     System.out.print("Type (Income/Expense): "); String type = sc.nextLine();
                     System.out.print("Amount: "); double amount = sc.nextDouble(); sc.nextLine();
                     System.out.print("Description: "); String desc = sc.nextLine();
                     System.out.print("Enter Payment Method (UPI/CASH/CARD): "); String paymentMethod = sc.nextLine();
                     System.out.print("Enter Payee: "); String payee = sc.nextLine();
                     Transaction newTrans = new Transaction(date, category, type, amount, desc, paymentMethod, payee);
                     boolean edited = manager.editTransaction(editId, newTrans);
                     System.out.println(edited ? "Transaction edited successfully." : "Invalid transaction ID.");
                }
                case 6 -> { // Delete Transaction (File)
                     System.out.print("Enter transaction ID to delete: "); int deleteId = sc.nextInt(); sc.nextLine();
                     boolean deleted = manager.deleteTransaction(deleteId);
                     System.out.println(deleted ? "Transaction deleted successfully." : "Invalid transaction ID.");
                }
                case 7 -> { // Show Monthly Report (File)
                    System.out.print("Enter month (MM-YYYY): "); String month = sc.nextLine();
                    manager.showMonthlySummary(month);
                }
                case 8 -> manager.showCategorySummary(); // Show Category-wise Report (File)
                case 9 -> { // Set Monthly Budget (File)
                    System.out.print("Enter your monthly budget: "); double budget = sc.nextDouble(); sc.nextLine();
                    manager.setMonthlyBudget(budget);
                }
                case 10 -> manager.checkBudgetStatus(); // Check Budget Status (File)
                case 11 -> { // Save Transactions to File only
                    manager.saveToFile("data/transactions.txt");
                    System.out.println("Transactions saved to file.");
                }

                // --- BANK ACCOUNT (DB) ---
                case 12 -> { // Add Bank Account (DB)
                    try {
                        System.out.println("--- Add New Bank Account (to Database) ---");
                        System.out.print("Enter Bank Name: "); String bank = sc.nextLine();
                        System.out.print("Enter Account Number: "); String accNum = sc.nextLine();
                        System.out.print("Enter Holder Name: "); String holder = sc.nextLine();
                        System.out.print("Enter IFSC Code: "); String ifsc = sc.nextLine();
                        System.out.print("Enter Current Balance: "); double balance = sc.nextDouble(); sc.nextLine();
                        System.out.print("Enter Account Type (1: Savings, 2: Current): "); int typeChoice = sc.nextInt(); sc.nextLine();
                        String accountType = ""; double interestRate = 0.0; double annualExpense = 0.0;
                        String accountSubtype = ""; String companyName = ""; String businessName = "";
                        if (typeChoice == 1) {
                            accountType = "Savings";
                            System.out.print("Enter Interest Rate (%): "); interestRate = sc.nextDouble(); sc.nextLine();
                            System.out.print("Enter Annual Expense (for interest calc): "); annualExpense = sc.nextDouble(); sc.nextLine();
                        } else {
                            accountType = "Current";
                            System.out.print("Enter Sub-Type (1: Salary, 2: Business): "); int subTypeChoice = sc.nextInt(); sc.nextLine();
                            if (subTypeChoice == 1) {
                                accountSubtype = "Salary"; System.out.print("Enter Company Name: "); companyName = sc.nextLine();
                            } else {
                                accountSubtype = "Business"; System.out.print("Enter Business Name: "); businessName = sc.nextLine();
                            }
                        }
                        BankAccount ba = new BankAccount(accNum, holder, bank, ifsc, balance, accountType, interestRate, annualExpense, accountSubtype, companyName, businessName);
                        manager.saveBankAccount(ba);
                        System.out.println("Bank Account saved to database successfully!");
                    } catch (Exception e) {
                        System.out.println("Error adding account: " + e.getMessage());
                        if (e instanceof java.util.InputMismatchException) sc.nextLine();
                    }
                }
                case 13 -> { // View Bank Accounts (DB)
                    try {
                        System.out.println("\n--- Bank Accounts (from Database) ---");
                        List<BankAccount> accounts = manager.getAllBankAccounts();
                        if (accounts.isEmpty()) System.out.println("No bank accounts found in the database.");
                        else accounts.forEach(acc -> System.out.println(acc.toString() + " | Balance: ₹" + acc.getBalance()));
                    } catch (SQLException e) {
                        System.out.println("Error loading bank accounts: " + e.getMessage());
                    }
                }

                // Cases 14, 15, 16, 17 Removed (Handled by Deposits tab in GUI)

                // --- OTHER FILE-BASED OPERATIONS ---
                case 18 -> { // Add Mutual Fund (File)
                    System.out.print("Enter investment amount: "); double amount = sc.nextDouble();
                    System.out.print("Enter expected return rate (%): "); double expectedRate = sc.nextDouble();
                    System.out.print("Enter duration in years: "); int years = sc.nextInt(); sc.nextLine();
                    manager.addMutualFund(new MutualFund(amount, expectedRate, years));
                    System.out.println("Mutual Fund investment added to list!");
                }
                case 19 -> { // View Mutual Funds (File)
                    System.out.println("\n--- Mutual Fund Investments (File) ---");
                    manager.viewMutualFunds();
                }
                case 20 -> { // Add Gold/Silver (File)
                    System.out.print("Enter metal type (Gold/Silver): "); String type = sc.nextLine();
                    System.out.print("Enter weight (in grams): "); double weight = sc.nextDouble();
                    System.out.print("Enter current price per gram: "); double pricePerGram = sc.nextDouble(); sc.nextLine();
                    manager.addGoldSilverInvestment(new GoldSilverInvestment(type, weight, pricePerGram));
                    System.out.println(type + " investment added to list!");
                }
                case 21 -> { // View Gold/Silver (File)
                    System.out.println("\n--- Gold & Silver Investments (File) ---");
                    manager.viewGoldSilverInvestments();
                }
                case 22 -> { // Add Credit Card (File)
                    System.out.print("Enter card name: "); String name = sc.nextLine();
                    System.out.print("Enter credit limit: "); double limit = sc.nextDouble();
                    System.out.print("Enter current expenses: "); double expenses = sc.nextDouble();
                    System.out.print("Enter amount to pay: "); double amountToPay = sc.nextDouble();
                    System.out.print("Enter days left to pay: "); int daysLeft = sc.nextInt(); sc.nextLine();
                    Card card = new Card(name, limit, expenses, amountToPay, daysLeft);
                    manager.addCreditCard(card);
                    System.out.println("Credit Card added to list!");
                }
                case 23 -> { // View Credit Cards (File)
                    System.out.println("\n--- Credit Cards (File) ---");
                    manager.viewCreditCards();
                }
                 case 24 -> { // Add Credit Card Expense (File)
                     System.out.println("Select card index to add expense:");
                     manager.viewCreditCards(); // Show cards with index (implicitly done by FinanceManager's view method if implemented that way)
                     System.out.print("Enter card number (index): "); int cardIndex = sc.nextInt(); sc.nextLine(); // Consume newline after int
                     System.out.print("Enter expense amount: "); double amount = sc.nextDouble(); sc.nextLine(); // Consume newline after double
                     manager.addCreditCardExpense(cardIndex, amount);
                 }
                 case 25 -> { // Make Credit Card Payment (File)
                     System.out.println("Select card index to make payment:");
                     manager.viewCreditCards();
                     System.out.print("Enter card number (index): "); int cardIndex = sc.nextInt(); sc.nextLine();
                     System.out.print("Enter payment amount: "); double amount = sc.nextDouble(); sc.nextLine();
                     manager.makeCreditCardPayment(cardIndex, amount);
                 }

                // --- SAVE ALL & EXIT ---
                case 26 -> { // Save All Files & Exit
                    manager.saveToFile("data/transactions.txt");
                    // manager.saveRecurringDeposits("data/recurringdeposits.txt"); // Removed
                    // manager.saveFixedDeposits("data/fixeddeposits.txt");       // Removed
                    manager.saveCreditCards("data/creditcards.txt");
                    manager.saveMutualFunds("data/mutualfunds.txt");
                    manager.saveGoldSilverInvestments("data/goldsilverinvestments.txt");
                    System.out.println("All file data saved successfully. Goodbye!");
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 26); // Only exit loop on choice 26

        sc.close();
    }
}