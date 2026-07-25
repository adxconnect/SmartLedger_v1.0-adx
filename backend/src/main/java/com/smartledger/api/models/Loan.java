package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    private String id;
    private String accountName;
    private String accountNumber;
    private String loanType; // Personal, Car, Home, Gold, Education
    private String provider; // Government Schemes, Bank
    private double principalAmount;
    private double interestRate;
    private String interestType; // Day, Quarter, Monthly, Annually
    private int tenure;
    private String startDate;
    private String status;
    private String notes;
    private double emiAmount;
    private double totalInterest;
    private double totalPayment;

    public void calculateLoanDetails() {
        if (principalAmount <= 0 || tenure <= 0) {
            this.emiAmount = 0;
            this.totalPayment = 0;
            this.totalInterest = 0;
            return;
        }
        if (interestRate == 0) {
            this.emiAmount = principalAmount / tenure;
            this.totalPayment = principalAmount;
            this.totalInterest = 0;
            return;
        }
        
        double r = interestRate / 100.0;
        if ("Annually".equalsIgnoreCase(interestType)) {
            r = r / 12.0; 
        }
        double n = tenure;
        double emi = (principalAmount * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        this.emiAmount = emi;
        this.totalPayment = emi * n;
        this.totalInterest = this.totalPayment - this.principalAmount;
    }
}
