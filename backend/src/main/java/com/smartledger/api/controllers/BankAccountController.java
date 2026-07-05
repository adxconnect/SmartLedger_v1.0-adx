package com.smartledger.api.controllers;

import com.smartledger.api.models.BankAccount;
import com.smartledger.api.services.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/bankaccounts")
@CrossOrigin(origins = "*")
public class BankAccountController {

    @Autowired
    private BankAccountService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody BankAccount entity) {
        try {
            return ResponseEntity.ok(service.saveBankAccount(entity));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public List<BankAccount> getAll() throws ExecutionException, InterruptedException {
        return service.getAllBankAccounts();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.deleteBankAccount(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting: " + e.getMessage());
        }
    }

}
