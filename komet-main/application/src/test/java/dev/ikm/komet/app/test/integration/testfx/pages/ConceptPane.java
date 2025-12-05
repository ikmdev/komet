package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.geometry.HorizontalDirection;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import org.testfx.api.FxRobot;
import javafx.scene.control.Tooltip;

/**
 * Page object for Concept Details operations.
 */
public class ConceptPane extends BasePage {
    
    private static final String SELECTOR_SUBMIT_BUTTON = "#submitButton";
    
    public ConceptPane(FxRobot robot) {
        super(robot);
    }

    /**
     * ???
     * Clicks "Edit Descriptions" button using tooltip lookup.
     * Takes the parameter as the pane the button is located in.
     */
    public ConceptPane clickEditDescriptionsButton(String paneName) {
        Button editDescriptionsButton = findButtonByTooltip("Edit Descriptions");
        if (editDescriptionsButton != null) {
            robot.interact(editDescriptionsButton::fire);
            waitFor(500);
            LOG.info("Clicked Edit Descriptions button");
        } else {
            LOG.warn("Edit Descriptions button not found");
        }
        return this;
    }

    /*
     * ???
     *Click "Edit Axioms" button using tooltip lookup
     * Takes the parameter as the pane the button is located in
     */
    public ConceptPane clickEditAxiomsButton(String paneName) {
        Button editAxiomsButton = findButtonByTooltip("Edit Axioms");
        if (editAxiomsButton != null) {
            robot.interact(editAxiomsButton::fire);
            waitFor(500);
            LOG.info("Clicked Edit Axioms button");
        } else {
            LOG.warn("Edit Axioms button not found");
        }
        return this;
    }

    /*Click "Coordinates" button using tooltip lookup
     * Takes the parameter as the pane the button is located in
     */
    public ConceptPane clickCoordinatesButton(String paneName) {
        Button coordinatesButton = findButtonByTooltip("Coordinates");
        if (coordinatesButton != null) {
            robot.interact(coordinatesButton::fire);
            waitFor(500);
            LOG.info("Clicked Coordinates button");
        } else {
            LOG.warn("Coordinates button not found");
        }
        return this;
    }

    /*
    *
    *
     * Can add the remaining buttons similarly
     * Reasoner buton in each concept pane has same tooltip as Reasoner in navigator pane
     * Different loactor will need to be used to differentiate them
     *
     *
    */
    
    /**
     * Clicks on "OTHER NAMES" section
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


     // Resizes the concept pane using drag and drop
      public ConceptPane resizeConceptPane() {
        Point2D handle = new Point2D(70, 50);
        robot.moveTo(handle);
        waitFor(300);
        robot.drag(MouseButton.PRIMARY).moveBy(100, 100).drop();
        waitFor(500);
        LOG.info("Resized concept pane");
        return this;   
      }
    
        
        /**
         * Helper method to find a Button by its tooltip text.
         */
        private Button findButtonByTooltip(String tooltipText) {
        return (Button) robot.lookup((java.util.function.Predicate<javafx.scene.Node>) n -> {
            if (n instanceof Button) {
                Button button = (Button) n;
                Tooltip tooltip = button.getTooltip();
                return tooltip != null && tooltipText.equals(tooltip.getText());
            }
            return false;
        }).query();
    }
}
