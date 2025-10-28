package src;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class Deposit {

    private int id;
    private String depositType; // "FD", "RD", "Gullak"
    private String holderName;
    private String description;
    private String goal;
    private String creationDate; // String representation from DB

    // FD/RD Specific
    private String accountNumber;
    private double principalAmount; // FD
    private double monthlyAmount;   // RD
    private double interestRate;
    private int tenure;
    private String tenureUnit;    // "Days", "Months", "Years"
    private String startDate;     // String representation "dd-MM-yyyy"

    // Gullak Specific
    private double currentTotal; // This will now be calculated from counts
    private String lastUpdated;   // String representation from DB
    private double gullakDueAmount;
    private Map<Integer, Integer> denominationCounts; // Key: Denomination, Value: Count

    // Constructor for loading from DB
    public Deposit(int id, String depositType, String holderName, String description, String goal, String creationDate,
                   String accountNumber, double principalAmount, double monthlyAmount, double interestRate,
                   int tenure, String tenureUnit, String startDate, double currentTotal,
                   String lastUpdated, double gullakDueAmount, Map<Integer, Integer> counts) {
        this.id = id;
        this.depositType = depositType;
        this.holderName = holderName;
        this.description = description;
        this.goal = goal;
        this.creationDate = creationDate;
        this.accountNumber = accountNumber;
        this.principalAmount = principalAmount;
        this.monthlyAmount = monthlyAmount;
        this.interestRate = interestRate;
        this.tenure = tenure;
        this.tenureUnit = tenureUnit;
        this.startDate = startDate;
        this.lastUpdated = lastUpdated;
        this.gullakDueAmount = gullakDueAmount;
        this.denominationCounts = (counts != null) ? counts : initializeEmptyCounts();
        this.currentTotal = calculateTotalFromDenominations(); // Calculate based on loaded counts
    }

    private Map<Integer, Integer> initializeEmptyCounts() {
        Map<Integer, Integer> counts = new HashMap<>();
        int[] denominations = {500, 200, 100, 50, 20, 10, 5, 2, 1};
        for (int denom : denominations) {
            counts.put(denom, 0);
        }
        return counts;
    }

    // --- Calculations ---

    public double calculateTotalFromDenominations() {
         if (!"Gullak".equals(depositType) || denominationCounts == null) {
            return 0;
         }
         double total = 0;
         for (Map.Entry<Integer, Integer> entry : denominationCounts.entrySet()) {
             total += entry.getKey() * entry.getValue();
         }
         this.currentTotal = total;
         return total;
    }

    // --- Replaced Placeholder ---
    public double calculateFDMaturityAmount() {
        if (!"FD".equals(depositType) || interestRate <= 0 || principalAmount <= 0 || tenure <= 0 || startDate == null) {
            return principalAmount;
        }
        try {
            double rate = interestRate / 100.0;
            double years = convertTenureToYears();
            // Simple Interest: P * (1 + rt)
            return principalAmount * (1 + rate * years);
            // Note: For compound interest, the formula depends on compounding frequency (e.g., quarterly, annually).
            // Example for annual compounding: P * Math.pow((1 + rate), years);
        } catch (Exception e) {
            System.err.println("Error calculating FD Maturity: " + e.getMessage());
            return principalAmount; // Fallback
        }
    }

    // --- Replaced Placeholder ---
    public double calculateRDMaturityAmount() {
         if (!"RD".equals(depositType) || interestRate <= 0 || monthlyAmount <= 0 || tenure <= 0 || !"Months".equals(tenureUnit)) {
             return ("Months".equals(tenureUnit)) ? monthlyAmount * tenure : 0; // Return total principal if valid RD
         }
        try {
            double P = monthlyAmount;
            int n = tenure; // Tenure must be in Months for this formula
            double r = interestRate / 100.0; // Annual rate
            double i = r / 12.0; // Monthly interest rate

            // Standard Future Value of Ordinary Annuity formula: M = P * [((1 + i)^n - 1) / i]
            // This calculates the value at the END of the last deposit month.
            double maturityValue = P * ((Math.pow(1 + i, n) - 1) / i);

            // Often, interest is calculated quarterly, which complicates things.
            // For simplicity, we'll use the monthly compounding formula.
            return maturityValue;

        } catch (Exception e) {
             System.err.println("Error calculating RD Maturity: " + e.getMessage());
             return ("Months".equals(tenureUnit)) ? monthlyAmount * tenure : 0; // Fallback to total principal
        }
    }

    // --- Replaced Placeholder ---
    public String calculateMaturityDate() {
        if (startDate == null || startDate.isEmpty() || tenure <= 0 || tenureUnit == null) {
            return "N/A";
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = start; // Initialize end date
            switch (tenureUnit) {
                case "Days":
                    end = start.plusDays(tenure);
                    break;
                case "Months":
                    end = start.plusMonths(tenure);
                    break;
                case "Years":
                    end = start.plusYears(tenure);
                    break;
                default:
                     return "Invalid Unit"; // Handle unexpected unit
            }
            return end.format(formatter);
        } catch (Exception e) {
            System.err.println("Error calculating maturity date: " + e.getMessage());
            return "Error";
        }
    }

    // --- Replaced Placeholder ---
    private double convertTenureToYears() {
        if (tenure <= 0 || tenureUnit == null) return 0.0;
        switch (tenureUnit) {
            case "Days": return (double)tenure / 365.0; // Use floating point division
            case "Months": return (double)tenure / 12.0;
            case "Years": return (double)tenure;
            default: return 0.0;
        }
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getDepositType() { return depositType; }
    public String getHolderName() { return holderName; }
    public String getDescription() { return description; }
    public String getGoal() { return goal; }
    public String getCreationDate() { return creationDate; }
    public String getAccountNumber() { return accountNumber; }
    public double getPrincipalAmount() { return principalAmount; }
    public double getMonthlyAmount() { return monthlyAmount; }
    public double getInterestRate() { return interestRate; }
    public int getTenure() { return tenure; }
    public String getTenureUnit() { return tenureUnit; }
    public String getStartDate() { return startDate; }
    public double getCurrentTotal() {
        if ("Gullak".equals(depositType)) {
             return calculateTotalFromDenominations();
        }
        return currentTotal;
     }
    public String getLastUpdated() { return lastUpdated; }
    public double getGullakDueAmount() { return gullakDueAmount; }
    public Map<Integer, Integer> getDenominationCounts() { return denominationCounts; }

    // --- Setters ---
    public void setDenominationCounts(Map<Integer, Integer> denominationCounts) {
        this.denominationCounts = denominationCounts;
        this.currentTotal = calculateTotalFromDenominations();
    }
    public void setGullakDueAmount(double gullakDueAmount) {
        this.gullakDueAmount = gullakDueAmount;
    }
     public void setDenominationCount(int denomination, int count) {
         if (this.denominationCounts != null && this.denominationCounts.containsKey(denomination)) {
              this.denominationCounts.put(denomination, count);
              this.currentTotal = calculateTotalFromDenominations();
         }
     }

    @Override
    public String toString() {
        String prefix = "";
        switch(depositType) {
            case "FD": prefix = "FD: "; break;
            case "RD": prefix = "RD: "; break;
            case "Gullak": prefix = "Gullak: "; break;
        }
        String name = (holderName != null && !holderName.isEmpty()) ? holderName : description;
        if (name == null || name.isEmpty()) name = "Deposit #" + id;
        return prefix + name;
    }
}