package src;
import java.util.Scanner;
import java.sql.SQLException;
// We need to import the new BankAccount class
import src.BankAccount; 
import java.util.List; // And we need List for case 13

public class Main {
    public static void main(String[] args) throws SQLException {
        FinanceManager manager = new FinanceManager();
        
        // Note: These file-loading methods are part of your old console app logic
        // The new GUI app reads from the database instead.
        manager.loadRecurringDeposits("data/recurringdeposits.txt");
        manager.loadFixedDeposits("data/fixeddeposits.txt");
        manager.loadCreditCards("data/creditcards.txt");
        manager.loadFromFile("data/transactions.txt");
        manager.loadMutualFunds("data/mutualfunds.txt");
        manager.loadGoldSilverInvestments("data/goldsilverinvestments.txt");

        Scanner sc = new Scanner(System.in);
        int choice = 0;

        do {
            System.out.println("\n--- Personal Finance Manager (Console) ---");
            System.out.println("1. Add Transaction");
            System.out.println("2. View Transactions (from file)");
            // ... (other options) ...
            System.out.println("12. Add Bank Account (to DB)");
            System.out.println("13. View Bank Accounts (from DB)");
            // ... (other options) ...
            System.out.println("26. Save and Exit");

            System.out.print("Enter choice: ");
            
            // Added error handling for non-integer input
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                sc.next(); // Clear the invalid input
                continue;
            }
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
                    System.out.print("Enter Payment Method (UPI/CASH/CARD): ");
                    String paymentMethod = sc.nextLine();
                    System.out.print("Enter Payee: ");
                    String payee = sc.nextLine();

                    // This uses the file-based addTransaction, not the DB one
                    manager.addTransaction(new Transaction(date, category, type, amount, desc, paymentMethod, payee));
                    System.out.println("Transaction added to local list (remember to save!)");
                }
                case 2 -> manager.viewAllTransactions();
                case 3 -> System.out.println("Current balance: ₹" + manager.calculateBalance());
                case 4 -> {
                    System.out.println("All transactions with IDs:");
                    manager.viewTransactionsWithIndex();
                }
                case 5 -> {
                    System.out.print("Enter transaction ID to edit: ");
                    int editId = sc.nextInt();
                    sc.nextLine(); 
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
                    System.out.print("Enter Payment Method (UPI/CASH/CARD): ");
                    String paymentMethod = sc.nextLine();
                    System.out.print("Enter Payee: ");
                    String payee = sc.nextLine();

                    Transaction newTrans = new Transaction(date, category, type, amount, desc, paymentMethod, payee);
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
                    // This is one of the "Save and Exit" options
                    manager.saveToFile("data/transactions.txt");
                    System.out.println("Transactions saved to file. Exiting.");
                }
                
                // --- CASE 12: REWRITTEN ---
                case 12 -> {
                    try {
                        System.out.println("--- Add New Bank Account (to Database) ---");
                        System.out.print("Enter Bank Name: ");
                        String bank = sc.nextLine();
                        System.out.print("Enter Account Number: ");
                        String accNum = sc.nextLine();
                        System.out.print("Enter Holder Name: ");
                        String holder = sc.nextLine();
                        System.out.print("Enter IFSC Code: ");
                        String ifsc = sc.nextLine();
                        System.out.print("Enter Current Balance: ");
                        double balance = sc.nextDouble();
                        sc.nextLine(); // Consume newline

                        System.out.print("Enter Account Type (1: Savings, 2: Current): ");
                        int typeChoice = sc.nextInt();
                        sc.nextLine();

                        String accountType = "";
                        double interestRate = 0.0;
                        double annualExpense = 0.0;
                        String accountSubtype = "";
                        String companyName = "";
                        String businessName = "";

                        if (typeChoice == 1) {
                            accountType = "Savings";
                            System.out.print("Enter Interest Rate (%): ");
                            interestRate = sc.nextDouble();
                            sc.nextLine();
                            System.out.print("Enter Annual Expense (for interest calc): ");
                            annualExpense = sc.nextDouble();
                            sc.nextLine();
                        } else {
                            accountType = "Current";
                            System.out.print("Enter Sub-Type (1: Salary, 2: Business): ");
                            int subTypeChoice = sc.nextInt();
                            sc.nextLine();
                            
                            if (subTypeChoice == 1) {
                                accountSubtype = "Salary";
                                System.out.print("Enter Company Name: ");
                                companyName = sc.nextLine();
                            } else {
                                accountSubtype = "Business";
                                System.out.print("Enter Business Name: ");
                                businessName = sc.nextLine();
                            }
                        }

                        // Use the new BankAccount constructor
                        BankAccount ba = new BankAccount(
                            accNum, holder, bank, ifsc, balance,
                            accountType, interestRate, annualExpense,
                            accountSubtype, companyName, businessName
                        );
                        
                        // Use the new save method
                        manager.saveBankAccount(ba);
                        System.out.println("Bank Account saved to database successfully!");

                    } catch (Exception e) {
                        System.out.println("Error adding account: " + e.getMessage());
                        // If there was a non-numeric input error, clear the scanner
                        if (e instanceof java.util.InputMismatchException) {
                            sc.nextLine();
                        }
                    }
                }
                
