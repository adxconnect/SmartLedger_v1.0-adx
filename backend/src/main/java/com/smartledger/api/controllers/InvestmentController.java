package com.smartledger.api.controllers;

import com.smartledger.api.models.Investment;
import com.smartledger.api.services.InvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/investments")
@CrossOrigin(origins = "*")
public class InvestmentController {

    @Autowired
    private InvestmentService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Investment entity) {
        try {
            return ResponseEntity.ok(service.saveInvestment(entity));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Investment> getAll() throws ExecutionException, InterruptedException {
        return service.getAllInvestments();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.deleteInvestment(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting: " + e.getMessage());
        }
    }

}
