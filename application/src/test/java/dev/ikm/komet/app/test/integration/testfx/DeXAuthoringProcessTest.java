package dev.ikm.komet.app.test.integration.testfx;

import dev.ikm.komet.app.App;
import dev.ikm.komet.app.AppState;
import dev.ikm.komet.app.test.integration.testfx.config.TestConfiguration;
import dev.ikm.komet.app.test.integration.testfx.helpers.workflows.*;
import dev.ikm.komet.app.test.integration.testfx.pages.*;
import dev.ikm.komet.app.test.integration.testfx.utils.TestReporter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.scene.input.KeyCode;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
@Tag("dex-authoring")
@DisplayName("DeX Authoring Process Test")
public class DeXAuthoringProcessTest extends TestConfiguration {

        private static final Logger LOG = LoggerFactory.getLogger(DeXAuthoringProcessTest.class);

        // Concept Details (instance variables for dynamic data)
        String fullyQualifiedName;
        String status;
        String moduleName;
        String path;
        String parentConceptName;

        private Path screenshotDirectory;
        private Path extentReportsDirectory;
        private App webApp;
        private TestReporter reporter;
        private FxRobot robot;

        // Workflow helpers
        private LaunchKometUserNR launchKometUserNR;
        private LaunchKometUserSA launchKometUserSA;
        private GithubConnection githubConnection;
        private AuthorConcepts authorConcepts;
        private AuthorPatterns authorPatterns;
        private AddDeviceExtensionMembershipPattern addDeviceExtensionMembershipPattern;
        private Reasoner reasoner;
        private GenerateDeXData generateDeXData;
        private AddDeviceCompanySemantic addDeviceCompanySemantic;
        private AddAssociatedDevicesSemantic addAssociatedDevicesSemantic;
        private AddTestPerformedSemantic addTestPerformedSemantic;
        private AddAllowedResultsSemantic addAllowedResultsSemantic;
        private AddQuantitativeAllowedResultsSemantic addQuantitativeAllowedResultsSemantic;
        private AddPopulationReferenceRangeSemantic addPopulationReferenceRangeSemantic;

        @BeforeAll
        public void setUpClass() {
                overrideUserHome();
                createScreenshotDirectory();
                createExtentReportsDirectory();
                
                // Read command-line parameters
                enableInfo = Boolean.parseBoolean(System.getProperty("enableInfo", "true"));
                enableSync = Boolean.parseBoolean(System.getProperty("enableSync", "true"));
    
                LOG.info("=== Starting DeX Authoring Process Test - Version {} ===", APP_VERSION);
                LOG.info("Click Info enabled: {}", enableInfo);
                LOG.info("Click Sync enabled: {}", enableSync);
        }

        @BeforeEach
        void setUp(FxRobot robot) throws Exception {
                this.robot = robot;
                FxToolkit.setupApplication(() -> {
                        webApp = new App();
                        return webApp;
                });

                reporter = new TestReporter(screenshotDirectory, extentReportsDirectory, robot,
                                "DeXAuthoringProcessTest");
                reporter.createTest("DeX Authoring Process Test - v" + APP_VERSION,
                                "Complete DeX authoring workflow with GitHub integration");

                // Initialize workflow helpers
                launchKometUserNR = new LaunchKometUserNR(robot, reporter);
                launchKometUserSA = new LaunchKometUserSA(robot, reporter);
                githubConnection = new GithubConnection(robot, reporter);
                authorConcepts = new AuthorConcepts(robot, reporter);
                authorPatterns = new AuthorPatterns(robot, reporter);
                addDeviceExtensionMembershipPattern = new AddDeviceExtensionMembershipPattern(robot, reporter);
                reasoner = new Reasoner(robot, reporter);
                addDeviceCompanySemantic = new AddDeviceCompanySemantic(robot, reporter);
                addAssociatedDevicesSemantic = new AddAssociatedDevicesSemantic(robot, reporter);
                addTestPerformedSemantic = new AddTestPerformedSemantic(robot, reporter);
                addAllowedResultsSemantic = new AddAllowedResultsSemantic(robot, reporter);
                addQuantitativeAllowedResultsSemantic = new AddQuantitativeAllowedResultsSemantic(robot, reporter);
                addPopulationReferenceRangeSemantic = new AddPopulationReferenceRangeSemantic(robot, reporter);
                generateDeXData = new GenerateDeXData(robot, reporter);
        }

        @AfterEach
        void tearDown() {
                try {
                        FxToolkit.cleanupStages();
                        if (webApp != null) {
                                FxToolkit.cleanupApplication(webApp);
                        }
                } catch (TimeoutException e) {
                        LOG.error("Timeout during teardown", e);
                } catch (Exception e) {
                        LOG.error("Unexpected error during teardown", e);
                }
        }

