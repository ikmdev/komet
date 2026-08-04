package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLDiTreeControlSkin;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

import java.util.function.Supplier;

/**
 * <p>KLDiTreeControl is the editable counterpart of {@link KLReadOnlyDiTreeControl}: it renders a
 * {@link DiTreeEntity} logical definition as the same axiom tree, and additionally lets the user
 * edit it in place:</p>
 *
 * <ul>
 *     <li>clicking a concept chip swaps it for an inline {@link KLComponentControl} search slot —
 *     picking a concept replaces the is-a, role type or role restriction;</li>
 *     <li>clause headers (sets, role groups) reveal an add button on hover and offer a structure
 *     context menu: add is-a / role / role group, change the set type, remove the axiom;</li>
 *     <li>new content mounts as template rows of empty search slots and is only applied to the
 *     definition once the required concepts are picked;</li>
 *     <li>an add-set affordance below the tree starts a definition from scratch when the field has
 *     no value yet.</li>
 * </ul>
 *
 * <p>Every edit produces a new {@link DiTreeEntity} in {@link #valueProperty()}; the owning field
 * binds that bidirectionally to the semantic's editable field value, so persistence follows the
 * standard save/commit flow of the hosting window.</p>
 *
 * <p>The control stays view-agnostic: inline search slots are produced by the
 * {@link #componentSlotFactoryProperty()}, which the owning field supplies with fully wired
 * {@link KLComponentControl} instances (see {@code KLDiTreeControlFactory}).</p>
 */
public class KLDiTreeControl extends KLReadOnlyDiTreeControl {

    public KLDiTreeControl() {
        getStyleClass().add("ditree-control");
        sceneProperty().subscribe(newScene -> {
            if (newScene != null) {
                String stylesheet = KLDiTreeControl.class.getResource("ditree-control.css").toExternalForm();
                if (!newScene.getStylesheets().contains(stylesheet)) {
                    newScene.getStylesheets().add(stylesheet);
                }
            }
        });
    }

    // -- property set seed nids
    /**
     * The initial property concept a newly added property set is seeded with (the classic axiom
     * control seeds SNOMED's "Concept model object attribute"). Resolved by the owning factory;
     * while unset (zero) the add-property-set items are disabled.
     */
    private final IntegerProperty propertySetSeedNid = new SimpleIntegerProperty(this, "propertySetSeedNid");
    public final int getPropertySetSeedNid() { return propertySetSeedNid.get(); }
    public final IntegerProperty propertySetSeedNidProperty() { return propertySetSeedNid; }
    public final void setPropertySetSeedNid(int nid) { propertySetSeedNid.set(nid); }

    /**
     * The initial property concept a newly added data or interval property set is seeded with
     * (the classic axiom control seeds SNOMED's "Concept model data attribute"). Resolved by the
     * owning factory; while unset (zero) the corresponding add items are disabled.
     */
    private final IntegerProperty dataPropertySetSeedNid = new SimpleIntegerProperty(this, "dataPropertySetSeedNid");
    public final int getDataPropertySetSeedNid() { return dataPropertySetSeedNid.get(); }
    public final IntegerProperty dataPropertySetSeedNidProperty() { return dataPropertySetSeedNid; }
    public final void setDataPropertySetSeedNid(int nid) { dataPropertySetSeedNid.set(nid); }

    // -- component slot factory
    /**
     * Produces a fully wired {@link KLComponentControl} (type-ahead completer, name renderer,
     * suggestion cells) each time the skin needs an inline search slot. Until this is set the
     * control renders read-only.
     */
    private final ObjectProperty<Supplier<KLComponentControl>> componentSlotFactory =
            new SimpleObjectProperty<>(this, "componentSlotFactory");
    public final Supplier<KLComponentControl> getComponentSlotFactory() { return componentSlotFactory.get(); }
    public final ObjectProperty<Supplier<KLComponentControl>> componentSlotFactoryProperty() { return componentSlotFactory; }
    public final void setComponentSlotFactory(Supplier<KLComponentControl> factory) { componentSlotFactory.set(factory); }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLDiTreeControlSkin(this);
    }
}