package dev.ikm.komet.controls;

import dev.ikm.komet.controls.skin.KLConceptNavigatorTreeCellSkin;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeCell;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Path;

import java.util.BitSet;
import java.util.List;

import static dev.ikm.komet.controls.ConceptNavigatorTreeItem.PS_STATE;
import static dev.ikm.komet.controls.KLConceptNavigatorControl.MAX_LEVEL;

public class KLConceptNavigatorTreeCell extends TreeCell<ConceptFacade> {

    public static final PseudoClass LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-long-hover");
    private static final PseudoClass BORDER_LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-border-long-hover");
    private static final PseudoClass BORDER_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-border-selected");
    private static final PseudoClass SHOW_LINEAGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("show-lineage");

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
    private static final PseudoClass ADDED_TAG_PSEUDO_CLASS = PseudoClass.getPseudoClass("added-tag");
    private static final PseudoClass RETIRED_TAG_PSEUDO_CLASS = PseudoClass.getPseudoClass("retired-tag");

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
            if (e.getClickCount() == 2 && !isEmpty() && !isViewLineage()) {
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

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLConceptNavigatorTreeCellSkin(this);
    }

    // viewLineageProperty
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


    // tagProperty
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
}
