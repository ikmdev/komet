package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import dev.ikm.komet.app.test.integration.testfx.pages.ConceptPane;
import dev.ikm.komet.app.test.integration.testfx.pages.LandingPage;
import dev.ikm.komet.app.test.integration.testfx.pages.NavigatorPanel;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Base class for workflow helpers.
 * Provides common dependencies and utility methods for all workflow classes.
 */
public abstract class BaseWorkflow {

    protected static final Logger LOG = LoggerFactory.getLogger(BaseWorkflow.class);

    protected final FxRobot robot;
    protected final TestReporter reporter;
    protected final ConceptPane conceptPane;
    protected final LandingPage landingPage;
    protected final NavigatorPanel navigator;

    /**
     * Constructs a BaseWorkflow with required dependencies.
     * 
     * @param robot    FxRobot instance for UI interactions
     * @param reporter TestReporter instance for logging test steps
     */
    protected BaseWorkflow(FxRobot robot, TestReporter reporter) {
        this.robot = robot;
        this.reporter = reporter;
        this.conceptPane = new ConceptPane(robot);
        this.landingPage = new LandingPage(robot);
        this.navigator = new NavigatorPanel(robot);
    }

    /**
     * Scrolls vertically in the specified direction.
     * 
     * @param direction    The KeyCode direction to scroll (UP or DOWN)
     * @param scrollAmount The number of times to press the key
     */
    protected void verticalScroll(KeyCode direction, int scrollAmount) {
        for (int i = 0; i < scrollAmount; i++) {
            robot.press(direction);
            robot.release(direction);
        }
    }

    /**
     * Scrolls horizontally in the specified direction.
     * 
     * @param direction    The KeyCode direction to scroll (LEFT or RIGHT)
     * @param scrollAmount The number of times to press the key
     */
    protected void horizontalScroll(KeyCode direction, int scrollAmount) {
        for (int i = 0; i < scrollAmount; i++) {
            robot.press(direction);
            robot.release(direction);
        }
    }

    /**
     * Waits for the specified amount of time.
     * 
     * @param milliseconds The number of milliseconds to wait
     * @throws InterruptedException if the thread is interrupted
     */
    protected void waitFor(int milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }
}
