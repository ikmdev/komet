package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.geometry.HorizontalDirection;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;

/**
 * Page object for Concept Details operations.
 */
public class ConceptPane extends BasePage {
    
    private static final String SELECTOR_SUBMIT_BUTTON = "#submitButton";
    
    public ConceptPane(FxRobot robot) {
        super(robot);
    }
    
    /**
     * Clicks on "OTHER NAMES" section and selects the first item.
     */
    public ConceptPane editOtherName() {
        robot.moveTo("OTHER NAMES (1):");
        robot.moveBy(0, 10);
        robot.clickOn();
        LOG.info("Selected other name");
        return this;
    }
    
    /**
     * Clears the name field and enters new text.
     */
    public ConceptPane updateOtherName(String newName) {
        robot.moveTo("Edit Description: Other Name");
        robot.moveBy(0, 45);
        robot.clickOn();
        robot.clickOn();
        robot.clickOn();
        waitFor(500);
        pressKey(KeyCode.DELETE);
        waitFor(500);
        type(newName);
        LOG.info("Updated name to: {}", newName);
        return this;
    }
    
    /**
     * Submits the changes.
     */
    public ConceptPane submit() {
        try {
            clickOn(SELECTOR_SUBMIT_BUTTON);
            LOG.info("Clicked Submit button using CSS selector");
        } catch (Exception e1) {
            try {
                scrollDown();
                clickOn(SELECTOR_SUBMIT_BUTTON);
                LOG.info("Clicked Submit button using CSS selector after scrolling");
            } catch (Exception e2) {
                try {
                    clickOnText("Submit");
                    LOG.info("Clicked Submit button using text");
                } catch (Exception e3) {
                    LOG.warn("Could not find Submit button");
                }
            }
        }
        return this;
    }

     /**
     * Resizes the concept pane by dragging from the top-right corner.
     */
    public ConceptPane resizeConceptPane() {
        waitFor(3000);
        
        // Scroll horizontally first
        robot.moveTo(new Point2D(400, 300));
        waitFor(500);
        robot.scroll(5, HorizontalDirection.RIGHT);
        waitFor(500);
        LOG.info("Scrolled editable area horizontally");
        
        /*
        // Find the concept pane
        Pane conceptPane = robot.lookup(node -> {
            if (node instanceof Pane && !(node instanceof BorderPane)) {
                Bounds bounds = node.localToScreen(node.getBoundsInLocal());
                return bounds != null && bounds.getWidth() > 100 && bounds.getHeight() > 100;
            }
            return false;
        }).queryAll().stream()
            .filter(node -> node instanceof Pane)
            .map(node -> (Pane) node)
            .findFirst()
            .orElse(null);
        
        if (conceptPane != null) {
            Bounds paneBounds = conceptPane.localToScreen(conceptPane.getBoundsInLocal());
            double topRightX = paneBounds.getMaxX() - 5;
            double topRightY = paneBounds.getMinY() + 5;
            
            robot.moveTo(topRightX, topRightY);
            waitFor(1000);
            LOG.info("Moved mouse to top-right corner at ({}, {})", topRightX, topRightY);
            
            robot.press(MouseButton.PRIMARY);
            waitFor(500);
            robot.moveBy(50, 50);
            waitFor(500);
            robot.release(MouseButton.PRIMARY);
            waitFor(1000);
            
            LOG.info("Resized concept pane");
        } else {
            // Fallback to absolute coordinates
            robot.moveTo(new Point2D(115, 200));
            waitFor(1000);
            robot.press(MouseButton.PRIMARY);
            waitFor(500);
            robot.moveBy(50, 50);
            waitFor(500);
            robot.release(MouseButton.PRIMARY);
            waitFor(1000);
            LOG.info("Resized using fallback coordinates");
        }
        */
        // Fallback to absolute coordinates
        
        return this;
    }
}
