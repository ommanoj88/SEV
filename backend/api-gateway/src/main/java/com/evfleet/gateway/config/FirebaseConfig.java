package com.evfleet.gateway.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Firebase Configuration
 * Initializes Firebase Admin SDK for token validation
 * If credentials file is not found, Firebase initialization is skipped
 * and the service operates in mock/development mode
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    private static final String CREDENTIALS_FILE = "firebase-credentials.json";

    @PostConstruct
    public void initializeFirebase() {
        try {
            File credentialsFile = new File(CREDENTIALS_FILE);
            
            // Check if credentials file exists
            if (!credentialsFile.exists()) {
                log.warn("Firebase credentials file not found at: {}", CREDENTIALS_FILE);
                log.warn("Firebase authentication will be disabled. Please provide {} for production use.", CREDENTIALS_FILE);
                return;
            }

            FileInputStream serviceAccount = new FileInputStream(credentialsFile);

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("✓ Firebase initialized successfully");
            } else {
                log.info("Firebase already initialized");
            }
        } catch (IOException e) {
            log.warn("Failed to initialize Firebase: {}", e.getMessage());
            log.warn("Firebase authentication will be disabled. Ensure {} is in classpath.", CREDENTIALS_FILE);
            log.warn("For production, add Firebase service account credentials");
        } catch (Exception e) {
            log.error("Unexpected error initializing Firebase: {}", e.getMessage(), e);
        }
    }
}
