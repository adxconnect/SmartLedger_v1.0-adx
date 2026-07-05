package com.smartledger.api.controllers;

import com.smartledger.api.models.GoldSilverInvestment;
import com.smartledger.api.services.GoldSilverInvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/goldsilverinvestments")
@CrossOrigin(origins = "*")
public class GoldSilverInvestmentController {

    @Autowired
    private GoldSilverInvestmentService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody GoldSilverInvestment entity) {
        try {
            return ResponseEntity.ok(service.saveGoldSilverInvestment(entity));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public List<GoldSilverInvestment> getAll() throws ExecutionException, InterruptedException {
        return service.getAllGoldSilverInvestments();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.deleteGoldSilverInvestment(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting: " + e.getMessage());
        }
    }

}
