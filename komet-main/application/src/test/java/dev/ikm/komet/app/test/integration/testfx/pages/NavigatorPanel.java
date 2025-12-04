package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import org.testfx.api.FxRobot;

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
        waitFor(500);
        LOG.info("Opened Home panel");
        return this;
    }
    
    /**
     * Opens the Nextgen Search panel.
     */
    public NavigatorPanel clickNextgenSearch() {
        ToggleButton nextgenSearchButton = findToggleButtonInNavigatorPane("Nextgen Search");
        robot.interact(nextgenSearchButton::fire);
        waitFor(1000);
        LOG.info("Opened Nextgen Search");
        return this;
    }

    /**
     * Opens the Nextgen Navigator panel.
     */
    public NavigatorPanel clickNextgenNavigator() {
        waitFor(2000); // Increased wait for UI to be ready after journal creation
        ToggleButton nextgenNavigatorButton = findToggleButtonInNavigatorPane("Nextgen Navigator");
        robot.interact(nextgenNavigatorButton::fire);
        waitFor(500);
        LOG.info("Opened Nextgen Navigator");
        return this;
    }

    //Clicks the Reasoner panel button
    public NavigatorPanel clickReasoner() {
        ToggleButton reasonerButton = findToggleButtonInNavigatorPane("Reasoner");
        robot.interact(reasonerButton::fire);
        waitFor(500);
        LOG.info("Opened Reasoner panel");
        return this;
    }

    //Clicks the Search panel button
    public NavigatorPanel clickSearch() {
        ToggleButton searchButton = findToggleButtonInNavigatorPane("Search");
        robot.interact(searchButton::fire);
        waitFor(500);
        LOG.info("Opened Search panel");
        return this;
    }

    //Clicks the Navigator panel button
    public NavigatorPanel clickNavigator() {
        ToggleButton navigatorButton = findToggleButtonInNavigatorPane("Navigator");
        robot.interact(navigatorButton::fire);
        waitFor(500);
        LOG.info("Opened Navigator panel");
        return this;
    }

    //Clicks the Nextgen Reasoner panel button
    public NavigatorPanel clickNextgenReasoner() {
        ToggleButton nextgenReasonerButton = findToggleButtonInNavigatorPane("Nextgen Reasoner");
        robot.interact(nextgenReasonerButton::fire);
        waitFor(500);
        LOG.info("Opened Nextgen Reasoner panel");
        return this;
    }

    //Clicks the Settings button
    public NavigatorPanel clickSettings() {
        ToggleButton settingsButton = findToggleButtonInNavigatorPane("Settings");
        robot.interact(settingsButton::fire);
        waitFor(500);
        LOG.info("Opened Settings panel");
        return this;
    }

    //Clicks the Profile button
    public NavigatorPanel clickProfile() {
        ToggleButton profileButton = findToggleButtonInNavigatorPane("Profile");
        robot.interact(profileButton::fire);
        waitFor(500);
        LOG.info("Opened Profile panel");
        return this;
    }
    
    /**
     * Clicks the Concepts button.
     */
    public NavigatorPanel clickConcepts() {
        clickOnText("CONCEPTS");
        LOG.info("Clicked CONCEPTS button");
        return this;
    }

    //Click the Patterns button
    public NavigatorPanel clickPatterns() {
        clickOnText("PATTERNS");
        LOG.info("Clicked PATTERNS button");
        return this;
    }
    
    /**
     * Expands a tree node by clicking the disclosure arrow.
     */
    public NavigatorPanel expandTreeNode(String nodeName) {
        waitFor(1000);
        Node node = robot.lookup(nodeName).query();
        robot.moveTo(node);
        robot.moveBy(-35, 0);
        robot.clickOn();
        waitFor(500);
        LOG.info("Expanded tree node: {}", nodeName);
        return this;
    }
    
    /**
     * Double-clicks on a tree item to open it.
     */
    public ConceptPane openConcept(String conceptName) {
        waitFor(1000);
        doubleClickOnText(conceptName);
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
    
        /**
     * Performs a search with the given query.
     */
    public NavigatorPanel search(String query) {
        clickOnText("🔍  Search");
        type(query);
        waitFor(2000);
        pressKey(KeyCode.ENTER);
        waitFor(2000);
        LOG.info("Searched for: {}", query);
        return this;
    }
    
    /**
     * Opens a concept from search results.
     */
    public ConceptPane openConceptFromResults(String conceptName) {
        doubleClickOnText(conceptName);
        waitFor(2000);
        closeDialogs();
        LOG.info("Opened concept from search results: {}", conceptName);
        return new ConceptPane(robot);
    }

        /**
     * Drags concept to editing area using the move button.
     */
    public NavigatorPanel dragToEditingArea(String conceptName) {
        waitFor(1000);
        
        javafx.scene.Node conceptText = robot.lookup(conceptName).query();
        Bounds conceptBounds = conceptText.localToScreen(conceptText.getBoundsInLocal());
        
        double moveButtonX = conceptBounds.getMinX() + 375;
        double moveButtonY = conceptBounds.getMinY() + (conceptBounds.getHeight() / 2);
        
        robot.moveTo(moveButtonX, moveButtonY);
        waitFor(300);
        robot.clickOn(MouseButton.PRIMARY);
        waitFor(200);
        robot.drag(MouseButton.PRIMARY).moveBy(50, 0).drop();
        waitFor(500);
        
        LOG.info("Dragged concept '{}' to editing area", conceptName);
        closeDialogs();
        return this;
    }
}
