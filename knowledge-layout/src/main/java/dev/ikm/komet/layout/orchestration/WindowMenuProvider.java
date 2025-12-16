package dev.ikm.komet.layout.orchestration;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;


/**
 * The WindowMenuProvider class is an implementation of the WindowMenuService interface.
 * It provides methods for adding window menus to a menu bar and updating those menus as the window options change.
 */
public class WindowMenuProvider implements WindowMenuService {

    /**
     * Adds a "Window" menu to the given menu bar and associates it with the specified stage.
     * The menu is added to the existing menu bar and the association is registered with the WindowMenuManager
     * to allow for menu updates when the window options change.
     *
     * @param stage    The stage to associate with the "Window" menu.
     * @param menuBar  The menu bar to which the "Window" menu will be added.
     */
    @Override
    public void addWindowMenu(Stage stage, MenuBar menuBar) {
        menuBar.getMenus().add(new Menu("Window"));
        WindowMenuManager.addStage(stage, menuBar);
    }

}
