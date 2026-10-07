package dev.ikm.komet.kview.controls;

import java.util.function.LongFunction;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.LongProperty;
import dev.ikm.komet.kview.controls.skin.KLReadOnlyDiTreeControlSkin;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
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
 *
 * <p>Resting the pointer on a concept chip peeks at that concept's own stated definition in a
 * transient popover, rendered by a nested instance of this control so the peek can be continued
 * from the concepts inside it. The definition is looked up through the
 * {@link #definitionResolverProperty()}; a control without one shows no peeks.</p>
 */
public class KLReadOnlyDiTreeControl extends KLReadOnlyBaseSingleValueControl<DiTreeEntity> {

    private static final PseudoClass COMPACT_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("compact-mode");

    public KLReadOnlyDiTreeControl() {
        getStyleClass().add("read-only-ditree-control");
    }

    // -- definition resolver
    /**
     * Resolves a concept nid into the stated logical definition shown when its chip is peeked,
     * or {@code null} when the concept has none — then the chip shows no peek. Unset (the
     * default) turns peeking off altogether.
     */
    private final ObjectProperty<LongFunction<DiTreeEntity>> definitionResolver =
            new SimpleObjectProperty<>(this, "definitionResolver");
    public final LongFunction<DiTreeEntity> getDefinitionResolver() { return definitionResolver.get(); }
    public final ObjectProperty<LongFunction<DiTreeEntity>> definitionResolverProperty() { return definitionResolver; }
    public final void setDefinitionResolver(LongFunction<DiTreeEntity> resolver) { definitionResolver.set(resolver); }

    // -- compact mode
    /**
     * Whether the control renders compactly, for a host that already frames the definition:
     * no read-only marker and no wash behind the tree (the {@code :compact-mode} pseudo-class).
     * The definition peek sets this on the nested control inside its popover; the window body
     * leaves it off.
     */
    private final BooleanProperty compactMode = new SimpleBooleanProperty(this, "compactMode") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(COMPACT_MODE_PSEUDO_CLASS, get());
        }
    };
    public final boolean isCompactMode() { return compactMode.get(); }
    public final BooleanProperty compactModeProperty() { return compactMode; }
    public final void setCompactMode(boolean value) { compactMode.set(value); }

    // -- component item resolver
    /**
     * Resolves a concept nid from the tree into the {@link ComponentItem} (description, identicon,
     * public id) that its chip renders.
     */
    private final ObjectProperty<LongFunction<ComponentItem>> componentItemResolver =
            new SimpleObjectProperty<>(this, "componentItemResolver");
    public final LongFunction<ComponentItem> getComponentItemResolver() { return componentItemResolver.get(); }
    public final ObjectProperty<LongFunction<ComponentItem>> componentItemResolverProperty() { return componentItemResolver; }
    public final void setComponentItemResolver(LongFunction<ComponentItem> resolver) { componentItemResolver.set(resolver); }

    // -- root concept nid
    /**
     * The concept the definition is about (the axiom semantic's referenced component). When set
     * (non-zero), the tree renders a root row with this concept's chip and indents the definition
     * below it, like the classic axiom control; when unset the definition renders flat.
     */
    private final LongProperty rootConceptNid = new SimpleLongProperty(this, "rootConceptNid");
    public final long getRootConceptNid() { return rootConceptNid.get(); }
    public final LongProperty rootConceptNidProperty() { return rootConceptNid; }
    public final void setRootConceptNid(long nid) { rootConceptNid.set(nid); }

    // -- description resolver
    /**
     * Resolves a meaning nid into a display text, used for clause kinds that have no dedicated
     * rendering yet (features, intervals, property sets, …).
     */
    private final ObjectProperty<LongFunction<String>> descriptionResolver =
            new SimpleObjectProperty<>(this, "descriptionResolver");
    public final LongFunction<String> getDescriptionResolver() { return descriptionResolver.get(); }
    public final ObjectProperty<LongFunction<String>> descriptionResolverProperty() { return descriptionResolver; }
    public final void setDescriptionResolver(LongFunction<String> resolver) { descriptionResolver.set(resolver); }

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
