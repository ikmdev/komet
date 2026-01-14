package dev.ikm.komet.app.test.integration.testfx.config;

import dev.ikm.komet.app.test.integration.testfx.utils.CredentialsReader;

/**
 * Central configuration class for DeX Authoring Process Tests.
 * Contains all constants, credentials, and test data used across test classes.
 */
public class TestConfiguration {

    // ==================== Directory Configuration ====================
    
    public static final String PROPERTY_TARGET_DATA_DIR = "target.data.directory";
    public static final String PROPERTY_USER_HOME = "user.home";
    public static final String BASE_DATA_DIR = System.getProperty(PROPERTY_TARGET_DATA_DIR,
            System.getProperty(PROPERTY_USER_HOME, System.getProperty("java.io.tmpdir")));
    public static final String SOLOR_DIR = "Solor";
    public static final String TEST_SCREENSHOTS_DIR = "test-screenshots-dex";
    public static final String EXTENT_REPORTS_DIR = "extent-reports-dex";

    // ==================== Version Configuration ====================
    
    public static final String PROPERTY_APP_VERSION = "komet.app.version";
    public static final String APP_VERSION = System.getProperty(PROPERTY_APP_VERSION, "1.58.0-SNAPSHOT");

    // ==================== Data Source Configuration ====================
    
    public static final String DATA_SOURCE_NAME = "SOLOR-GUDID";

    // ==================== Credentials Configuration ====================
    
    private static final CredentialsReader credentialsReader = new CredentialsReader();
    
    public static final String GITHUB_REPO_URL = credentialsReader.get("github_repo_url",
            "https://github.com/ikmdev/komet.git");
    public static final String GITHUB_EMAIL = credentialsReader.get("github_email", "test@gmail.com");
    public static final String GITHUB_USERNAME = credentialsReader.get("github_username", "KometTestUser");
    public static final String GITHUB_PASSWORD = credentialsReader.get("github_password", "KometTestPassword123");
    
    public static final String USERNAME = credentialsReader.get("komet_username", "KOMET user");
    public static final String PASSWORD = credentialsReader.get("komet_password", "KOMET user");

    // Private constructor to prevent instantiation
    private TestConfiguration() {
        throw new AssertionError("TestConfiguration should not be instantiated");
    }
}
