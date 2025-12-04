package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;

/**
 * Page object for Search panel operations.
 */
public class SearchPanel extends BasePage {
    
    public SearchPanel(FxRobot robot) {
        super(robot);
    }
    
    /**
     * Performs a search with the given query.
     */
    public SearchPanel search(String query) {
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
    public ConceptDetailsPage openConceptFromResults(String conceptName) {
        doubleClickOnText(conceptName);
        waitFor(2000);
        closeDialogs();
        LOG.info("Opened concept from search results: {}", conceptName);
        return new ConceptDetailsPage(robot);
    }
}
