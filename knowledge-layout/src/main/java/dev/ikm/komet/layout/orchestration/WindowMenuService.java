package dev.ikm.komet.layout.orchestration;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * The WindowMenuService interface is implemented a service that provide window menus,
 * and updates those menus as the window options change.
 */
public interface WindowMenuService {
    /**
     * Adds Window menu items to the end of the provided menu bar.
     *
     * @param stage    the stage of the application
     * @param menuBar  the menu bar to add the window menu items to
     */
    void addWindowMenu(Stage stage, MenuBar menuBar);


    /**
     * Retrieves the MenuBar component associated with the given Node.
     *
     * @param node The Node to search for a MenuBar.
     * @return An Optional object containing the MenuBar, if found, or empty if not.
     */
    static Optional<MenuBar> getMenuBar(Node node) {
        switch (node) {
            case MenuBar menuBar: return Optional.of(menuBar);
            case Parent parent:
                for (Node child: parent.getChildrenUnmodifiable()) {
                    Optional<MenuBar> menuBarOptional = getMenuBar(child);
                    if (menuBarOptional.isPresent()) {
                        return menuBarOptional;
                    }
                }
                break;
            default:
                // nothing to do...
        };
        return Optional.empty();
    }

}
