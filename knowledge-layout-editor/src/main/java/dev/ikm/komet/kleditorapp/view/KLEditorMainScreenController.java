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
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

import static dev.ikm.komet.kview.events.EventTopics.KL_TOPIC;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_APP;

public class KLEditorMainScreenController {
    private static final Logger LOG = LoggerFactory.getLogger(KLEditorMainScreenController.class);

    private final EvtBus eventBus = EvtBusFactory.getDefaultEvtBus();

    @FXML
    private ScrollPane canvasScrollPane;

    @FXML
    private StackPane canvas;

    @FXML
    private Button saveButton;

    @FXML
    private PropertiesPane propertiesPane;

    @FXML
    private BorderPane klEditorMainContainer;

    @FXML
    private TextField titleTextField;

    @FXML
    private ListView<PatternBrowserItem> patternBrowserListView;

    @FXML
    private ListView controlsListView;

    private KLEditorWindowController klEditorWindowController;

    @FXML
    private EditorWindowControl editorWindowControl;

    private EditorWindowModel editorWindowModel;

    private WindowSettings windowSettings;
    private KometPreferences klEditorAppPreferences;

    private ObservableViewNoOverride windowViewCoordinates;

    private ViewCalculator viewCalculator;
    private ObservableList<PatternBrowserItem> patternsList;

    public void init(KometPreferences klEditorAppPreferences, WindowSettings windowSettings, String windowToLoad) {
        this.klEditorAppPreferences = klEditorAppPreferences;
        this.windowSettings = windowSettings;

        canvasScrollPane.viewportBoundsProperty().subscribe(viewportBounds -> {
            canvas.setMinSize(viewportBounds.getWidth(), viewportBounds.getHeight());
        });

        this.windowViewCoordinates = windowSettings.getView();

        viewCalculator = ViewCalculatorWithCache.getCalculator(windowViewCoordinates.toViewCoordinateRecord());

        initPatternsList(viewCalculator);
        initControlsList();

        saveButton.disableProperty().bind(titleTextField.textProperty().isEmpty());

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
        patternsList = FXCollections.observableArrayList();
        PrimitiveData.get().forEachPatternNid(patternNid -> {
            Latest<PatternEntityVersion> latestPattern = viewCalculator.latest(patternNid);
            latestPattern.ifPresent(patternEntityVersion -> {
                if (EntityService.get().getEntity(patternEntityVersion.nid()).isPresent()) {
                    Entity<EntityVersion> entity = EntityService.get().getEntity(patternNid).get();
                    PatternBrowserItem patternBrowserItem = new PatternBrowserItem(entity, viewCalculator);
                    patternsList.add(patternBrowserItem);
                }
            });
        });

        // Sort
        FXCollections.sort(patternsList,
                Comparator.comparing(PatternBrowserItem::getTitle,
                        String.CASE_INSENSITIVE_ORDER));

        patternBrowserListView.setCellFactory(param -> new PatternBrowserCell(viewCalculator));
        patternBrowserListView.setItems(patternsList);
    }

    private void initControlsList() {

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