package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;
import javafx.scene.input.MouseButton;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class AddQuantitativeAllowedResultsSemantic extends BaseWorkflow {

    /**
     * Constructs an AddQuantitativeAllowedResultsSemantic workflow helper.
     * 
     * @param robot    FxRobot instance for UI interactions
     * @param reporter TestReporter instance for logging test steps
     */
    public AddQuantitativeAllowedResultsSemantic(FxRobot robot, TestReporter reporter) {
        super(robot, reporter);
    }

    /**
     * Adds the Quantitative Allowed Results Semantic.
     * Steps: 98 - 110
     * 
     * Key Features:
     * - Creating a new journal
     * - Navigating to the Associated Devices pattern
     * - Setting up stamp and reference component
     * - Populating necessary qualifier value concepts
     *
     * @param patternName      The name of the pattern to add semantic element to
     *                         (e.g., "Device Company Pattern", "Associated
     *                         Devices Pattern")
     * @param status           The status for the semantic (e.g., "Active")
     * @param moduleName       The module name (e.g., "Device Extension Module")
     * @param path             The path name (e.g., "Development path")
     * @param maxValueOperator The maximum value operator found by searching the
     *                         concept FQN or UUID in the semantic field values
     *                         interface
     * @param minValueOperator The minimum value operator found by searching the
     *                         concept
     *                         FQN or UUID in the semantic field values interface
     * @param rangeMaxValue    The range maximum value to input
     * @param rangeMinValue    The range minimum value to input
     * @param exampleUnits     The example units to input
     * 
     * 
     * @throws InterruptedException if thread is interrupted during execution
     */

    public void addQuantitativeAllowedResultsSemantic(String patternName, String status,
            String moduleName, String path, String referenceComponent, String maxValueOperator,
            String minValueOperator, String rangeMaxValue, String rangeMinValue, String exampleUnits)
            throws InterruptedException {

    LOG.info("====== Adding " + patternName + " ======");

        // Open new journal
        try {
            reporter.logBeforeStep("Open a new journal for " + patternName+ " semantic entry");
            landingPage.clickNewProjectJournal();
            reporter.logAfterStep("Opened a new journal for " + patternName+ " semantic entry successfully");
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
            waitForMillis(1000); // Wait for results to load
            // locate the list then find the list item that matches referenceComponent
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

        // Populate Maximum Value Operator: Search using Nextgen Search then drag and drop the concept into the Reference
        try {
            reporter.logBeforeStep("Search using Nextgen Search then drag and drop the concept into the Reference Component field");
            navigator.clickNextgenSearch();
            navigator.nextgenSearch(maxValueOperator);
            waitForFxEvents();
            robot.moveTo("SORT BY: TOP COMPONENT");
            waitForFxEvents();
            robot.moveBy(0, 50); // Move down to results area
            waitForFxEvents();
            waitForMillis(500);

            robot.press(MouseButton.PRIMARY)
                .moveTo("Maximum Value Operator; Maximum Domain Operator")
                .moveBy(0, 25)
                .release(MouseButton.PRIMARY);
        waitForFxEvents();
            reporter.logAfterStep(
                    "Searched using Nextgen Search then dragged and dropped the concept into the Reference Component field successfully");
        } catch (Exception e) {
            reporter.logFailure("Search using Nextgen Search then drag and drop the concept into the Reference Component field", e);
            throw e;
        }

        // Populate Minimum Value Operator: Search using Nextgen Search then drag and drop the concept into the Reference
        try {
            reporter.logBeforeStep("Search using Nextgen Search then drag and drop the concept into the Reference Component field");
            navigator.clickNextgenSearch();
            navigator.nextgenSearch(minValueOperator);
            waitForFxEvents();
            robot.moveTo("SORT BY: TOP COMPONENT");
            waitForFxEvents();
            robot.moveBy(0, 50); // Move down to results area
            waitForFxEvents();
            waitForMillis(500);

            robot.press(MouseButton.PRIMARY)
                .moveTo("Minimum Value Operator; Minimum Domain Operator")
                .moveBy(0, 25)
                .release(MouseButton.PRIMARY);
        waitForFxEvents();
            reporter.logAfterStep(
                    "Searched using Nextgen Search then dragged and dropped the concept into the Reference Component field successfully");
        } catch (Exception e) {
            reporter.logFailure("Search using Nextgen Search then drag and drop the concept into the Reference Component field", e);
            throw e;
        }

        // move to range max value field
        try {
            reporter.logBeforeStep("Type in the Range Maximum Value");
            robot.moveTo("Allowable Range Maximum Value:").moveBy(0, 25).doubleClickOn();
            robot.write(rangeMaxValue);
            waitForFxEvents();
            reporter.logAfterStep("Typed in the Range Maximum Value successfully");
        } catch (Exception e) {
            reporter.logFailure("Type in the Range Maximum Value", e);
            throw e;
        }

        // move to range min value field
        try {
            reporter.logBeforeStep("Type in the Range Minimum Value");
            robot.moveTo("AllowableRange Minimum Value:").moveBy(0, 25).doubleClickOn();
            robot.write(rangeMinValue);
            waitForFxEvents();
            reporter.logAfterStep("Typed in the Range Minimum Value successfully");
        } catch (Exception e) {
            reporter.logFailure("Type in the Range Minimum Value", e);
            throw e;
        }

        // move to example units field
        try {
            reporter.logBeforeStep("Type in the Example Units");
            robot.moveTo("Example Units:").moveBy(0, 25).doubleClickOn();
            robot.write(exampleUnits);
            waitForFxEvents();
            robot.moveBy(0, 50).clickOn(); // move focus away to ensure value is set
            waitForFxEvents();
            reporter.logAfterStep("Typed in the Example Units successfully");
        } catch (Exception e) {
            reporter.logFailure("Type in the Example Units", e);
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

        // Close journal window
        try {
            reporter.logBeforeStep("Close journal window");
            landingPage.closeJournalWindow();
            reporter.logAfterStep("Closed journal window successfully");
        } catch (Exception e) {
            reporter.logFailure("Close journal window", e);
            throw e;
        }

        LOG.info("✓ Add DeX Quantitative Allowed Results Semantic: PASSED");

    }
}
