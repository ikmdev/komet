/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework.controls;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.StyleClasses;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.dnd.KonceptDragSource;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * A reusable, CSS-driven badge that renders a single concept the way the AsciiDoc {@code k:}
 * Koncept chip does: the component's {@link Identicon LifeHash identicon} on the left, the
 * store-resolved name in a soft rounded small-caps pill, and a small leading status glyph that
 * preserves the taxonomic classification the navigator/axiom icon used to carry
 * (see {@link KonceptStatus}).
 *
 * <p>Because the identicon and the name are deterministic functions of the component's
 * {@link PublicId}, an on-screen badge matches the identicon and label shown for the same
 * component in generated documents (ike-issues#563). When the component's latest version is
 * inactive (retired) in the view, the name is struck through (a combining long-stroke overlay,
 * since the ellipsizing {@code Label} cannot strike through via CSS) and shown in the retired colour.
 *
 * <p>All visual treatment lives in {@code komet.css} ({@code .koncept-chip}, {@code .koncept-label},
 * {@code .koncept-status} and the {@code .koncept-defined}/{@code -primitive}/{@code -multiparent}/{@code -root}
 * colour modifiers), mirroring the AsciiDoc {@code koncept.css}; this control adds only the style
 * classes and the {@code inactive} pseudo-class. JavaFX CSS has no {@code font-variant}, so the
 * small-caps effect is approximated by upper-casing the label text and reducing its size.
 *
 * <p>The badge is a drag source (copy) carrying the component on a {@link KometClipboard}, and
 * exposes the full grounded identity (name, optional SCTID, UUID, nid) on hover. It is the shared
 * atom for the refreshed axiom tree (ike-issues#639) and the recursive semantic viewer
 * (ike-issues#641); it does not truncate the concept label.
 */
public class KonceptBadge extends HBox {

    private static final Logger LOG = LoggerFactory.getLogger(KonceptBadge.class);

    /** Pseudo-class driving the struck-through, retired-colour label when the component is inactive. */
    private static final PseudoClass INACTIVE = PseudoClass.getPseudoClass("inactive");

    /**
     * Pseudo-class a concept-expecting host enables via {@link #setConceptExpected(boolean)} to
     * escalate (for example a red border) a badge that carries a non-concept kind sigil.
     */
    private static final PseudoClass ALARM = PseudoClass.getPseudoClass("alarm");

    /** Default identicon edge length in pixels, sized to sit beside body text. */
    private static final double DEFAULT_ICON_SIZE = 14;

    /** Inline edge length (px) of the {@link ComponentKind#STAMP} pentagon sigil. */
    private static final double STAMP_SIGIL_SIZE = 14;

    /** Sentinel nid for a presentation-only badge built without a populated store/view. */
    private static final int UNKNOWN_NID = Integer.MIN_VALUE;

    private final int nid;
    private final PublicId publicId;
    private final ViewProperties viewProperties;
    private final boolean inactive;

    private final HBox sigilBox = new HBox();
    private final HBox statusBox = new HBox();
    private final ImageView identicon;
    private final Label label = new Label();

    private String conceptName;
    private String sctid;
    private PremiseType premiseType = PremiseType.INFERRED;
    private KonceptStatus status = KonceptStatus.NONE;
    private ComponentKind kind = ComponentKind.CONCEPT;
    private boolean conceptExpected = false;

    /**
     * Creates a badge for the given component, resolving its name, identicon, inactive state and
     * taxonomic status from the supplied view.
     *
     * @param entity         the component to render; its {@link EntityFacade#nid() nid} and
     *                       {@link EntityFacade#publicId() publicId} drive resolution
     * @param viewProperties the view used to resolve the name, latest state and classification
     */
    public KonceptBadge(EntityFacade entity, ViewProperties viewProperties) {
        this(entity.nid(), entity.publicId(), null, viewProperties, true);
    }

    /**
     * Creates a badge for the component with the given nid, resolving everything from the view.
     *
     * @param entityNid      the component nid
     * @param viewProperties the view used to resolve the name, latest state and classification
     */
    public KonceptBadge(int entityNid, ViewProperties viewProperties) {
        this(entityNid, PrimitiveData.publicId(entityNid), null, viewProperties, true);
    }

    /**
     * Creates a badge for the component with the given nid, optionally suppressing the status glyph
     * (for example for role values in an axiom tree, where classification is not meaningful).
     *
     * @param entityNid      the component nid
     * @param viewProperties the view used to resolve the name, latest state and classification
     * @param showStatus     {@code true} to compute and show the taxonomic status glyph
     */
    public KonceptBadge(int entityNid, ViewProperties viewProperties, boolean showStatus) {
        this(entityNid, PrimitiveData.publicId(entityNid), null, viewProperties, showStatus);
    }

    /**
     * Creates a presentation-only badge from a {@link PublicId} and a pre-resolved name, for
     * contexts without a populated store or view. Such a badge shows the identicon and label only —
     * no status glyph, inactive styling or drag source.
     *
     * @param publicId the component identifier driving the identicon and tooltip
     * @param name     the concept name to display (not truncated); may be {@code null}
     */
    public KonceptBadge(PublicId publicId, String name) {
        this(UNKNOWN_NID, publicId, name, null, false);
    }

    private KonceptBadge(int nid, PublicId publicId, String explicitName, ViewProperties viewProperties,
                         boolean showStatus) {
        this.nid = nid;
        this.publicId = publicId;
        this.viewProperties = viewProperties;
        this.inactive = computeInactive(nid, viewProperties);

        getStyleClass().add(StyleClasses.KONCEPT_CHIP.toString());

        this.identicon = (publicId != null)
                ? Identicon.generateIdenticon(publicId, (int) Math.round(DEFAULT_ICON_SIZE),
                        (int) Math.round(DEFAULT_ICON_SIZE))
                : new ImageView();
        this.identicon.setSmooth(false);
        this.identicon.getStyleClass().add(StyleClasses.KONCEPT_IDENTICON.toString());

        this.label.getStyleClass().add(StyleClasses.KONCEPT_LABEL.toString());
        // Let the label shrink and ellipsize (with the full name on the identity tooltip) so the
        // badge fits a fixed-width container without forcing a horizontal scrollbar.
        this.label.setMinWidth(0);
        this.label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(this.label, Priority.ALWAYS);
        setMaxWidth(Double.MAX_VALUE);
        setConceptName(explicitName != null ? explicitName : resolveName(nid, viewProperties));

        getChildren().addAll(sigilBox, statusBox, identicon, label);
        setStatus(showStatus && viewProperties != null && nid != UNKNOWN_NID
                ? computeStatus(nid, viewProperties, premiseType)
                : KonceptStatus.NONE);
        // Be honest about the component kind: a concept stays bare, every other kind gets its sigil
        // (a presentation-only badge, with no view to verify, stays the bare concept default).
        setKind(viewProperties != null && nid != UNKNOWN_NID
                ? ComponentKindResolver.resolve(nid, viewProperties.calculator())
                : ComponentKind.CONCEPT);

        pseudoClassStateChanged(INACTIVE, inactive);

        if (nid != UNKNOWN_NID) {
            KonceptDragSource.install(this, nid);
        }
        installTooltip();
    }

    /**
     * Sets the taxonomic status glyph shown ahead of the identicon, replacing any current glyph.
     *
     * @param status the classification to display; {@code null} is treated as {@link KonceptStatus#NONE}
     */
    public final void setStatus(KonceptStatus status) {
        this.status = (status == null) ? KonceptStatus.NONE : status;
        statusBox.getChildren().clear();
        boolean visible = this.status.hasGlyph();
        statusBox.setManaged(visible);
        statusBox.setVisible(visible);
        if (visible) {
            Text glyph = new Text(this.status.glyph());
            glyph.getStyleClass().addAll(StyleClasses.KONCEPT_STATUS.toString(), this.status.styleClass().toString());
            statusBox.getChildren().add(glyph);
            if (this.status.isMultiParent()) {
                Text fork = new Text(KonceptStatus.MULTI_PARENT_GLYPH);
                fork.getStyleClass().addAll(StyleClasses.KONCEPT_STATUS.toString(),
                        StyleClasses.KONCEPT_MULTIPARENT.toString());
                statusBox.getChildren().add(fork);
            }
        }
    }

    /**
     * The taxonomic status currently shown.
     *
     * @return the current {@link KonceptStatus} (never {@code null})
     */
    public KonceptStatus getStatus() {
        return status;
    }

    /**
     * Sets the component-kind sigil shown ahead of the identicon, replacing any current sigil
     * (ike-issues#638). A {@link ComponentKind#CONCEPT} shows no sigil (the bare default);
     * {@link ComponentKind#STAMP} shows the {@link StampSigil} pentagon; every other kind shows its
     * coloured letter glyph. The accessible kind name is installed on the sigil's tooltip — the
     * non-colour accessibility channel, so kind is never carried by colour alone.
     *
     * @param kind the component kind; {@code null} is treated as {@link ComponentKind#CONCEPT}
     */
    public final void setKind(ComponentKind kind) {
        this.kind = (kind == null) ? ComponentKind.CONCEPT : kind;
        sigilBox.getChildren().clear();
        boolean visible = this.kind.hasSigil();
        sigilBox.setManaged(visible);
        sigilBox.setVisible(visible);
        if (visible) {
            Node sigil = this.kind.isStamp() ? new StampSigil(STAMP_SIGIL_SIZE) : letterSigil(this.kind);
            Tooltip.install(sigil, new Tooltip(this.kind.accessibleName()));
            sigilBox.getChildren().add(sigil);
        }
        refreshAlarm();
    }

    /**
     * The component kind this badge is honest about.
     *
     * @return the current {@link ComponentKind} (never {@code null})
     */
    public ComponentKind getKind() {
        return kind;
    }

    /**
     * Whether this badge would violate a concept expectation — it carries a sigil, i.e. it is not a
     * bare concept — so a concept-expecting host can escalate it even without enabling the built-in
     * {@code alarm} styling.
     *
     * @return {@code true} when {@link #getKind()} is anything other than {@link ComponentKind#CONCEPT}
     */
    public boolean isConceptViolation() {
        return kind.hasSigil();
    }

    /**
     * Declares whether this badge sits in a context that <em>expects a concept</em> (a concept slot,
     * the assistant chip). When {@code true} and the badge carries any kind sigil, the badge enters
     * the {@code alarm} pseudo-class state so the host can escalate it (for example a red border).
     * The badge stays neutral by default; the host opts into the alarm.
     *
     * @param conceptExpected {@code true} if the host context requires a concept
     */
    public final void setConceptExpected(boolean conceptExpected) {
        this.conceptExpected = conceptExpected;
        refreshAlarm();
    }

    private void refreshAlarm() {
        pseudoClassStateChanged(ALARM, conceptExpected && kind.hasSigil());
    }

    private static Text letterSigil(ComponentKind kind) {
        Text glyph = new Text(kind.glyph());
        glyph.getStyleClass().add(StyleClasses.KONCEPT_SIGIL.toString());
        glyph.setFill(Color.web(kind.colorHex()));
        return glyph;
    }

    /**
     * Sets the premise type used when recomputing the taxonomic status, and recomputes it. Has no
     * effect on a presentation-only badge built without a view.
     *
     * @param premiseType the premise type ({@link PremiseType#STATED} or {@link PremiseType#INFERRED})
     */
    public void setPremiseType(PremiseType premiseType) {
        this.premiseType = premiseType;
        if (viewProperties != null && nid != UNKNOWN_NID && status != KonceptStatus.NONE) {
            setStatus(computeStatus(nid, viewProperties, premiseType));
        }
    }

    /**
     * Sets the SCTID shown in the identity tooltip and rebuilds the tooltip.
     *
     * @param sctid the SNOMED CT identifier, or {@code null} to omit it
     */
    public void setSctid(String sctid) {
        this.sctid = sctid;
        installTooltip();
    }

    /**
     * Sets the identicon edge length in pixels, regenerating the raster at that size so the
     * identicon stays crisp rather than upscaling the default {@value #DEFAULT_ICON_SIZE}px raster
     * (the {@link ImageView} has smoothing disabled). A larger badge therefore renders a sharp
     * identicon, not a blurred one.
     *
     * @param pixels the identicon width and height in pixels
     */
    public void setIconSize(double pixels) {
        identicon.setFitWidth(pixels);
        identicon.setFitHeight(pixels);
        if (publicId != null && pixels > 0) {
            int px = (int) Math.round(pixels);
            identicon.setImage(Identicon.generateIdenticon(publicId, px, px).getImage());
        }
    }

    /**
     * Whether the component's latest version is inactive (retired) in the badge's view.
     *
     * @return {@code true} if the component is inactive
     */
    public boolean isInactive() {
        return inactive;
    }

    private void setConceptName(String name) {
        this.conceptName = name;
        // JavaFX CSS has no font-variant; upper-case + reduced size approximates small-caps.
        String display = name == null ? "" : name.toUpperCase(Locale.ROOT);
        // A Label can't strike through via CSS (only Text can), and the label is a Label so it can
        // ellipsize. So for an inactive/retired concept, strike the name at the text level with a
        // combining long-stroke overlay (U+0336) on each character — a font-level strikethrough that
        // works in any font and survives ellipsis. The tooltip keeps the un-struck name.
        label.setText(inactive ? strikeThrough(display) : display);
    }

    /**
     * Returns {@code text} with a combining long-stroke overlay after each character, rendering it
     * struck through in any font.
     *
     * @param text the text to strike through
     * @return the struck-through text
     */
    private static String strikeThrough(String text) {
        StringBuilder struck = new StringBuilder(text.length() * 2);
        for (int i = 0; i < text.length(); i++) {
            struck.append(text.charAt(i)).append('̶');
        }
        return struck.toString();
    }

    private void installTooltip() {
        StringBuilder tip = new StringBuilder();
        if (inactive) {
            tip.append("Inactive — retired in this view\n");
        }
        tip.append(conceptName == null ? "" : conceptName);
        if (sctid != null) {
            tip.append("\nSCTID: ").append(sctid);
        }
        if (publicId != null) {
            tip.append("\nUUID: ").append(publicId.idString());
        }
        if (nid != UNKNOWN_NID) {
            tip.append("\nnid: ").append(nid);
        }
        Tooltip.install(this, new Tooltip(tip.toString()));
    }

    /**
     * The right edge of the identicon within this badge, in local (unscaled) coordinates —
     * used by {@link KonceptDragSource} to place the drag-view cursor just to the right of the
     * identicon so its detail stays fully visible.
     *
     * @return the identicon's right-edge x in badge-local coordinates
     */
    public double identiconRightEdge() {
        return identicon.getBoundsInParent().getMaxX();
    }

    private static String resolveName(int nid, ViewProperties viewProperties) {
        if (viewProperties == null || nid == UNKNOWN_NID) {
            return Integer.toString(nid);
        }
        // Respect the view's language coordinate (which prioritises the regular/preferred name over
        // the fully qualified name); do not force the FQN.
        return viewProperties.calculator().getDescriptionTextOrNid(nid);
    }

    private static boolean computeInactive(int nid, ViewProperties viewProperties) {
        if (viewProperties == null || nid == UNKNOWN_NID) {
            return false;
        }
        try {
            Latest<EntityVersion> latest = viewProperties.calculator().latest(nid);
            return latest.isPresent() && latest.get().stamp().state() != State.ACTIVE;
        } catch (RuntimeException e) {
            LOG.warn("Could not resolve active state for nid {}", nid, e);
            return false;
        }
    }

    /**
     * Computes the taxonomic classification glyph for a concept — the data counterpart of the
     * navigator/axiom icon produced by {@code AxiomView#computeGraphic}.
     *
     * <p>Unlike that method, which flags every non-root concept as multi-parent (its
     * {@code multiParent} test is {@code !parents.isEmpty()}, leaving the single-parent icon
     * branches unreachable), this uses {@code parents.size() > 1} so the single- and multi-parent
     * statuses are distinguished correctly.
     *
     * @param nid            the concept nid
     * @param viewProperties the view used to read parents and the logical definition
     * @param premiseType    the premise type to read the axiom tree under; falls back to
     *                       {@link PremiseType#STATED} when no tree exists for the requested premise
     * @return the classification, or {@link KonceptStatus#NONE} when the concept has no resolvable
     *         logical definition
     */
    public static KonceptStatus computeStatus(int nid, ViewProperties viewProperties, PremiseType premiseType) {
        if (nid == UNKNOWN_NID || nid == -1 || nid == TinkarTerm.UNINITIALIZED_COMPONENT.nid()) {
            return KonceptStatus.NONE;
        }
        IntIdList parents;
        try {
            parents = viewProperties.calculator().navigationCalculator().parentsOf(nid);
        } catch (RuntimeException ex) {
            LOG.warn("Could not resolve parents for nid {}", nid, ex);
            parents = IntIds.list.empty();
        }
        Latest<DiTreeEntity> definition = viewProperties.calculator().getAxiomTreeForEntity(nid, premiseType);
        if (!definition.isPresent()) {
            definition = viewProperties.calculator().getAxiomTreeForEntity(nid, PremiseType.STATED);
        }
        if (!definition.isPresent()) {
            return KonceptStatus.NONE;
        }
        if (parents.isEmpty()) {
            return KonceptStatus.ROOT;
        }
        boolean multiParent = parents.size() > 1;
        boolean sufficient = definition.get().containsVertexWithMeaning(TinkarTerm.SUFFICIENT_SET);
        if (sufficient) {
            return multiParent ? KonceptStatus.DEFINED_MULTIPARENT : KonceptStatus.DEFINED;
        }
        return multiParent ? KonceptStatus.PRIMITIVE_MULTIPARENT : KonceptStatus.PRIMITIVE;
    }
}
