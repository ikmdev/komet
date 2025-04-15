package dev.ikm.komet.kview.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;

import java.util.ResourceBundle;

/**
 * <p>A popup control containing an ObservableList of menu items.
 * </p>
 * <p>It is used by the {@link KLConceptNavigatorControl}, to show different options to the user, when there is
 * a multiple selection of {@link ConceptNavigatorTreeItem}.
 * </p>
 */
public class MultipleSelectionContextMenu extends ContextMenu {

    private final MenuItem populateMessage;
    private final MenuItem journalMessage;
    private final MenuItem chapterMessage;
    private final MenuItem copyMessage;
    private final MenuItem saveMessage;

    /**
     * Creates a new MultipleSelectionContextMenu
     */
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

    /**
     * <p>Sets the {@link EventHandler<ActionEvent>} that will be handled when the {@link #populateMessage} menu
     * item is fired.
     * </p>
     * @param eventHandler a {@link EventHandler<ActionEvent>}
     */
    public void setPopulateMessageAction(EventHandler<ActionEvent> eventHandler) {
        populateMessage.setOnAction(eventHandler);
    }

    /**
     * <p>Sets the {@link EventHandler<ActionEvent>} that will be handled when the {@link #journalMessage} menu
     * item is fired.
     * </p>
     * @param eventHandler a {@link EventHandler<ActionEvent>}
     */
    public void setJournalMessageAction(EventHandler<ActionEvent> eventHandler) {
        journalMessage.setOnAction(eventHandler);
    }

    /**
     * <p>Sets the {@link EventHandler<ActionEvent>} that will be handled when the {@link #chapterMessage} menu
     * item is fired.
     * </p>
     * @param eventHandler a {@link EventHandler<ActionEvent>}
     */
    public void setChapterMessageAction(EventHandler<ActionEvent> eventHandler) {
        chapterMessage.setOnAction(eventHandler);
    }

    /**
     * <p>Sets the {@link EventHandler<ActionEvent>} that will be handled when the {@link #copyMessage} menu
     * item is fired.
     * </p>
     * @param eventHandler a {@link EventHandler<ActionEvent>}
     */
    public void setCopyMessageAction(EventHandler<ActionEvent> eventHandler) {
        copyMessage.setOnAction(eventHandler);
    }

    /**
     * <p>Sets the {@link EventHandler<ActionEvent>} that will be handled when the {@link #saveMessage} menu
     * item is fired.
     * </p>
     * @param eventHandler a {@link EventHandler<ActionEvent>}
     */
    public void setSaveMessageAction(EventHandler<ActionEvent> eventHandler) {
        saveMessage.setOnAction(eventHandler);
    }

    /**
     * {@inheritDoc}
     * <p>Overridden to add the stylesheets from the control to the contextMenu.
     * </p>
     * @param ownerWindow The owner of the popup. This must not be null.
     * @param anchorX the x position of the popup anchor in screen coordinates
     * @param anchorY the y position of the popup anchor in screen coordinates
     */
    @Override
    public void show(Window ownerWindow, double anchorX, double anchorY) {
        super.show(ownerWindow, anchorX, anchorY);
        String actionsStylesheet = MultipleSelectionContextMenu.class.getResource("concept-navigator.css").toExternalForm();
        if (!getScene().getStylesheets().contains(actionsStylesheet)) {
            getScene().getStylesheets().add(actionsStylesheet);
        }
    }
}
