package dev.ikm.komet.app.test.integration.testfx.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to read test credentials from a CSV file.
 * CSV file format: key,value (e.g., github_username,myusername)
 */
public class CredentialsReader {
    
    private static final Logger LOG = LoggerFactory.getLogger(CredentialsReader.class);
    private static final String DEFAULT_CREDENTIALS_FILE = "test-credentials.csv";
    
    private final Map<String, String> credentials;
    
    public CredentialsReader() {
        this(getDefaultCredentialsPath());
    }
    
    public CredentialsReader(String filePath) {
        this.credentials = new HashMap<>();
        loadCredentials(filePath);
    }
    
    private static String getDefaultCredentialsPath() {
        // Look for credentials file in user's home directory under Solor folder
        Path credentialsPath = Paths.get(System.getProperty("user.home"), "Solor", DEFAULT_CREDENTIALS_FILE);
        return credentialsPath.toString();
    }
    
    private void loadCredentials(String filePath) {
        LOG.info("Attempting to load credentials from: {}", filePath);
        
        // Check if file exists
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            LOG.warn("Credentials file does not exist: {}", filePath);
            LOG.warn("Using default credential values");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    credentials.put(key, value);
                    LOG.info("Loaded credential [line {}]: {} = ***", lineNumber, key);
                } else {
                    LOG.warn("Invalid format at line {}: {}", lineNumber, line);
                }
            }
            LOG.info("Successfully loaded {} credentials from: {}", credentials.size(), filePath);
        } catch (IOException e) {
            LOG.error("Error reading credentials file: {}", filePath, e);
            LOG.warn("Using default credential values");
        }
    }
    
    /**
     * Gets a credential value by key.
     * @param key The credential key
     * @param defaultValue The default value if key not found
     * @return The credential value or default
     */
    public String get(String key, String defaultValue) {
        return credentials.getOrDefault(key, defaultValue);
    }
    
    /**
     * Gets a credential value by key.
     * @param key The credential key
     * @return The credential value or null if not found
     */
    public String get(String key) {
        return credentials.get(key);
    }
    
    /**
     * Checks if a credential exists.
     * @param key The credential key
     * @return true if the credential exists
     */
    public boolean has(String key) {
        return credentials.containsKey(key);
    }
}
