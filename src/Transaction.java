package src;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ArrayList;
import java.util.LinkedHashMap; // We want to keep the months in order
import java.util.List;
import java.util.Map;

public class Transaction {
    // New fields
    private int id;
    private String timestamp;
    private String day;
    private String paymentMethod;
    private String payee;

    // Existing fields
    private String date; // This will store the date as a "DD-MM-YYYY" String
    private String category;
    private String type; // "Income" or "Expense"
    private double amount;
    private String description;

    /**
     * Constructor for NEW transactions (from the UI dialog box).
     * We don't need id or timestamp, as the DB provides them automatically.
     */
    public Transaction(String date, String category, String type, double amount, String description, String paymentMethod, String payee) {
        this.date = date;
        this.category = category;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.paymentMethod = paymentMethod;
        this.payee = payee;
        // Automatically calculate the day name from the date
        this.day = getDayNameFromDate(date);
    }

    /**
     * Constructor for LOADING transactions (from the database).
     * This one includes all fields, including the ones from the DB.
     */
    public Transaction(int id, String date, String timestamp, String day, String category, String type, double amount, String description, String paymentMethod, String payee) {
        // Call the other constructor to set the main fields
        this(date, category, type, amount, description, paymentMethod, payee); 
        
        this.id = id;
        this.timestamp = timestamp;
        // We trust the day from the DB if it's there
        if (day != null && !day.isEmpty()) {
            this.day = day;
        }
    }
    
    /**
     * Helper function to get the full day name (e.g., "Tuesday") from a "DD-MM-YYYY" date string.
     */
    private String getDayNameFromDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate localDate = LocalDate.parse(dateStr, formatter);
            return localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        } catch (Exception e) {
            System.err.println("Error parsing date to get day name: " + dateStr + ". Error: " + e.getMessage());
            return "Unknown"; // Fallback
        }
    }

    // --- Getters for ALL fields ---
    public int getId() { return id; }
    public String getTimestamp() { return timestamp; }
    public String getDay() { return day; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPayee() { return payee; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }


    // --- File I/O (for console app) - Updated ---
    public String toCSV() {
        // We only save the "input" fields, not the auto-generated ones like id/timestamp
        return String.join(",", date, category, type, String.valueOf(amount), description, paymentMethod, payee);
    }

    public static Transaction fromCSV(String line) {
        String[] parts = line.split(",");
        String date = parts[0];
        String category = parts[1];
        String type = parts[2];
        double amount = Double.parseDouble(parts[3]);
        String description = (parts.length > 4) ? parts[4] : "";
        String paymentMethod = (parts.length > 5) ? parts[5] : "CASH"; // Default
        String payee = (parts.length > 6) ? parts[6] : ""; // Default
        
        // Use the simple constructor
        return new Transaction(date, category, type, amount, description, paymentMethod, payee);
    }

    // Original function - still useful
    public String getMonth() {
        String[] parts = date.split("-");
        if (parts.length >= 3) {
            return parts[1] + "-" + parts[2];
        }
        return date;
    }
    
}