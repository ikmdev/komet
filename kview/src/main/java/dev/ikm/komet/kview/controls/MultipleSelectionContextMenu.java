package dev.ikm.komet.kview.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;

import java.util.ResourceBundle;

public class MultipleSelectionContextMenu extends ContextMenu {

    private final MenuItem populateMessage;
    private final MenuItem journalMessage;
    private final MenuItem chapterMessage;
    private final MenuItem copyMessage;
    private final MenuItem saveMessage;

    public MultipleSelectionContextMenu() {
        setAutoHide(true);
        ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.concept-navigator");

        populateMessage = new MenuItem(resources.getString("multi.selection.context.menu.option.populate"), new IconRegion("icon", "populate"));
        journalMessage = new MenuItem(resources.getString("multi.selection.context.menu.option.journal"), new IconRegion("icon", "send"));
        chapterMessage = new MenuItem(resources.getString("multi.selection.context.menu.option.chapter"), new IconRegion("icon", "send"));
        copyMessage = new MenuItem(resources.getString("multi.selection.context.menu.option.copy"), new IconRegion("icon", "duplicate"));
        saveMessage = new MenuItem(resources.getString("multi.selection.context.menu.option.save"), new IconRegion("icon", "save"));

        getItems().addAll(populateMessage, journalMessage, chapterMessage, copyMessage, saveMessage);
        setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
    }

    public void setPopulateMessageAction(EventHandler<ActionEvent> eventHandler) {
        populateMessage.setOnAction(eventHandler);
    }
    public void setJournalMessageAction(EventHandler<ActionEvent> eventHandler) {
        journalMessage.setOnAction(eventHandler);
    }
    public void setChapterMessageAction(EventHandler<ActionEvent> eventHandler) {
        chapterMessage.setOnAction(eventHandler);
    }
    public void setCopyMessageAction(EventHandler<ActionEvent> eventHandler) {
        copyMessage.setOnAction(eventHandler);
    }
    public void setSaveMessageAction(EventHandler<ActionEvent> eventHandler) {
        saveMessage.setOnAction(eventHandler);
    }

    @Override
    public void show(Window ownerWindow, double anchorX, double anchorY) {
        super.show(ownerWindow, anchorX, anchorY);
        String actionsStylesheet = MultipleSelectionContextMenu.class.getResource("concept-navigator.css").toExternalForm();
        if (!getScene().getStylesheets().contains(actionsStylesheet)) {
            getScene().getStylesheets().add(actionsStylesheet);
        }
    }
}
