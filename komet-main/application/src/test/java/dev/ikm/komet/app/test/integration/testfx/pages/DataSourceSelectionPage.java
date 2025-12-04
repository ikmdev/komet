package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.scene.control.Button;
import org.testfx.api.FxRobot;

/**
 * Page object for the data source selection screen.
 */
public class DataSourceSelectionPage extends BasePage {
    
    private static final String SELECTOR_OK_BUTTON = "#okButton";
    
    public DataSourceSelectionPage(FxRobot robot) {
        super(robot);
    }
    
    /**
     * Selects a data source by clicking on its text.
     */
    public DataSourceSelectionPage selectDataSource(String dataSourceName) {
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
