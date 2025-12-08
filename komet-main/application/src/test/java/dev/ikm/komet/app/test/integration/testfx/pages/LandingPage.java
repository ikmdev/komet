package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.stage.Stage;
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
        
        waitFor(500);
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
        waitFor(1000);
        closeDialogs();
        LOG.info("Clicked 'Create Project Journal'");
        } catch  (Exception e) {
            robot.moveTo("My project journals");
            robot.moveBy(0, 50);
            scrollDown();
            waitFor(300);
            clickOnText("Create project journal");
            waitFor(1000);
            closeDialogs();
            LOG.info("Clicked 'Create Project Journal' after scrolling");
        }
        return this;
    }

    /*
    * Click Home button in landing page
    */
    public LandingPage clickHomeButton() {
        clickOnText("Home");
        waitFor(500);
        LOG.info("Clicked Home button");
        return this;
    }

    /*
    * Click Favorites button in landing page
    */
    public LandingPage clickFavoritesButton() {
        clickOnText("Favorites");
        waitFor(500);
        LOG.info("Clicked Favorites button");
        return this;
    }

    /*
    * Click Comments button in landing page
    */
    public LandingPage clickCommentsButton() {
        clickOnText("Comments");
        waitFor(500);
        LOG.info("Clicked Comments button");
        return this;
    }

    /*
    * Click Notifications button in landing page
    */
    public LandingPage clickNotificationsButton() {
        clickOnText("Notifications");
        waitFor(500);
        LOG.info("Clicked Notifications button");
        return this;
    }
    
    /*
    * Deletes a journal by its name.
    */
    public LandingPage deleteJournal(String journalName) {
        robot.moveTo(journalName);
        robot.moveBy(200, -150);
        waitFor(500);
        robot.clickOn();
        waitFor(500);
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
            waitFor(500);
            
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
                waitFor(500);
            }
            
            LOG.info("Closed journal window and refocused on main window");
        }
        
        return this;
    }
}
