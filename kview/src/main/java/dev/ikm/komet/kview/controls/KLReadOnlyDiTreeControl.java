package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyDiTreeControlSkin;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

import java.util.function.IntFunction;

/**
 * <p>KLReadOnlyDiTreeControl renders a {@link DiTreeEntity} field value — a logical definition
 * such as the one carried by the EL++ stated or inferred terminological axiom patterns — as an
 * axiom tree: set and role-group clauses become headers with a colored accent bar, and every
 * concept in the tree becomes a component chip (identicon + name) rendered by a
 * {@link ComponentItemNode}, which contributes the standard component context menu and
 * drag-and-drop behavior.</p>
 *
 * <p>The control is view-agnostic: concept nids in the tree are resolved to displayable
 * {@link ComponentItem}s through the {@link #componentItemResolverProperty()}, and clause
 * meanings without a dedicated rendering resolve their text through the
 * {@link #descriptionResolverProperty()}. Both are expected to be supplied by the owning
 * {@code KlField} from its view calculator.</p>
 */
public class KLReadOnlyDiTreeControl extends KLReadOnlyBaseSingleValueControl<DiTreeEntity> {

    public KLReadOnlyDiTreeControl() {
        getStyleClass().add("read-only-ditree-control");
    }

    // -- component item resolver
    /**
     * Resolves a concept nid from the tree into the {@link ComponentItem} (description, identicon,
     * public id) that its chip renders.
     */
    private final ObjectProperty<IntFunction<ComponentItem>> componentItemResolver =
            new SimpleObjectProperty<>(this, "componentItemResolver");
    public final IntFunction<ComponentItem> getComponentItemResolver() { return componentItemResolver.get(); }
    public final ObjectProperty<IntFunction<ComponentItem>> componentItemResolverProperty() { return componentItemResolver; }
    public final void setComponentItemResolver(IntFunction<ComponentItem> resolver) { componentItemResolver.set(resolver); }

    // -- description resolver
    /**
     * Resolves a meaning nid into a display text, used for clause kinds that have no dedicated
     * rendering yet (features, intervals, property sets, …).
     */
    private final ObjectProperty<IntFunction<String>> descriptionResolver =
            new SimpleObjectProperty<>(this, "descriptionResolver");
    public final IntFunction<String> getDescriptionResolver() { return descriptionResolver.get(); }
    public final ObjectProperty<IntFunction<String>> descriptionResolverProperty() { return descriptionResolver; }
    public final void setDescriptionResolver(IntFunction<String> resolver) { descriptionResolver.set(resolver); }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyDiTreeControlSkin(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return KLReadOnlyDiTreeControl.class.getResource("read-only-ditree-control.css").toExternalForm();
    }
}
