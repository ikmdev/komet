package dev.ikm.komet.app.test.integration.testfx.helpers.workflows;

import org.testfx.api.FxRobot;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Workflow helper for generating Device Extension (DeX) data.
 * This workflow encapsulates the process of creating DeX data entries by searching for
 * devices, copying their identifiers, and adding device names as alternate descriptions.
 * This is a critical step in the DeX authoring process that links device records to
 * their extension metadata.
 * 
 * Key Features:
 * 
 *   Opening a new journal for DeX data entry
 *   Searching for devices by supplied brand name
 *   Opening and copying device identifiers
 *   Adding device names as 'Other Name' descriptions
 *   Updating module metadata for device concepts
 * 
 */
public class GenerateDeXData extends BaseWorkflow {

    /**
     * Constructs a GenerateDeXData workflow helper.
     * 
     * @param robot    FxRobot instance for UI interactions
     * @param reporter TestReporter instance for logging test steps
     */
    public GenerateDeXData(FxRobot robot, TestReporter reporter) {
        super(robot, reporter);
    }

    /**
     * Generates DeX data by searching for a device and adding its name.
     * 
     * @param suppliedBrandName   The brand name to search for (e.g., "Albumin Gen2 5166861190")
     * @param identifiedDevice    The device name to add as 'Other Name' (e.g., "Roche Diagnostics COBAS Integra Albumin Gen.2")
     * @throws InterruptedException if thread is interrupted during execution
     */
    public void generateDeXData(String suppliedBrandName, String identifiedDevice) throws InterruptedException {
    
        LOG.info("====== DeX Data Generation ======");

    // Open a new journal for DeX data entry
                try {
                        reporter.logBeforeStep("Open a new journal for DeX data entry");
                        landingPage.clickNewProjectJournal();
                        reporter.logAfterStep("Opened a new journal for DeX data entry successfully");
                } catch (Exception e) {
                        reporter.logFailure("Open a new journal for DeX data entry", e);
                        throw e;
                }

                // Step 35: Search for the identified device by the supplied brand name + version/model for the DeX record you are creating
                try {
                        reporter.logBeforeStep("Step 35: Search for the identified device by the supplied brand name + version/model for the DeX record you are creating");
                        navigator.clickNextgenSearch();
                        navigator.nextgenSearch(suppliedBrandName);
                        //navigator.openNextGenSearchResult(suppliedBrandName);
                        //use after othr name is added
                        navigator.openNextGenSearchResult(identifiedDevice);
                        conceptPane.clickCopyButton();
                        reporter.logAfterStep("Step 35: Searched for the identified device successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 35: Search for the identified device by the supplied brand name + version/model for the DeX record you are creating", e);
                        throw e;
                }

                /*
                
                // Step 36: Add the Device Name from the Device Extension Data as an 'Other Name'
                try {
                        reporter.logBeforeStep("Step 36: Add the Device Name from the Device Extension Data as an 'Other Name'");
                        conceptPane.clickEditDescriptionsButton();
                        conceptPane.updateName(identifiedDevice);
                        conceptPane.updateModule("Device Extension Module");
                        conceptPane.submit();
                        reporter.logAfterStep("Step 36: Added the Device Name from the Device Extension Data as an 'Other Name' successfully");
                } catch (Exception e) {
                        reporter.logFailure("Step 36: Add the Device Name from the Device Extension Data as an 'Other Name'", e);
                        throw e;
                }
                
                //Steps 37 - 48 omitted

                
                
                // Step 49: Run Reasoner
                reasoner.runReasoner();     

                // Step 50: Click Info
                gitHubConnection.clickInfo();

                // Step 51: Click Sync
                gitHubConnection.clickSync();

                */

                //Close the journal
                try {
                        reporter.logBeforeStep("Close the DeX data journal");
                        landingPage.closeJournalWindow();
                        reporter.logAfterStep("Closed the DeX data journal successfully");
                } catch (Exception e) {
                        reporter.logFailure("Close the DeX data journal", e);
                        throw e;
                }
    }
}
