package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentCollectionControl;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.css.PseudoClass;
import javafx.event.Event;
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
 * Default skin implementation for the {@link KLComponentCollectionControl} control
 */
public class KLComponentCollectionControlSkin<T extends IntIdCollection> extends SkinBase<KLComponentCollectionControl<T>> {

    private static final Logger LOG = LoggerFactory.getLogger(KLComponentCollectionControlSkin.class);

    private static final PseudoClass DRAGGING_TO_SAME = PseudoClass.getPseudoClass("dragging-to-same");

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

    private final HashMap<KLComponentControl, Subscription> componentControlToSubscription = new HashMap<>();
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
    public KLComponentCollectionControlSkin(KLComponentCollectionControl<T> control) {
        super(control);
        control.setFocusTraversable(false);

        // Title
        titleLabel = new Label();
        titleLabel.getStyleClass().add("editable-title-label");
        titleLabel.textProperty().bind(control.titleProperty());

        createDropLine(null);

        // Init Components in Component List and wire up listener
        resetUI();
        control.valueProperty().subscribe(this::resetUI);

        // Add entry button
        addEntryButton = new Button(getString("add.entry.button.text"));
        addEntryButton.getStyleClass().add("add-entry-button");
        addEntryButton.setOnAction(event -> createComponentUI(0));
        addEntryButton.setDisable(true);

        getChildren().addAll(titleLabel, addEntryButton);

        getSkinnable().setOnMouseDragReleased(Event::consume);

        // Drag and Drop
        control.addEventFilter(DragEvent.DRAG_OVER, this::onDragOver);
        control.addEventHandler(DragEvent.DRAG_DROPPED, this::onDragDropped);
        control.addEventHandler(DragEvent.DRAG_EXITED, this::onDragExited);
    }

    private void clearComponents() {
        for (KLComponentControl componentControl : componentControls) {
            removeComponentControl(componentControl, componentControlToSubscription.get(componentControl));
        }

        componentControls.clear();
    }

    private void resetUI() {
        clearComponents();
        getSkinnable().getValue().forEach(nid -> {
            if (nid != 0) {
                EntityProxy entityProxy = EntityProxy.make(nid);
                createComponentUI(entityProxy.nid());
            }
        });

        createComponentUI(0);
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

        // Check if dragging within CList/CSet and it's dragging back to the same position
        if (dragEvent.getGestureSource() instanceof KLComponentControl componentControl) {
            int indexOfSourceComponent = componentControls.indexOf(componentControl);

            if (currentDropIndex == indexOfSourceComponent) {
                componentControl.setVisible(true);
                componentControl.pseudoClassStateChanged(DRAGGING_TO_SAME, true);
            } else {
                componentControl.setVisible(false);
                componentControl.pseudoClassStateChanged(DRAGGING_TO_SAME, false);
            }
        }
    }

    private void updateDropTargetLocation(DragEvent dragEvent) {
        double y = dragEvent.getY();

        for (KLComponentControl componentControl : componentControls) {
            double componentExtendedBoundsLowerY = componentControl.getLayoutY() - SPACE_BETWEEN_COMPONENTS;
            double componentExtendedBoundsUpperY = componentControl.getLayoutY() + componentControl.getHeight();

            if (y >= componentExtendedBoundsLowerY && y <= componentExtendedBoundsUpperY) {
                int componentIndex = componentControls.indexOf(componentControl);

                currentDropIndex = componentIndex;

                if (componentIndex == 0) {
                    // show the top drop line
                    currentDropLine = componentToDropLine.get(null);
                    currentDropLine.setVisible(true);
                    currentDropIndex = 0;
                } else {
                    KLComponentControl previousComponentControl = componentControls.get(componentIndex - 1);

                    currentDropLine = componentToDropLine.get(previousComponentControl);
                    currentDropLine.setVisible(true);
                }

                if (previousDropLine != null && previousDropLine != currentDropLine) {
                    previousDropLine.setVisible(false);
                }
                previousDropLine = currentDropLine;

                break;
            } else {
                if (previousDropLine != null) {
                    previousDropLine.setVisible(false);
                    previousDropLine = null;
                }
            }
        }
    }

