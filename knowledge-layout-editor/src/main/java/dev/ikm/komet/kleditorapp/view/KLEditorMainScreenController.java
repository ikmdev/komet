package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kleditorapp.model.SectionModel;
import dev.ikm.komet.kleditorapp.model.WindowModel;
import dev.ikm.komet.kview.events.KLEditorWindowCreatedEvent;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.kview.events.EventTopics.KL_TOPIC;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_APP;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_WINDOWS;

public class KLEditorMainScreenController {
    private static final Logger LOG = LoggerFactory.getLogger(KLEditorMainScreenController.class);

    private final EvtBus eventBus = EvtBusFactory.getDefaultEvtBus();

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

    @FXML
    private void onSave(ActionEvent actionEvent) {
        String windowTitle = titleTextField.getText();

        final KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final KometPreferences klEditorAppPreferences = appPreferences.node(KL_EDITOR_APP);
        final KometPreferences editorWindowPreferences = klEditorAppPreferences.node(windowTitle);

        List<String> editorWindows = klEditorAppPreferences.getList(KL_EDITOR_WINDOWS);
        if (!editorWindows.contains(windowTitle)) {
            editorWindows.add(windowTitle);
        }

        klEditorAppPreferences.putList(KL_EDITOR_WINDOWS, editorWindows);

        int numberColumns = columnsComboBox.getItems().indexOf(columnsComboBox.getSelectionModel().getSelectedItem()) + 1;
        editorWindowPreferences.putInt("COLUMNS", numberColumns);

        try {
            editorWindowPreferences.flush();
            klEditorAppPreferences.flush();
        } catch (BackingStoreException e) {
            LOG.error("Error writing KL Editor Window flag to preferences", e);
        }

        eventBus.publish(KL_TOPIC,
                new KLEditorWindowCreatedEvent(actionEvent, KLEditorWindowCreatedEvent.KL_EDITOR_WINDOW_CREATED, windowTitle));
    }
}