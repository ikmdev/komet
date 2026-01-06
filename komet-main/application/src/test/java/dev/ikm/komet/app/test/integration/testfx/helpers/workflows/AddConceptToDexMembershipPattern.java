package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import javafx.scene.input.KeyCode;
import org.testfx.api.FxRobot;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class AddConceptToDexMembershipPattern extends BaseWorkflow {

    /**
     * Constructs an AddConceptToDexMembershipPattern workflow helper.
     * 
     * @param robot    FxRobot instance for UI interactions
     * @param reporter TestReporter instance for logging test steps
     */
    public AddConceptToDexMembershipPattern(FxRobot robot, TestReporter reporter) {
        super(robot, reporter);
    }

    /**
     * Adds the specified concept to the Device Extension Membership Pattern (Steps
     * 25-33).
     * This method encapsulates the complete workflow for adding a concept to the
     * DeX Membership Pattern including:
     * - Navigating to the Patterns view
     * - Locating the Device Extension Membership Pattern
     * - Adding a new semantic element
     * - Setting up stamp information
     * - Configuring reference component
     *
     * @param fullyQualifiedName The fully qualified name to search for in step 31
     * @param pattern            The pattern name (e.g., "Device Extension
     *                           Membership Pattern")
     * @param status             The status for the concept (e.g., "Active")
     * @param moduleName         The module name (e.g., "Device Extension Module")
     * @param path               The path name (e.g., "Development path")
     * @throws InterruptedException if thread is interrupted during execution
     */
    
    public void addConceptToDexMembershipPattern(String fullyQualifiedName, String pattern,
            String status, String moduleName, String path)
            throws InterruptedException {

        // 25. Open the Navigator by selecting the hierarchy icon in the left panel
        try {
            reporter.logBeforeStep("Step 25: Open Navigator");
            navigator.clickNextgenNavigator();
            reporter.logAfterStep("Step 25: Opened Navigator successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 25: Open Navigator", e);
            throw e;
        }

        // 26. Switch the view to patterns by clicking on the Patterns text at the top
        try {
            reporter.logBeforeStep("Step 26: Switch to Patterns View");
            navigator.clickPatterns();
            reporter.logAfterStep("Step 26: Switched to Patterns View successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 26: Switch to Patterns View", e);
            throw e;
        }

        // 27. Locate the Device Extension Membership Pattern pattern
        try {
            reporter.logBeforeStep("Step 27: Locate " + pattern);
            robot.moveTo(pattern);
            reporter.logAfterStep("Step 27: Located " + pattern + " successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 27: Locate " + pattern, e);
            throw e;
        }

        // 28. Right Click the pattern and select "Add New Semantic Element"
        try {
            reporter.logBeforeStep("Step 28: Right Click and Add New Semantic Element");
            robot.rightClickOn();
            robot.clickOn("Add New Semantic Element");
            // Close navigator panel
            navigator.clickNextgenNavigator();
            reporter.logAfterStep("Step 28: Right Clicked and Add New Semantic Element successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 28: Right Click and Add New Semantic Element", e);
            throw e;
        }

        // 29. Update the Stamp to reflect
        // - Module: Device Extension Module
        // - Path: Development Path
        try {
            reporter.logBeforeStep("Step 29: Update Stamp Information");
            conceptPane.updateStamp(status, moduleName, path);
            reporter.logAfterStep("Step 29: Updated Stamp Information successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 29: Update Stamp Information", e);
            throw e;
        }

        // 30. Click the pencil icon that is in line with the Reference Component
        // section header
        try {
            reporter.logBeforeStep("Step 30: Click Reference Component Pencil Icon");
            conceptPane.clickEditReferenceComponentButton();
            reporter.logAfterStep("Step 30: Clicked Reference Component Pencil Icon");
        } catch (Exception e) {
            reporter.logFailure("Step 30: Click Reference Component Pencil Icon", e);
            throw e;
        }

        // 31. Click the Next Gen Search (magnifying glass) and search for a concept
        // using UUID or FQN. (If you have the UUID from the concept you created, you
        // can search by UUID in the Reference Component Search)
        try {
            reporter.logBeforeStep("Step 31: Click NextGen Search and search for a concept using UUID or FQN");
            navigator.clickNextgenSearch();
            navigator.nextgenSearch(fullyQualifiedName); // Search by FQN or uuid, whatever is provided
            waitForFxEvents();
            reporter.logAfterStep(
                    "Step 31: Clicked NextGen Search and searched for a concept using UUID or FQN successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 31: Click NextGen Search and search for a concept using UUID or FQN", e);
            throw e;
        }

        // 32. Locate the correct concept and drag and drop it into the Referenced
        // Component field
        try {
            reporter.logBeforeStep(
                    "Step 32: Locate the correct concept and drag and drop it into the Referenced Component field");
            conceptPane.dragToReferenceComponentField();
            reporter.logAfterStep(
                    "Step 32: Located the correct concept and drag and dropped it into the Referenced Component field successfully");
        } catch (Exception e) {
            reporter.logFailure(
                    "Step 32: Locate the correct concept and drag and drop it into the Referenced Component field", e);
            throw e;
        }

        // 33. Click Confirm and verify the correct reference component populates
        try {
            reporter.logBeforeStep("Step 33: Click Confirm and verify the correct reference component populates");
            navigator.clickNextgenSearch(); // Close search panel
            robot.clickOn("CONFIRM");
            waitForFxEvents();
            conceptPane.getPopulatedReferenceComponent();
            reporter.logAfterStep(
                    "Step 33: Clicked Confirm and verified the correct reference component populates successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 33: Click Confirm and verify the correct reference component populates", e);
            throw e;
        }

        // Navigate back to landing page for next concept creation
        try {
            reporter.logBeforeStep("Navigate back to Landing Page");
            // Close journal window and refocus onto landing page
            landingPage.closeJournalWindow();
            reporter.logAfterStep("Returned to Landing Page successfully");
        } catch (Exception e) {
            reporter.logFailure("Navigate back to Landing Page", e);
            throw e;
        }
    }
}