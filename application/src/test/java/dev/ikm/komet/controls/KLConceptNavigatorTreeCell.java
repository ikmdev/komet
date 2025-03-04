package dev.ikm.komet.controls;

import dev.ikm.komet.controls.skin.KLConceptNavigatorTreeCellSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Path;

import java.util.BitSet;
import java.util.List;

import static dev.ikm.komet.controls.ConceptNavigatorModel.PS_STATE;
import static dev.ikm.komet.controls.ConceptNavigatorModel.MAX_LEVEL;

public class KLConceptNavigatorTreeCell extends TreeCell<ConceptNavigatorModel> {

    public static final PseudoClass LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-long-hover");
    private static final PseudoClass BORDER_LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-border-long-hover");
    private static final PseudoClass BORDER_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-border-selected");
    private static final PseudoClass EXPAND_CONCEPT_PSEUDO_CLASS = PseudoClass.getPseudoClass("expand-concept");

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

    public static final DataFormat CONCEPT_NAVIGATOR_DRAG_FORMAT;
    static {
        DataFormat dataFormat = DataFormat.lookupMimeType("object/concept-navigator-format");
        CONCEPT_NAVIGATOR_DRAG_FORMAT = dataFormat == null ? new DataFormat("object/concept-navigator-format") : dataFormat;
    }

    private final ConceptTile conceptTile;
    private final KLConceptNavigatorControl treeView;

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
            if (e.getClickCount() == 2 && !isEmpty()) {
                if (treeView.getOnAction() != null) {
                    treeView.getOnAction().accept(List.of(getItem()));
                }
                e.consume();
            }
        });
    }

    public void unselectItem() {
        if (conceptTile != null) {
            conceptTile.unselectItem();
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

    @Override
    public void updateIndex(int newIndex) {
        super.updateIndex(newIndex);
        if (newIndex == -1) {
            cleanup();
        }
    }

    private boolean itemDirty = false;
    public final void markCellDirty() {
        itemDirty = true;
        requestLayout();
    }

    @Override
    protected void layoutChildren() {
        if (itemDirty) {
            updateItem(getItem(), isEmpty());
            itemDirty = false;
        }
        super.layoutChildren();
    }

    @Override
    protected void updateItem(ConceptNavigatorModel item, boolean empty) {
        super.updateItem(item, empty);
        getPseudoClassStates().stream()
                .filter(p -> p.getPseudoClassName().startsWith("cn-"))
                .forEach(p -> pseudoClassStateChanged(p, false));
        if (item != null && !empty) {
            conceptTile.setConcept(item);
            setGraphic(conceptTile);
            updateState(item.getBitSet());
            conceptTile.updateTooltip();
        } else {
            cleanup();
            setGraphic(null);
        }
    }

    private void updateState(BitSet bitSet) {
        pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, bitSet.get(PS_STATE.LONG_HOVER.getBit()));
        pseudoClassStateChanged(BORDER_LONG_HOVER_PSEUDO_CLASS, bitSet.get(PS_STATE.BORDER_LONG_HOVER.getBit()));
        pseudoClassStateChanged(BORDER_SELECTED_PSEUDO_CLASS, bitSet.get(PS_STATE.BORDER_SELECTED.getBit()));
        pseudoClassStateChanged(CURVED_LINE_LONG_HOVER_PSEUDO_CLASS, bitSet.get(PS_STATE.CURVED_LINE_LONG_HOVER.getBit()));
        pseudoClassStateChanged(CURVED_LINE_SELECTED_PSEUDO_CLASS, bitSet.get(PS_STATE.CURVED_LINE_SELECTED.getBit()));
        for (int i = 0; i < Math.min(getLevel(getTreeItem()), MAX_LEVEL); i++) {
            pseudoClassStateChanged(LINE_I_LONG_HOVER_PSEUDO_CLASS[i], bitSet.get(PS_STATE.LINE_I_LONG_HOVER.getBit() + i));
            pseudoClassStateChanged(LINE_I_SELECTED_PSEUDO_CLASS[i], bitSet.get(PS_STATE.LINE_I_SELECTED.getBit() + i));
        }
    }

    private int getLevel(TreeItem<ConceptNavigatorModel> treeItem) {
        return treeView.getTreeItemLevel(treeItem) - (treeView.isShowRoot() ? 0 : 1);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLConceptNavigatorTreeCellSkin(this);
    }

    // expandedProperty
    private final BooleanProperty expandedProperty = new SimpleBooleanProperty(this, "expanded") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(EXPAND_CONCEPT_PSEUDO_CLASS, get());
        }
    };
    public final BooleanProperty expandedProperty() {
       return expandedProperty;
    }
    public final boolean isExpanded() {
       return expandedProperty.get();
    }
    public final void setExpanded(boolean value) {
        expandedProperty.set(value);
    }

}