                // --- CASE 13: REWRITTEN ---
                case 13 -> {
                    try {
                        System.out.println("\n--- Bank Accounts (from Database) ---");
                        List<BankAccount> accounts = manager.getAllBankAccounts();
                        if (accounts.isEmpty()) {
                            System.out.println("No bank accounts found in the database.");
                        }
                        for (BankAccount acc : accounts) {
                            // Print a summary using the toString() method
                            System.out.println(acc.toString() + " | Balance: ₹" + acc.getBalance());
                        }
                    } catch (SQLException e) {
                        System.out.println("Error loading bank accounts: " + e.getMessage());
                    }
                }

                // ... (rest of your cases 14-25) ...

                case 14 -> {
                    System.out.print("Enter account number: ");
                    String accNum = sc.nextLine();
                    System.out.print("Enter principal amount: ");
                    double principal = sc.nextDouble();
                    System.out.print("Enter annual interest rate (%): ");
                    double rate = sc.nextDouble();
                    System.out.print("Enter duration in months: ");
                    int tenureMonths = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter start date (DD-MM-YYYY): ");
                    String startDate = sc.nextLine();
                    System.out.print("Enter holder name: ");
                    String holderName = sc.nextLine();

                    FixedDeposit fd = new FixedDeposit(accNum, principal, rate, tenureMonths, startDate, holderName);
                    manager.saveFixedDeposit(fd);  // Calls DB saving method
                    System.out.println("Fixed Deposit added successfully!");
                }
                case 15 -> {
                    System.out.println("\n--- Fixed Deposits ---");
                    // This old method still exists in your FinanceManager
                    manager.viewFixedDeposits();
                }
                case 16 -> {
                    System.out.print("Enter account number: ");
                    String accNum = sc.nextLine();
                    System.out.print("Enter monthly deposit amount: ");
                    double monthlyDeposit = sc.nextDouble();
                    System.out.print("Enter annual interest rate (%): ");
                    double rate = sc.nextDouble();
                    System.out.print("Enter duration in months: ");
                    int months = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter start date (DD-MM-YYYY): ");
                    String startDate = sc.nextLine();
                    System.out.print("Enter holder name: ");
                    String holderName = sc.nextLine();

                    RecurringDeposit rd = new RecurringDeposit(accNum, monthlyDeposit, rate, months, startDate, holderName);
                    manager.saveRecurringDeposit(rd);  // Calls DB saving method
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
                                        CreditCard card = new CreditCard(name, limit, expenses, amountToPay, daysLeft);
                                        manager.addCreditCard(card);
                                        System.out.println("Credit Card added successfully!");
                                    }
                                    case 23 -> {
                                        System.out.println("\n--- Credit Cards ---");
                                        manager.viewCreditCards();
                                    }
                                    case 24 -> {
                                        System.out.println("Calculating investment returns...");
                                        // TODO: Implement calculateInvestmentReturns() method in FinanceManager
                                        System.out.println("Investment returns calculation feature not yet implemented.");
                                    }
                                    case 25 -> {
                                        System.out.println("Generating comprehensive financial report...");
                                        // TODO: Implement generateFinancialReport() method in FinanceManager
                                        System.out.println("Financial report generation feature not yet implemented.");
                                    }
                                    case 26 -> {
                                        manager.saveToFile("data/transactions.txt");
                                        manager.saveRecurringDeposits("data/recurringdeposits.txt");
                                        manager.saveFixedDeposits("data/fixeddeposits.txt");
                                        manager.saveCreditCards("data/creditcards.txt");
                                        manager.saveMutualFunds("data/mutualfunds.txt");
                                        manager.saveGoldSilverInvestments("data/goldsilverinvestments.txt");
                                        System.out.println("All data saved successfully. Goodbye!");
                                    }
                                    default -> System.out.println("Invalid choice. Please try again.");
                                }
                            } while (choice != 26);
                    
                            sc.close();
                        }
                    }