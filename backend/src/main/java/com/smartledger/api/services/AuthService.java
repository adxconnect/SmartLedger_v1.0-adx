package com.smartledger.api.services;
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
    
    public Account getAccountById(String id) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("accounts").document(id).get().get();
        if (doc.exists()) {
            Account acc = doc.toObject(Account.class);
            if (acc != null) {
                acc.setPasswordHash(null);
                acc.setPasswordSalt(null);
                acc.setId(doc.getId());
                return acc;
            }
        }
        return null;
    }
}