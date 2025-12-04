package dev.ikm.komet.kview.mvvm.view.navigation;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.dnd.DragImageMaker;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.model.DragAndDropInfo;
import dev.ikm.komet.kview.mvvm.model.DragAndDropType;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Function;

import static dev.ikm.komet.kview.mvvm.view.common.PatternConstants.*;

/**
 * A ListCell for the Pattern Semantic.
 */
public class PatternSemanticListCell extends ListCell<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(PatternSemanticListCell.class);

    private Function<Integer, String> fetchDescriptionByNid;
    private Function<EntityFacade, String> fetchDescriptionByFacade;
    private ViewProperties viewProperties;

    private HBox hbox;
    private Label label;
    private SemanticTooltip tooltip;

    private SemanticEntity<?> currentSemanticEntity;
    private String currentSemanticTitle;

    public PatternSemanticListCell(Function<Integer, String> fetchDescriptionByNid,
                                   ViewProperties viewProperties) {

        this.fetchDescriptionByNid = fetchDescriptionByNid;
        this.viewProperties = viewProperties;

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        label = new Label();
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);

        tooltip = new SemanticTooltip(viewProperties);
        tooltip.setOnShowing(windowEvent -> {
            tooltip.update(currentSemanticEntity, currentSemanticTitle);
        });

        label.setTooltip(tooltip);

        hbox = new HBox();
        hbox.getStyleClass().add("pattern-instance-hbox");
        hbox.getChildren().add(label);
        StackPane stackPane = new StackPane();
        hbox.getChildren().add(stackPane);
        stackPane.getStyleClass().add("pattern-instance-hover-icon");
        label.getStyleClass().add("pattern-instance");
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        //setGraphic(null);
        if (item != null && !empty) {
            if (item instanceof String stringItem) {
                setContentDisplay(ContentDisplay.TEXT_ONLY);
                setText(stringItem);
            } else if (item instanceof Integer nid) {
                String entityDescriptionText = fetchDescriptionByNid.apply(nid);
                EntityFacade entity = Entity.getFast(nid);
                currentSemanticEntity = (SemanticEntity<?>) entity;
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                currentSemanticTitle = entityDescriptionText;
                label.setText(currentSemanticTitle);

                if (!entityDescriptionText.isEmpty()) {
                    Image identicon = Identicon.generateIdenticonImage(entity.publicId());
                    ImageView imageView = new ImageView(identicon);
                    imageView.setFitWidth(16);
                    imageView.setFitHeight(16);
                    label.setGraphic(imageView);
                }

                setGraphic(hbox);
                // make ListCell (row) draggable to the desktop
                setUpDraggable(hbox, entity, DragAndDropType.SEMANTIC);
            }
        } else {
            setGraphic(null);
        }
    }

    private void setUpDraggable(Node node, EntityFacade entity, DragAndDropType dropType) {
        Objects.requireNonNull(node, "The node must not be null.");
        Objects.requireNonNull(entity, "The entity must not be null.");

        // Associate the node with the entity's public ID and type for later retrieval or identification
        node.setUserData(new DragAndDropInfo(dropType, entity.publicId()));

        // Set up the drag detection event handler
        node.setOnDragDetected(mouseEvent -> {
            // Initiate a drag-and-drop gesture with copy or move transfer mode
            Dragboard dragboard = node.startDragAndDrop(TransferMode.COPY_OR_MOVE);

            // Create the content to be placed on the dragboard
            // Here, KometClipboard is used to encapsulate the entity's unique identifier (nid)
            KometClipboard content = new KometClipboard(EntityFacade.make(entity.nid()));

            // Generate the drag image using DragImageMaker
            DragImageMaker dragImageMaker = new DragImageMaker(node);
            Image dragImage = dragImageMaker.getDragImage();
            // Set the drag image on the dragboard
            if (dragImage != null) {
                dragboard.setDragView(dragImage);
            }

            // Place the content on the dragboard
            dragboard.setContent(content);

            // Log the drag event details for debugging or auditing
            LOG.info("Drag detected on node: " + mouseEvent.toString());

            // Consume the mouse event to prevent further processing
            mouseEvent.consume();
        });
    }

}
