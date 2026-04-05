package dev.ikm.komet.kview.events.genpurpose;

import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;

/**
 * An event used for the KL Windows (or GenPurpose...).
 */
public class KLPropertyPanelEvent extends Evt {
    public static final EvtType<KLPropertyPanelEvent> OPEN_PANEL = new EvtType<>(Evt.ANY, "OPEN_PANEL");

    public static final EvtType<KLPropertyPanelEvent> CLOSE_PANEL = new EvtType<>(Evt.ANY, "CLOSE_PANEL");

    /* EVENT */
    public static final EvtType<KLPropertyPanelEvent> SHOW_PANEL = new EvtType<>(Evt.ANY, "SHOW_PANEL");
    public static final EvtType<KLPropertyPanelEvent> SHOW_EDIT_SEMANTIC_FIELDS = new EvtType<>(SHOW_PANEL, "SHOW_EDIT_SEMANTIC_FIELDS");
    public static final EvtType<KLPropertyPanelEvent> CONFIRMATION_PANEL = new EvtType<>(SHOW_PANEL, "CONFIRMATION_PANEL");
    public static final EvtType<KLPropertyPanelEvent> NO_SELECTION_MADE_PANEL = new EvtType<>(SHOW_PANEL, "NO_SELECTION_MADE_PANEL");

    /*** private variables ***/
    private SemanticEntity<SemanticEntityVersion> semanticEntity;
    private PatternFacade patternFacade;
    private EntityFacade referenceComponent;

    /**
     *
     *
     * @param source        the object on which the Event initially occurred
     * @param eventType     type of the event
     */
    public KLPropertyPanelEvent(Object source, EvtType<KLPropertyPanelEvent> eventType) {
        super(source, eventType);
    }

    /**
     * Creates a KLPropertyPanelEvent that receives a semantic. Typically used with EvtType SHOW_EDIT_SEMANTIC_FIELDS in
     * which case the semanticEntity passed in is the semantic to edit.
     *
     * @param source the source of the event
     * @param eventType the event type
     * @param semanticEntity the Semantic to edit
     */
    public KLPropertyPanelEvent(Object source, EvtType<KLPropertyPanelEvent> eventType, SemanticEntity<SemanticEntityVersion> semanticEntity) {
        super(source, eventType);
        this.semanticEntity = semanticEntity;
    }

    /**
     * Creates a KLPropertyPanelEvent that receives a Reference Component and Pattern. Typically used with EvtType SHOW_ADD_SEMANTIC
     * in which case the referenceComponent and the pattern passed in are the Reference Component and the Pattern of the
     * Semantic that is to be created.
     *
     * @param source the source of the event
     * @param eventType the event type
     * @param referenceComponent the Reference Component of the Semantic to create
     * @param pattern the Pattern of the Semantic to create
     */
    public KLPropertyPanelEvent(Object source, EvtType<KLPropertyPanelEvent> eventType, EntityFacade referenceComponent, PatternFacade pattern) {
        super(source, eventType);
        this.referenceComponent = referenceComponent;
        this.patternFacade = pattern;
    }

    public SemanticEntity<SemanticEntityVersion> getSemantic() {
        return semanticEntity;
    }

    public PatternFacade getPatternFacade() { return patternFacade; }

    public EntityFacade getReferenceComponent() { return referenceComponent; }
}