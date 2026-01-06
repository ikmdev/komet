package dev.ikm.komet.app.test.integration.testfx;

import dev.ikm.komet.app.App;
import dev.ikm.komet.app.AppState;
import dev.ikm.komet.app.test.integration.testfx.helpers.workflows.*;
import dev.ikm.komet.app.test.integration.testfx.pages.*;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.geometry.VerticalDirection;

import static java.nio.file.Files.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.util.WaitForAsyncUtils.waitFor;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
@Tag("dex-authoring")
@DisplayName("DeX Authoring Process Test")
public class DeXAuthoringProcessTest {

        private static final Logger LOG = LoggerFactory.getLogger(DeXAuthoringProcessTest.class);

        // Configuration constants
        private static final String PROPERTY_TARGET_DATA_DIR = "target.data.directory";
        private static final String PROPERTY_USER_HOME = "user.home";
        private static final String BASE_DATA_DIR = System.getProperty(PROPERTY_TARGET_DATA_DIR,
                                                        System.getProperty(PROPERTY_USER_HOME, System.getProperty("java.io.tmpdir")));
        private static final String SOLOR_DIR = "Solor";
        private static final String TEST_SCREENSHOTS_DIR = "test-screenshots-dex";
        private static final String EXTENT_REPORTS_DIR = "extent-reports-dex";

        // Version tracking
        private static final String PROPERTY_APP_VERSION = "komet.app.version";
        private static final String APP_VERSION = System.getProperty(PROPERTY_APP_VERSION, "1.58.0-SNAPSHOT");

        // Test data
        private static final String DATA_SOURCE_NAME = "SOLOR-GUDID";
        
        // Load credentials from CSV file
        private static final dev.ikm.komet.app.test.integration.testfx.utils.CredentialsReader credentialsReader = 
                new dev.ikm.komet.app.test.integration.testfx.utils.CredentialsReader();
        
        private static final String GITHUB_REPO_URL = credentialsReader.get("github_repo_url", "https://github.com/ikmdev/komet.git");
        private static final String GITHUB_EMAIL = credentialsReader.get("github_email", "test@gmail.com");
        private static final String GITHUB_USERNAME = credentialsReader.get("github_username", "KometTestUser");
        private static final String GITHUB_PASSWORD = credentialsReader.get("github_password", "KometTestPassword123");

        private static final String USERNAME = credentialsReader.get("komet_username", "KOMET user");
        private static final String PASSWORD = credentialsReader.get("komet_password", "KOMET user");

        // Concept Details
        String fullyQualifiedName;
        String status;
        String moduleName;
        String path;
        String parentConceptName;
        
        // Test Performed Semantic Data
        String analyteConcept = "Albumin";
        String targetConcept = "Serum";
        String testPerformedConcept = "Quantitative";
        String instrumentConcept = "COBAS";
        String specimenConcept = "Blood";
        String detectionLimit = "0.1";
        String exampleUcumUnits = "g/L";

        private Path screenshotDirectory;
        private Path extentReportsDirectory;
        private App webApp;
        private TestReporter reporter;
        private FxRobot robot;

        // Workflow helpers
        private GithubConnection githubConnection;
        private AuthorConcepts authorConcepts;
        private AddConceptToDexMembershipPattern addConceptToDexMembership;
        private Reasoner reasoner;
        private AddDeviceCompanySemantic addDeviceCompanySemantic;
        private AddAssociateDevicesSemantic addAssociateDevicesSemantic;
        private AddTestPerformedSemantic addTestPerformedSemantic;
        private AddAllowedResultsSemantic addAllowedResultsSemantic;
        private AddQuantitativeAllowedResultsSemantic addQuantitativeAllowedResultsSemantic;
        private AddPopulationReferenceRangeSemantic addPopulationReferenceRangeSemantic;

        @BeforeAll
        public void setUpClass() {
                overrideUserHome();
                createScreenshotDirectory();
                createExtentReportsDirectory();
                LOG.info("=== Starting DeX Authoring Process Test - Version {} ===", APP_VERSION);
        }

