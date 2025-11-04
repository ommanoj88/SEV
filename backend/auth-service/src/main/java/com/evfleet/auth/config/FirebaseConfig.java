package com.evfleet.auth.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Configuration
 * Initializes Firebase Admin SDK for authentication
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config.file:firebase-service-account.json}")
    private String firebaseConfigPath;

    @Value("${firebase.database.url:https://ev-fleet-management.firebaseio.com}")
    private String databaseUrl;

    /**
     * Initialize Firebase Admin SDK
     * This method runs after the bean is constructed
     */
    @PostConstruct
    public void initialize() {
        try {
            // Check if Firebase is already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                log.info("Initializing Firebase Admin SDK...");

                Resource resource = null;

                // Try to load from file system first (for Docker mounts)
                File file = new File(firebaseConfigPath);
                if (file.exists() && file.isFile()) {
                    log.info("Found Firebase config file in file system at: {}", firebaseConfigPath);
                    resource = new FileSystemResource(file);
                } else {
                    // Try to load from classpath
                    log.info("Trying to load Firebase config from classpath: {}", firebaseConfigPath);
                    resource = new ClassPathResource(firebaseConfigPath);
                }

                if (resource.exists()) {
                    // Load credentials from file
                    try (InputStream serviceAccount = resource.getInputStream()) {
                        FirebaseOptions options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                .setDatabaseUrl(databaseUrl)
                                .build();

                        FirebaseApp.initializeApp(options);
                        log.info("Firebase Admin SDK initialized successfully with config file");
                    }
                } else {
                    // Try to use Application Default Credentials (for GCP environments)
                    log.warn("Firebase config file not found at: {}. Attempting to use Application Default Credentials", firebaseConfigPath);

                    try {
                        FirebaseOptions options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.getApplicationDefault())
                                .setDatabaseUrl(databaseUrl)
                                .build();

                        FirebaseApp.initializeApp(options);
                        log.info("Firebase Admin SDK initialized successfully with Application Default Credentials");
                    } catch (IOException e) {
                        log.error("Failed to initialize Firebase with Application Default Credentials", e);
                        log.warn("Firebase Admin SDK not initialized. Authentication features will not work.");
                        log.warn("To enable Firebase, please provide a service account key file at: {}", firebaseConfigPath);
                    }
                }
            } else {
                log.info("Firebase Admin SDK already initialized");
            }
        } catch (IOException e) {
            log.error("Error initializing Firebase Admin SDK: {}", e.getMessage(), e);
            log.warn("Firebase authentication will not be available");
        } catch (Exception e) {
            log.error("Unexpected error during Firebase initialization: {}", e.getMessage(), e);
            log.warn("Firebase authentication may not work correctly");
        }
    }
}
