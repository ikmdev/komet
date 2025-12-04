package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.ConceptNavigatorHelper;
import dev.ikm.komet.kview.controls.skin.KLConceptNavigatorTreeCellSkin;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Cell;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeCell;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Path;

import java.util.BitSet;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem.PS_STATE;
import static dev.ikm.komet.kview.controls.KLConceptNavigatorControl.MAX_LEVEL;

/**
 * <p>The {@link Cell} type used with the {@link KLConceptNavigatorControl} control.
 * </p>
 * <p>Uses a {@link ConceptFacade} as the type of the value contained within the
 * {@link #treeItemProperty() TreeItem} property.
 * </p>
 */
public class KLConceptNavigatorTreeCell extends TreeCell<ConceptFacade> {

    /**
     * Used to access internal methods of KLConceptNavigatorTreeCell from
     * {@link dev.ikm.komet.kview.controls.skin.KLConceptNavigatorTreeViewSkin}.
     */
    static {
        ConceptNavigatorHelper.setConceptNavigatorCellAccessor(new ConceptNavigatorHelper.ConceptNavigatorCellAccessor() {

            @Override
            public void markCellDirty(KLConceptNavigatorTreeCell treeCell) {
                treeCell.markCellDirty();
            }

            @Override
            public void unselectItem(KLConceptNavigatorTreeCell treeCell) {
                treeCell.unselectItem();
            }

        });
    }

    /**
     * <p>Several pseudoClasses definitions for connecting lines.
     * </p>
     * @see PS_STATE
     */
    public static final PseudoClass LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-long-hover");
    private static final PseudoClass BORDER_LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-border-long-hover");
    private static final PseudoClass BORDER_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-border-selected");

    private static final PseudoClass CURVED_LINE_LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-curved-line-long-hover");
    private static final PseudoClass CURVED_LINE_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-curved-line-selected");
    public static final PseudoClass[] LINE_I_LONG_HOVER_PSEUDO_CLASS;
    public static final PseudoClass[] LINE_I_SELECTED_PSEUDO_CLASS;
    static {
        LINE_I_LONG_HOVER_PSEUDO_CLASS = new PseudoClass[MAX_LEVEL];
        LINE_I_SELECTED_PSEUDO_CLASS = new PseudoClass[MAX_LEVEL];
        for (int i = 0; i < MAX_LEVEL; i++) {
            LINE_I_LONG_HOVER_PSEUDO_CLASS[i] = PseudoClass.getPseudoClass("cn-line-long-hover-" + i);
            LINE_I_SELECTED_PSEUDO_CLASS[i] = PseudoClass.getPseudoClass("cn-line-selected-" + i);
        }
    }

    /**
     * <p>PseudoClasses related to the {@link LineageBox}. When the viewLineage property of the
     * concept navigator tree item is set to true, the cell has to accommodate some space to
     * show the lineage box, and apply some visual changes to the {@link ConceptTile}.
     * </p>
     * @see #viewLineageProperty()
     */
    private static final PseudoClass SHOW_LINEAGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("show-lineage");

    /**
     * <p>PseudoClasses for tags
     * </p>
     * @see ConceptNavigatorTreeItem#tagProperty()
     */
    private static final PseudoClass ADDED_TAG_PSEUDO_CLASS = PseudoClass.getPseudoClass("added-tag");
    private static final PseudoClass RETIRED_TAG_PSEUDO_CLASS = PseudoClass.getPseudoClass("retired-tag");

    private static final PseudoClass HIGHLIGHTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("highlighted");
    /**
     * <p>Custom data format identifier used as means of identifying the data stored on a dragboard
     * when one or more concept items are selected and dragged to a given target.
     * </p>
     * <p>For each item, a list of UUID[] is added to the dragboard, from
     * <pre><code>conceptFacade.publicId().asUuidArray()</code></pre>
     * </p>
     */
    public static final DataFormat CONCEPT_NAVIGATOR_DRAG_FORMAT;
    static {
        DataFormat dataFormat = DataFormat.lookupMimeType("object/concept-navigator-format");
        CONCEPT_NAVIGATOR_DRAG_FORMAT = dataFormat == null ? new DataFormat("object/concept-navigator-format") : dataFormat;
    }

