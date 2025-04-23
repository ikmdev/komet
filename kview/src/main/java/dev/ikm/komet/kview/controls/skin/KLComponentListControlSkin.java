package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentListControl;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Line;
import javafx.util.Subscription;
import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Default skin implementation for the {@link KLComponentListControl} control
 */
public class KLComponentListControlSkin<T extends IntIdCollection> extends SkinBase<KLComponentListControl<T>> {

    private static final Logger LOG = LoggerFactory.getLogger(KLComponentListControlSkin.class);

    private final static double SPACE_BETWEEN_COMPONENTS = 10;
    private final static double SPACE_BETWEEN_NUMBER_AND_CONTROL = 5;

    private final Label titleLabel;
    private final Button addEntryButton;

    /**
     * This is the list of Components being rendered. The components with their nids will always be in the same
     * order as the nids in the value property (intIdList).
     * The last component is an aditional empty component with nid 0 that doesn't exist in the value property.
     */
    private final List<KLComponentControl> componentControls = new ArrayList<>();

    private final HashMap<KLComponentControl, Label> componentControlToNumberGraphic = new HashMap<>();

    private final HashMap<KLComponentControl, Line> componentToDropLine = new HashMap<>();

    private Line currentDropLine;
    private Line previousDropLine;

    private int currentDropIndex = -1;

