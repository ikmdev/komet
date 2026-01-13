package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class AuthorPatterns extends BaseWorkflow {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorConcepts.class);

    /**
     * Constructs an AuthorPatterns workflow helper.
     * 
     * @param robot    FxRobot instance for UI interactions
     * @param reporter TestReporter instance for logging test steps
     */
    public AuthorPatterns(FxRobot robot, TestReporter reporter) {
        super(robot, reporter);
    }

    /**
     * Authors patterns by navigating to the specified pattern and populating details.
     * 
     * @param patternName The name of the pattern to author (e.g., "Author Device Pattern")
     * @param status      The status for the pattern (e.g., "Active")
     * @param moduleName  The module name (e.g., "Device Extension Module")
     * @param path        The path name (e.g., "Development path")
     * @param caseSignificance The case significance (e.g., "Case insensitive")
     * @param language    The language for the pattern (e.g., "English language")
     * @param dataType    The data type for the field (e.g., "Component display field")
     * @param meaning     The meaning to search and assign (e.g., "Author")
     * @param purpose     The purpose to search and assign (e.g., "Author")
     * @throws InterruptedException if thread is interrupted during execution
     */

    public void authorPatterns(String patternName, String status,
            String moduleName, String path, String caseSignificance,
            String language, String dataType, String meaning, String purpose) throws InterruptedException {

        LOG.info("====== Authoring " + patternName + " ======");

        // Open new journal
        try {
            reporter.logBeforeStep("Open a new journal for " + patternName + " pattern entry");
            landingPage.clickNewProjectJournal();
            reporter.logAfterStep("Opened a new journal for " + patternName + " pattern entry successfully");
        } catch (Exception e) {
            reporter.logFailure("Open a new journal for " + patternName + " pattern entry", e);
            throw e;
        }

        //Click the "+" icon in the journal window toolbar
        try {
            reporter.logBeforeStep("CLICK '+' Icon in Journal Toolbar");
            navigator.clickCreate();
            reporter.logAfterStep("CLICK '+' Icon in Journal Toolbar successful");
        } catch (Exception e) {
            reporter.logFailure("CLICK '+' Icon in Journal Toolbar", e);
            throw e;
        }

        // 9. Click New Concept
        try {
            reporter.logBeforeStep("CLICK 'New Pattern'");
            robot.clickOn("New Pattern");
            waitForFxEvents();
            reporter.logAfterStep("CLICK 'New Pattern' successful");
        } catch (Exception e) {
            reporter.logFailure("CLICK 'New Pattern'", e);
            throw e;
        }

        // Update stamp information:
        // - Status: Active
        // - Module: Device Extension Module
        // - Path: Development Path
        try {
            reporter.logBeforeStep("Update Stamp Information");
            conceptPane.openStampEditor();
            conceptPane.updateStamp(status, moduleName, path);
            reporter.logAfterStep("Update Stamp Information successful");
        } catch (Exception e) {
            reporter.logFailure("Update Stamp Information", e);
            throw e;
        }

        //click pencil with "Edit Definition" tooltip
        try {
            reporter.logBeforeStep("CLICK 'Edit Definition' Pencil Icon");
            conceptPane.clickEditDefinitionButton();
            reporter.logAfterStep("CLICK 'Edit Definition' Pencil Icon successful");
        } catch (Exception e) { 
            reporter.logFailure("CLICK 'Edit Definition' Pencil Icon", e);
            throw e;
        }

        //Search purpose in Nextgen SEARCH and drag and drop to purpose field
        try {
            reporter.logBeforeStep("Search for " + purpose);
            navigator.clickNextgenSearch();
            navigator.nextgenSearch(purpose);
            robot.moveTo("SORT BY: TOP COMPONENT");
                waitForFxEvents();
                robot.moveBy(0, 50); // Move down to results area
                waitForFxEvents();
                waitFor(500);
                // drag and drop to Purpose field
                robot.press(MouseButton.PRIMARY)
                        .moveTo("Purpose").moveBy(40, 40)
                        .release(MouseButton.PRIMARY);
                waitForFxEvents();
                reporter.logAfterStep("Searched and selected " + purpose + " successfully");
        } catch (Exception e) {
            reporter.logFailure("Search and select " + purpose, e);
            throw e;
        }

        //Search meaning in Nextgen SEARCH and drag and drop to purpose field
        try {
            reporter.logBeforeStep("Search for " + meaning);
            navigator.nextgenSearch(meaning);
            robot.moveTo("SORT BY: TOP COMPONENT");
                waitForFxEvents();
                robot.moveBy(0, 50); // Move down to results area
                waitForFxEvents();
                waitFor(500);
                // drag and drop to Purpose field
                robot.press(MouseButton.PRIMARY)
                        .moveTo("Meaning").moveBy(40, 40)
                        .release(MouseButton.PRIMARY);
                waitForFxEvents();
                reporter.logAfterStep("Searched and selected " + meaning + " successfully");
        } catch (Exception e) {
            reporter.logFailure("Search and select " + meaning, e);
            throw e;
        }

        //Click "DONE"
        try {
            reporter.logBeforeStep("CLICK 'DONE' Button to complete pattern authoring");
            robot.clickOn("DONE");
            waitForFxEvents();
            navigator.clickNextgenSearch(); // Close Nextgen Search
            reporter.logAfterStep("CLICK 'DONE' Button to complete pattern authoring successful");
        } catch (Exception e) {
            reporter.logFailure("CLICK 'DONE' Button to complete pattern authoring", e);
            throw e;
        }

        //Click pencil with "Edit Description" tooltip
        try {
            reporter.logBeforeStep("CLICK 'Edit Description' Pencil Icon");
            conceptPane.clickEditDescriptionsButton();
            reporter.logAfterStep("CLICK 'Edit Description' Pencil Icon successful");
        } catch (Exception e) {
            reporter.logFailure("CLICK 'Edit Description' Pencil Icon", e);
            throw e;
        }

        //Create FQN
        try {
            reporter.logBeforeStep("Create Fully Qualified Name (FQN)");
            robot.clickOn("Add Fully Qualified Name");
            waitForFxEvents();
            robot.clickOn("Enter name");
            robot.write(patternName);
            waitForFxEvents();
            conceptPane.updateCaseSignificance(caseSignificance);
            conceptPane.updateStatus(status);
            conceptPane.updateModule(moduleName);
            conceptPane.updateLanguage(language);
            robot.clickOn("DONE");
            waitForFxEvents();
            reporter.logAfterStep("Created Fully Qualified Name (FQN) successfully");
        } catch (Exception e) {
            reporter.logFailure("Create Fully Qualified Name (FQN)", e);
            throw e;
        }

        //Create a single field
        try {
            reporter.logBeforeStep("Create a single field");
            conceptPane.clickEditFieldsButton();
            conceptPane.updateDataType(dataType);
            navigator.clickNextgenSearch();
            navigator.nextgenSearch(purpose);
            robot.moveTo("SORT BY: TOP COMPONENT");
                waitForFxEvents();
                robot.moveBy(0, 50); // Move down to results area
                waitForFxEvents();
                waitFor(500);
                // drag and drop to Purpose field
                robot.press(MouseButton.PRIMARY)
                        .moveTo("Purpose").moveBy(40, 40)
                        .release(MouseButton.PRIMARY);
                waitForFxEvents();

            navigator.nextgenSearch(meaning);
            robot.moveTo("SORT BY: TOP COMPONENT");
                waitForFxEvents();
                robot.moveBy(0, 50); // Move down to results area
                waitForFxEvents();
                waitFor(500);
                // drag and drop to Meaning field
                robot.press(MouseButton.PRIMARY)
                        .moveTo("Meaning").moveBy(40, 40)
                        .release(MouseButton.PRIMARY);
                waitForFxEvents();
                navigator.clickNextgenSearch(); // Close Nextgen Search
                robot.clickOn("DONE");
            waitForFxEvents();
            reporter.logAfterStep("Created a single field successfully");
        } catch (Exception e) {
            reporter.logFailure("Create a single field", e);
            throw e;
        }

        //FIX
        //Click Publish button with tooltip of "Submit"
        try {
            reporter.logBeforeStep("CLICK 'Publish' Button to submit pattern");
            conceptPane.clickPublishButton();
            waitForFxEvents();
            reporter.logAfterStep("CLICK 'Publish' Button to submit pattern successful");
        } catch (Exception e) {
            reporter.logFailure("CLICK 'Publish' Button to submit pattern", e);
            throw e;
        }
            }
}
