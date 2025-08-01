/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.mvvm.view.landingpage;

import static dev.ikm.komet.framework.controls.TimeAgoCalculatorUtil.calculateTimeAgoWithPeriodAndDuration;
import static dev.ikm.tinkar.events.FrameworkTopics.IMPORT_TOPIC;
import static dev.ikm.tinkar.events.FrameworkTopics.LANDING_PAGE_TOPIC;
import static dev.ikm.komet.framework.events.appevents.ProgressEvent.SUMMON;
import static dev.ikm.komet.kview.events.CreateJournalEvent.CREATE_JOURNAL;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.JournalTileEvent.CREATE_JOURNAL_TILE;
import static dev.ikm.komet.kview.fxutils.FXUtils.runOnFxThread;
import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.getJournalDirName;
import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.getJournalPreferences;
import static dev.ikm.komet.kview.mvvm.model.Constants.JOURNAL_NAME_PREFIX;
import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.CANCEL_BUTTON_TEXT_PROP;
import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.TASK_PROPERTY;
import static dev.ikm.komet.preferences.JournalWindowPreferences.DEFAULT_JOURNAL_HEIGHT;
import static dev.ikm.komet.preferences.JournalWindowPreferences.DEFAULT_JOURNAL_WIDTH;
import static dev.ikm.komet.preferences.JournalWindowPreferences.DEFAULT_JOURNAL_XPOS;
import static dev.ikm.komet.preferences.JournalWindowPreferences.DEFAULT_JOURNAL_YPOS;
import static dev.ikm.komet.preferences.JournalWindowPreferences.JOURNALS;
import static dev.ikm.komet.preferences.JournalWindowPreferences.JOURNAL_IDS;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_AUTHOR;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_DIR_NAME;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_HEIGHT;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_LAST_EDIT;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_TITLE;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_WIDTH;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_XPOS;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_YPOS;
import static dev.ikm.komet.preferences.JournalWindowSettings.WINDOW_NAMES;
import static javafx.stage.PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT;
import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.komet.framework.events.appevents.ProgressEvent;
import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.framework.progress.ProgressHelper;
import dev.ikm.komet.kview.controls.NotificationPopup;
import dev.ikm.komet.kview.events.CreateJournalEvent;
import dev.ikm.komet.kview.events.DeleteJournalEvent;
import dev.ikm.komet.kview.events.JournalTileEvent;
import dev.ikm.komet.kview.mvvm.model.JournalCounter;
import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.kview.mvvm.view.progress.ProgressController;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for the application's landing page that manages journal cards and their interactions.
 * <p>
 * This controller implements the BasicController interface and is responsible for:
 * <ul>
 *     <li>Displaying journal cards in a grid view</li>
 *     <li>Creating new journal instances</li>
 *     <li>Managing existing journals</li>
 *     <li>Loading and saving journal preferences</li>
 *     <li>Handling journal-related events (create, delete)</li>
 * </ul>
 * <p>
 * The controller uses an event bus system to communicate with other parts of the application
 * and manages preferences storage for persistent journal data.
 */
