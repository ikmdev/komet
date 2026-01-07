package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;

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
            robot.clickOn("OK");
            reporter.logAfterStep("Step 49.d: Clicked 'OK' on confirmation popup successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 49.d: Click 'OK' on confirmation popup", e);
            throw e;
        }

        // Verify reasoner completed - look for "Reasoner completed successfully" popup
        try {
            reporter.logBeforeStep("Verify reasoner completed successfully");
            boolean popupFound = false;
            for (int i = 0; i < 30; i++) {
                waitForFxEvents();
                try {
                    // Look for "Reasoner completed successfully" text in the scene
                    if (robot.lookup("Reasoner completed successfully").tryQuery().isPresent()) {
                        popupFound = true;
                        LOG.info("Reasoner completed successfully popup appeared after {} attempts", i + 1);
                        break;
                    }
                } catch (Exception e) {
                    // Popup not found yet, continue waiting
                }
                Thread.sleep(1000);
            }

            if (!popupFound) {
                throw new RuntimeException("Reasoner completed successfully popup did not appear within 30 seconds");
            }

            waitForFxEvents();
            reporter.logAfterStep("Reasoner completed successfully verified");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while waiting for reasoner completion", e);
            reporter.logFailure("Verify reasoner completed successfully", e);
            throw new RuntimeException("Interrupted while waiting for reasoner completion", e);
        } catch (Exception e) {
            reporter.logFailure("Verify reasoner completed successfully", e);
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
