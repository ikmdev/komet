package dev.ikm.komet.layout_engine.component.menu;

import dev.ikm.komet.framework.temp.FxGet;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.coordinate.stamp.StampPathImmutable;
import dev.ikm.tinkar.coordinate.view.VertexSort;
import dev.ikm.tinkar.coordinate.view.VertexSortNaturalOrder;
import dev.ikm.tinkar.coordinate.view.VertexSortNone;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.application.Platform;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.List;
import java.util.concurrent.Callable;

public class MenuChangeItemsForViewTask implements Callable<MenuItem>, ScopedValue.CallableOp<MenuItem, Exception> {
    @Override
    public MenuItem call() throws Exception {
        ObservableView observableView = ViewMenuFactory.OBSERVABLE_VIEW.get();
        ViewCalculator viewCalculator = ViewMenuFactory.VIEW_CALCULATOR.get();

        Menu changeViewMenu = new Menu("Change view");
        addChangeItemsForView(viewCalculator,
                changeViewMenu.getItems(),
                observableView);

        return changeViewMenu;
    }

    private static void addChangeItemsForView(ViewCalculator viewCalculator, List<MenuItem> menuItems,
                                              ObservableView observableView) {

        ViewMenuFactory.addChangeStates(menuItems, "Change allowed states", observableView.stampCoordinate().allowedStatesProperty());

        ViewMenuFactory.addChangeStates(menuItems, "Change allowed edge and language states", observableView.stampCoordinate().allowedStatesProperty());

        ViewMenuFactory.addChangeStates(menuItems, "Change allowed vertex states", observableView.navigationCoordinate().vertexStatesProperty());

        Menu changePathMenu = new Menu("Change path");
        menuItems.add(changePathMenu);
        for (PublicIdStringKey key : FxGet.pathCoordinates(viewCalculator).keySet()) {
            CheckMenuItem item = new CheckMenuItem(key.getString());
            StampPathImmutable pathCoordinate = FxGet.pathCoordinates(viewCalculator).get(key);
            int pathNid = pathCoordinate.pathConceptNid();
            item.setSelected(pathNid == observableView.stampCoordinate().pathNidForFilter());
            item.setDisable(item.isSelected());
            item.setUserData(FxGet.pathCoordinates(viewCalculator).get(key));
            item.setOnAction(event -> {
                StampPathImmutable path = (StampPathImmutable) item.getUserData();
                Platform.runLater(() -> observableView.setViewPath((path.pathConceptNid())));
                event.consume();
            });
            changePathMenu.getItems().add(item);
        }


        Menu changeVertexSortMenu = new Menu("Change sort");
        menuItems.add(changeVertexSortMenu);
        VertexSort[] sorts = new VertexSort[]{VertexSortNaturalOrder.SINGLETON, VertexSortNone.SINGLETON};
        for (VertexSort vertexSort : sorts) {
            CheckMenuItem item = new CheckMenuItem(vertexSort.getVertexSortName());
            item.setSelected(observableView.navigationCoordinate().sortVerticesProperty().equals(vertexSort));
            item.setDisable(item.isSelected());
            item.setOnAction(event -> {
                Platform.runLater(() ->
                        observableView.navigationCoordinate().sortVerticesProperty().setValue(vertexSort == VertexSortNaturalOrder.SINGLETON));
                event.consume();
            });
            changeVertexSortMenu.getItems().add(item);
        }
    }
}