        @AfterAll
        public void cleanupAfterAll() {
                if (reporter != null) {
                        reporter.flush();
                }
                LOG.info("=== DeX Authoring Process Test Complete ===");
        }

        @Test
        @DisplayName("DeX Authoring Process")
        public void testDeXAuthoringProcess(FxRobot robot) throws TimeoutException, InterruptedException {
                LOG.info("Starting DeX Authoring Process Test");

                ConceptPane conceptPane = new ConceptPane(robot);
                LandingPage landingPage = new LandingPage(robot);
                DataSourceSelectionPage dataSourcePage = new DataSourceSelectionPage(robot);
                LoginPage loginPage = new LoginPage(robot);
                NavigatorPanel navigator = new NavigatorPanel(robot);
                GitHubConnectionPage gitHubConnectionPage = new GitHubConnectionPage(robot);
                LaunchKometUserSA launchKometUserSA = new LaunchKometUserSA(robot);

                // Step 1: Launch KOMET, select dataset, and login as user
                launchKometUserSA.launchKomet(DATA_SOURCE_NAME, USERNAME, PASSWORD);
                LOG.info("✓ Application Launch and Login: Complete");

                // ========= Steps 2-6: Connect to GitHub Repository ==========
                githubConnection.connectToGitHub(GITHUB_REPO_URL, GITHUB_EMAIL, GITHUB_USERNAME, GITHUB_PASSWORD);
                //LOG.info("✓ GitHub Connection: Complete");

                
                // ========== Steps 7-34: Create Concepts (Target, Analyte, Company/Device Labeler) ==========

                // Create Target concept
                authorConcepts.createConcept("Albumin", "Active", "Device Extension Module", "Development path", "Target");
                addDeviceExtensionMembershipPattern.addDeviceExtensionMembershipPattern("Albumin", "Device Extension Membership Pattern",
                               "Active", "Device Extension Module", "Development path");

             
                // Create Analyte concept
                authorConcepts.createConcept("Albumin", "Active", "Device Extension Module", "Development path", "Analyte");
                addDeviceExtensionMembershipPattern.addDeviceExtensionMembershipPattern("Albumin",
                                "Device Extension Membership Pattern", "Active", "Device Extension Module",
                                "Development path");


                // Create Company concept
                authorConcepts.createConcept("Roche Diagnostics GmbH", "Active", "Device Extension Module", "Development path", "Company");
                addDeviceExtensionMembershipPattern.addDeviceExtensionMembershipPattern("Roche Diagnostics GmbH", "Device Extension Membership Pattern",
                                "Active", "Device Extension Module", "Development path");

             
                LOG.info("✓ Concept Creation Complete");


                // ========== Steps 35-51: DeX Data Generation ==========
                generateDeXData.generateDeXData("Albumin Gen2 5166861190", "Roche Diagnostics COBAS Integra Albumin Gen.2");


                LOG.info("✓ DeX Data Generation: Complete");


                // ========= Step 49: Run the Reasoner ==========
                reasoner.runReasoner();

                LOG.info("✓ Reasoner Run: Complete");


                // ========= Steps 50: Click Info ==========
                if (enableInfo) {
                        githubConnection.clickInfoButton();
                        LOG.info("✓ Click Info Button: COMPLETE");
                } else {
                        LOG.info("⊘ Click Info Button: SKIPPED");
                }


                // ========= Steps 51: Click Sync ==========
                if (enableSync) {
                        githubConnection.clickSyncButton();
                        LOG.info("✓ Click Sync Button: COMPLETE");
                } else {
                        LOG.info("⊘ Click Sync Button: SKIPPED");
                }


                // ========== Steps 52-62: Add DeX Device Company Semantic ==========
                addDeviceCompanySemantic.addDeviceCompanySemantic("Device Company Pattern", "Active",
                                "Device Extension Module",
                                "Development path", "Albumin Gen2 5166861190", "Roche Diagnostics GmbH");

                LOG.info("✓ Add DeX Device Company Semantic: Complete");


                // ========== Steps 63-71: Add DeX Associated Devices Semantic ==========
                String[] associatedDeviceIdentifiers = new String[] { "Blank Concept" };

                addAssociatedDevicesSemantic.addAssociatedDevicesSemantic("Associated Devices Pattern", "Active",
                                "Device Extension Module",
                                "Development path", "Albumin Gen2 5166861190", associatedDeviceIdentifiers);

                LOG.info("✓ Add DeX Associated Devices Semantic: Complete");


                // ========== Steps 72-87: Add DeX Test Performed Semantic ==========
                String[] analyteConcepts = new String[] { "Albumin" };
                String[] targetConcepts = new String[] { "Albumin" };
                String[] testPerformedConcepts = new String[] { "61151-7" };
                String[] instrumentConcepts = new String[] { "COBAS 8000 c 702" };
                String[] specimenConcepts = new String[] { "119364003", "119361006" };

                addTestPerformedSemantic.addTestPerformedSemantic("Test Performed Pattern", "Active",
                                "Device Extension Module", "Development path", "Albumin Gen2 5166861190",
                                analyteConcepts, targetConcepts, testPerformedConcepts,
                                instrumentConcepts, specimenConcepts, "2.0", "g/L");

                LOG.info("✓ Add DeX Test Performed Semantic: Complete");
                

                // ========== Steps 88-97: Add DeX Allowed Results Semantic ==========

                String[] qualifierConcepts = new String[] { "258794004" };
                addAllowedResultsSemantic.addAllowedResultsSemantic("Allowed Results Pattern",
                                "Allowed Results Pattern",
                                "Active", "Device Extension Module", "Development path",
                                "[Test Performed] of <Roche Diagnostics COBAS Integra Albumin Gen.2> for [Device Extension Record]",
                                qualifierConcepts);

                LOG.info("✓ Add DeX Allowed Results Semantic: Complete");

                
                // ========== Steps 98-110: Add Dex Quantitative Allowed Result Range Semantic ==========

                addQuantitativeAllowedResultsSemantic.addQuantitativeAllowedResultsSemantic(
                                "Quantitative Allowed Result Range Pattern",
                                "Active", "Device Extension Module", "Development path",
                                "[Test Performed] of <Roche Diagnostics COBAS Integra Albumin Gen.2> for [Device Extension Record]",
                                "Equal To", "Equal To", "0.0", "0.0", "Test");

                LOG.info("✓ Add DeX Quantitative Allowed Result Range Semantic: Complete");


                // ========== Steps 111-125: Add Dex Population Reference Range Semantic ==========

                addPopulationReferenceRangeSemantic.addPopulationReferenceRangeSemantic(
                                "Population Reference Range Pattern",
                                "Active", "Device Extension Module", "Development path",
                                "[Test Performed] of <Roche Diagnostics COBAS Integra Albumin Gen.2> for [Device Extension Record]",
                                "Adults", "Equal To", "Equal To", "52", "35", "g/L");

                LOG.info("✓ Add DeX Population Reference Range Semantic: Complete");


                // ========= Steps 126 - 127: Run the Reasoner ==========
                reasoner.runReasoner();

                LOG.info("✓ Reasoner Run: Complete");


                // ========= Steps 128: Click Info ==========
                if (enableInfo) {
                        githubConnection.clickInfoButton();
                        LOG.info("✓ Click Info Button: COMPLETE");
                } else {
                        LOG.info("⊘ Click Info Button: SKIPPED");
                }       


                // ========= Steps 129: Click Sync ==========
                if (enableSync) {
                        githubConnection.clickSyncButton();
                        LOG.info("✓ Click Sync Button: COMPLETE");
                } else {
                        LOG.info("⊘ Click Sync Button: SKIPPED");
                }


                //========== Additional Steps: Create a Pattern ==========
                LOG.info("Authoring a Pattern");
                authorPatterns.authorPatterns("Test Pattern", "Active",
                                "Device Extension Module", "Development path", "Case insensitive",
                                "English language", "Component display field", "Author", "Author");

                LOG.info("✓ Authoring a Pattern: COMPLETE");

        }

