package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentListControl;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Line;
import javafx.util.Subscription;
import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Default skin implementation for the {@link KLComponentListControl} control
 */
public class KLComponentListControlSkin extends SkinBase<KLComponentListControl> {

    private static final Logger LOG = LoggerFactory.getLogger(KLComponentListControlSkin.class);

    private final static double SPACE_BETWEEN_COMPONENTS = 10;
    private final static double spacingBetweenNumberAndControl = 5;

    private final Label titleLabel;
    private final Button addEntryButton;

    private final HashMap<Integer, KLComponentControl> nidToComponentControl = new HashMap<>();
    private final HashMap<KLComponentControl, Integer> componentControlToNid = new HashMap<>();

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

        // Create controls based on control's intDList values
        for(int i = 0; i < control.getValue().size(); i++) {
            int nid = control.getValue().get(i);
            if (nid != 0) {
                EntityProxy entityProxy = EntityProxy.make(nid);
                createComponentUI(entityProxy.nid());
            }
        }

        // Add entry button
        addEntryButton = new Button(getString("add.entry.button.text"));
        addEntryButton.getStyleClass().add("add-entry-button");
        addEntryButton.setOnAction(event -> createComponentUI(0));

        getChildren().addAll(titleLabel, addEntryButton);

        createComponentUI(0);
        // Only allow one empty KLComponentControl
        addEntryButton
                .disableProperty()
                .bind(Bindings.createBooleanBinding(() ->
                        getChildren()
                                .stream()
                                .anyMatch(n -> n instanceof KLComponentControl cc && cc.getEntity() == null),
                        getChildren(),
                        control.valueProperty()));

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
            dragEvent.acceptTransferModes(TransferMode.COPY);
        }

        updateDropTargetLocation(dragEvent);
    }

    private void updateDropTargetLocation(DragEvent dragEvent) {
        KLComponentListControl control = getSkinnable();

        double y = dragEvent.getY();

        for (KLComponentControl componentControl : componentControlToNid.keySet()) {
            double componentExtendedBoundsLowerY = componentControl.getLayoutY() - SPACE_BETWEEN_COMPONENTS;
            double componentExtendedBoundsUpperY = componentControl.getLayoutY() + componentControl.getHeight();

            if (y >= componentExtendedBoundsLowerY && y <= componentExtendedBoundsUpperY) {
                IntIdList intIdList = control.getValue();
                MutableIntList mutableList = IntLists.mutable.of(intIdList.toArray());

                int componentIndex = mutableList.indexOf(componentControlToNid.get(componentControl));

                currentDropIndex = componentIndex;

                if (componentIndex >= 1) {
                    int previousNid = mutableList.get(componentIndex - 1);

                    KLComponentControl previousComponentControl = nidToComponentControl.get(previousNid);

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
        KLComponentListControl control = getSkinnable();
        int componentNid = componentControl.getEntity().nid();

        IntIdList intIdList = control.getValue();
        MutableIntList mutableList = IntLists.mutable.of(intIdList.toArray());

        mutableList.remove(componentNid);
        mutableList.addAtIndex(currentDropIndex, componentNid);

        control.setValue(IntIds.list.of(mutableList.toArray()));

        currentDropLine.setVisible(false);

        control.requestLayout();

        dragEvent.setDropCompleted(true);
        dragEvent.consume();
    }

    /**
     * Create UI for a component with the given nid.
     * A nid of 0 means create a component that's empty.
     *
     * @param nid the nid that is going to be associated with the component
     */
    private void createComponentUI(int nid) {
        KLComponentListControl control = getSkinnable();

        KLComponentControl componentControl = new KLComponentControl();
        if (nid != 0) {
            EntityProxy entityProxy = EntityProxy.make(nid);
            componentControl.setEntity(entityProxy);
        }

        // update Nid to control hashmaps
        nidToComponentControl.put(nid, componentControl);
        componentControlToNid.put(componentControl, nid);

        Subscription subscription = componentControl.entityProperty().subscribe(entity -> {
            if (entity != null) {
                int oldNid = componentControlToNid.get(componentControl);
                int newNid = entity.nid();

                if (oldNid == newNid) {
                    return;
                }

                if (oldNid == 0) { // we're adding a new nid
                    IntIdList intIdList = control.getValue();
                    MutableIntList mutableList = IntLists.mutable.of(intIdList.toArray());
                    mutableList.add(entity.nid());
                    control.setValue(IntIds.list.of(mutableList.toArray()));

                    // Component Control was empty so had no drop line we need to create one now
                    createDropLineForComponent(componentControl);

                } else { // we're setting the control's valid nid to another nid
                    IntIdList intIdList = control.getValue();
                    MutableIntList mutableList = IntLists.mutable.of(intIdList.toArray());
                    int index = mutableList.indexOf(oldNid);

                    mutableList.set(index, newNid);
                    control.setValue(IntIds.list.of(mutableList.toArray()));

                }

                // update Nid to control hashmaps
                nidToComponentControl.put(newNid, componentControl);
                componentControlToNid.put(componentControl, newNid);

                nidToComponentControl.remove(oldNid);

                if (oldNid == 0) {
                    createComponentUI(0);
                }
            }
        });

        componentControl.setOnRemoveAction(ev -> removeComponentControl(componentControl, subscription));

        Label numberLabel = createNumberLabel(componentControl);

        if (nid != 0) {
            createDropLineForComponent(componentControl);
        }

        getChildren().add(componentControl);
        getChildren().add(numberLabel);

        getSkinnable().requestLayout();
    }

    private void createDropLineForComponent(KLComponentControl componentControl) {
        Line dropLine = new Line();
        dropLine.getStyleClass().add("drop-line");
        componentToDropLine.put(componentControl, dropLine);
        dropLine.setVisible(false);

        getChildren().add(dropLine);
    }

    private void removeComponentControl(KLComponentControl componentControl, Subscription subscription) {
        KLComponentListControl control = getSkinnable();
        int nidToRemove = componentControl.getEntity().nid();

        subscription.unsubscribe();

        componentControlToNid.remove(componentControl);
        nidToComponentControl.remove(nidToRemove);

        getChildren().remove(componentControl);
        getChildren().remove(componentToDropLine.get(componentControl));
        getChildren().remove(componentControlToNumberGraphic.get(componentControl));

        componentToDropLine.remove(componentControl);
        componentControlToNumberGraphic.remove(componentControl);

        IntIdList intIdList = control.getValue();
        MutableIntList mutableList = IntLists.mutable.of(intIdList.toArray());
        mutableList.remove(nidToRemove);
        control.setValue(IntIds.list.of(mutableList.toArray()));
    }

    private Label createNumberLabel(KLComponentControl componentControl) {
        Label numberLabel = new Label();
        numberLabel.getStyleClass().add("number-label");
        componentControlToNumberGraphic.put(componentControl, numberLabel);
        return numberLabel;
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
        for (int nid : getSkinnable().getValue().toArray()) {
            double componentControlUsedHeight = layoutComponentControl(contentWidth, nid, padding, x, y,
                                                                       labelNumber, contentX);
            // prepare for next iteration
            labelNumber += 1;
            y += componentControlUsedHeight + SPACE_BETWEEN_COMPONENTS;
        }
        // layout final empty component control
        double componentControlUsedHeight = layoutComponentControl(contentWidth, 0, padding, x, y, labelNumber, contentX);
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
     * @param nid the nid associated with this component control. 0 means it has no nid and so it's empty
     * @param padding the padding
     * @param componentStartX the start x of the layout of the component
     * @param componentStartY the start y of the layout of the component
     * @param labelNumber the number that should be visible on the number label
     * @param dropLineX the x start for the drop line
     *
     * @return the height that the component control occupies after it has been laid out
     */
    private double layoutComponentControl(double contentWidth, int nid, Insets padding, double componentStartX,
                                          double componentStartY, int labelNumber, double dropLineX) {
        KLComponentControl componentControl = nidToComponentControl.get(nid);

        Label numberLabel = componentControlToNumberGraphic.get(componentControl);
        double labelWidth = numberLabel.prefWidth(-1);
        double labelHeight = numberLabel.prefHeight(labelWidth);

        double componentControlPrefWidth = contentWidth - padding.getRight() - componentStartX - labelWidth - spacingBetweenNumberAndControl;
        double componentControlPrefHeight = componentControl.prefHeight(componentControlPrefWidth);

        // Layout number label
        double labelX = snapPositionX(componentStartX);
        double labelY = snapPositionY(componentStartY + componentControlPrefHeight / 2d - labelHeight / 2d);
        labelWidth = snapSizeX(labelWidth);
        numberLabel.resizeRelocate(labelX, labelY, labelWidth, snapSizeY(labelHeight));
        numberLabel.setText(String.valueOf(labelNumber));

        // Layout component Control
        componentStartX += labelX + labelWidth + spacingBetweenNumberAndControl;
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
