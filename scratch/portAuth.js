const fs = require('fs');
const path = require('path');

const base = 'c:\\Users\\softl\\OneDrive\\Desktop\\SmartLedger_v1.0-adx-main\\backend\\src\\main\\java\\com\\smartledger\\api';

fs.mkdirSync(path.join(base, 'utils'), { recursive: true });

const accountModel = `package com.smartledger.api.models;
import lombok.Data;
@Data
public class Account {
    private String id;
    private String accountName;
    private String email;
    private String passwordHash;
    private String passwordSalt;
}`;

const passwordHasher = `package com.smartledger.api.utils;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    private static final int ITERATIONS = 65_536;
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(char[] password, String salt) {
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        PBEKeySpec spec = new PBEKeySpec(password, saltBytes, ITERATIONS, HASH_BYTES * 8);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("Error while hashing password", ex);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verifyPassword(char[] password, String salt, String expectedHash) {
        String hashed = hashPassword(password, salt);
        return slowEquals(hashed, expectedHash);
    }

    private static boolean slowEquals(String a, String b) {
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();
        int diff = aBytes.length ^ bBytes.length;
        for (int i = 0; i < aBytes.length && i < bBytes.length; i++) {
            diff |= aBytes[i] ^ bBytes[i];
        }
        return diff == 0;
    }
}`;

const authService = `package com.smartledger.api.services;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.smartledger.api.models.Account;
import com.smartledger.api.utils.PasswordHasher;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public Account signup(String name, String email, String password) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        if (!db.collection("accounts").whereEqualTo("email", email).get().get().isEmpty()) {
            throw new RuntimeException("An account with this email already exists.");
        }
        
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password.toCharArray(), salt);
        
        Account acc = new Account();
        acc.setAccountName(name);
        acc.setEmail(email);
        acc.setPasswordSalt(salt);
        acc.setPasswordHash(hash);
        
        DocumentReference docRef = db.collection("accounts").document();
        acc.setId(docRef.getId());
        docRef.set(acc).get();
        
        acc.setPasswordHash(null);
        acc.setPasswordSalt(null);
        return acc;
    }
    
    public Account login(String email, String password) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        QuerySnapshot query = db.collection("accounts").whereEqualTo("email", email).get().get();
        if (query.isEmpty()) throw new RuntimeException("Invalid email or password.");
        
        Account acc = query.getDocuments().get(0).toObject(Account.class);
        if (!PasswordHasher.verifyPassword(password.toCharArray(), acc.getPasswordSalt(), acc.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password.");
        }
        
        acc.setPasswordHash(null);
        acc.setPasswordSalt(null);
        return acc;
    }
}`;

const authController = `package com.smartledger.api.controllers;
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
}`;

fs.writeFileSync(path.join(base, 'models', 'Account.java'), accountModel);
fs.writeFileSync(path.join(base, 'utils', 'PasswordHasher.java'), passwordHasher);
fs.writeFileSync(path.join(base, 'services', 'AuthService.java'), authService);
fs.writeFileSync(path.join(base, 'controllers', 'AuthController.java'), authController);
console.log('Successfully ported Auth components to Spring Boot API');
