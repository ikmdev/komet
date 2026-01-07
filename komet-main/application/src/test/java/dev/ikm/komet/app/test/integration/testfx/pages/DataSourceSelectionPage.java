package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.scene.control.Button;
import org.testfx.api.FxRobot;

/**
 * Page object representing the Data Source Selection screen in the Komet application.
 * This page allows users to select a data source from available options (e.g., SOLOR-GUDID)
 * and proceed to the next step in the application launch workflow.
 * 
 * Key Responsibilities:
 *   Selecting data sources from the list view
 *   Handling scrolling to find data sources not immediately visible
 *   Clicking the OK button to confirm selection
 *   Navigating to the Login page after data source selection
 * 
 */
public class DataSourceSelectionPage extends BasePage {
    
    private static final String SELECTOR_OK_BUTTON = "#okButton";
    
    public DataSourceSelectionPage(FxRobot robot) {
        super(robot);
    }
    
    /**
     * Selects a data source by clicking on its text.
     * Scrolls down if the data source is not visible.
     */
    public DataSourceSelectionPage selectDataSource(String dataSourceName) {
        // Try to find the data source, scroll if not visible
        int maxScrollAttempts = 10;
        boolean found = false;
        
        for (int i = 0; i < maxScrollAttempts; i++) {
            try {
                // Check if the data source is visible
                if (robot.lookup(dataSourceName).tryQuery().isPresent()) {
                    found = true;
                    LOG.info("Found data source: {} after {} scroll attempts", dataSourceName, i);
                    break;
                }
                // Scroll down to find the data source
                scrollDown();
                waitFor(300);
            } catch (Exception e) {
                // Continue scrolling
                scrollDown();
                waitFor(300);
            }
        }
        
        if (!found) {
            LOG.warn("Data source not found after scrolling, attempting to click anyway: {}", dataSourceName);
        }
        clickOnText(dataSourceName);
        LOG.info("Selected data source: {}", dataSourceName);
        return this;
    }
    
    /**
     * Clicks the OK button to proceed.
     */
    public LoginPage clickOk() {
        Button okButton = lookup(SELECTOR_OK_BUTTON, Button.class);
        robot.clickOn(okButton);
        waitFor(1000); // Wait for login screen to load
        LOG.info("Clicked OK button");
        return new LoginPage(robot);
    }
}
