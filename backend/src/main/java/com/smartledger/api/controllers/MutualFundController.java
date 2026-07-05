package com.smartledger.api.controllers;

import com.smartledger.api.models.MutualFund;
import com.smartledger.api.services.MutualFundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/mutualfunds")
@CrossOrigin(origins = "*")
public class MutualFundController {

    @Autowired
    private MutualFundService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody MutualFund entity) {
        try {
            return ResponseEntity.ok(service.saveMutualFund(entity));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public List<MutualFund> getAll() throws ExecutionException, InterruptedException {
        return service.getAllMutualFunds();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.deleteMutualFund(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting: " + e.getMessage());
        }
    }

}
