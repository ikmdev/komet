package dev.ikm.komet.kview.events.genpurpose;

import dev.ikm.komet.framework.observable.ObservableComposer;

import dev.ikm.komet.layout.editor.model.EditorPatternModel;
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

    /**
     * Shows the pattern's defaults semantic in the DEFAULTS tab's edit form — sent by the window
     * when the tab is selected or the toolbar's field-defaults button pressed. Carries the
     * defaults semantic, the composer it is edited through and the form title.
     */
    public static final EvtType<KLPropertyPanelEvent> SHOW_PATTERN_FIELD_DEFAULTS = new EvtType<>(SHOW_PANEL, "SHOW_PATTERN_FIELD_DEFAULTS");

    /*** private variables ***/
    private SemanticEntity<SemanticEntityVersion> semanticEntity;
    private EditorPatternModel editorPatternModel;
    private PatternFacade patternFacade;
    private EntityFacade referenceComponent;
    private ObservableComposer composer;
    private String formTitle;

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
     * Creates a KLPropertyPanelEvent that receives the semantic to edit along with the KL Editor model
     * of the Pattern it is edited as, whose fields carry how the semantic's fields were authored to
     * behave (e.g. whether they can still be edited in edit mode). Typically used with EvtType
     * SHOW_EDIT_SEMANTIC_FIELDS.
     *
     * @param source the source of the event
     * @param eventType the event type
     * @param semanticEntity the Semantic to edit
     * @param editorPatternModel the KL Editor model of the Pattern the Semantic is edited as
     */
    public KLPropertyPanelEvent(Object source, EvtType<KLPropertyPanelEvent> eventType, SemanticEntity<SemanticEntityVersion> semanticEntity,
                                EditorPatternModel editorPatternModel) {
        super(source, eventType);
        this.semanticEntity = semanticEntity;
        this.editorPatternModel = editorPatternModel;
    }

    /**
     * Shows the edit form for a semantic that is composed outside the window's own composer — a
     * pattern's defaults semantic, which commits in its own module — under its own title.
     *
     * @param composer  the composer the form edits the semantic through
     * @param formTitle the form's title, in place of "Pattern Fields"
     */
    public KLPropertyPanelEvent(Object source, EvtType<KLPropertyPanelEvent> eventType, SemanticEntity<SemanticEntityVersion> semanticEntity,
                                ObservableComposer composer, String formTitle) {
        super(source, eventType);
        this.semanticEntity = semanticEntity;
        this.composer = composer;
        this.formTitle = formTitle;
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

    public EditorPatternModel getEditorPatternModel() { return editorPatternModel; }

    public PatternFacade getPatternFacade() { return patternFacade; }

    /** The composer the form edits the semantic through; null for the window's own composer. */
    public ObservableComposer getComposer() { return composer; }

    /** The form's title; null for the default one. */
    public String getFormTitle() { return formTitle; }

    public EntityFacade getReferenceComponent() { return referenceComponent; }
}