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
 * Page object representing the Navigator Panel in the Komet application.
 * The Navigator Panel provides browsing and searching capabilities for
 * concepts, patterns, and other functionalities. It supports multiple views
 * including tree-based hierarchy navigation and search result displays.
 * 
 * Key Responsibilities:
 *   Opening and closing the NextGen Navigator, NextGen Search, Reasoner, etc. panels
 *   Switching between Concepts and Patterns tabs
 *   Performing searches across the terminology
 *   Opening search results to view details
 * 
 */


public class NavigatorPanel extends BasePage {

    private static final String SELECTOR_NEXTGEN_NAVIGATOR_BUTTON = "#navigatorToggleButton";
    private static final String SELECTOR_NEXTGEN_SEARCH_BUTTON = "#nextGenSearchToggleButton";
    private static final String SELECTOR_REASONER_BUTTON = "#reasonerToggleButton";
    private static final String SELECTOR_SEARCH_BUTTON = "#searchToggleButton";
    private static final String SELECTOR_NAVIGATOR_BUTTON = "#conceptNavigatorToggleButton";
    private static final String SELECTOR_NEXTGEN_REASONER_BUTTON = "#nextGenReasonerToggleButton";
    private static final String SELECTOR_SETTINGS_BUTTON = "#settingsToggleButton";
    private static final String SELECTOR_CREATE_BUTTON = "#addButton";
    private static final String SELECTOR_CONCEPTS_BUTTON = "#conceptsToggleButton";
    private static final String SELECTOR_PATTERNS_BUTTON = "#patternsToggleButton";


    
    public NavigatorPanel(FxRobot robot) {
        super(robot);
    }

