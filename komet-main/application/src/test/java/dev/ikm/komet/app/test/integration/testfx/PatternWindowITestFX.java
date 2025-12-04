package dev.ikm.komet.app.test.integration.testfx;

import dev.ikm.komet.app.App;
import dev.ikm.komet.app.AppState;
import dev.ikm.tinkar.common.service.DataServiceController;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import org.testfx.api.FxService;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.service.support.CaptureSupport;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static java.nio.file.Files.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.util.NodeQueryUtils.hasText;
import static org.testfx.util.WaitForAsyncUtils.waitFor;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Integration test class for testing the PatternWindow functionality using TestFX.
 * <p>
 * This class sets up a JavaFX application environment for testing the UI and interaction
 * of the PatternWindow in the application. It uses JUnit 5 and TestFX to simulate user interactions
 * and verify UI behavior.
 * </p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class PatternWindowITestFX {

    private static final Logger LOG = LoggerFactory.getLogger(PatternWindowITestFX.class);

    // CSS Selectors
    private static final String SELECTOR_DATA_SOURCE_CHOICE_BOX = "#dataSourceChoiceBox";
    private static final String SELECTOR_FILE_LIST_VIEW = "#fileListView";
    private static final String SELECTOR_OK_BUTTON = "#okButton";
    private static final String SELECTOR_LANDING_PAGE_BORDER_PANE = "#landingPageBorderPane";
    private static final String SELECTOR_CREATE_CARD_PANE = "#createCardPane";
    private static final String SELECTOR_JOURNAL_BORDER_PANE = "#journalBorderPane";
    private static final String SELECTOR_ADD_BUTTON = "#addButton";
    private static final String SELECTOR_NEW_PATTERN = "New Pattern";
    private static final String SELECTOR_DETAILS_OUTER_BORDER_PANE = "#detailsOuterBorderPane";
    private static final String SELECTOR_PATTERN_TEXT = "#patternText";
    private static final String SELECTOR_CLOSE_CONCEPT_BUTTON = "#closeConceptButton";
    private static final String SELECTOR_JOURNAL_CARD_NAME = "#journalCardName";
    private static final String SELECTOR_MENU_OPTION_BUTTON = "#menuOptionButton";
    private static final String SELECTOR_DELETE_OPTION = "Delete";

    // Property Keys
    private static final String PROPERTY_TARGET_DATA_DIR = "target.data.directory";
    private static final String PROPERTY_USER_HOME = "user.home";

    // Directory and File Names
    private static final String TINKAR_STARTER_DATA_PREFIX = "tinkar-starter-data";
    private static final String TINKAR_STARTER_DATA_ZIP_PREFIX = TINKAR_STARTER_DATA_PREFIX + "-reasoned";
    private static final String TINKAR_STARTER_DATA_DIR = TINKAR_STARTER_DATA_PREFIX + "-reasoned";
    private static final String TINKAR_STARTER_DATA_EXTENSION = ".zip";
    private static final String BASE_DATA_DIR = System.getProperty(PROPERTY_TARGET_DATA_DIR);
    private static final String SOLOR_DIR = "Solor";
    private static final String TEST_SCREENSHOTS_DIR = "test-screenshots";

    // Button Texts and Labels
    private static final String BUTTON_TEXT_OK = "OK";
    private static final String LABEL_JOURNAL_PREFIX = "Journal";
    private static final String LABEL_PATTERN = "Pattern";

    // Other Strings
    private static final String NODE_PATTERN_PANE = "patternPane";

    // Timeout Constants
    private static final int TIMEOUT_SECONDS = 10;

    private Path screenshotDirectory;
    private App webApp;

    /**
     * Enum representing available data sources.
     */
    private enum DataSource {
        OPEN_SPINED_ARRAY_STORE("Open SpinedArrayStore"),
        NEW_SPINED_ARRAY_STORE("New Spined Array Store");

        private final String displayName;

        DataSource(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }


    /**
     * Sets up the test class by overriding the user home directory,
     * creating the screenshot directory, and ensuring the Tinkar starter data exists.
     */
    @BeforeAll
    public void setUpClass() {
        overrideUserHome();
        createScreenshotDirectory();
        ensureTinkarStarterDataExists();
    }

    /**
     * Sets up the TestFX environment before each test.
     *
     * @throws Exception if there is an error initializing the application
     */
    @BeforeEach
    void setUp() throws Exception {
        // Initialize the application
        FxToolkit.setupApplication(() -> {
            webApp = new App();
            return webApp;
        });
    }

    /**
     * Cleans up the TestFX environment after each test.
     * Ensures stages and the application are properly disposed of.
     */
    @AfterEach
    void tearDown() {
        try {
            FxToolkit.cleanupStages();
            if (webApp != null) {
                FxToolkit.cleanupApplication(webApp);
            }
        } catch (TimeoutException e) {
            LOG.error("Timeout during teardown", e);
            fail("Teardown failed due to timeout.");
        } catch (Exception e) {
            LOG.error("Unexpected error during teardown", e);
            fail("Teardown failed due to unexpected error.");
        }
    }

    /**
     * Tests the PatternWindow functionality by interacting with UI components.
     *
     * @param robot the TestFX {@link FxRobot} instance used to simulate user interactions
     * @throws TimeoutException if an operation times out
     */
    @Test
    @DisplayName("Verify Pattern Window Functionality")
    public void testPatternWindow(FxRobot robot) throws TimeoutException {
        // Wait for the application state to become SELECT_DATA_SOURCE
        assertInitialAppState();

        // Select the Open SpinedArrayStore data source
        selectDataSourceByName(robot, DataSource.OPEN_SPINED_ARRAY_STORE);
        verifySelectedDataSource(robot, DataSource.OPEN_SPINED_ARRAY_STORE);

        // Verify that the file list view is present and contains the tinkar starter data source
        ListView<?> listView = lookupNode(robot, SELECTOR_FILE_LIST_VIEW, ListView.class);
        boolean foundTinkarStarterData = selectListViewItem(robot, listView, TINKAR_STARTER_DATA_PREFIX);

        // Handle the case where the tinkar data source is missing or already exists
        if (!foundTinkarStarterData) {
            createTinkarDataSourceIfMissing(robot, listView);
        } else {
            handleExistingTinkarDataSource(robot, listView);
        }

        // Wait for the application state to become RUNNING
        assertRunningAppState();

        // Verify that the primary stage is present and maximize it
        Pane landingPage = lookupNode(robot, SELECTOR_LANDING_PAGE_BORDER_PANE, Pane.class);
        Stage primaryStage = (Stage) landingPage.getScene().getWindow();
        assertNotNull(primaryStage, "Primary stage should be present");
        maximizeStage(primaryStage);

        // Navigate to create a new journal
        Pane createNewJournalPane = lookupNode(robot, SELECTOR_CREATE_CARD_PANE, AnchorPane.class);
        robot.clickOn(createNewJournalPane);
        waitForFxEvents();

        // Create a new journal and pattern
        Pane journalPane = lookupNode(robot, SELECTOR_JOURNAL_BORDER_PANE, BorderPane.class);
        Stage journalStage = (Stage) journalPane.getScene().getWindow();
        assertNotNull(journalStage, "Journal pane should be present");

        String journalTitle = journalStage.getTitle();
        assertTrue(journalTitle.startsWith(LABEL_JOURNAL_PREFIX),
                "Journal title should start with 'Journal'");

        Button addButton = lookupNode(robot, SELECTOR_ADD_BUTTON, Button.class);
        robot.clickOn(addButton);
        waitForFxEvents();

        robot.clickOn(SELECTOR_NEW_PATTERN);
        waitForFxEvents();

        // Verify that the pattern window is present and the title is correct
        Pane patternPane = lookupNode(robot, SELECTOR_DETAILS_OUTER_BORDER_PANE, BorderPane.class);
        Text patternTitle = lookupNode(robot, SELECTOR_PATTERN_TEXT, Text.class);
        assertEquals(LABEL_PATTERN, patternTitle.getText(), "Pattern title should be 'Pattern'");

        // Capture and save a screenshot of the pattern window
        saveNodeScreenshot(patternPane, NODE_PATTERN_PANE);

        // Close the pattern window
        Button closeButton = lookupNode(robot, SELECTOR_CLOSE_CONCEPT_BUTTON, Button.class);
        robot.clickOn(closeButton);
        waitForFxEvents();

        // Close the journal window
        Platform.runLater(journalStage::close);
        waitForFxEvents();

        // Delete the journal card that was created in the test to clean up
        Text targetJournalText = robot.lookup(SELECTOR_JOURNAL_CARD_NAME).queryAll().stream()
                .filter(node -> node instanceof Text)
                .map(node -> (Text) node)
                .filter(text -> journalTitle.equals(text.getText()))
                .findFirst()
                .orElse(null);
        assertNotNull(targetJournalText, "Text " + journalTitle + " not found in any journal card.");

        // Traverse up the hierarchy to find the journal card
        Parent journalCard = targetJournalText.getParent();
        while (journalCard != null && !(journalCard instanceof AnchorPane)) {
            journalCard = journalCard.getParent();
        }

        // Lookup the menu option button in the journal card and click the delete option
        Pane journalCardPane = (AnchorPane) journalCard;
        Button cardMenuOptionButton = robot.from(journalCardPane).lookup(SELECTOR_MENU_OPTION_BUTTON).queryAs(Button.class);
        assertNotNull(cardMenuOptionButton, "Menu option button should be present");
        robot.clickOn(cardMenuOptionButton);
        waitForFxEvents();
        robot.clickOn(SELECTOR_DELETE_OPTION);
        waitForFxEvents();

        // Get the App state and close the primary stage
        LOG.info("WebApp.state.get() = {}", App.state.get());
        Platform.runLater(primaryStage::close);
    }

    /**
     * Asserts that the initial state of the application is SELECT_DATA_SOURCE.
     */
    private void assertInitialAppState() {
        assertEquals(AppState.SELECT_DATA_SOURCE, App.state.get(),
                "Initial state should be SELECT_DATA_SOURCE");
    }

    /**
     * Waits for the application state to become RUNNING and asserts it.
     *
     * @throws TimeoutException if the application state does not become RUNNING within the timeout period
     */
    private void assertRunningAppState() throws TimeoutException {
        LOG.info("Waiting up to {} seconds for the application state to become RUNNING.", TIMEOUT_SECONDS);
        waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS, () -> App.state.get() == AppState.RUNNING);
        assertEquals(AppState.RUNNING, App.state.get(), "Application state should be RUNNING");
        LOG.info("Application state has reached RUNNING.");
        waitForFxEvents();
    }

    /**
     * Selects a data source by its name from a {@link ChoiceBox}.
     *
     * @param robot          the TestFX {@link FxRobot} instance
     * @param dataSourceEnum the enum representing the data source to select
     */
    private void selectDataSourceByName(FxRobot robot, DataSource dataSourceEnum) {
        ChoiceBox<DataServiceController<?>> choiceBox = lookupNode(robot, SELECTOR_DATA_SOURCE_CHOICE_BOX, ChoiceBox.class);

        Platform.runLater(() -> {
            for (DataServiceController<?> controller : choiceBox.getItems()) {
                if (controller.controllerName().equals(dataSourceEnum.getDisplayName())) {
                    choiceBox.getSelectionModel().select(controller);
                    break;
                }
            }
        });

        waitForFxEvents();

        // Verify selection
        DataServiceController<?> selectedController = choiceBox.getSelectionModel().getSelectedItem();
        assertNotNull(selectedController, "A data source should be selected");
        assertEquals(dataSourceEnum.getDisplayName(), selectedController.controllerName(),
                "Selected data source should match the expected name");
    }

    /**
     * Verifies that the selected data source in the {@link ChoiceBox} matches the expected name.
     *
     * @param robot          the TestFX {@link FxRobot} instance
     * @param dataSourceEnum the enum representing the expected data source
     */
    private void verifySelectedDataSource(FxRobot robot, DataSource dataSourceEnum) {
        ChoiceBox<DataServiceController<?>> choiceBox = lookupNode(robot, SELECTOR_DATA_SOURCE_CHOICE_BOX, ChoiceBox.class);

        DataServiceController<?> selectedController = choiceBox.getSelectionModel().getSelectedItem();
        assertNotNull(selectedController, "A data source should be selected in the ChoiceBox");

        String actualName = selectedController.controllerName();
        assertEquals(dataSourceEnum.getDisplayName(), actualName,
                String.format("Selected controller name should be '%s' but was '%s'",
                        dataSourceEnum.getDisplayName(), actualName));

        waitForFxEvents();
    }

    /**
     * Selects an item in a {@link ListView} whose text contains the specified prefix.
     *
     * @param robot    the TestFX {@link FxRobot} instance
     * @param listView the {@link ListView} to search
     * @param prefix   the prefix to match in item text
     * @return true if an item was selected, false otherwise
     */
    private boolean selectListViewItem(FxRobot robot, ListView<?> listView, String prefix) {
        return listView.getItems().stream()
                .filter(item -> item.toString().contains(prefix))
                .findFirst()
                .map(item -> {
                    robot.clickOn(item.toString());
                    return true;
                })
                .orElse(false);
    }

    /**
     * Handles the case where the tinkar data source is missing by creating a new one.
     *
     * @param robot    the TestFX {@link FxRobot} instance
     * @param listView the {@link ListView} to update
     */
    private void createTinkarDataSourceIfMissing(FxRobot robot, ListView<?> listView) {
        selectDataSourceByName(robot, DataSource.NEW_SPINED_ARRAY_STORE);
        waitForFxEvents();

        listView = lookupNode(robot, SELECTOR_FILE_LIST_VIEW, ListView.class);
        // Verify that the new Spined Array Store (SAS) data source is selected
        boolean foundTinkarNewSAS = selectListViewItem(robot, listView, TINKAR_STARTER_DATA_PREFIX);
        assertTrue(foundTinkarNewSAS, "Should find 'tinkar' in the new Spined Array Store data source");

        TextField newFolderTextField = robot.lookup(node -> node instanceof TextField && node.isVisible()).query();
        robot.clickOn(newFolderTextField);
        robot.write(TINKAR_STARTER_DATA_DIR);
        waitForFxEvents();

        verifyThat(SELECTOR_OK_BUTTON, isVisible());
        verifyThat(SELECTOR_OK_BUTTON, hasText(BUTTON_TEXT_OK));

        Button okButton = lookupNode(robot, SELECTOR_OK_BUTTON, Button.class);
        robot.clickOn(okButton);
        waitForFxEvents();
    }

    /**
     * Handles the case where the tinkar data source already exists.
     *
     * @param robot    the TestFX {@link FxRobot} instance
     * @param listView the {@link ListView} to verify
     */
    private void handleExistingTinkarDataSource(FxRobot robot, ListView<?> listView) {
        Object selectedListItem = listView.getSelectionModel().getSelectedItem();
        assertNotNull(selectedListItem, "An item should be selected in the ListView");
        assertTrue(selectedListItem.toString().startsWith(TINKAR_STARTER_DATA_PREFIX),
                "Selected item should start with " + TINKAR_STARTER_DATA_PREFIX);

        verifyThat(SELECTOR_OK_BUTTON, isVisible());
        verifyThat(SELECTOR_OK_BUTTON, hasText(BUTTON_TEXT_OK));

        Button okButton = lookupNode(robot, SELECTOR_OK_BUTTON, Button.class);
        robot.clickOn(okButton);
        waitForFxEvents();
    }

    /**
     * Maximizes the given JavaFX {@link Stage}.
     *
     * @param stage the stage to maximize
     */
    private void maximizeStage(Stage stage) {
        Platform.runLater(() -> {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            // Center the stage (optional since we're maximizing)
            double centerX = bounds.getMinX() + (bounds.getWidth() - stage.getWidth()) / 2;
            double centerY = bounds.getMinY() + (bounds.getHeight() - stage.getHeight()) / 2;
            stage.setX(centerX);
            stage.setY(centerY);
            stage.setMaximized(true);
        });
        waitForFxEvents();
    }

    /**
     * Creates the screenshot directory within the temporary directory.
     * Ensures the directory exists before tests are executed.
     */
    private void createScreenshotDirectory() {
        try {
            screenshotDirectory = Paths.get(BASE_DATA_DIR, SOLOR_DIR, TEST_SCREENSHOTS_DIR);
            if (notExists(screenshotDirectory)) {
                createDirectories(screenshotDirectory);
                LOG.info("Created screenshot directory at {}", screenshotDirectory.toAbsolutePath());
            } else {
                LOG.info("Screenshot directory already exists at {}", screenshotDirectory.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create screenshot directory", e);
        }
    }

    /**
     * Captures a screenshot of the specified JavaFX node and saves it to the screenshot directory.
     *
     * @param node       the JavaFX node to capture
     * @param identifier a unique identifier for the screenshot file name
     */
    private void saveNodeScreenshot(Node node, String identifier) {
        CaptureSupport captureSupport = FxService.serviceContext().getCaptureSupport();
        Image image = captureSupport.captureNode(node);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        String screenshotName = String.format("%s_%s.png", identifier, timestamp);
        Path screenshotPath = screenshotDirectory.resolve(screenshotName);
        LOG.info("Node screenshot saved at {}", screenshotPath.toAbsolutePath());
        captureSupport.saveImage(image, screenshotPath);
    }

    /**
     * Ensures that the 'tinkar-starter-data' directory or its corresponding ZIP file exists within the Solor directory.
     * If neither is found, logs an error and fails the test setup.
     */
    private void ensureTinkarStarterDataExists() {
        final Path solorDirectory = Paths.get(BASE_DATA_DIR, SOLOR_DIR);
        final Path tinkarDataPath = solorDirectory.resolve(TINKAR_STARTER_DATA_DIR);

        try {
            final boolean directoryExists = isDirectory(tinkarDataPath);
            final boolean zipExists = zipFileExists(solorDirectory, TINKAR_STARTER_DATA_ZIP_PREFIX);

            if (directoryExists) {
                LOG.info("'{}' directory already exists at '{}'.", TINKAR_STARTER_DATA_DIR,
                        tinkarDataPath.toAbsolutePath());
                return;
            }

            if (zipExists) {
                LOG.info("ZIP file for '{}' exists in '{}'.", TINKAR_STARTER_DATA_DIR,
                        solorDirectory.toAbsolutePath());
                return;
            }

            LOG.error("Neither '{}' directory nor a ZIP file starting with '{}' was found in '{}'.",
                    TINKAR_STARTER_DATA_DIR, TINKAR_STARTER_DATA_ZIP_PREFIX, solorDirectory.toAbsolutePath());
            fail("Required data 'tinkar-starter-data' could not be found in the Solor directory.");

        } catch (Exception e) {
            LOG.error("Error while ensuring 'tinkar-starter-data' exists.", e);
            fail("Failed to verify 'tinkar-starter-data' existence due to an unexpected error.");
        }
    }

    /**
     * Checks if a ZIP file with a specified prefix exists in a directory.
     *
     * @param directory the directory to search in
     * @param prefix    the prefix of the ZIP file
     * @return true if a matching ZIP file exists, false otherwise
     */
    private boolean zipFileExists(Path directory, String prefix) {
        try (Stream<Path> files = list(directory)) {
            return files.anyMatch(path -> {
                String fileName = path.getFileName().toString();
                return fileName.startsWith(prefix) &&
                        fileName.endsWith(TINKAR_STARTER_DATA_EXTENSION) && isRegularFile(path);
            });
        } catch (IOException e) {
            LOG.error("Failed to access Solor directory to verify tinkar-starter-data.", e);
            fail("Failed to access Solor directory to verify tinkar-starter-data.");
            return false;
        }
    }

    /**
     * Looks up a JavaFX {@link Node} by its CSS selector and type.
     *
     * @param robot    the TestFX {@link FxRobot} instance
     * @param selector the CSS selector of the node
     * @param type     the class type of the node
     * @param <T>      the type of the node
     * @return the found node cast to the specified type
     */
    private <T extends Node> T lookupNode(FxRobot robot, String selector, Class<T> type) {
        T node = robot.lookup(selector).queryAs(type);
        assertNotNull(node, String.format("Node with selector '%s' and type '%s' should be present", selector, type.getSimpleName()));
        return node;
    }

    /**
     * Overrides the user home directory to point to the test data directory.
     * This is useful for ensuring that tests use a consistent and isolated data directory.
     */
    private static void overrideUserHome() {
        String absoluteDataDir = Paths.get(BASE_DATA_DIR).toAbsolutePath().toString();
        System.setProperty(PROPERTY_USER_HOME, absoluteDataDir);
        LOG.info("Overriding {} to {}", PROPERTY_USER_HOME, absoluteDataDir);
    }
}