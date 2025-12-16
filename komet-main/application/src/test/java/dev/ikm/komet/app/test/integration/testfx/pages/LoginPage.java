package dev.ikm.komet.app.test.integration.testfx.pages;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import org.testfx.api.FxRobot;

/**
 * Page object for the login screen.
 */
public class LoginPage extends BasePage {
    
    private static final String SELECTOR_USER_CHOOSER = "#Button";
    private static final String SELECTOR_PASSWORD_FIELD = "#passwordField";
    private static final String SELECTOR_SIGN_IN_BUTTON = "#loginButton";
    
    public LoginPage(FxRobot robot){
        super(robot);
    }
    
    /**
     * Selects a user from the dropdown.
     */
    public LoginPage selectUser(String username) {
        waitForFxEvents();
        
        ComboBox<?> userComboBox = null;
        if (robot.lookup(SELECTOR_USER_CHOOSER).tryQuery().isPresent()) {
            userComboBox = robot.lookup(SELECTOR_USER_CHOOSER).queryComboBox();
        } else if (robot.lookup(".combo-box").tryQuery().isPresent()) {
            userComboBox = robot.lookup(".combo-box").queryComboBox();
        }
        
        if (userComboBox != null) {
            robot.clickOn(userComboBox);
            waitForFxEvents();
            clickOnText(username);
            LOG.info("Selected user: {}", username);
        } else {
            LOG.error("User chooser ComboBox not found");
        }
        
        return this;
    }
    
    /**
     * Enters the password.
     */
    public LoginPage enterPassword(String password) {
        clickOn(SELECTOR_PASSWORD_FIELD);
        type(password);
        LOG.info("Entered password");
        return this;
    }
    
    /**
     * Clicks the sign-in button.
     */
    public LandingPage clickSignIn() {
        Button signInButton = lookup(SELECTOR_SIGN_IN_BUTTON, Button.class);
        robot.clickOn(signInButton);
        waitForFxEvents();
        LOG.info("Clicked SIGN IN button");
        return new LandingPage(robot);
    }


}
