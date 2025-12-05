package dev.ikm.komet.layout_engine.component.menu;

import dev.ikm.komet.framework.view.ObservableCoordinate;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.PropertyWithOverride;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static dev.ikm.komet.layout_engine.component.menu.ViewMenuFactory.getNameAndValueString;

public class OverrideMenuItems implements Callable<List<MenuItem>>, ScopedValue.CallableOp<List<MenuItem>, Exception> {
    @Override
    public List<MenuItem> call() throws Exception {
        ObservableView observableView = ViewMenuFactory.OBSERVABLE_VIEW.get();
        ViewCalculator viewCalculator = ViewMenuFactory.VIEW_CALCULATOR.get();

        List<MenuItem> menuItems = new ArrayList<>();

        makeRecursiveOverrideMenu(viewCalculator, menuItems, observableView);
        return menuItems;
    }

    private static boolean makeRecursiveOverrideMenu(ViewCalculator viewCalculator, List<MenuItem> menuItems,
                                                     ObservableCoordinate observableCoordinate) {
        menuItems.add(new MenuItem(observableCoordinate.getClass().getSimpleName()));
        menuItems.add(new MenuItem("Has overrides: " + observableCoordinate.hasOverrides()));
        if (observableCoordinate.hasOverrides()) {
            Menu overridesMenu = new Menu(viewCalculator.toPreferredEntityStringOrInputString(observableCoordinate.getName()) + " has overrides");
            menuItems.add(overridesMenu);
            for (Property property : observableCoordinate.getBaseProperties()) {
                if (property instanceof PropertyWithOverride) {
                    PropertyWithOverride propertyWithOverride = (PropertyWithOverride) property;
                    if (propertyWithOverride.isOverridden()) {
                        overridesMenu.getItems().add(new MenuItem(getNameAndValueString(viewCalculator, propertyWithOverride)));
                    }
                }
            }
            addRemoveOverrides(observableCoordinate, menuItems);

            for (ObservableCoordinate compositeCoordinate : observableCoordinate.getCompositeCoordinates()) {
                if (makeRecursiveOverrideMenu(viewCalculator, overridesMenu.getItems(),
                        compositeCoordinate)) {
                    menuItems.add(new SeparatorMenuItem());
                }
            }
            return true;
        }
        return false;
    }

    private static void addRemoveOverrides(ObservableCoordinate observableCoordinate, List<MenuItem> menuItems) {
        if (observableCoordinate.hasOverrides()) {
            MenuItem removeOverrides = new MenuItem("Remove overrides");
            menuItems.add(removeOverrides);
            removeOverrides.setOnAction(event -> {
                Platform.runLater(() -> observableCoordinate.removeOverrides());
                event.consume();
            });
        }
    }

}
