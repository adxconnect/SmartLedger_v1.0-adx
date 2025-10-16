public class Transaction {
    private String date;
    private String category;
    private String type; // "Income" or "Expense"
    private double amount;
    private String description;

    public Transaction(String date, String category, String type, double amount, String description) {
        this.date = date;
        this.category = category;
        this.type = type;
        this.amount = amount;
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return date + " | " + category + " | " + type + " | " + amount + " | " + description;
    }
}
