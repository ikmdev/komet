package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import dev.ikm.komet.app.App;
import dev.ikm.komet.app.AppState;
import dev.ikm.komet.app.test.integration.testfx.pages.ConceptPane;
import dev.ikm.komet.app.test.integration.testfx.pages.DataSourceSelectionPage;
import dev.ikm.komet.app.test.integration.testfx.pages.LandingPage;
import dev.ikm.komet.app.test.integration.testfx.pages.LoginPage;
import dev.ikm.komet.app.test.integration.testfx.pages.NavigatorPanel;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.nio.file.Files.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.util.WaitForAsyncUtils.waitFor;
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
    protected final LoginPage loginPage;
    protected final DataSourceSelectionPage dataSource;

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
        this.loginPage = new LoginPage(robot);
        this.dataSource = new DataSourceSelectionPage(robot);
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
     * Waits for the specified number of milliseconds and then waits for FX events.
     * 
     * @param milliseconds The number of milliseconds to wait
     */
    protected void waitForMillis(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
            waitForFxEvents();
        } catch (InterruptedException e) {
            LOG.error("Interrupted during wait", e);
            Thread.currentThread().interrupt();
        }
    }

            protected void assertInitialAppState() throws TimeoutException {
                waitFor(10, TimeUnit.SECONDS, () -> App.state.get() == AppState.SELECT_DATA_SOURCE);
                assertEquals(AppState.SELECT_DATA_SOURCE, App.state.get(),
                                "Application should be in SELECT_DATA_SOURCE state");
        }

        protected void assertSelectUserState() throws TimeoutException {
                waitFor(60, TimeUnit.SECONDS, () -> {
                        AppState current = App.state.get();
                        System.out.println("Waiting for data load, current state: " + current);
                        return current == AppState.SELECT_USER || current == AppState.RUNNING;
                });
                AppState currentState = App.state.get();
                assertTrue(currentState == AppState.SELECT_USER || currentState == AppState.RUNNING,
                                "Application should be in SELECT_USER or RUNNING state after data load, but was: "
                                                + currentState);
        }

        protected void assertRunningAppState() throws TimeoutException {
                System.out.println("Current app state before wait: " + App.state.get());
                waitFor(60, TimeUnit.SECONDS, () -> {
                        AppState current = App.state.get();
                        System.out.println("Waiting for RUNNING, current state: " + current);
                        return current == AppState.RUNNING;
                });
                assertEquals(AppState.RUNNING, App.state.get(),
                                "Application should be in RUNNING state");
        }

}
