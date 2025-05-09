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

import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.kview.mvvm.model.JournalCounter;
import dev.ikm.komet.kview.events.CreateJournalEvent;
import dev.ikm.komet.kview.events.DeleteJournalEvent;
import dev.ikm.komet.kview.events.JournalTileEvent;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.getJournalDirName;
import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.getJournalPreferences;
import static dev.ikm.komet.kview.mvvm.model.Constants.JOURNAL_NAME_PREFIX;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.CreateJournalEvent.CREATE_JOURNAL;
import static dev.ikm.komet.kview.events.JournalTileEvent.CREATE_JOURNAL_TILE;
import static dev.ikm.komet.framework.controls.TimeAgoCalculatorUtil.calculateTimeAgoWithPeriodAndDuration;
import static dev.ikm.komet.preferences.JournalWindowPreferences.*;
import static dev.ikm.komet.preferences.JournalWindowSettings.*;

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
    private ScrollPane journalProjectCardScrollPane;

    @FXML
    private FlowPane gridViewFlowPane;

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
    private EvtBus kViewEventBus;

    private Subscriber<JournalTileEvent> createJournalTileSubscriber;

    private Subscriber<DeleteJournalEvent> deleteJournalSubscriber;

    private final Map<UUID, JournalCardController> journalCardControllerMap = new HashMap<>();

    public ToggleButton getSettingsToggleButton() {
        return settingsToggleButton;
    }

    public void setSettingsToggleButton(ToggleButton settingsToggleButton) {
        this.settingsToggleButton = settingsToggleButton;
    }

    @FXML
    @Override
    public void initialize() {
        clearView();

        notificationTypeFilterComboBox.getItems().addAll("All types");
        notificationTypeFilterComboBox.getSelectionModel().selectFirst();

        // get the instance of the event bus
        kViewEventBus = EvtBusFactory.getDefaultEvtBus();
        LOG.debug("Event bus instance %s, %s".formatted(this.getClass().getSimpleName(), kViewEventBus));
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
                kViewEventBus.publish(JOURNAL_TOPIC, new CreateJournalEvent(this, CREATE_JOURNAL, prefX));
            });
            journalCardControllerMap.put(journalTopic, journalCardController);
            journalCard.setUserData(journalSettingsFinal);
            gridViewFlowPane.getChildren().addFirst(journalCard);
        };

        kViewEventBus.subscribe(JOURNAL_TOPIC, JournalTileEvent.class, createJournalTileSubscriber);

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
        kViewEventBus.subscribe(JOURNAL_TOPIC, DeleteJournalEvent.class, deleteJournalSubscriber);

        journalProjectCardScrollPane.viewportBoundsProperty().addListener((ov, oldBounds, bounds) -> {
            gridViewFlowPane.setPrefWidth(bounds.getWidth());
            gridViewFlowPane.setPrefHeight(bounds.getHeight());
        });

        loadPreferencesForLandingPage();
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
            kViewEventBus.publish(JOURNAL_TOPIC,
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
        kViewEventBus.unsubscribe(JOURNAL_TOPIC, JournalTileEvent.class, createJournalTileSubscriber);
        kViewEventBus.unsubscribe(JOURNAL_TOPIC, DeleteJournalEvent.class, deleteJournalSubscriber);
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
        kViewEventBus.publish(JOURNAL_TOPIC,
                new JournalTileEvent(newProjectJournalButton, CREATE_JOURNAL_TILE, journalWindowSettingsObjectMap));

        kViewEventBus.publish(JOURNAL_TOPIC,
                new CreateJournalEvent(this, CREATE_JOURNAL, journalWindowSettingsObjectMap));

        LOG.info("CARD LAUNCHED");
    }

    public Label getWelcomeTitleLabel() {
        return welcomeTitleLabel;
    }
}
