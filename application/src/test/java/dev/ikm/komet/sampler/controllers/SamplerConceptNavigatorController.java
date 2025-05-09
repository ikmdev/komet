package dev.ikm.komet.sampler.controllers;

import dev.ikm.komet.app.AppState;
import dev.ikm.komet.app.LoadDataSourceTask;
import dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem;
import dev.ikm.komet.kview.controls.ConceptNavigatorUtils;
import dev.ikm.komet.kview.controls.KLConceptNavigatorControl;
import dev.ikm.komet.kview.controls.KLSearchControl;
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
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static dev.ikm.komet.app.AppState.RUNNING;
import static dev.ikm.komet.app.AppState.STARTING;
import static dev.ikm.komet.kview.controls.KLConceptNavigatorTreeCell.CONCEPT_NAVIGATOR_DRAG_FORMAT;
import static dev.ikm.komet.preferences.JournalWindowPreferences.MAIN_KOMET_WINDOW;

public class SamplerConceptNavigatorController {

    @FXML
    private Label samplerDescription;

    @FXML
    private KLSearchControl searchControl;

    @FXML
    private KLConceptNavigatorControl conceptNavigatorControl;

    @FXML
    private SplitPane root;

    @FXML
    private VBox conceptArea;

    @FXML
    private CheckBox showTagsCheckBox;

    @FXML
    private Button cleanAreaButton;

    @FXML
    private VBox datasetBox;

    private static final String STYLE = """
            data:text/css,
            
            .sample-control-container,
            .sample-control-container:focused {
                -fx-background-color: transparent;
                -fx-background-insets: 0;
                -fx-padding: 0;
            }
            
            .sample-control-container > .split-pane-divider {
                -fx-padding: 0;
            }
            
            .sample-control-container > * > .inner-container {
                -fx-background-color: #6E7989;
                -fx-padding: 0;
                -fx-spacing: -8;
            }
            .sample-control-container > * > .center-container {
                -fx-background-color: #fbfbfb;
                -fx-border-color: #e6e6e6;
                -fx-background-radius: 5px;
                -fx-border-radius: 5px;
                -fx-alignment: center;
                -fx-padding: 1.5em;
                -fx-spacing: 20;
                -fx-min-width: 200;
            }
            
            .sample-control-container > * > .center-container.dashed-border {
                -fx-border-color: #5DCF16;
                -fx-border-width: 3;
                -fx-border-style: segments(15, 12, 15, 12) line-cap round;
            }
            
            .sample-control-container > * > .control-configuration-container > .label,
            .sample-control-container > * > .center-container > .label {
                -fx-font-family: "Noto Sans";
                -fx-font-size: 12;
                -fx-font-weight: 600;
                -fx-text-fill: #2E3240;
                -fx-alignment: center-left;
                -fx-wrap-text: true;
            }
            
            .sample-control-container > * > .control-configuration-container {
                -fx-spacing: 10;
                -fx-padding: 2;
            }
            
            .sample-control-container > * > .control-configuration-container > .label.title {
                -fx-font-size: 13;
                -fx-font-weight: bold;
            }
            
            .sample-control-container > * > .control-configuration-container > .dataset-box {
                -fx-spacing: 10;
                -fx-alignment: center-left;
                -fx-border-color: #e6e6e6;
                -fx-background-color: #fbfbfb;
            }
            
            .sample-control-container > * > .control-configuration-container > .dataset-box > .label {
                -fx-font-size: 11;
                -fx-font-weight: 400;
                -fx-cursor: hand;
            }
            
            .sample-control-container > * > .control-configuration-container > .dataset-box > .label.selected {
                -fx-font-weight: bold;
            }
            
            """;

