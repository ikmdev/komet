package dev.ikm.komet.app.test.integration.testfx.pages;

import javafx.geometry.Bounds;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import org.testfx.api.FxRobot;

/**
 * Page object for Journal operations.
 */
public class JournalPage extends BasePage {
    
    public JournalPage(FxRobot robot) {
        super(robot);
    }
    
    /**
     * Drags KOMET user bar to editing area using the move button.
     */
    public JournalPage dragKometUserToEditingArea() {
        waitFor(1000);
        
        javafx.scene.Node kometUserText = robot.lookup("KOMET user").query();
        Bounds kometUserBounds = kometUserText.localToScreen(kometUserText.getBoundsInLocal());
        
        double moveButtonX = kometUserBounds.getMinX() + 375;
        double moveButtonY = kometUserBounds.getMinY() + (kometUserBounds.getHeight() / 2);
        
        robot.moveTo(moveButtonX, moveButtonY);
        waitFor(300);
        robot.clickOn(MouseButton.PRIMARY);
        waitFor(200);
        robot.drag(MouseButton.PRIMARY).moveBy(50, 0).drop();
        waitFor(500);
        
        LOG.info("Dragged KOMET user to editing area");
        closeDialogs();
        return this;
    }
    
    /**
     * Resizes the concept pane by dragging from the top-right corner.
     */
    public JournalPage resizeConceptPane() {
        waitFor(3000);
        
        // Scroll horizontally first
        robot.moveTo(new Point2D(400, 300));
        waitFor(500);
        robot.scroll(5, HorizontalDirection.RIGHT);
        waitFor(500);
        LOG.info("Scrolled editable area horizontally");
        
        /*
        // Find the concept pane
        Pane conceptPane = robot.lookup(node -> {
            if (node instanceof Pane && !(node instanceof BorderPane)) {
                Bounds bounds = node.localToScreen(node.getBoundsInLocal());
                return bounds != null && bounds.getWidth() > 100 && bounds.getHeight() > 100;
            }
            return false;
        }).queryAll().stream()
            .filter(node -> node instanceof Pane)
            .map(node -> (Pane) node)
            .findFirst()
            .orElse(null);
        
        if (conceptPane != null) {
            Bounds paneBounds = conceptPane.localToScreen(conceptPane.getBoundsInLocal());
            double topRightX = paneBounds.getMaxX() - 5;
            double topRightY = paneBounds.getMinY() + 5;
            
            robot.moveTo(topRightX, topRightY);
            waitFor(1000);
            LOG.info("Moved mouse to top-right corner at ({}, {})", topRightX, topRightY);
            
            robot.press(MouseButton.PRIMARY);
            waitFor(500);
            robot.moveBy(50, 50);
            waitFor(500);
            robot.release(MouseButton.PRIMARY);
            waitFor(1000);
            
            LOG.info("Resized concept pane");
        } else {
            // Fallback to absolute coordinates
            robot.moveTo(new Point2D(115, 200));
            waitFor(1000);
            robot.press(MouseButton.PRIMARY);
            waitFor(500);
            robot.moveBy(50, 50);
            waitFor(500);
            robot.release(MouseButton.PRIMARY);
            waitFor(1000);
            LOG.info("Resized using fallback coordinates");
        }
        */
        // Fallback to absolute coordinates
        
        return this;
    }
}
