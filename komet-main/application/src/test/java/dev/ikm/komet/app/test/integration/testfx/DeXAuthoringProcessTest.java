package dev.ikm.komet.app.test.integration.testfx;

import dev.ikm.komet.app.App;
import dev.ikm.komet.app.AppState;
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
        private static final String EXTENT_REPORTS_DIR = "extent-reports";

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

        //concept details
        String fullyQualifiedName = "Albumin";
        String status = "Active";
        String moduleName = "Device Extension Module";
        String path = "Development path";
        String parentConceptName = "Target";

        private Path screenshotDirectory;
        private Path extentReportsDirectory;
        private App webApp;
        private TestReporter reporter;

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
        @DisplayName("Complete DeX Authoring Process - New Concept Creation")
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
                 // Step 2: In the tool bar, click Exchange
                 try {
                        reporter.logBeforeStep("Step 2: CLICK 'Exchange'");
                        robot.clickOn("Exchange");
                        waitForFxEvents();
                        reporter.logAfterStep("Step 2: CLICK 'Exchange' successful");
                 } catch (Exception e) {
                        reporter.logFailure("Step 2: CLICK 'Exchange'", e);
                        throw e;
                 }
                 
                 // Step 3: Click Info
                 try {
                        reporter.logBeforeStep("Step 3: CLICK 'Info'");
                        robot.clickOn("Info");
                        waitForFxEvents();
                        reporter.logAfterStep("Step 3: CLICK 'Info' successful");
                 } catch (Exception e) {
                        reporter.logFailure("Step 3: CLICK 'Info'", e);
                        throw e;
                 }
                 
                 // Step 4: Input the repo url and the user's github account info
                 try {
                        reporter.logBeforeStep("Step 4: INPUT Repo URL and GitHub Credentials");
                        gitHubConnectionPage.enterGitHubCredentials(GITHUB_REPO_URL, GITHUB_EMAIL, GITHUB_USERNAME, GITHUB_PASSWORD);
                        reporter.logAfterStep("Step 4: INPUT Repo URL and GitHub Credentials successful");
                 } catch (Exception e) {
                        reporter.logFailure("Step 4: INPUT Repo URL and GitHub Credentials", e);
                        throw e;
                 }
                 
                  // Step 5: Click Connect
                  try {
                        reporter.logBeforeStep("Step 5: CLICK 'Connect'");
                        robot.clickOn("CONNECT");
                        waitForFxEvents();
                        reporter.logAfterStep("Step 5: CLICK 'Connect' successful");
                  } catch (Exception e) {
                        reporter.logFailure("Step 5: CLICK 'Connect'", e);
                        throw e;
                  }
                  
                  // Step 6: Wait for connection to establish and close the GitHub Info popup
                  try {
                        reporter.logBeforeStep("Step 6: Wait for GitHub connection and close 'GitHub Info' popup");
                        
                        // Wait for "GitHub Info" popup to appear (indicates successful connection)
                        boolean popupFound = false;
                        for (int i = 0; i < 30; i++) {
                              waitForFxEvents();
                              try {
                                    // Look for "GitHub Info" text in the scene
                                    if (robot.lookup("GitHub Info").tryQuery().isPresent()) {
                                          popupFound = true;
                                          LOG.info("GitHub Info popup appeared after {} attempts", i + 1);
                                          break;
                                    }
                              } catch (Exception e) {
                                    // Popup not found yet, continue waiting
                              }
                              Thread.sleep(1000);
                        }
                        
                        if (!popupFound) {
                              throw new RuntimeException("GitHub Info popup did not appear within 15 seconds");
                        }
                        
                        waitForFxEvents();
                        
                        // Click CLOSE button to close the popup - use moveTo first then click
                        robot.clickOn();
                        waitForFxEvents();
                        robot.moveTo("CLOSE");
                        waitForFxEvents();
                        robot.clickOn();
                        waitForFxEvents();
                        
                        reporter.logAfterStep("Step 6: GitHub connection verified - 'GitHub Info' popup closed successfully");
                  } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOG.error("Interrupted while waiting for GitHub connection", e);
                        reporter.logFailure("Step 6: Wait for GitHub connection", e);
                        throw new RuntimeException("Interrupted while waiting for GitHub connection", e);
                  } catch (Exception e) {
                        reporter.logFailure("Step 6: Wait for GitHub connection and close popup", e);
                        throw e;
                 }

                 */
        
                // Steps 7-33: Create the first concept with specified details
                createConcept(robot, fullyQualifiedName, status, moduleName, path, parentConceptName);

                // 34. Repeat for all necessary DeX concepts (Target, Analyte, Company/Device
                createConcept(robot, "Albumin", "Active", "Device Extension Module", "Development path", "Target");
                createConcept(robot, "Albumin", "Active", "Device Extension Module", "Development path", "Company/Device Labeler");


                LOG.info("✓ DeX Authoring Process Test: PASSED");

        }

        /**
         * Creates a new concept with the specified details (Steps 7-33).
         * This method encapsulates the complete workflow for creating a new concept including:
         * - Creating a project journal
         * - Setting up concept with stamp information
         * - Adding fully qualified name
         * - Configuring axioms and parent concept
         * - Creating semantic element
         * - Setting up reference component
         * - Returning to landing page for next concept
         *
         * @param robot FxRobot instance for UI interactions
         * @param fullyQualifiedName The fully qualified name for the concept
         * @param status The status for the concept (e.g., "Active")
         * @param moduleName The module name (e.g., "Device Extension Module")
         * @param path The path name (e.g., "Development path")
         * @param parentConceptName The parent concept name to search for and link
         * @throws InterruptedException if thread is interrupted during execution
         */
        private void createConcept(FxRobot robot, String fullyQualifiedName, String status, 
                                  String moduleName, String path, String parentConceptName) 
                throws InterruptedException {
                
                ConceptPane conceptPane = new ConceptPane(robot);
                LandingPage landingPage = new LandingPage(robot);
                NavigatorPanel navigator = new NavigatorPanel(robot);

                // 7. Open an existing project journal or create a new one
                try {
                        reporter.logBeforeStep("Step 7: CLICK 'Create Project Journal'");
                        landingPage.clickNewProjectJournal();
                        reporter.logAfterStep("Step 7: CLICK 'Create Project Journal' successful");
                } catch (Exception e) {
                        reporter.logFailure("Step 7: CLICK 'Create Project Journal'", e);
                        throw e;
                }

                // 8. Click the "+" icon in the journal window toolbar
                try {
                        reporter.logBeforeStep("Step 8: CLICK '+' Icon in Journal Toolbar");
                        navigator.clickCreate();
                        reporter.logAfterStep("Step 8: CLICK '+' Icon in Journal Toolbar successful");
                } catch (Exception e) {
                        reporter.logFailure("Step 8: CLICK '+' Icon in Journal Toolbar", e);
                        throw e;
                }

                // 9. Click New Concept
                try {
                        reporter.logBeforeStep("Step 9: CLICK 'New Concept'");
                        robot.clickOn("New Concept");
                        reporter.logAfterStep("Step 9: CLICK 'New Concept' successful");
                } catch (Exception e) {
                        reporter.logFailure("Step 9: CLICK 'New Concept'", e);
                        throw e;
                }

                // 10. Update stamp information:
                // - Module: Device Extension Module
                // - Path: Development Path
                try {
                        reporter.logAfterStep("Step 10: Update Stamp Information");
                        conceptPane.openStampEditor();
                        conceptPane.updateStamp(status, moduleName, path);
                        reporter.logAfterStep("Step 10: Updated Stamp Information successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 10: Update Stamp Information", e);
                        throw e;
                }

                // 11. Click the Properties Panel toggle in the top right of the window
                try {
                        reporter.logBeforeStep("Step 11: Click the Properties Panel toggle");
                        conceptPane.clickPropertiesToggle();
                        reporter.logAfterStep("Step 11: Clicked the Properties Panel toggle");
                } catch (Exception e) {
                        reporter.logFailure("Step 11: Click the Properties Panel toggle", e);
                        throw e;
                }

                // 12. Add the Fully Qualified Name information
                // - Module: Device Extension Module
                try {
                        reporter.logBeforeStep("Step 12: Update Fully Qualified Name");
                        conceptPane.updateFullyQualifiedName(fullyQualifiedName);
                        conceptPane.updateModule(moduleName);
                        reporter.logAfterStep("Step 12: Updated Fully Qualified Name successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 12: Update Fully Qualified Name", e);
                        throw e;
                }

                // 13. Click Submit
                try {
                        reporter.logBeforeStep("Step 13: USER to CLICK Submit button");
                        conceptPane.submit();
                        reporter.logAfterStep("Step 13: Clicked Submit button");
                } catch (Exception e) {
                        reporter.logFailure("Step 13: USER to CLICK Submit button", e);
                        throw e;
                }

                /*
                // Scroll horizontally to the left to reveal the pencil icon
                robot.clickOn(250, 250);
                horizontalScroll(robot, KeyCode.LEFT, 20);
                waitForFxEvents();
                */

                // 14. Click the pencil icon that is in line with the axioms section header
                try {
                        reporter.logBeforeStep("Step 14: Click Axioms Pencil Icon");
                        conceptPane.clickEditAxiomsButton();
                        reporter.logAfterStep("Step 14: Clicked Axioms Pencil Icon");
                } catch (Exception e) {
                        reporter.logFailure("Step 14: Click Axioms Pencil Icon", e);
                        throw e;
                }
                // 15. Click "Add Necessary Set"
                try {
                        reporter.logBeforeStep("Step 15: Click Add Necessary Set");
                        robot.clickOn("Add Necessary Set");
                        waitForFxEvents();
                        reporter.logAfterStep("Step 15  : Clicked Add Necessary Set");
                } catch (Exception e) {
                        reporter.logFailure("Step 15: Click Add Necessary Set", e);
                        throw e;
                }

                /*
                // scroll down
                        robot.scroll(5, VerticalDirection.DOWN);
                        waitForFxEvents();
                */

                // 16. Click the pencil icon aligned to the third axiom row
                try {
                        reporter.logBeforeStep("Step 16: Click pencil icon following 'Anonymous concept'");
                        // Move to "Anonymous concept" text then move right to find the button
                        robot.moveTo("[Anonymous concept] ");
                        robot.moveBy(425, 0); // Move 425 pixels to the right to find the button
                        robot.clickOn();
                        reporter.logAfterStep("Step 16: Clicked pencil icon following 'Anonymous concept'");
                } catch (Exception e) {
                        reporter.logFailure("Step 16: Click pencil icon following 'Anonymous concept'", e);
                        throw e;
                }

                // 17. Click "Remove axiom"
                try {
                        reporter.logBeforeStep("Step 17: Click Remove Axiom");
                        robot.clickOn("Remove axiom");
                        reporter.logAfterStep("Step 17  : Clicked Remove Axiom");
                } catch (Exception e) {
                        reporter.logFailure("Step 17    : Click Remove Axiom", e);
                        throw e;
                }

                // 18. Click the pencil icon aligned to the second axiom row
                try {
                        reporter.logBeforeStep("Step 18: Click pencil icon aligned to second axiom row");
                        // Move to "Necessary set:" text then move right to find the button
                        robot.moveTo("Necessary set: " + fullyQualifiedName);
                        robot.moveBy(450, 0); // Move 450 pixels to the right to find the button
                        robot.clickOn();
                        reporter.logAfterStep("Step 18: Clicked pencil icon aligned to second axiom row");
                } catch (Exception e) {
                        reporter.logFailure("Step 18: Click pencil icon aligned to second axiom row", e);
                        throw e;
                }

                // 19. Click "add is-a"
                try {
                        reporter.logBeforeStep("Step 19: Click Add Is-A");
                        robot.clickOn("Add is-a");
                        reporter.logAfterStep("Step 19: Clicked Add Is-A");
                } catch (Exception e) {
                        reporter.logFailure("Step 19: Click Add Is-A", e);
                        throw e;
                }

                // 20. Click the pencil icon aligned to the third axiom row
                try {
                        reporter.logBeforeStep("Step 20: Click pencil icon alligned to third axiom row");
                        // Move to "Anonymous concept" text then move right to find the button
                        robot.moveTo("Anonymous concept");
                        robot.moveBy(445, 0); // Move 445 pixels to the right to find the button
                        robot.clickOn();
                        reporter.logAfterStep("Step 20: Clicked pencil icon alligned to third axiom row");
                } catch (Exception e) {
                        reporter.logFailure("Step 20: Click pencil icon alligned to third axiom row", e);
                        throw e;
                }

                // 21. Click search for concept
                try {
                        reporter.logBeforeStep("Step 21: Click Search for Concept");
                        conceptPane.clickSearchForConcept();
                        reporter.logAfterStep("Step 21: Clicked Search for Concept");
                } catch (Exception e) {
                        reporter.logFailure("Step 21: Click Search for Concept", e);
                        throw e;
                }

                // 22. Search for parent concept using the search panel
                try {
                        reporter.logBeforeStep("Step 22: Search for Parent Concept");
                        conceptPane.searchForParentConcept(parentConceptName);
                        reporter.logAfterStep("Step 22: Searched for Parent Concept successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 22: Search for Parent Concept", e);
                        throw e;
                }

                // 23. When the required parent concept is located, double click to submit
                try {
                        reporter.logBeforeStep("Step 23: Locate parent concept and double click to select");
                        conceptPane.selectParentConcept(parentConceptName);
                        reporter.logAfterStep(
                                        "Step 23: Located parent concept and double clicked to select successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 23: Locate parent concept and double click to select  ", e);
                        throw e;
                }


                // 24. In the identifier row, copy the UUID by utilizing the copy shortcut
                try {
                        reporter.logBeforeStep("Step 24: Copy UUID from Identifier Row");
                        conceptPane.clickCopyButton();
                        reporter.logAfterStep("Step 24: Copied UUID from Identifier Row successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 24: Copy UUID from Identifier Row", e);
                        throw e;
                }

                // 25. Open the Navigator by selecting the hierarchy icon in the left panel
                try {
                        reporter.logBeforeStep("Step 25: Open Navigator");
                        navigator.clickNextgenNavigator();
                        reporter.logAfterStep("Step 25: Opened Navigator successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 25: Open Navigator", e);
                        throw e;
                }

                // 26. Switch the view to patterns by clicking on the Patterns text at the top
                try {
                        reporter.logBeforeStep("Step 26: Switch to Patterns View");
                        navigator.clickPatterns();
                        reporter.logAfterStep("Step 26: Switched to Patterns View successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 26: Switch to Patterns View", e);
                        throw e;
                }

                // 27. Locate the Device Extension Membership Pattern pattern
                try {
                        reporter.logBeforeStep("Step 27: Locate Device Extension Membership Pattern");
                        robot.moveTo("Device Extension Membership Pattern");
                        reporter.logAfterStep("Step 27: Located Device Extension Membership Pattern successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 27: Locate Device Extension Membership Pattern", e);
                        throw e;
                }

                // 28. Right Click the pattern and select "Add New Semantic Element"
                try {
                        reporter.logBeforeStep("Step 28: Right Click and Add New Semantic Element");
                        robot.rightClickOn();
                        robot.clickOn("Add New Semantic Element");
                        // close navigator panel
                        navigator.clickNextgenNavigator();

                        reporter.logAfterStep("Step 28: Right Clicked and Add New Semantic Element successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 28: Right Click and Add New Semantic Element", e);
                        throw e;
                }

                /*
                // Scroll horizontally to the RIGHT to reveal confirm button
                robot.clickOn(250, 250);
                horizontalScroll(robot, KeyCode.RIGHT, 1);
                waitForFxEvents();
                */

                // 29. "Update the Stamp to reflect
                // - Module: Device Extension Module
                // - Path: Development Path"
                try {
                        reporter.logBeforeStep("Step 29: Update Stamp Information");
                        conceptPane.updateStamp(status, moduleName, path);
                        reporter.logAfterStep("Step 29: Updated Stamp Information successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 29: Update Stamp Information", e);
                        throw e;
                }

                // 30. Click the pencil icon that is in line with the Reference Component
                // section header
                try {
                        reporter.logBeforeStep("Step 30: Click Reference Component Pencil Icon");
                        conceptPane.clickEditReferenceComponentButton();
                        reporter.logAfterStep("Step 30: Clicked Reference Component Pencil Icon");
                } catch (Exception e) {
                        reporter.logFailure("Step 30: Click Reference Component Pencil Icon", e);
                        throw e;
                }

                /*
                // Scroll horizontally to the LEFT
                robot.clickOn(600, 300);
                horizontalScroll(robot, KeyCode.LEFT, 2);
                waitForFxEvents();
                */

                // 31. Click the Next Gen Search (magnifying glass) and search for a concept
                // using UUID or FQN. (If you have the UUID from the concept you created, you
                // can search by UUID in the Reference Component Dearch)
                try {
                        reporter.logBeforeStep(
                                        "Step 31: Click NextGen Search and search for a concept using UUID or FQN");
                        navigator.clickNextgenSearch();
                        navigator.nextgenSearch(fullyQualifiedName); // search by FQN
                        waitForFxEvents();
                        reporter.logAfterStep(
                                        "Step 31: Clicked NextGen Search and searched for a concept using UUID or FQN successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 31: Click NextGen Search and search for a concept using UUID or FQN",
                                        e);
                        throw e;
                }

                /*
                // Scroll horizontally to the RIGHT
                robot.clickOn(600, 300);
                waitForFxEvents();
                horizontalScroll(robot, KeyCode.RIGHT, 5);
                waitForFxEvents();
                */

                // 32. Locate the correct concept and drag and drop it into the Referenced
                // Component field
                try {
                        reporter.logBeforeStep(
                                        "Step 32: Locate the correct concept and drag and drop it into the Referenced Component field");
                        conceptPane.dragToReferenceComponentField();
                        reporter.logAfterStep(
                                        "Step 32: Located the correct concept and drag and dropped it into the Referenced Component field successfully");
                } catch (Exception e) {
                        reporter.logFailure(
                                        "Step 32: Locate the correct concept and drag and drop it into the Referenced Component field",
                                        e);
                        throw e;
                }

                // 33. Click Confirm and verify the correct reference component populates
                try {
                        reporter.logBeforeStep(
                                        "Step 33: Click Confirm and verify the correct reference component populates");
                        robot.clickOn("CONFIRM");
                        navigator.clickNextgenSearch(); // close search panel
                        //horizontalScroll(robot, KeyCode.RIGHT, 10); // scroll right to see populated reference component
                        // ASSERT THAT THE CORRECT REFERENCE COMPONENT POPULATES
                        String populatedReferenceComponent = conceptPane.getPopulatedReferenceComponent();
                        assertEquals(fullyQualifiedName, populatedReferenceComponent, "The populated reference component should match the selected concept");
                        reporter.logAfterStep(          
                                        "Step 33: Clicked Confirm and verified the correct reference component populates successfully");        

                } catch (Exception e) {
                        reporter.logFailure(
                                        "Step 33: Click Confirm and verify the correct reference component populates",
                                        e);
                        throw e;
                }

                // Navigate back to landing page for next concept creation
                try {
                        reporter.logBeforeStep("Navigate back to Landing Page");
                        //close journal window and refocus onto landing page
                        landingPage.clickHomeButton();
                        reporter.logAfterStep("Returned to Landing Page successfully");
                } catch (Exception e) {
                        reporter.logFailure("Navigate back to Landing Page", e);
                        throw e;
                }
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
