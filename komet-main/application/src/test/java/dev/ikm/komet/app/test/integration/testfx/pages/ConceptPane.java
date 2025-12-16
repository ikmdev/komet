package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import org.testfx.api.FxRobot;
import javafx.scene.control.Tooltip;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Page object for Concept Details operations.
 */
public class ConceptPane extends BasePage {
    
    private static final String SELECTOR_SUBMIT_BUTTON = "#submitButton";
    
    public ConceptPane(FxRobot robot) {
        super(robot);
    }

    /*
    * Clicks the "Properties"toggle 
    */
    public ConceptPane clickPropertiesToggle() {
        robot.moveTo("PROPERTIES");
        waitForFxEvents();
        robot.moveBy(50, 0); // Move right to find the toggle button
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        LOG.info("Clicked Properties toggle");
        return this;
    }

    /**
     * ???
     * Clicks "Edit Descriptions" button using tooltip lookup.
     * Takes the parameter as the pane the button is located in.
     */
    public ConceptPane clickEditDescriptionsButton() {
        Button editDescriptionsButton = findButtonByTooltip("Edit Descriptions");
        if (editDescriptionsButton != null) {
            robot.interact(editDescriptionsButton::fire);
            waitForFxEvents();
            LOG.info("Clicked Edit Descriptions button");
        } else {
            LOG.warn("Edit Descriptions button not found");
        }
        return this;
    }

    /*
    * Click the copy button using tooltip lookup
    */
    public ConceptPane clickCopyButton() {
        waitForFxEvents();
        // First move to the text containing "Komet ID: " to make the copy button appear
        javafx.scene.Node kometIdNode = robot.lookup((javafx.scene.Node node) -> {
            if (node instanceof javafx.scene.text.Text) {
                String text = ((javafx.scene.text.Text) node).getText();
                return text != null && text.contains("Komet ID: ");
            }
            return false;
        }).query();
        
        robot.moveTo(kometIdNode);
        waitForFxEvents();
        robot.moveBy(130, 0); // Move right to find the copy button
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        LOG.info("Clicked Copy button");
        return this;
    }

