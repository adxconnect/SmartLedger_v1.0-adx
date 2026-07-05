package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Investment {
    private String id;
    private String assetType;
    private String holderName;
    private String description;
    private String goal;
    private String startDate;
    private String accountDetails;
    private String tickerSymbol;
    private String exchange;
    private double quantity;
    private double initialUnitCost;
    private double currentUnitPrice;
    private String propertyAddress;
    private int tenureYears;
    private double interestRate;
}
