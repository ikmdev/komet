package dev.ikm.komet.app.test.integration.testfx.pages;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

import org.testfx.api.FxRobot;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Page object for Navigator panel operations.
 */
public class NavigatorPanel extends BasePage {
    
    public NavigatorPanel(FxRobot robot) {
        super(robot);
    }

    //Clicks the Home panel button
    public NavigatorPanel clickHome() {
        ToggleButton homeButton = findToggleButtonInNavigatorPane("Home");
        robot.interact(homeButton::fire);
        waitForFxEvents();
        LOG.info("Opened Home panel");
        return this;
    }
    
    /**
     * Opens the Nextgen Search panel.
     */
    public NavigatorPanel clickNextgenSearch() {
        waitFor(1000); // Ensure UI is ready after journal creation
        ToggleButton nextgenSearchButton = findToggleButtonInNavigatorPane("Nextgen Search");
        robot.interact(nextgenSearchButton::fire);
        waitForFxEvents();
        LOG.info("Opened Nextgen Search");
        return this;
    }

    /**
     * Opens the Nextgen Navigator panel.
     */
    public NavigatorPanel clickNextgenNavigator() {
        waitForFxEvents(); // Ensure UI is ready after journal creation
        ToggleButton nextgenNavigatorButton = findToggleButtonInNavigatorPane("Nextgen Navigator");
        robot.interact(nextgenNavigatorButton::fire);
        waitForFxEvents();
        LOG.info("Opened Nextgen Navigator");
        return this;
    }

    //Clicks the Reasoner panel button
    public NavigatorPanel clickReasoner() {
        ToggleButton reasonerButton = findToggleButtonInNavigatorPane("Reasoner");
        robot.interact(reasonerButton::fire);
        waitForFxEvents();
        LOG.info("Opened Reasoner panel");
        return this;
    }

    //Clicks the Search panel button
    public NavigatorPanel clickSearch() {
        ToggleButton searchButton = findToggleButtonInNavigatorPane("Search");
        robot.interact(searchButton::fire);
        waitForFxEvents();
        LOG.info("Opened Search panel");
        return this;
    }

    //Clicks the Navigator panel button
    public NavigatorPanel clickNavigator() {
        ToggleButton navigatorButton = findToggleButtonInNavigatorPane("Navigator");
        robot.interact(navigatorButton::fire);
        waitForFxEvents();
        LOG.info("Opened Navigator panel");
        return this;
    }

    //Clicks the Nextgen Reasoner panel button
    public NavigatorPanel clickNextgenReasoner() {
        ToggleButton nextgenReasonerButton = findToggleButtonInNavigatorPane("Nextgen Reasoner");
        robot.interact(nextgenReasonerButton::fire);
        waitForFxEvents();
        LOG.info("Opened Nextgen Reasoner panel");
        return this;
    }

    //Clicks the Create button
    public NavigatorPanel clickCreate() {
        // Wait for the Create button to appear (journal window needs time to load)
        javafx.scene.control.Button createButton = null;
        int maxAttempts = 4; // 4 attempts * 100ms = 400ms max
        
        for (int i = 0; i < maxAttempts; i++) {
            try {
                createButton = findButtonInNavigatorPane("Create");
                if (createButton != null) {
                    LOG.info("Found Create button after {} attempts", i + 1);
                    break;
                }
            } catch (Exception e) {
                // Button not found yet, continue waiting
            }
            waitForFxEvents();
        }
        
        if (createButton == null) {
            LOG.error("Create button not found after waiting 400ms");
            throw new RuntimeException("Create button with tooltip 'Create' not found after waiting 400ms");
        }
        
        robot.interact(createButton::fire);
        waitForFxEvents();
        LOG.info("Clicked Create button");
        return this;
    }

    
    /**
     * Clicks the Concepts button.
     */
    public NavigatorPanel clickConcepts() {
        clickOnText("CONCEPTS");
        waitForFxEvents();
        LOG.info("Clicked CONCEPTS button");
        return this;
    }

    //Click the Patterns button
    public NavigatorPanel clickPatterns() {
        clickOnText("PATTERNS");
        waitForFxEvents();
        LOG.info("Clicked PATTERNS button");
        return this;
    }
    
