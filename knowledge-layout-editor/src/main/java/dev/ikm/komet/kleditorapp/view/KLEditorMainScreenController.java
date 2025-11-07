package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kleditorapp.model.SectionModel;
import dev.ikm.komet.kleditorapp.model.WindowModel;
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
import javafx.scene.control.ComboBox;
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
    private BorderPane klEditorMainContainer;

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
    private KometPreferences klEditorAppPreferences;

    private ObservableViewNoOverride windowView;

    private ViewCalculator viewCalculator;
    private ObservableList<Entity<EntityVersion>> patterns;

    public void init(KometPreferences klEditorAppPreferences, WindowSettings windowSettings, String windowToLoad) {
        this.klEditorAppPreferences = klEditorAppPreferences;
        this.windowSettings = windowSettings;

        this.windowView = windowSettings.getView();

        viewCalculator = ViewCalculatorWithCache.getCalculator(windowView.toViewCoordinateRecord());

        initPatternsList(viewCalculator);

        // Init KLEditorWindow Controller
        klEditorWindowController.init(viewCalculator);

        // Init Window
        initWindow(windowToLoad);

        // Columns ComboBox
        for (int i = 1 ; i <= 4 ; ++i) {
            columnsComboBox.getItems().add(i + " column");
        }
        columnsComboBox.setValue(columnsComboBox.getItems().getFirst());

        // setup Toast Manager
        KLToastManager.initParent(klEditorMainContainer);
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
        WindowModel windowModel = WindowModel.instance();

        if (windowTitle != null) {
            loadWindow(windowTitle);
        }

        titleTextField.textProperty().bindBidirectional(windowModel.titleProperty());

        // sections
        SectionModel sectionModel = windowModel.getMainSection();
        sectionNameTextField.textProperty().bindBidirectional(sectionModel.nameProperty());
    }

    private void loadWindow(String windowTitle) {
        WindowModel windowModel = WindowModel.instance();

        windowModel.setTitle(windowTitle);

        windowModel.load(klEditorAppPreferences, viewCalculator);
    }

    @FXML
    public void initialize() {
    }

    @FXML
    private void onSave(ActionEvent actionEvent) {
        final KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final KometPreferences klEditorAppPreferences = appPreferences.node(KL_EDITOR_APP);

        WindowModel.instance().save(klEditorAppPreferences);

        String windowTitle = WindowModel.instance().getTitle();

        eventBus.publish(KL_TOPIC,
                new KLEditorWindowCreatedOrRemovedEvent(actionEvent, KLEditorWindowCreatedOrRemovedEvent.KL_EDITOR_WINDOW_CREATED, windowTitle));

        KLToastManager.toast().show(Toast.Status.SUCCESS, "Window saved successfully");
    }
}