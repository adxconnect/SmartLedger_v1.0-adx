package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoldSilverInvestment {
    private String id;
    private String type;
    private double weight;
    private double pricePerGram;
}
