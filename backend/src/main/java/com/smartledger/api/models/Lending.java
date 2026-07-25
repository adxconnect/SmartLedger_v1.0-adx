package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lending {
    private String id;
    private String lenderUid;
    private String lenderName;
    private String borrowerUid;
    private String borrowerName;
    private String loanType;
    private double principalAmount;
    private double interestRate;
    private String interestType; // Day, Month, Quarter, Annual
    private int tenure;
    private String dateLent;
    private String dateOfClosing;
    private String status;
    private String notes;
    private double monthlyPayment;
    private double totalInterestToReceive;
    private double totalToReceive;

    public void calculateLendingDetails() {
        if (principalAmount <= 0 || tenure <= 0) {
            this.monthlyPayment = 0;
            this.totalToReceive = 0;
            this.totalInterestToReceive = 0;
            return;
        }
        if (interestRate == 0) {
            this.monthlyPayment = principalAmount / tenure;
            this.totalToReceive = principalAmount;
            this.totalInterestToReceive = 0;
            return;
        }
        // Since interest calculation might vary by type, we'll keep it simple
        // assuming interestRate is per year if Annual, per month if Month, etc.
        // But to keep EMI formula working without breaking backward compatibility:
        double r = interestRate / 100.0;
        if ("Annual".equalsIgnoreCase(interestType)) {
            r = r / 12.0; // Standard EMI uses monthly rate
        }
        double n = tenure;
        double emi = (principalAmount * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        this.monthlyPayment = emi;
        this.totalToReceive = emi * n;
        this.totalInterestToReceive = this.totalToReceive - this.principalAmount;
    }
}
