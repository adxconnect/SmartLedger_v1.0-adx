package src;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Loan {

    private int id;
    private String lenderName;
    private String loanType;
    private double principalAmount;
    private double interestRate; // Annual rate
    private int tenureMonths;
    private String startDate; // Stored as "YYYY-MM-DD"
    private String status;
    private String notes;

    // --- Calculated Fields ---
    private double emiAmount;
    private double totalInterest;
    private double totalPayment;

    /**
     * Constructor for creating a NEW loan.
     * It automatically calculates EMI, Total Interest, and Total Payment.
     */
    public Loan(String lenderName, String loanType, double principalAmount, 
                double interestRate, int tenureMonths, String startDate, String notes) {
        this.id = 0; // 0 indicates not yet in DB
        this.lenderName = lenderName;
        this.loanType = loanType;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.startDate = startDate;
        this.notes = notes;
        this.status = "Active";
        
        // Perform the financial calculations
        calculateLoanDetails();
    }

    /**
     * Constructor for LOADING an existing loan from the database.
     * It uses the pre-calculated values stored in the DB.
     */
    public Loan(int id, String lenderName, String loanType, double principalAmount, 
                double interestRate, int tenureMonths, String startDate, String status, 
                String notes, double emiAmount, double totalInterest, double totalPayment) {
        this.id = id;
        this.lenderName = lenderName;
        this.loanType = loanType;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.startDate = startDate;
        this.status = status;
        this.notes = notes;
        
        // Set the pre-calculated values
        this.emiAmount = emiAmount;
        this.totalInterest = totalInterest;
        this.totalPayment = totalPayment;
    }

    /**
     * The core EMI and Total Payment calculation logic.
     * E = P * r * (1+r)^n / ((1+r)^n - 1)
     */
    private void calculateLoanDetails() {
        if (principalAmount <= 0 || tenureMonths <= 0) {
            this.emiAmount = 0;
            this.totalPayment = 0;
            this.totalInterest = 0;
            return;
        }
        
        // Handle 0% interest loan (e.g., "No-Cost EMI")
        if (interestRate == 0) {
            this.emiAmount = principalAmount / tenureMonths;
            this.totalPayment = principalAmount;
            this.totalInterest = 0;
            return;
        }

        // Convert annual rate (e.g., 8.5) to monthly rate (e.g., 0.007083)
        double r = (interestRate / 12.0) / 100.0;
        double n = tenureMonths; // Total number of payments

        // Calculate EMI
        double emi = (principalAmount * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);

        this.emiAmount = emi;
        this.totalPayment = emi * n;
        this.totalInterest = this.totalPayment - this.principalAmount;
    }

    /**
     * Used for display in the JList on the main UI.
     */
    @Override
    public String toString() {
        return loanType + " - " + lenderName;
    }

    // --- Getters ---
    // (Add Setters only if you need to edit these fields later)
    public int getId() { return id; }
    public String getLenderName() { return lenderName; }
    public String getLoanType() { return loanType; }
    public double getPrincipalAmount() { return principalAmount; }
    public double getInterestRate() { return interestRate; }
    public int getTenureMonths() { return tenureMonths; }
    public String getStartDate() { return startDate; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public double getEmiAmount() { return emiAmount; }
    public double getTotalInterest() { return totalInterest; }
    public double getTotalPayment() { return totalPayment; }
}