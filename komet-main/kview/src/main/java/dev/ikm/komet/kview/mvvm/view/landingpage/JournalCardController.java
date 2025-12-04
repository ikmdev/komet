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

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.CreateJournalEvent.CREATE_JOURNAL;
import static dev.ikm.komet.kview.events.DeleteJournalEvent.DELETE_JOURNAL;
import static dev.ikm.komet.kview.events.JournalTileEvent.UPDATE_JOURNAL_TILE;
import static dev.ikm.komet.preferences.JournalWindowSettings.WINDOW_COUNT;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_TITLE;

import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.kview.fxutils.MenuHelper;
import dev.ikm.komet.kview.events.CreateJournalEvent;
import dev.ikm.komet.kview.events.DeleteJournalEvent;
import dev.ikm.komet.kview.events.JournalTileEvent;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.komet.framework.preferences.PrefX;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static dev.ikm.komet.preferences.JournalWindowSettings.*;

public class JournalCardController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(JournalCardController.class);

    @FXML
    Pane cardPane;

    @FXML
    Text journalCardName;

    @FXML
    Label journalTimestampValue;

    @FXML
    Text journalCardWindowCount;

    @FXML
    Button menuOptionButton;

    private UUID journalTopic;
    private EvtBus journalEventBus;
    private Subscriber<JournalTileEvent> updateCard;
    private final ContextMenu contextMenu = buildMenuOptionContextMenu();

    @FXML
    public void initialize() {
        journalEventBus = EvtBusFactory.getDefaultEvtBus();
        LOG.debug("Event bus instance %s, %s".formatted(this.getClass().getSimpleName(), journalEventBus));

        // Add a context menu to the menu options button on the card
        setupContextMenuOptions(menuOptionButton);

        // Listen for update card or journal tile event .
        updateCard = evt -> {
            final PrefX journalWindowSettingsMap = evt.getJournalWindowSettingsMap();

            // grab the name of the journal
            final String journalName = journalWindowSettingsMap.getValue(JOURNAL_TITLE);
            // Process UPDATE_JOURNAL_TILE event type only.
            if (evt.getEventType() != UPDATE_JOURNAL_TILE || !journalCardName.getText().equals(journalName)) return;

            // Update the card's info
            if (journalWindowSettingsMap.getValue(WINDOW_COUNT) != null) {
                journalCardWindowCount.setText("Windows: " + journalWindowSettingsMap.getValue(WINDOW_COUNT));
            }

            // Update the card's menu option user can delete
            if (journalWindowSettingsMap.getValue(CAN_DELETE) != null) {
                Boolean canDelete = journalWindowSettingsMap.getValue(CAN_DELETE);
                if (canDelete == null) {
                    canDelete = true;
                }
                // update menu item
                disableMenuItem("Delete", !canDelete); // not can delete means disable
            }
        };
        journalEventBus.subscribe(JOURNAL_TOPIC, JournalTileEvent.class, updateCard);
    }

    @Override
    public void updateView() {

    }

    @Override
    public void clearView() {

    }

    @Override
    public void cleanup() {
        journalEventBus.unsubscribe(JOURNAL_TOPIC, JournalTileEvent.class, updateCard);
    }

    private void setupContextMenuOptions(Button menuOptionButton) {
        menuOptionButton.setOnAction(actionEvent -> {
            contextMenu.setHideOnEscape(true);

            Bounds currentBounds = menuOptionButton.getLayoutBounds();
            Bounds boundsInScene = menuOptionButton.localToScreen(currentBounds);

            LOG.debug("Bounds (in getLayoutBounds) = " + currentBounds);
            LOG.debug("Bounds (in Scene) = " + boundsInScene);

            // Show context menu under button (based on Scene)
            double x = boundsInScene.getMinX();
            double y = boundsInScene.getMaxY() + menuOptionButton.getInsets().getTop() + menuOptionButton.getInsets().getBottom();
            LOG.debug("double y = boundsInScene.getMaxY() +  menuOptionButton.getInsets().getTop() + menuOptionButton.getInsets().getBottom(); \n" +
                    "%s = %s + %s + %s".formatted(y,
                            boundsInScene.getMaxY(),
                            menuOptionButton.getInsets().getTop(),
                            menuOptionButton.getInsets().getBottom()));

            LOG.debug("actionEvent = " + actionEvent);
            contextMenu.show(menuOptionButton.getScene().getWindow(), x, y);
        });
    }


    /**
     * Enable or disable a menu item by name.
     * @param name
     * @param disable
     */
    private void disableMenuItem(String name, boolean disable) {
        // scan through context menu (it's flat so it's easy) and find menu item to enable or disable.
        contextMenu.getItems()
                .stream()
                .filter(menuItem ->
                        String.valueOf(menuItem.getText())
                              .toLowerCase()
                              .trim()
                              .equals(name.toLowerCase().trim()))
                .findFirst()
                .ifPresent(menuItem -> menuItem.setDisable(disable));
    }
    /**
     * Builds the context menu for a card's menu option button (hover - upper right)
     * @return a ContextMenu
     */
    private ContextMenu buildMenuOptionContextMenu() {
        MenuHelper menuHelper = MenuHelper.getInstance();
        // name, state, style class
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("kview-context-menu");

        // If there are any specialized styling to be added
        final String[] styleClass = new String[]{};
        final int NAME = 0;
        final int ENABLED = 1;
        final int ACTION = 2;
        Object[][] menuItems = new Object[][] {
                { "Open", false, null},
                { "Open in new window", false,  (EventHandler<ActionEvent>) actionEvent ->
                        journalEventBus.publish(JOURNAL_TOPIC, new CreateJournalEvent(this, CREATE_JOURNAL, (PrefX) cardPane.getUserData()))
                },
                { MenuHelper.SEPARATOR },
                { "Star selection", false, null},
                { "Copy link", false, null},
                { "Share", false, null},
                { "Duplicate", false, null},
                { MenuHelper.SEPARATOR },
                { "Rename", false, null},
                { "Move file...", false, null},
                { MenuHelper.SEPARATOR },
                { "Delete", true,  (EventHandler<ActionEvent>) actionEvent ->
                        journalEventBus.publish(JOURNAL_TOPIC,
                                new DeleteJournalEvent(this, DELETE_JOURNAL, journalTopic))
                },
        };
        for (Object[] menuItemObj : menuItems) {
            if (MenuHelper.SEPARATOR.equals(menuItemObj[NAME])){
                contextMenu.getItems().add(new SeparatorMenuItem());
                continue;
            }

            // uses a default action if one is not given.
            EventHandler<ActionEvent> menuItemAction = switch (menuItemObj[ACTION]) {
                case null ->  actionEvent -> LOG.info(menuItemObj[NAME] + " " + journalCardName.getText());
                case EventHandler  eventHandler -> eventHandler;
                default -> null;
            };

            // Create a menu item. Todo: if/when you have sub menus
            MenuItem menuItem = menuHelper.createMenuOption(
                    String.valueOf(menuItemObj[NAME]),                           /* name */
                    Boolean.parseBoolean(String.valueOf(menuItemObj[ENABLED])),  /* enabled */
                    styleClass,                                                  /* styling */
                    menuItemAction,                                              /* action when selected */
                    null                                                         /* optional graphic */
            );

            contextMenu.getItems().add(menuItem);
        }

        return contextMenu;
    }

    public void setJournalTopic(UUID journalTopic) {
        this.journalTopic = journalTopic;
    }

    public void setJournalCardName(String journalCardName) {
        this.journalCardName.setText(journalCardName);
    }

    public void setJournalTimestampValue(String journalTimestampValue) {
        this.journalTimestampValue.setText(journalTimestampValue);
    }

    public void setJournalCardWindowCount(String journalCardWindowCount) {
        this.journalCardWindowCount.setText(journalCardWindowCount);
    }
}