        // ========== Helper Methods ==========

        private void overrideUserHome() {
                try {
                        Path targetDataPath = Paths.get(BASE_DATA_DIR, SOLOR_DIR);
                        if (!Files.exists(targetDataPath)) {
                                Files.createDirectories(targetDataPath);
                                LOG.info("Created data directory: {}", targetDataPath);
                        }
                        System.setProperty(PROPERTY_USER_HOME, targetDataPath.getParent().toString());
                } catch (Exception e) {
                        LOG.error("Failed to override user home directory", e);
                }
        }

        private void createScreenshotDirectory() {
                try {
                        screenshotDirectory = Paths.get(BASE_DATA_DIR, SOLOR_DIR, TEST_SCREENSHOTS_DIR);
                        if (!Files.exists(screenshotDirectory)) {
                                Files.createDirectories(screenshotDirectory);
                                LOG.info("Created screenshot directory: {}", screenshotDirectory);
                        }
                } catch (Exception e) {
                        LOG.error("Failed to create screenshot directory", e);
                }
        }

        private void createExtentReportsDirectory() {
                try {
                        extentReportsDirectory = Paths.get(BASE_DATA_DIR, SOLOR_DIR, EXTENT_REPORTS_DIR);
                        if (!Files.exists(extentReportsDirectory)) {
                                Files.createDirectories(extentReportsDirectory);
                                LOG.info("Created extent reports directory: {}", extentReportsDirectory);
                        }
                } catch (Exception e) {
                        LOG.error("Failed to create extent reports directory", e);
                }
        }

}
