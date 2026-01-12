package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import org.testfx.api.FxRobot;
import javafx.scene.control.Tooltip;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Page object representing the Concept Details pane in the Komet application.
 * This is a comprehensive interface for viewing and editing concept information including
 * descriptions, axioms, properties, stamps, semantic details, and reference components.
 * It supports both viewing existing concepts and authoring new concepts with full metadata.
 * 
 * Key Responsibilities:
 *   Editing concept descriptions (Fully Qualified Name, Other Names)
 *   Managing concept properties and toggles
 *   Updating stamp information (Status, Module, Path)
 *   Editing axioms
 *   Managing reference components for semantic patterns
 *   Editing semantic details for pattern-based semantics
 *   Searching and selecting parent concepts
 *   Copying concept identifiers (UUID)
 *   Submitting changes and handling validation
 *   Drag-and-drop operations for reference components
 */
public class ConceptPane extends BasePage {
    
    private static final String SELECTOR_PROPERTIES_TOGGLE = "#propertiesToggleButton";
    private static final String SELECTOR_ADD_REFERENCE_BUTTON = "#addReferenceButton";

    
    public ConceptPane(FxRobot robot) {
        super(robot);
    }

    /**
     * Clicks the Properties toggle button.
     */
    public ConceptPane clickPropertiesToggle() {
        robot.clickOn(SELECTOR_PROPERTIES_TOGGLE);
        waitForFxEvents();
        LOG.info("Clicked Properties toggle");
        return this;
    }

    /**
     * Clicks the Edit Descriptions button using tooltip lookup.
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

    /**
     * Clicks the Edit Descriptions button using tooltip lookup.
     */
    public ConceptPane clickEditDefinitionsButton() {
        Button editDefinitionsButton = findButtonByTooltip("Edit Definitions");
        if (editDefinitionsButton != null) {
            robot.interact(editDefinitionsButton::fire);
            waitForFxEvents();
            LOG.info("Clicked Edit Definitions button");
        } else {
            LOG.warn("Edit Definitions button not found");
        }
        return this;
    }

        /**
     * Clicks the Edit Descriptions button using tooltip lookup.
     */
    public ConceptPane clickEditFieldsButton() {
        Button editFieldsButton = findButtonByTooltip("Edit Fields");
        if (editFieldsButton != null) {
            robot.interact(editFieldsButton::fire);
            waitForFxEvents();
            LOG.info("Clicked Edit Fields button");
        } else {
            LOG.warn("Edit Fields button not found");
        }
        return this;
    }


    /**
     * Clicks the Copy button next to the Komet ID.
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

    /**
     * Clicks the Edit Axioms button using tooltip lookup.
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

    //Click Pattern Definition button using tooltip lookup
    public ConceptPane clickEditDefinitionButton() {
        Button patternDefinitionButton = findButtonByTooltip("Edit Definition");
        if (patternDefinitionButton != null) {
            robot.interact(patternDefinitionButton::fire);
            waitForFxEvents();
            LOG.info("Clicked Pattern Definition button");
        } else {
            LOG.warn("Pattern Definition button not found");
        }
        return this;
    }

    /**
     * Clicks the Coordinates button using tooltip lookup.
     * @param paneName The name of the pane containing the button
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

    //Click publish button using tooltip lookup
    public ConceptPane clickPublishButton() {
        Button publishButton = findButtonByTooltip("Submit");
        if (publishButton != null) {
            robot.interact(publishButton::fire);
            waitForFxEvents();
            LOG.info("Clicked Publish button");
        } else {
            LOG.warn("Publish button not found");
        }
        return this;
    }

    /**
     * Clicks on the OTHER NAMES section to edit.
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

    /**
     * Updates the Fully Qualified Name field.
     * @param newFullyQualifiedName The new fully qualified name to enter
     */
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