    private void onDragDropped(DragEvent dragEvent) {
        if (!(dragEvent.getGestureSource() instanceof KLComponentControl)) {
            return;
        }

        KLComponentControl componentControl = (KLComponentControl) dragEvent.getGestureSource();
        KLComponentCollectionControl<T> control = getSkinnable();
        int componentNid = componentControl.getEntity().nid();
        int indexOfSourceComponent = componentControls.indexOf(componentControl);

        if (currentDropLine != null) {
            currentDropLine.setVisible(false);
        }

        if (currentDropIndex > indexOfSourceComponent) {
            currentDropIndex = currentDropIndex - 1;
        }

        MutableIntList mutableList = createMutableIntListCopy(control.getValue());
        mutableList.remove(componentNid);
        mutableList.addAtIndex(currentDropIndex, componentNid);
        setValueFromIntList(mutableList);

        dragEvent.setDropCompleted(true);
        dragEvent.consume();
    }

    private void setValueFromIntList(MutableIntList mutableList) {
        KLComponentCollectionControl<T> control = getSkinnable();

        if (control.getValue() instanceof IntIdList) {
            ((KLComponentCollectionControl<IntIdList>)control).setValue(IntIds.list.of(mutableList.toArray()));
        } else if(control.getValue() instanceof IntIdSet) {
            ((KLComponentCollectionControl<IntIdSet>)control).setValue(IntIds.set.of(mutableList.toArray()));
        }
    }

    /**
     * Create UI for a component with the given nid.
     * A nid of 0 means create a component that's empty.
     *
     * @param nid the nid that is going to be associated with the component
     */
    private void createComponentUI(int nid) {
        KLComponentCollectionControl<T> control = getSkinnable();

        KLComponentControl componentControl = new KLComponentControl();
        if (nid != 0) {
            EntityProxy entityProxy = EntityProxy.make(nid);
            componentControl.setEntity(entityProxy);
        }

        // Setup Typeahead
        componentControl.setTypeAheadCompleter(control.getTypeAheadCompleter());
        componentControl.setTypeAheadStringConverter(control.getTypeAheadStringConverter());
        componentControl.setSuggestionsCellFactory(control.getSuggestionsCellFactory());
        componentControl.setTypeAheadHeaderPane(control.getTypeAheadHeaderPane());

        // Setup name renderer
        componentControl.setComponentNameRenderer(control.getComponentNameRenderer());

        // Setup dropping multiple concepts
        componentControl.setOnDroppingMultipleConcepts(control.getOnDroppingMultipleConcepts());

        componentControls.add(componentControl);

        Subscription subscription = componentControl.entityProperty().subscribe(() -> {
            if (!componentControl.isEmpty()) {
                int oldNidIndex = componentControls.indexOf(componentControl);
                int newNid = componentControl.getEntity().nid();

                if (oldNidIndex >= getSkinnable().getValue().size()) { // we're adding a new nid
                    MutableIntList mutableList = createMutableIntListCopy(control.getValue());
                    mutableList.add(newNid);
                    setValueFromIntList(mutableList);
                } else { // we're setting the control's valid nid to another nid
                    MutableIntList mutableList = createMutableIntListCopy(control.getValue());
                    mutableList.set(oldNidIndex, newNid);
                    setValueFromIntList(mutableList);
                }
            }
        });

        componentControlToSubscription.put(componentControl, subscription);

        componentControl.setOnRemoveAction(ev -> removeNid(nid));

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
    }

    private static MutableIntList createMutableIntListCopy(IntIdCollection intIdCollection) {
        return IntLists.mutable.wrapCopy(intIdCollection.toArray());
    }

    private Line createDropLine(KLComponentControl componentControl) {
        Line dropLine = new Line();
        dropLine.getStyleClass().add("drop-line");
        dropLine.setVisible(false);

        componentToDropLine.put(componentControl, dropLine);
        getChildren().add(dropLine);

        return dropLine;
    }

    private Label createNumberLabel(KLComponentControl componentControl) {
        Label numberLabel = new Label();
        numberLabel.getStyleClass().add("number-label");
        componentControlToNumberGraphic.put(componentControl, numberLabel);
        return numberLabel;
    }

    private void removeNid(int nidToRemove) {
        KLComponentCollectionControl<T> control = getSkinnable();

        IntIdCollection intIdList = control.getValue();
        MutableIntList mutableList = createMutableIntListCopy(intIdList);
        mutableList.remove(nidToRemove);
        setValueFromIntList(mutableList);
    }

    private void removeComponentControl(KLComponentControl componentControl, Subscription subscription) {
        subscription.unsubscribe();

        getChildren().remove(componentToDropLine.get(componentControl));
        componentToDropLine.remove(componentControl);

        getChildren().remove(componentControlToNumberGraphic.get(componentControl));
        componentControlToNumberGraphic.remove(componentControl);

        getChildren().remove(componentControl);
    }

