package dev.ikm.komet.kview.mvvm.view.search;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static dev.ikm.komet.kview.mvvm.model.DragAndDropType.CONCEPT;
import static dev.ikm.komet.kview.mvvm.view.search.NextGenSearchController.setUpDraggable;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.VIEW_PROPERTIES;

// Unfortunately we cannot have the generic type defined in the ListCell (the item needs to be of type Object).
// There seems to be a bug in JavaFX where after you change the Cell Factory, the now defunct Cells still hang
// on and listen for changes in the ListView items. So we always need to check the type of the item passed in
// to updateItem. We need to check its type to make sure the current ListView item being passed in still applies
// to the cell
public class SearchCellTopComponent extends SearchCellBase {
    private static final Logger LOG = LoggerFactory.getLogger(SearchCellTopComponent.class);
    public static final String SORT_CONCEPT_RESULT_CONCEPT_FXML = "search-result-concept-entry.fxml";

    private final SortResultConceptEntryController controller;
    private final Pane parentPane;
    /**
     * Supplies the current text query so the concept title can be highlighted
     * against the still-current query each time {@link #updateItem} runs. Read
     * via supplier (not captured at construction) because the cell instance
     * outlives a single search — the {@link javafx.scene.control.ListView}
     * virtual flow reuses cells across repeated searches.
     */
    private final Supplier<String> currentQueryTextSupplier;

    public SearchCellTopComponent(ViewProperties viewProperties, UUID journalTopic,
                                  ObservableViewNoOverride observableViewNoOverride,
                                  Supplier<String> currentQueryTextSupplier) {
        super(viewProperties, journalTopic, observableViewNoOverride);
        this.currentQueryTextSupplier = currentQueryTextSupplier;

        Config config = new Config(SortResultConceptEntryController.class.getResource(SORT_CONCEPT_RESULT_CONCEPT_FXML));
        config.updateViewModel("searchEntryViewModel", (searchEntryViewModel) ->
                searchEntryViewModel
                        .addProperty(VIEW_PROPERTIES, viewProperties)
                        .addProperty(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic)
        );

        JFXNode<Pane, SortResultConceptEntryController> searchConceptEntryJFXNode = FXMLMvvmLoader.make(config);

        parentPane = searchConceptEntryJFXNode.node();
        controller = searchConceptEntryJFXNode.controller();

        VBox.setMargin(parentPane, new Insets(8, 0, 8, 0));

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void onPopulateConcept(ActionEvent actionEvent) {
        controller.populateConcept(actionEvent);
    }

    @Override
    protected void onOpenInConceptNavigator(ActionEvent actionEvent) {
        controller.openInConceptNavigator(actionEvent);
    }

    @Override
    protected void onOpenAsKLWindow(ActionEvent actionEvent, String windowTitle) {
        controller.openAsKLWindow(actionEvent, windowTitle);
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setGraphic(null);
        } else {
            if (item instanceof Map.Entry) {
                Map.Entry<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>> mapEntry = (Map.Entry<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>>) item;
                SearchPanelController.NidTextRecord nidTextRecord = mapEntry.getKey();

                int topNid = nidTextRecord.nid();
                String topText = viewProperties.nodeView().calculator().getDescriptionTextOrNid(topNid);
                Latest<EntityVersion> latestTopVersion = viewProperties.nodeView().calculator().latest(topNid);
                if (latestTopVersion.isPresent()) {
                    EntityVersion entityVersion = latestTopVersion.get();

                    controller.setIdenticon(Identicon.generateIdenticonImage(entityVersion.publicId()));
                    controller.setWindowView(observableViewNoOverride);
                    Entity entity = Entity.get(entityVersion.nid()).get();
                    controller.setData(entity);
                    controller.setComponentText(highlightedTitle(topText));

                    controller.getDescriptionListViewItems().setAll(mapEntry.getValue());

                    if (entityVersion.active()) {
                        controller.getRetiredHBox().getChildren().remove(controller.getRetiredLabel());
                    } else if (!controller.getRetiredHBox().getChildren().contains(controller.getRetiredLabel())) {
                        // if the top component is inactive and the retired label is missing (because the cell factory is reused),
                        // then put the retired label back in place
                        controller.getRetiredHBox().getChildren().add(1, controller.getRetiredLabel());
                    }
                    controller.setRetired(!entityVersion.active());

                    setUpDraggable(parentPane, entity, CONCEPT);

                    setGraphic(parentPane);
                } else if (!nidTextRecord.publicIds().isEmpty()) {
                    // Remote-backed result: no local entity, render using data carried in NidTextRecord
                    UUID[] uuids = nidTextRecord.publicIds().toArray(new UUID[0]);
                    controller.setIdenticon(Identicon.generateIdenticonImage(PublicIds.of(uuids)));
                    controller.setWindowView(observableViewNoOverride);
                    controller.setData(null);
                    controller.setRemotePublicIds(nidTextRecord.publicIds());
                    controller.setComponentText(nidTextRecord.text());
                    controller.getDescriptionListViewItems().setAll(mapEntry.getValue());
                    if (nidTextRecord.active()) {
                        controller.getRetiredHBox().getChildren().remove(controller.getRetiredLabel());
                    } else if (!controller.getRetiredHBox().getChildren().contains(controller.getRetiredLabel())) {
                        controller.getRetiredHBox().getChildren().add(1, controller.getRetiredLabel());
                    }
                    controller.setRetired(!nidTextRecord.active());
                    setGraphic(parentPane);
                } else {
                    setGraphic(null);
                }
            }
        }
    }

    /**
     * Mark up the concept title with the same {@code <B>...</B>} convention
     * Lucene's highlighter uses for description-semantic snippets, so the
     * shared per-word renderer can paint matched terms with the same green
     * background. Bare-text fallbacks (no current query, or a parser error)
     * leave the title unmarked — the renderer treats that as plain text.
     */
    private String highlightedTitle(String topText) {
        String query = currentQueryTextSupplier == null ? "" : currentQueryTextSupplier.get();
        if (query == null || query.isEmpty() || topText == null || topText.isEmpty()) {
            return topText;
        }
        try {
            return viewProperties.nodeView().calculator().highlight(query, topText);
        } catch (Exception e) {
            LOG.debug("Title highlight failed for query='{}', falling back to plain text", query, e);
            return topText;
        }
    }
}
