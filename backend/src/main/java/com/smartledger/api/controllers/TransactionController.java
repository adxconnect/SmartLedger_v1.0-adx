package com.smartledger.api.controllers;

import com.smartledger.api.models.Transaction;
import com.smartledger.api.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*") // Allows the React frontend to make requests
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<String> createTransaction(@RequestBody Transaction transaction) {
        try {
            return ResponseEntity.ok(transactionService.saveTransaction(transaction));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage() + " | Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "none"));
        }
    }

    @GetMapping
    public List<Transaction> getAllTransactions() throws ExecutionException, InterruptedException {
        return transactionService.getAllTransactions();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        try {
            return ResponseEntity.ok(transactionService.deleteTransaction(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting: " + e.getMessage());
        }
    }

}
