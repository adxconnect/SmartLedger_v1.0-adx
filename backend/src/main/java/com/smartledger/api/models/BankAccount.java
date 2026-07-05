package com.smartledger.api.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {
    private String id;
    private String accountNumber;
    private String holderName;
    private String bankName;
    private String ifscCode;
    private double balance;
    private String accountType;
    private double interestRate;
    private double annualExpense;
    private String accountSubtype;
    private String companyName;
    private String businessName;
}
