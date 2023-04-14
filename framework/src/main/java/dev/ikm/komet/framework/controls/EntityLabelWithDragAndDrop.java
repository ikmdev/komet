package dev.ikm.komet.framework.controls;


import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Dragboard;
import javafx.stage.WindowEvent;
import org.controlsfx.property.editor.PropertyEditor;
import dev.ikm.komet.framework.MenuItemWithText;
import dev.ikm.komet.framework.PseudoClasses;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.context.AddToContextMenu;
import dev.ikm.komet.framework.context.AddToContextMenuSimple;
import dev.ikm.komet.framework.dnd.DragAndDropHelper;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.component.Pattern;
import dev.ikm.tinkar.component.Semantic;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.ProxyFactory;

import java.util.Optional;
import java.util.function.Function;

import static dev.ikm.komet.framework.PseudoClasses.INACTIVE_PSEUDO_CLASS;
import static dev.ikm.komet.framework.StyleClasses.CONCEPT_LABEL;
import static dev.ikm.komet.framework.StyleClasses.PROP_SHEET_ENTITY_LABEL;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * @author kec
 */
public class EntityLabelWithDragAndDrop
        extends Label implements PropertyEditor<EntityFacade>, Function<EntityFacade, String> {

    public static final String EMPTY_TEXT = "empty";
    final ViewProperties viewProperties;
    final ObjectProperty<EntityFacade> entityFocusProperty;
    final Function<EntityFacade, String> descriptionTextUpdater;
    final DragAndDropHelper dragAndDropHelper;
    final SimpleIntegerProperty selectionIndexProperty;
    final Runnable unlink;
    final AddToContextMenu[] contextMenuProviders;

    //~--- constructors --------------------------------------------------------
    private EntityLabelWithDragAndDrop(ViewProperties viewProperties,
                                       ObjectProperty<EntityFacade> entityFocusProperty,
                                       Function<EntityFacade, String> descriptionTextUpdater,
                                       SimpleIntegerProperty selectionIndexProperty,
                                       Runnable unlink,
                                       AddToContextMenu[] contextMenuProviders) {
        super(EMPTY_TEXT);
        setWrapText(true);
        if (descriptionTextUpdater == null) {
            this.descriptionTextUpdater = this;
        } else {
            this.descriptionTextUpdater = descriptionTextUpdater;
        }
        this.viewProperties = viewProperties;
        this.entityFocusProperty = entityFocusProperty;
        this.selectionIndexProperty = selectionIndexProperty;
        this.unlink = unlink;
        this.contextMenuProviders = contextMenuProviders;
        this.getStyleClass().add(CONCEPT_LABEL.toString());
        this.dragAndDropHelper = new DragAndDropHelper(this, () -> {
            Optional<EntityFacade> optionalConcept = Optional.ofNullable(entityFocusProperty.getValue());

            if (optionalConcept.isPresent()) {
                return optionalConcept.get();
            }
            return null;

        }, this::droppedValue, mouseEvent -> true,
                dragEvent -> true);


        this.setMinWidth(100);
        this.setMaxWidth(Double.MAX_VALUE);

        ContextMenu contextMenu = new ContextMenu();

        for (PublicIdStringKey<ActivityStream> activityFeedKey : ActivityStreams.KEYS) {
            MenuItem item = new MenuItemWithText(activityFeedKey.getString() + " history");
            contextMenu.getItems().add(item);
        }

        this.setContextMenu(contextMenu);
        contextMenu.setOnShowing(this::handle);
        setText(this.descriptionTextUpdater.apply(entityFocusProperty.get()));
        entityFocusProperty.addListener((observable, oldValue, newValue) -> {
            setText(this.descriptionTextUpdater.apply(newValue));
            setPseudoClasses();
        });
        Platform.runLater(() -> setPseudoClasses());

    }

    public static EntityLabelWithDragAndDrop make(ViewProperties viewProperties,
                                                  ObjectProperty<EntityFacade> entityFocusProperty) {

        SimpleIntegerProperty selectionIndexProperty = new SimpleIntegerProperty();
        Runnable unlink = () -> {
        };
        AddToContextMenu[] contextMenuProviders = new AddToContextMenu[]{new AddToContextMenuSimple()};
        Function<EntityFacade, String> descriptionTextUpdater = entityFacade -> {
            if (entityFacade == null) {
                return EMPTY_TEXT;
            }
            return viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(entityFacade);
        };

        EntityLabelWithDragAndDrop entityLabel = new EntityLabelWithDragAndDrop(viewProperties, entityFocusProperty,
                descriptionTextUpdater, selectionIndexProperty, unlink, contextMenuProviders);
        viewProperties.calculator().latest(entityFocusProperty.get()).ifPresentOrElse(entityVersion ->
                        entityLabel.pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, !entityVersion.active()),
                () -> entityLabel.pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, true));
        entityLabel.getStyleClass().remove(CONCEPT_LABEL.toString());
        entityLabel.getStyleClass().add(PROP_SHEET_ENTITY_LABEL.toString());
        return entityLabel;
    }

    public static EntityLabelWithDragAndDrop make(ViewProperties viewProperties,
                                                  ObjectProperty<EntityFacade> entityFocusProperty,
                                                  Function<EntityFacade, String> descriptionTextUpdater,
                                                  SimpleIntegerProperty selectionIndexProperty,
                                                  Runnable unlink,
                                                  AddToContextMenu[] contextMenuProviders) {
        return new EntityLabelWithDragAndDrop(viewProperties, entityFocusProperty, descriptionTextUpdater, selectionIndexProperty, unlink, contextMenuProviders);
    }

    public static EntityLabelWithDragAndDrop make(ViewProperties viewProperties,
                                                  ObjectProperty<EntityFacade> entityFocusProperty,
                                                  SimpleIntegerProperty selectionIndexProperty,
                                                  Runnable unlink,
                                                  AddToContextMenu[] contextMenuProviders) {
        return new EntityLabelWithDragAndDrop(viewProperties, entityFocusProperty, null, selectionIndexProperty, unlink, contextMenuProviders);
    }

    public static void setFullyQualifiedText(EntityLabelWithDragAndDrop label) {

        Optional<EntityFacade> optionalEntity = Optional.ofNullable(label.entityFocusProperty.getValue());
        if (optionalEntity.isPresent()) {
            label.setText(
                    label.viewProperties.nodeView().getFullyQualifiedNameTextOrNid(optionalEntity.get().nid())
            );
        } else {
            setLabelToEmptyText(label);
        }
    }
