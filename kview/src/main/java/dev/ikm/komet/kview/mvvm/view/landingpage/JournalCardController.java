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

import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.kview.fxutils.MenuHelper;
import dev.ikm.komet.kview.events.CreateJournalEvent;
import dev.ikm.komet.kview.events.DeleteJournalEvent;
import dev.ikm.komet.kview.events.JournalTileEvent;
import dev.ikm.komet.kview.mvvm.model.JournalNames;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.komet.framework.preferences.PrefX;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
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
    /** The in-place name editor while a rename is in progress; {@code null} otherwise. */
    private TextField nameEditor;
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

            // Match on the journal's topic, its identity — never on its name, which is display text
            // and need not be unique (ike-issues#1127).
            final UUID eventJournalTopic = journalWindowSettingsMap.getValue(JOURNAL_TOPIC);
            // Process UPDATE_JOURNAL_TILE event type only.
            if (evt.getEventType() != UPDATE_JOURNAL_TILE || !Objects.equals(journalTopic, eventJournalTopic)) return;

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
        journalCardName.textProperty().unbind();
    }

    /**
     * Starts renaming the journal in place: the card's name is swapped for a text field holding it.
     * Enter or moving focus away commits; Escape cancels. A blank name is rejected and the journal
     * keeps its previous name (ike-issues#1128).
     */
    private void beginRename() {
        if (nameEditor != null || journalTopic == null
                || !(journalCardName.getParent() instanceof Pane parent)) {
            return;
        }
        TextField editor = new TextField(journalCardName.getText());
        editor.getStyleClass().add("journal-card-name-editor");
        editor.setFont(journalCardName.getFont());
        // The card opens its journal when clicked; clicks while editing the name must not reach it.
        editor.addEventHandler(MouseEvent.MOUSE_CLICKED, Event::consume);
        editor.setOnAction(actionEvent -> endRename(true));
        editor.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                keyEvent.consume();
                endRename(false);
            }
        });
        editor.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                endRename(true);
            }
        });
        nameEditor = editor;
        parent.getChildren().set(parent.getChildren().indexOf(journalCardName), editor);
        // After the card's menu has closed, so the menu does not take the focus back.
        Platform.runLater(() -> {
            editor.requestFocus();
            editor.selectAll();
        });
    }

    /**
     * Ends an in-place rename, storing the edited name when committing, and puts the card's name back.
     *
     * @param commit {@code true} to rename the journal to the edited text, {@code false} to cancel
     */
    private void endRename(boolean commit) {
        TextField editor = nameEditor;
        if (editor == null) {
            return;
        }
        // Cleared first: removing the focused editor below moves focus, which re-enters here.
        nameEditor = null;
        if (commit) {
            JournalNames.get().rename(journalTopic, editor.getText());
        }
        if (editor.getParent() instanceof Pane parent) {
            parent.getChildren().set(parent.getChildren().indexOf(editor), journalCardName);
        }
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
                { "Rename", true, (EventHandler<ActionEvent>) actionEvent -> beginRename() },
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

    /**
     * Sets the journal this card stands for, and shows that journal's live name, so a rename made
     * from the card or from the journal's own header appears here at once (ike-issues#1128).
     *
     * @param journalTopic the journal's topic, its identity
     */
    public void setJournalTopic(UUID journalTopic) {
        this.journalTopic = journalTopic;
        journalCardName.textProperty().bind(JournalNames.get().nameProperty(journalTopic));
    }

    public void setJournalTimestampValue(String journalTimestampValue) {
        this.journalTimestampValue.setText(journalTimestampValue);
    }

    public void setJournalCardWindowCount(String journalCardWindowCount) {
        this.journalCardWindowCount.setText(journalCardWindowCount);
    }
}
