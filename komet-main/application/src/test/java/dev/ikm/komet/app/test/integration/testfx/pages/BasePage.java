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
            Thread.sleep(500);
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
            Thread.sleep(750);
            waitForFxEvents();
            LOG.info("Closed dialog boxes");
        } catch (InterruptedException e) {
            LOG.error("Interrupted while closing dialogs", e);
        }
    }
    
    /**
     * Captures a screenshot with the given description.
     */
    protected void captureScreenshot(String description) {
        try {
            String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String filename = String.format("%s_%s.png", timestamp, 
                description.replaceAll("[^a-zA-Z0-9-_]", "_"));
            
            java.nio.file.Path screenshotDir = java.nio.file.Paths.get(
                System.getProperty("user.home"), "Solor", "test-screenshots");
            java.nio.file.Files.createDirectories(screenshotDir);
            
            java.io.File screenshotFile = screenshotDir.resolve(filename).toFile();
            
            // Capture the entire screen including all windows and dialogs
            java.awt.Robot awtRobot = new java.awt.Robot();
            java.awt.Rectangle screenRect = new java.awt.Rectangle(
                java.awt.Toolkit.getDefaultToolkit().getScreenSize());
            java.awt.image.BufferedImage screenCapture = awtRobot.createScreenCapture(screenRect);
            
            javax.imageio.ImageIO.write(screenCapture, "png", screenshotFile);
            LOG.info("Screenshot captured: {}", screenshotFile.getAbsolutePath());
        } catch (Exception e) {
            LOG.error("Failed to capture screenshot: {}", description, e);
        }
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
     * Looks up a node by selector and casts to specified type.
     */
    protected <T extends Node> T lookup(String selector, Class<T> type) {
        Node node = robot.lookup(selector).query();
        assertNotNull(node, "Node with selector '" + selector + "' should exist");
        return type.cast(node);
    }
}
