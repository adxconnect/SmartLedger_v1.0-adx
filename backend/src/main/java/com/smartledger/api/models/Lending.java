package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lending {
    private String id;
    private String borrowerName;
    private String loanType;
    private double principalAmount;
    private double interestRate;
    private int tenureMonths;
    private String dateLent;
    private String status;
    private String notes;
    private double monthlyPayment;
    private double totalInterestToReceive;
    private double totalToReceive;

    public void calculateLendingDetails() {
        if (principalAmount <= 0 || tenureMonths <= 0) {
            this.monthlyPayment = 0;
            this.totalToReceive = 0;
            this.totalInterestToReceive = 0;
            return;
        }
        if (interestRate == 0) {
            this.monthlyPayment = principalAmount / tenureMonths;
            this.totalToReceive = principalAmount;
            this.totalInterestToReceive = 0;
            return;
        }
        double r = (interestRate / 12.0) / 100.0;
        double n = tenureMonths;
        double emi = (principalAmount * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        this.monthlyPayment = emi;
        this.totalToReceive = emi * n;
        this.totalInterestToReceive = this.totalToReceive - this.principalAmount;
    }
}
