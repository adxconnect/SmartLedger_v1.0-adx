package com.smartledger.api.controllers;

import com.smartledger.api.models.TaxProfile;
import com.smartledger.api.services.TaxProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/taxprofiles")
@CrossOrigin(origins = "*")
public class TaxProfileController {

    @Autowired
    private TaxProfileService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody TaxProfile entity) {
        try {
            return ResponseEntity.ok(service.saveTaxProfile(entity));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public List<TaxProfile> getAll() throws ExecutionException, InterruptedException {
        return service.getAllTaxProfiles();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.deleteTaxProfile(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting: " + e.getMessage());
        }
    }

}
