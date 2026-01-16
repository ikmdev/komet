package dev.ikm.komet.app.test.integration.testfx.config;

import dev.ikm.komet.app.test.integration.testfx.utils.CredentialsReader;

/**
 * Central configuration class for DeX Authoring Process Tests.
 * Contains all constants, credentials, and test data used across test classes.
 */
public class TestConfiguration {

    // ==================== Test Configuration ====================
    public static boolean enableInfo = true;
    public static boolean enableSync = true;

    // ==================== Directory Configuration ====================
    
    protected static final String PROPERTY_TARGET_DATA_DIR = "target.data.directory";
    protected static final String PROPERTY_USER_HOME = "user.home";
    protected static final String BASE_DATA_DIR = System.getProperty(PROPERTY_TARGET_DATA_DIR,
            System.getProperty(PROPERTY_USER_HOME, System.getProperty("java.io.tmpdir")));
    protected static final String SOLOR_DIR = "Solor";
    protected static final String TEST_SCREENSHOTS_DIR = "test-screenshots-dex";
    protected static final String EXTENT_REPORTS_DIR = "extent-reports-dex";

    // ==================== Version Configuration ====================
    
    protected static final String PROPERTY_APP_VERSION = "komet.app.version";
    protected static final String APP_VERSION = System.getProperty(PROPERTY_APP_VERSION, "1.58.0-SNAPSHOT");

    // ==================== Data Source Configuration ====================
    
    protected static final String DATA_SOURCE_NAME = "SOLOR-GUDID";

    // ==================== Credentials Configuration ====================
    
    protected static final CredentialsReader credentialsReader = new CredentialsReader();
    
    protected static final String GITHUB_REPO_URL = credentialsReader.get("github_repo_url",
            "https://github.com/ikmdev/komet.git");
    protected static final String GITHUB_EMAIL = credentialsReader.get("github_email", "test@gmail.com");
    protected static final String GITHUB_USERNAME = credentialsReader.get("github_username", "KometTestUser");
    protected static final String GITHUB_PASSWORD = credentialsReader.get("github_password", "KometTestPassword123");
    
    protected static final String USERNAME = ("KOMET user");
    protected static final String PASSWORD = ("KOMET user");

    // ==================== Selectors ====================
    protected static final String SELECTOR_PROPERTIES_TOGGLE = "#propertiesToggleButton";
    protected static final String SELECTOR_ADD_REFERENCE_BUTTON = "#addReferenceButton";
    protected static final String SELECTOR_PUBLISH_BUTTON = "#savePatternButton";
    protected static final String SELECTOR_EDIT_DESCRIPTIONS_BUTTON = "#addDescriptionButton";
    protected static final String SELECTOR_EDIT_DEFINITIONS_BUTTON = "#addDefinitionButton";
    protected static final String SELECTOR_EDIT_FIELDS_BUTTON = "#addFieldsButton";
    protected static final String SELECTOR_SORT_BY_BUTTON = "#sortByButton";

    protected static final String SELECTOR_OK_BUTTON = "#okButton";

    protected static final String SELECTOR_GITHUB_STATUS = "#githubStatusHyperlink";
    protected static final String SELECTOR_LANDING_PAGE_BORDER_PANE = "#landingPageBorderPane";
    protected static final String SELECTOR_NEW_PROJECT_JOURNAL_BUTTON = "#newProjectJournalButton";
    protected static final String SELECTOR_IMPORT_BUTTON = "#importButton";

    protected static final String SELECTOR_USER_CHOOSER = "#Button";
    protected static final String SELECTOR_PASSWORD_FIELD = "#passwordField";
    protected static final String SELECTOR_SIGN_IN_BUTTON = "#loginButton";

    protected static final String SELECTOR_NEXTGEN_NAVIGATOR_BUTTON = "#navigatorToggleButton";
    protected static final String SELECTOR_NEXTGEN_SEARCH_BUTTON = "#nextGenSearchToggleButton";
    protected static final String SELECTOR_REASONER_BUTTON = "#reasonerToggleButton";
    protected static final String SELECTOR_SEARCH_BUTTON = "#searchToggleButton";
    protected static final String SELECTOR_NAVIGATOR_BUTTON = "#conceptNavigatorToggleButton";
    protected static final String SELECTOR_NEXTGEN_REASONER_BUTTON = "#nextGenReasonerToggleButton";
    protected static final String SELECTOR_SETTINGS_BUTTON = "#settingsToggleButton";
    protected static final String SELECTOR_CREATE_BUTTON = "#addButton";
    protected static final String SELECTOR_CONCEPTS_BUTTON = "#conceptsToggleButton";
    protected static final String SELECTOR_PATTERNS_BUTTON = "#patternsToggleButton";

    /**
     * Protected constructor to allow subclasses (test classes, page objects, workflows)
     * to extend this class and inherit all configuration constants.
     * Direct instantiation is still prevented by being protected.
     */
    protected TestConfiguration() {
        // Protected constructor - allows subclasses to inherit configuration
    }
}