    /**
     * Creates a new KLComponentListControlSkin instance, installing the necessary child
     * nodes into the Control {@link javafx.scene.control.Control#getChildrenUnmodifiable() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public KLComponentListControlSkin(KLComponentListControl control) {
        super(control);

        // Title
        titleLabel = new Label();
        titleLabel.getStyleClass().add("title-label");
        titleLabel.textProperty().bind(control.titleProperty());

        // Create component controls based on this control's values
        control.getValue().forEach(nid -> {
            if (nid != 0) {
                EntityProxy entityProxy = EntityProxy.make(nid);
                createComponentUI(entityProxy.nid());
            }
        });

        // Add entry button
        addEntryButton = new Button(getString("add.entry.button.text"));
        addEntryButton.getStyleClass().add("add-entry-button");
        addEntryButton.setOnAction(event -> createComponentUI(0));
        addEntryButton.setDisable(true);

        getChildren().addAll(titleLabel, addEntryButton);

        createComponentUI(0);

        getSkinnable().setOnMouseDragReleased(Event::consume);

        // Drag and Drop
        control.addEventFilter(DragEvent.DRAG_OVER, this::onDragOver);
        control.addEventHandler(DragEvent.DRAG_DROPPED, this::onDragDropped);
        control.addEventHandler(DragEvent.DRAG_EXITED, this::onDragExited);
    }

    private void onDragExited(DragEvent dragEvent) {
        if (previousDropLine != null) {
            previousDropLine.setVisible(false);
        }
    }

    private void onDragOver(DragEvent dragEvent) {
        if (dragEvent.getDragboard().hasContent(KLComponentControlSkin.COMPONENT_CONTROL_DRAG_FORMAT)) {
            dragEvent.acceptTransferModes(TransferMode.MOVE);
        }

        updateDropTargetLocation(dragEvent);
    }

    private void updateDropTargetLocation(DragEvent dragEvent) {
        double y = dragEvent.getY();

        for (KLComponentControl componentControl : componentControls) {
            double componentExtendedBoundsLowerY = componentControl.getLayoutY() - SPACE_BETWEEN_COMPONENTS;
            double componentExtendedBoundsUpperY = componentControl.getLayoutY() + componentControl.getHeight();

            if (y >= componentExtendedBoundsLowerY && y <= componentExtendedBoundsUpperY) {
                int componentIndex = componentControls.indexOf(componentControl);

                currentDropIndex = componentIndex;

                if (componentIndex >= 1) {
                    KLComponentControl previousComponentControl = componentControls.get(componentIndex - 1);

                    currentDropLine = componentToDropLine.get(previousComponentControl);
                    currentDropLine.setVisible(true);
                } else {
                    currentDropLine = null;
                }

                if (previousDropLine != null && previousDropLine != currentDropLine) {
                    previousDropLine.setVisible(false);
                }
                previousDropLine = currentDropLine;

                break;
            }
        }
    }

    private void onDragDropped(DragEvent dragEvent) {
        if (!(dragEvent.getGestureSource() instanceof KLComponentControl)) {
            return;
        }

        KLComponentControl componentControl = (KLComponentControl) dragEvent.getGestureSource();
        KLComponentListControl<T> control = getSkinnable();
        int componentNid = componentControl.getEntity().nid();

        IntIdCollection intIdList = control.getValue();
        MutableIntList mutableList = IntLists.mutable.of(intIdList.toArray());

        mutableList.remove(componentNid);
        mutableList.addAtIndex(currentDropIndex, componentNid);

        componentControls.remove(componentControl);
        componentControls.add(currentDropIndex, componentControl);

        setValueFromIntList(mutableList);

        if (currentDropLine != null) {
            currentDropLine.setVisible(false);
        }

        control.requestLayout();

        dragEvent.setDropCompleted(true);
        dragEvent.consume();
    }

    private void setValueFromIntList(MutableIntList mutableList) {
        KLComponentListControl<T> control = getSkinnable();

        if (control.getValue() instanceof IntIdList) {
            ((KLComponentListControl<IntIdList>)control).setValue(IntIds.list.of(mutableList.toArray()));
        } else if(control.getValue() instanceof IntIdSet) {
            ((KLComponentListControl<IntIdSet>)control).setValue(IntIds.set.of(mutableList.toArray()));
        }
    }

    /**
     * Create UI for a component with the given nid.
     * A nid of 0 means create a component that's empty.
     *
     * @param nid the nid that is going to be associated with the component
     */
    private void createComponentUI(int nid) {
        KLComponentListControl<T> control = getSkinnable();

        KLComponentControl componentControl = new KLComponentControl();
        if (nid != 0) {
            EntityProxy entityProxy = EntityProxy.make(nid);
            componentControl.setEntity(entityProxy);
        }

        componentControls.add(componentControl);

        Subscription subscription = componentControl.entityProperty().subscribe(entity -> {
            if (!componentControl.isEmpty()) {
                int oldNidIndex = componentControls.indexOf(componentControl);
                int newNid = entity.nid();

                if (oldNidIndex >= getSkinnable().getValue().size()) { // we're adding a new nid
                    IntIdCollection intIdCollection = control.getValue();
                    MutableIntList mutableList = IntLists.mutable.of(intIdCollection.toArray());
                    mutableList.add(newNid);
                    setValueFromIntList(mutableList);

                    // Component Control was empty so had no drop line we need to create one now
                    createDropLine(componentControl);

                    // Create new empty component at the bottom
                    createComponentUI(0);
                } else { // we're setting the control's valid nid to another nid
                    IntIdCollection intIdCollection = control.getValue();
                    MutableIntList mutableList = IntLists.mutable.of(intIdCollection.toArray());
                    mutableList.set(oldNidIndex, newNid);
                    setValueFromIntList(mutableList);
                }
            }
        });

        componentControl.setOnRemoveAction(ev -> removeComponentControl(componentControl, subscription));

        if (control.getValue() instanceof IntIdSet) {
            componentControl.setComponentAllowedFilter(componentPublicId
                    -> !control.getValue().contains(EntityService.get().nidForPublicId(componentPublicId)));
        }

        componentControl.showDragHandleProperty().bind(componentControl.hoverProperty());

        componentControl.addEventHandler(MouseEvent.DRAG_DETECTED, _ -> {
            componentControl.setVisible(false);
        });

        componentControl.addEventHandler(DragEvent.DRAG_DONE, _ -> {
            componentControl.setVisible(true);
        });

        Label numberLabel = createNumberLabel(componentControl);

        if (nid != 0) {
            createDropLine(componentControl);
        }

        getChildren().add(componentControl);
        getChildren().add(numberLabel);

        getSkinnable().requestLayout();
    }

    private void createDropLine(KLComponentControl componentControl) {
        Line dropLine = new Line();
        dropLine.getStyleClass().add("drop-line");
        componentToDropLine.put(componentControl, dropLine);
        dropLine.setVisible(false);

        getChildren().add(dropLine);
    }

    private Label createNumberLabel(KLComponentControl componentControl) {
        Label numberLabel = new Label();
        numberLabel.getStyleClass().add("number-label");
        componentControlToNumberGraphic.put(componentControl, numberLabel);
        return numberLabel;
    }

