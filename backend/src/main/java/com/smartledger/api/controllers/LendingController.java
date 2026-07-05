package com.smartledger.api.controllers;

import com.smartledger.api.models.Lending;
import com.smartledger.api.services.LendingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/lendings")
@CrossOrigin(origins = "*")
public class LendingController {

    @Autowired
    private LendingService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Lending entity) {
        try {
            return ResponseEntity.ok(service.saveLending(entity));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Lending> getAll() throws ExecutionException, InterruptedException {
        return service.getAllLendings();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.deleteLending(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting: " + e.getMessage());
        }
    }

}
