package dev.ikm.komet.kview.mvvm.view.search;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

import static dev.ikm.komet.kview.mvvm.view.search.NextGenSearchController.getDragAndDropType;
import static dev.ikm.komet.kview.mvvm.view.search.NextGenSearchController.setUpDraggable;

// Unfortunately we cannot have the generic type defined in the ListCell (the item needs to be of type Object).
// There seems to be a bug in JavaFX where after you change the Cell Factory, the now defunct Cells still hang
// on and listen for changes in the ListView items. So we always need to check the type of the item passed in
// to updateItem. We need to check its type to make sure the current ListView item being passed in still applies
// to the cell
public class SearchCellDescriptionSemantic extends ListCell {
    public static final String SORT_SEMANTIC_RESULT_CONCEPT_FXML = "search-result-semantic-entry.fxml";

    private SortResultSemanticEntryController controller;
    private Node content;
    private ObservableViewNoOverride observableViewNoOverride;

    public SearchCellDescriptionSemantic(ObservableViewNoOverride observableViewNoOverride) {
        JFXNode<Pane, SortResultSemanticEntryController> searchSemanticEntryJFXNode = FXMLMvvmLoader
                .make(SortResultSemanticEntryController.class.getResource(SORT_SEMANTIC_RESULT_CONCEPT_FXML));

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
            if (item instanceof LatestVersionSearchResult latestVersionSearchResult) {
                SemanticEntityVersion semantic = latestVersionSearchResult.latestVersion().get();

                controller.setIdenticon(Identicon.generateIdenticonImage(semantic.publicId()));
                controller.setSemanticText(formatHighlightedString(latestVersionSearchResult.highlightedString()));
                controller.setWindowView(observableViewNoOverride);
                Entity entity = Entity.getConceptForSemantic(semantic.nid()).get();
                controller.setData(entity);
                if (semantic.active()) {
                    controller.getRetiredHBox().getChildren().remove(controller.getRetiredLabel());
                    controller.increaseTextFlowWidth();
                }

                VBox.setMargin(content, new Insets(2, 0, 2, 0));

                setUpDraggable(content, entity, getDragAndDropType(entity));

                setGraphic(content);
            }
        }
    }

    private String formatHighlightedString(String highlightedString) {
        String string = (highlightedString == null) ? "" : highlightedString;
        return string.replaceAll("<B>", "")
                .replaceAll("</B>", "")
                .replaceAll("\\s+", " ");
    }
}