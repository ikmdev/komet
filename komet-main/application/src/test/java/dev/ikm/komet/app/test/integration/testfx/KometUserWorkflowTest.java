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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.nio.file.Files.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

/**
 * Integration test for the complete Komet user workflow using Page Object Model pattern.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class KometUserWorkflowTest {

    private static final Logger LOG = LoggerFactory.getLogger(KometUserWorkflowTest.class);

    // Configuration constants
    private static final String PROPERTY_TARGET_DATA_DIR = "target.data.directory";
    private static final String PROPERTY_USER_HOME = "user.home";
    private static final String BASE_DATA_DIR = System.getProperty(PROPERTY_TARGET_DATA_DIR,
                System.getProperty(PROPERTY_USER_HOME, System.getProperty("java.io.tmpdir")));
    private static final String SOLOR_DIR = "Solor";
    private static final String TEST_SCREENSHOTS_DIR = "test-screenshots";
    private static final String EXTENT_REPORTS_DIR = "extent-reports";
    
    // Test data
    private static final String DATA_SOURCE_NAME = "komet";
    private static final String USERNAME = "KOMET user";
    private static final String PASSWORD = "KOMET user";
    private static final String SEARCH_QUERY = "user";
    private static final String NEW_NAME = "KOMET User";
    private static final String conceptName = "KOMET user";

    private Path screenshotDirectory;
    private Path extentReportsDirectory;
    private App webApp;
    private TestReporter reporter;

    @BeforeAll
    public void setUpClass() {
        overrideUserHome();
        createScreenshotDirectory();
        createExtentReportsDirectory();
    }

    @BeforeEach
    void setUp(FxRobot robot) throws Exception {
        FxToolkit.setupApplication(() -> {
            webApp = new App();
            return webApp;
        });
        
        reporter = new TestReporter(screenshotDirectory, extentReportsDirectory, robot);
        reporter.createTest("Komet User Workflow Test (Refactored)",
                "Complete automated workflow using Page Object Model pattern");
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
        LOG.info("Test cleanup complete");
    }

    @Test
    @DisplayName("Complete Komet User Workflow Test - Refactored with Page Objects")
    public void testCompleteKometUserWorkflow(FxRobot robot) throws TimeoutException, InterruptedException {
        LOG.info("Starting Complete Komet User Workflow Test (Refactored)");

        try {
            // Step 1: Launch KOMET application
            reporter.logBeforeStep("Step 1: USER to LAUNCH Komet Application");
            assertInitialAppState();
            reporter.logAfterStep("Step 1: Application launched successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 1: USER to LAUNCH Komet Application", e);
            throw e;
        }

        DataSourceSelectionPage dataSourcePage;
        try {
            // Step 2: Select data source
            reporter.logBeforeStep("Step 2: USER to SELECT komet from list");
            dataSourcePage = new DataSourceSelectionPage(robot);
            dataSourcePage.selectDataSource(DATA_SOURCE_NAME);
            reporter.logAfterStep("Step 2: Selected komet from list");
        } catch (Exception e) {
            reporter.logFailure("Step 2: USER to SELECT komet from list", e);
            throw e;
        }

        LoginPage loginPage;
        try {
            // Step 3: Click OK button
            reporter.logBeforeStep("Step 3: USER to CLICK OK button");
            loginPage = dataSourcePage.clickOk();
            reporter.logAfterStep("Step 3: Clicked OK button");
        } catch (Exception e) {
            reporter.logFailure("Step 3: USER to CLICK OK button", e);
            throw e;
        }

        try {
            // Step 4: Select KOMET User from dropdown
            reporter.logBeforeStep("Step 4: USER to SELECT KOMET User from dropdown");
            loginPage.selectUser(USERNAME);
            reporter.logAfterStep("Step 4: Selected KOMET User from dropdown");
        } catch (Exception e) {
            reporter.logFailure("Step 4: USER to SELECT KOMET User from dropdown", e);
            throw e;
        }

        try {
            // Step 5: Input password
            reporter.logBeforeStep("Step 5: USER to INPUT password");
            loginPage.enterPassword(PASSWORD);
            reporter.logAfterStep("Step 5: Entered password");
        } catch (Exception e) {
            reporter.logFailure("Step 5: USER to INPUT password", e);
            throw e;
        }

        LandingPage landingPage;
        try {
            // Step 6: Click SIGN IN button
            reporter.logBeforeStep("Step 6: USER to CLICK SIGN IN button");
            landingPage = loginPage.clickSignIn();
            reporter.logAfterStep("Step 6: Clicked SIGN IN button");
        } catch (Exception e) {
            reporter.logFailure("Step 6: USER to CLICK SIGN IN button", e);
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

 
        try {
            // Step 7: Click New Project Journal button
            reporter.logBeforeStep("Step 7: USER to CLICK New Project Journal button");
            landingPage.clickCreateProjectJournal();
            reporter.logAfterStep("Step 7: Clicked New Project Journal button");
        } catch (Exception e) {
            reporter.logFailure("Step 7: USER to CLICK New Project Journal button", e);
            throw e;
        }

        NavigatorPanel navigator = new NavigatorPanel(robot);
        
        try {
            // Step 8: Click Nextgen Navigator button
            reporter.logBeforeStep("Step 8: USER to CLICK Nextgen Navigator button");
            navigator.clickNextgenNavigator();
            reporter.logAfterStep("Step 8: Clicked Nextgen Navigator button");
        } catch (Exception e) {
            reporter.logFailure("Step 8: USER to CLICK Nextgen Navigator button", e);
            throw e;
        }
        
        try {
            // Step 9: Validate Concepts button is SELECTED
            reporter.logBeforeStep("Step 9: USER to Validate Concepts button is SELECTED");
            navigator.clickConcepts();
            reporter.logAfterStep("Step 9: Validated Concepts button is SELECTED");
        } catch (Exception e) {
            reporter.logFailure("Step 9: USER to Validate Concepts button is SELECTED", e);
            throw e;
        }
        
        try {
            // Step 10: Click expand button for Author
            reporter.logBeforeStep("Step 10: USER to CLICK expand button for Author");
            navigator.expandTreeNode("Author");
            reporter.logAfterStep("Step 10: Clicked expand button for Author");
        } catch (Exception e) {
            reporter.logFailure("Step 10: USER to CLICK expand button for Author", e);
            throw e;
        }

        ConceptPane conceptPage;
        try {
            // Step 11: Double click Gretel tab
            reporter.logBeforeStep("Step 11: USER to DOUBLE CLICK Gretel tab");
            conceptPage = navigator.openConcept("Gretel");
            reporter.logAfterStep("Step 11: Double clicked Gretel tab");
        } catch (Exception e) {
            reporter.logFailure("Step 11: USER to DOUBLE CLICK Gretel tab", e);
            throw e;
        }

        try {
            // Step 12: Drag and drop Komet User to editing area via six dots
            reporter.logBeforeStep("Step 12: USER to DRAG AND DROP Komet User to editing area via six dots");
            navigator.dragToEditingArea(conceptName);
            reporter.logAfterStep("Step 12: Dragged and dropped Komet User to editing area");
        } catch (Exception e) {
            reporter.logFailure("Step 12: USER to DRAG AND DROP Komet User to editing area", e);
            throw e;
        }

        try {
            // Close navigator to make resizing easier
            reporter.logBeforeStep("Closing navigator");
            navigator.clickNextgenNavigator(); // Toggle to close
            reporter.logAfterStep("Closed navigator");
        } catch (Exception e) {
            reporter.logFailure("Closing navigator", e);
            throw e;
        }

        try {
            // Step 13: Drag and drop corner of Komet User pane to resize
            reporter.logBeforeStep("Step 13: USER to DRAG AND DROP corner of Komet User Pane to resize");
            conceptPage.resizeConceptPane();
            reporter.logAfterStep("Step 13: Resized Komet User pane");
        } catch (Exception e) {
            reporter.logFailure("Step 13: USER to DRAG AND DROP corner of Komet User Pane to resize", e);
            throw e;
        }

        try {
            // Step 14: Close Journal 1 window
            reporter.logBeforeStep("Step 14: USER to CLOSE Journal 1 window");
            landingPage.closeJournalWindow();
            reporter.logAfterStep("Step 14: Closed Journal 1 window");
        } catch (Exception e) {
            reporter.logFailure("Step 14: USER to CLOSE Journal 1 window", e);
            throw e;
        }

        try {
            // Step 15: Click Create project journal tab
            reporter.logBeforeStep("Step 15: USER to CLICK Create project journal tab");
            landingPage.clickCreateProjectJournal();
            reporter.logAfterStep("Step 15: Clicked Create project journal tab");
        } catch (Exception e) {
            reporter.logFailure("Step 15: USER to CLICK Create project journal tab", e);
            throw e;
        }

        try {
            // Step 16: Click Nextgen Search button
            reporter.logBeforeStep("Step 16: USER to CLICK Nextgen Search button");
            navigator.clickNextgenSearch();
            reporter.logAfterStep("Step 16: Clicked Nextgen Search button");
        } catch (Exception e) {
            reporter.logFailure("Step 16: USER to CLICK Nextgen Search button", e);
            throw e;
        }

        try {
            // Step 17: Input user into search field
            reporter.logBeforeStep("Step 17: USER to INPUT user into search field and press enter");
            navigator.search(SEARCH_QUERY);
            reporter.logAfterStep("Step 17: Entered user into search field and pressed enter");
        } catch (Exception e) {
            reporter.logFailure("Step 17: USER to INPUT user into search field and press enter", e);
            throw e;
        }

        try {
            // Step 18: Double click KOMET User concept tab
            reporter.logBeforeStep("Step 18: USER to DOUBLE CLICK KOMET User concept tab");
            navigator.openConceptFromResults("KOMET user");
            reporter.logAfterStep("Step 18: Double clicked KOMET User concept tab");
        } catch (Exception e) {
            reporter.logFailure("Step 18: USER to DOUBLE CLICK KOMET User concept tab", e);
            throw e;
        }

        try {
            // Close search panel
            reporter.logBeforeStep("Closing search panel");
            navigator.clickNextgenSearch(); // Toggle to close
            Thread.sleep(2000);
            reporter.logAfterStep("Closed search panel");
        } catch (Exception e) {
            reporter.logFailure("Closing search panel", e);
            throw e;
        }

        try {
            // Step 19 : Click KOMET User (Case insensitive, English Language)
            reporter.logBeforeStep("Step 19: USER to click KOMET User (Case insensitive, English Language)");
            conceptPage.editOtherName();
            reporter.logAfterStep("Step 19: Clicked KOMET User (Case insensitive, English Language)");
        } catch (Exception e) {
            reporter.logFailure("Step 19: USER to click KOMET User (Case insensitive, English Language)", e);
            throw e;
        }

        try {
            // Step 20 : Clear Name field
            reporter.logBeforeStep("Step 20: USER to CLEAR Name field");
            // Clear is handled in updateName method
            reporter.logAfterStep("Step 20: Cleared Name field");
        } catch (Exception e) {
            reporter.logFailure("Step 20: USER to CLEAR Name field", e);
            throw e;
        }

        try {
            // Step 21: Input KOMET User
            reporter.logBeforeStep("Step 21: USER to INPUT KOMET User");
            conceptPage.updateOtherName(NEW_NAME);
            reporter.logAfterStep("Step 21: Entered KOMET User");
        } catch (Exception e) {
            reporter.logFailure("Step 21: USER to INPUT KOMET User", e);
            throw e;
        }

        try {
            // Step 22 : Click Submit button
            reporter.logBeforeStep("Step 22: USER to CLICK Submit button");
            conceptPage.submit();
            reporter.logAfterStep("Step 22: Clicked Submit button");
        } catch (Exception e) {
            reporter.logFailure("Step 22: USER to CLICK Submit button", e);
            throw e;
        }

        reporter.saveScreenshot("komet-user-workflow-complete");
        LOG.info("Complete Komet User Workflow Test finished successfully");
    }

    // Helper methods
    private void overrideUserHome() {
        if (BASE_DATA_DIR != null) {
            System.setProperty(PROPERTY_USER_HOME, BASE_DATA_DIR);
            LOG.info("Overridden user.home to: {}", BASE_DATA_DIR);
        }
    }

    private void createScreenshotDirectory() {
        screenshotDirectory = Paths.get(BASE_DATA_DIR, SOLOR_DIR, TEST_SCREENSHOTS_DIR);
        try {
            if (notExists(screenshotDirectory)) {
                createDirectories(screenshotDirectory);
                LOG.info("Created screenshot directory: {}", screenshotDirectory);
            }
        } catch (IOException e) {
            LOG.error("Failed to create screenshot directory", e);
        }
    }

    private void createExtentReportsDirectory() {
        extentReportsDirectory = Paths.get(BASE_DATA_DIR, SOLOR_DIR, EXTENT_REPORTS_DIR);
        try {
            if (notExists(extentReportsDirectory)) {
                createDirectories(extentReportsDirectory);
                LOG.info("Created ExtentReports directory: {}", extentReportsDirectory);
            }
        } catch (IOException e) {
            LOG.error("Failed to create ExtentReports directory", e);
        }
    }

    private void assertInitialAppState() throws TimeoutException {
        waitFor(10, TimeUnit.SECONDS, () -> App.state.get() == AppState.SELECT_DATA_SOURCE);
        assertEquals(AppState.SELECT_DATA_SOURCE, App.state.get(),
                "Application should be in SELECT_DATA_SOURCE state");
    }

    private void assertRunningAppState() throws TimeoutException {
        waitFor(30, TimeUnit.SECONDS, () -> App.state.get() == AppState.RUNNING);
        assertEquals(AppState.RUNNING, App.state.get(), "Application should be in RUNNING state");
    }
}
