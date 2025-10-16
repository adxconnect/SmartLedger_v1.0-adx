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
            System.out.println("4. Exit");
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
                case 4 -> System.out.println("Exiting program. Goodbye!");
                default -> System.out.println("Invalid choice!");
            }
        } while (choice != 4);
    }
}
