package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Abstract base class for all Page Object implementations in the Komet test framework.
 * This class provides common utilities and helper methods that are shared across all
 * page objects, following the Page Object Model design pattern. It encapsulates core
 * TestFX interactions and provides reusable functionality for UI automation.
 * 
 * Key Features:
 *   Centralized FxRobot instance management
 *   Common UI interaction methods (click, type, press keys)
 *   Wait and synchronization utilities
 *   Screenshot capture capabilities
 *   Scrolling and navigation helpers
 *   Dialog management (ESC to close)
 *   Element lookup with type safety
 * 
 * Design Pattern:
 * All concrete page objects extend this base class to inherit common functionality
 * while implementing page-specific operations. This promotes code reuse and maintains
 * a consistent API across all page objects.
 * 
 */


public abstract class BasePage {
    
    protected static final Logger LOG = LoggerFactory.getLogger(BasePage.class);
    protected final FxRobot robot;
    
    public BasePage(FxRobot robot) {
        this.robot = robot;
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
    
    /**
     * Scrolls down by pressing DOWN arrow key.
     */
    protected void scrollDown() {
        robot.press(KeyCode.DOWN);
        robot.release(KeyCode.DOWN);
        waitForFxEvents();
    }
    
    /**
     * Clicks on text in the UI.
     */
    protected void clickOnText(String text) {
        robot.clickOn(text);
        waitForFxEvents();
    }
    
    /**
     * Double-clicks on text in the UI.
     */
    protected void doubleClickOnText(String text) {
        robot.doubleClickOn(text);
        waitForFxEvents();
    }
    
    /**
     * Clicks on a node matching the query.
     */
    protected void clickOn(String query) {
        robot.clickOn(query);
        waitForFxEvents();
    }
    
    /**
     * Types text using keyboard.
     */
    protected void type(String text) {
        robot.write(text);
        waitForFxEvents();
    }
    
    /**
     * Presses a key.
     */
    protected void pressKey(KeyCode keyCode) {
        robot.press(keyCode);
        robot.release(keyCode);
        waitForFxEvents();
    }

}

