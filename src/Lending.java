package src;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Lending {

    private int id;
    private String borrowerName;
    private String loanType;
    private double principalAmount;
    private double interestRate; // Annual rate
    private int tenureMonths;
    private String dateLent; // Stored as "YYYY-MM-DD"
    private String status;
    private String notes;

    // --- Calculated Fields ---
    private double monthlyPayment;      // The EMI they owe you
    private double totalInterestToReceive;
    private double totalToReceive;      // Principal + Interest

    /**
     * Constructor for creating a NEW lending record.
     * Automatically calculates EMI, Total Interest, and Total Repayment.
     */
    public Lending(String borrowerName, String loanType, double principalAmount, 
                   double interestRate, int tenureMonths, String dateLent, String notes) {
        this.id = 0; // 0 indicates not yet in DB
        this.borrowerName = borrowerName;
        this.loanType = loanType;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.dateLent = dateLent;
        this.notes = notes;
        this.status = "Active";
        
        // Perform the financial calculations
        calculateLendingDetails();
    }

    /**
     * Constructor for LOADING an existing record from the database.
     */
    public Lending(int id, String borrowerName, String loanType, double principalAmount, 
                   double interestRate, int tenureMonths, String dateLent, String status, 
                   String notes, double monthlyPayment, double totalInterest, double totalPayment) {
        this.id = id;
        this.borrowerName = borrowerName;
        this.loanType = loanType;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.dateLent = dateLent;
        this.status = status;
        this.notes = notes;
        
        // Set the pre-calculated values from DB
        this.monthlyPayment = monthlyPayment;
        this.totalInterestToReceive = totalInterest;
        this.totalToReceive = totalPayment;
    }

    /**
     * The core EMI and Total Payment calculation logic.
     * E = P * r * (1+r)^n / ((1+r)^n - 1)
     */
    private void calculateLendingDetails() {
        if (principalAmount <= 0 || tenureMonths <= 0) {
            this.monthlyPayment = 0;
            this.totalToReceive = 0;
            this.totalInterestToReceive = 0;
            return;
        }
        
        // Handle 0% interest loan
        if (interestRate == 0) {
            this.monthlyPayment = principalAmount / tenureMonths;
            this.totalToReceive = principalAmount;
            this.totalInterestToReceive = 0;
            return;
        }

        // Convert annual rate (e.g., 8.5) to monthly rate (e.g., 0.007083)
        double r = (interestRate / 12.0) / 100.0;
        double n = tenureMonths; // Total number of payments

        // Calculate EMI (Monthly Payment)
        double emi = (principalAmount * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);

        this.monthlyPayment = emi;
        this.totalToReceive = emi * n;
        this.totalInterestToReceive = this.totalToReceive - this.principalAmount;
    }

    /**
     * Used for display in the JList on the main UI.
     */
    @Override
    public String toString() {
        return borrowerName + " (" + loanType + ")";
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getBorrowerName() { return borrowerName; }
    public String getLoanType() { return loanType; }
    public double getPrincipalAmount() { return principalAmount; }
    public double getInterestRate() { return interestRate; }
    public int getTenureMonths() { return tenureMonths; }
    public String getDateLent() { return dateLent; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public double getMonthlyPayment() { return monthlyPayment; }
    public double getTotalInterestToReceive() { return totalInterestToReceive; }
    public double getTotalToReceive() { return totalToReceive; }
}