    /**
     * Expands a tree node by clicking the disclosure arrow.
     */
    public NavigatorPanel expandTreeNode(String nodeName) {
        waitForFxEvents();
        Node node = robot.lookup(nodeName).query();
        robot.moveTo(node);
        waitForFxEvents();
        robot.moveBy(-35, 0);
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        LOG.info("Expanded tree node: {}", nodeName);
        return this;
    }
    
    /**
     * Double-clicks on a tree item to open it.
     */
    public ConceptPane openConcept(String conceptName) {
        doubleClickOnText(conceptName);
        waitForFxEvents();
        LOG.info("Opened concept: {}", conceptName);
        closeDialogs();
        return new ConceptPane(robot);
    }
    
    private ToggleButton findToggleButtonInNavigatorPane(String tooltipText) {
        // Directly search for toggle button by tooltip text without pane constraints
        return (ToggleButton) robot.lookup((java.util.function.Predicate<javafx.scene.Node>) n -> {
            if (n instanceof ToggleButton) {
                ToggleButton toggleBtn = (ToggleButton) n;
                Tooltip tooltip = toggleBtn.getTooltip();
                return tooltip != null && tooltipText.equals(tooltip.getText());
            }
            return false;
        }).query();
    }
    
    private javafx.scene.control.Button findButtonInNavigatorPane(String tooltipText) {
        // Directly search for button by tooltip text without pane constraints
        return (javafx.scene.control.Button) robot.lookup((java.util.function.Predicate<javafx.scene.Node>) n -> {
            if (n instanceof javafx.scene.control.Button) {
                javafx.scene.control.Button btn = (javafx.scene.control.Button) n;
                Tooltip tooltip = btn.getTooltip();
                return tooltip != null && tooltipText.equals(tooltip.getText());
            }
            return false;
        }).query();
    }
    
        /**
     * Performs a search with the given query.
     */
    public NavigatorPanel search(String query) {
        clickOnText("🔍  Search");
        waitForFxEvents(); // Wait for click to be processed
        type(query);
        waitForFxEvents(); // Wait for text to be processed
        robot.press(KeyCode.ENTER);
        waitForFxEvents();
        robot.release(KeyCode.ENTER);
        waitForFxEvents(); // Ensure Enter is processed
        LOG.info("Searched for: {}", query);
        return this;
    }
    
    /**
     * Opens a concept from search results.
     */
    public ConceptPane openConceptFromResults(String conceptName) {
        doubleClickOnText(conceptName);
        waitForFxEvents();
        closeDialogs();
        LOG.info("Opened concept from search results: {}", conceptName);
        return new ConceptPane(robot);
    }

        /**
     * Drags concept to editing area using the move button.
     */
    public NavigatorPanel dragToEditingArea(String conceptName) {
        
        javafx.scene.Node conceptText = robot.lookup(conceptName).query();
        Bounds conceptBounds = conceptText.localToScreen(conceptText.getBoundsInLocal());
        
        double moveButtonX = conceptBounds.getMinX() + 375;
        double moveButtonY = conceptBounds.getMinY() + (conceptBounds.getHeight() / 2);
        
        robot.moveTo(moveButtonX, moveButtonY);
        waitForFxEvents();
        robot.clickOn(MouseButton.PRIMARY);
        waitForFxEvents();
        robot.drag(MouseButton.PRIMARY).moveBy(50, 0).drop();
        waitForFxEvents();
        
        LOG.info("Dragged concept '{}' to editing area", conceptName);
        closeDialogs();
        return this;
    }

        //scroll horizontally to the left of right and how many times using the direction and amount
    public NavigatorPanel scrollHorizontally(String direction, int amount) {
                        //click in the center of the screen
                        robot.clickOn(250,250);
                        for (int i = 0; i < amount; i++) {
                                //PRESS THE RIGHT DIRECTIONAL ARROW KEY
                                robot.press(KeyCode.valueOf(direction));
                                robot.release(KeyCode.valueOf(direction));
                }
        LOG.info("Scrolled horizontally {} by {} times", direction, amount);
        return this;
    }
}
