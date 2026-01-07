package dev.ikm.komet.app.test.integration.testfx.pages;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import org.testfx.api.FxRobot;

/**
 * Page object representing the Login/User Selection screen in the Komet application.
 * This page appears after data source selection and handles user authentication.
 * Users must select the desired identity from a dropdown and provide credentials to
 * access the application.
 * 
 * Key Responsibilities:
 *   Selecting user from the available user dropdown list
 *   Entering password credentials
 *   Submitting login form via Sign In button
 *   Navigating to the Landing Page upon successful authentication
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
     * Scrolls down if the username is not visible in the dropdown.
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
            
            // Try to find the username, scroll if not visible
            int maxScrollAttempts = 10;
            boolean found = false;
            
            for (int i = 0; i < maxScrollAttempts; i++) {
                try {
                    // Check if the username is visible
                    if (robot.lookup(username).tryQuery().isPresent()) {
                        found = true;
                        LOG.info("Found username: {} after {} scroll attempts", username, i);
                        break;
                    }
                    // Scroll down to find the username
                    scrollDown();
                    waitFor(300);
                } catch (Exception e) {
                    // Continue scrolling
                    scrollDown();
                    waitFor(300);
                }
            }
            
            if (!found) {
                LOG.warn("Username not found after scrolling, attempting to click anyway: {}", username);
            }
            
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
