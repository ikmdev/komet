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

public class SearchCellDescriptionSemantic extends ListCell<LatestVersionSearchResult> {
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
    protected void updateItem(LatestVersionSearchResult item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setGraphic(null);
        } else {
            SemanticEntityVersion semantic = item.latestVersion().get();

            controller.setIdenticon(Identicon.generateIdenticonImage(semantic.publicId()));
            controller.setSemanticText(formatHighlightedString(item.highlightedString()));
            Entity entity = Entity.getConceptForSemantic(semantic.nid()).get();
            controller.setData((ConceptEntity) entity);
            if (semantic.active()) {
                controller.getRetiredHBox().getChildren().remove(controller.getRetiredLabel());
                controller.increaseTextFlowWidth();
            }
            controller.setWindowView(observableViewNoOverride);

            VBox.setMargin(content, new Insets(2, 0, 2, 0));

            setUpDraggable(content, entity, getDragAndDropType(entity));

            setGraphic(content);
        }
    }

    private String formatHighlightedString(String highlightedString) {
        String string = (highlightedString == null) ? "" : highlightedString;
        return string.replaceAll("<B>", "")
                .replaceAll("</B>", "")
                .replaceAll("\\s+", " ");
    }
}