    /**
     * Opens the Home panel.
     */
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
        robot.clickOn(SELECTOR_NEXTGEN_SEARCH_BUTTON);
        waitForFxEvents();
        LOG.info("Opened Nextgen Search");
        return this;
    }

    /**
     * Opens the Nextgen Navigator panel.
     */
    public NavigatorPanel clickNextgenNavigator() {
        robot.clickOn(SELECTOR_NEXTGEN_NAVIGATOR_BUTTON);
        waitForFxEvents();
        LOG.info("Opened Nextgen Navigator");
        return this;
    }

    /**
     * Opens the Reasoner panel.
     */
    public NavigatorPanel clickReasoner() {
        robot.clickOn(SELECTOR_REASONER_BUTTON);
        waitForFxEvents();
        LOG.info("Opened Reasoner panel");
        return this;
    }

    /**
     * Opens the Search panel.
     */
    public NavigatorPanel clickSearch() {
        robot.clickOn(SELECTOR_SEARCH_BUTTON);
        waitForFxEvents();
        LOG.info("Opened Search panel");
        return this;
    }

    /**
     * Opens the Navigator panel.
     */
    public NavigatorPanel clickNavigator() {
        robot.clickOn(SELECTOR_NAVIGATOR_BUTTON);
        waitForFxEvents();
        LOG.info("Opened Navigator panel");
        return this;
    }

    /**
     * Opens the Nextgen Reasoner panel.
     */
    public NavigatorPanel clickNextgenReasoner() {
        robot.clickOn(SELECTOR_NEXTGEN_REASONER_BUTTON);
        waitForFxEvents();
        LOG.info("Opened Nextgen Reasoner panel");
        return this;
    }

    /**
     * Clicks the Reasoner starburst button.
     */
    public NavigatorPanel clickReasonerStarburst() {
        // Move to button with name "+"
        robot.moveTo("+");
        waitForFxEvents();
        // Move down 30 pixels to the starburst button
        robot.moveBy(0, 30);
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        LOG.info("Clicked Reasoner starburst button");
        return this;
    }

    /**
     * Clicks the Create button in the journal window toolbar.
     * Waits for the button to appear with retry logic.
     */
    public NavigatorPanel clickCreate() {
        robot.clickOn(SELECTOR_CREATE_BUTTON);
        waitForFxEvents();
        LOG.info("Clicked Create button");
        return this;
    }

    
    /**
     * Clicks the Concepts button.
     */
    public NavigatorPanel clickConcepts() {
        clickOn(SELECTOR_CONCEPTS_BUTTON);
        waitForFxEvents();
        LOG.info("Clicked CONCEPTS button");
        return this;
    }

    /**
     * Clicks the PATTERNS button to switch to patterns view.
     */
    public NavigatorPanel clickPatterns() {
        clickOn(SELECTOR_PATTERNS_BUTTON);
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
     * Performs a search with Nextgen Search
     */
    public NavigatorPanel nextgenSearch(String query) {
        robot.moveTo("SORT BY: TOP COMPONENT");
        robot.moveBy(0, -40); // Move up to the search box
        //clear search box
        robot.clickOn();
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.press(KeyCode.BACK_SPACE).release(KeyCode.BACK_SPACE);
        waitForFxEvents();
        type(query);
        waitForFxEvents(); // Wait for text to be processed
        waitFor(500); // Wait for typing to settle
        robot.press(KeyCode.ENTER);
        robot.release(KeyCode.ENTER);
        robot.press(KeyCode.ENTER);
        robot.release(KeyCode.ENTER);
        waitForFxEvents(); // Ensure Enter is processed
        waitFor(2000);
        LOG.info("Searched for: {}", query);
        return this;
    }

    /**
     * Opens a specific search result by finding and double-clicking on it.
     * @param searchText The text to search for within the search results list
     */
    public NavigatorPanel openNextGenSearchResult(String searchText) {
        // Wait for search results to appear
        waitForFxEvents();
        waitFor(1000); // Wait for results to populate
        
        // Find the list control by screen coordinates [l=59,t=181,r=431,b=1032]
        var listControls = robot.lookup((javafx.scene.Node node) -> {
            return (node instanceof javafx.scene.control.ListView || 
                    node instanceof javafx.scene.control.TreeView) && 
                   node.isVisible();
        }).queryAll();
        
        javafx.scene.Node targetList = null;
        for (javafx.scene.Node node : listControls) {
            try {
                javafx.geometry.Bounds boundsInScreen = node.localToScreen(node.getBoundsInLocal());
                if (boundsInScreen != null) {
                    double left = boundsInScreen.getMinX();
                    double top = boundsInScreen.getMinY();
                    
                    // Match list at position left~59, top~181 (50px tolerance)
                    if (Math.abs(left - 59) < 50 && Math.abs(top - 181) < 50) {
                        LOG.info("Found target list at position [l={},t={}]", (int)left, (int)top);
                        targetList = node;
                        break;
                    }
                }
            } catch (Exception e) {
                LOG.warn("Failed to check list coordinates: {}", e.getMessage());
            }
        }
        
        if (targetList == null) {
            throw new RuntimeException("Could not find list at position [l=59,t=181]");
        }
        
        // Find the text within the list
        javafx.scene.Node resultNode = robot.from(targetList).lookup((javafx.scene.Node node) -> {
            if (node instanceof javafx.scene.text.Text) {
                String text = ((javafx.scene.text.Text) node).getText();
                return text != null && text.contains(searchText);
            }
            return false;
        }).query();
        
        robot.doubleClickOn(resultNode);
        waitForFxEvents();
        LOG.info("Opened search result: {}", searchText);
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

    public NavigatorPanel scrollPatternResults(String patternName) {
        ToggleButton nextgenNavigatorButton = findToggleButtonInNavigatorPane("Nextgen Navigator");
        robot.moveTo(nextgenNavigatorButton);
        waitForFxEvents();
        robot.moveBy(35, 0);
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        for (int i = 0; i < 6; i++) {
            robot.press(KeyCode.DOWN);
            waitForFxEvents();
            robot.release(KeyCode.DOWN);
            waitForFxEvents();
            }
        LOG.info("Scrolled to pattern: {}", patternName);
        return this;
    }

}
