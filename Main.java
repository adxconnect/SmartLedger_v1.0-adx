import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        FinanceManager manager = new FinanceManager();
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
            System.out.println("7. Exit");
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

                default -> System.out.println("Invalid choice!");
            }
        } while (choice != 7);
    }
}
