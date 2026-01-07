package dev.ikm.komet.app.test.integration.testfx.pages;

import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;
import org.testfx.api.FxRobot;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Page object representing the GitHub Connection dialog in the Komet application.
 * This dialog enables users to configure Git/GitHub integration for collaborative
 * terminology authoring and version control.
 * 
 * Key Responsibilities:
 *   Opening the GitHub connection settings dialog
 *   Entering GitHub repository URL
 *   Managing GitHub access credentials
 *   Validating and confirming connection settings
 */
public class GitHubConnectionPage extends BasePage {

    private static final String GITHUB_REPO_URL = "https://github.com/ikmdev/komet";
    private final TestReporter reporter;

    public GitHubConnectionPage(FxRobot robot) {
        super(robot);
        this.reporter = null; // GitHubConnectionPage is constructed without reporter in test method
    }
    
    public GitHubConnectionPage(FxRobot robot, TestReporter reporter) {
        super(robot);
        this.reporter = reporter;
    }

    /**
     * Enters GitHub credentials for repository connection.
     * @param GITHUB_REPO_URL The GitHub repository URL
     * @param githubEmail The GitHub email address
     * @param githubUsername The GitHub username
     * @param githubPassword The GitHub password
     */
    public GitHubConnectionPage enterGitHubCredentials(String GITHUB_REPO_URL, String githubEmail, String githubUsername, String githubPassword) {
        robot.moveTo("Git URL");
        robot.moveBy(0, 30);
        robot.clickOn();
        robot.write(GITHUB_REPO_URL);
        robot.moveTo("Git Email");
        robot.moveBy(0, 30);
        robot.clickOn();
        robot.write(githubEmail);
        robot.moveTo("Git User Name");
        robot.moveBy(0, 30);
        robot.clickOn();
        robot.write(githubUsername);
        robot.moveTo("Git Password");
        robot.moveBy(0, 30);
        robot.clickOn();
        robot.write(githubPassword);
        LOG.info("Entered GitHub credentials for user: {}", githubUsername);
        return this;
    }

    //Click Sync
    public void clickSync() {
        try {
            reporter.logBeforeStep("Step 51: Click 'Sync'");
            robot.clickOn("Sync");
            waitForFxEvents();
            reporter.logAfterStep("Step 51: Clicked 'Sync' successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 51: Click 'Sync'", e);
            throw e;
        }
        waitForFxEvents();
    }

    //Click Info
    public void clickInfo() {
        try {
            reporter.logBeforeStep("Step 3: CLICK 'Info'");
            robot.clickOn("Info");
            waitForFxEvents();
            reporter.logAfterStep("Step 3: CLICK 'Info' successful");
        } catch (Exception e) {
            reporter.logFailure("Step 3: CLICK 'Info'", e);
            throw e;
        }
        waitForFxEvents();
    }
}
