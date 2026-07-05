package com.smartledger.api.controllers;
import com.smartledger.api.models.Account;
import com.smartledger.api.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired private AuthService authService;
    
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> payload) {
        try {
            Account acc = authService.signup(payload.get("name"), payload.get("email"), payload.get("password"));
            return ResponseEntity.ok(acc);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(400).body(err);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        try {
            Account acc = authService.login(payload.get("email"), payload.get("password"));
            return ResponseEntity.ok(acc);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(401).body(err);
        }
    }
}