        @BeforeEach
        void setUp(FxRobot robot) throws Exception {
                this.robot = robot;
                FxToolkit.setupApplication(() -> {
                        webApp = new App();
                        return webApp;
                });

                reporter = new TestReporter(screenshotDirectory, extentReportsDirectory, robot,
                                "DeXAuthoringProcessTest");
                reporter.createTest("DeX Authoring Process Test - v" + APP_VERSION,
                                "Complete DeX authoring workflow with GitHub integration");

                // Initialize workflow helpers
                githubConnection = new GithubConnection(robot, reporter);
                authorConcepts = new AuthorConcepts(robot, reporter);
                addConceptToDexMembership = new AddConceptToDexMembershipPattern(robot, reporter);
                reasoner = new Reasoner(robot, reporter);
                addDeviceCompanySemantic = new AddDeviceCompanySemantic(robot, reporter);
                addAssociateDevicesSemantic = new AddAssociateDevicesSemantic(robot, reporter);
                addTestPerformedSemantic = new AddTestPerformedSemantic(robot, reporter);
                addAllowedResultsSemantic = new AddAllowedResultsSemantic(robot, reporter);
                addQuantitativeAllowedResultsSemantic = new AddQuantitativeAllowedResultsSemantic(robot, reporter);
                addPopulationReferenceRangeSemantic = new AddPopulationReferenceRangeSemantic(robot, reporter);
        }

        @AfterEach
        void tearDown() {
                try {
                        FxToolkit.cleanupStages();
                        if (webApp != null) {
                                FxToolkit.cleanupApplication(webApp);
                        }
                } catch (TimeoutException e) {
                        LOG.error("Timeout during teardown", e);
                } catch (Exception e) {
                        LOG.error("Unexpected error during teardown", e);
                }
        }

        @AfterAll
        public void cleanupAfterAll() {
                if (reporter != null) {
                        reporter.flush();
                }
                LOG.info("=== DeX Authoring Process Test Complete ===");
        }

