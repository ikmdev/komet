package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;
import dev.ikm.komet.app.test.integration.testfx.pages.LoginPage;
import dev.ikm.komet.app.test.integration.testfx.pages.DataSourceSelectionPage;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Workflow helper for launching Komet and logging in as a user.
 * This workflow encapsulates the process of starting the application, selecting a data source,
 * and authenticating a user to get to the main running state.
 * 
 * Key Features:
 * - Application launch and data source selection
 * - User authentication with username/password
 * - State verification at each step
 * - Window maximization after successful login
 */
public class LaunchKometUserSA extends BaseWorkflow {

        private static final Logger LOG = LoggerFactory.getLogger(LaunchKometUserSA.class);

        // Test data
        private static final String DATA_SOURCE_STORE = "Open SpinedArrayStore";
    /**
     * Constructs a LaunchKometUserSA workflow helper with reporter.
     * 
     * @param robot    FxRobot instance for UI interactions
     * @param reporter TestReporter instance for logging test steps
     */
    public LaunchKometUserSA(FxRobot robot, TestReporter reporter) {
        super(robot, reporter);
    }

    /**
     * Constructs a LaunchKometUserSA workflow helper without reporter.
     * This constructor allows the helper to be used without test reporting.
     * 
     * @param robot FxRobot instance for UI interactions
     */
    public LaunchKometUserSA(FxRobot robot) {
        super(robot, null);
    }

    /**
     * Launches Komet application, selects data source, and logs in as specified user.
     * 
     * @param USERNAME The username for login
     * @param PASSWORD The password for login
     * @throws InterruptedException if thread is interrupted during execution
     * @throws java.util.concurrent.TimeoutException if waiting for application state times out
     */
    public void launchKomet(String DATA_SOURCE_NAME, String USERNAME, String PASSWORD) throws InterruptedException, java.util.concurrent.TimeoutException {
    // Step 1: Launch KOMET, select dataset, and login as user
                LOG.info("Launching KOMET application with " + DATA_SOURCE_NAME + " data source, and logging in as " + USERNAME);

                try {
                        //Launch KOMET
                        if (reporter != null) reporter.logBeforeStep("Step 1: USER to LAUNCH Komet Application");
                        assertInitialAppState();
                        if (reporter != null) reporter.logAfterStep("Step 1: Application launched successfully");
                } catch (Exception e) {
                        if (reporter != null) reporter.logFailure("Step 1: USER to LAUNCH Komet Application", e);
                        throw e;
                }

                try{
                        //Select Data Source Store
                        if (reporter != null) reporter.logBeforeStep("USER to SELECT Data Source Store");
                        dataSource.selectDataSourceStore(DATA_SOURCE_STORE);
                        if (reporter != null) reporter.logAfterStep("USER selected Data Source Store");     
                } catch (Exception e) {
                        if (reporter != null) reporter.logFailure("USER to SELECT Data Source Store", e);
                        throw e;
                }

                try {
                        // Select data source
                        if (reporter != null) reporter.logBeforeStep("Step 2: USER to SELECT " + DATA_SOURCE_NAME + " from list");
                        dataSource.selectDataSource(DATA_SOURCE_NAME);
                        if (reporter != null) reporter.logAfterStep("Step 2: Selected " + DATA_SOURCE_NAME + " from list");
                } catch (Exception e) {
                        if (reporter != null) reporter.logFailure("Step 2: USER to SELECT " + DATA_SOURCE_NAME + " from list", e);
                        throw e;
                }

                try {
                        // Click OK button
                        if (reporter != null) reporter.logBeforeStep("USER to CLICK OK button");
                        dataSource.clickOk();
                        if (reporter != null) reporter.logAfterStep("USER clicked OK button");
                } catch (Exception e) {
                        if (reporter != null) reporter.logFailure("USER to CLICK OK button", e);
                        throw e;
                }

                try {
                        //Wait until the data is loaded and the "Welcome to Komet" screen is displayed
                        if (reporter != null) reporter.logBeforeStep("Waiting for data to load");
                        assertSelectUserState();
                        if (reporter != null) reporter.logAfterStep("Data loaded successfully");
                } catch (Exception e) {
                        if (reporter != null) reporter.logFailure("Waiting for data to load", e);
                        throw e;
                }

                try {
                        // Select KOMET User from dropdown
                        if (reporter != null) reporter.logBeforeStep("USER to SELECT KOMET User from dropdown");
                        loginPage.selectUser(USERNAME);
                        if (reporter != null) reporter.logAfterStep("USER selected KOMET User from dropdown");
                } catch (Exception e) {
                        if (reporter != null) reporter.logFailure("USER to SELECT KOMET User from dropdown", e);
                        throw e;
                }

                try {
                        // Input password
                        if (reporter != null) reporter.logBeforeStep("USER to INPUT password");
                        loginPage.enterPassword(PASSWORD);
                        if (reporter != null) reporter.logAfterStep("USER entered password");
                } catch (Exception e) {
                        if (reporter != null) reporter.logFailure("USER to INPUT password", e);
                        throw e;
                }

                try {
                        // Click SIGN IN button
                        if (reporter != null) reporter.logBeforeStep("USER to CLICK SIGN IN button");
                        loginPage.clickSignIn();
                        if (reporter != null) reporter.logAfterStep("USER clicked SIGN IN button");
                } catch (Exception e) {
                        if (reporter != null) reporter.logFailure("USER to CLICK SIGN IN button", e);
                        throw e;
                }

                try {
                        // Wait for running state
                        if (reporter != null) reporter.logBeforeStep("Waiting for application running state");
                        assertRunningAppState();
                        landingPage.maximizeWindow();
                        if (reporter != null) reporter.logAfterStep("Application in running state and maximized");
                } catch (Exception e) {
                        if (reporter != null) reporter.logFailure("Application running state", e);
                        throw e;
                }

            }
}
