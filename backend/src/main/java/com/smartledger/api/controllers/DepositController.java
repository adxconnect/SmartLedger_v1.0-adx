package com.smartledger.api.controllers;

import com.smartledger.api.models.Deposit;
import com.smartledger.api.services.DepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/deposits")
@CrossOrigin(origins = "*")
public class DepositController {

    @Autowired
    private DepositService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Deposit entity) {
        try {
            return ResponseEntity.ok(service.saveDeposit(entity));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Deposit> getAll() throws ExecutionException, InterruptedException {
        return service.getAllDeposits();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.deleteDeposit(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting: " + e.getMessage());
        }
    }

}
