package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class AddAssociatedDevicesSemantic extends BaseWorkflow {

    /**
     * Constructs an AddAssociatedDevicesSemantic workflow helper.
     * 
     * @param robot    FxRobot instance for UI interactions
     * @param reporter TestReporter instance for logging test steps
     */
    public AddAssociatedDevicesSemantic(FxRobot robot, TestReporter reporter) {
        super(robot, reporter);
    }

    /**
     * Adds the Associated Devices Semantic.
     * Steps: 63 - 71
     * This method encapsulates the complete workflow for adding semantic elements
     * including:
     * - Creating a new journal
     * - Navigating to the Associated Devices pattern
     * - Setting up stamp and reference component
     * - Populating device labeler field with provided identifier
     *
     * @param patternName        The name of the pattern to add semantic element to
     *                           (e.g., "Device Company Pattern", "Associated
     *                           Devices Pattern")
     * @param status             The status for the semantic (e.g., "Active")
     * @param moduleName         The module name (e.g., "Device Extension Module")
     * @param path               The path name (e.g., "Development path")
     * @param referenceComponent The reference component to paste (device
     *                           name/identifier from clipboard)
     * @param deviceLabelers     One or more device labeler identifiers to populate
     *                           in semantic details
     * @throws InterruptedException if thread is interrupted during execution
     */
    public void addAssociatedDevicesSemantic(String patternName, String status,
            String moduleName, String path, String referenceComponent,
            String... deviceLabelers)
            throws InterruptedException {

        LOG.info("====== Adding " + patternName + " ======");

        // Open new journal
        try {
            reporter.logBeforeStep("Open a new journal for " + patternName + " semantic entry");
            landingPage.clickNewProjectJournal();
            reporter.logAfterStep("Opened a new journal for " + patternName + " semantic entry successfully");
        } catch (Exception e) {
            reporter.logFailure("Open a new journal for " + patternName + " semantic entry", e);
            throw e;
        }

        // Open nextgen navigator
        try {
            reporter.logBeforeStep("Open NextGen Navigator");
            navigator.clickNextgenNavigator();
            reporter.logAfterStep("Opened NextGen Navigator successfully");
        } catch (Exception e) {
            reporter.logFailure("Open NextGen Navigator", e);
            throw e;
        }

        // Click Patterns tab
        try {
            reporter.logBeforeStep("Click Patterns tab");
            navigator.clickPatterns();
            reporter.logAfterStep("Clicked Patterns tab successfully");
        } catch (Exception e) {
            reporter.logFailure("Click Patterns tab", e);
            throw e;
        }

        // Move to the specified pattern
        try {
            reporter.logBeforeStep("Move to '" + patternName + "'");
            robot.moveTo(patternName);
            // if pattern is not visible, scroll down 10, repeat till visible
            while (!robot.lookup(patternName).tryQuery().isPresent()) {
                verticalScroll(KeyCode.DOWN, 10);
                waitForFxEvents();
            }

            reporter.logAfterStep("Moved to '" + patternName + "' successfully");
        } catch (Exception e) {
            reporter.logFailure("Move to '" + patternName + "'", e);
            throw e;
        }

        // Right Click the pattern and select "Add New Semantic Element"
        try {
            reporter.logBeforeStep("Right Click the pattern and select 'Add New Semantic Element'");
            robot.rightClickOn(patternName);
            waitForFxEvents();
            robot.clickOn("Add New Semantic Element");
            waitForFxEvents();
            navigator.clickNextgenNavigator();
            reporter.logAfterStep("Right Clicked the pattern and selected 'Add New Semantic Element' successfully");
        } catch (Exception e) {
            reporter.logFailure("Right Click the pattern and select 'Add New Semantic Element'", e);
            throw e;
        }

        // Update the Stamp
        try {
            reporter.logBeforeStep("Update the Stamp to reflect Module: " + moduleName);
            conceptPane.updateStamp(status, moduleName, path);
            reporter.logAfterStep("Updated the Stamp to reflect Module: " + moduleName + " successfully");
        } catch (Exception e) {
            reporter.logFailure("Update the Stamp to reflect Module: " + moduleName, e);
            throw e;
        }

        // Click the pencil icon that is in line with the Reference Component section
        // header
        try {
            reporter.logBeforeStep("Click the pencil icon that is in line with the Reference Component section header");
            conceptPane.clickEditReferenceComponentButton();
            reporter.logAfterStep(
                    "Clicked the pencil icon that is in line with the Reference Component section header successfully");
        } catch (Exception e) {
            reporter.logFailure("Click the pencil icon that is in line with the Reference Component section header", e);
            throw e;
        }

        // Paste UUID from clipboard
        try {
            reporter.logBeforeStep("Paste UUID from clipboard");
            robot.rightClickOn("🔍  Search");
            waitForFxEvents();
            robot.clickOn("Paste");
            waitForFxEvents();
            waitFor(1000); // Wait for results to load
            //if referenceComponent is not visible, scroll down 10, repeat till visible
            while (!robot.lookup(referenceComponent).tryQuery().isPresent()) {
                verticalScroll(KeyCode.DOWN, 10);
                waitForFxEvents();
            }
            robot.clickOn(referenceComponent);
            waitForFxEvents();
            reporter.logAfterStep("Pasted UUID from clipboard successfully");
        } catch (Exception e) {
            reporter.logFailure("Paste UUID from clipboard", e);
            throw e;
        }

        // Click Confirm and verify the correct reference component populates
        try {
            reporter.logBeforeStep("Click Confirm and verify the correct reference component populates");
            robot.clickOn("CONFIRM");
            waitForFxEvents();
            reporter.logAfterStep(
                    "Clicked Confirm and verified the correct reference component populated successfully");
        } catch (Exception e) {
            reporter.logFailure("Click Confirm and verify the correct reference component populates", e);
            throw e;
        }

        // Click the pencil that is in line with the Semantic Details section header
        try {
            reporter.logBeforeStep("Click the pencil that is in line with the Semantic Details section header");
            conceptPane.clickEditSemanticDetailsButton();
            reporter.logAfterStep(
                    "Clicked the pencil that is in line with the Semantic Details section header successfully");
        } catch (Exception e) {
            reporter.logFailure("Click the pencil that is in line with the Semantic Details section header", e);
            throw e;
        }

        // Populate the Device Labeler field(s)
        try {
            reporter.logBeforeStep(
                    "Populate the Associated Devices field by searching for device identifier/ Associated device identifiers");
            // complete the following until the list returns null or empty
            for (String associatedDevice : deviceLabelers) {
                robot.rightClickOn("🔍  Search");
                waitForFxEvents();
                robot.clickOn("Paste");
                waitForFxEvents();
                Thread.sleep(500); // Wait for results to load
                // press down arrow then press enter
                robot.doubleClickOn(associatedDevice);
                waitForFxEvents();
            }
            reporter.logAfterStep("Populated the Associated Devices field successfully");
        } catch (Exception e) {
            reporter.logFailure("Populate the Associated Devices field by searching for device identifier(s)", e);
            throw e;
        }

        // Click Submit
        try {
            reporter.logBeforeStep("Click Submit");
            conceptPane.submit();
            waitForFxEvents();
            reporter.logAfterStep("Clicked Submit successfully");
        } catch (Exception e) {
            reporter.logFailure("Click Submit", e);
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
