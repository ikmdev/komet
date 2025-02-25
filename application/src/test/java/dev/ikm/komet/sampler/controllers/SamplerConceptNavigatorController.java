package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.app.AppState;
import dev.ikm.komet.app.LoadDataSourceTask;
import dev.ikm.komet.controls.ConceptNavigatorModel;
import dev.ikm.komet.controls.KLConceptNavigatorControl;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.komet.navigator.graph.ViewNavigator;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.common.service.DataUriOption;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.ikm.komet.app.AppState.RUNNING;
import static dev.ikm.komet.app.AppState.STARTING;
import static dev.ikm.komet.controls.KLConceptNavigatorTreeCell.CONCEPT_NAVIGATOR_DRAG_FORMAT;
import static dev.ikm.komet.preferences.JournalWindowPreferences.MAIN_KOMET_WINDOW;

public class SamplerConceptNavigatorController {

    private static final SimpleObjectProperty<Navigator> navigatorProperty = new SimpleObjectProperty<>();

    @FXML
    private Label samplerDescription;

    @FXML
    private KLConceptNavigatorControl conceptNavigatorControl;

    @FXML
    private VBox conceptArea;

    private static final String STYLE = """
            data:text/css,
            
            .sample-control-container > .center-container {
                -fx-background-color: #fbfbfb;
                -fx-border-color: #e6e6e6;
                -fx-background-radius: 5px;
                -fx-border-radius: 5px;
                -fx-alignment: center;
                -fx-padding: 1.5em;
            }
            
            .sample-control-container > .center-container.dashed-border {
                -fx-border-color: #5DCF16;
                -fx-border-width: 3;
                -fx-border-style: segments(15, 12, 15, 12) line-cap round;
            }
            
            .sample-control-container > .center-container > .label {
                -fx-font-family: "Noto Sans";
                -fx-font-size: 12;
                -fx-font-weight: 600;
                -fx-text-fill: #2E3240;
                -fx-alignment: center-left;
                -fx-wrap-text: true;
            }
            
            """;

    public void initialize() {
        samplerDescription.setText("The Concept Navigator control is a tree view to display a hierarchy of concepts");
        conceptNavigatorControl.setHeader("Concept Header");
        conceptNavigatorControl.setOnDoubleClick(item -> {
            Entity<?> entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(item.getModel().publicId()));
            conceptArea.getChildren().setAll(new Label(entity.entityToString()));
        });

        conceptArea.setOnDragDropped(event -> {
            boolean success = false;
            if (event.getDragboard().hasContent(CONCEPT_NAVIGATOR_DRAG_FORMAT)) {
                Dragboard dragboard = event.getDragboard();
                UUID[] uuids = (UUID[]) dragboard.getContent(CONCEPT_NAVIGATOR_DRAG_FORMAT);
                Entity<?> entity = EntityService.get().getEntityFast(EntityService.get().nidForUuids(uuids));
                conceptArea.getChildren().setAll(new Label(entity.entityToString()));
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        conceptArea.setOnDragOver(event -> {
            if (event.getDragboard().hasContent(CONCEPT_NAVIGATOR_DRAG_FORMAT)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        conceptArea.setOnDragEntered(event -> {
            if (event.getDragboard().hasContent(CONCEPT_NAVIGATOR_DRAG_FORMAT)) {
                if (!conceptArea.getStyleClass().contains("dashed-border")) {
                    conceptArea.getStyleClass().add("dashed-border");
                }
                event.consume();
            }
        });
        conceptArea.setOnDragExited(event -> {
            conceptArea.getStyleClass().remove("dashed-border");
            event.consume();
        });
        conceptArea.getStylesheets().add(STYLE);

        navigatorProperty.subscribe((o, n) -> {
            List<TreeItem<ConceptNavigatorModel>> root = getRoot(n);
            conceptNavigatorControl.setRoot(root.getFirst());
        });
        LoadDataset.open();
    }

    private static List<TreeItem<ConceptNavigatorModel>> getRoot(Navigator navigator) {
        List<TreeItem<ConceptNavigatorModel>> children = new ArrayList<>();
        if (navigator == null) {
            return children;
        }
        for (int rootNid : navigator.getRootNids()) {
            TreeItem<ConceptNavigatorModel> treeItem = getConceptNavigatorModelTreeItem(navigator, rootNid);
            treeItem.setExpanded(true);
            children.add(treeItem);
        }
        return children;
    }

    private static List<TreeItem<ConceptNavigatorModel>> getChildren(Navigator navigator, ConceptFacade facade) {
        List<TreeItem<ConceptNavigatorModel>> children = new ArrayList<>();
        for (Edge edge : navigator.getChildEdges(facade.nid())) {
            TreeItem<ConceptNavigatorModel> treeItem = getConceptNavigatorModelTreeItem(navigator, edge.destinationNid());
            children.add(treeItem);
        }
        return children;
    }

    private static TreeItem<ConceptNavigatorModel> getConceptNavigatorModelTreeItem(Navigator navigator, int nid) {
        ConceptFacade facade = Entity.getFast(nid);
        ConceptNavigatorModel conceptNavigatorModel = new ConceptNavigatorModel(facade);
        TreeItem<ConceptNavigatorModel> treeItem = new TreeItem<>(conceptNavigatorModel);
        if (!navigator.getChildEdges(facade.nid()).isEmpty()) {
            treeItem.getChildren().addAll(getChildren(navigator, facade));
        }
        return treeItem;
    }

    /**
     * Requires ~/Solor/tinkar-starter-with-sample-data-Reasoned-1.0.4
     */
    private static class LoadDataset {

        private static final SimpleObjectProperty<AppState> state = new SimpleObjectProperty<>(STARTING);

        private LoadDataset() {}

        static void open() {
            // Load dataset
            PrimitiveData.getControllerOptions().stream()
                    .filter(dsc -> "Open SpinedArrayStore".equals(dsc.controllerName()))
                    .findFirst()
                    .ifPresent(controller -> {
                        List<DataUriOption> list = controller.providerOptions();
                        list.stream()
                                .filter(p -> p.toString().contains("1.0.4"))
                                .findFirst()
                                .ifPresent(controller::setDataUriOption);
                        PrimitiveData.setController(controller);
                    });

            state.subscribe(s -> {
                if (RUNNING == s) {
                    KometPreferences windowPreferences = KometPreferencesImpl.getConfigurationRootPreferences().node(MAIN_KOMET_WINDOW);
                    ObservableViewNoOverride view = new WindowSettings(windowPreferences).getView();
                    ViewProperties viewProperties = view.makeOverridableViewProperties();
                    Navigator navigator = new ViewNavigator(viewProperties.nodeView());
                    navigatorProperty.set(navigator);
                }
            });
            TinkExecutor.threadPool().submit(new LoadDataSourceTask(state));
        }
    }
}