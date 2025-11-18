package com.evfleet.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Configuration for EVFleet Monolith
 *
 * Initializes Firebase Admin SDK for authentication and other Firebase services.
 * Supports both file system and classpath-based service account configuration.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config.path:firebase-service-account.json}")
    private String firebaseConfigPath;

    /**
     * Initialize Firebase Admin SDK
     * Attempts to load configuration from file system first, then classpath
     */
    @PostConstruct
    public void initialize() {
        try {
            // Check if FirebaseApp is already initialized
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase Admin SDK already initialized");
                return;
            }

            InputStream serviceAccount = getServiceAccountStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized successfully");

        } catch (IOException e) {
            log.warn("Failed to initialize Firebase Admin SDK - Firebase features will be disabled", e);
            log.warn("To enable Firebase, provide firebase-service-account.json in the classpath or configure firebase.config.path");
        }
    }

    /**
     * Get service account input stream
     * Tries file system first, then classpath
     */
    private InputStream getServiceAccountStream() throws IOException {
        // Try file system first
        File configFile = new File(firebaseConfigPath);
        if (configFile.exists()) {
            log.info("Loading Firebase config from file system: {}", firebaseConfigPath);
            return new FileInputStream(configFile);
        }

        // Fall back to classpath
        log.info("Loading Firebase config from classpath: {}", firebaseConfigPath);
        return new ClassPathResource(firebaseConfigPath).getInputStream();
    }

    /**
     * Get Firebase App instance
     * Returns null if Firebase is not initialized
     */
    @Bean
    public FirebaseApp firebaseApp() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }
        } catch (IllegalStateException e) {
            log.warn("Firebase not initialized - returning null");
        }
        return null;
    }
}
