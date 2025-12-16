package dev.ikm.komet.layout_engine.component.menu;

import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.scene.control.MenuItem;

import java.util.concurrent.Callable;


public class MenuStateItemsForNavigationTask implements Callable<MenuItem>, ScopedValue.CallableOp<MenuItem, Exception> {
    @Override
    public MenuItem call() throws Exception {
        ObservableView observableView = ViewMenuFactory.OBSERVABLE_VIEW.get();
        ViewCalculator viewCalculator = ViewMenuFactory.VIEW_CALCULATOR.get();

        return ViewMenuFactory.makeStateMenu(observableView.navigationCoordinate(), "Navigation coordinate", viewCalculator);
    }

}
