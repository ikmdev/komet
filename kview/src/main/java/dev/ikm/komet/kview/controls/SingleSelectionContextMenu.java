package dev.ikm.komet.kview.controls;

import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;

import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * <p>A popup control containing an ObservableList of menu items.
 * </p>
 * <p>It is used by the {@link KLConceptNavigatorControl}, to show different options to the user, when there is
 * a single selection of {@link ConceptNavigatorTreeItem}.
 * </p>
 */
public class SingleSelectionContextMenu extends ContextMenu {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.concept-navigator");

    private final Menu relatedMenuItem;
    private final String relatedMenuItemId = "relatedMenuItemId";
    private final MenuItem workspaceMenuItem;

    private final String actionsStylesheet = SingleSelectionContextMenu.class.getResource("concept-navigator.css").toExternalForm();
    private final ListChangeListener<Window> windowListChangeListener = c -> {
        while (c.next()) {
            if (c.wasAdded()) {
                for (Window window : c.getAddedSubList()) {
                    // This applies to this SingleSelectionContextMenu and also to the subMenu
                    // that is just ContextMenu
                    if (window instanceof SingleSelectionContextMenu ||
                            (window instanceof ContextMenu cm && !cm.getItems().isEmpty() &&
                                cm.getItems().getFirst().getParentMenu() != null &&
                                relatedMenuItemId.equals(cm.getItems().getFirst().getParentMenu().getId()))) {
                        if (!window.getScene().getStylesheets().contains(actionsStylesheet)) {
                            // If the .context-menu styling is not added to the application stylesheet,
                            // we need this hack, to add the styling to the context-menu window scene.
                            window.getScene().getStylesheets().add(actionsStylesheet);
                        }
                    }
                }
            }
        }
    };

    /**
     * Creates a new SingleSelectionContextMenu
     */
    public SingleSelectionContextMenu() {
        setAutoHide(true);
        setId("SingleSelectionContextMenuId");

        relatedMenuItem = new Menu(resources.getString("single.selection.context.menu.option.related"));
        relatedMenuItem.setId(relatedMenuItemId);
        workspaceMenuItem = new MenuItem(resources.getString("single.selection.context.menu.option.workspace"));

        getItems().addAll(relatedMenuItem, workspaceMenuItem);
        setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);

        setOnShowing(_ -> Window.getWindows().addListener(windowListChangeListener));
        setOnHiding(_ -> Window.getWindows().removeListener(windowListChangeListener));
    }

    /**
     * <p>Sets the {@link EventHandler<ActionEvent>} that will be handled when the {@link #workspaceMenuItem} menu
     * item is fired.
     * </p>
     * @param eventHandler a {@link EventHandler<ActionEvent>}
     */
    public void setWorkspaceMenuItemAction(EventHandler<ActionEvent> eventHandler) {
        workspaceMenuItem.setOnAction(eventHandler);
    }

    /**
     * <p>The passed {@link List<ConceptFacade>} is used to create a subMenu for {@link #relatedMenuItem}.
     * </p>
     * <p>For each menu item, it sets the {@link Consumer<ConceptFacade>} that will be accepted if such
     * item is fired.
     * </p>
     * @param conceptFacadeList a {@link List<ConceptFacade>}
     * @param consumer a {@link Consumer<ConceptFacade>}
     * @see ConceptNavigatorTreeItem#relatedConceptsProperty()
     */
    public void setRelatedByMenuItemAction(List<ConceptFacade> conceptFacadeList, Consumer<ConceptFacade> consumer) {
        if (conceptFacadeList == null) {
            return;
        }
        relatedMenuItem.getItems().setAll(
                conceptFacadeList.stream()
                        .map(concept -> {
                            MenuItem menuItem = new MenuItem(concept.description());
                            menuItem.setOnAction(_ -> consumer.accept(concept));
                            return menuItem;
                        })
                        .toList());
    }

}