    /**
     * Updates the Name field.
     * @param newName The new name to enter
     */
    public ConceptPane updateName(String newName) {
        robot.clickOn("Enter Name");
        waitForFxEvents();
        waitFor(300); // Ensure field is ready
        robot.write(newName);
        waitForFxEvents();
        waitFor(300); // Ensure text is processed
        LOG.info("Updated name to: {}", newName);
        return this;
    }

    /**
     * Opens the stamp editor dialog.
     */
    public ConceptPane openStampEditor() {
        robot.clickOn("#stampViewControl");
        waitForFxEvents();
        LOG.info("Opened Stamp Editor");
        return this;
    }

    /**
     * Updates the stamp with status, module, and path.
     * @param status The status to set
     * @param module The module to select
     * @param path The path to select
     */
    public ConceptPane updateStamp(String status, String module, String path) {
        waitForFxEvents();
        waitFor(500);
        this.updateStatus(status);
        waitForFxEvents();
        waitFor(200);
        this.updateModule(module);
        waitForFxEvents();
        waitFor(200);
        this.updatePath(path);
        waitForFxEvents();
        waitFor(200);
        clickConfirm();
        waitForFxEvents();
        waitFor(500); // Ensure stamp is applied
        LOG.info("Updated stamp to - Status: {}, Module: {}, Path: {}", status, module, path);
        return this;
    }

    /**
     * Updates the status by selecting from the dropdown.
     * @param newStatus The status to select
     */
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

    /**
     * Updates the module by selecting from the dropdown.
     * @param newModule The module to select
     */
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

    /**
     * Updates the path by selecting from the dropdown.
     * @param newPath The path to select
     */
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

        /**
     * Updates the case significance by selecting from the dropdown.
     * @param caseSignificance The case significance to select
     */
    public ConceptPane updateCaseSignificance(String caseSignificance) {
        robot.moveTo("Case Significance");
        waitForFxEvents();
        robot.moveBy(100, 0); // Move right from the label to find the ComboBox
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        waitFor(500); // Wait for dropdown to fully open
        
        // Look for ListCell containing the path text (dropdown items)
        var dropdownItems = robot.lookup(".list-cell").lookup(caseSignificance).queryAll();
        if (!dropdownItems.isEmpty()) {
            // Click the first item found in the dropdown
            robot.clickOn(dropdownItems.iterator().next());
        } else {
            // Fallback: try to find any occurrence that's not the label
            var matches = robot.lookup(caseSignificance).queryAll();
            if (matches.size() > 1) {
                // Click the last occurrence (most likely to be in dropdown)
                robot.clickOn(matches.stream().skip(matches.size() - 1).findFirst().get());
            } else {
                // Fallback to first/only occurrence
                robot.clickOn(caseSignificance);
            }
        }
        waitForFxEvents();
        LOG.info("Updated case significance to: {}", caseSignificance);
        return this;
    }

        public ConceptPane updateLanguage(String language) {
        robot.moveTo("Language");
        waitForFxEvents();
        robot.moveBy(100, 0); // Move right from the label to find the ComboBox
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        waitFor(500); // Wait for dropdown to fully open
        
        // Look for ListCell containing the path text (dropdown items)
        var dropdownItems = robot.lookup(".list-cell").lookup(language).queryAll();
        if (!dropdownItems.isEmpty()) {
            // Click the first item found in the dropdown
            robot.clickOn(dropdownItems.iterator().next());
        } else {
            // Fallback: try to find any occurrence that's not the label
            var matches = robot.lookup(language).queryAll();
            if (matches.size() > 1) {
                // Click the last occurrence (most likely to be in dropdown)
                robot.clickOn(matches.stream().skip(matches.size() - 1).findFirst().get());
            } else {
                // Fallback to first/only occurrence
                robot.clickOn(language);
            }
        }
        waitForFxEvents();
        LOG.info("Updated language to: {}", language);
        return this;
    }

