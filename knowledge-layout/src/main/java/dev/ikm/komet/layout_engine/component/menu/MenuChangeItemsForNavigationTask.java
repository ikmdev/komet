package dev.ikm.komet.layout_engine.component.menu;

import dev.ikm.komet.framework.temp.FxGet;
import dev.ikm.komet.framework.view.ObservableNavigationCoordinate;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.List;
import java.util.concurrent.Callable;

public class MenuChangeItemsForNavigationTask implements Callable<MenuItem>, ScopedValue.CallableOp<MenuItem, Exception> {
    @Override
    public MenuItem call() throws Exception {
        ObservableView observableView = ViewMenuFactory.OBSERVABLE_VIEW.get();
        ViewCalculator viewCalculator = ViewMenuFactory.VIEW_CALCULATOR.get();
        Menu changeNavigationMenu = new Menu("Change navigation");
        addChangeItemsForNavigation(viewCalculator,
                changeNavigationMenu.getItems(),
                observableView.navigationCoordinate());

        return changeNavigationMenu;
    }

    private static void addChangeItemsForNavigation(ViewCalculator viewCalculator,
                                                    List<MenuItem> menuItems,
                                                    ObservableNavigationCoordinate observableCoordinate) {
        for (ImmutableList<PatternFacade> navOption : FxGet.navigationOptions()) {
            StringBuilder menuText = new StringBuilder();
            for (PatternFacade navConcept : navOption) {
                if (menuText.length() > 0) {
                    menuText.append(", ");
                }
                menuText.append(viewCalculator.getPreferredDescriptionStringOrNid(navConcept));
            }
            CheckMenuItem item = new CheckMenuItem(menuText.toString());
            if (navOption.size() == observableCoordinate.navigationPatternNids().size()) {
                boolean foundAll = true;
                for (PatternFacade navPattern : navOption) {
                    if (!observableCoordinate.navigationPatternNids().contains(navPattern.nid())) {
                        foundAll = false;
                    }
                }
                item.setSelected(foundAll);
                if (item.isSelected()) {
                    item.setDisable(true);
                }

            }
            if (!item.isSelected()) {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        ObservableSet<PatternFacade> newSet = FXCollections.observableSet(navOption.toArray(new PatternFacade[navOption.size()]));
                        observableCoordinate.navigationPatternsProperty().setValue(newSet);
                    });
                    event.consume();
                });
            }
            menuItems.add(item);
        }
    }
}