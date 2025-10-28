package src;

// This single class can hold data for ANY type of bank account.
public class BankAccount {

    private int id;
    
    // Common Details
    private String accountNumber;
    private String holderName;
    private String bankName;
    private String ifscCode;
    private double balance;
    
    // Type
    private String accountType; // "Savings" or "Current"
    
    // Savings Details
    private double interestRate;
    private double annualExpense;
    
    // Current Details
    private String accountSubtype; // "Salary" or "Business"
    private String companyName;
    private String businessName;

    // --- Constructors ---

    // A "master" constructor to set all fields
    public BankAccount(int id, String accountNumber, String holderName, String bankName, String ifscCode, double balance,
                       String accountType, double interestRate, double annualExpense,
                       String accountSubtype, String companyName, String businessName) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.bankName = bankName;
        this.ifscCode = ifscCode;
        this.balance = balance;
        this.accountType = accountType;
        this.interestRate = interestRate;
        this.annualExpense = annualExpense;
        this.accountSubtype = accountSubtype;
        this.companyName = companyName;
        this.businessName = businessName;
    }
    
    // A simpler constructor for the "Add" dialog
    public BankAccount(String accountNumber, String holderName, String bankName, String ifscCode, double balance,
                       String accountType, double interestRate, double annualExpense,
                       String accountSubtype, String companyName, String businessName) {
        this.id = 0; // 0 means "not yet in database"
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.bankName = bankName;
        this.ifscCode = ifscCode;
        this.balance = balance;
        this.accountType = accountType;
        this.interestRate = interestRate;
        this.annualExpense = annualExpense;
        this.accountSubtype = accountSubtype;
        this.companyName = companyName;
        this.businessName = businessName;
    }

    // --- Special Calculations ---
    
    /**
     * Calculates the principal amount that will earn interest.
     */
    public double getInterestApplicableAmount() {
        if ("Savings".equals(accountType)) {
            double principal = balance - annualExpense;
            return Math.max(0, principal); // Don't return a negative amount
        }
        return 0;
    }

    /**
     * Calculates the estimated annual interest.
     */
    public double getEstimatedAnnualInterest() {
        if ("Savings".equals(accountType)) {
            return getInterestApplicableAmount() * (interestRate / 100.0);
        }
        return 0;
    }

    // --- Getters ---
    // (We need getters for all fields so the UI and DAO can read them)

    public int getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public String getBankName() { return bankName; }
    public String getIfscCode() { return ifscCode; }
    public double getBalance() { return balance; }
    public String getAccountType() { return accountType; }
    public double getInterestRate() { return interestRate; }
    public double getAnnualExpense() { return annualExpense; }
    public String getAccountSubtype() { return accountSubtype; }
    public String getCompanyName() { return companyName; }
    public String getBusinessName() { return businessName; }

    /**
     * This is what will be shown in the new JList (our navigation menu).
     */
    @Override
    public String toString() {
        return bankName + " - " + accountNumber;
    }
}