    private final ConceptTile conceptTile;
    private final KLConceptNavigatorControl treeView;

    /**
     * <p>Creates a KLConceptNavigatorTreeCell instance.
     * </p>
     * <p>A {@link ConceptTile} instance is created as well. It will be set as the
     * graphic node for this cell.
     * </p>
     * <p>The default disclosure node of the {@link TreeCell} is removed, since the
     * conceptTile provides a custom one.
     * </p>
     * <p>A double click event on this cell is processed passing its concept to the
     * {@link KLConceptNavigatorControl#onActionProperty()}.
     * </p>
     * @param treeView the {@link KLConceptNavigatorControl} that holds this cell
     */
    public KLConceptNavigatorTreeCell(KLConceptNavigatorControl treeView) {
        this.treeView = treeView;
        conceptTile = new ConceptTile(this, treeView);

        disclosureNodeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (getDisclosureNode() != null) {
                    setDisclosureNode(null);
                    disclosureNodeProperty().removeListener(this);
                }
            }
        });
        setText(null);

        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !isEmpty() && !isViewLineage()) {
                if (treeView.getOnAction() != null) {
                    Consumer<ConceptFacade> consumer = treeView.getOnAction().apply(KLConceptNavigatorControl.CONTEXT_MENU_ACTION.OPEN_IN_WORKSPACE);
                    if (consumer != null) {
                        consumer.accept(getItem());
                    }
                }
                e.consume();
            }
        });

        getStyleClass().add("navigator-tree-cell");
    }

    /**
     * <p>Updates the index associated with this IndexedCell.
     * </p>
     * <p>This is overridden to force a cleanup of discarded cells
     * </p>
     * @param newIndex the index associated with this indexed cell
     */
    @Override
    public void updateIndex(int newIndex) {
        super.updateIndex(newIndex);
        if (newIndex == -1) {
            cleanup();
        }
    }

    /**
     * <p>Invoked during the layout pass to layout the children in this parent.
     * </p>
     * <p>This is overridden to force a call to {@link #updateItem(ConceptFacade, boolean)} when
     * the cell is marked dirty.
     * </p>
     */
    @Override
    protected void layoutChildren() {
        if (itemDirty) {
            updateItem(getItem(), isEmpty());
            itemDirty = false;
        }
        super.layoutChildren();
    }

    /**
     * <p>Set the {@link ConceptTile} as graphic for not empty cells, with an
     * updated {@link ConceptFacade}, updating the pseudoClasses state of this
     * cell, and its tooltip.
     * </p>
     * @param item The new item for the cell.
     * @param empty whether or not this cell represents data from the list. If it
     *        is empty, then it does not represent any domain data, but is a cell
     *        being used to render an "empty" row.
     */
    @Override
    protected void updateItem(ConceptFacade item, boolean empty) {
        super.updateItem(item, empty);
        getPseudoClassStates().stream()
                .filter(p -> p.getPseudoClassName().startsWith("cn-"))
                .forEach(p -> pseudoClassStateChanged(p, false));
        if (item != null && !empty) {
            ConceptNavigatorTreeItem model = (ConceptNavigatorTreeItem) getTreeItem();
            conceptTile.setConcept(model);
            setGraphic(conceptTile);
            updateState(model.getBitSet());
            conceptTile.updateTooltip();
        } else {
            cleanup();
            setGraphic(null);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLConceptNavigatorTreeCellSkin(this);
    }

    /**
     * <p>Boolean property that toggles the 'show-lineage' pseudoClass, related to the {@link LineageBox}.
     * </p>
     * @see ConceptNavigatorTreeItem#viewLineageProperty()
     */
    private final BooleanProperty viewLineageProperty = new SimpleBooleanProperty(this, "viewLineage") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SHOW_LINEAGE_PSEUDO_CLASS, get());
        }
    };
    public final BooleanProperty viewLineageProperty() {
        return viewLineageProperty;
    }
    public final boolean isViewLineage() {
        return viewLineageProperty.get();
    }
    public final void setViewLineage(boolean value) {
        viewLineageProperty.set(value);
    }

    /**
     * <p>Property that toggles the 'added-tag' and 'retired-tag' pseudoClasses, when the {@link ConceptNavigatorTreeItem.TAG}
     * for the concept associated to this treeCell is set.
     * </p>
     * @see ConceptNavigatorTreeItem#tagProperty()
     */
    private final ObjectProperty<ConceptNavigatorTreeItem.TAG> tagProperty = new SimpleObjectProperty<>(this, "tag", ConceptNavigatorTreeItem.TAG.NONE) {
        @Override
        protected void invalidated() {
            ConceptNavigatorTreeItem.TAG tag = get();
            pseudoClassStateChanged(ADDED_TAG_PSEUDO_CLASS, tag == ConceptNavigatorTreeItem.TAG.ADDED);
            pseudoClassStateChanged(RETIRED_TAG_PSEUDO_CLASS, tag == ConceptNavigatorTreeItem.TAG.RETIRED);
        }
    };
    public final ObjectProperty<ConceptNavigatorTreeItem.TAG> tagProperty() {
        return tagProperty;
    }
    public final ConceptNavigatorTreeItem.TAG getTag() {
        return tagProperty.get();
    }
    public final void setTag(ConceptNavigatorTreeItem.TAG value) {
        tagProperty.set(value);
    }

    /**
     * <p>Property that toggles the 'highlighted' pseudo class.
     * </p>
     * @see ConceptNavigatorTreeItem#highlightedProperty()
     */
    private final BooleanProperty highlightedProperty = new SimpleBooleanProperty(this, "highlighted") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(HIGHLIGHTED_PSEUDO_CLASS, get());
        }
    };
    public final BooleanProperty highlightedProperty() {
        return highlightedProperty;
    }
    public final boolean isHighlighted() {
        return highlightedProperty.get();
    }
    public final void setHighlighted(boolean value) {
        highlightedProperty.set(value);
    }

    private void unselectItem() {
        if (conceptTile != null) {
            conceptTile.stopHoverTransition();
        }
    }

    private void cleanup() {
        if (conceptTile != null) {
            conceptTile.cleanup();
        }
        getChildren().removeIf(Path.class::isInstance);
        getPseudoClassStates().stream()
                .filter(p -> p.getPseudoClassName().startsWith("cn-"))
                .forEach(p -> pseudoClassStateChanged(p, false));
    }

    private boolean itemDirty = false;
    private void markCellDirty() {
        itemDirty = true;
        requestLayout();
    }

    private void updateState(BitSet bitSet) {
        pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, bitSet.get(PS_STATE.LONG_HOVER.getBit()));
        pseudoClassStateChanged(BORDER_LONG_HOVER_PSEUDO_CLASS, bitSet.get(PS_STATE.BORDER_LONG_HOVER.getBit()));
        pseudoClassStateChanged(BORDER_SELECTED_PSEUDO_CLASS, bitSet.get(PS_STATE.BORDER_SELECTED.getBit()));
        pseudoClassStateChanged(CURVED_LINE_LONG_HOVER_PSEUDO_CLASS, bitSet.get(PS_STATE.CURVED_LINE_LONG_HOVER.getBit()));
        pseudoClassStateChanged(CURVED_LINE_SELECTED_PSEUDO_CLASS, bitSet.get(PS_STATE.CURVED_LINE_SELECTED.getBit()));
        for (int i = 0; i < Math.min(getLevel((ConceptNavigatorTreeItem) getTreeItem()), MAX_LEVEL); i++) {
            pseudoClassStateChanged(LINE_I_LONG_HOVER_PSEUDO_CLASS[i], bitSet.get(PS_STATE.LINE_I_LONG_HOVER.getBit() + i));
            pseudoClassStateChanged(LINE_I_SELECTED_PSEUDO_CLASS[i], bitSet.get(PS_STATE.LINE_I_SELECTED.getBit() + i));
        }
    }

    private int getLevel(ConceptNavigatorTreeItem model) {
        return treeView.getTreeItemLevel(model) - (treeView.isShowRoot() ? 0 : 1);
    }
}