    /*
     * ???
     *Click "Edit Axioms" button using tooltip lookup
     * Takes the parameter as the pane the button is located in
     */
    public ConceptPane clickEditAxiomsButton() {
        waitForFxEvents();
        Button editAxiomsButton = findButtonByTooltip("Edit Axioms");
        if (editAxiomsButton != null) {
            robot.interact(editAxiomsButton::fire);
            waitForFxEvents();
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
            waitForFxEvents();
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
        waitForFxEvents();
        robot.moveBy(0, 10);
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        LOG.info("Selected other name");
        return this;
    }

    //Update Fully Qualified Name
    public ConceptPane updateFullyQualifiedName(String newFullyQualifiedName) {
        robot.clickOn("Enter Name");
        waitForFxEvents();
        robot.write(newFullyQualifiedName);
        waitForFxEvents();
        LOG.info("Updated Fully Qualified Name to: {}", newFullyQualifiedName);
        return this;
    }

    //open stamp editor
    public ConceptPane openStampEditor() {
        waitForFxEvents();
        robot.moveTo("PROPERTIES");
        waitForFxEvents();
        robot.moveBy(0, 50);
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        LOG.info("Opened Stamp Editor");
        return this;
    }


    public ConceptPane updateStamp(String status, String module, String path) {
        waitForFxEvents();
        this.updateStatus(status);
        waitForFxEvents();
        this.updateModule(module);
        waitForFxEvents();
        this.updatePath(path);
        waitForFxEvents();
        robot.clickOn("CONFIRM");
        waitForFxEvents();
        LOG.info("Updated stamp to - Status: {}, Module: {}, Path: {}", status, module, path);
        return this;
    }

    //update status by selecting in combobox
    public ConceptPane updateStatus(String newStatus) {
        robot.moveTo("Status");
        waitForFxEvents();
        robot.moveBy(100, 0); // Move right from the label to find the ComboBox
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        // Click the second occurrence of the status (in the combobox dropdown)
        var matches = robot.lookup(newStatus).queryAll();
        if (matches.size() > 1) {
            // Click the second occurrence
            robot.clickOn(matches.stream().skip(1).findFirst().get());
        } else {
            // Fallback to first/only occurrence
            robot.clickOn(newStatus);
        }
        waitForFxEvents();
        LOG.info("Updated status to: {}", newStatus);
        return this;
    }    

    //update module by selecting in combobox
    public ConceptPane updateModule(String newModule) {
        robot.moveTo("Module");
        waitForFxEvents();
        robot.moveBy(100, 0); // Move right from the label to find the ComboBox
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        // Click the second occurrence of the module name (in the combobox dropdown)
        var matches = robot.lookup(newModule).queryAll();
        if (matches.size() > 1) {
            // Click the second occurrence
            robot.clickOn(matches.stream().skip(1).findFirst().get());
        } else {
            // Fallback to first/only occurrence
            robot.clickOn(newModule);
        }
        waitForFxEvents();
        LOG.info("Updated module to: {}", newModule);
        return this;
    }

    //update path by selecting in combobox
    public ConceptPane updatePath(String newPath) {
        robot.moveTo("Path");
        waitForFxEvents();
        robot.moveBy(100, 0); // Move right from the label to find the ComboBox
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        // Click the second occurrence of the path name (in the combobox dropdown)
        var matches = robot.lookup(newPath).queryAll();
        if (matches.size() > 1) {
            // Click the second occurrence
            robot.clickOn(matches.stream().skip(1).findFirst().get());
        } else {
            // Fallback to first/only occurrence
            robot.clickOn(newPath);
        }
        waitForFxEvents();
        LOG.info("Updated path to: {}", newPath);
        return this;
    }

    public ConceptPane clickEditReferenceComponentButton(){
        //button has "Edit Reference Component" tooltip
        Button editReferenceComponentButton = findButtonByTooltip("Edit Reference Component");
        if (editReferenceComponentButton != null) {
            robot.interact(editReferenceComponentButton::fire);
            waitForFxEvents();
            LOG.info("Clicked Edit Reference Component button");
        } else {
            LOG.warn("Edit Reference Component button not found");
        }
        return this;
    }
    
    /**
     * Clears the name field and enters new text.
     */
    public ConceptPane updateOtherName(String newName) {
        robot.moveTo("Edit Description: Other Name");
        waitForFxEvents();
        robot.moveBy(0, 45);
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        pressKey(KeyCode.DELETE);
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

    //searches for parent concept using the search panel in concept pane
    public ConceptPane searchForParentConcept(String parentConcept) {
        // Move to and click the search field
        robot.moveTo("enter search query");
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        
        // Additional click to ensure focus
        robot.clickOn();
        waitForFxEvents();
              
        // Enter search text and execute search
        robot.write(parentConcept);
        waitForFxEvents(); // Wait for text to be processed
        
        robot.press(KeyCode.ENTER);
        waitForFxEvents();
        robot.release(KeyCode.ENTER);
        waitForFxEvents(); // Ensure Enter is processed
       
        LOG.info("Searched for parent concept: {}", parentConcept);
        return this;

    }

    //Select the parent concept from the search results
    public ConceptPane selectParentConcept(String parentConcept) {
        waitForFxEvents();
        
        // Additional wait to ensure results are fully rendered
        waitFor(3000);
        
        robot.moveTo("Top component with score order");
        waitForFxEvents();
        robot.moveBy(0,25);
        waitForFxEvents();
        robot.doubleClickOn(parentConcept);
        waitForFxEvents();

        LOG.info("Selected parent concept: {}", parentConcept);
        return this;
    }

    //clicks search for a concept
    public ConceptPane clickSearchForConcept() {
        robot.moveTo("Choose replacement is-a");
        waitForFxEvents();
        robot.clickOn("Search for concept");
        waitForFxEvents(); // Wait for click to be processed
        // Wait for search popup to appear - verify it's actually open
        if (!waitForText("Top component with score order", 20, 500)) {
            LOG.error("Search popup did not appear after clicking 'Search for concept'");
            captureScreenshot("search_popup_not_appeared");
            throw new RuntimeException("Search popup failed to open");
        }
        waitForFxEvents(); // Ensure popup is stable
        LOG.info("Clicked Search for concept and popup opened");
        return this;
    }

    //drag to reference component search field
    public ConceptPane dragToReferenceComponentField() {
        waitFor(4000); // Wait for UI to stabilize
        // Initial mouse movement to activate mouse interactions
        robot.moveTo("SORT BY: TOP COMPONENT");
        waitForFxEvents();
        // Move to center of screen first to ensure mouse is active
        robot.moveBy(100, 0);
        waitForFxEvents();
        robot.moveBy(-100, 0);
        waitForFxEvents();
        // Now move to the drag source
        robot.moveTo("SORT BY: TOP COMPONENT");
        waitForFxEvents();
        robot.moveBy(0, 210); // Move down to the next element in the tree
        waitForFxEvents();
        // Single click to ensure element is focused
        robot.clickOn();
        waitForFxEvents();
        // Press and hold at current position for drag
        robot.press(MouseButton.PRIMARY);
        waitForFxEvents();
        // Small movement to initiate drag
        robot.moveBy(0, 5);
        waitForFxEvents();
        // Move to target while holding
        robot.moveTo("🔍  Search");
        waitForFxEvents();
        // Release to drop
        robot.release(MouseButton.PRIMARY);
        waitForFxEvents();
        LOG.info("Dragged concept to Reference Component field");
        return this;
    }

    public String getPopulatedReferenceComponent(){
        // get the text from beneath the text "CONCEPT" withing the Reference Component section
        robot.moveTo("CONCEPT");
        waitForFxEvents();
        robot.moveBy(0, 30); // Move down to the populated reference component text
        waitForFxEvents();
        javafx.scene.Node referenceComponentNode = robot.lookup((javafx.scene.Node node) -> {
            if (node instanceof javafx.scene.text.Text) {
                String text = ((javafx.scene.text.Text) node).getText();
                return text != null && !text.isEmpty();
            }
            return false;
        }).query();
        //return the text
        String populatedReferenceComponent = ((javafx.scene.text.Text) referenceComponentNode).getText();
        LOG.info("Populated Reference Component: {}", populatedReferenceComponent);
        return populatedReferenceComponent;
    }


     // Resizes the concept pane using drag and drop
      public ConceptPane resizeConceptPane() {
        Point2D handle = new Point2D(70, 50);
        robot.moveTo(handle);
        waitForFxEvents();
        robot.drag(MouseButton.PRIMARY).moveBy(100, 100).drop();
        waitForFxEvents();
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
