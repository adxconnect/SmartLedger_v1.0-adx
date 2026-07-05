package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MutualFund {
    private String id;
    private double amount;
    private double expectedRate;
    private int years;
}
