package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    private String id;
    private String lenderName;
    private String loanType;
    private double principalAmount;
    private double interestRate;
    private int tenureMonths;
    private String startDate;
    private String status;
    private String notes;
    private double emiAmount;
    private double totalInterest;
    private double totalPayment;

    public void calculateLoanDetails() {
        if (principalAmount <= 0 || tenureMonths <= 0) {
            this.emiAmount = 0;
            this.totalPayment = 0;
            this.totalInterest = 0;
            return;
        }
        if (interestRate == 0) {
            this.emiAmount = principalAmount / tenureMonths;
            this.totalPayment = principalAmount;
            this.totalInterest = 0;
            return;
        }
        double r = (interestRate / 12.0) / 100.0;
        double n = tenureMonths;
        double emi = (principalAmount * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        this.emiAmount = emi;
        this.totalPayment = emi * n;
        this.totalInterest = this.totalPayment - this.principalAmount;
    }
}
