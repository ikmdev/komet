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
import javafx.scene.control.Tooltip;
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
    private Tooltip tooltip;

    private PatternSemanticListCell() {
        // hide default
    }

    public PatternSemanticListCell(Function<Integer, String> fetchDescriptionByNid,
                                   Function<EntityFacade, String> fetchDescriptionByFacade,
                                   ViewProperties viewProperties) {

        this.fetchDescriptionByNid = fetchDescriptionByNid;
        this.fetchDescriptionByFacade = fetchDescriptionByFacade;
        this.viewProperties = viewProperties;

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        label = new Label();
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);

        tooltip = new Tooltip();
        Tooltip.install(label, tooltip);

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
                if (entity instanceof SemanticEntity<?> semanticEntity) {
                    if (semanticEntity.patternNid() == IDENTIFIER_PATTERN_PROXY.nid()) {
                        //TODO Move better string descriptions to language calculator
                        Latest<? extends SemanticEntityVersion> latestId = viewProperties.calculator().latest(semanticEntity);
                        ImmutableList fields = latestId.get().fieldValues();
                        entityDescriptionText = fetchDescriptionByFacade.apply((EntityFacade) fields.get(0)) +
                                ": " + fields.get(1);
                    } else if (semanticEntity.patternNid() == INFERRED_DEFINITION_PATTERN_PROXY.nid()) {
                        entityDescriptionText =
                                "Inferred definition for: " + fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == INFERRED_NAVIGATION_PATTERN_PROXY.nid()) {
                        entityDescriptionText =
                                "Inferred is-a relationships for: " + fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == PATH_MEMBERSHIP_PROXY.nid()) {
                        entityDescriptionText =
                                fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == STATED_DEFINITION_PATTERN_PROXY.nid()) {
                        entityDescriptionText =
                                "Stated definition for: " + fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == STATED_NAVIGATION_PATTERN_PROXY.nid()) {
                        entityDescriptionText =
                                "Stated is-a relationships for: " + fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == UK_DIALECT_PATTERN_PROXY.nid()) {
                        Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                        ImmutableList fields = latestAcceptability.get().fieldValues();
                        entityDescriptionText =
                                "UK dialect " + fetchDescriptionByFacade.apply((EntityFacade) fields.get(0)) +
                                        ": " + fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == US_DIALECT_PATTERN_PROXY.nid()) {
                        Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                        ImmutableList fields = latestAcceptability.get().fieldValues();
                        entityDescriptionText =
                                "US dialect " + fetchDescriptionByFacade.apply((EntityFacade) fields.get(0)) +
                                        ": " + fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid());
                    } else if (semanticEntity.patternNid() == VERSION_CONTROL_PATH_ORIGIN_PATTERN_PROXY.nid()) {
                        Latest<? extends SemanticEntityVersion> latestPathOrigins = viewProperties.calculator().latest(semanticEntity);
                        ImmutableList fields = latestPathOrigins.get().fieldValues();
                        entityDescriptionText =
                                fetchDescriptionByNid.apply(semanticEntity.referencedComponentNid()) +
                                        " origin: " + DateTimeUtil.format((Instant) fields.get(1)) +
                                        " on " + fetchDescriptionByFacade.apply((EntityFacade) fields.get(0));
                    }
                }

                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                label.setText(entityDescriptionText);
                tooltip.setText(entityDescriptionText);

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
