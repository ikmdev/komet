package dev.ikm.komet.kview.mvvm.view.search;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.view.search.NextGenSearchController.getDragAndDropType;
import static dev.ikm.komet.kview.mvvm.view.search.NextGenSearchController.setUpDraggable;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;

// Unfortunately, we cannot have the generic type defined in the ListCell (the item needs to be of type Object).
// There seems to be a bug in JavaFX where after you change the Cell Factory, the now defunct Cells still hang
// on and listen for changes in the ListView items. So we always need to check the type of the item passed in
// to updateItem. We need to check its type to make sure the current ListView item being passed in still applies
// to the cell
public class SearchCellNid extends ListCell {

    public static final String SORT_SEMANTIC_RESULT_CONCEPT_FXML = "search-result-semantic-entry.fxml";

    private SortResultSemanticEntryController controller;
    private Node content;
    private ObservableViewNoOverride observableViewNoOverride;
    private ViewProperties viewProperties;

    public SearchCellNid(ViewProperties viewProperties, ObservableViewNoOverride observableViewNoOverride, UUID journalTopic) {
        Config config = new Config(SortResultSemanticEntryController.class.getResource(SORT_SEMANTIC_RESULT_CONCEPT_FXML))
                .updateViewModel("searchEntryViewModel", (viewModel -> viewModel
                        .setPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic)
                ));

        JFXNode<Pane, SortResultSemanticEntryController> searchSemanticEntryJFXNode = FXMLMvvmLoader.make(config);

        this.viewProperties = viewProperties;
        this.observableViewNoOverride = observableViewNoOverride;

        content = searchSemanticEntryJFXNode.node();
        controller = searchSemanticEntryJFXNode.controller();
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setGraphic(null);
        } else {
            if (item instanceof Integer nid) {
                String topText = viewProperties.nodeView().calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(nid);
                Latest<EntityVersion> latestTopVersion = viewProperties.nodeView().calculator().latest(nid);

                latestTopVersion.ifPresentOrElse(entityVersion -> {
                    controller.setIdenticon(Identicon.generateIdenticonImage(entityVersion.publicId()));
                    controller.setSemanticText(topText);
                    controller.setWindowView(observableViewNoOverride);
                    Entity entity = Entity.get(entityVersion.nid()).get();
                    controller.setData(entity);
                    if (entityVersion.active()) {
                        controller.getRetiredHBox().getChildren().remove(controller.getRetiredLabel());
                    }
                    VBox.setMargin(content, new Insets(2, 0, 2, 0));

                    setUpDraggable(content, entity, getDragAndDropType(entity));

                    setGraphic(content);
                }, () -> {
                    setGraphic(null);
                });
            }
        }
    }
}