        @Test
        @DisplayName("DeX Authoring Process - New Concept Creation")
        public void testDeXAuthoringProcess(FxRobot robot) throws TimeoutException, InterruptedException {
                LOG.info("Starting DeX Authoring Process Test");

                ConceptPane conceptPane = new ConceptPane(robot);
                LandingPage landingPage = new LandingPage(robot);
                DataSourceSelectionPage dataSourcePage = new DataSourceSelectionPage(robot);
                LoginPage loginPage = new LoginPage(robot);
                NavigatorPanel navigator = new NavigatorPanel(robot);
                GitHubConnectionPage gitHubConnectionPage = new GitHubConnectionPage(robot);

                // Step 1: Launch KOMET application with SOLOR-GUDID Dataset
                try {

                        reporter.logBeforeStep("Step 1: USER to LAUNCH Komet Application");
                        assertInitialAppState();
                        reporter.logAfterStep("Step 1: Application launched successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 1: USER to LAUNCH Komet Application", e);
                        throw e;
                }

                try {
                        // Select data source
                        reporter.logBeforeStep("Step 2: USER to SELECT SOLOR-GUDID from list");
                        dataSourcePage.selectDataSource(DATA_SOURCE_NAME);
                        reporter.logAfterStep("Step 2: Selected SOLOR-GUDID from list");
                } catch (Exception e) {
                        reporter.logFailure("Step 2: USER to SELECT SOLOR-GUDID from list", e);
                        throw e;
                }

                try {
                        // Click OK button
                        reporter.logBeforeStep("USER to CLICK OK button");
                        loginPage = dataSourcePage.clickOk();
                        reporter.logAfterStep("USER clicked OK button");
                } catch (Exception e) {
                        reporter.logFailure("USER to CLICK OK button", e);
                        throw e;
                }

                try {
                        // wait until the data is loaded and the "Welcome to Komet" screen is displayed
                        reporter.logBeforeStep("Waiting for data to load");
                        assertSelectUserState();
                        reporter.logAfterStep("Data loaded successfully");
                } catch (Exception e) {
                        reporter.logFailure("Waiting for data to load", e);
                        throw e;
                }

                try {
                        // Select KOMET User from dropdown
                        reporter.logBeforeStep("USER to SELECT KOMET User from dropdown");
                        loginPage.selectUser(USERNAME);
                        reporter.logAfterStep("USER selected KOMET User from dropdown");
                } catch (Exception e) {
                        reporter.logFailure("USER to SELECT KOMET User from dropdown", e);
                        throw e;
                }

                try {
                        // Input password
                        reporter.logBeforeStep("USER to INPUT password");
                        loginPage.enterPassword(PASSWORD);
                        reporter.logAfterStep("USER entered password");
                } catch (Exception e) {
                        reporter.logFailure("USER to INPUT password", e);
                        throw e;
                }

                try {
                        // Click SIGN IN button
                        reporter.logBeforeStep("USER to CLICK SIGN IN button");
                        landingPage = loginPage.clickSignIn();
                        reporter.logAfterStep("USER clicked SIGN IN button");
                } catch (Exception e) {
                        reporter.logFailure("USER to CLICK SIGN IN button", e);
                        throw e;
                }

                try {
                        // Wait for running state
                        reporter.logBeforeStep("Waiting for application running state");
                        assertRunningAppState();
                        landingPage.maximizeWindow();
                        reporter.logAfterStep("Application in running state and maximized");
                } catch (Exception e) {
                        reporter.logFailure("Application running state", e);
                        throw e;
                }

                
                
                // ========== Steps 2-6: Connect to GitHub Repository ==========
                githubConnection.connectToGitHub(GITHUB_REPO_URL, GITHUB_EMAIL, GITHUB_USERNAME, GITHUB_PASSWORD);

                // ========== Steps 7-33: Create Concepts ==========
                // Create Target concept
                authorConcepts.createConcept("Albumin", "Active", "Device Extension Module", "Development path", "Target");
                addConceptToDexMembership.addConceptToDexMembershipPattern("Albumin", "Device Extension Membership Pattern", "Active", "Device Extension Module", "Development path");

                // 34. Repeat for all necessary DeX concepts (Target, Analyte, Company/Device Labeler)
                //create Analyte concept
                authorConcepts.createConcept("Albumin", "Active", "Device Extension Module", "Development path", "Analyte");
                addConceptToDexMembership.addConceptToDexMembershipPattern("Albumin", "Device Extension Membership Pattern", "Active", "Device Extension Module", "Development path");

                // Create Company concept
                authorConcepts.createConcept("Roche Diagnostics GmbH", "Active", "Device Extension Module", "Development path", "Company");
                addConceptToDexMembership.addConceptToDexMembershipPattern("Roche Diagnostics GmbH", "Device Extension Membership Pattern", "Active", "Device Extension Module", "Development path");

                //LOG.info("✓ Concept Creation: PASSED");
                
                

                // ========== DeX Data Generation ==========
                LOG.info("===== DeX Data Generation =====");

                String suppliedBrandName = "Albumin Gen2 5166861190";
                String indentifiedDevice = "Roche Diagnostics COBAS Integra Albumin Gen.2";

                // Open a new journal for DeX data entry
                try {
                        reporter.logBeforeStep("Open a new journal for DeX data entry");
                        landingPage.clickNewProjectJournal();
                        reporter.logAfterStep("Opened a new journal for DeX data entry successfully");
                } catch (Exception e) {
                        reporter.logFailure("Open a new journal for DeX data entry", e);
                        throw e;
                }

                // Step 35: Search for the identified device by the supplied brand name + version/model for the DeX record you are creating
                try {
                        reporter.logBeforeStep("Step 35: Search for the identified device by the supplied brand name + version/model for the DeX record you are creating");
                        navigator.clickNextgenSearch();
                        navigator.nextgenSearch(suppliedBrandName);
                        navigator.openNextGenSearchResult(suppliedBrandName);
                        //navigator.openNextGenSearchResult(indentifiedDevice);
                        conceptPane.clickCopyButton();
                        reporter.logAfterStep("Step 35: Searched for the identified device successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 35: Search for the identified device by the supplied brand name + version/model for the DeX record you are creating", e);
                        throw e;
                }

                
                // Step 36: Add the Device Name from the Device Extension Data as an 'Other Name'
                try {
                        reporter.logBeforeStep("Step 36: Add the Device Name from the Device Extension Data as an 'Other Name'");
                        conceptPane.clickEditDescriptionsButton();
                        conceptPane.updateName(indentifiedDevice);
                        conceptPane.updateModule("Device Extension Module");
                        conceptPane.submit();
                        reporter.logAfterStep("Step 36: Added the Device Name from the Device Extension Data as an 'Other Name' successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 36: Add the Device Name from the Device Extension Data as an 'Other Name'", e);
                        throw e;
                }
                
                
                // Step 49: Run Reasoner
                try{
                        reporter.logBeforeStep("Step 49: Run Reasoner");
                        reasoner.runReasoner();     
                        reporter.logAfterStep("Step 49: Reasoner run successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 49: Run Reasoner", e);
                        throw e;
                }


                // Step 50: Click Info
                try {
                        reporter.logBeforeStep("Step 50: Click 'Info'");
                        robot.clickOn("Info");
                        waitForFxEvents();
                        reporter.logAfterStep("Step 50: Clicked 'Info' successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 50: Click 'Info'", e);
                        throw e;
                }

                // Step 51: Click Sync
                try {
                        reporter.logBeforeStep("Step 51: Click 'Sync'");
                        robot.clickOn("Sync");
                        waitForFxEvents();
                        reporter.logAfterStep("Step 51: Clicked 'Sync' successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 51: Click 'Sync'", e);
                        throw e;
                }

                LOG.info("✓ DeX Data Generation: PASSED");

                

                // ========== Steps 52-62: Add DeX Device Company Semantic ==========
                
                addDeviceCompanySemantic.addDeviceCompanySemantic("Device Company Pattern", "Active", "Device Extension Module", 
                                                    "Development path", "Albumin Gen2 5166861190", "Roche Diagnostics GmbH");

                LOG.info("✓ Add DeX Device Company Semantic: PASSED");

                // ========== Steps 63-71: Add DeX Associated Devices Semantic ==========

                // Add Associated Devices semantic (with multiple device identifiers)
                String[] associatedDeviceIdentifiers = new String[]{"Device1", "Device2", "Device3"};

                addAssociateDevicesSemantic.addAssociatedDevicesSemantic("Associated Devices Pattern", "Active", "Device Extension Module", 
                                                    "Development path", "Roche Diagnostics COBAS Integra Albumin Gen.2", associatedDeviceIdentifiers);

                LOG.info("✓ Add DeX Associated Devices Semantic: PASSED");

                // ========== Steps 72-87: Add DeX Test Performed Semantic ==========

                String[] specimens = new String[]{specimenConcept};
                addTestPerformedSemantic.addTestPerformedSemantic("Test Performed Pattern", "Active", "Device Extension Module",
                        "Development path", analyteConcept, targetConcept, testPerformedConcept, instrumentConcept, 
                        specimens, detectionLimit, exampleUcumUnits);
                        
                LOG.info("✓ Add DeX Test Performed Semantic: PASSED");

                // ========== Steps 88-97: Add DeX Allowed Results Semantic ==========

                String[] qualifierConcepts = new String[]{"Less than", "Greater than", "Equal to"};
                addAllowedResultsSemantic.addAllowedResultsSemantic("Allowed Results Pattern", "Allowed Results Pattern", 
                        "Active", "Device Extension Module", "Development path", qualifierConcepts);

                LOG.info("✓ Add DeX Allowed Results Semantic: PASSED");

                // ========== Steps 98-110: Add Dex Quantitative Allowed Results Range Semantic ==========

                addQuantitativeAllowedResultsSemantic.addQuantitativeAllowedResultsSemantic("Quantitative Allowed Results Pattern", 
                        "Active", "Device Extension Module", "Development path",
                        "<=", ">=", "10.0", "1.0", "g/L");

                LOG.info("✓ Add DeX Quantitative Allowed Results Semantic: PASSED");

                // ========== Steps 111-123: Add Dex Population Reference Range Semantic ==========

                addPopulationReferenceRangeSemantic.addPopulationReferenceRangeSemantic("Population Reference Range Pattern", 
                        "Active", "Device Extension Module", "Development path",
                        "Adult", "<=", ">=", "10.0", "1.0", "g/L");

                LOG.info("✓ Add DeX Population Reference Range Semantic: PASSED");


                //TODO: 
                //Ensure semantics behave as expected
                //Run test in its entirety to ensure stability
                //Fix any issues that arise during full test run

                //Issues: 
                //When running the full test, window stops responding (when clicking patterns and when creating new journal)

        }