            public ConceptPane updateDataType(String dataType) {
        robot.moveTo("Data type");
        waitForFxEvents();
        robot.moveBy(0, 25);
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        waitFor(500); // Wait for dropdown to fully open
        
        // Look for ListCell containing the path text (dropdown items)
        var dropdownItems = robot.lookup(".list-cell").lookup(dataType).queryAll();
        if (!dropdownItems.isEmpty()) {
            // Click the first item found in the dropdown
            robot.clickOn(dropdownItems.iterator().next());
        } else {
            // Fallback: try to find any occurrence that's not the label
            var matches = robot.lookup(dataType).queryAll();
            if (matches.size() > 1) {
                // Click the last occurrence (most likely to be in dropdown)
                robot.clickOn(matches.stream().skip(matches.size() - 1).findFirst().get());
            } else {
                // Fallback to first/only occurrence
                robot.clickOn(dataType);
            }
        }
        waitForFxEvents();
        LOG.info("Updated data type to: {}", dataType);
        return this;
    }

    public ConceptPane clickEditReferenceComponentButton(){
        robot.clickOn(SELECTOR_ADD_REFERENCE_BUTTON);
        waitForFxEvents();
            LOG.info("Clicked Edit Reference Component button");
        return this;
    }

    public ConceptPane clickEditSemanticDetailsButton() {
        //button has "Semantic Details" tooltip
        Button semanticDetailsButton = findButtonByTooltip("Edit Details");
        if (semanticDetailsButton != null) {
            robot.interact(semanticDetailsButton::fire);
            waitForFxEvents();
            LOG.info("Clicked Semantic Edit Details button");
        } else {
            LOG.warn("Semantic Edit Details button not found");
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
            robot.clickOn("SUBMIT");
            LOG.info("Clicked Submit button using CSS selector");
        } catch (Exception e1) {
            try {
                scrollDown();
                robot.clickOn("SUBMIT");
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

    //Click CONFIRM button
    public ConceptPane clickConfirm() {
        try {
            robot.clickOn("CONFIRM");
            LOG.info("Clicked CONFIRM button using CSS selector");
        } catch (Exception e1) {
            try {
                scrollDown();
                robot.clickOn("CONFIRM");
                LOG.info("Clicked CONFIRM button using CSS selector after scrolling");
            } catch (Exception e2) {
                try {
                    clickOnText("CONFIRM");
                    LOG.info("Clicked CONFIRM button using text");
                } catch (Exception e3) {
                    LOG.warn("Could not find CONFIRM button");
                }
            }
        }
        return this;
    }

    /**
     * Searches for parent concept using the search panel in concept pane.
     * @param parentConceptName The name of the parent concept to search for
     */
    public ConceptPane searchForParentConcept(String parentConcept) {
        // Ensure search window is stable before interacting
        waitFor(2000);
        waitForFxEvents();
        
        // Focus the search window explicitly and verify it's focused
        try {
            robot.targetWindow().requestFocus();
            waitForFxEvents();
            waitFor(1000);
            LOG.info("Search window focused for typing");
        } catch (Exception e) {
            LOG.warn("Could not explicitly focus search window: {}", e.getMessage());
        }
        
        // Wait longer for the search field with more attempts
        if (!waitForText("enter search query", 30, 300)) {
            LOG.error("Search field 'enter search query' not found - window may have closed");
            captureScreenshot("search_field_not_found");
            throw new RuntimeException("Search field not found - window closed unexpectedly");
        }
        
        LOG.info("Search field found, looking up TextField node");
        waitForFxEvents();
        waitFor(500);
        
        // Find the TextField with exact prompt text "enter search query" that's in the active window
        try {
            var textFields = robot.lookup((javafx.scene.Node node) -> {
                if (node instanceof javafx.scene.control.TextField) {
                    javafx.scene.control.TextField tf = (javafx.scene.control.TextField) node;
                    // Look for TextField with exact prompt text "enter search query", visible, editable, and focusTraversable
                    return tf.isVisible() && 
                           tf.isEditable() &&
                           tf.isFocusTraversable() &&
                           tf.getPromptText() != null && 
                           tf.getPromptText().equalsIgnoreCase("enter search query") &&
                           tf.getScene() != null &&
                           tf.getScene().getWindow() != null &&
                           tf.getScene().getWindow().isFocused();
                }
                return false;
            }).queryAll();
            
            LOG.info("Found {} TextField nodes with exact prompt 'enter search query' in focused window", textFields.size());
            
            if (textFields.isEmpty()) {
                // Try without the window focus check
                LOG.warn("No TextField in focused window, trying all visible ones");
                textFields = robot.lookup((javafx.scene.Node node) -> {
                    if (node instanceof javafx.scene.control.TextField) {
                        javafx.scene.control.TextField tf = (javafx.scene.control.TextField) node;
                        return tf.isVisible() && 
                               tf.isEditable() &&
                               tf.getPromptText() != null && 
                               tf.getPromptText().equalsIgnoreCase("enter search query");
                    }
                    return false;
                }).queryAll();
                LOG.info("Found {} TextField nodes total", textFields.size());
            }
            
            if (textFields.isEmpty()) {
                throw new RuntimeException("TextField with exact prompt 'enter search query' not found");
            }
            
            // Find the correct TextField by checking screen position (looking for one near top=622)
            javafx.scene.control.TextField searchField = null;
            LOG.info("Found {} TextFields, checking screen bounds to find the correct one", textFields.size());
            
            int index = 0;
            for (javafx.scene.Node node : textFields) {
                javafx.scene.control.TextField tf = (javafx.scene.control.TextField) node;
                index++;
                
                try {
                    // Get the screen bounds of this TextField
                    javafx.geometry.Bounds boundsInScreen = tf.localToScreen(tf.getBoundsInLocal());
                    
                    if (boundsInScreen != null) {
                        double left = boundsInScreen.getMinX();
                        double top = boundsInScreen.getMinY();
                        double right = boundsInScreen.getMaxX();
                        double bottom = boundsInScreen.getMaxY();
                        
                        LOG.info("TextField #{} - Screen bounds: [l={},t={},r={},b={}], Visible: {}, Focusable: {}", 
                                index, (int)left, (int)top, (int)right, (int)bottom,
                                tf.isVisible(), tf.isFocusTraversable());
                        
                        // Look for TextField with top coordinate around 622 (within 50 pixels tolerance)
                        // and left coordinate around 229
                        if (Math.abs(top - 622) < 50 && Math.abs(left - 229) < 50) {
                            LOG.info("TextField #{} matches expected position (top~622, left~229) - selecting this one!", index);
                            searchField = tf;
                            break;
                        }
                    } else {
                        LOG.warn("TextField #{} has null screen bounds", index);
                    }
                } catch (Exception e) {
                    LOG.warn("TextField #{} failed to get bounds: {}", index, e.getMessage());
                }
            }
            
            if (searchField == null) {
                LOG.warn("No TextField found at expected position, using first visible one");
                // Fallback to first visible TextField
                for (javafx.scene.Node node : textFields) {
                    javafx.scene.control.TextField tf = (javafx.scene.control.TextField) node;
                    if (tf.isVisible() && tf.isFocusTraversable()) {
                        searchField = tf;
                        break;
                    }
                }
            }
            
            if (searchField == null) {
                searchField = (javafx.scene.control.TextField) textFields.iterator().next();
            }
            
            LOG.info("Selected TextField - Prompt: '{}', Editable: {}, Visible: {}, Focused: {}, Window focused: {}", 
                    searchField.getPromptText(), searchField.isEditable(), searchField.isVisible(), 
                    searchField.isFocused(), searchField.getScene().getWindow().isFocused());
            
            waitForFxEvents();
            waitFor(500);
            
            // Click the TextField to focus it
            robot.clickOn(searchField);
            waitForFxEvents();
            waitFor(1000);
            
            // Verify the field is now focused
            if (!searchField.isFocused()) {
                LOG.error("TextField is NOT focused after click - trying again");
                robot.clickOn(searchField);
                waitForFxEvents();
                waitFor(800);
            }
            
            LOG.info("TextField ready, focused: {}, ready to type", searchField.isFocused());
            
        } catch (Exception e) {
            LOG.error("Failed to find or focus TextField", e);
            captureScreenshot("failed_to_find_textfield");
            throw new RuntimeException("Failed to find or focus TextField", e);
        }
              
        // Enter search text and execute search
        LOG.info("Starting to type: {}", parentConcept);
        robot.write(parentConcept);
        waitForFxEvents(); // Wait for text to be processed
        waitFor(1000); // Ensure all characters are in the field
        
        LOG.info("Pressing ENTER to search");
        robot.press(KeyCode.ENTER);
        waitForFxEvents();
        robot.release(KeyCode.ENTER);
        waitForFxEvents(); // Ensure Enter is processed
        waitFor(1000); // Wait for search to execute
       
        LOG.info("Searched for parent concept: {}", parentConcept);
        return this;
    }

    /**
     * Selects the parent concept from the search results.
     * @param parentConceptName The name of the parent concept to select
     */
    public ConceptPane selectParentConcept(String parentConcept) {
        waitForFxEvents();
        
        // Additional wait to ensure results tree is fully rendered
        waitFor(3500);
        waitForFxEvents();     
        //robot.moveTo("Top component with score order");
        //waitForFxEvents();
        robot.moveBy(0, 80);
        waitForFxEvents();
        waitFor(2000); // Stabilize before double-click
        
        // Then double-click to select
        robot.doubleClickOn();
        waitForFxEvents();
        waitFor(1000); // Ensure selection is processed

        LOG.info("Selected parent concept: {}", parentConcept);
        return this;
    }

    /**
     * Clicks the search for concept button.
     */
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
        
        // Get the currently focused window (should be the search popup)
        waitForFxEvents();
        try {
            robot.targetWindow().requestFocus();
            waitForFxEvents();
            waitFor(300); // Allow focus to take effect
            LOG.info("Search window focused successfully");
        } catch (Exception e) {
            LOG.warn("Could not explicitly focus search window: {}", e.getMessage());
        }
        
        waitForFxEvents(); // Ensure popup is stable
        waitFor(500); // Additional stabilization for popup content
        LOG.info("Clicked Search for concept and popup opened");
        return this;
    }

    /**
     * Drags a concept to the reference component search field.
     */
    public ConceptPane dragToReferenceComponentField() {
        // Wait for search results to appear
        waitForFxEvents();
        waitFor(1500); // Wait for results to populate

        // move to the second instance of the concept name in the results
        robot.moveTo("SORT BY: TOP COMPONENT");
        waitForFxEvents();
        robot.moveBy(0, 50); // Move down to results area
        waitForFxEvents();
        waitFor(500);

        robot.press(MouseButton.PRIMARY)
                .moveTo("🔍  Search")
                .release(MouseButton.PRIMARY);
        waitForFxEvents();

        LOG.info("Dragged concept to Reference Component field (via located search entry)");

        return this;
    }

    /**
     * Validates that the reference component was populated successfully.
     */
    public ConceptPane getPopulatedReferenceComponent(){
        // Validate that success popup appears
        if (!waitForText("Semantic Details Added Successfully!", 20, 500)) {
            LOG.error("Success popup 'Semantic Details Added Successfully!' did not appear");
            captureScreenshot("success_popup_not_found");
            throw new RuntimeException("Success popup did not appear after adding semantic details");
        }
        LOG.info("Success popup 'Semantic Details Added Successfully!' appeared - validation successful");
        waitForFxEvents();
        waitFor(1000); // Wait for popup to be fully visible
        
        return this;
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
