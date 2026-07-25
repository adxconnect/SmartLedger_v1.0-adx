package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Investment {
    private String id;
    private String investmentName;
    private String assetType;
    private String assetSymbol;
    private double quantity;
    private double amountInvested;
    private String investmentType;
    private String startDate;
    private String dateOfMaturity;
}