    /** {@inheritDoc} */
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        // TODO use getInsets() instead of getPadding()
        Insets padding = getSkinnable().getPadding();
        double titlePrefWidth = titleLabel.prefWidth(-1);
        double titlePrefHeight = titleLabel.prefHeight(titlePrefWidth);
        double x = contentX + padding.getLeft();
        double y = contentY + padding.getTop();
        titleLabel.resizeRelocate(x, y, titlePrefWidth, titlePrefHeight);

        double adjustedPrefHeight = titlePrefHeight;
        double bottomPadding = titleLabel.getPadding().getBottom();

        if (bottomPadding <= SPACE_BETWEEN_COMPONENTS) {
            adjustedPrefHeight -= bottomPadding;
        } else {
            adjustedPrefHeight -= (bottomPadding - SPACE_BETWEEN_COMPONENTS);
        }

        y += adjustedPrefHeight;

        double topDropLineHeight = layoutTopDropLine(contentWidth, padding, y);

        y += topDropLineHeight;

        // rather than calculate the topDropLine width, use the same width of the dropLine for the components
        int labelNumber = 1;
        int index = 0;
        for (; index < getSkinnable().getValue().size(); ++index) {
            double componentControlUsedHeight = layoutComponentControl(contentWidth, index, padding, y,
                                                                       labelNumber);
            // prepare for next iteration
            labelNumber += 1;
            y += componentControlUsedHeight + SPACE_BETWEEN_COMPONENTS;
        }
        // layout final empty component control
        double componentControlUsedHeight = layoutComponentControl(contentWidth, index, padding, y, labelNumber);
        y += componentControlUsedHeight + SPACE_BETWEEN_COMPONENTS;

        // layout add Entry button
        double buttonPrefWidth = addEntryButton.prefWidth(-1);
        addEntryButton.resizeRelocate(contentWidth - buttonPrefWidth - padding.getRight(), y,
                                      buttonPrefWidth, addEntryButton.prefHeight(buttonPrefWidth));
    }

    private double layoutTopDropLine(double contentWidth, Insets padding, double y) {
        Line dropLine = componentToDropLine.get(null);

        return layoutDropLine(dropLine, contentWidth, padding, y);
    }

    private double layoutDropLine(Line dropLine, double contentWidth, Insets padding, double y) {
        // Subtract the padding and at least 1 to be less than the width of the content pane.
        // Using the same value as SPACE_BETWEEN_NUMBER_AND_CONTROL keeps the right component border within the
        // pane, to not be covered by the vertical scroll bar.  This was realized in final testing for the PR.
        double dropLineWidth = contentWidth - padding.getRight() - padding.getLeft() - SPACE_BETWEEN_NUMBER_AND_CONTROL;
        // center the drop line horizontally within the contentWidth
        double dropLineX = contentWidth / 2 - dropLineWidth / 2;
        // center the drop line vertically within the height, which is the Space between the components
        double dropLineY = y + SPACE_BETWEEN_COMPONENTS / 2d;

        dropLine.setStartX(dropLineX);
        dropLine.setStartY(dropLineY);
        dropLine.setEndX(dropLineX + dropLineWidth);
        dropLine.setEndY(dropLineY);

        return SPACE_BETWEEN_COMPONENTS;
    }

    /**
     * Lays out an individual component control and also the drop line associated with it in case there is any.
     *
     * @param contentWidth the total width available for laying out this component control
     * @param index the index of the control to layout
     * @param padding the padding
     * @param componentStartY the start y of the layout of the component
     * @param labelNumber the number that should be visible on the number label
     *
     * @return the height that the component control occupies after it has been laid out
     */
    private double layoutComponentControl(double contentWidth, int index, Insets padding,
                                          double componentStartY, int labelNumber) {
        KLComponentControl componentControl = componentControls.get(index);
        double componentStartX = padding.getLeft();

        Label numberLabel = componentControlToNumberGraphic.get(componentControl);
        double labelWidth = numberLabel.prefWidth(-1);
        double labelHeight = numberLabel.prefHeight(labelWidth);

        double componentControlPrefWidth = contentWidth - padding.getRight() - padding.getLeft() - SPACE_BETWEEN_NUMBER_AND_CONTROL - labelWidth;
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
            layoutDropLine(dropLine, contentWidth, padding,componentStartY + componentControlPrefHeight);
        }

        return componentControlPrefHeight;
    }

    private static String getString(String key) {
        return ResourceBundle.getBundle("dev.ikm.komet.kview.controls.component-list-control").getString(key);
    }
}
