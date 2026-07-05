package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxProfile {
    private String id;
    private String profileName;
    private String profileType;
    private String financialYear;
    private double grossIncome;
    private double totalDeductions;
    private double taxableIncome;
    private double taxPaid;
    private String notes;
    private String deletedOn;

    public void calculateTaxableIncome() {
        this.taxableIncome = Math.max(0, this.grossIncome - this.totalDeductions);
    }
}
