package com.talkkia.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-file:classpath:possible-point-327508-firebase-adminsdk-9gohw-3902500a66.json}")
    private String serviceAccountFile;

    @PostConstruct
    public void initializeFirebase() {
        List<FirebaseApp> apps = FirebaseApp.getApps();
        log.info("Firebase init start. configuredSource='{}', existingApps={}", serviceAccountFile, apps.size());

        if (!apps.isEmpty()) {
            log.info("Skip Firebase init because default app already exists.");
            return;
        }

        try {
            try (InputStream serviceAccount = getServiceAccountInputStream()) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
            }
            log.info("Firebase SDK initialized successfully. source={}", serviceAccountFile);
        } catch (Exception e) {
            log.error("Failed to initialize Firebase SDK. configuredSource='{}', env.FIREBASE_SERVICE_ACCOUNT_FILE='{}'",
                    serviceAccountFile, System.getenv("FIREBASE_SERVICE_ACCOUNT_FILE"), e);
            throw new IllegalStateException("Firebase SDK init failed", e);
        }
    }

    private InputStream getServiceAccountInputStream() throws Exception {
        String source = serviceAccountFile == null ? "" : serviceAccountFile.trim();
        if (source.isEmpty()) {
            throw new IllegalStateException("firebase.service-account-file is empty");
        }

        if (source.startsWith("classpath:")) {
            String classpathFile = source.substring("classpath:".length());
            ClassPathResource resource = new ClassPathResource(classpathFile);
            log.info("Trying Firebase credentials from classpath: path='{}', exists={}",
                    classpathFile, resource.exists());
            return resource.getInputStream();
        }

        Path filePath = Paths.get(source);
        log.info("Trying Firebase credentials from file: path='{}', exists={}, readable={}",
                filePath, Files.exists(filePath), Files.isReadable(filePath));
        return new FileInputStream(source);
    }
}
