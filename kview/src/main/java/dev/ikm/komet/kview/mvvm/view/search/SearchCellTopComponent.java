package dev.ikm.komet.kview.mvvm.view.search;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.model.DragAndDropType.CONCEPT;
import static dev.ikm.komet.kview.mvvm.view.search.NextGenSearchController.setUpDraggable;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;

// Unfortunately we cannot have the generic type defined in the ListCell (the item needs to be of type Object).
// There seems to be a bug in JavaFX where after you change the Cell Factory, the now defunct Cells still hang
// on and listen for changes in the ListView items. So we always need to check the type of the item passed in
// to updateItem. We need to check its type to make sure the current ListView item being passed in still applies
// to the cell
public class SearchCellTopComponent extends ListCell {

    public static final String SORT_CONCEPT_RESULT_CONCEPT_FXML = "search-result-concept-entry.fxml";

    private final ViewProperties viewProperties;
    private final ObservableViewNoOverride observableViewNoOverride;

    private final SortResultConceptEntryController controller;
    private final Pane parentPane;

    public SearchCellTopComponent(ViewProperties viewProperties, UUID jornalTopic, ObservableViewNoOverride observableViewNoOverride) {
        this.viewProperties = viewProperties;
        this.observableViewNoOverride = observableViewNoOverride;

        Config config = new Config(SortResultConceptEntryController.class.getResource(SORT_CONCEPT_RESULT_CONCEPT_FXML));
        config.updateViewModel("searchEntryViewModel", (searchEntryViewModel) ->
                searchEntryViewModel
                        .addProperty(VIEW_PROPERTIES, viewProperties)
                        .addProperty(CURRENT_JOURNAL_WINDOW_TOPIC, jornalTopic)
        );

        JFXNode<Pane, SortResultConceptEntryController> searchConceptEntryJFXNode = FXMLMvvmLoader.make(config);

        parentPane = searchConceptEntryJFXNode.node();
        controller = searchConceptEntryJFXNode.controller();

        VBox.setMargin(parentPane, new Insets(8, 0, 8, 0));

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setGraphic(null);
        } else {
            if (item instanceof Map.Entry) {
                Map.Entry<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>> mapEntry = (Map.Entry<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>>) item;

                int topNid = mapEntry.getKey().nid();
                String topText = viewProperties.nodeView().calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(topNid); // top text I assume is the title text
                Latest<EntityVersion> latestTopVersion = viewProperties.nodeView().calculator().latest(topNid);
                if (latestTopVersion.isPresent()) {
                    EntityVersion entityVersion = latestTopVersion.get();

                    controller.setIdenticon(Identicon.generateIdenticonImage(entityVersion.publicId()));
                    controller.setWindowView(observableViewNoOverride);
                    Entity entity = Entity.get(entityVersion.nid()).get();
                    controller.setData(entity);
                    controller.setComponentText(topText);

                    controller.getDescriptionListViewItems().setAll(mapEntry.getValue());

                    if (entityVersion.active()) {
                        controller.getRetiredHBox().getChildren().remove(controller.getRetiredLabel());
                    }
                    controller.setRetired(!entityVersion.active());

                    setUpDraggable(parentPane, entity, CONCEPT);

                    setGraphic(parentPane);
                } else {
                    setGraphic(null);
                }
            }
        }
    }
}
