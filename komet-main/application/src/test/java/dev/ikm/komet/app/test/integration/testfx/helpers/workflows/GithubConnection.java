package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class GithubConnection extends BaseWorkflow {

    private static final Logger LOG = LoggerFactory.getLogger(GithubConnection.class);

    /**
     * Constructs a GithubConnection workflow helper.
     * 
     * @param robot    FxRobot instance for UI interactions
     * @param reporter TestReporter instance for logging test steps
     */
    public GithubConnection(FxRobot robot, TestReporter reporter) {
        super(robot, reporter);
    }

    /**
     * Connects to GitHub repository for data synchronization
     * Steps 2-6
     * 
     *  Key Features:
     *      Opening Exchange menu
     *      Opening Info panel
     *      Entering GitHub credentials
     *      Connecting to repository
     *      Verifying connection success
     *
     * @param githubRepoUrl  The GitHub repository URL
     * @param githubEmail    The GitHub email address
     * @param githubUsername The GitHub username
     * @param githubPassword The GitHub password
     * @throws InterruptedException if thread is interrupted during execution
     */
    public void connectToGitHub(String githubRepoUrl, String githubEmail,
            String githubUsername, String githubPassword)
            throws InterruptedException {

        LOG.info("====== Connecting to GitHub Repository ======");

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
            // Note: GitHubConnectionPage needs to be instantiated
            dev.ikm.komet.app.test.integration.testfx.pages.GitHubConnectionPage gitHubConnectionPage = new dev.ikm.komet.app.test.integration.testfx.pages.GitHubConnectionPage(
                    robot);
            gitHubConnectionPage.enterGitHubCredentials(githubRepoUrl, githubEmail, githubUsername, githubPassword);
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
                        LOG.info("CONNECTION SUCCESSFUL: GitHub Info popup appeared after {} attempts", i + 1);
                        break;
                    }
                } catch (Exception e) {
                    // Popup not found yet, continue waiting
                }
                Thread.sleep(1000);
            }

            if (!popupFound) {
                throw new RuntimeException("GitHub Info popup did not appear within 30 seconds");
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
    }

}
