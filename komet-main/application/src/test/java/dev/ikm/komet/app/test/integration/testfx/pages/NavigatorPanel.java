package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import org.testfx.api.FxRobot;

/**
 * Page object for Navigator panel operations.
 */
public class NavigatorPanel extends BasePage {
    
    public NavigatorPanel(FxRobot robot) {
        super(robot);
    }
    
    /**
     * Opens the Nextgen Navigator panel.
     */
    public NavigatorPanel openNavigator() {
        waitFor(3000); // Increased wait for UI to be ready after journal creation
        ToggleButton nextgenNavigatorButton = findToggleButtonInNavigatorPane("Nextgen Navigator");
        robot.interact(nextgenNavigatorButton::fire);
        waitFor(1000);
        LOG.info("Opened Nextgen Navigator");
        return this;
    }
    
    /**
     * Opens the Nextgen Search panel.
     */
    public SearchPanel openCloseSearch() {
        ToggleButton nextgenSearchButton = findToggleButtonInNavigatorPane("Nextgen Search");
        robot.interact(nextgenSearchButton::fire);
        waitFor(2000);
        LOG.info("Opened Nextgen Search");
        return new SearchPanel(robot);
    }
    
    /**
     * Clicks the Concepts button.
     */
    public NavigatorPanel clickConcepts() {
        clickOnText("CONCEPTS");
        LOG.info("Clicked CONCEPTS button");
        return this;
    }
    
    /**
     * Expands a tree node by clicking the disclosure arrow.
     */
    public NavigatorPanel expandTreeNode(String nodeName) {
        waitFor(2000);
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
    public ConceptDetailsPage openConcept(String conceptName) {
        waitFor(2000);
        doubleClickOnText(conceptName);
        LOG.info("Opened concept: {}", conceptName);
        closeDialogs();
        return new ConceptDetailsPage(robot);
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
}
