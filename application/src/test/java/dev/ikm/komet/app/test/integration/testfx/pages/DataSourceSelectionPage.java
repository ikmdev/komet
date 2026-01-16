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
    
    public DataSourceSelectionPage(FxRobot robot) {
        super(robot);
    }
    
    /*
    *Selects Data Source Store from list
     */
    public DataSourceSelectionPage selectDataSourceStore(String dataSourceStore) {
        robot.moveTo("    data source:");
        robot.moveBy(50, 0);
        robot.clickOn();
        // Look for ListCell containing the path text (dropdown items)
        var dropdownItems = robot.lookup(".list-cell").lookup(dataSourceStore).queryAll();
        if (!dropdownItems.isEmpty()) {
            // Click the first item found in the dropdown
            robot.clickOn(dropdownItems.iterator().next());
        } else {
            // Fallback: try to find any occurrence that's not the label
            var matches = robot.lookup(dataSourceStore).queryAll();
            if (matches.size() > 1) {
                // Click the last occurrence (most likely to be in dropdown)
                robot.clickOn(matches.stream().skip(matches.size() - 1).findFirst().get());
            } else {
                // Fallback to first/only occurrence
                robot.clickOn(dataSourceStore);
            }
        }
        LOG.info("Selected Data Source Store");
        return this;
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

    /*
    * Create New Folder Name
    */
    public DataSourceSelectionPage createNewFolder(String folderName) {
        robot.moveTo("New folder name");
        robot.moveBy(50, 0);
        robot.clickOn();
        robot.write(folderName);
        LOG.info("Created New Folder: " + folderName);
        return this;
    }
    
    /**
     * Clicks the OK button to proceed.
     */
    public LoginPage clickOk() {
        robot.clickOn(SELECTOR_OK_BUTTON);
        waitFor(1000); // Wait for login screen to load
        LOG.info("Clicked OK button");
        return new LoginPage(robot);
    }
}
