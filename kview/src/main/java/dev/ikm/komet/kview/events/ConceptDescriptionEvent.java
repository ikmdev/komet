package dev.ikm.komet.kview.events;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.PublicId;

public class ConceptDescriptionEvent extends Evt {

    public static final EvtType<ConceptDescriptionEvent> CONCEPT_DESCRIPTION_EVENT = new EvtType<>(Evt.ANY, "CONCEPT_DESCRIPTION_EVENT");
    public static final EvtType<ConceptDescriptionEvent> ADD_CONCEPT_FQN = new EvtType<>(CONCEPT_DESCRIPTION_EVENT, "ADD_CONCEPT_FQN");
    public static final EvtType<ConceptDescriptionEvent> EDIT_CONCEPT_FQN = new EvtType<>(CONCEPT_DESCRIPTION_EVENT, "EDIT_CONCEPT_FQN");
    public static final EvtType<ConceptDescriptionEvent> ADD_CONCEPT_OTHER_NAME = new EvtType<>(CONCEPT_DESCRIPTION_EVENT, "ADD_CONCEPT_OTHER_NAME");
    public static final EvtType<ConceptDescriptionEvent> EDIT_CONCEPT_OTHER_NAME = new EvtType<>(CONCEPT_DESCRIPTION_EVENT, "EDIT_CONCEPT_OTHER_NAME");

    private PublicId publicId;
    private ViewProperties viewProperties;

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @param publicId the public id that has to be edited.
     * @throws IllegalArgumentException if source is null
     */
    public ConceptDescriptionEvent(Object source, EvtType eventType, PublicId publicId, ViewProperties viewProperties) {
        super(source, eventType);
        this.publicId = publicId;
        this.viewProperties = viewProperties;
    }

    public PublicId getPublicId() {
        return publicId;
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public void setViewProperties(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }
}
