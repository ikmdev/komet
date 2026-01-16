package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;
import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class Reasoner extends BaseWorkflow {

    private static final Logger LOG = LoggerFactory.getLogger(Reasoner.class);

    /**
     * Constructs a Reasoner workflow helper.
     * 
     * @param robot    FxRobot instance for UI interactions
     * @param reporter TestReporter instance for logging test steps
     */
    public Reasoner(FxRobot robot, TestReporter reporter) {
        super(robot, reporter);
    }

    /**
     * Runs the reasoner to classify concepts
     * Steps: 49/ 126
     * 
     * Key Features:
     * - Opening reasoner panel
     * - Clicking starburst button
     * - Initiating reasoner run
     * - Confirming execution
     * - Verifying completion
     *
     * @throws InterruptedException if thread is interrupted during execution
     */
    public void runReasoner() throws InterruptedException {

        LOG.info("====== Running Reasoner ======");

        // Step 49.a: Click reasoner in navigation pane
        try {
            reporter.logBeforeStep("Step 49.a: Click reasoner in navigation pane");
            navigator.clickReasoner();
            reporter.logAfterStep("Step 49.a: Clicked reasoner in navigation pane successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 49.a: Click reasoner in navigation pane", e);
            throw e;
        }

        // Step 49.b: Click starburst
        try {
            reporter.logBeforeStep("Step 49.b: Click starburst");
            navigator.clickReasonerStarburst();
            reporter.logAfterStep("Step 49.b: Clicked starburst successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 49.b: Click starburst", e);
            throw e;
        }

        // Step 49.c: Click "Run reasoner"
        try {
            reporter.logBeforeStep("Step 49.c: Click 'Run reasoner'");
            robot.clickOn("Run reasoner");
            reporter.logAfterStep("Step 49.c: Clicked 'Run reasoner' successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 49.c: Click 'Run reasoner'", e);
            throw e;
        }

        // Step 49.d: Click "OK" on confirmation popup
        try {
            reporter.logBeforeStep("Step 49.d: Click 'OK' on confirmation popup");
            robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
            waitForFxEvents();
            reporter.logAfterStep("Step 49.d: Clicked 'OK' on confirmation popup successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 49.d: Click 'OK' on confirmation popup", e);
            throw e;
        }

        // Wait until reasoner is completed before proceeding
        try {
            reporter.logBeforeStep("Step 49.e: Waiting for reasoner to complete");
            
            // Click on Activity to view task status
            LOG.info("Opening Activity view to monitor reasoner progress");
            navigator.clickReasoner();
            waitForFxEvents();
            robot.clickOn("Activity");
            waitForFxEvents();
            waitForMillis(1000);
            
            // Wait until "No tasks running" is visible (reasoner completed)
            LOG.info("Waiting for 'No tasks running' to appear (max 2 minutes)...");
            boolean reasonerCompleted = false;
            long startTime = System.currentTimeMillis();
            long timeout = 120000; // 2 minutes
            
            while (!reasonerCompleted && (System.currentTimeMillis() - startTime) < timeout) {
                try {
                    if (robot.lookup("No tasks running").tryQuery().isPresent()) {
                        LOG.info("Reasoner completed - 'No tasks running' is now visible");
                        reasonerCompleted = true;
                    } else {
                        LOG.debug("Reasoner still running, waiting...");
                        waitForMillis(2000); // Check every 2 seconds
                    }
                } catch (Exception e) {
                    LOG.debug("Still checking for reasoner completion...");
                    waitForMillis(2000);
                }
            }
            
            if (!reasonerCompleted) {
                LOG.error("Reasoner did not complete within 2 minutes");
                throw new RuntimeException("Reasoner timeout - did not complete within 2 minutes");
            }
            
            waitForFxEvents();
            reporter.logAfterStep("Reasoner completed successfully - 'No tasks running' detected");
            
        } catch (Exception e) {
            reporter.logFailure("Waiting for reasoner to complete", e);
            throw e;
        }

        // Close journal window
        try {
            reporter.logBeforeStep("Close journal window");
            landingPage.closeJournalWindow();
            reporter.logAfterStep("Closed journal window successfully");
        } catch (Exception e) {
            reporter.logFailure("Close journal window", e);
            throw e;
        }
    }
}
