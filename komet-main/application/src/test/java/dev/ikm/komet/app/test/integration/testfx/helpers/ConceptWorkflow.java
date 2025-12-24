package dev.ikm.komet.app.test.integration.testfx.helpers;

import dev.ikm.komet.app.test.integration.testfx.pages.ConceptPane;
import dev.ikm.komet.app.test.integration.testfx.pages.LandingPage;
import dev.ikm.komet.app.test.integration.testfx.pages.NavigatorPanel;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Helper class for reusable concept authoring workflows.
 * This class encapsulates common patterns for creating concepts, adding them to patterns,
 * and managing semantic elements in the DeX authoring process.
 */
public class ConceptWorkflow {

    private static final Logger LOG = LoggerFactory.getLogger(ConceptWorkflow.class);
    
    private final FxRobot robot;
    private final TestReporter reporter;
    private final ConceptPane conceptPane;
    private final LandingPage landingPage;
    private final NavigatorPanel navigator;

    /**
     * Constructs a ConceptWorkflow helper with the required dependencies.
     * 
     * @param robot FxRobot instance for UI interactions
     * @param reporter TestReporter instance for logging test steps
     */
    public ConceptWorkflow(FxRobot robot, TestReporter reporter) {
        this.robot = robot;
        this.reporter = reporter;
        this.conceptPane = new ConceptPane(robot);
        this.landingPage = new LandingPage(robot);
        this.navigator = new NavigatorPanel(robot);
    }

