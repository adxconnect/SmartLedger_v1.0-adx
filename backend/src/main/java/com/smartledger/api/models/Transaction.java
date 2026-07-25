package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private String id; // Firestore uses String for Document IDs
    private String date;
    private String day;
    private String category;
    private String type; // "Debit", "Credit", "Interest"
    private double amount;
    private String description;
    private String paymentMethod;
    private String payee;
    private String accountId;
    private String accountType;
}
