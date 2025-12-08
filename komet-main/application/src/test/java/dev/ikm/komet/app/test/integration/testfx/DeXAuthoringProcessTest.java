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

import static java.nio.file.Files.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

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
        private static final String GITHUB_REPO_URL = "https://github.com/your-org/your-repo";
        private static final String GITHUB_USERNAME = "your-github-username";
        private static final String GITHUB_PASSWORD = "your-github-password";

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

                reporter = new TestReporter(screenshotDirectory, extentReportsDirectory, robot);
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

                // Step 1: Launch KOMET application with SOLOR-GUDID Dataset
                try {
                        reporter.logBeforeStep("Step 1: USER to LAUNCH Komet Application");
                        assertInitialAppState();
                        reporter.logAfterStep("Step 1: Application launched successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 1: USER to LAUNCH Komet Application", e);
                        throw e;
                }

                DataSourceSelectionPage dataSourcePage;

                try {
                        reporter.logBeforeStep("USER to SELECT SOLOR-GUDID from list");
                        dataSourcePage = new DataSourceSelectionPage(robot);
                        dataSourcePage.selectDataSource(DATA_SOURCE_NAME);
                        reporter.logAfterStep("USER to SELECT SOLOR-GUDID from list");
                } catch (Exception e) {
                        reporter.logFailure("USER to SELECT SOLOR-GUDID from list", e);
                        throw e;
                }

                // Step 2:In the tool bar, click Exchange
                try {
                        reporter.logBeforeStep("Step 2: CLICK Exchange");
                        robot.clickOn("Exchange");
                        reporter.logAfterStep("Step 2: CLICK Exchange successful");
                } catch (Exception e) {
                        reporter.logFailure("Step 2: CLICK Exchange", e);
                        throw e;
                }

                // Step 3: Click Info
                try {
                        reporter.logBeforeStep("Step 3: CLICK Info");
                        robot.clickOn("Info");
                        reporter.logAfterStep("Step 3: CLICK Info successful");
                } catch (Exception e) {
                        reporter.logFailure("Step 3: CLICK Info", e);
                        throw e;
                }

                // Step 4: Input the repo url and the user's github account info
                try {
                        reporter.logBeforeStep("Step 4: INPUT Repo URL and GitHub Credentials");
                        robot.clickOn("Repository URL");
                        robot.write(GITHUB_REPO_URL);
                        robot.clickOn("GitHub Username");
                        robot.write(GITHUB_USERNAME);
                        robot.clickOn("GitHub Password");
                        robot.write(GITHUB_PASSWORD);
                        reporter.logAfterStep("Step 4: INPUT Repo URL and GitHub Credentials successful");
                } catch (Exception e) {
                        reporter.logFailure("Step 4: INPUT Repo URL and GitHub Credentials", e);
                        throw e;
                }

                // Step 5: Click Connect
                try {
                        reporter.logBeforeStep("Step 5: CLICK Connect");
                        robot.clickOn("Connect");
                        reporter.logAfterStep("Step 5: CLICK Connect successful");
                } catch (Exception e) {
                        reporter.logFailure("Step 5: CLICK Connect", e);
                        throw e;
                }

                // 6. Verify the connection was successful

                // 7. Open an existing project journal or create a new one
                try {
                        reporter.logBeforeStep("Step 7: CREATE New Project Journal");
                        LandingPage landingPage = new LandingPage(robot);
                        landingPage.clickCreateProjectJournal();
                        reporter.logAfterStep("Step 7: CREATE New Project Journal successful");
                } catch (Exception e) {
                        reporter.logFailure("Step 7: CREATE New Project Journal", e);
                        throw e;
                }

                // 8. Click the "+" icon in the journal window toolbar
                try{
                        reporter.logBeforeStep("Step 8: CLICK Plus Icon in Journal Toolbar");
                        NavigatorPanel navigatorPanel = new NavigatorPanel(robot);
                        navigatorPanel.clickCreate();
                        reporter.logAfterStep("Step 8: CLICK Plus Icon in Journal Toolbar successful");
                } catch (Exception e) {
                        reporter.logFailure("Step 8: CLICK Plus Icon in Journal Toolbar", e);
                        throw e;
                }

                // 9. Click New Concept
                try {
                        reporter.logBeforeStep("Step 9: CLICK New Concept");
                        robot.clickOn("New Concept");
                        reporter.logAfterStep("Step 9: CLICK New Concept successful");
                } catch (Exception e) {
                        reporter.logFailure("Step 9: CLICK New Concept", e);
                        throw e;
                }

                // 10. Click the ellipses in the top right of the blank concept window to add stamp information
                // -   Module: Device Extension Module
                // -   Path: Development Path
                // 11. Click the Properties Panel toggle in the top right of the window
                // 12. Add the Fully Qualified Name information
                // -   Module: Device Extension Module

                ConceptPane conceptPage = new ConceptPane(robot);
                // 13. Click Submit
                try {
                        reporter.logBeforeStep("Step 13: USER to CLICK Submit button");
                        conceptPage.submit();
                        reporter.logAfterStep("Step 13: Clicked Submit button");
                } catch (Exception e) {
                        reporter.logFailure("Step 13: USER to CLICK Submit button", e);
                        throw e;
                }

                // 14. Click the pencil icon that is in line with the axioms section header
                // 15. Click "Add Necessary Set"
                try {
                        reporter.logBeforeStep("Step 15: Click Add Necessary Set");
                        robot.clickOn("Add Necessary Set");
                        reporter.logAfterStep("Step 15  : Clicked Add Necessary Set");
                } catch (Exception e) {
                        reporter.logFailure("Step 15: Click Add Necessary Set", e);
                        throw e;
                }

                // 16. Click the pencil icon aligned to the third axiom row
                // 17. Click "remove axiom"
                try {
                        reporter.logBeforeStep("Step 17: Click Remove Axiom");
                        robot.clickOn("remove axiom");
                        reporter.logAfterStep("Step 17  : Clicked Remove Axiom");
                } catch (Exception e) {
                        reporter.logFailure("Step 17    : Click Remove Axiom", e);
                        throw e;
                }
                // 18. Click the pencil icon aligned to the second axiom row
                // 19. Click "add is-a"
                try {
                        reporter.logBeforeStep("Step 20: Click Add Is-A");
                        robot.clickOn("add is-a");
                        reporter.logAfterStep("Step 20: Clicked Add Is-A");
                } catch (Exception e) {
                        reporter.logFailure("Step 20: Click Add Is-A", e);
                        throw e;
                }

                // 20. Click the pencil icon aligned to the third axiom row
                // 21. Click search for concept
                // 22. Search for parent concept using the search panel
                // 23. When the required parent concept is located, double click to submit

                LOG.info("✓ DeX Authoring Process Test: PASSED");

        }

        // ========== Helper Methods ==========

        private void assertInitialAppState() throws TimeoutException {
                waitFor(10, TimeUnit.SECONDS, () -> App.state.get() == AppState.SELECT_DATA_SOURCE);
                assertEquals(AppState.SELECT_DATA_SOURCE, App.state.get(),
                                "Application should be in SELECT_DATA_SOURCE state");
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
                        screenshotDirectory = Paths.get(BASE_DATA_DIR, TEST_SCREENSHOTS_DIR);
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
                        extentReportsDirectory = Paths.get(BASE_DATA_DIR, EXTENT_REPORTS_DIR);
                        if (!exists(extentReportsDirectory)) {
                                createDirectories(extentReportsDirectory);
                                LOG.info("Created extent reports directory: {}", extentReportsDirectory);
                        }
                } catch (Exception e) {
                        LOG.error("Failed to create extent reports directory", e);
                }
        }
}
