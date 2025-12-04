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

    private final MenuItem populateMenuItem;
    private final MenuItem journalMenuItem;
    private final MenuItem chapterMenuItem;
    private final MenuItem copyMenuItem;
    private final MenuItem saveMenuItem;

    /**
     * Creates a new MultipleSelectionContextMenu
     */
    public MultipleSelectionContextMenu() {
        setAutoHide(true);
        ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.concept-navigator");

        populateMenuItem = new MenuItem(resources.getString("multi.selection.context.menu.option.populate"), new IconRegion("icon", "populate"));
        journalMenuItem = new MenuItem(resources.getString("multi.selection.context.menu.option.journal"), new IconRegion("icon", "send"));
        chapterMenuItem = new MenuItem(resources.getString("multi.selection.context.menu.option.chapter"), new IconRegion("icon", "send"));
        copyMenuItem = new MenuItem(resources.getString("multi.selection.context.menu.option.copy"), new IconRegion("icon", "duplicate"));
        saveMenuItem = new MenuItem(resources.getString("multi.selection.context.menu.option.save"), new IconRegion("icon", "save"));

        getItems().addAll(populateMenuItem, journalMenuItem, chapterMenuItem, copyMenuItem, saveMenuItem);
        setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
    }

    /**
     * <p>Sets the {@link EventHandler<ActionEvent>} that will be handled when the {@link #populateMenuItem} menu
     * item is fired.
     * </p>
     * @param eventHandler a {@link EventHandler<ActionEvent>}
     */
    public void setPopulateMenuItemAction(EventHandler<ActionEvent> eventHandler) {
        populateMenuItem.setOnAction(eventHandler);
    }

    /**
     * <p>Sets the {@link EventHandler<ActionEvent>} that will be handled when the {@link #journalMenuItem} menu
     * item is fired.
     * </p>
     * @param eventHandler a {@link EventHandler<ActionEvent>}
     */
    public void setJournalMenuItemAction(EventHandler<ActionEvent> eventHandler) {
        journalMenuItem.setOnAction(eventHandler);
    }

    /**
     * <p>Sets the {@link EventHandler<ActionEvent>} that will be handled when the {@link #chapterMenuItem} menu
     * item is fired.
     * </p>
     * @param eventHandler a {@link EventHandler<ActionEvent>}
     */
    public void setChapterMenuItemAction(EventHandler<ActionEvent> eventHandler) {
        chapterMenuItem.setOnAction(eventHandler);
    }

    /**
     * <p>Sets the {@link EventHandler<ActionEvent>} that will be handled when the {@link #copyMenuItem} menu
     * item is fired.
     * </p>
     * @param eventHandler a {@link EventHandler<ActionEvent>}
     */
    public void setCopyMenuItemAction(EventHandler<ActionEvent> eventHandler) {
        copyMenuItem.setOnAction(eventHandler);
    }

    /**
     * <p>Sets the {@link EventHandler<ActionEvent>} that will be handled when the {@link #saveMenuItem} menu
     * item is fired.
     * </p>
     * @param eventHandler a {@link EventHandler<ActionEvent>}
     */
    public void setSaveMenuItemAction(EventHandler<ActionEvent> eventHandler) {
        saveMenuItem.setOnAction(eventHandler);
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
