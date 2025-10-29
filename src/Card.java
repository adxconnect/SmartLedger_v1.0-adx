package src;

// Necessary for Date parsing if you want to store validFrom/validThrough as LocalDate
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID; // To generate a unique ID if not using DB auto-increment for a card entry

public class Card {

    private int id; // Database ID
    private String uniqueId; // A unique ID for internal tracking if needed, separate from DB PK
    private String cardName; // User-friendly name for the card (e.g., "My SBI Visa")
    private String cardType; // "Credit Card" or "Debit Card"

    // Sensitive Data - will be protected by 2FA
    private String cardNumber;   // 16-digit number
    private String validFrom;    // MM/YY - optional, can be null or empty
    private String validThrough; // MM/YY
    private String cvv;          // 3 or 4 digit CVV/CVC
    private String frontImagePath; // Path to the front image of the card
    private String backImagePath;  // Path to the back image of the card

    // Credit Card Specific
    private double creditLimit;
    private double currentExpenses;
    private double amountToPay;
    private int daysLeftToPay; // Days till due date

    // Debit Card Specific (can add more if needed)
    // For now, Debit cards won't have specific fields beyond the common ones

    private String creationDate; // YYYY-MM-DD for consistency

    // DateTimeFormatter for MM/YY
    private static final DateTimeFormatter MM_YY_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");


    // --- Constructors ---

    // Constructor for new Card creation (without ID, UUID generated)
    public Card(String cardName, String cardType, String cardNumber, String validFrom, String validThrough,
                String cvv, String frontImagePath, String backImagePath,
                double creditLimit, double currentExpenses, double amountToPay, int daysLeftToPay,
                String creationDate) {
        this.uniqueId = UUID.randomUUID().toString(); // Generate a unique ID
        this.cardName = cardName;
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.validFrom = validFrom;
        this.validThrough = validThrough;
        this.cvv = cvv;
        this.frontImagePath = frontImagePath;
        this.backImagePath = backImagePath;
        this.creditLimit = creditLimit;
        this.currentExpenses = currentExpenses;
        this.amountToPay = amountToPay;
        this.daysLeftToPay = daysLeftToPay;
        this.creationDate = creationDate;
    }

    // Constructor for loading from DB (with ID and existing Unique ID)
    public Card(int id, String uniqueId, String cardName, String cardType, String cardNumber, String validFrom, String validThrough,
                String cvv, String frontImagePath, String backImagePath,
                double creditLimit, double currentExpenses, double amountToPay, int daysLeftToPay,
                String creationDate) {
        this(cardName, cardType, cardNumber, validFrom, validThrough, cvv, frontImagePath, backImagePath,
             creditLimit, currentExpenses, amountToPay, daysLeftToPay, creationDate);
        this.id = id;
        this.uniqueId = uniqueId; // Override with loaded uniqueId
    }


    // --- Getters ---
    public int getId() { return id; }
    public String getUniqueId() { return uniqueId; }
    public String getCardName() { return cardName; }
    public String getCardType() { return cardType; }
    public String getCardNumber() { return cardNumber; }
    public String getValidFrom() { return validFrom; }
    public String getValidThrough() { return validThrough; }
    public String getCvv() { return cvv; }
    public String getFrontImagePath() { return frontImagePath; }
    public String getBackImagePath() { return backImagePath; }
    public double getCreditLimit() { return creditLimit; }
    public double getCurrentExpenses() { return currentExpenses; }
    public double getAmountToPay() { return amountToPay; }
    public int getDaysLeftToPay() { return daysLeftToPay; }
    public String getCreationDate() { return creationDate; }


    // --- Setters (for updating fields) ---
    public void setId(int id) { this.id = id; } // Used after saving a new card to get the DB ID
    public void setCardName(String cardName) { this.cardName = cardName; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public void setValidFrom(String validFrom) { this.validFrom = validFrom; }
    public void setValidThrough(String validThrough) { this.validThrough = validThrough; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    public void setFrontImagePath(String frontImagePath) { this.frontImagePath = frontImagePath; }
    public void setBackImagePath(String backImagePath) { this.backImagePath = backImagePath; }
    public void setCreditLimit(double creditLimit) { this.creditLimit = creditLimit; }
    public void setCurrentExpenses(double currentExpenses) { this.currentExpenses = currentExpenses; }
    public void setAmountToPay(double amountToPay) { this.amountToPay = amountToPay; }
    public void setDaysLeftToPay(int daysLeftToPay) { this.daysLeftToPay = daysLeftToPay; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }


    // --- Helper Methods ---

    // Method to mask sensitive details for display
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "**** **** **** " + (cardNumber != null ? cardNumber : "");
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    // Basic toString for display in lists
    @Override
    public String toString() {
        return cardType + ": " + cardName + " (" + getMaskedCardNumber() + ")";
    }

    // You might want to add validation methods here later (e.g., isValidCardNumber, isValidCVV)
}