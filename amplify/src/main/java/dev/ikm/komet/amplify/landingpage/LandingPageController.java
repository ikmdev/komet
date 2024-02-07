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
package dev.ikm.komet.amplify.landingpage;

import static dev.ikm.komet.amplify.commons.Constants.JOURNAL_NAME_PREFIX;
import static dev.ikm.komet.amplify.events.AmplifyTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.framework.controls.TimeAgoCalculatorUtil.calculateTimeAgoWithPeriodAndDuration;
import static dev.ikm.komet.preferences.JournalWindowPreferences.*;
import static dev.ikm.komet.preferences.JournalWindowSettings.*;

import dev.ikm.komet.amplify.commons.BasicController;
import dev.ikm.komet.amplify.commons.JournalCounter;
import dev.ikm.komet.amplify.events.CreateJournalEvent;
import dev.ikm.komet.amplify.events.CreateJournalTileEvent;
import dev.ikm.komet.amplify.events.DeleteJournalEvent;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LandingPageController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(LandingPageController.class);

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

    public static final String DEMO_AUTHOR = "David";
    private EvtBus amplifyEventBus;

    private Subscriber createJournalTileSubscriber;

    private Subscriber deleteJournalSubscriber;

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
        // get the instance of the event bus
        amplifyEventBus = EvtBusFactory.getInstance(EvtBus.class);
        createJournalTileSubscriber = evt -> {
            if (evt instanceof CreateJournalTileEvent createEvt) {
                final String journalName;

                //Creating a new journal card
                FXMLLoader amplifyJournalCard = new FXMLLoader(LandingPageController.class.getResource("amplify-journal-card.fxml"));
                Pane journalCard = null;
                try {
                    journalCard = amplifyJournalCard.load();
                } catch (IOException e) {
                    throw new RuntimeException("unable to load journal card fxml ", e);
                }
                JournalCardController journalCardController = amplifyJournalCard.getController();
                PrefX journalWindowSettingsObjectMap = createEvt.getJournalWindowSettingsMap();
                if (null != journalWindowSettingsObjectMap) {
                    journalName = journalWindowSettingsObjectMap.getValue(JOURNAL_TITLE);
                    LocalDateTime nowDateTime = LocalDateTime.now();
                    ZoneId nowZoneId = ZoneId.systemDefault();
                    String calculatedTimeAgo = calculateTimeAgoWithPeriodAndDuration(nowDateTime, nowZoneId);
                    journalCardController.setJournalTimestampValue(calculatedTimeAgo);
                    if(journalWindowSettingsObjectMap.getValue(CONCEPT_NAMES) != null) {
                        journalCardController.setJournalCardConceptCount("Concepts: "
                                + ((List<String>) journalWindowSettingsObjectMap.getValue(CONCEPT_NAMES)).size());
                    }
                    else {
                        journalCardController.setJournalCardConceptCount("Concepts: 0");
                    }
                } else {
                    journalName = "Journal " + JournalCounter.getInstance().intValue();
                    journalWindowSettingsObjectMap = PrefX.create();
                    journalWindowSettingsObjectMap.setValue(JOURNAL_TITLE, journalName);
                    journalCardController.setJournalTimestampValue("Edited Now");

                }
                journalCardController.setJournalCardName(journalName);

                final PrefX journalSettingsFinal = journalWindowSettingsObjectMap;
                // get the correct Journal X name where X is a counting number 1...n
                // decide if the name comes from the event or not
                journalCard.setOnMouseClicked(event -> {
                    amplifyEventBus.publish(JOURNAL_TOPIC,
                            new CreateJournalEvent(this,
                                    CreateJournalEvent.CREATE_JOURNAL, journalSettingsFinal));
                    // fire create journal event... AND this should be the ONLY place it comes from besides the menu
                });


                journalCard.setUserData(journalSettingsFinal);
                gridViewFlowPane.getChildren().addFirst(journalCard);
            }
        };
        amplifyEventBus.subscribe(JOURNAL_TOPIC, createJournalTileSubscriber);


        deleteJournalSubscriber = evt -> {
            if (evt instanceof DeleteJournalEvent delEvt) {
                // remove the tile by finding its journal name
                gridViewFlowPane.getChildren().removeIf(node ->
                        node.getUserData() != null && ((PrefX) node.getUserData()).getValue(JOURNAL_TITLE) != null
                                && ((PrefX) node.getUserData()).getValue(JOURNAL_TITLE).equals(delEvt.getJournalName())
                );
            }
        };
        amplifyEventBus.subscribe(JOURNAL_TOPIC, deleteJournalSubscriber);

        journalProjectCardScrollPane.viewportBoundsProperty().addListener((ov, oldBounds, bounds) -> {
            gridViewFlowPane.setPrefWidth(bounds.getWidth());
            gridViewFlowPane.setPrefHeight(bounds.getHeight());
        });

        newProjectJournalButton.setOnAction(event ->  {
            JournalCounter.getInstance().getAndIncrement();
            // publish the event that the new journal button was pressed
            PrefX journalWindowSettingsObjectMap = PrefX.create();
            String journalName = JOURNAL_NAME_PREFIX + JournalCounter.getInstance().intValue();
            journalWindowSettingsObjectMap.setValue(JOURNAL_TITLE, journalName);

            // publish an event to create the tile on the landing page
            amplifyEventBus.publish(JOURNAL_TOPIC,
                    new CreateJournalTileEvent(newProjectJournalButton,
                            CreateJournalTileEvent.CREATE_JOURNAL_TILE, journalWindowSettingsObjectMap));

            // and also publish an event to create the journal window itself
            PrefX journalWindowSettingsMap = PrefX.create();
            journalWindowSettingsMap.setValue(JOURNAL_TITLE, journalName);
            amplifyEventBus.publish(JOURNAL_TOPIC,
                    new CreateJournalEvent(this, CreateJournalEvent.CREATE_JOURNAL,
                            journalWindowSettingsMap));
        });
        loadPreferencesForLandingPage();
    }

    private void loadPreferencesForLandingPage() {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences journalPreferences = appPreferences.node(JOURNAL_WINDOW);
        for (String journalSubWindowPrefFolder : journalPreferences.getList(JOURNAL_NAMES)) {
            KometPreferences journalSubWindowPreferences = appPreferences.node(JOURNAL_WINDOW +
                    File.separator + journalSubWindowPrefFolder);
            Optional<String> journalTitleOptional = journalSubWindowPreferences.get(JOURNAL_TITLE);

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
            List<String> conceptList = journalSubWindowPreferences.getList(journalSubWindowPreferences.enumToGeneralKey(CONCEPT_NAMES));

            PrefX prefX = PrefX.create()
                .setValue(JOURNAL_TITLE, journalTitleOptional.get())
                .setValue(JOURNAL_HEIGHT, height)
                .setValue(JOURNAL_WIDTH, width)
                .setValue(JOURNAL_XPOS, xpos)
                .setValue(JOURNAL_YPOS, ypos)
                .setValue(CONCEPT_NAMES, conceptList)
                .setValue(JOURNAL_AUTHOR, journalAuthor)
                .setValue(JOURNAL_LAST_EDIT, journalLastEditOpt.isPresent() ?
                    journalLastEditOpt.getAsLong() : null);

            // keep track of latest journal number when reloading from preferences
            JournalCounter.getInstance().set(parseJournalNumber(journalTitleOptional.get()));
            amplifyEventBus.publish(JOURNAL_TOPIC,
                    new CreateJournalTileEvent(newProjectJournalButton,
                            CreateJournalTileEvent.CREATE_JOURNAL_TILE, prefX));

        }
    }

    private int parseJournalNumber(String journalName) {
        return Integer.parseInt(journalName.split(" ")[1]);
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
        amplifyEventBus.unsubscribe(JOURNAL_TOPIC, createJournalTileSubscriber);
        amplifyEventBus.unsubscribe(JOURNAL_TOPIC, deleteJournalSubscriber);
    }

    @FXML
    void createNewJournalViewFromCard() {
        JournalCounter.getInstance().getAndIncrement();
        amplifyEventBus.publish(JOURNAL_TOPIC,
                new CreateJournalTileEvent(this,
                        CreateJournalTileEvent.CREATE_JOURNAL_TILE, null));
        LOG.info("CARD LAUNCHED");
    }
}
