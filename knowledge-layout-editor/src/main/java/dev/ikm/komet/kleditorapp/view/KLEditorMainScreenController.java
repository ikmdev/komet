package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kleditorapp.view.control.EditorWindowControl;
import dev.ikm.komet.kleditorapp.view.control.PatternBrowserCell;
import dev.ikm.komet.kleditorapp.view.propertiespane.PropertiesPane;
import dev.ikm.komet.layout.editor.EditorWindowManager;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.events.KLEditorWindowCreatedOrRemovedEvent;
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
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.ikm.komet.kview.events.EventTopics.KL_TOPIC;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_APP;

public class KLEditorMainScreenController {
    private static final Logger LOG = LoggerFactory.getLogger(KLEditorMainScreenController.class);

    private final EvtBus eventBus = EvtBusFactory.getDefaultEvtBus();

    @FXML
    private PropertiesPane propertiesPane;

    @FXML
    private BorderPane klEditorMainContainer;

    @FXML
    private TextField titleTextField;

    @FXML
    private ListView patternBrowserListView;

    private KLEditorWindowController klEditorWindowController;

    @FXML
    private EditorWindowControl editorWindowControl;

    private EditorWindowModel editorWindowModel;

    private WindowSettings windowSettings;
    private KometPreferences klEditorAppPreferences;

    private ObservableViewNoOverride windowViewCoordinates;

    private ViewCalculator viewCalculator;
    private ObservableList<Entity<EntityVersion>> patterns;

    public void init(KometPreferences klEditorAppPreferences, WindowSettings windowSettings, String windowToLoad) {
        this.klEditorAppPreferences = klEditorAppPreferences;
        this.windowSettings = windowSettings;

        this.windowViewCoordinates = windowSettings.getView();

        viewCalculator = ViewCalculatorWithCache.getCalculator(windowViewCoordinates.toViewCoordinateRecord());

        initPatternsList(viewCalculator);

        // Init Window
        initWindow(windowToLoad);

        // Init KLEditorWindow Controller
        klEditorWindowController = new KLEditorWindowController(editorWindowModel, editorWindowControl, viewCalculator);

        // Selection Manager
        SelectionManager selectionManager = SelectionManager.init(editorWindowControl);

        // Properties pane
        propertiesPane.init(selectionManager);

        klEditorWindowController.start();

        // setup Toast Manager
        KLToastManager.initParent(klEditorMainContainer);
    }

    public void shutdown() {
        klEditorWindowController.shutdown();
    }

    private void initPatternsList(ViewCalculator viewCalculator) {
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

    private void initWindow(String windowTitle) {
        loadWindow(windowTitle);
        titleTextField.textProperty().bindBidirectional(editorWindowModel.titleProperty());
    }

    private void loadWindow(String windowTitle) {
        if (windowTitle != null) {
            final KometPreferences editorWindowPreferences = klEditorAppPreferences.node(windowTitle);
            editorWindowModel = EditorWindowManager.loadWindowModel(editorWindowPreferences, viewCalculator, windowTitle);
        } else {
            editorWindowModel = EditorWindowManager.loadWindowModel(null, viewCalculator, null);
        }
    }

    @FXML
    private void onSave(ActionEvent actionEvent) {
        final KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final KometPreferences klEditorAppPreferences = appPreferences.node(KL_EDITOR_APP);

        EditorWindowManager.save(klEditorAppPreferences, editorWindowModel);

        eventBus.publish(KL_TOPIC,
                new KLEditorWindowCreatedOrRemovedEvent(actionEvent, KLEditorWindowCreatedOrRemovedEvent.KL_EDITOR_WINDOW_CREATED, editorWindowModel.getTitle()));

        KLToastManager.toast().show(Toast.Status.SUCCESS, "Window saved successfully");
    }
}