        // ========== Helper Methods ==========

        private void assertInitialAppState() throws TimeoutException {
                waitFor(10, TimeUnit.SECONDS, () -> App.state.get() == AppState.SELECT_DATA_SOURCE);
                assertEquals(AppState.SELECT_DATA_SOURCE, App.state.get(),
                                "Application should be in SELECT_DATA_SOURCE state");
        }

        private void assertSelectUserState() throws TimeoutException {
                waitFor(60, TimeUnit.SECONDS, () -> {
                        AppState current = App.state.get();
                        System.out.println("Waiting for data load, current state: " + current);
                        return current == AppState.SELECT_USER || current == AppState.RUNNING;
                });
                AppState currentState = App.state.get();
                assertTrue(currentState == AppState.SELECT_USER || currentState == AppState.RUNNING,
                                "Application should be in SELECT_USER or RUNNING state after data load, but was: "
                                                + currentState);
        }

        private void assertRunningAppState() throws TimeoutException {
                System.out.println("Current app state before wait: " + App.state.get());
                waitFor(60, TimeUnit.SECONDS, () -> {
                        AppState current = App.state.get();
                        System.out.println("Waiting for RUNNING, current state: " + current);
                        return current == AppState.RUNNING;
                });
                assertEquals(AppState.RUNNING, App.state.get(),
                                "Application should be in RUNNING state");
        }

