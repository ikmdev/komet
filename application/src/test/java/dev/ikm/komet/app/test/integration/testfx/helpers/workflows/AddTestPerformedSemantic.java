package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;
import javafx.scene.input.MouseButton;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class AddTestPerformedSemantic extends BaseWorkflow {

    /**
     * Constructs an AddTestPerformedSemantic workflow helper.
     * 
     * @param robot    FxRobot instance for UI interactions
     * @param reporter TestReporter instance for logging test steps
     */
    public AddTestPerformedSemantic(FxRobot robot, TestReporter reporter) {
        super(robot, reporter);
    }

    /**
     * Adds the Test Performed Semantic.
     * Steps: 72 - 86
     * 
     * Key Features:
     * - Creating a new journal
     * - Navigating to the Associated Devices pattern
     * - Setting up stamp and reference component
     * - Populating analytes, targets, test performed, instruments, specimens,
     * detection limit, and example UCUM units
     *
     * @param patternName      The name of the pattern to add semantic element to
     *                         (e.g., "Device Company Pattern", "Associated
     *                         Devices Pattern")
     * @param status           The status for the semantic (e.g., "Active")
     * @param moduleName       The module name (e.g., "Device Extension Module")
     * @param path             The path name (e.g., "Development path")
     * @param analytes         One or more analyte identifiers to populate in
     *                         semantic details
     * @param targets          One or more target identifiers to populate in
     *                         semantic details
     * @param testPerformed    Test perfomed found by the LOINC code
     * @param instruments      One or more instrument identifiers found by
     *                         searching for Device Identifier
     * @param specimens        One or more specimen identifiers found by searching
     *                         the FQN of SNOMED ID
     * @param detectionLimit   The detection limit value to input
     * @param exampleUcumUnits The example UCUM units to input
     * 
     * @throws InterruptedException if thread is interrupted during execution
     */
    public void addTestPerformedSemantic(String patternName, String status,
            String moduleName, String path, String referenceComponent,
            String[] analytes, String[] targets, String[] testPerformed,
            String[] instruments, String[] specimens, String detectionLimit, String exampleUcumUnits)
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
            for (int i = 0; i < 6; i++) {
                navigator.scrollPatternResults(patternName);
                waitForFxEvents();
                }
            robot.moveTo(patternName);
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

        // Add the Analyte(s) by searching the analyte concept in the NextGen Search
        try {
            reporter.logBeforeStep("Search and select Analyte");
            navigator.clickNextgenSearch();
            waitForFxEvents();
            navigator.nextgenSearch(analytes[0]);
            waitForFxEvents();
            robot.moveTo("SORT BY: TOP COMPONENT");
                waitForFxEvents();
                robot.moveBy(0, 50); // Move down to results area
                waitForFxEvents();
                waitForMillis(500);
                // drag and drop to Device Labeler field
                robot.press(MouseButton.PRIMARY)
                        .moveTo("Analyte:").moveBy(40, 40)
                        .release(MouseButton.PRIMARY);
                waitForFxEvents();
                reporter.logAfterStep("Searched and selected Analyte successfully");
        } catch (Exception e) {
            reporter.logFailure("Search and select Analyte", e);
            throw e;
        }

        // Add the Target(s) by searching the analyte concept FQN or UUID in the NextGen
        // Search
        try {
            reporter.logBeforeStep("Search and select Target");
            navigator.nextgenSearch(targets[0]);
            waitForFxEvents();
            robot.moveTo("SORT BY: TOP COMPONENT");
                waitForFxEvents();
                robot.moveBy(0, 50); // Move down to results area
                waitForFxEvents();
                waitForMillis(500);
                // drag and drop to Device Labeler field
                robot.press(MouseButton.PRIMARY)
                        .moveTo("Target:").moveBy(40, 40)
                        .release(MouseButton.PRIMARY);
                waitForFxEvents();
                reporter.logAfterStep("Searched and selected Target successfully");
        } catch (Exception e) {
            reporter.logFailure("Search and select Target", e);
            throw e;
        }

        // move to test performed field
        try {
            reporter.logBeforeStep("Search and select Test Performed");
            navigator.nextgenSearch(testPerformed[0]);
            waitForFxEvents();
            robot.moveTo("SORT BY: TOP COMPONENT");
                waitForFxEvents();
                robot.moveBy(0, 50); // Move down to results area
                waitForFxEvents();
                waitForMillis(500);
                // drag and drop to Device Labeler field
                robot.press(MouseButton.PRIMARY)
                        .moveTo("Test Performed:").moveBy(40, 40)
                        .release(MouseButton.PRIMARY);
                waitForFxEvents();
                robot.moveTo("Test Performed:");
                robot.moveBy(430, 0);
                robot.drag()
                    .moveBy(0, 50)
                    .drop();
                waitForFxEvents();
                reporter.logAfterStep("Searched and selected Test Performed successfully");
        } catch (Exception e) {
            reporter.logFailure("Search and select Test Performed", e);
            throw e;
        }

        // move to insrtrument field
        try {
            reporter.logBeforeStep("Search and select Instrument");
            navigator.nextgenSearch(instruments[0]);
            waitForFxEvents();
            robot.moveTo("SORT BY: TOP COMPONENT");
                waitForFxEvents();
                robot.moveBy(0, 50); // Move down to results area
                waitForFxEvents();
                waitForMillis(500);
                // drag and drop to Device Labeler field
                robot.press(MouseButton.PRIMARY)
                        .moveTo("Instrument:").moveBy(40, 40)
                        .release(MouseButton.PRIMARY);
                waitForFxEvents();
                reporter.logAfterStep("Searched and selected Instrument successfully");
        } catch (Exception e) {
            reporter.logFailure("Search and select Instrument", e);
            throw e;
        }

        //Specimen 1
        try {
            reporter.logBeforeStep("Search and select Specimen");
            navigator.nextgenSearch(specimens[0]);
            waitForFxEvents();
            robot.moveTo("SORT BY: TOP COMPONENT");
                waitForFxEvents();
                robot.moveBy(0, 50); // Move down to results area
                waitForFxEvents();
                waitForMillis(500);
                // drag and drop to Device Labeler field
                robot.press(MouseButton.PRIMARY)
                        .moveTo("Specimen:").moveBy(40, 40)
                        .release(MouseButton.PRIMARY);
                waitForFxEvents();
                reporter.logAfterStep("Searched and selected Specimen successfully");
        } catch (Exception e) {
            reporter.logFailure("Search and select Specimen", e);
            throw e;
        }

        //Specimen 2
        try {
            reporter.logBeforeStep("Search and select Specimen");
            navigator.nextgenSearch(specimens[1]);
            waitForFxEvents();
            robot.moveTo("SORT BY: TOP COMPONENT");
                waitForFxEvents();
                robot.moveBy(0, 50); // Move down to results area
                waitForFxEvents();
                waitForMillis(500);
                // drag and drop to Device Labeler field
                robot.press(MouseButton.PRIMARY)
                        .moveTo("Specimen:").moveBy(40, 95)
                        .release(MouseButton.PRIMARY);
                waitForFxEvents();
                // close NextGen Search
                navigator.clickNextgenSearch();
                waitForFxEvents();
                robot.moveTo("Specimen:");
                robot.moveBy(450, 0);
                robot.drag()
                    .moveBy(0, 100)
                    .drop();
                waitForFxEvents();
                reporter.logAfterStep("Searched and selected Specimen successfully");
        } catch (Exception e) {
            reporter.logFailure("Search and select Specimen", e);
            throw e;
        }


        // move to detection limit field
        try {
            reporter.logBeforeStep("Type in the Detection Limit");
            robot.moveTo("Detection Limit:").moveBy(0, 25).doubleClickOn();
            robot.write(detectionLimit);
            waitForFxEvents();
            reporter.logAfterStep("Typed in the Detection Limit successfully");
        } catch (Exception e) {
            reporter.logFailure("Type in the Detection Limit", e);
            throw e;
        }

        // move to example ucum units field
        try {
            reporter.logBeforeStep("Type in the Example UCUM units");
            robot.moveTo("Example UCUM Units:").moveBy(0, 25).doubleClickOn();
            robot.write(exampleUcumUnits);
            waitForFxEvents();
            robot.moveBy(25, 50).clickOn(); // move focus away to ensure input is registered
            waitForFxEvents(); 
            reporter.logAfterStep("Typed in the Example UCUM units successfully");
        } catch (Exception e) {
            reporter.logFailure("Type in the Example UCUM units", e);
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

        //click copy button
        try{
            reporter.logBeforeStep("Click Copy button to copy the semantic UUID to clipboard");
            conceptPane.clickCopyButton();
            reporter.logAfterStep("Clicked Copy button to copy the semantic UUID to clipboard successfully");
        } catch (Exception e) {
            reporter.logFailure("Click Copy button to copy the semantic UUID to clipboard", e);
            throw e;
        }

        // close journal window
        try {
            reporter.logBeforeStep("Close journal window");
            landingPage.closeJournalWindow();
            reporter.logAfterStep("Closed journal window successfully");
        } catch (Exception e) {
            reporter.logFailure("Close journal window", e);
            throw e;
        }

        LOG.info("✓ Add DeX Test Performed Semantic: PASSED");
    }
}
