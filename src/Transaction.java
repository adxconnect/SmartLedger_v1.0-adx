package src;
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

    public static Transaction fromCSV(String line) {
        String[] parts = line.split(",");
        return new Transaction(
            parts[0], // date
            parts[1], // category
            parts[2], // type
            Double.parseDouble(parts[3]), // amount
            parts.length > 4 ? parts[4] : ""  // description (safe fallback)
        );
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String toCSV() {
        return String.join(",", date, category, type, String.valueOf(amount), description);
    }

    public String getMonth() {
        // Extract month (as MM-YYYY) from the date format DD-MM-YYYY
        String[] parts = date.split("-");
        if (parts.length >= 3) {
            return parts[1] + "-" + parts[2];
        }
        return date;
    }
}