        private void overrideUserHome() {
                try {
                        Path targetDataPath = Paths.get(BASE_DATA_DIR, SOLOR_DIR);
                        if (!exists(targetDataPath)) {
                                createDirectories(targetDataPath);
                                LOG.info("Created data directory: {}", targetDataPath);
                        }
                        System.setProperty(PROPERTY_USER_HOME, targetDataPath.getParent().toString());
                } catch (Exception e) {
                        LOG.error("Failed to override user home directory", e);
                }
        }

        private void createScreenshotDirectory() {
                try {
                        screenshotDirectory = Paths.get(BASE_DATA_DIR, SOLOR_DIR, TEST_SCREENSHOTS_DIR);
                        if (!exists(screenshotDirectory)) {
                                createDirectories(screenshotDirectory);
                                LOG.info("Created screenshot directory: {}", screenshotDirectory);
                        }
                } catch (Exception e) {
                        LOG.error("Failed to create screenshot directory", e);
                }
        }

        private void createExtentReportsDirectory() {
                try {
                        extentReportsDirectory = Paths.get(BASE_DATA_DIR, SOLOR_DIR, EXTENT_REPORTS_DIR);
                        if (!exists(extentReportsDirectory)) {
                                createDirectories(extentReportsDirectory);
                                LOG.info("Created extent reports directory: {}", extentReportsDirectory);
                        }
                } catch (Exception e) {
                        LOG.error("Failed to create extent reports directory", e);
                }
        }

        private void horizontalScroll(FxRobot robot, KeyCode direction, int scrollAmount) {
                try {
                        reporter.logBeforeStep("Scroll horizontally");
                        for (int i = 0; i < scrollAmount; i++) {
                                robot.press(direction);
                                robot.release(direction);
                        }
                        reporter.logAfterStep("Scrolled horizontally");
                } catch (Exception e) {
                        reporter.logFailure("Scroll horizontally", e);
                        throw e;
                }
        }

        private void verticalScroll(KeyCode direction, int scrollAmount) {
                try {
                        reporter.logBeforeStep("Scroll vertically");
                        for (int i = 0; i < scrollAmount; i++) {
                                robot.press(direction);
                                robot.release(direction);
                        }
                        reporter.logAfterStep("Scrolled vertically");
                } catch (Exception e) {
                        reporter.logFailure("Scroll vertically", e);
                        throw e;
                }
        }

        /**
        * Waits for a specified duration.
        */
        public void waitForDelay(int milliseconds) {
                try {
                Thread.sleep(milliseconds);
                waitForFxEvents();
                } catch (InterruptedException e) {
                LOG.error("Interrupted during wait", e);
                }
        }
}
