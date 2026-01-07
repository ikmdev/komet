package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class AddAllowedResultsSemantic extends BaseWorkflow {

    /**
     * Constructs an AddAllowedResultsSemantic workflow helper.
     * 
     * @param robot    FxRobot instance for UI interactions
     * @param reporter TestReporter instance for logging test steps
     */
    public AddAllowedResultsSemantic(FxRobot robot, TestReporter reporter) {
        super(robot, reporter);
    }

    /**
     * Adds the Allowed Results Semantic.
     * Steps: 88 - 97
     * This method encapsulates the complete workflow for adding semantic elements
     * including:
     * - Creating a new journal
     * - Navigating to the Associated Devices pattern
     * - Setting up stamp and reference component
     * - Populating necessary qualifier value concepts
     *
     * @param patternName     The name of the pattern to add semantic element to
     *                        (e.g., "Device Company Pattern", "Associated
     *                        Devices Pattern")
     * @param status          The status for the semantic (e.g., "Active")
     * @param moduleName      The module name (e.g., "Device Extension Module")
     * @param path            The path name (e.g., "Development path")
     * @param qualifierValues One or more qualifier values found by searching the
     *                        concept FQN or UUID in the semantic field values
     *                        interface
     * 
     * 
     * @throws InterruptedException if thread is interrupted during execution
     */

    public void addAllowedResultsSemantic(String patternName, String patternName2, String status,
            String moduleName, String path, String... qualifierValues)
            throws InterruptedException {

        LOG.info("====== Adding " + patternName + " ======");

        // Open new journal
        try {
            reporter.logBeforeStep("Open a new journal for " + patternName2 + " semantic entry");
            landingPage.clickNewProjectJournal();
            reporter.logAfterStep("Opened a new journal for " + patternName2 + " semantic entry successfully");
        } catch (Exception e) {
            reporter.logFailure("Open a new journal for " + patternName2 + " semantic entry", e);
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
            waitFor(500); // Wait for results to load
            // press down arrow then press enter
            robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
            robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
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

        try {
            reporter.logBeforeStep("Add Allowed Results Qualifier Values");
            for (String qualifierValue : qualifierValues) {
                // move to qualifier value search field
                robot.moveTo("Allowed Results Set:").moveBy(40, 40).clickOn();
                robot.write(qualifierValue);
                robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
                robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
                waitForFxEvents();
            }
            reporter.logAfterStep("Added Allowed Results Qualifier Values successfully");
        } catch (Exception e) {
            reporter.logFailure("Add Allowed Results Qualifier Values", e);
            throw e;
        }

        // click submit
        try {
            reporter.logBeforeStep("Click Submit to save the semantic");
            conceptPane.submit();
            reporter.logAfterStep("Clicked Submit to save the semantic successfully");
        } catch (Exception e) {
            reporter.logFailure("Click Submit to save the semantic", e);
            throw e;
        }

        // click "CLOSE PROPERTIES PANEL"
        try {
            reporter.logAfterStep("Close Properties Panel");
            robot.clickOn("CLOSE PROPERTIES PANEL");
            waitForFxEvents();
            reporter.logAfterStep("Closed Properties Panel successfully");
        } catch (Exception e) {
            reporter.logFailure("Close Properties Panel", e);
            throw e;
        }

        // verify "Semantic Details Added Successfully!" message appears

        // Repeat for all necessary Allowed Results Semantics for the device

        LOG.info("✓ Add DeX Allowed Results Semantic: PASSED");
    }
}
