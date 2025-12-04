package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;

/**
 * Page object for Concept Details operations.
 */
public class ConceptDetailsPage extends BasePage {
    
    private static final String SELECTOR_SUBMIT_BUTTON = "#submitButton";
    
    public ConceptDetailsPage(FxRobot robot) {
        super(robot);
    }
    
    /**
     * Clicks on "OTHER NAMES" section and selects the first item.
     */
    public ConceptDetailsPage selectOtherName() {
        robot.moveTo("OTHER NAMES (1):");
        robot.moveBy(0, 10);
        robot.clickOn();
        LOG.info("Selected other name");
        return this;
    }
    
    /**
     * Clears the name field and enters new text.
     */
    public ConceptDetailsPage updateName(String newName) {
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
    public ConceptDetailsPage submit() {
        // Scroll vertically to make Submit button visible
        robot.scroll(10, javafx.geometry.VerticalDirection.DOWN);
        
        try {
            clickOn(SELECTOR_SUBMIT_BUTTON);
            LOG.info("Clicked Submit button using CSS selector");
        } catch (Exception e1) {
            try {
                clickOnText("Submit");
                LOG.info("Clicked Submit button using text");
            } catch (Exception e2) {
                LOG.warn("Could not find Submit button");
            }
        }
        return this;
    }
}