    private void removeComponentControl(KLComponentControl componentControl, Subscription subscription) {
        KLComponentListControl<T> control = getSkinnable();
        int nidToRemove = componentControl.getEntity().nid();

        subscription.unsubscribe();

        componentControls.remove(componentControl);

        getChildren().remove(componentControl);
        getChildren().remove(componentToDropLine.get(componentControl));
        getChildren().remove(componentControlToNumberGraphic.get(componentControl));

        componentToDropLine.remove(componentControl);
        componentControlToNumberGraphic.remove(componentControl);

        IntIdCollection intIdList = control.getValue();
        MutableIntList mutableList = IntLists.mutable.of(intIdList.toArray());
        mutableList.remove(nidToRemove);
        setValueFromIntList(mutableList);
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        super.dispose();
    }

    /** {@inheritDoc} */
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        Insets padding = getSkinnable().getPadding();
        double labelPrefWidth = titleLabel.prefWidth(-1);
        double labelPrefHeight = titleLabel.prefHeight(labelPrefWidth);
        double x = contentX + padding.getLeft();
        double y = contentY + padding.getTop();
        titleLabel.resizeRelocate(x, y, labelPrefWidth, labelPrefHeight);
        y += labelPrefHeight;

        int labelNumber = 1;
        int index = 0;
        for (; index < getSkinnable().getValue().size(); ++index) {
            double componentControlUsedHeight = layoutComponentControl(contentWidth, index, padding, x, y,
                                                                       labelNumber, contentX);
            // prepare for next iteration
            labelNumber += 1;
            y += componentControlUsedHeight + SPACE_BETWEEN_COMPONENTS;
        }
        // layout final empty component control
        double componentControlUsedHeight = layoutComponentControl(contentWidth, index, padding, x, y, labelNumber, contentX);
        y += componentControlUsedHeight + SPACE_BETWEEN_COMPONENTS;

        // layout add Entry button
        double buttonPrefWidth = addEntryButton.prefWidth(-1);
        addEntryButton.resizeRelocate(contentWidth - buttonPrefWidth - padding.getRight(), y,
                                      buttonPrefWidth, addEntryButton.prefHeight(buttonPrefWidth));
    }

    /**
     * Lays out an individual component control and also the drop line associated with it in case there is any.
     *
     * @param contentWidth the total width available for laying out this component control
     * @param index the index of the control to layout
     * @param padding the padding
     * @param componentStartX the start x of the layout of the component
     * @param componentStartY the start y of the layout of the component
     * @param labelNumber the number that should be visible on the number label
     * @param dropLineX the x start for the drop line
     *
     * @return the height that the component control occupies after it has been laid out
     */
    private double layoutComponentControl(double contentWidth, int index, Insets padding, double componentStartX,
                                          double componentStartY, int labelNumber, double dropLineX) {
        KLComponentControl componentControl = componentControls.get(index);

        Label numberLabel = componentControlToNumberGraphic.get(componentControl);
        double labelWidth = numberLabel.prefWidth(-1);
        double labelHeight = numberLabel.prefHeight(labelWidth);

        double componentControlPrefWidth = contentWidth - padding.getRight() - componentStartX - labelWidth - SPACE_BETWEEN_NUMBER_AND_CONTROL;
        double componentControlPrefHeight = componentControl.prefHeight(componentControlPrefWidth);

        // Layout number label
        double labelX = snapPositionX(componentStartX);
        double labelY = snapPositionY(componentStartY + componentControlPrefHeight / 2d - labelHeight / 2d);
        labelWidth = snapSizeX(labelWidth);
        numberLabel.resizeRelocate(labelX, labelY, labelWidth, snapSizeY(labelHeight));
        numberLabel.setText(String.valueOf(labelNumber));

        // Layout component Control
        componentStartX += labelX + labelWidth + SPACE_BETWEEN_NUMBER_AND_CONTROL;
        componentControl.resizeRelocate(componentStartX, componentStartY, componentControlPrefWidth, componentControlPrefHeight);

        // Layout drop line
        Line dropLine = componentToDropLine.get(componentControl);

        if (dropLine != null) {
            double dropLineWidth = componentControlPrefWidth + labelWidth;
            dropLine.setStartX(dropLineX);
            dropLine.setStartY(componentStartY + componentControlPrefHeight + SPACE_BETWEEN_COMPONENTS / 2d);
            dropLine.setEndX(dropLineX + dropLineWidth);
            dropLine.setEndY(componentStartY + componentControlPrefHeight + SPACE_BETWEEN_COMPONENTS / 2d + 2);
        }

        return componentControlPrefHeight;
    }

    private static String getString(String key) {
        return ResourceBundle.getBundle("dev.ikm.komet.kview.controls.component-list-control").getString(key);
    }
}
