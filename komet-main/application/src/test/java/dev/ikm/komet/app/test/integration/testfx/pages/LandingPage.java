package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.application.Platform;
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
    public JournalPage createProjectJournal() {
        waitFor(2000);
        clickOnText("Create project journal");
        waitFor(1000);
        closeDialogs();
        LOG.info("Clicked 'Create Project Journal' button");
        return new JournalPage(robot);
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