    public void initialize() {
        samplerDescription.setText("The Concept Navigator control is a tree view to display a hierarchy of concepts");

        searchControl.setOnAction(_ -> {
            Navigator navigator = conceptNavigatorControl.getNavigator();
            if (navigator == null) {
                return;
            }
            ViewCalculator calculator = navigator.getViewCalculator();
            searchControl.setResultsPlaceholder("Searching...");
            TinkExecutor.threadPool().execute(() -> {
                try {
                    List<LatestVersionSearchResult> results = calculator.search(searchControl.getText(), 1000).toList();
                    List<KLSearchControl.SearchResult> searchResults = new ArrayList<>();
                    results.stream()
                            .filter(result -> result.latestVersion().isPresent())
                            .forEach(result -> {
                                SemanticEntityVersion semantic = result.latestVersion().get();
                                searchResults.addAll(
                                        Entity.getConceptForSemantic(semantic.nid()).map(entity -> {
                                            int[] parentNids = navigator.getParentNids(entity.nid());
                                            List<KLSearchControl.SearchResult> list = new ArrayList<>();
                                            if (parentNids != null) {
                                                for (int parentNid : parentNids) {
                                                    ConceptFacade parent = Entity.getFast(parentNid);
                                                    list.add(new KLSearchControl.SearchResult(parent, entity, searchControl.getText()));
                                                }
                                            } else {
                                                list.add(new KLSearchControl.SearchResult(null, entity, searchControl.getText()));
                                            }
                                            return list;
                                        }).orElse(List.of()));
                            });
                    // NOTE: different semanticIds give same entity
                    List<KLSearchControl.SearchResult> distinctResults = searchResults.stream().distinct().toList();
                    Platform.runLater(() -> {
                        searchControl.setResultsPlaceholder(null);
                        searchControl.resultsProperty().addAll(distinctResults);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        searchControl.setOnFilterAction(_ -> {
            // DUMMY
            searchControl.setFilterSet(!searchControl.isFilterSet());
        });

        conceptNavigatorControl.setHeader("Concept Header");

        conceptNavigatorControl.setOnAction(action -> switch (action) {
            case OPEN_IN_WORKSPACE, POPULATE_SELECTION -> item -> {
                List<UUID[]> uuids = List.<UUID[]>of(item.publicId().asUuidArray());
                populateArea(uuids);
            };
            case SHOW_RELATED_CONCEPTS -> {
                // Dummy, for now just add the parents of the selected item as related content:
                TreeItem<ConceptFacade> selectedItem = conceptNavigatorControl.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    conceptNavigatorControl.getNavigator().getParentNids(selectedItem.getValue().nid());
                    List<ConceptFacade> list = Arrays.stream(conceptNavigatorControl.getNavigator().getParentNids(selectedItem.getValue().nid())).boxed()
                            .map(nid -> (ConceptFacade) Entity.getFast(nid)).toList();
                    ((ConceptNavigatorTreeItem) selectedItem).setRelatedConcepts(list);
                }
                yield i -> System.out.println("Click! on " + i.description());
            }
            case SEND_TO_JOURNAL, SEND_TO_CHAPTER, COPY, SAVE_TO_FAVORITES -> _ -> {}; // TODO: Add implementation
        });
        searchControl.setOnLongHover(conceptNavigatorControl::expandAndHighlightConcept);
        searchControl.setOnSearchResultClick(_ -> conceptNavigatorControl.unhighlightConceptsWithDelay());
        searchControl.setOnClearSearch(_ -> ConceptNavigatorUtils.resetConceptNavigator(conceptNavigatorControl));

        showTagsCheckBox.selectedProperty().subscribe(s -> conceptNavigatorControl.setShowTags(s));

        cleanAreaButton.disableProperty().bind(Bindings.size(conceptArea.getChildren()).isEqualTo(0));

        conceptArea.setOnDragDropped(event -> {
            boolean success = false;
            if (event.getDragboard().hasContent(CONCEPT_NAVIGATOR_DRAG_FORMAT)) {
                Dragboard dragboard = event.getDragboard();
                List<List<UUID[]>> uuids = (List<List<UUID[]>>) dragboard.getContent(CONCEPT_NAVIGATOR_DRAG_FORMAT);
                uuids.forEach(this::populateArea);
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
        root.getStylesheets().add(STYLE);

        LoadDataset.open(conceptNavigatorControl::setNavigator, datasetBox);
    }

    private void populateArea(List<UUID[]> uuids) {
        for (UUID[] uuid : uuids) {
            Entity<?> entity = EntityService.get().getEntityFast(EntityService.get().nidForUuids(uuid));
            conceptArea.getChildren().add(new Label(entity.entityToString()));
        }
    }

    @FXML
    private void cleanArea() {
        conceptArea.getChildren().clear();
    }

    /**
     * Requires ~/Solor/tinkar-starter-test-data-with-navigator-scenarios-reasoned
     * or ~/Solor/snomedct-international
     */
    private static class LoadDataset {

        private static final SimpleObjectProperty<AppState> state = new SimpleObjectProperty<>(STARTING);

        private LoadDataset() {}

        static void open(Consumer<Navigator> consumer, VBox box) {
            // Load dataset
            PrimitiveData.getControllerOptions().stream()
                    .filter(dsc -> "Open SpinedArrayStore".equals(dsc.controllerName()))
                    .findFirst()
                    .ifPresent(controller -> {
                        List<DataUriOption> list = controller.providerOptions();
                        list.forEach(p -> {
                            Label option = new Label(p.toFile().getName());
                            option.setOnMouseClicked(_ -> {
                                option.getStyleClass().add("selected");
                                box.setMouseTransparent(true);
                                controller.setDataUriOption(p);
                                TinkExecutor.threadPool().submit(new LoadDataSourceTask(state));
                            });
                            box.getChildren().add(option);
                        });
                        PrimitiveData.setController(controller);
                    });

            state.subscribe(s -> {
                if (RUNNING == s) {
                    KometPreferences windowPreferences = KometPreferencesImpl.getConfigurationRootPreferences().node(MAIN_KOMET_WINDOW);
                    ObservableViewNoOverride view = new WindowSettings(windowPreferences).getView();
                    ViewProperties viewProperties = view.makeOverridableViewProperties();
                    Navigator navigator = new ViewNavigator(viewProperties.nodeView());
                    consumer.accept(navigator);
                }
            });
        }
    }
}