public class LandingPageController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(LandingPageController.class);

    @FXML
    private Label welcomeTitleLabel;

    @FXML
    private Hyperlink githubStatusHyperlink;

    @FXML
    private Label selectedDatasetTitleLabel;

    @FXML
    private ScrollPane journalProjectCardScrollPane;

    @FXML
    private FlowPane gridViewFlowPane;

    @FXML
    private ToggleButton progressToggleButton;

    @FXML
    ToggleButton settingsToggleButton;

    @FXML
    Button newProjectJournalButton;

    @FXML
    Pane createCardPane;

    @FXML
    BorderPane landingPageBorderPane;

    @FXML
    ComboBox<String> notificationTypeFilterComboBox;

    public static final String DEMO_AUTHOR = "David";
    public static final String LANDING_PAGE_SOURCE = "LANDING_PAGE_SOURCE";

    private final EvtBus landingPageEventBus = EvtBusFactory.getDefaultEvtBus();
    private final Map<UUID, JournalCardController> journalCardControllerMap = new HashMap<>();

    private Subscriber<JournalTileEvent> createJournalTileSubscriber;
    private Subscriber<DeleteJournalEvent> deleteJournalSubscriber;

    private final VBox progressPopupPane = new VBox();
    private NotificationPopup progressNotificationPopup;


    @FXML
    @Override
    public void initialize() {
        clearView();

        notificationTypeFilterComboBox.getItems().addAll("All types");
        notificationTypeFilterComboBox.getSelectionModel().selectFirst();

        progressPopupPane.getStyleClass().add("progress-popup-pane");

        LOG.debug("Event bus instance %s, %s".formatted(this.getClass().getSimpleName(), landingPageEventBus));
        createJournalTileSubscriber = evt -> {

            // If NOT a CREATE_JOURNAL_TILE type do not execute code below!
            if (evt.getEventType() != CREATE_JOURNAL_TILE) return;

            final UUID journalTopic;
            final String journalName;

            //Creating a new journal card
            FXMLLoader journalCardLoader = new FXMLLoader(LandingPageController.class.getResource("journal-card.fxml"));
            Pane journalCard;
            JournalCardController journalCardController;
            try {
                journalCard = journalCardLoader.load();
                journalCardController = journalCardLoader.getController();

            } catch (IOException e) {
                throw new RuntimeException("unable to load journal card fxml ", e);
            }

            PrefX journalWindowSettingsObjectMap = evt.getJournalWindowSettingsMap();
            if (null != journalWindowSettingsObjectMap) {
                journalTopic = journalWindowSettingsObjectMap.getValue(JOURNAL_TOPIC);
                journalName = journalWindowSettingsObjectMap.getValue(JOURNAL_TITLE);
                LocalDateTime nowDateTime = LocalDateTime.now();
                ZoneId nowZoneId = ZoneId.systemDefault();
                String calculatedTimeAgo = calculateTimeAgoWithPeriodAndDuration(nowDateTime, nowZoneId);
                journalCardController.setJournalTimestampValue(calculatedTimeAgo);
                List<String> journalWindowNames = journalWindowSettingsObjectMap.getValue(WINDOW_NAMES);
                journalCardController.setJournalCardWindowCount(journalWindowNames != null ?
                        "Windows: " + journalWindowNames.size() : "Windows: 0");
            } else {
                journalTopic = UUID.randomUUID();
                journalName = "Journal " + JournalCounter.getInstance().get();
                journalWindowSettingsObjectMap = PrefX.create();
                journalWindowSettingsObjectMap.setValue(JOURNAL_TOPIC, journalTopic);
                journalWindowSettingsObjectMap.setValue(JOURNAL_TITLE, journalName);
                journalCardController.setJournalTimestampValue("Edited Now");
            }
            journalCardController.setJournalTopic(journalTopic);
            journalCardController.setJournalCardName(journalName);
            final PrefX journalSettingsFinal = journalWindowSettingsObjectMap;
            // get the correct Journal X name where X is a counting number 1...n
            // decide if the name comes from the event or not
            journalCard.setOnMouseClicked(event -> {
                PrefX prefX;
                // if card already exists then load from disk.
                if (gridViewFlowPane.getChildren().contains(journalCard)) {
                    // fetch preferences from disk for journal settings.
                    prefX = loadJournalWindowPreference(
                            journalSettingsFinal.getValue(JOURNAL_TOPIC),
                            journalSettingsFinal.getValue(JOURNAL_DIR_NAME));
                } else {
                    // newly added card to landing page.
                    prefX = journalSettingsFinal;
                }
                // fire create journal event... AND this should be the ONLY place it comes from besides the menu
                landingPageEventBus.publish(JOURNAL_TOPIC, new CreateJournalEvent(this, CREATE_JOURNAL, prefX));
            });
            journalCardControllerMap.put(journalTopic, journalCardController);
            journalCard.setUserData(journalSettingsFinal);
            gridViewFlowPane.getChildren().addFirst(journalCard);
        };

        landingPageEventBus.subscribe(JOURNAL_TOPIC, JournalTileEvent.class, createJournalTileSubscriber);

        deleteJournalSubscriber = evt -> {
            // remove the tile by finding its journal name
            gridViewFlowPane.getChildren().removeIf(node -> {

                boolean removeIt = node.getUserData() != null && ((PrefX) node.getUserData()).getValue(JOURNAL_TOPIC) != null
                        && ((PrefX) node.getUserData()).getValue(JOURNAL_TOPIC).equals(evt.getJournalTopic());

                if (!removeIt) return false;

                if (node.getUserData() instanceof PrefX prefX) {
                    final UUID journalTopic = prefX.getValue(JOURNAL_TOPIC);
                    final String journalDirName = getJournalDirName(journalTopic);

                    // remove preferences.
                    KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
                    KometPreferences journalsPreferences = appPreferences.node(JOURNALS);
                    KometPreferences journalWindowPreferences = journalsPreferences.node(journalDirName);
                    List<String> journalDirNames = journalsPreferences.getList(JOURNAL_IDS);
                    journalDirNames.remove(journalDirName);
                    journalsPreferences.putList(JOURNAL_IDS, journalDirNames);

                    try {
                        journalWindowPreferences.removeNode();
                        journalsPreferences.sync();
                        // TODO Remove all concept folders.
                    } catch (BackingStoreException e) {
                        throw new RuntimeException(e);
                    } finally {
                        journalCardControllerMap.remove((UUID) prefX.getValue(JOURNAL_TOPIC)).cleanup();
                    }
                }
                return true;
            });

            // reset Journal counter (The add journal card is in the flow pane)
            int maxJournalNumber = gridViewFlowPane.getChildren()
                    .stream()
                    .filter(node -> node.getUserData() instanceof PrefX)
                    .map(node -> (PrefX) node.getUserData())
                    .map(prefX -> parseJournalNumber(prefX.getValue(JOURNAL_TITLE).toString()))
                    .max(Comparator.naturalOrder())
                    .orElse(0);
            JournalCounter.getInstance().set(maxJournalNumber);
        };
        landingPageEventBus.subscribe(JOURNAL_TOPIC, DeleteJournalEvent.class, deleteJournalSubscriber);

        journalProjectCardScrollPane.viewportBoundsProperty().addListener((ov, oldBounds, bounds) -> {
            gridViewFlowPane.setPrefWidth(bounds.getWidth());
            gridViewFlowPane.setPrefHeight(bounds.getHeight());
        });

        // Setup the progress listener for task progress events
        setupProgressListener();

        // Load the preferences for the landing page
        loadPreferencesForLandingPage();
    }

    /**
     * Subscribes to progress events (on {@code PROGRESS_TOPIC}) and displays a
     * {@link NotificationPopup} to show progress information.
     * <p>
     * When a new {@link ProgressEvent} with the event type {@code SUMMON} is received,
     * this method:
     * <ul>
     *   <li>Creates and configures the {@link NotificationPopup} that displays the
     *       current progress tasks.</li>
     *   <li>Makes the {@code progressToggleButton} visible (so the user can manually
     *       show or hide the popup).</li>
     *   <li>Builds the progress user interface and attaches it to the popup.</li>
     * </ul>
     */
    private void setupProgressListener() {
        // Subscribe to progress events on the event bus
        Subscriber<ProgressEvent> progressPopupSubscriber = evt -> {
            // if SUMMON event type, load stuff and reference task to progress popup
            if (evt.getEventType() == SUMMON) {
                runOnFxThread(() -> {
                    // Make the toggle button visible so users can open the popover
                    progressToggleButton.setVisible(true);

                    Task<Void> task = evt.getTask();

                    // Build the UI (Pane + Controller) for the progress popup
                    JFXNode<Pane, ProgressController> progressJFXNode = createProgressBox(task, evt.getCancelButtonText());
                    ProgressController progressController = progressJFXNode.controller();
                    Pane progressPane = progressJFXNode.node();

                    // Create a new NotificationPopup to show the progress pane
                    progressNotificationPopup = new NotificationPopup(progressPopupPane);
                    progressNotificationPopup.setAnchorLocation(WINDOW_BOTTOM_LEFT);

                    // Hide popup when clicking on the progressPopupPane background (if autoHide is enabled)
                    progressPopupPane.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
                        if (e.getPickResult().getIntersectedNode() == progressPopupPane
                                && progressNotificationPopup.isAutoHide()) {
                            progressNotificationPopup.hide();
                        }
                    });

                    // Close button handler in the progress pane
                    progressController.getCloseProgressButton().setOnAction(actionEvent -> {
                        // Cancel the task
                        ProgressHelper.cancel(task);

                        // Remove the progress pane from the popup
                        progressPopupPane.getChildren().remove(progressPane);
                        if (progressPopupPane.getChildren().isEmpty()) {
                            progressToggleButton.setSelected(false);
                            progressToggleButton.setVisible(false);
                        }
                    });

                    progressNotificationPopup.setOnShown(windowEvent -> {
                        // Select the toggle button when the popup is shown
                        progressToggleButton.setSelected(true);
                    });

                    progressNotificationPopup.setOnHidden(windowEvent -> {
                        // Deselect the toggle button when the popup is hidden
                        progressToggleButton.setSelected(false);
                    });

                    progressToggleButton.setOnAction(actionEvent -> {
                        // Toggle button logic to show/hide the popup
                        if (progressToggleButton.isSelected()) {
                            if (progressNotificationPopup.isShowing()) {
                                progressNotificationPopup.hide();
                            } else {
                                progressNotificationPopup.show(progressToggleButton, this::supplyProgressPopupAnchorPoint);
                            }
                        } else {
                            progressNotificationPopup.hide();
                        }
                    });

                    // Before adding the progress UI to the popup's vertical container
                    // Check if we already have 4 progress panes and remove the oldest one if needed
                    if (progressPopupPane.getChildren().size() >= 4) {
                        // Remove the oldest progress pane (first child)
                        Node oldestPane = progressPopupPane.getChildren().getFirst();
                        progressPopupPane.getChildren().remove(oldestPane);
                    }

                    // Add the progress UI to the popup's vertical container
                    progressPopupPane.getChildren().add(progressPane);

                    // Show the progress popup immediately for this new task
                    progressNotificationPopup.show(progressToggleButton, this::supplyProgressPopupAnchorPoint);
                });
            }
        };
        landingPageEventBus.subscribe(LANDING_PAGE_TOPIC, ProgressEvent.class, progressPopupSubscriber);
    }

    private JFXNode<Pane, ProgressController> createProgressBox(Task<Void> task, String cancelButtonText) {
        Config config = new Config(ProgressController.class.getResource("progress.fxml"))
                .updateViewModel("progressViewModel", (viewModel -> viewModel
                        .setPropertyValue(TASK_PROPERTY, task)
                        .setPropertyValue(CANCEL_BUTTON_TEXT_PROP, cancelButtonText))
                );

        return FXMLMvvmLoader.make(config);
    }

    /**
     * Computes and returns the coordinates at which the progress popup
     * ({@link #progressNotificationPopup}) should be anchored, ensuring it appears to
     * the right of the {@code progressToggleButton} and near the lower edge of
     * the workspace.
     * <p>
     * The resulting anchor point is used by {@link NotificationPopup#show(Node, Supplier)}
     * or similar popup methods to place the popup on the screen.
     *
     * @return a {@code Point2D} representing the (X, Y) coordinates where the progress
     * popup should be anchored
     */
    private Point2D supplyProgressPopupAnchorPoint() {
        final Bounds progressToggleButtonScreenBounds =
                progressToggleButton.localToScreen(progressToggleButton.getBoundsInLocal());
        final Bounds landingPageScreenBounds = landingPageBorderPane.localToScreen(landingPageBorderPane.getBoundsInLocal());
        final double progressListVBoxPadding = 12.0;  // Padding around the progress list VBox

        // Adjust the progress popup’s height to fit within the workspace bounds.
        progressPopupPane.setPrefHeight(landingPageScreenBounds.getHeight() - (4 * progressListVBoxPadding - 4.0));

        // Position the popup to the right of the toggle button, near the bottom of the workspace.
        final double popupAnchorX = progressToggleButtonScreenBounds.getMinX()
                + progressToggleButton.getWidth() + progressListVBoxPadding;
        final double popupAnchorY = landingPageScreenBounds.getMaxY() - 2 * progressListVBoxPadding;
        return new Point2D(popupAnchorX, popupAnchorY);
    }

    /**
     * Loads journal window preferences from storage.
     * <p>
     * This method retrieves the saved preferences for a specific journal identified by its UUID
     * and directory name, creating a preferences map with all the journal settings.
     *
     * @param journalTopic The UUID of the journal
     * @param journalDirName The directory name for the journal
     * @return A PrefX object containing all the journal's preferences
     */
    private PrefX loadJournalWindowPreference(UUID journalTopic, String journalDirName) {
        final KometPreferences journalWindowPreferences = getJournalPreferences(journalTopic);
        final String journalTitle = journalWindowPreferences.get(JOURNAL_TITLE)
                .orElse("Journal %s".formatted(JournalCounter.getInstance().get()));

        Double xpos = journalWindowPreferences.getDouble(JOURNAL_XPOS, DEFAULT_JOURNAL_XPOS);
        Double ypos = journalWindowPreferences.getDouble(JOURNAL_YPOS, DEFAULT_JOURNAL_YPOS);
        Double width = journalWindowPreferences.getDouble(JOURNAL_WIDTH, DEFAULT_JOURNAL_WIDTH);
        Double height = journalWindowPreferences.getDouble(JOURNAL_HEIGHT, DEFAULT_JOURNAL_HEIGHT);
        String journalAuthor = journalWindowPreferences.get(JOURNAL_AUTHOR, "");
        OptionalLong journalLastEditOpt = journalWindowPreferences.getLong(JOURNAL_LAST_EDIT);
        List<String> windowList = journalWindowPreferences.getList(
                journalWindowPreferences.enumToGeneralKey(WINDOW_NAMES));

        return PrefX.create()
                .setValue(JOURNAL_DIR_NAME, journalDirName)
                .setValue(JOURNAL_TOPIC, journalTopic)
                .setValue(JOURNAL_TITLE, journalTitle)
                .setValue(JOURNAL_HEIGHT, height)
                .setValue(JOURNAL_WIDTH, width)
                .setValue(JOURNAL_XPOS, xpos)
                .setValue(JOURNAL_YPOS, ypos)
                .setValue(WINDOW_NAMES, windowList)
                .setValue(JOURNAL_AUTHOR, journalAuthor)
                .setValue(JOURNAL_LAST_EDIT, journalLastEditOpt.isPresent() ?
                        journalLastEditOpt.getAsLong() : null);
    }

    /**
     * Loads all journal preferences from storage and creates journal tiles.
     * <p>
     * This method:
     * <ul>
     *     <li>Retrieves all saved journals from preferences</li>
     *     <li>Creates journal tiles for each valid journal entry</li>
     *     <li>Updates the journal counter based on existing journals</li>
     *     <li>Handles cleanup of invalid journal entries</li>
     *     <li>Publishes events to create journal tiles in the UI</li>
     * </ul>
     */
    private void loadPreferencesForLandingPage() {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences journalsPreferences = appPreferences.node(JOURNALS);
        List<String> journalDirNames = journalsPreferences.getList(JOURNAL_IDS);
        List<String> journalsToRemove = new ArrayList<>();
        for (String journalDirName : journalDirNames) {
            KometPreferences journalSubWindowPreferences = journalsPreferences.node(journalDirName);
            Optional<UUID> journalTopicOptional = journalSubWindowPreferences.getUuid(JOURNAL_TOPIC);
            if (journalTopicOptional.isEmpty()) {
                journalsToRemove.add(journalDirName);
                continue;
            }

            Optional<String> journalTitleOptional = journalSubWindowPreferences.get(JOURNAL_TITLE);

            Double xpos = journalSubWindowPreferences.getDouble(JOURNAL_XPOS, DEFAULT_JOURNAL_XPOS);
            Double ypos = journalSubWindowPreferences.getDouble(JOURNAL_YPOS, DEFAULT_JOURNAL_YPOS);
            Double width = journalSubWindowPreferences.getDouble(JOURNAL_WIDTH, DEFAULT_JOURNAL_WIDTH);
            Double height = journalSubWindowPreferences.getDouble(JOURNAL_HEIGHT, DEFAULT_JOURNAL_HEIGHT);
            String journalAuthor = journalSubWindowPreferences.get(JOURNAL_AUTHOR, "");
            OptionalLong journalLastEditOpt = journalSubWindowPreferences.getLong(JOURNAL_LAST_EDIT);
            List<String> windowNames = journalSubWindowPreferences.getList(
                    journalSubWindowPreferences.enumToGeneralKey(WINDOW_NAMES));

            PrefX prefX = PrefX.create()
                    .setValue(JOURNAL_DIR_NAME, journalDirName)
                    .setValue(JOURNAL_TOPIC, journalTopicOptional.get())
                    .setValue(JOURNAL_TITLE, journalTitleOptional.get())
                    .setValue(JOURNAL_HEIGHT, height)
                    .setValue(JOURNAL_WIDTH, width)
                    .setValue(JOURNAL_XPOS, xpos)
                    .setValue(JOURNAL_YPOS, ypos)
                    .setValue(WINDOW_NAMES, windowNames)
                    .setValue(JOURNAL_AUTHOR, journalAuthor)
                    .setValue(JOURNAL_LAST_EDIT, journalLastEditOpt.isPresent() ?
                            journalLastEditOpt.getAsLong() : null);

            // keep track of latest journal number when reloading from preferences
            JournalCounter.getInstance().set(parseJournalNumber(journalTitleOptional.get()));
            landingPageEventBus.publish(JOURNAL_TOPIC,
                    new JournalTileEvent(newProjectJournalButton,
                            CREATE_JOURNAL_TILE, prefX));
        }
        journalDirNames.removeAll(journalsToRemove);
        journalsPreferences.putList(JOURNAL_IDS, journalDirNames);
        try {
            journalsPreferences.flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static int parseJournalNumber(String journalName) {
        Pattern pattern = Pattern.compile("\\d+$");
        Matcher matcher = pattern.matcher(journalName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return -1; // invalid
    }

    @Override
    public void updateView() {

    }

    @Override
    public void clearView() {
        gridViewFlowPane.getChildren().removeIf(node -> node.getId() == null);
    }

    @Override
    public void cleanup() {
        landingPageEventBus.unsubscribe(JOURNAL_TOPIC, JournalTileEvent.class, createJournalTileSubscriber);
        landingPageEventBus.unsubscribe(JOURNAL_TOPIC, DeleteJournalEvent.class, deleteJournalSubscriber);
    }

    /**
     * Creates a new journal view from the card.
     * <p>
     * This method is triggered when the user clicks on the "Create New Journal" button.
     * It:
     * <ul>
     *     <li>Creates a new UUID for the journal</li>
     *     <li>Generates a new journal name with an incremented counter</li>
     *     <li>Creates a preferences map for the new journal</li>
     *     <li>Publishes events to create a journal tile and a journal view</li>
     * </ul>
     *
     * @param event The event that triggered this method
     */
    @FXML
    void createNewJournalViewFromCard(Event event) {
        // publish the event that the new journal button was pressed
        final PrefX journalWindowSettingsObjectMap = PrefX.create();
        final UUID journalTopic = UUID.randomUUID();
        final String journalName = JOURNAL_NAME_PREFIX + JournalCounter.getInstance().incrementAndGet();
        final String journalDirName = getJournalDirName(journalTopic);
        journalWindowSettingsObjectMap.setValue(JOURNAL_TOPIC, journalTopic);
        journalWindowSettingsObjectMap.setValue(JOURNAL_TITLE, journalName);
        journalWindowSettingsObjectMap.setValue(JOURNAL_DIR_NAME, journalDirName);

        // publish an event to create the tile on the landing page
        landingPageEventBus.publish(JOURNAL_TOPIC,
                new JournalTileEvent(newProjectJournalButton, CREATE_JOURNAL_TILE, journalWindowSettingsObjectMap));

        landingPageEventBus.publish(JOURNAL_TOPIC,
                new CreateJournalEvent(this, CREATE_JOURNAL, journalWindowSettingsObjectMap));

        LOG.info("CARD LAUNCHED");
    }

    public BorderPane getRoot() {
        return landingPageBorderPane;
    }

    public Label getWelcomeTitleLabel() {
        return welcomeTitleLabel;
    }

    public Hyperlink getGithubStatusHyperlink() {
        return githubStatusHyperlink;
    }

    public void setSelectedDatasetTitle(String value) {
        selectedDatasetTitleLabel.setText(value);
    }

    /**
     * Handles the import button press to pop up the import dialog window
     *
     * @param event
     */
    @FXML
    private void openImport(ActionEvent event) {
        EvtBusFactory.getDefaultEvtBus().publish(IMPORT_TOPIC, new Evt(LANDING_PAGE_SOURCE, Evt.ANY));
    }
}
