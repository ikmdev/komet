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

import dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils;
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

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.ikm.komet.kview.klwindows.KlWindowPreferencesUtils.shortenUUID;
import static dev.ikm.komet.kview.mvvm.model.Constants.JOURNAL_NAME_PREFIX;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.CreateJournalEvent.CREATE_JOURNAL;
import static dev.ikm.komet.kview.events.JournalTileEvent.CREATE_JOURNAL_TILE;
import static dev.ikm.komet.framework.controls.TimeAgoCalculatorUtil.calculateTimeAgoWithPeriodAndDuration;
import static dev.ikm.komet.preferences.JournalWindowPreferences.*;
import static dev.ikm.komet.preferences.JournalWindowSettings.*;

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

    private final Map<String, JournalCardController> journalCardControllerMap = new HashMap<>();

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
                journalWindowSettingsObjectMap.setValue(JOURNAL_TITLE, journalName);
                journalWindowSettingsObjectMap.setValue(JOURNAL_TOPIC, journalTopic);
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
                    prefX = loadJournalWindowPreference(journalSettingsFinal.getValue(JOURNAL_DIR_NAME));
                } else {
                    // newly added card to landing page.
                    prefX = journalSettingsFinal;
                }
                // fire create journal event... AND this should be the ONLY place it comes from besides the menu
                kViewEventBus.publish(JOURNAL_TOPIC, new CreateJournalEvent(this, CREATE_JOURNAL, prefX));
            });
            journalCardControllerMap.put(journalTopic.toString(), journalCardController);
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
                    // remove preferences.
                    KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
                    KometPreferences journalPreferences = appPreferences.node(JOURNALS);
                    List<String> journalNames = journalPreferences.getList(JOURNAL_IDS);

                    String journalSubWindowPrefFolder = JOURNAL_FOLDER_PREFIX
                            + shortenUUID(prefX.getValue(JOURNAL_TOPIC));
                    journalNames.remove(journalSubWindowPrefFolder);

                    KometPreferences journalSubWindowPreferences = appPreferences.node(JOURNALS +
                            File.separator + journalSubWindowPrefFolder);
                    try {
                        journalPreferences.putList(JOURNAL_IDS, journalNames);
                        journalSubWindowPreferences.flush();
                        journalSubWindowPreferences.removeNode();
                        journalPreferences.sync();
                        // TODO Remove all concept folders.
                    } catch (BackingStoreException e) {
                        throw new RuntimeException(e);
                    } finally {
                        journalCardControllerMap.remove(prefX.getValue(JOURNAL_TOPIC).toString()).cleanup();
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

    private PrefX loadJournalWindowPreference(String journalSubWindowPrefFolder) {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences journalSubWindowPreferences = appPreferences.node(JOURNALS +
                File.separator + journalSubWindowPrefFolder);

        UUID journalTopic = journalSubWindowPreferences.getUuid(JOURNAL_TOPIC).orElse(null);
        String journalTitle = journalSubWindowPreferences.get(JOURNAL_TITLE).orElse("Journal %s".formatted(JournalCounter.getInstance().get()));

        Double height = journalSubWindowPreferences.getDouble(
                journalSubWindowPreferences.enumToGeneralKey(JOURNAL_HEIGHT), DEFAULT_JOURNAL_HEIGHT);
        Double width = journalSubWindowPreferences.getDouble(
                journalSubWindowPreferences.enumToGeneralKey(JOURNAL_WIDTH), DEFAULT_JOURNAL_WIDTH);
        Double xpos = journalSubWindowPreferences.getDouble(
                journalSubWindowPreferences.enumToGeneralKey(JOURNAL_XPOS), DEFAULT_JOURNAL_XPOS);
        Double ypos = journalSubWindowPreferences.getDouble(
                journalSubWindowPreferences.enumToGeneralKey(JOURNAL_YPOS), DEFAULT_JOURNAL_YPOS);
        String journalAuthor = journalSubWindowPreferences.get(
                journalSubWindowPreferences.enumToGeneralKey(JOURNAL_AUTHOR), "");
        OptionalLong journalLastEditOpt = journalSubWindowPreferences.getLong(
                journalSubWindowPreferences.enumToGeneralKey(JOURNAL_LAST_EDIT));
        List<String> windowList = journalSubWindowPreferences.getList(
                journalSubWindowPreferences.enumToGeneralKey(WINDOW_NAMES));

        return PrefX.create()
                .setValue(JOURNAL_DIR_NAME, journalSubWindowPrefFolder)
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

    private void loadPreferencesForLandingPage() {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences journalPreferences = appPreferences.node(JOURNALS);
        List<String> journalNames = journalPreferences.getList(JOURNAL_IDS);
        List<String> namesToRemove = new ArrayList<>();
        for (String journalSubWindowPrefFolder : journalNames) {
            KometPreferences journalSubWindowPreferences = appPreferences.node(JOURNALS +
                    File.separator + journalSubWindowPrefFolder);
            Optional<UUID> journalTopicOptional = journalSubWindowPreferences.getUuid(JOURNAL_TOPIC);
            if (journalTopicOptional.isEmpty()) {
                namesToRemove.add(journalSubWindowPrefFolder);
                continue;
            }

            Optional<String> journalTitleOptional = journalSubWindowPreferences.get(JOURNAL_TITLE);
            if (journalTitleOptional.isEmpty()) {
                namesToRemove.add(journalSubWindowPrefFolder);
                continue;
            }

            Double height = journalSubWindowPreferences.getDouble(
                    journalSubWindowPreferences.enumToGeneralKey(JOURNAL_HEIGHT), DEFAULT_JOURNAL_HEIGHT);
            Double width = journalSubWindowPreferences.getDouble(
                    journalSubWindowPreferences.enumToGeneralKey(JOURNAL_WIDTH), DEFAULT_JOURNAL_WIDTH);
            Double xpos = journalSubWindowPreferences.getDouble(
                    journalSubWindowPreferences.enumToGeneralKey(JOURNAL_XPOS), DEFAULT_JOURNAL_XPOS);
            Double ypos = journalSubWindowPreferences.getDouble(
                    journalSubWindowPreferences.enumToGeneralKey(JOURNAL_YPOS), DEFAULT_JOURNAL_YPOS);
            String journalAuthor = journalSubWindowPreferences.get(
                    journalSubWindowPreferences.enumToGeneralKey(JOURNAL_AUTHOR), "");
            OptionalLong journalLastEditOpt = journalSubWindowPreferences.getLong(
                    journalSubWindowPreferences.enumToGeneralKey(JOURNAL_LAST_EDIT));
            List<String> conceptList = journalSubWindowPreferences.getList(journalSubWindowPreferences.enumToGeneralKey(WINDOW_NAMES));

            PrefX prefX = PrefX.create()
                .setValue(JOURNAL_DIR_NAME, journalSubWindowPrefFolder)
                .setValue(JOURNAL_TOPIC, journalTopicOptional.get())
                .setValue(JOURNAL_TITLE, journalTitleOptional.get())
                .setValue(JOURNAL_HEIGHT, height)
                .setValue(JOURNAL_WIDTH, width)
                .setValue(JOURNAL_XPOS, xpos)
                .setValue(JOURNAL_YPOS, ypos)
                .setValue(WINDOW_NAMES, conceptList)
                .setValue(JOURNAL_AUTHOR, journalAuthor)
                .setValue(JOURNAL_LAST_EDIT, journalLastEditOpt.isPresent() ?
                    journalLastEditOpt.getAsLong() : null);

            // keep track of latest journal number when reloading from preferences
            JournalCounter.getInstance().set(parseJournalNumber(journalTitleOptional.get()));
            kViewEventBus.publish(JOURNAL_TOPIC,
                    new JournalTileEvent(newProjectJournalButton,
                            CREATE_JOURNAL_TILE, prefX));
        }
        journalNames.removeAll(namesToRemove);
        journalPreferences.putList(JOURNAL_IDS, journalNames);
        try {
            journalPreferences.flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static int parseJournalNumber(String journalName) {
        Pattern pattern = Pattern.compile("\\d+$");
        Matcher matcher = pattern.matcher(journalName);
        while (matcher.find()) {

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

    @FXML
    void createNewJournalViewFromCard(Event event) {
        // publish the event that the new journal button was pressed
        PrefX journalWindowSettingsObjectMap = PrefX.create();
        UUID journalTopic = UUID.randomUUID();
        String journalName = JOURNAL_NAME_PREFIX + JournalCounter.getInstance().incrementAndGet();
        String journalDirName = JOURNAL_FOLDER_PREFIX + shortenUUID(journalTopic);
        journalWindowSettingsObjectMap.setValue(JOURNAL_TOPIC, journalTopic);
        journalWindowSettingsObjectMap.setValue(JOURNAL_TITLE, journalName);
        journalWindowSettingsObjectMap.setValue(JOURNAL_DIR_NAME, journalDirName);

        // publish an event to create the tile on the landing page
        kViewEventBus.publish(JOURNAL_TOPIC,
                new JournalTileEvent(newProjectJournalButton, CREATE_JOURNAL_TILE, journalWindowSettingsObjectMap));

        // and also publish an event to create the journal window itself
        PrefX journalWindowSettingsMap = PrefX.create();
        journalWindowSettingsMap.setValue(JOURNAL_TOPIC, journalTopic);
        journalWindowSettingsMap.setValue(JOURNAL_TITLE, journalName);
        journalWindowSettingsMap.setValue(JOURNAL_DIR_NAME, journalDirName);

        kViewEventBus.publish(JOURNAL_TOPIC,
                new CreateJournalEvent(this, CREATE_JOURNAL, journalWindowSettingsMap));

        LOG.info("CARD LAUNCHED");
    }

    public Label getWelcomeTitleLabel() {
        return welcomeTitleLabel;
    }
}
