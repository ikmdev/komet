package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.controls.KonceptBadge;
import dev.ikm.komet.framework.controls.KonceptKindResolver;
import dev.ikm.komet.framework.controls.KonceptStatus;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import network.ike.docs.konceptcore.KonceptKind;

/**
 * Creates {@link ComponentItemNode}s wired to the rest of the app — the counterpart of
 * {@link KLComponentControlFactory} for rendered components. The node itself is a pure UI control
 * that renders whatever component kind and taxonomic status it is given; nodes obtained here
 * resolve both from the rendered component's {@link PublicId} through the store and the default
 * view calculator (the same overload {@link ComponentItemActions#setDragView} uses, so the glyphs
 * match the pattern navigator's {@link KonceptBadge} for the same component), re-resolving
 * whenever the node's component changes.
 *
 * <p>The one-mark rule (ike-issues#742/#861) is applied here: only a bare concept carries a
 * status cluster — a kind sigil and a status mark never co-occur.
 */
public final class ComponentItemNodeFactory {

    private ComponentItemNodeFactory() {
    }

    /**
     * Creates a node with glyph resolution attached, for a host that supplies the component later
     * through {@link ComponentItemNode#setComponentItem(ComponentItem)}.
     *
     * @return the wired node
     */
    public static ComponentItemNode create() {
        ComponentItemNode componentItemNode = new ComponentItemNode();
        attachGlyphResolution(componentItemNode);
        return componentItemNode;
    }

    /**
     * Creates a node rendering the given component, with glyph resolution attached.
     *
     * @param componentItem the component to render
     * @return the wired node
     */
    public static ComponentItemNode create(ComponentItem componentItem) {
        ComponentItemNode componentItemNode = new ComponentItemNode(componentItem);
        attachGlyphResolution(componentItemNode);
        return componentItemNode;
    }

    /**
     * Attaches glyph resolution to a node this factory did not construct — the FXML case, where
     * the loader instantiates the node. Resolves the node's current component now, and again
     * whenever its {@link ComponentItemNode#componentItemProperty() componentItem} changes. Call
     * once per node; the wiring lives as long as the node's property does, so there is nothing to
     * dispose.
     *
     * @param componentItemNode the node to keep resolved
     */
    public static void attachGlyphResolution(ComponentItemNode componentItemNode) {
        componentItemNode.componentItemProperty()
                .subscribe(componentItem -> updateGlyphs(componentItemNode, componentItem));
    }

    /**
     * Resolves the component's kind and status and pushes them into the node. A component without
     * a {@link PublicId}, or a node rendered before the store is running (Scene Builder, a plain
     * control test), resolves to the bare defaults — no glyphs.
     */
    private static void updateGlyphs(ComponentItemNode componentItemNode, ComponentItem componentItem) {
        PublicId publicId = componentItem == null ? null : componentItem.getPublicId();
        if (publicId == null || !PrimitiveData.running()) {
            componentItemNode.setKonceptKind(KonceptKind.CONCEPT);
            componentItemNode.setKonceptStatus(KonceptStatus.NONE);
            return;
        }

        ViewCalculator calculator = Calculators.View.Default();
        int nid = PrimitiveData.nid(publicId);
        KonceptKind kind = KonceptKindResolver.resolve(nid, calculator);
        componentItemNode.setKonceptKind(kind);
        componentItemNode.setKonceptStatus(kind == KonceptKind.CONCEPT
                ? KonceptBadge.computeStatus(nid, calculator, PremiseType.INFERRED)
                : KonceptStatus.NONE);
    }
}