    /**
     * Creates a new concept with the specified details (Steps 7-24).
     * This method encapsulates the complete workflow for creating a new concept including:
     * - Creating a project journal
     * - Setting up concept with stamp information
     * - Adding fully qualified name
     * - Configuring axioms and parent concept
     *
     * @param fullyQualifiedName The fully qualified name for the concept
     * @param status The status for the concept (e.g., "Active")
     * @param moduleName The module name (e.g., "Device Extension Module")
     * @param path The path name (e.g., "Development path")
     * @param parentConceptName The parent concept name to search for and link
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

    /**
     * Adds the specified concept to the Device Extension Membership Pattern (Steps 25-33).
     * This method encapsulates the complete workflow for adding a concept to the DeX Membership Pattern including:
     * - Navigating to the Patterns view
     * - Locating the Device Extension Membership Pattern
     * - Adding a new semantic element
     * - Setting up stamp information
     * - Configuring reference component
     *
     * @param fullyQualifiedName The fully qualified name to search for in step 31
     * @param pattern The pattern name (e.g., "Device Extension Membership Pattern")
     * @param status The status for the concept (e.g., "Active")
     * @param moduleName The module name (e.g., "Device Extension Module")
     * @param path The path name (e.g., "Development path")
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

        // 30. Click the pencil icon that is in line with the Reference Component section header
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
            reporter.logAfterStep("Step 31: Clicked NextGen Search and searched for a concept using UUID or FQN successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 31: Click NextGen Search and search for a concept using UUID or FQN", e);
            throw e;
        }

        // 32. Locate the correct concept and drag and drop it into the Referenced Component field
        try {
            reporter.logBeforeStep("Step 32: Locate the correct concept and drag and drop it into the Referenced Component field");
            conceptPane.dragToReferenceComponentField();
            reporter.logAfterStep("Step 32: Located the correct concept and drag and dropped it into the Referenced Component field successfully");
        } catch (Exception e) {
            reporter.logFailure("Step 32: Locate the correct concept and drag and drop it into the Referenced Component field", e);
            throw e;
        }

        // 33. Click Confirm and verify the correct reference component populates
        try {
            reporter.logBeforeStep("Step 33: Click Confirm and verify the correct reference component populates");
            navigator.clickNextgenSearch(); // Close search panel
            robot.clickOn("CONFIRM");
            waitForFxEvents();
            conceptPane.getPopulatedReferenceComponent();
            reporter.logAfterStep("Step 33: Clicked Confirm and verified the correct reference component populates successfully");
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

    /**
     * Adds a semantic element to a specified pattern.
     * This method encapsulates the complete workflow for adding semantic elements including:
     * - Creating a new journal
     * - Navigating to the specified pattern
     * - Adding a new semantic element
     * - Setting up stamp and reference component
     * - Populating device labeler field(s) with provided identifiers
     *
     * @param patternName The name of the pattern to add semantic element to (e.g., "Device Company Pattern", "Associated Devices Pattern")
     * @param status The status for the semantic (e.g., "Active")
     * @param moduleName The module name (e.g., "Device Extension Module")
     * @param path The path name (e.g., "Development path")
     * @param referenceComponent The reference component to paste (device name/identifier from clipboard)
     * @param deviceLabelers One or more device labeler identifiers to populate in semantic details
     * @throws InterruptedException if thread is interrupted during execution
     */
    public void addSemanticToPattern(String patternName, String status, 
                                     String moduleName, String path, String referenceComponent,
                                     String... deviceLabelers) 
            throws InterruptedException {

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
            //if pattern is not visible, scroll down 10, repeat till visible
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

        // Click the pencil icon that is in line with the Reference Component section header
        try {
            reporter.logBeforeStep("Click the pencil icon that is in line with the Reference Component section header");
            conceptPane.clickEditReferenceComponentButton();
            reporter.logAfterStep("Clicked the pencil icon that is in line with the Reference Component section header successfully");
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
            waitFor(1500); // Wait for results to load
            //press down arrow then press enter
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
            reporter.logAfterStep("Clicked Confirm and verified the correct reference component populated successfully");
        } catch (Exception e) {
            reporter.logFailure("Click Confirm and verify the correct reference component populates", e);
            throw e;
        }

        // Click the pencil that is in line with the Semantic Details section header
        try {
            reporter.logBeforeStep("Click the pencil that is in line with the Semantic Details section header");
            conceptPane.clickEditSemanticDetailsButton();
            reporter.logAfterStep("Clicked the pencil that is in line with the Semantic Details section header successfully");
        } catch (Exception e) {
            reporter.logFailure("Click the pencil that is in line with the Semantic Details section header", e);
            throw e;
        }


        // Populate the Device Labeler field(s)
        try {
            reporter.logBeforeStep("Populate the Device Labeler/Associated Devices field by searching for device identifier/ Associated device identifiers");

            //IF TEXT SAYS DEVICE LABELER, SEARCH AND SELECT THEN SUBMIT, IF TEST SAYS ASSOCIATED DEVICES, 
            // SEARCH FOR DEVICE, SELECT THEN 
            // CLICK THE NEXT SEARCH AND DO THE SAME UNTIL THE LIST IS EMPTY
            
            if{"Device Labeler".isDisplayed()}{
                    robot.clickOn("🔍  Search");
                    waitForFxEvents();
                    robot.write(deviceLabeler);
                    waitFor(1500); // Wait for results to load
                    //press down arrow then press enter
                    robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
                    robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
                    waitForFxEvents();
            }
            else{
                //complete the following until there list retuns null or empty
                while(deviceLabelers.hasNext()){
                    String associatedDevices = deviceLabelers.next();

                    robot.clickOn("🔍  Search");
                    waitForFxEvents();
                    robot.write(associatedDevices);
                    waitFor(1500); // Wait for results to load
                    //press down arrow then press enter
                    robot.press(KeyCode.DOWN).release(KeyCode.DOWN);
                    robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
                    waitForFxEvents();
                }

            }
            reporter.logAfterStep("Populated the Device Labeler/Associated Devices field successfully");
        } catch (Exception e) {
            reporter.logFailure("Populate the Device Labeler/Associated Devices field by searching for device identifier(s)", e);
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

    /**
     * Connects to GitHub repository for data synchronization (Steps 2-6).
     * This method encapsulates the complete workflow for GitHub connection including:
     * - Opening Exchange menu
     * - Opening Info panel
     * - Entering GitHub credentials
     * - Connecting to repository
     * - Verifying connection success
     *
     * @param githubRepoUrl The GitHub repository URL
     * @param githubEmail The GitHub email address
     * @param githubUsername The GitHub username
     * @param githubPassword The GitHub password
     * @throws InterruptedException if thread is interrupted during execution
     */
    public void connectToGitHub(String githubRepoUrl, String githubEmail, 
                               String githubUsername, String githubPassword) 
            throws InterruptedException {

        // Step 2: In the tool bar, click Exchange
        try {
            reporter.logBeforeStep("Step 2: CLICK 'Exchange'");
            robot.clickOn("Exchange");
            waitForFxEvents();
            reporter.logAfterStep("Step 2: CLICK 'Exchange' successful");
        } catch (Exception e) {
            reporter.logFailure("Step 2: CLICK 'Exchange'", e);
            throw e;
        }

        // Step 3: Click Info
        try {
            reporter.logBeforeStep("Step 3: CLICK 'Info'");
            robot.clickOn("Info");
            waitForFxEvents();
            reporter.logAfterStep("Step 3: CLICK 'Info' successful");
        } catch (Exception e) {
            reporter.logFailure("Step 3: CLICK 'Info'", e);
            throw e;
        }

        // Step 4: Input the repo url and the user's github account info
        try {
            reporter.logBeforeStep("Step 4: INPUT Repo URL and GitHub Credentials");
            // Note: GitHubConnectionPage needs to be instantiated
            dev.ikm.komet.app.test.integration.testfx.pages.GitHubConnectionPage gitHubConnectionPage = 
                new dev.ikm.komet.app.test.integration.testfx.pages.GitHubConnectionPage(robot);
            gitHubConnectionPage.enterGitHubCredentials(githubRepoUrl, githubEmail, githubUsername, githubPassword);
            reporter.logAfterStep("Step 4: INPUT Repo URL and GitHub Credentials successful");
        } catch (Exception e) {
            reporter.logFailure("Step 4: INPUT Repo URL and GitHub Credentials", e);
            throw e;
        }

        // Step 5: Click Connect
        try {
            reporter.logBeforeStep("Step 5: CLICK 'Connect'");
            robot.clickOn("CONNECT");
            waitForFxEvents();
            reporter.logAfterStep("Step 5: CLICK 'Connect' successful");
        } catch (Exception e) {
            reporter.logFailure("Step 5: CLICK 'Connect'", e);
            throw e;
        }

        // Step 6: Wait for connection to establish and close the GitHub Info popup
        try {
            reporter.logBeforeStep("Step 6: Wait for GitHub connection and close 'GitHub Info' popup");

            // Wait for "GitHub Info" popup to appear (indicates successful connection)
            boolean popupFound = false;
            for (int i = 0; i < 30; i++) {
                waitForFxEvents();
                try {
                    // Look for "GitHub Info" text in the scene
                    if (robot.lookup("GitHub Info").tryQuery().isPresent()) {
                        popupFound = true;
                        LOG.info("GitHub Info popup appeared after {} attempts", i + 1);
                        break;
                    }
                } catch (Exception e) {
                    // Popup not found yet, continue waiting
                }
                Thread.sleep(1000);
            }

            if (!popupFound) {
                throw new RuntimeException("GitHub Info popup did not appear within 30 seconds");
            }

            waitForFxEvents();

            // Click CLOSE button to close the popup - use moveTo first then click
            robot.clickOn();
            waitForFxEvents();
            robot.moveTo("CLOSE");
            waitForFxEvents();
            robot.clickOn();
            waitForFxEvents();

            reporter.logAfterStep("Step 6: GitHub connection verified - 'GitHub Info' popup closed successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while waiting for GitHub connection", e);
            reporter.logFailure("Step 6: Wait for GitHub connection", e);
            throw new RuntimeException("Interrupted while waiting for GitHub connection", e);
        } catch (Exception e) {
            reporter.logFailure("Step 6: Wait for GitHub connection and close popup", e);
            throw e;
        }
    }

    /**
     * Runs the reasoner to classify concepts (Step 49).
     * This method encapsulates the complete workflow for running the reasoner including:
     * - Opening reasoner panel
     * - Clicking starburst button
     * - Initiating reasoner run
     * - Confirming execution
     * - Verifying completion
     *
     * @throws InterruptedException if thread is interrupted during execution
     */
    public void runReasoner() throws InterruptedException {

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

    /**
     * Scrolls horizontally in the specified direction.
     * 
     * @param direction The KeyCode direction to scroll (LEFT or RIGHT)
     * @param scrollAmount The number of times to press the key
     */
    private void horizontalScroll(KeyCode direction, int scrollAmount) {
        try {
            reporter.logBeforeStep("Scroll horizontally");
            for (int i = 0; i < scrollAmount; i++) {
                robot.press(direction);
                robot.release(direction);
            }
            reporter.logAfterStep("Scrolled horizontally");
        } catch (Exception e) {
            reporter.logFailure("Scroll horizontally", e);
            throw e;
        }
    }

    /**
     * Waits for the specified amount of time.
     * 
     * @param milliseconds The number of milliseconds to wait
     * @throws InterruptedException if the thread is interrupted
     */
    private void waitFor(int milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }
}
