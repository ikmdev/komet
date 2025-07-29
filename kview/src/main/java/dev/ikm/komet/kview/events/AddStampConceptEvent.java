package dev.ikm.komet.kview.events;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.PublicId;

public class AddStampConceptEvent extends Evt {

    public static final EvtType<EditOtherNameConceptEvent> ADD_STAMP = new EvtType<>(Evt.ANY, "ADD_STAMP");

    private PublicId publicId;
    private ViewProperties viewProperties;

    public AddStampConceptEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }

    /**
     * Constructs a prototypical Event.
     *
     * @param source    source of the event
     * @param eventType     type of the event
     * @param viewProperties  payload needed to for the Edit Other Name Form
     */
    public AddStampConceptEvent(Object source, EvtType eventType, ViewProperties viewProperties) {
        super(source, eventType);
        this.viewProperties = viewProperties;
    }

    /**
     * Constructs a prototypical Event.
     *
     * @param source    source of the event
     * @param eventType     type of the event
     */
    public AddStampConceptEvent(Object source, EvtType eventType, PublicId publicId) {
        super(source, eventType);
        this.publicId = publicId;
    }

    public PublicId getPublicId() {
        return publicId;
    }

    public void setPublicId(PublicId publicId) {
        this.publicId = publicId;
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public void setViewProperties(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }
}