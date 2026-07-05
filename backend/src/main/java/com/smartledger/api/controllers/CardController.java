package com.smartledger.api.controllers;

import com.smartledger.api.models.Card;
import com.smartledger.api.services.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*")
public class CardController {

    @Autowired
    private CardService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Card entity) {
        try {
            return ResponseEntity.ok(service.saveCard(entity));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Card> getAll() throws ExecutionException, InterruptedException {
        return service.getAllCards();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.deleteCard(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting: " + e.getMessage());
        }
    }

}
