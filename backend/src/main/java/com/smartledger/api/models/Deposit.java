package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deposit {
    private String id;
    private String depositType;
    private String holderName;
    private String description;
    private String goal;
    private String creationDate;
    private String accountNumber;
    private double principalAmount;
    private double monthlyAmount;
    private double interestRate;
    private int tenure;
    private String tenureUnit;
    private String startDate;
    private double currentTotal;
    private String lastUpdated;
    private double gullakDueAmount;

    public double calculateFDMaturityAmount() {
        if (!"FD".equals(depositType) || interestRate <= 0 || principalAmount <= 0 || tenure <= 0 || startDate == null) {
            return principalAmount;
        }
        try {
            double rate = interestRate / 100.0;
            double years = convertTenureToYears();
            return principalAmount * (1 + rate * years);
        } catch (Exception e) {
            return principalAmount;
        }
    }

    public double calculateRDMaturityAmount() {
         if (!"RD".equals(depositType) || interestRate <= 0 || monthlyAmount <= 0 || tenure <= 0 || !"Months".equals(tenureUnit)) {
             return ("Months".equals(tenureUnit)) ? monthlyAmount * tenure : 0;
         }
        try {
            double P = monthlyAmount;
            int n = tenure;
            double r = interestRate / 100.0;
            double i = r / 12.0;
            return P * ((Math.pow(1 + i, n) - 1) / i);
        } catch (Exception e) {
             return ("Months".equals(tenureUnit)) ? monthlyAmount * tenure : 0;
        }
    }

    public String calculateMaturityDate() {
        if (startDate == null || startDate.isEmpty() || tenure <= 0 || tenureUnit == null) {
            return "N/A";
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = start;
            switch (tenureUnit) {
                case "Days": end = start.plusDays(tenure); break;
                case "Months": end = start.plusMonths(tenure); break;
                case "Years": end = start.plusYears(tenure); break;
                default: return "Invalid Unit";
            }
            return end.format(formatter);
        } catch (Exception e) {
            return "Error";
        }
    }

    private double convertTenureToYears() {
        if (tenure <= 0 || tenureUnit == null) return 0.0;
        switch (tenureUnit) {
            case "Days": return (double)tenure / 365.0;
            case "Months": return (double)tenure / 12.0;
            case "Years": return (double)tenure;
            default: return 0.0;
        }
    }
}