//~--- methods -------------------------------------------------------------

    private static void setLabelToEmptyText(Label label) {
        label.setText(EMPTY_TEXT);
    }

    public static void setPreferredText(EntityLabelWithDragAndDrop label) {
        Optional<EntityFacade> optionalConcept = Optional.ofNullable(label.entityFocusProperty.getValue());
        if (optionalConcept.isPresent()) {
            label.setText(
                    label.viewProperties.nodeView().getPreferredDescriptionStringOrNid(optionalConcept.get().nid())
            );
        } else {
            setLabelToEmptyText(label);
        }
    }

    void droppedValue(Dragboard dragboard) {
        this.unlink.run();
        if (dragboard.hasContent(KometClipboard.KOMET_CONCEPT_PROXY)) {
            setValue(ProxyFactory.fromXmlFragment((String) dragboard.getContent(KometClipboard.KOMET_CONCEPT_PROXY)));
        } else if (dragboard.hasContent(KometClipboard.KOMET_SEMANTIC_PROXY)) {
            setValue(ProxyFactory.fromXmlFragment((String) dragboard.getContent(KometClipboard.KOMET_SEMANTIC_PROXY)));
        } else if (dragboard.hasContent(KometClipboard.KOMET_PATTERN_PROXY)) {
            setValue(ProxyFactory.fromXmlFragment((String) dragboard.getContent(KometClipboard.KOMET_PATTERN_PROXY)));
        } else {
            setValue(null);
        }
    }

    private void handle(WindowEvent event) {
        ContextMenu contextMenu = (ContextMenu) event.getSource();
        contextMenu.getItems().clear();
        for (AddToContextMenu contextMenuProvider : contextMenuProviders) {
            contextMenuProvider.addToContextMenu(this, contextMenu, this.viewProperties,
                    this.entityFocusProperty, this.selectionIndexProperty, this.unlink);
        }

    }

    public void setEntity(EntityFacade entityFacade) {
        this.entityFocusProperty.set(entityFacade);
        setPseudoClasses();
    }

    private void setPseudoClasses() {
        EntityFacade entityFacade = getValue();
        // Get the actual entity to get accurate type...
        if (entityFacade != null) {
            entityFacade = Entity.getFast(entityFacade);
        }
        if (entityFacade == null) {
            this.pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, false);
            this.pseudoClassStateChanged(PseudoClasses.SEMANTIC_PSEUDO_CLASS, false);
            this.pseudoClassStateChanged(PseudoClasses.PATTERN_PSEUDO_CLASS, false);
        } else if (entityFacade instanceof Concept) {
            this.pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, true);
            this.pseudoClassStateChanged(PseudoClasses.SEMANTIC_PSEUDO_CLASS, false);
            this.pseudoClassStateChanged(PseudoClasses.PATTERN_PSEUDO_CLASS, false);
        } else if (entityFacade instanceof Pattern) {
            this.pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, false);
            this.pseudoClassStateChanged(PseudoClasses.SEMANTIC_PSEUDO_CLASS, false);
            this.pseudoClassStateChanged(PseudoClasses.PATTERN_PSEUDO_CLASS, true);
        } else if (entityFacade instanceof Semantic) {
            this.pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, false);
            this.pseudoClassStateChanged(PseudoClasses.SEMANTIC_PSEUDO_CLASS, false);
            this.pseudoClassStateChanged(PseudoClasses.PATTERN_PSEUDO_CLASS, true);
        }
    }

    @Override
    public String apply(EntityFacade entityFacade) {
        if (this.entityFocusProperty.get() == null) {
            return EMPTY_TEXT;
        } else {
            return this.viewProperties.nodeView().calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(this.entityFocusProperty.getValue());
        }
    }

    //~--- set methods ---------------------------------------------------------
    private void setDescriptionText(String latestDescriptionText) {
        this.setText(latestDescriptionText);
    }

    private void setEmptyText() {
        setLabelToEmptyText(this);
    }

    @Override
    public Node getEditor() {
        return this;
    }

    @Override
    public EntityFacade getValue() {
        return this.entityFocusProperty.getValue();
    }

    @Override
    public void setValue(EntityFacade value) {
        this.setEntity(value);
    }


}
