package dev.ikm.komet.kview.events.genpurpose;

import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

public class KLPropertyPanelEvent extends Evt {
    public static final EvtType<PropertyPanelEvent> OPEN_PANEL = new EvtType<>(Evt.ANY, "OPEN_PANEL");

    public static final EvtType<PropertyPanelEvent> CLOSE_PANEL = new EvtType<>(Evt.ANY, "CLOSE_PANEL");

    /* EVENT */
    public static final EvtType<PropertyPanelEvent> SHOW_PANEL = new EvtType<>(Evt.ANY, "SHOW_PANEL");
    public static final EvtType<PropertyPanelEvent> SHOW_EDIT_SEMANTIC_FIELDS = new EvtType<>(SHOW_PANEL, "SHOW_EDIT_SEMANTIC_FIELDS");
    public static final EvtType<PropertyPanelEvent> CONFIRMATION_PANEL = new EvtType<>(SHOW_PANEL, "CONFIRMATION_PANEL");
    public static final EvtType<PropertyPanelEvent> NO_SELECTION_MADE_PANEL = new EvtType<>(SHOW_PANEL, "NO_SELECTION_MADE_PANEL");
    public static final EvtType<PropertyPanelEvent> SHOW_EDIT_SINGLE_SEMANTIC_FIELD = new EvtType<>(SHOW_EDIT_SEMANTIC_FIELDS, "SHOW_EDIT_SINGLE_SEMANTIC_FIELD");
    public static final EvtType<PropertyPanelEvent> SHOW_ADD_REFERENCE_SEMANTIC_FIELD = new EvtType<>(SHOW_PANEL, "SHOW_ADD_REFERENCE_SEMANTIC_FIELD");

    /*** private variables ***/
    private EditorSectionModel editorSectionModel;

    /**
     *
     * @param source        the object on which the Event initially occurred
     * @param eventType     type of the event
     */
    public KLPropertyPanelEvent(Object source, EvtType<PropertyPanelEvent> eventType) {
        super(source, eventType);
    }

    /**
     *
     * @param source
     * @param eventType
     */
    public KLPropertyPanelEvent(Object source, EvtType<PropertyPanelEvent> eventType, EditorSectionModel sectionModel) {
        super(source, eventType);
        this.editorSectionModel = sectionModel;
    }

    public EditorSectionModel getEditorSectionModel() {
        return editorSectionModel;
    }

}