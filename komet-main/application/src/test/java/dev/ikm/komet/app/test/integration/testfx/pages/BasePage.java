package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Base class for all page objects providing common functionality.
 */
public abstract class BasePage {
    
    protected static final Logger LOG = LoggerFactory.getLogger(BasePage.class);
    protected final FxRobot robot;
    
    public BasePage(FxRobot robot) {
        this.robot = robot;
    }
    
    /**
     * Clicks on a node identified by CSS selector.
     */
    protected void clickOn(String selector) {
        robot.clickOn(selector);
        waitForFxEvents();
    }
    
    /**
     * Clicks on a text element.
     */
    protected void clickOnText(String text) {
        robot.clickOn(text);
        waitForFxEvents();
    }
    
    /**
     * Double-clicks on a text element.
     */
    protected void doubleClickOnText(String text) {
        robot.doubleClickOn(text);
        waitForFxEvents();
    }
    
    /**
     * Types text into the currently focused field.
     */
    protected void type(String text) {
        robot.write(text);
        waitForFxEvents();
    }
    
    /**
     * Presses a key.
     */
    protected void pressKey(KeyCode key) {
        robot.press(key);
        robot.release(key);
        waitForFxEvents();
    }
    
    /**
     * Presses ESC key multiple times to close dialogs.
     * Captures a screenshot before closing to document the dialog.
     */
    protected void closeDialogs() {
        try {
            // Wait for dialog to be fully rendered
            waitForFxEvents();
            
            // Capture screenshot before closing dialog
            captureScreenshot("dialog_displayed_before_close");
            LOG.info("Captured screenshot of dialog before closing");
            
            for (int i = 0; i < 3; i++) {
                robot.press(KeyCode.ESCAPE);
                robot.release(KeyCode.ESCAPE);
                Thread.sleep(300);
                waitForFxEvents();
            }
            waitForFxEvents();
            LOG.info("Closed dialog boxes");
        } catch (InterruptedException e) {
            LOG.error("Interrupted while closing dialogs", e);
        }
    }

    /** 
     * Scrolls down the current view.
     */
    protected void scrollDown(){
        robot.scroll(10, javafx.geometry.VerticalDirection.DOWN);
        LOG.info("Scrolled down to find the desired element");
    }

    /**
     * Captures a screenshot with the given description.
     * Note: Screenshots are only saved to file on test failure via TestReporter.logFailure()
     */
    protected void captureScreenshot(String description) {
        // Log the screenshot attempt - actual file saving only happens on test failure
        LOG.info("Screenshot point: {}", description);
    }
    
    /**
     * Waits for a specified duration.
     */
    protected void waitFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
            waitForFxEvents();
        } catch (InterruptedException e) {
            LOG.error("Interrupted during wait", e);
        }
    }
    
    /**
     * Waits for text to appear in the scene with retries.
     * @param text The text to wait for
     * @param maxAttempts Maximum number of attempts (default 10)
     * @param waitBetweenMs Milliseconds to wait between attempts (default 500)
     * @return true if text was found, false otherwise
     */
    protected boolean waitForText(String text, int maxAttempts, long waitBetweenMs) {
        for (int i = 0; i < maxAttempts; i++) {
            try {
                waitForFxEvents();
                if (robot.lookup(text).tryQuery().isPresent()) {
                    LOG.info("Found text '{}' after {} attempts", text, i + 1);
                    return true;
                }
                Thread.sleep(waitBetweenMs);
            } catch (Exception e) {
                LOG.debug("Attempt {} to find text '{}' failed", i + 1, text);
            }
        }
        LOG.warn("Text '{}' not found after {} attempts", text, maxAttempts);
        return false;
    }
    
    /**
     * Waits for text to appear with default parameters (10 attempts, 500ms between).
     */
    protected boolean waitForText(String text) {
        return waitForText(text, 10, 500);
    }
    
    /**
     * Looks up a node by selector and casts to specified type.
     */
    protected <T extends Node> T lookup(String selector, Class<T> type) {
        Node node = robot.lookup(selector).query();
        assertNotNull(node, "Node with selector '" + selector + "' should exist");
        return type.cast(node);
    }

}

