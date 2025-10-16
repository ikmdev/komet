package dev.ikm.komet.framework.menu;

import dev.ikm.tinkar.common.util.text.NaturalOrder;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.List;

/**
 This is a service for maintaining the contents of a Menu in sorted order.
 The menu is maintained in sorted order even if items are added or removed from the menu.

 When a menu is registered with the MenuSorterService, the contents of each <it>menu group</it>
 in the menu are sorted alphabetically, making use of a sorting HINT that is stored in the
 property map associated with the MenuItem. A menu group is a group of components that
 share the same sorting HINT. HINTs are sorted according to the dev.ikm.tinkar.common.util.text#NaturalOrder
 algorithm, which sorts numbers after characters, and will sort numbers containing strings
 by numeric value (1 test, 2 test, 10 test).
 */

public interface MenuSorterService {
    enum SORT {
        HINT
    }

    default void sortMenu(Menu menu) {
        sortMenuItems(menu.getItems());
    }

    default void sortMenuItems(List<? extends MenuItem> menuItems) {
        menuItems.sort((menuItem1, menuItem2) ->  {
            String sortString1 = menuItem1.getProperties().getOrDefault(SORT.HINT, "") + menuItem1.getText();
            String sortString2 = menuItem2.getProperties().getOrDefault(SORT.HINT, "") + menuItem2.getText();

            return NaturalOrder.compareStrings(sortString1, sortString2);
        });
    }

    /**
     * Checks if the contents of a menu are registered for sorting by this service.
     *
     * @param menu the menu to be checked
     * @return true if the menu is sorted, false otherwise
     */
    boolean isMenuRegistered(Menu menu);

    /**
     * Registers a menu with the MenuSorterService for maintaining its contents in sorted order.
     * <p>
     * When a menu is registered with the MenuSorterService, the contents of each menu group in the menu
     * are sorted alphabetically.
     * </p>
     *
     * @param menu the menu to be registered
     */
    void registerMenu(Menu menu);

    /**
     * Unregisters a menu from the MenuSorterService.
     * <p>
     * When a menu is unregistered, it will no longer have its contents maintained in sorted order.
     * </p>
     *
     * @param menu the menu to unregister
     */
    void unregisterMenu(Menu menu);
}
