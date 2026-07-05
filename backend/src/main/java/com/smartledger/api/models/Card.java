package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    private String id;
    private String uniqueId;
    private String cardName;
    private String cardType;
    private String cardNumber;
    private String validFrom;
    private String validThrough;
    private String cvv;
    private String frontImagePath;
    private String backImagePath;
    private double creditLimit;
    private double currentExpenses;
    private double amountToPay;
    private int daysLeftToPay;
    private String creationDate;

    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "**** **** **** " + (cardNumber != null ? cardNumber : "");
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
