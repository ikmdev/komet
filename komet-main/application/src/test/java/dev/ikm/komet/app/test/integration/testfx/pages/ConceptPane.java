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
        waitFor(300); // Ensure field is ready
        robot.write(newFullyQualifiedName);
        waitForFxEvents();
        waitFor(300); // Ensure text is processed
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
        waitFor(500); // Ensure stamp editor dialog opens
        LOG.info("Opened Stamp Editor");
        return this;
    }


    public ConceptPane updateStamp(String status, String module, String path) {
        waitForFxEvents();
        this.updateStatus(status);
        waitForFxEvents();
        waitFor(200);
        this.updateModule(module);
        waitForFxEvents();
        waitFor(200);
        this.updatePath(path);
        waitForFxEvents();
        waitFor(200);
        robot.clickOn("CONFIRM");
        waitForFxEvents();
        waitFor(500); // Ensure stamp is applied
        LOG.info("Updated stamp to - Status: {}, Module: {}, Path: {}", status, module, path);
        return this;
    }

    //update status by selecting in combobox
    public ConceptPane updateStatus(String newStatus) {
        waitFor(500);
        robot.moveTo("Status");
        waitForFxEvents();
        robot.moveBy(100, 0); // Move right from the label to find the ComboBox
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        waitFor(500); // Wait for dropdown to fully open
        
        // Look for ListCell containing the status text (dropdown items)
        var dropdownItems = robot.lookup(".list-cell").lookup(newStatus).queryAll();
        if (!dropdownItems.isEmpty()) {
            // Click the first item found in the dropdown
            robot.clickOn(dropdownItems.iterator().next());
        } else {
            // Fallback: try to find any occurrence that's not the label
            var matches = robot.lookup(newStatus).queryAll();
            if (matches.size() > 1) {
                // Click the last occurrence (most likely to be in dropdown)
                robot.clickOn(matches.stream().skip(matches.size() - 1).findFirst().get());
            } else {
                // Last resort: use arrow keys to navigate
                int maxAttempts = 20;
                for (int i = 0; i < maxAttempts; i++) {
                    robot.press(KeyCode.DOWN);
                    waitForFxEvents();
                    robot.release(KeyCode.DOWN);
                    waitForFxEvents();
                    if (robot.lookup(newStatus).tryQuery().isPresent()) {
                        robot.press(KeyCode.ENTER);
                        waitForFxEvents();
                        robot.release(KeyCode.ENTER);
                        waitForFxEvents();
                        break;
                    }
                }
            }
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
        waitFor(500); // Wait for dropdown to fully open
        
        // Look for ListCell containing the module text (dropdown items)
        var dropdownItems = robot.lookup(".list-cell").lookup(newModule).queryAll();
        if (!dropdownItems.isEmpty()) {
            // Click the first item found in the dropdown
            robot.clickOn(dropdownItems.iterator().next());
        } else {
            // Fallback: try to find any occurrence that's not the label
            var matches = robot.lookup(newModule).queryAll();
            if (matches.size() > 1) {
                // Click the last occurrence (most likely to be in dropdown)
                robot.clickOn(matches.stream().skip(matches.size() - 1).findFirst().get());
            } else {
                // Last resort: use arrow keys to navigate
                int maxAttempts = 20;
                for (int i = 0; i < maxAttempts; i++) {
                    robot.press(KeyCode.DOWN);
                    waitForFxEvents();
                    robot.release(KeyCode.DOWN);
                    waitForFxEvents();
                    if (robot.lookup(newModule).tryQuery().isPresent()) {
                        robot.press(KeyCode.ENTER);
                        waitForFxEvents();
                        robot.release(KeyCode.ENTER);
                        waitForFxEvents();
                        break;
                    }
                }
            }
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
        waitFor(500); // Wait for dropdown to fully open
        
        // Look for ListCell containing the path text (dropdown items)
        var dropdownItems = robot.lookup(".list-cell").lookup(newPath).queryAll();
        if (!dropdownItems.isEmpty()) {
            // Click the first item found in the dropdown
            robot.clickOn(dropdownItems.iterator().next());
        } else {
            // Fallback: try to find any occurrence that's not the label
            var matches = robot.lookup(newPath).queryAll();
            if (matches.size() > 1) {
                // Click the last occurrence (most likely to be in dropdown)
                robot.clickOn(matches.stream().skip(matches.size() - 1).findFirst().get());
            } else {
                // Fallback to first/only occurrence
                robot.clickOn(newPath);
            }
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
        robot.moveTo("Top component with score order");
        waitForFxEvents();
        robot.moveBy(0,-25);
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        waitFor(200);
        
        // Additional click to ensure focus
        robot.clickOn();
        waitForFxEvents();
        waitFor(300); // Ensure field is focused and ready
              
        // Enter search text and execute search
        robot.write(parentConcept);
        waitForFxEvents(); // Wait for text to be processed
        waitFor(500); // Ensure all characters are in the field
        
        robot.press(KeyCode.ENTER);
        waitForFxEvents();
        robot.release(KeyCode.ENTER);
        waitForFxEvents(); // Ensure Enter is processed
        waitFor(500); // Wait for search to execute
       
        LOG.info("Searched for parent concept: {}", parentConcept);
        return this;

    }

    //Select the parent concept from the search results
    public ConceptPane selectParentConcept(String parentConcept) {
        waitForFxEvents();
        
        // Additional wait to ensure results tree is fully rendered
        waitFor(3000);
        waitForFxEvents();        
        robot.moveTo("Top component with score order");
        waitForFxEvents();
        robot.moveBy(0, 25);
        waitForFxEvents();
        waitFor(300); // Stabilize before double-click
        robot.doubleClickOn();
        waitForFxEvents();
        waitFor(300); // Ensure selection is processed

        LOG.info("Selected parent concept: {}", parentConcept);
        return this;
    }

    //clicks search for a concept
    public ConceptPane clickSearchForConcept() {
        robot.moveTo("Choose replacement is-a");
        waitForFxEvents();
        robot.clickOn("Search for concept");
        waitForFxEvents(); // Wait for click to be processed
        waitFor(500); // Initial stabilization
        // Wait for search popup to appear - verify it's actually open
        if (!waitForText("Top component with score order", 30, 300)) {
            LOG.error("Search popup did not appear after clicking 'Search for concept'");
            captureScreenshot("search_popup_not_appeared");
            throw new RuntimeException("Search popup failed to open");
        }
        waitForFxEvents(); // Ensure popup is stable
        waitFor(500); // Additional stabilization for popup content
        LOG.info("Clicked Search for concept and popup opened");
        return this;
    }

    //drag to reference component search field
    public ConceptPane dragToReferenceComponentField() {
        waitFor(1000); // Initial stabilization

        
        // Locate the drag source
        robot.moveTo("SORT BY: TOP COMPONENT");
        waitForFxEvents();
        robot.moveBy(0, 210); // Move down to the element in the tree
        waitForFxEvents();
        waitFor(300); // Stabilize on element
        
            // Begin drag        
        robot.press(MouseButton.PRIMARY);
        waitForFxEvents();
        waitFor(300); // Hold before moving
        
        // Small movement to initiate drag gesture
        robot.moveBy(0, 10);
        waitForFxEvents();
        waitFor(200);
        
        // Continue dragging to target
        robot.moveTo("🔍  Search");
        waitForFxEvents();
        waitFor(300); // Ensure we're at target
        
        // Release to drop
        robot.release(MouseButton.PRIMARY);
        waitForFxEvents();
        waitFor(500); // Ensure drop is processed
        
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
