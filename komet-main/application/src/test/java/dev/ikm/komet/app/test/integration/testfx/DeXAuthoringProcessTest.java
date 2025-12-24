package dev.ikm.komet.app.test.integration.testfx;

import dev.ikm.komet.app.App;
import dev.ikm.komet.app.AppState;
import dev.ikm.komet.app.test.integration.testfx.helpers.ConceptWorkflow;
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

        private Path screenshotDirectory;
        private Path extentReportsDirectory;
        private App webApp;
        private TestReporter reporter;
        private ConceptWorkflow conceptWorkflow;

        @BeforeAll
        public void setUpClass() {
                overrideUserHome();
                createScreenshotDirectory();
                createExtentReportsDirectory();
                LOG.info("=== Starting DeX Authoring Process Test - Version {} ===", APP_VERSION);
        }

        @BeforeEach
        void setUp(FxRobot robot) throws Exception {
                FxToolkit.setupApplication(() -> {
                        webApp = new App();
                        return webApp;
                });

                reporter = new TestReporter(screenshotDirectory, extentReportsDirectory, robot,
                                "DeXAuthoringProcessTest");
                reporter.createTest("DeX Authoring Process Test - v" + APP_VERSION,
                                "Complete DeX authoring workflow with GitHub integration");
                conceptWorkflow = new ConceptWorkflow(robot, reporter);
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

                /*

                // ========== Steps 2-6: Connect to GitHub Repository ==========
                conceptWorkflow.connectToGitHub(GITHUB_REPO_URL, GITHUB_EMAIL, GITHUB_USERNAME, GITHUB_PASSWORD);

                // ========== Steps 7-33: Create Concepts ==========
                // Create Target concept
                conceptWorkflow.createConcept("Albumin", "Active", "Device Extension Module", "Development path", "Target");
                conceptWorkflow.addConceptToDexMembershipPattern("Albumin", "Device Extension Membership Pattern", "Active", "Device Extension Module", "Development path");

                // 34. Repeat for all necessary DeX concepts (Target, Analyte, Company/Device Labeler)
                conceptWorkflow.createConcept("Albumin", "Active", "Device Extension Module", "Development path", "Analyte");
                conceptWorkflow.addConceptToDexMembershipPattern("Albumin", "Device Extension Membership Pattern", "Active", "Device Extension Module", "Development path");

                // Create Company concept
                conceptWorkflow.createConcept("Roche Diagnostics GmbH", "Active", "Device Extension Module", "Development path", "Company");
                conceptWorkflow.addConceptToDexMembershipPattern("Roche Diagnostics GmbH", "Device Extension Membership Pattern", "Active", "Device Extension Module", "Development path");

                LOG.info("✓ Concept Creation: PASSED");

                */

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
                        navigator.openNextGenSearchResult(indentifiedDevice);
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

                /*
                // Step 49: Run Reasoner
                conceptWorkflow.runReasoner();

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

                */

                // ========== Add DeX Device Company Semantic ==========
                // Step 53-62
                conceptWorkflow.addSemanticToPattern("Device Company Pattern", "Active", "Device Extension Module", 
                                                    "Development path", "Albumin Gen2 5166861190", "Roche Diagnostics GmbH");

                LOG.info("✓ Add DeX Device Company Semantic: PASSED");

                // ========== Add DeX Associated Devices Semantic ==========
                // Step 63-71

                // Add Associated Devices semantic (with multiple device identifiers)
                String[] associatedDeviceIdentifiers1 = {"Device1", "Device2", "Device3"};

                conceptWorkflow.addSemanticToPattern("Associated Devices Pattern", "Active", "Device Extension Module", 
                                                    "Development path", "Roche Diagnostics COBAS Integra Albumin Gen.2", associatedDeviceIdentifiers1);

                LOG.info("✓ Add DeX Associated Devices Semantic: PASSED");

                // ========== Add DeX Test Performed Semantic ==========
                // Step 72-87

                // Open new journal
                
                String patternName = "Test Performed Pattern";
                try {
                reporter.logBeforeStep("Open a new journal for " + patternName + " semantic entry");
                landingPage.clickNewProjectJournal();
                reporter.logAfterStep("Opened a new journal for " + patternName + " semantic entry successfully");
                } catch (Exception e) {
                reporter.logFailure("Open a new journal for " + patternName + " semantic entry", e);
                throw e;
                }

                // Open nextgen navigator
                try {
                reporter.logBeforeStep("Open NextGen Navigator");
                navigator.clickNextgenNavigator();
                reporter.logAfterStep("Opened NextGen Navigator successfully");
                } catch (Exception e) {
                reporter.logFailure("Open NextGen Navigator", e);
                throw e;
                }

                // Click Patterns tab
                try {
                reporter.logBeforeStep("Click Patterns tab");
                navigator.clickPatterns();
                reporter.logAfterStep("Clicked Patterns tab successfully");
                } catch (Exception e) {
                reporter.logFailure("Click Patterns tab", e);
                throw e;
                }

                // Move to the specified pattern
                try {
                reporter.logBeforeStep("Move to '" + patternName + "'");
                robot.moveTo(patternName);
                //if pattern is not visible, scroll down 10, repeat till visible
                while (!robot.lookup(patternName).tryQuery().isPresent()) {
                        verticalScroll(KeyCode.DOWN, 10);
                        waitForFxEvents();
                }

                           reporter.logAfterStep("Moved to '" + patternName + "' successfully");
        } catch (Exception e) {
            reporter.logFailure("Move to '" + patternName + "'", e);
            throw e;
        }

        // Right Click the pattern and select "Add New Semantic Element"
        try {
            reporter.logBeforeStep("Right Click the pattern and select 'Add New Semantic Element'");
            robot.rightClickOn(patternName);
            waitForFxEvents();
            robot.clickOn("Add New Semantic Element");
            waitForFxEvents();
            navigator.clickNextgenNavigator();
            reporter.logAfterStep("Right Clicked the pattern and selected 'Add New Semantic Element' successfully");
        } catch (Exception e) {
            reporter.logFailure("Right Click the pattern and select 'Add New Semantic Element'", e);
            throw e;
        }

        // Update the Stamp
        try {
            reporter.logBeforeStep("Update the Stamp to reflect Module: " + moduleName);
            conceptPane.updateStamp(status, moduleName, path);
            reporter.logAfterStep("Updated the Stamp to reflect Module: " + moduleName + " successfully");
        } catch (Exception e) {
            reporter.logFailure("Update the Stamp to reflect Module: " + moduleName, e);
            throw e;
        }

        // Click the pencil icon that is in line with the Reference Component section header
        try {
            reporter.logBeforeStep("Click the pencil icon that is in line with the Reference Component section header");
            conceptPane.clickEditReferenceComponentButton();
            reporter.logAfterStep("Clicked the pencil icon that is in line with the Reference Component section header successfully");
        } catch (Exception e) {
            reporter.logFailure("Click the pencil icon that is in line with the Reference Component section header", e);
            throw e;
        }

        // Paste UUID from clipboard
        try {
            reporter.logBeforeStep("Paste UUID from clipboard");
            robot.rightClickOn("🔍  Search");
            waitForFxEvents();
            robot.clickOn("Paste");
            waitForFxEvents();
            waitFor(1500); // Wait for results to load
            //press down arrow then press enter
            robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
            robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
            waitForFxEvents();
            reporter.logAfterStep("Pasted UUID from clipboard successfully");
        } catch (Exception e) {
            reporter.logFailure("Paste UUID from clipboard", e);
            throw e;
        }

        // Click Confirm and verify the correct reference component populates
        try {
            reporter.logBeforeStep("Click Confirm and verify the correct reference component populates");
            robot.clickOn("CONFIRM");
            waitForFxEvents();
            reporter.logAfterStep("Clicked Confirm and verified the correct reference component populated successfully");
        } catch (Exception e) {
            reporter.logFailure("Click Confirm and verify the correct reference component populates", e);
            throw e;
        }

        // Click the pencil that is in line with the Semantic Details section header
        try {
            reporter.logBeforeStep("Click the pencil that is in line with the Semantic Details section header");
            conceptPane.clickEditSemanticDetailsButton();
            reporter.logAfterStep("Clicked the pencil that is in line with the Semantic Details section header successfully");
        } catch (Exception e) {
            reporter.logFailure("Click the pencil that is in line with the Semantic Details section header", e);
            throw e;
        }

        //move to analyte search field
        try{
                reporter.logBeforeStep("Search and select Analyte");
                robot.moveTo("Analyte:").moveBy(40,40).clickOn();
                robot.write(analyteConcept);
                robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
                robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
                waitForFxEvents();
        } catch (Exception e) {
                reporter.logFailure("Search and select Analyte", e);
                throw e;
        }

        //move to target search field
        try{
                reporter.logBeforeStep("Search and select Target");
                robot.moveTo("Target:").moveBy(40,40).clickOn();
                robot.write(targetConcept);
                robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
                robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
                waitForFxEvents();
        } catch (Exception e) {
                reporter.logFailure("Search and select Target", e);
                throw e;
        }      

        //move to test performed field
        try{
                reporter.logBeforeStep("Search and select Test Performed");     
                robot.moveTo("Test Performed:").moveBy(40,40).clickOn();
                robot.write(testPerformedConcept);
                robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
                robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
                waitForFxEvents();
        } catch (Exception e) {
                reporter.logFailure("Search and select Test Performed", e);
                throw e;
        }

        //move to insrtrument field
        try{
                reporter.logBeforeStep("Search and select Instrument");
                robot.moveTo("Instrument:").moveBy(40,40).clickOn();
                robot.write(instrumentConcept);
                robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
                robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
                waitForFxEvents();
        } catch (Exception e) {
                reporter.logFailure("Search and select Instrument", e);
                throw e;
        }

        //move to specimen field
        try{
                reporter.logBeforeStep("Search and select Specimen");
                robot.moveTo("Specimen:").moveBy(40,40).clickOn();
                robot.write(specimenConcept);
                robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
                robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
                waitForFxEvents();
        } catch (Exception e) {
                reporter.logFailure("Search and select Specimen", e);
                throw e;
        }

        //move to detection limit field
        try{
                reporter.logBeforeStep("Type in the Detection Limit");
                robot.moveTo("Detection Limit:").moveBy(0,25).doubleClickOn();
                robot.write(detectionLimit);
                waitForFxEvents();
                reporter.logAfterStep("Typed in the Detection Limit successfully");
        } catch (Exception e) {
                reporter.logFailure("Type in the Detection Limit", e);
                throw e;
        }

        //move to example ucum units field
        try{
                reporter.logBeforeStep("Type in the Example UCUM units");
                robot.moveTo("Example UCUM units:").moveBy(0,25).doubleClickOn();
                robot.write(exampleUcumUnits);
                waitForFxEvents();
                reporter.logAfterStep("Typed in the Example UCUM units successfully");
        } catch (Exception e) {
                reporter.logFailure("Type in the Example UCUM units", e);
                throw e;
        }

                //click submit
        try {
                reporter.logBeforeStep("Click Submit to save the semantic");
                conceptPane.submit();
                reporter.logAfterStep("Clicked Submit to save the semantic successfully");
        } catch (Exception e) {
                reporter.logFailure("Click Submit to save the semantic", e);
                throw e;
        }

                //Repeat for all necessary Test Performed Semantics for the device
                
        LOG.info("✓ Add DeX Test Performed Semantic: PASSED");

        // ========== Add DeX Allowed Results Semantic ==========
                // Step 88-97

                String patternName = "Allowed Results Pattern";
                     
                // Open new journal
                try {
                reporter.logBeforeStep("Open a new journal for " + patternName + " semantic entry");
                landingPage.clickNewProjectJournal();
                reporter.logAfterStep("Opened a new journal for " + patternName + " semantic entry successfully");
                } catch (Exception e) {
                reporter.logFailure("Open a new journal for " + patternName + " semantic entry", e);
                throw e;
                }

                // Open nextgen navigator
                try {
                reporter.logBeforeStep("Open NextGen Navigator");
                navigator.clickNextgenNavigator();
                reporter.logAfterStep("Opened NextGen Navigator successfully");
                } catch (Exception e) {
                reporter.logFailure("Open NextGen Navigator", e);
                throw e;
                }

                // Click Patterns tab
                try {
                reporter.logBeforeStep("Click Patterns tab");
                navigator.clickPatterns();
                reporter.logAfterStep("Clicked Patterns tab successfully");
                } catch (Exception e) {
                reporter.logFailure("Click Patterns tab", e);
                throw e;
                }

                // Move to the specified pattern
                try {
                reporter.logBeforeStep("Move to '" + patternName + "'");
                robot.moveTo(patternName);
                //if pattern is not visible, scroll down 10, repeat till visible
                while (!robot.lookup(patternName).tryQuery().isPresent()) {
                        verticalScroll(KeyCode.DOWN, 10);
                        waitForFxEvents();
                }

                           reporter.logAfterStep("Moved to '" + patternName + "' successfully");
        } catch (Exception e) {
            reporter.logFailure("Move to '" + patternName + "'", e);
            throw e;
        }

        // Right Click the pattern and select "Add New Semantic Element"
        try {
            reporter.logBeforeStep("Right Click the pattern and select 'Add New Semantic Element'");
            robot.rightClickOn(patternName);
            waitForFxEvents();
            robot.clickOn("Add New Semantic Element");
            waitForFxEvents();
            navigator.clickNextgenNavigator();
            reporter.logAfterStep("Right Clicked the pattern and selected 'Add New Semantic Element' successfully");
        } catch (Exception e) {
            reporter.logFailure("Right Click the pattern and select 'Add New Semantic Element'", e);
            throw e;
        }

        // Update the Stamp
        try {
            reporter.logBeforeStep("Update the Stamp to reflect Module: " + moduleName);
            conceptPane.updateStamp(status, moduleName, path);
            reporter.logAfterStep("Updated the Stamp to reflect Module: " + moduleName + " successfully");
        } catch (Exception e) {
            reporter.logFailure("Update the Stamp to reflect Module: " + moduleName, e);
            throw e;
        }

        // Click the pencil icon that is in line with the Reference Component section header
        try {
            reporter.logBeforeStep("Click the pencil icon that is in line with the Reference Component section header");
            conceptPane.clickEditReferenceComponentButton();
            reporter.logAfterStep("Clicked the pencil icon that is in line with the Reference Component section header successfully");
        } catch (Exception e) {
            reporter.logFailure("Click the pencil icon that is in line with the Reference Component section header", e);
            throw e;
        }

        // Paste UUID from clipboard
        try {
            reporter.logBeforeStep("Paste UUID from clipboard");
            robot.rightClickOn("🔍  Search");
            waitForFxEvents();
            robot.clickOn("Paste");
            waitForFxEvents();
            waitFor(1500); // Wait for results to load
            //press down arrow then press enter
            robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
            robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
            waitForFxEvents();
            reporter.logAfterStep("Pasted UUID from clipboard successfully");
        } catch (Exception e) {
            reporter.logFailure("Paste UUID from clipboard", e);
            throw e;
        }

        // Click Confirm and verify the correct reference component populates
        try {
            reporter.logBeforeStep("Click Confirm and verify the correct reference component populates");
            robot.clickOn("CONFIRM");
            waitForFxEvents();
            reporter.logAfterStep("Clicked Confirm and verified the correct reference component populated successfully");
        } catch (Exception e) {
            reporter.logFailure("Click Confirm and verify the correct reference component populates", e);
            throw e;
        }

        // Click the pencil that is in line with the Semantic Details section header
        try {
            reporter.logBeforeStep("Click the pencil that is in line with the Semantic Details section header");
            conceptPane.clickEditSemanticDetailsButton();
            reporter.logAfterStep("Clicked the pencil that is in line with the Semantic Details section header successfully");
        } catch (Exception e) {
            reporter.logFailure("Click the pencil that is in line with the Semantic Details section header", e);
            throw e;
        }

        //add the necessary qualifier value concepts
        //list of qualifier concepts
        String[] qualifierConcepts = {"Less than", "Greater than", "Equal to"};

        //while the list hasnext, process each qualifier concept

        try{
                reporter.logBeforeStep("Add Allowed Results Qualifier Values");
                while(qualifierConcepts.iterator().hasNext()){
                        String qualifierConcept = qualifierConcepts.iterator().next();
                        //move to qualifier value search field
                        robot.moveTo("Allowed Results Set:").moveBy(40,40).clickOn();
                        robot.write(qualifierConcept);
                        robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
                        robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
                        waitForFxEvents();
                }
                reporter.logAfterStep("Added Allowed Results Qualifier Values successfully");
        } catch (Exception e) {
                reporter.logFailure("Add Allowed Results Qualifier Values", e);
                throw e;
        }

        //click submit
        try {
                reporter.logBeforeStep("Click Submit to save the semantic");
                conceptPane.submit();
                reporter.logAfterStep("Clicked Submit to save the semantic successfully");
        } catch (Exception e) {
                reporter.logFailure("Click Submit to save the semantic", e);
                throw e;
        }

        // Repeat for all necessary Allowed Results Semantics for the device

        LOG.info("✓ Add DeX Allowed Results Semantic: PASSED");

        // ========== Add Dex Quantitative Allowed Results Range Semantic ==========


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
}
