package src;

import java.sql.Timestamp;

public class TaxProfile {

    private int id;
    private String profileName;
    private String profileType;
    private String financialYear;
    
    private double grossIncome;
    private double totalDeductions;
    private double taxableIncome; // Calculated: gross - deductions
    private double taxPaid;
    private String notes;
    private Timestamp deletedOn; // For recycle bin tracking

    // Constructor for loading from DB
    public TaxProfile(int id, String profileName, String profileType, String financialYear, 
                      double grossIncome, double totalDeductions, double taxableIncome, 
                      double taxPaid, String notes) {
        this.id = id;
        this.profileName = profileName;
        this.profileType = profileType;
        this.financialYear = financialYear;
        this.grossIncome = grossIncome;
        this.totalDeductions = totalDeductions;
        this.taxableIncome = taxableIncome;
        this.taxPaid = taxPaid;
        this.notes = notes;
    }
    
    // Constructor for creating a new profile (ID will be set by DB)
    public TaxProfile(String profileName, String profileType, String financialYear, 
                      double grossIncome, double totalDeductions, double taxPaid, String notes) {
        this.id = 0; // 0 indicates not yet in DB
        this.profileName = profileName;
        this.profileType = profileType;
        this.financialYear = financialYear;
        this.grossIncome = grossIncome;
        this.totalDeductions = totalDeductions;
        this.taxPaid = taxPaid;
        this.notes = notes;
        
        // Calculate taxable income on creation
        this.calculateTaxableIncome();
    }
    
    /**
     * The core calculation logic.
     */
    public final void calculateTaxableIncome() {
        // Basic calculation. Can be made more complex later.
        this.taxableIncome = Math.max(0, this.grossIncome - this.totalDeductions);
    }

    /**
     * Used for display in the JList on the main UI.
     */
    @Override
    public String toString() {
        return profileName + " (" + financialYear + ")";
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getProfileName() { return profileName; }
    public String getProfileType() { return profileType; }
    public String getFinancialYear() { return financialYear; }
    public double getGrossIncome() { return grossIncome; }
    public double getTotalDeductions() { return totalDeductions; }
    public double getTaxableIncome() { return taxableIncome; }
    public double getTaxPaid() { return taxPaid; }
    public String getNotes() { return notes; }
    public Timestamp getDeletedOn() { return deletedOn; }

    // --- Setters (for editing) ---
    public void setProfileName(String profileName) { this.profileName = profileName; }
    public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }
    public void setGrossIncome(double grossIncome) { this.grossIncome = grossIncome; }
    public void setTotalDeductions(double totalDeductions) { this.totalDeductions = totalDeductions; }
    public void setTaxPaid(double taxPaid) { this.taxPaid = taxPaid; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setDeletedOn(Timestamp deletedOn) { this.deletedOn = deletedOn; }
    // Note: profileType and id should not be changed after creation
}