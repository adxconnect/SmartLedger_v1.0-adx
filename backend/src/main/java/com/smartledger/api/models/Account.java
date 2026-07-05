package com.smartledger.api.models;
import lombok.Data;
@Data
public class Account {
    private String id;
    private String accountName;
    private String email;
    private String passwordHash;
    private String passwordSalt;
}