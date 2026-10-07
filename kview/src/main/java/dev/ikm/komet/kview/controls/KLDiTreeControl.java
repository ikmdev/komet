package dev.ikm.komet.kview.controls;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.LongProperty;
import dev.ikm.komet.kview.controls.skin.KLDiTreeControlSkin;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

import java.util.List;
import java.util.function.Function;
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

    // -- rule actions provider
    /**
     * Provides the structure actions for a clause vertex, sourced from the axiom rules engine —
     * the same rules that drive the classic axiom control's context menu. A null vertex stands
     * for the definition root (also when no definition exists yet). Wired by the owning field;
     * while unset the skin falls back to its built-in action catalog.
     */
    private final ObjectProperty<Function<EntityVertex, List<AxiomRuleAction>>> ruleActionsProvider = new SimpleObjectProperty<>(this, "ruleActionsProvider");
    public final Function<EntityVertex, List<AxiomRuleAction>> getRuleActionsProvider() { return ruleActionsProvider.get(); }
    public final ObjectProperty<Function<EntityVertex, List<AxiomRuleAction>>> ruleActionsProviderProperty() { return ruleActionsProvider; }
    public final void setRuleActionsProvider(Function<EntityVertex, List<AxiomRuleAction>> provider) { ruleActionsProvider.set(provider); }

    // -- property set seed nids
    /**
     * The initial property concept a newly added property set is seeded with (the classic axiom
     * control seeds SNOMED's "Concept model object attribute"). Resolved by the owning factory;
     * while unset (zero) the add-property-set items are disabled.
     */
    private final LongProperty propertySetSeedNid = new SimpleLongProperty(this, "propertySetSeedNid");
    public final long getPropertySetSeedNid() { return propertySetSeedNid.get(); }
    public final LongProperty propertySetSeedNidProperty() { return propertySetSeedNid; }
    public final void setPropertySetSeedNid(long nid) { propertySetSeedNid.set(nid); }

    /**
     * The initial property concept a newly added data or interval property set is seeded with
     * (the classic axiom control seeds SNOMED's "Concept model data attribute"). Resolved by the
     * owning factory; while unset (zero) the corresponding add items are disabled.
     */
    private final LongProperty dataPropertySetSeedNid = new SimpleLongProperty(this, "dataPropertySetSeedNid");
    public final long getDataPropertySetSeedNid() { return dataPropertySetSeedNid.get(); }
    public final LongProperty dataPropertySetSeedNidProperty() { return dataPropertySetSeedNid; }
    public final void setDataPropertySetSeedNid(long nid) { dataPropertySetSeedNid.set(nid); }

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