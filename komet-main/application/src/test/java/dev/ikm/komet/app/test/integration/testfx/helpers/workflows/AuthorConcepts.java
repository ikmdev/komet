package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class AuthorConcepts extends BaseWorkflow {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorConcepts.class);

    /**
     * Constructs an AuthorConcepts workflow helper.
     * 
     * @param robot    FxRobot instance for UI interactions
     * @param reporter TestReporter instance for logging test steps
     */
    public AuthorConcepts(FxRobot robot, TestReporter reporter) {
        super(robot, reporter);
    }

    /**
     * Creates a new concept with the specified details (Steps 7-24).
     * This method encapsulates the complete workflow for creating a new concept
     * including:
     * - Creating a project journal
     * - Setting up concept with stamp information
     * - Adding fully qualified name
     * - Configuring axioms and parent concept
     *
     * @param fullyQualifiedName The fully qualified name for the concept
     * @param status             The status for the concept (e.g., "Active")
     * @param moduleName         The module name (e.g., "Device Extension Module")
     * @param path               The path name (e.g., "Development path")
     * @param parentConceptName  The parent concept name to search for and link
     * @throws InterruptedException if thread is interrupted during execution
     */
    public void createConcept(String fullyQualifiedName, String status,
            String moduleName, String path, String parentConceptName)
            throws InterruptedException {

        // 7. Open an existing project journal or create a new one
        try {
            reporter.logBeforeStep("Step 7: CLICK 'Create Project Journal'");
            landingPage.clickNewProjectJournal();
            reporter.logAfterStep("Step 7: CLICK 'Create Project Journal' successful");
        } catch (Exception e) {
            reporter.logFailure("Step 7: CLICK 'Create Project Journal'", e);
            throw e;
        }

        // 8. Click the "+" icon in the journal window toolbar
        try {
            reporter.logBeforeStep("Step 8: CLICK '+' Icon in Journal Toolbar");
            navigator.clickCreate();
            reporter.logAfterStep("Step 8: CLICK '+' Icon in Journal Toolbar successful");
        } catch (Exception e) {
            reporter.logFailure("Step 8: CLICK '+' Icon in Journal Toolbar", e);
            throw e;
        }

        // 9. Click New Concept
        try {
            reporter.logBeforeStep("Step 9: CLICK 'New Concept'");
            robot.clickOn("New Concept");
            waitForFxEvents();
            reporter.logAfterStep("Step 9: CLICK 'New Concept' successful");
        } catch (Exception e) {
            reporter.logFailure("Step 9: CLICK 'New Concept'", e);
            throw e;
        }

        // 10. Update stamp information:
        // - Module: Device Extension Module
        // - Path: Development Path
        try {
            reporter.logBeforeStep("Step 10: Update Stamp Information");
            conceptPane.openStampEditor();
            conceptPane.updateStamp(status, moduleName, path);
            reporter.logAfterStep("Step 10: Updated Stamp Information successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 10: Update Stamp Information", e);
            throw e;
        }

        // 11. Click the Properties Panel toggle in the top right of the window
        try {
            reporter.logBeforeStep("Step 11: Click the Properties Panel toggle");
            conceptPane.clickPropertiesToggle();
            reporter.logAfterStep("Step 11: Clicked the Properties Panel toggle");
        } catch (Exception e) {
            reporter.logFailure("Step 11: Click the Properties Panel toggle", e);
            throw e;
        }

        // 12. Add the Fully Qualified Name information
        // - Module: Device Extension Module
        try {
            reporter.logBeforeStep("Step 12: Update Fully Qualified Name");
            conceptPane.updateFullyQualifiedName(fullyQualifiedName);
            conceptPane.updateModule(moduleName);
            reporter.logAfterStep("Step 12: Updated Fully Qualified Name successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 12: Update Fully Qualified Name", e);
            throw e;
        }

        // 13. Click Submit
        try {
            reporter.logBeforeStep("Step 13: USER to CLICK Submit button");
            conceptPane.submit();
            reporter.logAfterStep("Step 13: Clicked Submit button");
        } catch (Exception e) {
            reporter.logFailure("Step 13: USER to CLICK Submit button", e);
            throw e;
        }

        // If "Concept Name" is not visible, scroll to left to find the pencil icon
        if (!robot.lookup("Concept Name").tryQuery().isPresent()) {
            robot.clickOn(250, 250);
            horizontalScroll(KeyCode.LEFT, 20);
            waitForFxEvents();
        }

        // 14. Click the pencil icon that is in line with the axioms section header
        try {
            reporter.logBeforeStep("Step 14: Click Axioms Pencil Icon");
            conceptPane.clickEditAxiomsButton();
            waitForFxEvents();
            Thread.sleep(2000); // Wait for axioms editor to fully initialize
            reporter.logAfterStep("Step 14: Clicked Axioms Pencil Icon");
        } catch (Exception e) {
            reporter.logFailure("Step 14: Click Axioms Pencil Icon", e);
            throw e;
        }

        // 15. Click "Add Necessary Set"
        try {
            reporter.logBeforeStep("Step 15: Click Add Necessary Set");
            robot.clickOn("Add Necessary Set");
            waitForFxEvents();
            reporter.logAfterStep("Step 15: Clicked Add Necessary Set");
        } catch (Exception e) {
            reporter.logFailure("Step 15: Click Add Necessary Set", e);
            throw e;
        }

        // 16. Click the pencil icon aligned to the third axiom row
        try {
            reporter.logBeforeStep("Step 16: Click pencil icon following 'Anonymous concept'");
            // Move to "Anonymous concept" text then move right to find the button
            robot.moveTo("[Anonymous concept] ");
            robot.moveBy(425, 0); // Move 425 pixels to the right to find the button
            robot.clickOn();
            reporter.logAfterStep("Step 16: Clicked pencil icon following 'Anonymous concept'");
        } catch (Exception e) {
            reporter.logFailure("Step 16: Click pencil icon following 'Anonymous concept'", e);
            throw e;
        }

        // 17. Click "Remove axiom"
        try {
            reporter.logBeforeStep("Step 17: Click Remove Axiom");
            robot.clickOn("Remove axiom");
            reporter.logAfterStep("Step 17: Clicked Remove Axiom");
        } catch (Exception e) {
            reporter.logFailure("Step 17: Click Remove Axiom", e);
            throw e;
        }

        // 18. Click the pencil icon aligned to the second axiom row
        try {
            reporter.logBeforeStep("Step 18: Click pencil icon aligned to second axiom row");
            // Find text that contains "Necessary set: " and move to it
            robot.moveTo("Necessary set: " + fullyQualifiedName);
            // If the name is Roche Diagnostics x = 400, else x = 450
            if (fullyQualifiedName.equals("Roche Diagnostics GmbH")) {
                robot.moveBy(405, 0); // Move 400 pixels to the right to find the button
            } else {
                robot.moveBy(450, 0); // Move 450 pixels to the right to find the button
            }
            robot.clickOn();
            reporter.logAfterStep("Step 18: Clicked pencil icon aligned to second axiom row");
        } catch (Exception e) {
            reporter.logFailure("Step 18: Click pencil icon aligned to second axiom row", e);
            throw e;
        }

        // 19. Click "add is-a"
        try {
            reporter.logBeforeStep("Step 19: Click Add Is-A");
            robot.clickOn("Add is-a");
            reporter.logAfterStep("Step 19: Clicked Add Is-A");
        } catch (Exception e) {
            reporter.logFailure("Step 19: Click Add Is-A", e);
            throw e;
        }

        // 20. Click the pencil icon aligned to the third axiom row
        try {
            reporter.logBeforeStep("Step 20: Click pencil icon aligned to third axiom row");
            // Move to "Anonymous concept" text then move right to find the button
            robot.moveTo("Anonymous concept");
            robot.moveBy(445, 0); // Move 445 pixels to the right to find the button
            robot.clickOn();
            reporter.logAfterStep("Step 20: Clicked pencil icon aligned to third axiom row");
        } catch (Exception e) {
            reporter.logFailure("Step 20: Click pencil icon aligned to third axiom row", e);
            throw e;
        }

        // 21. Click search for concept
        try {
            reporter.logBeforeStep("Step 21: Click Search for Concept");
            conceptPane.clickSearchForConcept();
            waitForFxEvents();
            reporter.logAfterStep("Step 21: Clicked Search for Concept and window opened");
        } catch (Exception e) {
            reporter.logFailure("Step 21: Click Search for Concept", e);
            throw e;
        }

        // 22. Search for parent concept using the search panel
        try {
            reporter.logBeforeStep("Step 22: Search for Parent Concept");
            conceptPane.searchForParentConcept(parentConceptName);
            reporter.logAfterStep("Step 22: Searched for Parent Concept successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 22: Search for Parent Concept", e);
            throw e;
        }

        // 23. When the required parent concept is located, double click to submit
        try {
            reporter.logBeforeStep("Step 23: Locate parent concept and double click to select");
            conceptPane.selectParentConcept(parentConceptName);
            reporter.logAfterStep("Step 23: Located parent concept and double clicked to select successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 23: Locate parent concept and double click to select", e);
            throw e;
        }

        // 24. In the identifier row, copy the UUID by utilizing the copy shortcut
        try {
            reporter.logBeforeStep("Step 24: Copy UUID from Identifier Row");
            conceptPane.clickCopyButton();
            reporter.logAfterStep("Step 24: Copied UUID from Identifier Row successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 24: Copy UUID from Identifier Row", e);
            throw e;
        }
    }
}
