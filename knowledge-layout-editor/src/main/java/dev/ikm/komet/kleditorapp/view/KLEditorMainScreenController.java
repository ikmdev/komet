package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kleditorapp.model.SectionModel;
import dev.ikm.komet.kleditorapp.model.WindowModel;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class KLEditorMainScreenController {

    @FXML
    private ComboBox<String> columnsComboBox;

    @FXML
    private TextField sectionNameTextField;

    @FXML
    private TextField titleTextField;

    @FXML
    private ListView patternBrowserListView;

    @FXML
    private KLEditorWindowController klEditorWindowController;

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

        setupWindow();

        // Init KLEditorWindow
        klEditorWindowController.init(viewCalculator);

        // Columns ComboBox
        for (int i = 1 ; i <= 4 ; ++i) {
            columnsComboBox.getItems().add(i + " column");
        }
        columnsComboBox.setValue(columnsComboBox.getItems().getFirst());
    }

    private void setupWindow() {
        WindowModel windowModel = WindowModel.instance();

        titleTextField.textProperty().bindBidirectional(windowModel.titleProperty());

        // sections
        SectionModel sectionModel = windowModel.getSections().getFirst();
        sectionNameTextField.textProperty().bindBidirectional(sectionModel.nameProperty());
    }

    @FXML
    public void initialize() {
    }
}