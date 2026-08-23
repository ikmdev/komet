package dev.ikm.komet.kview.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;

import java.util.function.Consumer;

public abstract class KLReadOnlyMultiComponentControl extends KLReadOnlyBaseControl {
    public static final PseudoClass EDIT_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("edit-mode");

    /**
     * Each component's description is rendered in a wrapping node, so this control's height depends
     * on the width it is given. Neither {@code Control} nor {@code SkinBase} reports a content bias,
     * so without this override parents would size the control with {@code prefHeight(-1)} and only
     * ever make room for a single line of text per component.
     *
     * @return {@link Orientation#HORIZONTAL}
     */
    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    // -- on remove action
    private ObjectProperty<Consumer<ComponentItem>> onRemoveAction = new SimpleObjectProperty<>();
    public Consumer<ComponentItem> getOnRemoveAction() { return onRemoveAction.get(); }
    public ObjectProperty<Consumer<ComponentItem>> onRemoveActionProperty() { return onRemoveAction; }
    public void setOnRemoveAction(Consumer<ComponentItem> onEditAction) { this.onRemoveAction.set(onEditAction); }

    // -- on populate action
    private ObjectProperty<Consumer<Integer>> onPopulateAction = new SimpleObjectProperty<>();
    public Consumer<Integer> getOnPopulateAction() { return onPopulateAction.get(); }
    public ObjectProperty<Consumer<Integer>> onPopulateActionProperty() { return onPopulateAction; }
    public void setOnPopulateAction(Consumer<Integer> onPopulateAction) { this.onPopulateAction.set(onPopulateAction); }
}