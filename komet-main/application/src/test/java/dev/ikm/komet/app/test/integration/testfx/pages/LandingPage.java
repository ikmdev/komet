package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

import org.testfx.api.FxRobot;

/**
 * Page object for the main landing page.
 */
public class LandingPage extends BasePage {
    
    private static final String SELECTOR_LANDING_PAGE_BORDER_PANE = "#landingPageBorderPane";
    
    public LandingPage(FxRobot robot) {
        super(robot);
    }
    
    /**
     * Maximizes the application window.
     */
    public LandingPage maximizeWindow() {
        javafx.scene.layout.Pane landingPage = lookup(SELECTOR_LANDING_PAGE_BORDER_PANE, javafx.scene.layout.Pane.class);
        Stage primaryStage = (Stage) landingPage.getScene().getWindow();
        
        Platform.runLater(() -> {
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
        });
        
        waitForFxEvents();
        LOG.info("Maximized window");
        return this;
    }
    
    /**
     * Clicks the "Create Project Journal" button.
     */
    public LandingPage clickCreateProjectJournal() {
        try {
        waitFor(500);
        clickOnText("Create project journal");
        waitForFxEvents();
        maximizeWindow();  
        closeDialogs();
        LOG.info("Clicked 'Create Project Journal'");
        } catch  (Exception e) {
            robot.moveTo("My project journals");
            robot.moveBy(0, 50);
            scrollDown();
            waitForFxEvents();
            clickOnText("Create project journal");
            waitForFxEvents();
            maximizeWindow();  
            closeDialogs();
            LOG.info("Clicked 'Create Project Journal' after scrolling");
        }
        return this;
    }

    /**
     * Clicks the NEW PROJECT JOURNAL button.
     */
    public LandingPage clickNewProjectJournal() {
        waitForFxEvents();
        clickOnText("NEW PROJECT JOURNAL");
        waitForFxEvents();
        maximizeWindow();  
        LOG.info("Clicked 'New Project Journal'");
        return this;
    }

    /**
     * Clicks the Home button in the landing page.
     */
    public LandingPage clickHomeButton() {
        clickOnText("Home");
        waitForFxEvents();
        LOG.info("Clicked Home button");
        return this;
    }

    /**
     * Clicks the Favorites button in the landing page.
     */
    public LandingPage clickFavoritesButton() {
        clickOnText("Favorites");
        waitForFxEvents();
        LOG.info("Clicked Favorites button");
        return this;
    }

    /**
     * Clicks the Comments button in the landing page.
     */
    public LandingPage clickCommentsButton() {
        clickOnText("Comments");
        waitForFxEvents();
        LOG.info("Clicked Comments button");
        return this;
    }

    /**
     * Clicks the Notifications button in the landing page.
     */
    public LandingPage clickNotificationsButton() {
        clickOnText("Notifications");
        waitForFxEvents();
        LOG.info("Clicked Notifications button");
        return this;
    }

    /**
     * Deletes a journal by its name.
     * @param journalName The name of the journal to delete
     */
    public LandingPage deleteJournal(String journalName) {
        robot.moveTo(journalName);
        robot.moveBy(200, -150);
        waitForFxEvents();
        robot.clickOn();
        waitForFxEvents();
        clickOnText("Delete");
        LOG.info("Deleted journal: {}", journalName);
        return this;
    }

    /**
     * Closes the current journal window.
     */
    public LandingPage closeJournalWindow() {
        Stage journalWindow = robot.listTargetWindows().stream()
            .filter(window -> window instanceof Stage)
            .map(window -> (Stage) window)
            .filter(stage -> stage.getTitle() != null && stage.getTitle().contains("Journal"))
            .findFirst()
            .orElse(null);
        
        if (journalWindow != null) {
            Stage finalJournalStage = journalWindow;
            Platform.runLater(finalJournalStage::close);
            waitForFxEvents();
            
            // Refocus on main window
            Stage mainStage = robot.listTargetWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(stage -> stage.getTitle() != null && !stage.getTitle().contains("Journal"))
                .findFirst()
                .orElse(null);
            
            if (mainStage != null) {
                Platform.runLater(() -> {
                    mainStage.requestFocus();
                    mainStage.toFront();
                });
                waitForFxEvents();
            }
            
            LOG.info("Closed journal window and refocused on main window");
        }
        
        return this;
    }
}
