package dev.ikm.komet.kleditorapp;

import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class KLEditorWindowController {

    @FXML
    private ListView patternBrowserListView;

    private WindowSettings windowSettings;
    private KometPreferences nodePreferences;

    private ObservableViewNoOverride windowView;

    private ViewCalculator viewCalculator;
    private ObservableList<Entity<EntityVersion>> patterns;

    public void init(KometPreferences nodePreferences, WindowSettings windowSettings) {
        this.nodePreferences = nodePreferences;
        this.windowSettings = windowSettings;

        this.windowView = windowSettings.getView();

        ViewCalculator viewCalculator = ViewCalculatorWithCache.getCalculator(windowView.toViewCoordinateRecord());

        patterns = FXCollections.observableArrayList();
        PrimitiveData.get().forEachPatternNid(patternNid -> {
            Latest<PatternEntityVersion> latestPattern = viewCalculator.latest(patternNid);
            latestPattern.ifPresent(patternEntityVersion -> {
                if (EntityService.get().getEntity(patternEntityVersion.nid()).isPresent()) {
                    patterns.add(EntityService.get().getEntity(patternNid).get());
                }
            });
        });

        patternBrowserListView.setCellFactory(param -> new PatternBrowserCell(viewCalculator));
        patternBrowserListView.setItems(patterns);
    }

    @FXML
    public void initialize() {
    }
}