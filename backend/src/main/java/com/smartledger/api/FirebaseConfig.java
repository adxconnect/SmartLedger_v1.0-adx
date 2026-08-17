package com.smartledger.api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() {
        System.out.println("========== INITIALIZING FIREBASE ==========");
        try {
            // Support Render.com secret files or local classpath
            InputStream serviceAccount;
            java.io.File renderSecretFile = new java.io.File("/etc/secrets/serviceAccountKey.json");
            
            if (renderSecretFile.exists()) {
                serviceAccount = new java.io.FileInputStream(renderSecretFile);
                System.out.println("Using Firebase key from Render Secret File.");
            } else {
                serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");
                System.out.println("Using Firebase key from classpath.");
            }

            if (serviceAccount == null) {
                System.err.println("CRITICAL ERROR: Firebase Service Account Key not found in resources!");
                throw new RuntimeException("Firebase Key Missing! Make sure serviceAccountKey.json is in src/main/resources");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("========== FIREBASE HAS BEEN INITIALIZED SUCCESSFULLY! ==========");
            }
        } catch (Exception e) {
            System.err.println("FIREBASE INITIALIZATION FAILED: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
