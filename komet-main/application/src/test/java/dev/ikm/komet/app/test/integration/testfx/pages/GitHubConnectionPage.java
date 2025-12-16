package dev.ikm.komet.app.test.integration.testfx.pages;

import org.testfx.api.FxRobot;

public class GitHubConnectionPage extends BasePage {

    private static final String GITHUB_REPO_URL = "https://github.com/ikmdev/komet";

    
    public GitHubConnectionPage(FxRobot robot) {
        super(robot);
    }   

        //Enter GitHub credentials
    public GitHubConnectionPage enterGitHubCredentials(String GITHUB_REPO_URL, String githubEmail, String githubUsername, String githubPassword) {
        robot.moveTo("Git URL");
        robot.moveBy(0,30);
        robot.clickOn();
        robot.write(GITHUB_REPO_URL);
        robot.moveTo("Git Email");
        robot.moveBy(0,30);
        robot.clickOn();
        robot.write(githubEmail);
        robot.moveTo("Git User Name");
        robot.moveBy(0,30);
        robot.clickOn();
        robot.write(githubUsername);
        robot.moveTo("Git Password");
        robot.moveBy(0,30);
        robot.clickOn();
        robot.write(githubPassword);
        LOG.info("Entered GitHub credentials for user: {}", githubUsername);
        return this;
    }
    
}
